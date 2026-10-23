package football;

import football.config.GameConfig;
import football.core.GameEngine;
import football.model.GameState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Main game class - entry point and Swing integration.
 * Thin wrapper around GameEngine.
 */
public final class FootballGame extends JPanel {

    private final GameEngine engine;
    private boolean fullscreen = false;
    private static JFrame frame;

    public FootballGame() {
        this.engine = new GameEngine();

        setPreferredSize(new Dimension(GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT));
        setBackground(new Color(GameConfig.FIELD_DARK_R, GameConfig.FIELD_DARK_G, GameConfig.FIELD_DARK_B));
        setFocusable(true);

        // Input listeners
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                engine.keyPressed(e.getKeyCode());
                handleGlobalKeys(e.getKeyCode());
            }

            @Override
            public void keyReleased(KeyEvent e) {
                engine.keyReleased(e.getKeyCode());
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                double scale = calculateScale();
                double offsetX = calculateOffsetX(scale);
                double offsetY = calculateOffsetY(scale);
                engine.mouseClicked(e.getX(), e.getY(), scale, offsetX, offsetY);
            }
        });

        // Game loop timer
        new Timer(GameConfig.TICK_MS, e -> {
            engine.update();
            repaint();
        }).start();
    }

    private void handleGlobalKeys(int keyCode) {
        if (keyCode == KeyEvent.VK_F11) {
            toggleFullscreen();
        }
        if (keyCode == KeyEvent.VK_R && engine.state() == GameState.MENU) {
            // Handled by engine
        }
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        engine.render(g, getWidth(), getHeight());
    }

    private double calculateScale() {
        return Math.min(getWidth() / (double) GameConfig.WINDOW_WIDTH,
                        getHeight() / (double) GameConfig.WINDOW_HEIGHT);
    }

    private double calculateOffsetX(double scale) {
        return (getWidth() - GameConfig.WINDOW_WIDTH * scale) / 2.0;
    }

    private double calculateOffsetY(double scale) {
        return (getHeight() - GameConfig.WINDOW_HEIGHT * scale) / 2.0;
    }

    private void toggleFullscreen() {
        fullscreen = !fullscreen;
        frame.setExtendedState(fullscreen ? JFrame.MAXIMIZED_BOTH : JFrame.NORMAL);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            frame = new JFrame(GameConfig.WINDOW_TITLE);
            FootballGame game = new FootballGame();
            frame.add(game);
            frame.pack();
            frame.setResizable(true);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}