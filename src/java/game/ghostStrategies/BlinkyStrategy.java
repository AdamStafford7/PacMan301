package game.ghostStrategies;

import game.Game;

import java.util.*;

public class BlinkyStrategy implements IGhostStrategy {

    // ------------ GENERIC BFS WITH TUNNEL + DEBUG SUPPORT ------------

    private int[] bfsTarget(int targetX, int targetY) {

        // Convert ghost position to tile coordinates
        int blinkyX = Game.getBlinky().getxPos() / 8;
        int blinkyY = Game.getBlinky().getyPos() / 8;

        boolean[][] walls = Game.getMazeWalls();

        int rows = walls.length;        // Y tiles
        int cols = walls[0].length;     // X tiles

        boolean[][] visited = new boolean[rows][cols];
        int[][][] parent = new int[rows][cols][2];

        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[]{blinkyX, blinkyY});
        visited[blinkyY][blinkyX] = true;

        int[][] dirs = {
                {1, 0}, {-1, 0}, {0, 1}, {0, -1}
        };

        boolean found = false;

        // ---------------- BFS LOOP ----------------
        while (!queue.isEmpty()) {

            int[] cur = queue.poll();
            int cx = cur[0];
            int cy = cur[1];

            if (cx == targetX && cy == targetY) {
                found = true;
                break;
            }

            for (int[] d : dirs) {

                int nx = cx + d[0];
                int ny = cy + d[1];

                // -------- TUNNEL WRAP-AROUND (LEFT/RIGHT) --------
                if (nx < 0) nx = cols - 1;
                if (nx >= cols) nx = 0;

                // -------- NO VERTICAL WRAP --------
                if (ny < 0 || ny >= rows)
                    continue;

                // -------- WALL CHECK --------
                if (walls[ny][nx]) continue;
                if (visited[ny][nx]) continue;

                visited[ny][nx] = true;
                parent[ny][nx][0] = cx;
                parent[ny][nx][1] = cy;

                queue.add(new int[]{nx, ny});
            }
        }

        // -------- SAFETY FALLBACK --------
        if (!found) {
            int pacX = Game.getPacman().getxPos() / 8;
            int pacY = Game.getPacman().getyPos() / 8;
            return bfsTarget(pacX, pacY);
        }

        // ---------------- PATH RECONSTRUCTION ----------------
        LinkedList<int[]> path = new LinkedList<>();

        int cx = targetX;
        int cy = targetY;

        while (!(cx == blinkyX && cy == blinkyY)) {

            path.addFirst(new int[]{cx, cy});

            int px = parent[cy][cx][0];
            int py = parent[cy][cx][1];

            cx = px;
            cy = py;
        }

        int[] next = path.get(0);

        // ✅✅✅ DEBUG PRINT (PROVES BFS IS ACTIVE EVERY FRAME)
        System.out.println("BFS TARGET TILE = (" + next[0] + ", " + next[1] + ")");

        return new int[]{ next[0] * 8, next[1] * 8 };
    }

    // ---------------- STRATEGY INTERFACE ----------------

    // ✅ CHASE MODE = BFS TO PAC-MAN
    @Override
    public int[] getChaseTargetPosition() {

        int pacX = Game.getPacman().getxPos() / 8;
        int pacY = Game.getPacman().getyPos() / 8;

        return bfsTarget(pacX, pacY);
    }

    // ✅ SCATTER MODE = ALSO BFS TO PAC-MAN (NO CORNERS)
    @Override
    public int[] getScatterTargetPosition() {

        int pacX = Game.getPacman().getxPos() / 8;
        int pacY = Game.getPacman().getyPos() / 8;

        // ✅ DEBUG PRINT to show Scatter still follows BFS
        System.out.println("SCATTER MODE → STILL USING BFS");

        return bfsTarget(pacX, pacY);
    }
}