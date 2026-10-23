package football.rendering;

import football.config.GameConfig;
import football.core.GameEngine;
import football.core.ParticleSystem;
import football.core.ReplaySystem;
import football.core.RingSystem;
import football.events.EventManager;
import football.model.*;
import football.physics.PhysicsEngine;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Main renderer coordinating all rendering subsystems.
 * Handles coordinate transformation and screen shake.
 */
public final class GameRenderer {

    private final FieldRenderer fieldRenderer = new FieldRenderer();
    private final ParticleRenderer particleRenderer = new ParticleRenderer();
    private final UIRenderer uiRenderer = new UIRenderer();
    private final RenderUtils renderUtils = new RenderUtils();

    private int shake = 0;
    private int flash = 0;

    public void render(Graphics2D g, GameState state, Player p1, Player p2, Ball ball,
                       PhysicsEngine physicsEngine, EventManager eventManager,
                       ParticleSystem particleSystem, RingSystem ringSystem,
                       ReplaySystem replaySystem, int score1, int score2,
                       String lastEvent, boolean botMode,
                       Color ballColor, String player1Name, String player2Name,
                       GameEngine.MenuScreen menuScreen, int customizeStep, String textInput) {

        // Clear screen
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);

        // Apply transformations
        double scale = renderUtils.calculateScale(g.getClipBounds().width, g.getClipBounds().height);
        double offsetX = renderUtils.calculateOffsetX(g.getClipBounds().width, scale);
        double offsetY = renderUtils.calculateOffsetY(g.getClipBounds().height, scale);

        g.translate(offsetX, offsetY);
        g.scale(scale, scale);

        // Screen shake
        if (shake > 0) {
            g.translate((Math.random() - 0.5) * GameConfig.SCREEN_SHAKE_INTENSITY,
                    (Math.random() - 0.5) * GameConfig.SCREEN_SHAKE_INTENSITY);
        }

        switch (state) {
            case MENU -> uiRenderer.drawMenu(g, botMode, ballColor, player1Name, player2Name, menuScreen, customizeStep, textInput);
            case PLAY -> drawGameplay(g, p1, p2, ball, physicsEngine, eventManager,
                    particleSystem, ringSystem, score1, score2, lastEvent, ballColor, player1Name, player2Name);
            case REPLAY -> uiRenderer.drawReplay(g, replaySystem.frames(), replaySystem.currentIndex(), p1, p2);
            case WIN -> uiRenderer.drawWin(g, score1, score2, botMode, player1Name, player2Name);
        }
    }

    private void drawGameplay(Graphics2D g, Player p1, Player p2, Ball ball,
                              PhysicsEngine physicsEngine, EventManager eventManager,
                              ParticleSystem particleSystem, RingSystem ringSystem,
                              int score1, int score2, String lastEvent,
                              Color ballColor, String player1Name, String player2Name) {

        // Field
        fieldRenderer.draw(g, eventManager);

        // Effects (particles, rings, flash)
        ParticleRenderer.BallState ballState = new ParticleRenderer.BallState(ball.position());
        particleRenderer.draw(g, particleSystem.particles(), ringSystem.rings(),
                ballState, flash, shake);

        // Ball - use custom color
        renderUtils.drawBall(g, ball.position().x(), ball.position().y(),
                ball.radius(), ballColor, Color.BLACK);

        // Players
        renderUtils.drawBall(g, p1.position().x(), p1.position().y(),
                p1.radius(), p1.color(), p1.darkColor());
        renderUtils.drawBall(g, p2.position().x(), p2.position().y(),
                p2.radius(), p2.color(), p2.darkColor());

        // Charge meters
        renderUtils.drawChargeMeter(g, p1);
        renderUtils.drawChargeMeter(g, p2);

        // HUD
        uiRenderer.drawHUD(g, score1, score2, eventManager, lastEvent, player1Name, player2Name);
    }

    // Effect triggers
    public void triggerShake(int duration) { this.shake = duration; }
    public void triggerFlash(int duration) { this.flash = duration; }
    public void updateEffects() {
        if (shake > 0) shake--;
        if (flash > 0) flash--;
    }

    public int shake() { return shake; }
    public int flash() { return flash; }
}