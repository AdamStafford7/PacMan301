package game.entities.ghosts;

import game.ghostStrategies.InkyStrategy;

//Classe concrète de Inky (le fantôme bleu)
public class Inky extends Ghost {
    public Inky(int xPos, int yPos) {
        super(xPos, yPos, "inky.png");
        // FIX: Pass 'this' (Inky). The strategy finds Blinky on its own now.
        setStrategy(new InkyStrategy(this));
    }
}