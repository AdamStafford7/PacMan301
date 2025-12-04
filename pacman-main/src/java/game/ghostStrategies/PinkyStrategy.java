package game.ghostStrategies;

import game.Game;
import game.entities.Wall;
import game.entities.ghosts.Ghost;
import game.utils.Utils;
import java.util.*;

public class PinkyStrategy implements IGhostStrategy {
    private Ghost me;

    public PinkyStrategy(Ghost me) {
        this.me = me;
    }

    @Override
    public int[] getChaseTargetPosition() {
        // 1. Calculate the target (2 tiles/32px in front of Pacman)
        int[] target = Utils.getPointDistanceDirection(
                Game.getPacman().getxPos(), 
                Game.getPacman().getyPos(), 
                32, // 32 pixels ahead
                Utils.directionConverter(Game.getPacman().getDirection())
        );

        // 2. Use A* (Self-contained) to find the path
        if (me.onTheGrid()) {
            return solveAStar(me.getxPos(), me.getyPos(), target[0], target[1]);
        }
        return target;
    }

    @Override
    public int[] getScatterTargetPosition() {
        return new int[]{0, 0}; // Top Left
    }

    // --- A* ALGORITHM IMPLEMENTATION (HIDDEN INSIDE) ---
    private int[] solveAStar(int startX, int startY, int targetX, int targetY) {
        int CELL_SIZE = 8;
        int COLS = 56; // 448/8
        int ROWS = 62; // 496/8
        
        // Build simple grid of walls
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

        if (sX == tX && sY == tY) return new int[]{targetX, targetY};

        PriorityQueue<Node> open = new PriorityQueue<>();
        Set<String> closed = new HashSet<>();
        open.add(new Node(sX, sY, null, 0, Math.abs(sX - tX) + Math.abs(sY - tY)));

        while (!open.isEmpty()) {
            Node current = open.poll();
            if (current.x == tX && current.y == tY) {
                // Backtrack to find the FIRST step
                while (current.parent != null && (current.parent.x != sX || current.parent.y != sY)) {
                    current = current.parent;
                }
                return new int[]{current.x * CELL_SIZE, current.y * CELL_SIZE};
            }

            closed.add(current.x + "," + current.y);

            int[][] dirs = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
            for (int[] d : dirs) {
                int nx = current.x + d[0], ny = current.y + d[1];
                if (nx >= 0 && nx < COLS && ny >= 0 && ny < ROWS && !walls[nx][ny] && !closed.contains(nx + "," + ny)) {
                    double g = current.g + 1;
                    double h = Math.abs(nx - tX) + Math.abs(ny - tY);
                    open.add(new Node(nx, ny, current, g, h));
                }
            }
        }
        return new int[]{targetX, targetY};
    }

    // Tiny Helper Class for A*
    private class Node implements Comparable<Node> {
        int x, y;
        Node parent;
        double g, h;
        Node(int x, int y, Node p, double g, double h) { this.x=x; this.y=y; parent=p; this.g=g; this.h=h; }
        public int compareTo(Node o) { return Double.compare(g + h, o.g + o.h); }
    }
}