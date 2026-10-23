package football.input;

import football.model.Player;
import football.model.Ball;

/**
 * Coordinates input handling for both players.
 */
public final class InputHandler {

    private final HumanInput human1 = new HumanInput();
    private final HumanInput human2 = new HumanInput();
    private BotInput botInput;
    private boolean botMode = false;

    public void setBotMode(boolean enabled) {
        this.botMode = enabled;
        if (enabled) {
            this.botInput = new BotInput();
        } else {
            this.botInput = null;
        }
    }

    public void keyPressed(int keyCode) {
        human1.keyPressed(keyCode);
        human2.keyPressed(keyCode);
    }

    public void keyReleased(int keyCode) {
        human1.keyReleased(keyCode);
        human2.keyReleased(keyCode);
    }

    public void update(Player p1, Player p2, Ball ball) {
        human1.update(p1);
        if (botMode && botInput != null) {
            botInput.update(p2, ball);
        } else {
            human2.update(p2);
        }
    }

    public boolean isP1KickPressed(Player p1) {
        return human1.isKickPressed(p1);
    }

    public boolean isP2KickPressed(Player p2) {
        return human2.isKickPressed(p2);
    }

    public boolean isBotMode() { return botMode; }

    public void clear() {
        human1.clear();
        human2.clear();
    }
}