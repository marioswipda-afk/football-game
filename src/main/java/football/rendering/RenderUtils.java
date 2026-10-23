package football.rendering;

import football.config.GameConfig;
import football.model.Player;
import football.model.Vector2D;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * Utility class for coordinate transformations and common drawing operations.
 */
public final class RenderUtils {

    RenderUtils() {}

    /**
     * Calculate scale factor to fit game in window maintaining aspect ratio.
     */
    public static double calculateScale(int windowWidth, int windowHeight) {
        return Math.min(windowWidth / (double) GameConfig.WINDOW_WIDTH,
                        windowHeight / (double) GameConfig.WINDOW_HEIGHT);
    }

    /**
     * Calculate X offset to center game horizontally.
     */
    public static double calculateOffsetX(int windowWidth, double scale) {
        return (windowWidth - GameConfig.WINDOW_WIDTH * scale) / 2.0;
    }

    /**
     * Calculate Y offset to center game vertically.
     */
    public static double calculateOffsetY(int windowHeight, double scale) {
        return (windowHeight - GameConfig.WINDOW_HEIGHT * scale) / 2.0;
    }

    /**
     * Convert screen coordinates to game coordinates.
     */
    public static Vector2D screenToGame(int screenX, int screenY, double offsetX, double offsetY, double scale) {
        double gameX = (screenX - offsetX) / scale;
        double gameY = (screenY - offsetY) / scale;
        return new Vector2D(gameX, gameY);
    }

    /**
     * Check if a point is inside a rectangle in game coordinates.
     */
    public static boolean contains(Rectangle2D rect, Vector2D point) {
        return rect.contains(point.x(), point.y());
    }

    /**
     * Draw a soccer ball at the given position.
     */
    public static void drawBall(Graphics2D g, double x, double y, double radius,
                                Color baseColor, Color spotColor) {
        // Main ball
        g.setColor(baseColor);
        g.fillOval((int) (x - radius), (int) (y - radius), (int) (2 * radius), (int) (2 * radius));

        // Pentagon pattern
        g.setColor(spotColor);
        int[] px = new int[5];
        int[] py = new int[5];
        for (int i = 0; i < 5; i++) {
            double angle = Math.PI / 2 + i * 2 * Math.PI / 5;
            px[i] = (int) (x + radius * 0.35 * Math.cos(angle));
            py[i] = (int) (y - radius * 0.35 * Math.sin(angle));
        }
        g.fillPolygon(px, py, 5);

        // White dots
        for (int i = 0; i < 5; i++) {
            double angle = Math.PI / 2 + i * 2 * Math.PI / 5 + Math.PI / 5;
            int dotX = (int) (x + radius * 0.8 * Math.cos(angle));
            int dotY = (int) (y - radius * 0.8 * Math.sin(angle));
            g.fillOval(dotX - 3, dotY - 3, 6, 6);
        }

        // Outline
        g.setColor(Color.DARK_GRAY);
        g.setStroke(new BasicStroke(2));
        g.drawOval((int) (x - radius), (int) (y - radius), (int) (2 * radius), (int) (2 * radius));
    }

    /**
     * Draw charge meter above player.
     */
    public static void drawChargeMeter(Graphics2D g, Player player) {
        if (!player.charging()) return;

        int w = GameConfig.CHARGE_BAR_WIDTH;
        int h = GameConfig.CHARGE_BAR_HEIGHT;
        int x = (int) player.position().x() - w / 2;
        int y = (int) player.position().y() - (int) player.radius() - GameConfig.CHARGE_BAR_OFFSET_Y;

        g.setColor(Color.DARK_GRAY);
        g.fillRect(x, y, w, h);

        Color chargeColor = player.charge() > GameConfig.SUPER_KICK_THRESHOLD ? Color.YELLOW : new Color(120, 220, 120);
        g.setColor(chargeColor);
        g.fillRect(x, y, (int) (w * player.charge()), h);

        g.setColor(Color.WHITE);
        g.drawRect(x, y, w, h);
    }
}