package game.ghostStrategies;

import game.Game;
import game.entities.Wall;
import game.entities.ghosts.Ghost;
import game.utils.Utils;
import java.util.*;

/*
 * CITATION:
 * Logic implemented based on the paper:
 * "EXPLORING THE POSSIBILITIES OF MADDPG FOR UAV SWARM CONTROL BY SIMULATING IN PAC-MAN ENVIRONMENT"
 * Authors: Artem Novikov, Sergiy Yakovlev, Ivan Gushchin
 * Published in: Radioelectronic and Computer Systems, 2025, no. 1(113)
 * DOI: 10.32620/reks.2025.1.21
 * Algorithm: A* Search
 */
public class PinkyStrategy implements IGhostStrategy {
    private Ghost me;
    
    // Static map to prevent lag
    private static boolean[][] staticGrid = null;
    private static final int CELL_SIZE = 8;
    private static final int COLS = 56; 
    private static final int ROWS = 62; 

    public PinkyStrategy(Ghost me) {
        this.me = me;
    }

    @Override
    public int[] getChaseTargetPosition() {
        int[] target = Utils.getPointDistanceDirection(
                Game.getPacman().getxPos(), 
                Game.getPacman().getyPos(), 
                32, 
                Utils.directionConverter(Game.getPacman().getDirection())
        );

        if (me.onTheGrid()) {
            return solveAStar(me.getxPos(), me.getyPos(), target[0], target[1]);
        }
        return target;
    }

    @Override
    public int[] getScatterTargetPosition() {
        return new int[]{0, 0};
    }

    private int[] solveAStar(int startX, int startY, int targetX, int targetY) {
        if (staticGrid == null) {
            staticGrid = new boolean[COLS][ROWS];
            for (Wall w : Game.getWalls()) {
                int gx = w.getxPos() / CELL_SIZE;
                int gy = w.getyPos() / CELL_SIZE;
                if (gx >= 0 && gx < COLS && gy >= 0 && gy < ROWS) staticGrid[gx][gy] = true;
            }
        }

        int sX = startX / CELL_SIZE;
        int sY = startY / CELL_SIZE;
        int tX = Math.max(0, Math.min(COLS - 1, targetX / CELL_SIZE));
        int tY = Math.max(0, Math.min(ROWS - 1, targetY / CELL_SIZE));

        // ANTI-FREEZE 1: If target is a wall, don't try to pathfind to it. 
        if (staticGrid[tX][tY]) return new int[]{targetX, targetY};

        PriorityQueue<Node> open = new PriorityQueue<>();
        Set<String> closed = new HashSet<>();
        open.add(new Node(sX, sY, null, 0, Math.abs(sX - tX) + Math.abs(sY - tY)));

        // ANTI-FREEZE 2: Safety counter
        int loops = 0;

        while (!open.isEmpty()) {
            loops++;
            // If we've searched 1000 nodes and haven't found it, just give up to save FPS
            if (loops > 1000) return new int[]{targetX, targetY};

            Node current = open.poll();
            if (current.x == tX && current.y == tY) {
                while (current.parent != null && (current.parent.x != sX || current.parent.y != sY)) {
                    current = current.parent;
                }
                return new int[]{current.x * CELL_SIZE, current.y * CELL_SIZE};
            }

            closed.add(current.x + "," + current.y);

            int[][] dirs = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
            for (int[] d : dirs) {
                int nx = current.x + d[0], ny = current.y + d[1];
                if (nx >= 0 && nx < COLS && ny >= 0 && ny < ROWS && !staticGrid[nx][ny] && !closed.contains(nx + "," + ny)) {
                    double g = current.g + 1;
                    double h = Math.abs(nx - tX) + Math.abs(ny - tY);
                    open.add(new Node(nx, ny, current, g, h));
                }
            }
        }
        return new int[]{targetX, targetY};
    }

    private class Node implements Comparable<Node> {
        int x, y;
        Node parent;
        double g, h;
        Node(int x, int y, Node p, double g, double h) { this.x=x; this.y=y; parent=p; this.g=g; this.h=h; }
        public int compareTo(Node o) { return Double.compare(g + h, o.g + o.h); }
    }
}