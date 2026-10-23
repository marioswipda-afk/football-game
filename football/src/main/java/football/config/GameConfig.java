package football.config;

/**
 * Centralized game configuration constants.
 * Single source of truth for all game parameters.
 */
public final class GameConfig {

    // Window
    public static final int WINDOW_WIDTH = 960;
    public static final int WINDOW_HEIGHT = 640;
    public static final String WINDOW_TITLE = "Soccer Ball Showdown";
    public static final int TARGET_FPS = 60;
    public static final int TICK_MS = 1000 / TARGET_FPS; // ~16ms

    // Field
    public static final int GOAL_WIDTH = 150;
    public static final int GOAL_DEPTH = 30;
    public static final int GOAL_TOP = WINDOW_HEIGHT / 2 - GOAL_WIDTH / 2;
    public static final int GOAL_BOTTOM = WINDOW_HEIGHT / 2 + GOAL_WIDTH / 2;
    public static final int BALL_RADIUS = 13;
    public static final int PLAYER_RADIUS = 20;

    // Gameplay - ADJUSTED: faster players, ball stops near player
    public static final int WIN_SCORE = 5;
    public static final double PLAYER_MAX_SPEED = 7.5;        // Was 5.2 - faster
    public static final double PLAYER_ACCELERATION = 1.1;     // Was 0.7 - more responsive
    public static final double PLAYER_FRICTION = 0.85;        // Was 0.82 - slightly more slide
    public static final double DIAGONAL_SPEED_FACTOR = 0.7071;

    // Ball stops when player is near (magnet effect)
    public static final double BALL_STOP_DISTANCE = 45.0;     // Distance to start slowing ball
    public static final double BALL_STOP_FORCE = 0.15;        // How strongly ball stops
    public static final double BALL_STOP_MAX_DIST = 60.0;     // Max distance for effect

    // Kick mechanics
    public static final double KICK_BASE_POWER = 8.0;         // Was 7.0
    public static final double KICK_CHARGE_MULTIPLIER = 18.0; // Was 17.0
    public static final double KICK_CHARGE_RATE = 0.022;
    public static final double KICK_MAX_CHARGE = 1.0;
    public static final double SUPER_KICK_THRESHOLD = 0.55;
    public static final double KICK_DETECTION_RADIUS = 34.0;
    public static final double BALL_INHERIT_VELOCITY_FACTOR = 0.6;
    public static final double COLLISION_RESTITUTION = 1.8;

    // Ball physics - less sticky in corners
    public static final double BALL_FRICTION_NORMAL = 0.985;
    public static final double BALL_FRICTION_MUD = 0.94;
    public static final double BALL_FRICTION_ICE = 0.997;
    public static final double BALL_WALL_RESTITUTION = 0.8;   // Was 0.75 - bouncier
    public static final double BALL_POST_RESTITUTION = 0.8;   // Was 0.7 - bouncier
    public static final double MIN_VELOCITY = 0.05;
    public static final double CORNER_ESCAPE_BOOST = 1.2;     // Extra push out of corners

    // Trail
    public static final int MAX_TRAIL_POINTS = 10;
    public static final double TRAIL_VELOCITY_THRESHOLD = 7.0;

    // Replay - REDUCED to 3 seconds (180 frames at 60fps)
    public static final int MAX_SNAPSHOTS = 180;              // 3 seconds
    public static final int REPLAY_EXTRA_FRAMES = 30;         // Was 90 - just 0.5s extra

    // Events (disabled)
    public static final int EVENT_DURATION_TICKS = 0;
    public static final int EVENT_INTERVAL_BASE = 0;
    public static final int EVENT_INTERVAL_VARIANCE = 0;
    public static final int FIRST_EVENT_DELAY = 0;
    public static final double WIND_FORCE = 0.0;

    // Particles
    public static final int PARTICLE_LIFE = 30;
    public static final int PARTICLE_COUNT_NORMAL = 10;
    public static final int PARTICLE_COUNT_SUPER = 24;
    public static final double PARTICLE_SPEED_MIN = 2.0;
    public static final double PARTICLE_SPEED_MAX = 7.0;
    public static final double PARTICLE_FRICTION = 0.9;

    // Rings
    public static final int RING_INITIAL_RADIUS = 10;
    public static final int RING_LIFE = 20;
    public static final double RING_EXPANSION_RATE = 4.0;
    public static final int RING_ALPHA_FACTOR = 10;

    // Visual effects
    public static final int SCREEN_SHAKE_DURATION_NORMAL = 10;
    public static final int SCREEN_SHAKE_DURATION_GOAL = 14;
    public static final int SCREEN_SHAKE_INTENSITY = 12;
    public static final int FLASH_DURATION = 10;
    public static final int FLASH_RADIUS = 40;
    public static final int FLASH_ALPHA_FACTOR = 20;

    // Colors
    public static final int PLAYER1_COLOR_R = 220, PLAYER1_COLOR_G = 60, PLAYER1_COLOR_B = 60;
    public static final int PLAYER1_DARK_R = 120, PLAYER1_DARK_G = 0, PLAYER1_DARK_B = 0;
    public static final int PLAYER2_COLOR_R = 60, PLAYER2_COLOR_G = 110, PLAYER2_COLOR_B = 230;
    public static final int PLAYER2_DARK_R = 0, PLAYER2_DARK_G = 20, PLAYER2_DARK_B = 130;
    public static final int FIELD_LIGHT_R = 44, FIELD_LIGHT_G = 160, FIELD_LIGHT_B = 44;
    public static final int FIELD_DARK_R = 34, FIELD_DARK_G = 139, FIELD_DARK_B = 34;
    public static final int MUD_COLOR_R = 120, MUD_COLOR_G = 80, MUD_COLOR_B = 30, MUD_COLOR_A = 70;
    public static final int ICE_COLOR_R = 150, ICE_COLOR_G = 220, ICE_COLOR_B = 255, ICE_COLOR_A = 60;
    public static final int REPLAY_BG_R = 20, REPLAY_BG_G = 80, REPLAY_BG_B = 20;
    public static final int REPLAY_OVERLAY_R = 0, REPLAY_OVERLAY_G = 0, REPLAY_OVERLAY_B = 0, REPLAY_OVERLAY_A = 180;
    public static final int MENU_OVERLAY_R = 0, MENU_OVERLAY_G = 0, MENU_OVERLAY_B = 0, MENU_OVERLAY_A = 140;
    public static final int WIN_OVERLAY_R = 0, WIN_OVERLAY_G = 0, WIN_OVERLAY_B = 0, WIN_OVERLAY_A = 170;

    // UI
    public static final int MENU_BUTTON_WIDTH = 300;
    public static final int MENU_BUTTON_HEIGHT = 48;
    public static final int MENU_BUTTON_X = 330;
    public static final int MENU_BUTTON_START_Y = 190;
    public static final int MENU_BUTTON_SPACING = 65;
    public static final int CHARGE_BAR_WIDTH = 44;
    public static final int CHARGE_BAR_HEIGHT = 8;
    public static final int CHARGE_BAR_OFFSET_Y = 18;

    // Sound
    public static final int SAMPLE_RATE = 22050;
    public static final float SOUND_VOLUME = 0.6f;
    public static final float EVENT_SOUND_VOLUME = 0.4f;

    private GameConfig() {
        // Utility class
    }
}