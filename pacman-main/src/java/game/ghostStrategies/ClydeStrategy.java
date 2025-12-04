package game.ghostStrategies;

import game.Game;
import game.GameplayPanel;
import game.entities.ghosts.Ghost;
import game.utils.Utils;

public class ClydeStrategy implements IGhostStrategy {
    private Ghost me;
    public ClydeStrategy(Ghost ghost) { this.me = ghost; }

    @Override
    public int[] getChaseTargetPosition() {
        // If distance > 8 dots (64 pixels), chase Pacman
        if (Utils.getDistance(me.getxPos(), me.getyPos(), Game.getPacman().getxPos(), Game.getPacman().getyPos()) >= 64) {
            return new int[]{Game.getPacman().getxPos(), Game.getPacman().getyPos()};
        } else {
            // Otherwise, run home
            return getScatterTargetPosition();
        }
    }

    @Override
    public int[] getScatterTargetPosition() {
        return new int[]{0, GameplayPanel.height}; // Bottom Left
    }
}