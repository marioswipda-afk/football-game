package football.rendering;

import football.config.GameConfig;
import football.events.EventManager;
import football.model.GameState;
import football.model.Player;
import football.model.Vector2D;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * Renders UI elements: menus, HUD, win screen, replay overlay.
 */
public final class UIRenderer {

    UIRenderer() {}

    public static void drawMenu(Graphics2D g, boolean fullscreen) {
        drawMenuBackground(g);
        drawMenuTitle(g);
        drawMenuButtons(g, fullscreen);
        drawMenuInstructions(g);
        drawDecorativeBalls(g);
    }

    private static void drawMenuBackground(Graphics2D g) {
        // Dark gradient background
        GradientPaint gp = new GradientPaint(
                0, 0, new Color(15, 50, 15),
                0, GameConfig.WINDOW_HEIGHT, new Color(5, 25, 5)
        );
        g.setPaint(gp);
        g.fillRect(0, 0, GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);

        // Subtle grass pattern overlay
        g.setColor(new Color(GameConfig.FIELD_LIGHT_R, GameConfig.FIELD_LIGHT_G, GameConfig.FIELD_LIGHT_B, 30));
        for (int i = 0; i < 8; i++) {
            g.fillRect(i * GameConfig.WINDOW_WIDTH / 8, 0, GameConfig.WINDOW_WIDTH / 8, GameConfig.WINDOW_HEIGHT);
        }

        // Center circle faint
        g.setColor(new Color(255, 255, 255, 15));
        g.setStroke(new BasicStroke(2));
        g.drawOval(GameConfig.WINDOW_WIDTH / 2 - 120, GameConfig.WINDOW_HEIGHT / 2 - 120, 240, 240);
        g.drawLine(GameConfig.WINDOW_WIDTH / 2, 0, GameConfig.WINDOW_WIDTH / 2, GameConfig.WINDOW_HEIGHT);
    }

    private static void drawMenuTitle(Graphics2D g) {
        String title = "SOCCER BALL SHOWDOWN";
        String subtitle = "2 Player Local Football";

        // Title shadow/glow effect
        g.setFont(new Font("SansSerif", Font.BOLD, 56));
        FontMetrics fm = g.getFontMetrics();

        // Glow layers
        for (int i = 4; i >= 1; i--) {
            float alpha = 0.08f * i;
            g.setColor(new Color(255, 215, 0, (int)(255 * alpha))); // Gold glow
            g.drawString(title,
                    GameConfig.WINDOW_WIDTH / 2 - fm.stringWidth(title) / 2 + i,
                    125 + i);
        }

        // Main title - gold
        g.setColor(new Color(255, 215, 0)); // Gold
        g.drawString(title, GameConfig.WINDOW_WIDTH / 2 - fm.stringWidth(title) / 2, 125);

        // Subtitle
        g.setFont(new Font("SansSerif", Font.PLAIN, 22));
        fm = g.getFontMetrics();
        g.setColor(new Color(200, 230, 200));
        g.drawString(subtitle, GameConfig.WINDOW_WIDTH / 2 - fm.stringWidth(subtitle) / 2, 160);

        // Decorative line under title
        g.setStroke(new BasicStroke(3));
        g.setColor(new Color(255, 215, 0, 180));
        int lineWidth = 300;
        g.drawLine(GameConfig.WINDOW_WIDTH / 2 - lineWidth / 2, 170, GameConfig.WINDOW_WIDTH / 2 + lineWidth / 2, 170);
    }

    private static void drawMenuButtons(Graphics2D g, boolean fullscreen) {
        String[] labels = getMenuLabels(fullscreen);

        for (int i = 0; i < labels.length; i++) {
            Rectangle2D rect = getButtonRect(i);
            drawButton(g, rect, labels[i], i == 4); // Last button is fullscreen toggle
        }
    }

    private static void drawButton(Graphics2D g, Rectangle2D rect, String label, boolean isToggle) {
        int x = (int) rect.getX();
        int y = (int) rect.getY();
        int w = (int) rect.getWidth();
        int h = (int) rect.getHeight();

        // Button background gradient
        GradientPaint btnGrad = new GradientPaint(
                x, y, new Color(30, 100, 30),
                x, y + h, new Color(15, 60, 15)
        );
        g.setPaint(btnGrad);
        g.fillRoundRect(x, y, w, h, 16, 16);

        // Button border - gold accent
        g.setColor(new Color(255, 215, 0, 200));
        g.setStroke(new BasicStroke(2.5f));
        g.drawRoundRect(x, y, w, h, 16, 16);

        // Inner highlight
        g.setColor(new Color(255, 255, 255, 30));
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(x + 3, y + 3, w - 6, h - 6, 12, 12);

        // Button text
        g.setFont(new Font("SansSerif", Font.BOLD, 22));
        FontMetrics fm = g.getFontMetrics();
        int textX = x + (w - fm.stringWidth(label)) / 2;
        int textY = y + (h + fm.getAscent()) / 2 - 2;

        // Text shadow
        g.setColor(new Color(0, 0, 0, 120));
        g.drawString(label, textX + 1, textY + 1);

        // Main text
        g.setColor(Color.WHITE);
        g.drawString(label, textX, textY);

        // Toggle indicator for fullscreen button
        if (isToggle) {
            String status = label.contains("LIGADA") ? "ON" : "OFF";
            Color statusColor = label.contains("LIGADA") ? new Color(100, 255, 100) : new Color(255, 100, 100);
            g.setFont(new Font("SansSerif", Font.BOLD, 16));
            fm = g.getFontMetrics();
            g.setColor(statusColor);
            g.drawString("[" + status + "]", x + w - fm.stringWidth("[" + status + "]") - 15, textY);
        }
    }

    private static void drawMenuInstructions(Graphics2D g) {
        g.setFont(new Font("SansSerif", Font.PLAIN, 16));
        FontMetrics fm = g.getFontMetrics();

        String[] instructions = {
            "Jogador 1:  WASD  +  segurar F para SUPER CHUTE",
            "Jogador 2:  Setas  +  segurar ENTER para SUPER CHUTE",
            "Primeiro a " + GameConfig.WIN_SCORE + " gols vence  •  R = Menu  •  F11 = Tela Cheia"
        };

        int startY = 520;
        for (int i = 0; i < instructions.length; i++) {
            String text = instructions[i];
            int x = GameConfig.WINDOW_WIDTH / 2 - fm.stringWidth(text) / 2;
            int y = startY + i * 28;

            // Shadow
            g.setColor(new Color(0, 0, 0, 100));
            g.drawString(text, x + 1, y + 1);

            // Main - slightly different colors
            g.setColor(i == 2 ? new Color(255, 215, 0) : new Color(200, 230, 200));
            g.drawString(text, x, y);
        }
    }

    private static void drawDecorativeBalls(Graphics2D g) {
        // Draw decorative soccer balls in corners
        int ballSize = 40;
        int margin = 50;

        // Top-left
        drawDecorativeBall(g, margin, margin, ballSize);
        // Top-right
        drawDecorativeBall(g, GameConfig.WINDOW_WIDTH - margin - ballSize, margin, ballSize);
        // Bottom-left
        drawDecorativeBall(g, margin, GameConfig.WINDOW_HEIGHT - margin - ballSize, ballSize);
        // Bottom-right
        drawDecorativeBall(g, GameConfig.WINDOW_WIDTH - margin - ballSize, GameConfig.WINDOW_HEIGHT - margin - ballSize, ballSize);
    }

    private static void drawDecorativeBall(Graphics2D g, int x, int y, int size) {
        g.setColor(new Color(255, 255, 255, 25));
        g.fillOval(x, y, size, size);
        g.setColor(new Color(0, 0, 0, 25));
        g.setStroke(new BasicStroke(1.5f));
        g.drawOval(x, y, size, size);

        // Simple pentagon pattern
        g.setColor(new Color(255, 255, 255, 15));
        int cx = x + size / 2;
        int cy = y + size / 2;
        int r = size / 2 - 2;
        for (int i = 0; i < 5; i++) {
            double angle = Math.PI / 2 + i * 2 * Math.PI / 5;
            int px = (int) (cx + r * 0.5 * Math.cos(angle));
            int py = (int) (cy - r * 0.5 * Math.sin(angle));
            g.fillOval(px - 2, py - 2, 4, 4);
        }
    }

    public static void drawHUD(Graphics2D g, int score1, int score2,
                               EventManager eventManager, String lastEvent) {
        // Score with shadow
        g.setFont(new Font("SansSerif", Font.BOLD, 44));
        String scoreText = score1 + "  -  " + score2;
        FontMetrics fm = g.getFontMetrics();
        int scoreX = GameConfig.WINDOW_WIDTH / 2 - fm.stringWidth(scoreText) / 2;

        // Shadow
        g.setColor(new Color(0, 0, 0, 100));
        g.drawString(scoreText, scoreX + 2, 58);
        // Main - gold
        g.setColor(new Color(255, 215, 0));
        g.drawString(scoreText, scoreX, 56);

        // Event/message
        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        String eventText = eventManager.hasActiveEvent()
                ? "Evento: " + eventManager.activeEvent().displayName()
                : lastEvent;
        fm = g.getFontMetrics();
        int eventX = GameConfig.WINDOW_WIDTH / 2 - fm.stringWidth(eventText) / 2;

        g.setColor(new Color(0, 0, 0, 100));
        g.drawString(eventText, eventX + 1, GameConfig.WINDOW_HEIGHT - 49);
        g.setColor(Color.WHITE);
        g.drawString(eventText, eventX, GameConfig.WINDOW_HEIGHT - 50);

        // Controls hint
        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g.setColor(new Color(180, 210, 180));
        String controls = "P1: WASD + F   |   P2: Setas + ENTER   |   R: Menu   |   F11: Tela Cheia";
        fm = g.getFontMetrics();
        g.drawString(controls, GameConfig.WINDOW_WIDTH / 2 - fm.stringWidth(controls) / 2, GameConfig.WINDOW_HEIGHT - 18);
    }

    public static void drawWin(Graphics2D g, int score1, int score2, boolean botMode) {
        // Dark overlay with gradient
        GradientPaint gp = new GradientPaint(
                0, 0, new Color(0, 0, 0, 200),
                0, GameConfig.WINDOW_HEIGHT, new Color(0, 0, 0, 150)
        );
        g.setPaint(gp);
        g.fillRect(0, 0, GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);

        // Winner text with gold glow
        g.setFont(new Font("SansSerif", Font.BOLD, 56));
        String winner = score1 > score2 ? "JOGADOR 1 VENCEU!" : "JOGADOR 2 VENCEU!";
        FontMetrics fm = g.getFontMetrics();
        int winnerX = GameConfig.WINDOW_WIDTH / 2 - fm.stringWidth(winner) / 2;
        int winnerY = GameConfig.WINDOW_HEIGHT / 2 - 30;

        // Glow
        for (int i = 5; i >= 1; i--) {
            g.setColor(new Color(255, 215, 0, 30 * i));
            g.drawString(winner, winnerX + i, winnerY + i);
        }
        // Main
        g.setColor(new Color(255, 215, 0));
        g.drawString(winner, winnerX, winnerY);

        // Subtitle
        g.setFont(new Font("SansSerif", Font.PLAIN, 24));
        String sub = botMode
                ? (score1 > score2 ? "Você venceu o bot!" : "O bot venceu!")
                : "Placar final: " + score1 + " - " + score2;
        fm = g.getFontMetrics();
        int subX = GameConfig.WINDOW_WIDTH / 2 - fm.stringWidth(sub) / 2;
        g.setColor(new Color(220, 240, 220));
        g.drawString(sub, subX, GameConfig.WINDOW_HEIGHT / 2 + 25);

        // Restart hint
        g.setFont(new Font("SansSerif", Font.PLAIN, 18));
        String hint = "Clique ou pressione R para voltar ao menu";
        fm = g.getFontMetrics();
        int hintX = GameConfig.WINDOW_WIDTH / 2 - fm.stringWidth(hint) / 2;
        g.setColor(new Color(180, 210, 180));
        g.drawString(hint, hintX, GameConfig.WINDOW_HEIGHT / 2 + 80);

        // Decorative balls
        drawDecorativeBall(g, GameConfig.WINDOW_WIDTH / 2 - 180, winnerY - 50, 30);
        drawDecorativeBall(g, GameConfig.WINDOW_WIDTH / 2 + 150, winnerY - 50, 30);
    }

    public static void drawReplay(Graphics2D g, double[][] replayFrames, int replayIdx,
                                  Player p1, Player p2) {
        // Dark overlay
        g.setColor(new Color(GameConfig.REPLAY_OVERLAY_R, GameConfig.REPLAY_OVERLAY_G,
                GameConfig.REPLAY_OVERLAY_B, GameConfig.REPLAY_OVERLAY_A));
        g.fillRect(0, 0, GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);

        // Replay window
        int rw = 560, rh = 373;
        int rx = (GameConfig.WINDOW_WIDTH - rw) / 2;
        int ry = (GameConfig.WINDOW_HEIGHT - rh) / 2;

        // Window background with gradient
        GradientPaint winGrad = new GradientPaint(
                rx, ry, new Color(20, 70, 20),
                rx, ry + rh, new Color(10, 40, 10)
        );
        g.setPaint(winGrad);
        g.fillRoundRect(rx, ry, rw, rh, 12, 12);

        // Border
        g.setColor(new Color(255, 215, 0, 200));
        g.setStroke(new BasicStroke(3));
        g.drawRoundRect(rx, ry, rw, rh, 12, 12);

        // Inner glow
        g.setColor(new Color(255, 255, 255, 20));
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(rx + 4, ry + 4, rw - 8, rh - 8, 8, 8);

        if (replayFrames != null && replayFrames.length > 0) {
            double[] frame = replayFrames[Math.min(replayIdx, replayFrames.length - 1)];
            double sx = rw / (double) GameConfig.WINDOW_WIDTH;
            double sy = rh / (double) GameConfig.WINDOW_HEIGHT;

            RenderUtils.drawBall(g, rx + frame[0] * sx, ry + frame[1] * sy,
                    p1.radius() * sx, p1.color(), p1.darkColor());
            RenderUtils.drawBall(g, rx + frame[2] * sx, ry + frame[3] * sy,
                    p2.radius() * sx, p2.color(), p2.darkColor());
            RenderUtils.drawBall(g, rx + frame[4] * sx, ry + frame[5] * sy,
                    GameConfig.BALL_RADIUS * sx, Color.WHITE, Color.BLACK);
        }

        // Replay label
        g.setColor(new Color(255, 215, 0));
        g.setFont(new Font("SansSerif", Font.BOLD, 28));
        g.drawString("REPLAY DO GOL!", rx + 15, ry - 15);
    }

    private static String[] getMenuLabels(boolean fullscreen) {
        return new String[]{
                "2 Jogadores (mesmo teclado)",
                "Contra Bot - F\u00e1cil",
                "Contra Bot - M\u00e9dio",
                "Contra Bot - Dif\u00edcil",
                fullscreen ? "Tela cheia: LIGADA" : "Tela cheia: DESLIGADA"
        };
    }

    private static Rectangle2D getButtonRect(int index) {
        int x = GameConfig.MENU_BUTTON_X;
        int y = GameConfig.MENU_BUTTON_START_Y + index * GameConfig.MENU_BUTTON_SPACING;
        return new Rectangle2D.Double(x, y, GameConfig.MENU_BUTTON_WIDTH, GameConfig.MENU_BUTTON_HEIGHT);
    }

    public static int getButtonIndexAt(Vector2D point) {
        for (int i = 0; i < 5; i++) {
            if (getButtonRect(i).contains(point.x(), point.y())) {
                return i;
            }
        }
        return -1;
    }
}