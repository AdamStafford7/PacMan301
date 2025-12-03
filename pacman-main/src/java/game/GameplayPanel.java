package game;

import game.utils.KeyHandler;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.*;

//Panneau de la "zone de jeu"
public class GameplayPanel extends JPanel implements Runnable {
    public static int width;
    public static int height;
    private Thread thread;
    private boolean running = false;

    private BufferedImage img;
    private Graphics2D g;
    private Image backgroundImage;

    private KeyHandler key;

    private Game game;

    public GameplayPanel(int width, int height) throws IOException {
        this.width = width;
        this.height = height;
        setPreferredSize(new Dimension(width, height));
        setFocusable(true);
        requestFocus();

        // FIX: Changed "pacman.png" to "background.png"
        // The /img/ tells Java to look in the "img" folder at the source root
        URL bgUrl = getClass().getResource("/img/background.png");
        
        if (bgUrl != null) {
            backgroundImage = ImageIO.read(bgUrl);
        } else {
            System.err.println("ERROR: Could not find background.png in /img/ folder!");
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();

        if (thread == null) {
            thread = new Thread(this, "GameThread");
            thread.start();
        }
    }

    //initialisation du jeu
    public void init() {
        running = true;
        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        g = (Graphics2D) img.getGraphics();

        key = new KeyHandler(this);

        game = new Game();
    }

    //mise à jour du jeu
    public void update() {
        game.update();
    }

    //gestion des inputs
    public void input(KeyHandler key) {
        game.input(key);
    }

    //"rendu du jeu" ; on prépare ce qui va être affiché en dessinant sur une "image" : un fond et les entités du jeu au dessus
    public void render() {
        if (g != null) {
            // Check if background loaded successfully before drawing
            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, width, height, null);
            } else {
                // Fallback: draw black background if image missing
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, width, height);
            }
            game.render(g);
        }
    }

    //Affichage du jeu : on affiche l'image avec le rendu
    public void draw() {
        Graphics g2 = this.getGraphics();
        if (g2 != null) {
            g2.drawImage(img, 0, 0, width, height, null);
            g2.dispose();
        }
    }

    @Override
    public void run() {
        init();

        //Pour faire en sorte que le jeu tourne à 60FPS (tutoriel consulté : https://www.youtube.com/watch?v=LhUN3EKZiio)
        final double GAME_HERTZ = 60.0;
        final double TBU = 1000000000 / GAME_HERTZ; //Time before update

        final int MUBR = 5; // Must update before render

        double lastUpdateTime = System.nanoTime();
        double lastRenderTime;

        final double TARGET_FPS = 60.0;
        final double TTBR = 1000000000 / TARGET_FPS; //Total time before render

        int frameCount = 0;
        int lastSecondTime = (int) (lastUpdateTime / 1000000000);
        int oldFrameCount = 0;

        while (running) {
            double now = System.nanoTime();
            int updateCount = 0;
            while ((now - lastUpdateTime) > TBU && (updateCount < MUBR)) {
                input(key);
                update();
                lastUpdateTime += TBU;
                updateCount++;
            }

            if (now - lastUpdateTime > TBU) {
                lastUpdateTime = now - TBU;
            }

            render();
            draw();
            lastRenderTime = now;
            frameCount++;

            int thisSecond = (int) (lastUpdateTime / 1000000000);
            if (thisSecond > lastSecondTime) {
                if (frameCount != oldFrameCount) {
                    //System.out.println("FPS : " + frameCount);
                    oldFrameCount = frameCount;
                }
                frameCount = 0;
                lastSecondTime = thisSecond;
            }

            while ((now - lastRenderTime < TTBR) && (now - lastUpdateTime < TBU)) {
                Thread.yield();

                try {
                    Thread.sleep(1);
                } catch (Exception e) {
                    System.err.println("ERROR yielding thread");
                }

                now = System.nanoTime();
            }
        }
    }
}