/*
 * -----------------------------------------------------------------------------------
 * PROJECT: Pac-Man Ghost AI
 * AUTHORS: Adam Stafford, Barnabas Fluck
 * -----------------------------------------------------------------------------------
 * CITATION:
 * Logic implemented based on the paper:
 * "EXPLORING THE POSSIBILITIES OF MADDPG FOR UAV SWARM CONTROL BY SIMULATING IN PAC-MAN ENVIRONMENT"
 * Authors: Artem Novikov, Sergiy Yakovlev, Ivan Gushchin
 * Published in: Radioelectronic and Computer Systems, 2025, no. 1(113)
 * DOI: 10.32620/reks.2025.1.21
 * * Implemented Algorithm:
 * - [cite_start]Blinky: Breadth-First Search (BFS) for optimal pursuit [cite: 12]
 * -----------------------------------------------------------------------------------
 */

package game.ghostStrategies;

import game.Game;
import game.GameplayPanel;
import game.entities.Wall;
import game.entities.ghosts.Ghost;
import java.util.*;

public class BlinkyStrategy implements IGhostStrategy {
    private Ghost me;

    // PERFORMANCE OPTIMIZATION: Static grid to prevent lag
    private static boolean[][] staticGrid = null;
    private static final int CELL_SIZE = 8;
    private static final int COLS = 56;
    private static final int ROWS = 62;

    public BlinkyStrategy(Ghost me) {
        this.me = me;
    }

    @Override
    public int[] getChaseTargetPosition() {
        //Target Pac-Man's current position
        int targetX = Game.getPacman().getxPos();
        int targetY = Game.getPacman().getyPos();

        //Use BFS to find the exact next step to take
        if (me.onTheGrid()) {
            return solveBFS(me.getxPos(), me.getyPos(), targetX, targetY);
        }
        
        return new int[]{targetX, targetY};
    }
    
    @Override
    public int[] getScatterTargetPosition() {
        //Blinky's home is Top-Right
        return new int[]{GameplayPanel.width, 0};
    }

    // BREADTH-FIRST SEARCH (BFS) IMPLEMENTATION
    private int[] solveBFS(int startX, int startY, int targetX, int targetY) {
        //Build Grid (Once)
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

        //Anti-Freeze: If target is a wall, return raw coords immediately
        if (staticGrid[tX][tY]) return new int[]{targetX, targetY};

        //SUPER-OPTIMIZATION: Use boolean array instead of HashSet to avoid lag spikes
        boolean[][] visited = new boolean[COLS][ROWS];
        
        Queue<Node> queue = new LinkedList<>();
        queue.add(new Node(sX, sY, null));
        visited[sX][sY] = true;

        int loops = 0;

        while (!queue.isEmpty()) {
            loops++;
            //Tighter safety break (500 is enough for Pac-Man grid)
            if (loops > 500) return new int[]{targetX, targetY}; 

            Node current = queue.poll();

            //If found target, backtrack to the FIRST move
            if (current.x == tX && current.y == tY) {
                while (current.parent != null && (current.parent.x != sX || current.parent.y != sY)) {
                    current = current.parent;
                }
                return new int[]{current.x * CELL_SIZE, current.y * CELL_SIZE};
            }

            //Check Neighbors
            int[][] dirs = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
            for (int[] d : dirs) {
                int nx = current.x + d[0], ny = current.y + d[1];
                
                //Fast array lookup is much faster than HashMap
                if (nx >= 0 && nx < COLS && ny >= 0 && ny < ROWS && 
                    !staticGrid[nx][ny] && !visited[nx][ny]) {
                    
                    visited[nx][ny] = true;
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