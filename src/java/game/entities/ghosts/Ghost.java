package game.entities.ghosts;

import game.Game;
import game.entities.MovingEntity;
import game.ghostStates.*;
import game.ghostStrategies.IGhostStrategy;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Abstract class for ghosts.
 * Handles state transitions, movement logic, and rendering.
 */
public abstract class Ghost extends MovingEntity {

    protected GhostState state;

    protected final GhostState chaseMode;
    protected final GhostState scatterMode;
    protected final GhostState frightenedMode;
    protected final GhostState eatenMode;
    protected final GhostState houseMode;

    protected int modeTimer = 0;
    protected int frightenedTimer = 0;
    protected boolean isChasing = false;

    protected static BufferedImage frightenedSprite1;
    protected static BufferedImage frightenedSprite2;
    protected static BufferedImage eatenSprite;

    protected IGhostStrategy strategy;

    public Ghost(int xPos, int yPos, String spriteName) {
        super(32, xPos, yPos, 2, spriteName, 2, 0.1f);

        // Create all ghost states
        chaseMode = new ChaseMode(this);
        scatterMode = new ScatterMode(this);
        frightenedMode = new FrightenedMode(this);
        eatenMode = new EatenMode(this);
        houseMode = new HouseMode(this);

        // Initial state
        state = houseMode;

        // Load sprites
        try {
            frightenedSprite1 = ImageIO.read(getClass().getClassLoader().getResource("img/ghost_frightened.png"));
            frightenedSprite2 = ImageIO.read(getClass().getClassLoader().getResource("img/ghost_frightened_2.png"));
            eatenSprite = ImageIO.read(getClass().getClassLoader().getResource("img/ghost_eaten.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // -------- STATE SWITCHES --------
    public void switchChaseMode() { state = chaseMode; }
    public void switchScatterMode() { state = scatterMode; }

    public void switchFrightenedMode() {
        frightenedTimer = 0;
        state = frightenedMode;
    }

    public void switchEatenMode() { state = eatenMode; }

    public void switchHouseMode() { state = houseMode; }

    public void switchChaseModeOrScatterMode() {
        if (isChasing) switchChaseMode();
        else switchScatterMode();
    }

    public IGhostStrategy getStrategy() { return this.strategy; }
    public void setStrategy(IGhostStrategy strategy) { this.strategy = strategy; }

    public GhostState getState() { return state; }


    // -------- MAIN UPDATE LOGIC --------

    @Override
    public void update() {

        // 🔥 Freeze all ghosts except Blinky (testing mode)
        if (!(this instanceof game.entities.ghosts.Blinky)) return;

        // Ghosts do not move until Pac-Man moves
        if (!Game.getFirstInput()) return;

        // Frightened mode timer
        if (state == frightenedMode) {
            frightenedTimer++;

            if (frightenedTimer >= (60 * 7)) {
                state.timerFrightenedModeOver();
            }
        }

        // Chase/Scatter mode switching timer
        if (state == chaseMode || state == scatterMode) {
            modeTimer++;

            if ((isChasing && modeTimer >= (60 * 20)) ||
                    (!isChasing && modeTimer >= (60 * 5))) {

                state.timerModeOver();
                isChasing = !isChasing;
            }
        }

        // Ghost house transitions
        if (xPos == 208 && yPos == 168) {
            state.outsideHouse();
        }

        if (xPos == 208 && yPos == 200) {
            state.insideHouse();
        }

        // Compute movement direction based on state & strategy
        state.computeNextDir();

        // Move ghost
        updatePosition();
    }


    // -------- RENDERING --------

    @Override
    public void render(Graphics2D g) {

        if (state == frightenedMode) {

            // Flickering effect near end of frightened time
            if (frightenedTimer <= (60 * 5) || frightenedTimer % 20 > 10) {
                g.drawImage(
                        frightenedSprite1.getSubimage((int) subimage * size, 0, size, size),
                        this.xPos, this.yPos,
                        null
                );
            } else {
                g.drawImage(
                        frightenedSprite2.getSubimage((int) subimage * size, 0, size, size),
                        this.xPos, this.yPos,
                        null
                );
            }

        } else if (state == eatenMode) {

            // Draw only the eyes (eaten ghost)
            g.drawImage(
                    eatenSprite.getSubimage(direction * size, 0, size, size),
                    this.xPos, this.yPos,
                    null
            );

        } else {

            // Normal ghost sprite
            g.drawImage(
                    sprite.getSubimage(
                            (int) subimage * size + direction * size * nbSubimagesPerCycle,
                            0,
                            size,
                            size
                    ),
                    this.xPos, this.yPos,
                    null
            );
        }
    }
}