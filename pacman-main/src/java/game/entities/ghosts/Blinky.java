package game.entities.ghosts;

import game.ghostStrategies.BlinkyStrategy;

//Classe concrète de Blinky (le fantôme rouge)
public class Blinky extends Ghost {
    public Blinky(int xPos, int yPos) {
        super(xPos, yPos, "blinky.png");
        // FIX: Pass 'this' so the strategy knows who is moving
        setStrategy(new BlinkyStrategy(this));
    }
}