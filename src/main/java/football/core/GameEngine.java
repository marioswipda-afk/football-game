package football.core;

import football.config.GameConfig;
import football.core.ParticleSystem;
import football.core.ReplaySystem;
import football.core.RingSystem;
import football.events.EventManager;
import football.events.EventType;
import football.input.BotInput;
import football.input.InputHandler;
import football.model.*;
import football.physics.CollisionDetector;
import football.physics.PhysicsEngine;
import football.rendering.GameRenderer;
import football.rendering.RenderUtils;
import football.rendering.UIRenderer;
import football.audio.SoundManager;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

/**
 * Core game engine - coordinates all game systems.
 * Contains game logic but no rendering or input handling directly.
 */
public final class GameEngine {

    private final Player p1;
    private final Player p2;
    private final Ball ball;
    private final PhysicsEngine physicsEngine;
    private final EventManager eventManager;
    private final InputHandler inputHandler;
    private final ParticleSystem particleSystem;
    private final RingSystem ringSystem;
    private final ReplaySystem replaySystem;
    private final GameRenderer renderer;

    private GameState state = GameState.MENU;
    private int score1 = 0;
    private int score2 = 0;
    private String lastEvent = "Kick off!";
    private int tick = 0;
    private boolean botMode = false;
    
    // Player customization
    private String player1Name = "Jogador 1";
    private String player2Name = "Jogador 2";
    private Color ballColor = Color.WHITE;
    private Color player1Color = new Color(GameConfig.PLAYER1_COLOR_R, GameConfig.PLAYER1_COLOR_G, GameConfig.PLAYER1_COLOR_B);
    private Color player2Color = new Color(GameConfig.PLAYER2_COLOR_R, GameConfig.PLAYER2_COLOR_G, GameConfig.PLAYER2_COLOR_B);
    
    // Menu navigation
    private enum MenuScreen { MAIN, CUSTOMIZE }
    private MenuScreen menuScreen = MenuScreen.MAIN;
    private int customizeStep = 0; // 0=P1 name, 1=P2 name, 2=ball color, 3=P1 color, 4=P2 color
    private StringBuilder textInput = new StringBuilder();

    public GameEngine() {
        // Initialize entities
        this.p1 = new Player(
                new Vector2D(150, GameConfig.WINDOW_HEIGHT / 2.0),
                new Color(GameConfig.PLAYER1_COLOR_R, GameConfig.PLAYER1_COLOR_G, GameConfig.PLAYER1_COLOR_B),
                new Color(GameConfig.PLAYER1_DARK_R, GameConfig.PLAYER1_DARK_G, GameConfig.PLAYER1_DARK_B),
                KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_F
        );

        this.p2 = new Player(
                new Vector2D(GameConfig.WINDOW_WIDTH - 150, GameConfig.WINDOW_HEIGHT / 2.0),
                new Color(GameConfig.PLAYER2_COLOR_R, GameConfig.PLAYER2_COLOR_G, GameConfig.PLAYER2_COLOR_B),
                new Color(GameConfig.PLAYER2_DARK_R, GameConfig.PLAYER2_DARK_G, GameConfig.PLAYER2_DARK_B),
                KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_ENTER
        );

        this.ball = new Ball(new Vector2D(GameConfig.WINDOW_WIDTH / 2.0, GameConfig.WINDOW_HEIGHT / 2.0));

        // Initialize systems
        this.physicsEngine = new PhysicsEngine();
        this.eventManager = new EventManager();
        this.inputHandler = new InputHandler();
        this.particleSystem = new ParticleSystem();
        this.ringSystem = new RingSystem();
        this.replaySystem = new ReplaySystem();
        this.renderer = new GameRenderer();
    }

    public void update() {
        tick++;

        switch (state) {
            case PLAY -> updatePlay();
            case REPLAY -> updateReplay();
            case WIN -> {} // No update needed
            case MENU -> {} // No update needed
        }

        renderer.updateEffects();
    }

    private void updatePlay() {
        // Handle input
        inputHandler.update(p1, p2, ball);

        // Handle kick input
        if (inputHandler.isP1KickPressed(p1) && p1.hasBall(ball.position())) {
            p1.setCharging(true);
        }
        if (!botMode && inputHandler.isP2KickPressed(p2) && p2.hasBall(ball.position())) {
            p2.setCharging(true);
        }

        // Physics
        physicsEngine.update(p1, p2, ball, eventManager);

        // Events
        eventManager.update(tick);

        // Record for replay
        replaySystem.recordSnapshot(p1, p2, ball);

        // Check for goal
        int goal = CollisionDetector.checkGoal(ball);
        if (goal != 0) {
            handleGoal(goal);
        }
    }

    private void updateReplay() {
        replaySystem.advanceReplay();
        particleSystem.update();
        ringSystem.update();

        if (replaySystem.isReplayFinished(GameConfig.REPLAY_EXTRA_FRAMES)) {
            if (state != GameState.WIN) {
                resetPositions();
                state = GameState.PLAY;
                replaySystem.reset();
                eventManager.reset();
            }
        }
    }

    private void handleGoal(int scorer) {
        if (scorer == 1) {
            score1++;
            lastEvent = "GOL! Jogador 1 marcou!";
        } else {
            score2++;
            lastEvent = "GOL! Jogador 2 marcou!";
        }

        SoundManager.playGoal();
        renderer.triggerShake(GameConfig.SCREEN_SHAKE_DURATION_GOAL);
        renderer.triggerFlash(GameConfig.FLASH_DURATION);

        replaySystem.stopRecording();

        if (score1 >= GameConfig.WIN_SCORE || score2 >= GameConfig.WIN_SCORE) {
            state = GameState.WIN;
            SoundManager.playWin();
        } else {
            state = GameState.REPLAY;
            replaySystem.startReplay();
        }
    }

    public void render(Graphics2D g, int windowWidth, int windowHeight) {
        renderer.render(g, state, p1, p2, ball, physicsEngine, eventManager,
                particleSystem, ringSystem, replaySystem, score1, score2, lastEvent, botMode,
                ballColor, player1Name, player2Name, menuScreen, customizeStep, textInput.toString());
    }

    // Input callbacks
    public void keyReleased(int keyCode) {
        inputHandler.keyReleased(keyCode);

        // Handle kick release
        if (state == GameState.PLAY) {
            if (keyCode == p1.kickKey() && p1.charging()) {
                kick(p1);
            }
            if (!botMode && keyCode == p2.kickKey() && p2.charging()) {
                kick(p2);
            }
        }
    }

    public void keyTyped(char c) {
        if (state == GameState.MENU && menuScreen == MenuScreen.CUSTOMIZE) {
            if (c == '\b') { // Backspace
                if (textInput.length() > 0) {
                    textInput.deleteCharAt(textInput.length() - 1);
                }
            } else if (c == '\n' || c == '\r') { // Enter - confirm
                applyCustomizeInput();
            } else if (textInput.length() < 16 && (Character.isLetterOrDigit(c) || c == ' ')) {
                textInput.append(c);
            }
        }
    }

    public void keyPressed(int keyCode) {
        inputHandler.keyPressed(keyCode);

        // Global keys
        if (keyCode == KeyEvent.VK_R) {
            resetGame();
        }
        if (keyCode == KeyEvent.VK_ESCAPE) {
            if (state == GameState.PLAY) {
                state = GameState.MENU;
            } else if (state == GameState.MENU && menuScreen == MenuScreen.CUSTOMIZE) {
                menuScreen = MenuScreen.MAIN;
                customizeStep = 0;
                textInput.setLength(0);
            }
        }
    }

    public void mouseClicked(int x, int y, double scale, double offsetX, double offsetY) {
        if (state == GameState.WIN) {
            state = GameState.MENU;
            return;
        }
        if (state != GameState.MENU) return;

        Vector2D gamePoint = RenderUtils.screenToGame(x, y, offsetX, offsetY, scale);
        int buttonIndex = UIRenderer.getButtonIndexAt(gamePoint);

        if (buttonIndex >= 0) {
            handleMenuButton(buttonIndex);
        }
    }

    private void handleMenuButton(int index) {
        if (menuScreen == MenuScreen.CUSTOMIZE) {
            handleCustomizeButton(index);
            return;
        }
        switch (index) {
            case 0 -> { botMode = false; startMatch(); }
            case 1 -> { botMode = true; inputHandler.setBotMode(true); startMatch(); }
            case 2 -> { menuScreen = MenuScreen.CUSTOMIZE; customizeStep = 0; textInput.setLength(0); }
            case 3 -> toggleFullscreen(); // Handled by main class
        }
    }

    private void handleCustomizeButton(int index) {
        // Customize screen has: [0] P1 Name, [1] P2 Name, [2] Ball Color, [3] P1 Color, [4] P2 Color, [5] Back
        switch (index) {
            case 0 -> { customizeStep = 0; textInput.setLength(0); textInput.append(player1Name); }
            case 1 -> { customizeStep = 1; textInput.setLength(0); textInput.append(botMode ? "Bot" : player2Name); }
            case 2 -> { customizeStep = 2; cycleBallColor(); }
            case 3 -> { customizeStep = 3; cyclePlayer1Color(); }
            case 4 -> { customizeStep = 4; cyclePlayer2Color(); }
            case 5 -> { menuScreen = MenuScreen.MAIN; customizeStep = 0; textInput.setLength(0); }
        }
    }

    private void applyCustomizeInput() {
        String input = textInput.toString().trim();
        if (input.isEmpty()) return;
        switch (customizeStep) {
            case 0 -> player1Name = input;
            case 1 -> player2Name = input;
        }
        textInput.setLength(0);
    }

    private void cycleBallColor() {
        Color[] colors = {Color.WHITE, Color.YELLOW, Color.ORANGE, Color.PINK, Color.CYAN, new Color(50, 205, 50), Color.MAGENTA};
        int idx = 0;
        for (int i = 0; i < colors.length; i++) {
            if (colors[i].equals(ballColor)) { idx = (i + 1) % colors.length; break; }
        }
        ballColor = colors[idx];
    }

    private void cyclePlayer1Color() {
        Color[] colors = {
            new Color(220, 60, 60), new Color(60, 220, 60), new Color(60, 60, 220),
            new Color(220, 220, 60), new Color(220, 60, 220), new Color(60, 220, 220),
            new Color(255, 140, 0), new Color(140, 0, 255)
        };
        int idx = 0;
        for (int i = 0; i < colors.length; i++) {
            if (colors[i].equals(player1Color)) { idx = (i + 1) % colors.length; break; }
        }
        player1Color = colors[idx];
        // Update player color
        p1.setColor(player1Color);
        p1.setDarkColor(player1Color.darker());
    }

    private void cyclePlayer2Color() {
        Color[] colors = {
            new Color(60, 110, 230), new Color(220, 60, 60), new Color(60, 220, 60),
            new Color(60, 60, 220), new Color(220, 220, 60), new Color(220, 60, 220),
            new Color(60, 220, 220), new Color(255, 140, 0)
        };
        int idx = 0;
        for (int i = 0; i < colors.length; i++) {
            if (colors[i].equals(player2Color)) { idx = (i + 1) % colors.length; break; }
        }
        player2Color = colors[idx];
        // Update player color
        p2.setColor(player2Color);
        p2.setDarkColor(player2Color.darker());
    }

    private void kick(Player player) {
        Vector2D delta = ball.position().subtract(player.position());
        double distance = delta.length();

        if (distance < player.radius() + ball.radius() + GameConfig.KICK_DETECTION_RADIUS) {
            if (distance < 1) {
                delta = new Vector2D(player == p1 ? 1 : -1, 0);
                distance = 1;
            }

            double power = GameConfig.KICK_BASE_POWER + player.charge() * GameConfig.KICK_CHARGE_MULTIPLIER;
            Vector2D kickVelocity = delta.normalized().multiply(power)
                    .add(player.velocity().multiply(GameConfig.BALL_INHERIT_VELOCITY_FACTOR));

            ball.setVelocity(kickVelocity);

            SoundManager.playKick(player.charge());
            boolean superKick = player.charge() > GameConfig.SUPER_KICK_THRESHOLD;

            particleSystem.addBurst(ball.position(),
                    superKick ? Color.YELLOW : Color.ORANGE,
                    superKick ? GameConfig.PARTICLE_COUNT_SUPER : GameConfig.PARTICLE_COUNT_NORMAL,
                    GameConfig.PARTICLE_SPEED_MIN, GameConfig.PARTICLE_SPEED_MAX);

            ringSystem.addRing(ball.position(), superKick ? Color.YELLOW : Color.WHITE);

            if (superKick) {
                lastEvent = "SUPER CHUTE!";
                renderer.triggerShake(GameConfig.SCREEN_SHAKE_DURATION_NORMAL);
                renderer.triggerFlash(GameConfig.FLASH_DURATION);
            } else {
                lastEvent = "Chute!";
            }
        }

        player.resetCharge();
    }

    private void startMatch() {
        score1 = 0;
        score2 = 0;
        lastEvent = "Kick off!";
        eventManager.reset();
        tick = 0;
        resetPositions();
        state = GameState.PLAY;
        replaySystem.startRecording();
        inputHandler.clear();
    }

    private void resetPositions() {
        p1.reset(new Vector2D(150, GameConfig.WINDOW_HEIGHT / 2.0));
        p2.reset(new Vector2D(GameConfig.WINDOW_WIDTH - 150, GameConfig.WINDOW_HEIGHT / 2.0));
        ball.reset(new Vector2D(GameConfig.WINDOW_WIDTH / 2.0, GameConfig.WINDOW_HEIGHT / 2.0));
        physicsEngine.clearTrail();
        particleSystem.clear();
        ringSystem.clear();
    }

    private void resetGame() {
        score1 = 0;
        score2 = 0;
        lastEvent = "Menu";
        state = GameState.MENU;
        eventManager.reset();
        physicsEngine.clearTrail();
        particleSystem.clear();
        ringSystem.clear();
        replaySystem.reset();
        inputHandler.clear();
        resetPositions();
    }

    private void toggleFullscreen() {
        // Handled by main class - just a placeholder
    }

    // Getters for main class
    public GameState state() { return state; }
    public int score1() { return score1; }
    public int score2() { return score2; }
    public boolean botMode() { return botMode; }
    public void setBotMode(boolean botMode) { this.botMode = botMode; }
    public void setFullscreen(boolean fullscreen) { /* handled by main */ }
    
    // Customization getters
    public String player1Name() { return player1Name; }
    public String player2Name() { return player2Name; }
    public Color ballColor() { return ballColor; }
    public Color player1Color() { return player1Color; }
    public Color player2Color() { return player2Color; }
    public MenuScreen menuScreen() { return menuScreen; }
    public int customizeStep() { return customizeStep; }
    public String textInput() { return textInput.toString(); }
}