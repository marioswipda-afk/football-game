package football.rendering;

import football.config.GameConfig;
import football.events.EventManager;
import football.events.EventType;

import java.awt.*;

/**
 * Renders the soccer field including grass, lines, goals, and event overlays.
 */
public final class FieldRenderer {

    FieldRenderer() {}

    public static void draw(Graphics2D g, EventManager eventManager) {
        drawGrass(g);
        drawEventOverlay(g, eventManager);
        drawLines(g);
        drawGoals(g);
    }

    private static void drawGrass(Graphics2D g) {
        for (int i = 0; i < 6; i++) {
            g.setColor(i % 2 == 0
                    ? new Color(GameConfig.FIELD_LIGHT_R, GameConfig.FIELD_LIGHT_G, GameConfig.FIELD_LIGHT_B)
                    : new Color(GameConfig.FIELD_DARK_R, GameConfig.FIELD_DARK_G, GameConfig.FIELD_DARK_B));
            g.fillRect(i * GameConfig.WINDOW_WIDTH / 6, 0, GameConfig.WINDOW_WIDTH / 6, GameConfig.WINDOW_HEIGHT);
        }
    }

    private static void drawEventOverlay(Graphics2D g, EventManager eventManager) {
        if (!eventManager.hasActiveEvent()) return;

        switch (eventManager.activeEvent()) {
            case MUD -> {
                g.setColor(new Color(GameConfig.MUD_COLOR_R, GameConfig.MUD_COLOR_G,
                        GameConfig.MUD_COLOR_B, GameConfig.MUD_COLOR_A));
                g.fillRect(0, 0, GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);
            }
            case ICE -> {
                g.setColor(new Color(GameConfig.ICE_COLOR_R, GameConfig.ICE_COLOR_G,
                        GameConfig.ICE_COLOR_B, GameConfig.ICE_COLOR_A));
                g.fillRect(0, 0, GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);
            }
            default -> {}
        }
    }

    private static void drawLines(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(3));

        // Outer boundary
        g.drawRect(8, 8, GameConfig.WINDOW_WIDTH - 16, GameConfig.WINDOW_HEIGHT - 16);
        // Center line
        g.drawLine(GameConfig.WINDOW_WIDTH / 2, 8, GameConfig.WINDOW_WIDTH / 2, GameConfig.WINDOW_HEIGHT - 8);
        // Center circle
        g.drawOval(GameConfig.WINDOW_WIDTH / 2 - 70, GameConfig.WINDOW_HEIGHT / 2 - 70, 140, 140);
        // Penalty areas
        g.drawRect(8, GameConfig.WINDOW_HEIGHT / 2 - 110, 90, 220);
        g.drawRect(GameConfig.WINDOW_WIDTH - 98, GameConfig.WINDOW_HEIGHT / 2 - 110, 90, 220);
    }

    private static void drawGoals(Graphics2D g) {
        // Goal areas
        g.setColor(Color.WHITE);
        g.fillRect(0, GameConfig.GOAL_TOP, GameConfig.GOAL_DEPTH, GameConfig.GOAL_WIDTH);
        g.fillRect(GameConfig.WINDOW_WIDTH - GameConfig.GOAL_DEPTH, GameConfig.GOAL_TOP,
                GameConfig.GOAL_DEPTH, GameConfig.GOAL_WIDTH);

        // Goal nets
        g.setColor(Color.LIGHT_GRAY);
        for (int y = GameConfig.GOAL_TOP; y <= GameConfig.GOAL_BOTTOM; y += 15) {
            g.drawLine(0, y, GameConfig.GOAL_DEPTH, y);
            g.drawLine(GameConfig.WINDOW_WIDTH - GameConfig.GOAL_DEPTH, y, GameConfig.WINDOW_WIDTH, y);
        }
    }
}