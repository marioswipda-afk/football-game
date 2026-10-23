package football.input;

import football.model.Player;
import football.model.Vector2D;

import java.util.HashSet;
import java.util.Set;

/**
 * Handles human keyboard input for a player.
 */
public final class HumanInput {

    private final Set<Integer> pressedKeys = new HashSet<>();

    public void keyPressed(int keyCode) {
        pressedKeys.add(keyCode);
    }

    public void keyReleased(int keyCode) {
        pressedKeys.remove(keyCode);
    }

    public void update(Player player) {
        int mx = 0, my = 0;
        if (pressedKeys.contains(player.leftKey())) mx -= 1;
        if (pressedKeys.contains(player.rightKey())) mx += 1;
        if (pressedKeys.contains(player.upKey())) my -= 1;
        if (pressedKeys.contains(player.downKey())) my += 1;

        player.setInputDirection(new Vector2D(mx, my));
    }

    public boolean isKickPressed(Player player) {
        return pressedKeys.contains(player.kickKey());
    }

    public void clear() {
        pressedKeys.clear();
    }
}