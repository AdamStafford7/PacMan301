package game.ghostStrategies;

import game.Game;
import game.GameplayPanel;
import game.entities.Wall;
import game.entities.ghosts.Ghost;
import game.utils.Utils;
import java.util.*;

public class InkyStrategy implements IGhostStrategy {
    private Ghost me;
    private Ghost blinky;

    public InkyStrategy(Ghost ghost) {
        this.me = ghost;
        this.blinky = Game.getBlinky();
    }

    @Override
    public int[] getChaseTargetPosition() {
        int pacX = Game.getPacman().getxPos();
        int pacY = Game.getPacman().getyPos();
        
        // 1. Determine Target (Vector Math based on Blinky)
        int[] target;
        if (blinky != null) {
            int[] pacFacing = Utils.getPointDistanceDirection(pacX, pacY, 16, Utils.directionConverter(Game.getPacman().getDirection()));
            double distBlinky = Utils.getDistance(pacFacing[0], pacFacing[1], blinky.getxPos(), blinky.getyPos());
            double dirBlinky = Utils.getDirection(blinky.getxPos(), blinky.getyPos(), pacFacing[0], pacFacing[1]);
            target = Utils.getPointDistanceDirection(pacFacing[0], pacFacing[1], distBlinky, dirBlinky);
        } else {
            target = new int[]{pacX, pacY};
        }

        // 2. Hybrid Logic: If close use BFS, if far just go towards target
        if (me.onTheGrid()) {
             return solveBFS(me.getxPos(), me.getyPos(), target[0], target[1]);
        }
        return target;
    }

    @Override
    public int[] getScatterTargetPosition() {
        return new int[]{GameplayPanel.width, GameplayPanel.height}; // Bottom Right
    }

    // --- BFS ALGORITHM IMPLEMENTATION (HIDDEN INSIDE) ---
    private int[] solveBFS(int startX, int startY, int targetX, int targetY) {
        int CELL_SIZE = 8;
        int COLS = 56;
        int ROWS = 62;
        
        // Build grid
        boolean[][] walls = new boolean[COLS][ROWS];
        for (Wall w : Game.getWalls()) {
            int gx = w.getxPos() / CELL_SIZE;
            int gy = w.getyPos() / CELL_SIZE;
            if (gx >= 0 && gx < COLS && gy >= 0 && gy < ROWS) walls[gx][gy] = true;
        }

        int sX = startX / CELL_SIZE;
        int sY = startY / CELL_SIZE;
        int tX = Math.max(0, Math.min(COLS - 1, targetX / CELL_SIZE));
        int tY = Math.max(0, Math.min(ROWS - 1, targetY / CELL_SIZE));

        Queue<Node> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        queue.add(new Node(sX, sY, null));
        visited.add(sX + "," + sY);

        while (!queue.isEmpty()) {
            Node current = queue.poll();
            if (current.x == tX && current.y == tY) {
                while (current.parent != null && (current.parent.x != sX || current.parent.y != sY)) {
                    current = current.parent;
                }
                return new int[]{current.x * CELL_SIZE, current.y * CELL_SIZE};
            }

            int[][] dirs = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
            for (int[] d : dirs) {
                int nx = current.x + d[0], ny = current.y + d[1];
                if (nx >= 0 && nx < COLS && ny >= 0 && ny < ROWS && !walls[nx][ny] && !visited.contains(nx + "," + ny)) {
                    visited.add(nx + "," + ny);
                    queue.add(new Node(nx, ny, current));
                }
            }
        }
        return new int[]{targetX, targetY};
    }

    private class Node {
        int x, y;
        Node parent;
        Node(int x, int y, Node p) { this.x=x; this.y=y; parent=p; }
    }
}