package game;

import game.utils.KeyHandler;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class GameplayPanel extends JPanel implements Runnable {

    public static int width;
    public static int height;

    private Thread thread;
    private boolean running = false;

    private BufferedImage img;
    private Graphics2D bufferG;
    private Image backgroundImage;

    private KeyHandler key;
    private Game game;

    public GameplayPanel(int width, int height) throws IOException {
        GameplayPanel.width = width;
        GameplayPanel.height = height;

        setPreferredSize(new Dimension(width, height));
        setFocusable(true);

        backgroundImage = ImageIO.read(
                getClass().getClassLoader().getResource("img/background.png")
        );
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if (thread == null) {
            thread = new Thread(this, "GameThread");
            thread.start();
        }
    }

    public void init() {
        running = true;

        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        bufferG = img.createGraphics();
        bufferG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        key = new KeyHandler(this);
        addKeyListener(key);

        game = new Game();
    }

    public void updateGame() {
        game.update();
    }

    public void inputGame() {
        game.input(key);
    }

    public void renderGame() {
        if (bufferG != null) {
            // Clear screen by drawing the background
            bufferG.drawImage(backgroundImage, 0, 0, width, height, null);

            // Render game (Pacman + Ghosts)
            game.render(bufferG);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (img != null) {
            g.drawImage(img, 0, 0, null);
        }
    }

    @Override
    public void run() {
        init();

        final double GAME_HERTZ = 60.0;
        final double TBU = 1_000_000_000.0 / GAME_HERTZ;
        final int MUBR = 5;

        double lastUpdate = System.nanoTime();
        double lastRender;

        final double TARGET_FPS = 60.0;
        final double TTBR = 1_000_000_000.0 / TARGET_FPS;

        int frameCount = 0;
        int lastSecond = (int) (lastUpdate / 1_000_000_000L);

        while (running) {
            double now = System.nanoTime();
            int updates = 0;

            // Update game logic
            while ((now - lastUpdate > TBU) && (updates < MUBR)) {
                inputGame();
                updateGame();
                lastUpdate += TBU;
                updates++;
            }

            if (now - lastUpdate > TBU) {
                lastUpdate = now - TBU;
            }

            // Render to off-screen buffer
            renderGame();

            // Schedule repaint on EDT (correct Swing usage)
            repaint();

            lastRender = now;
            frameCount++;

            int thisSecond = (int) (lastUpdate / 1_000_000_000L);
            if (thisSecond > lastSecond) {
                lastSecond = thisSecond;
                frameCount = 0;
            }

            // Maintain FPS
            while (now - lastRender < TTBR) {
                Thread.yield();
                try {
                    Thread.sleep(1);
                } catch (Exception ignored) {}
                now = System.nanoTime();
            }
        }
    }
}