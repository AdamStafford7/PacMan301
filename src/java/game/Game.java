package game;

import game.entities.*;
import game.entities.ghosts.Blinky;
import game.entities.ghosts.Ghost;
import game.ghostFactory.*;
import game.ghostStates.EatenMode;
import game.ghostStates.FrightenedMode;
import game.utils.CollisionDetector;
import game.utils.CsvReader;
import game.utils.KeyHandler;

import java.awt.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

//Classe gérant le jeu en lui même
public class Game implements Observer {

    //Pour lister les différentes entités présentes sur la fenêtre
    private List<Entity> objects = new ArrayList<>();
    private List<Ghost> ghosts = new ArrayList<>();
    private static List<Wall> walls = new ArrayList<>();

    // NEW: 2D grid describing walls for BFS
    private static boolean[][] mazeWalls;

    private static Pacman pacman;
    private static Blinky blinky;

    private static boolean firstInput = false;

    public Game() {

        //Initialisation du jeu

        //Chargement du fichier csv du niveau
        List<List<String>> data = null;
        try {
            data = new CsvReader().parseCsv(getClass().getClassLoader().getResource("level/level.csv").toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        int cellsPerRow = data.get(0).size();
        int cellsPerColumn = data.size();
        int cellSize = 8;   // Your game uses 8px tiles!

        // Allocate the BFS wall grid
        mazeWalls = new boolean[cellsPerColumn][cellsPerRow];

        CollisionDetector collisionDetector = new CollisionDetector(this);
        AbstractGhostFactory abstractGhostFactory = null;

        //Pour chaque case du CSV, création de la bonne entité
        for (int xx = 0; xx < cellsPerRow; xx++) {
            for (int yy = 0; yy < cellsPerColumn; yy++) {

                String dataChar = data.get(yy).get(xx);

                // ---------------- WALLS ----------------
                if (dataChar.equals("x")) {
                    objects.add(new Wall(xx * cellSize, yy * cellSize));
                    mazeWalls[yy][xx] = true;   // mark wall in BFS grid
                } else {
                    mazeWalls[yy][xx] = false;  // walkable
                }

                // ---------------- PACMAN ----------------
                if (dataChar.equals("P")) {
                    pacman = new Pacman(xx * cellSize, yy * cellSize);
                    pacman.setCollisionDetector(collisionDetector);

                    pacman.registerObserver(GameLauncher.getUIPanel());
                    pacman.registerObserver(this);
                }

                // ---------------- GHOSTS ----------------
                else if (dataChar.equals("b") || dataChar.equals("p") || dataChar.equals("i") || dataChar.equals("c")) {

                    switch (dataChar) {
                        case "b": abstractGhostFactory = new BlinkyFactory(); break;
                        case "p": abstractGhostFactory = new PinkyFactory();   break;
                        case "i": abstractGhostFactory = new InkyFactory();    break;
                        case "c": abstractGhostFactory = new ClydeFactory();   break;
                    }

                    Ghost ghost = abstractGhostFactory.makeGhost(xx * cellSize, yy * cellSize);
                    ghosts.add(ghost);

                    if (dataChar.equals("b")) {
                        blinky = (Blinky) ghost;
                    }
                }

                // ---------------- PAC-GUM ----------------
                else if (dataChar.equals(".")) {
                    objects.add(new PacGum(xx * cellSize, yy * cellSize));
                }

                // ---------------- SUPER PAC-GUM ----------------
                else if (dataChar.equals("o")) {
                    objects.add(new SuperPacGum(xx * cellSize, yy * cellSize));
                }

                // ---------------- GHOST HOUSE ----------------
                else if (dataChar.equals("-")) {
                    objects.add(new GhostHouse(xx * cellSize, yy * cellSize));
                }
            }
        }

        objects.add(pacman);
        objects.addAll(ghosts);

        // Store all walls in wall list
        for (Entity o : objects) {
            if (o instanceof Wall) {
                walls.add((Wall) o);
            }
        }
    }

    //Getter for BFS wall grid
    public static boolean[][] getMazeWalls() {
        return mazeWalls;
    }

    public static List<Wall> getWalls() {
        return walls;
    }

    public List<Entity> getEntities() {
        return objects;
    }

    //Mise à jour de toutes les entités
    public void update() {
        for (Entity o : objects) {
            if (!o.isDestroyed()) o.update();
        }
    }

    //Gestion des inputs
    public void input(KeyHandler k) {
        pacman.input(k);
    }

    //Rendu de toutes les entités
    public void render(Graphics2D g) {
        for (Entity o : objects) {
            if (!o.isDestroyed()) o.render(g);
        }
    }

    public static Pacman getPacman() {
        return pacman;
    }

    public static Blinky getBlinky() {
        return blinky;
    }

    public static void setFirstInput(boolean b) {
        firstInput = b;
    }

    public static boolean getFirstInput() {
        return firstInput;
    }

    // ---------------- OBSERVER CALLBACKS ----------------

    @Override
    public void updatePacGumEaten(PacGum pg) {
        pg.destroy();
    }

    @Override
    public void updateSuperPacGumEaten(SuperPacGum spg) {
        spg.destroy();
        for (Ghost gh : ghosts) {
            gh.getState().superPacGumEaten();
        }
    }

    @Override
    public void updateGhostCollision(Ghost gh) {
        if (gh.getState() instanceof FrightenedMode) {
            gh.getState().eaten();
        } else if (!(gh.getState() instanceof EatenMode)) {
            System.out.println("Game over !\nScore : " + GameLauncher.getUIPanel().getScore());
            System.exit(0);
        }
    }
}
