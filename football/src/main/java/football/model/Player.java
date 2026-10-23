package football.model;

import football.config.GameConfig;
import java.awt.Color;

/**
 * Player entity with position, velocity, and input state.
 * Mutable entity for game loop performance.
 */
public final class Player {

    private Vector2D position;
    private Vector2D velocity;
    private final double radius;
    private Color color;
    private Color darkColor;
    private final int upKey;
    private final int downKey;
    private final int leftKey;
    private final int rightKey;
    private final int kickKey;

    // Mutable state (input/charge)
    private Vector2D inputDirection = Vector2D.ZERO;
    private double charge = 0.0;
    private boolean charging = false;

    public Player(Vector2D position, Color color, Color darkColor,
                  int upKey, int downKey, int leftKey, int rightKey, int kickKey) {
        this.position = position;
        this.velocity = Vector2D.ZERO;
        this.radius = GameConfig.PLAYER_RADIUS;
        this.color = color;
        this.darkColor = darkColor;
        this.upKey = upKey;
        this.downKey = downKey;
        this.leftKey = leftKey;
        this.rightKey = rightKey;
        this.kickKey = kickKey;
    }

    // Getters
    public Vector2D position() { return position; }
    public Vector2D velocity() { return velocity; }
    public double radius() { return radius; }
    public Color color() { return color; }
    public Color darkColor() { return darkColor; }
    public int upKey() { return upKey; }
    public int downKey() { return downKey; }
    public int leftKey() { return leftKey; }
    public int rightKey() { return rightKey; }
    public int kickKey() { return kickKey; }
    public Vector2D inputDirection() { return inputDirection; }
    public double charge() { return charge; }
    public boolean charging() { return charging; }

    // Mutators
    public void setPosition(Vector2D position) { this.position = position; }
    public void setVelocity(Vector2D velocity) { this.velocity = velocity; }
    public void setInputDirection(Vector2D direction) { this.inputDirection = direction; }
    public void setColor(Color color) { this.color = color; }
    public void setDarkColor(Color darkColor) { this.darkColor = darkColor; }

    public void setCharging(boolean charging) {
        this.charging = charging;
        if (charging) this.charge = 0.0;
    }

    public void increaseCharge() {
        if (charging && charge < GameConfig.KICK_MAX_CHARGE) {
            charge = Math.min(GameConfig.KICK_MAX_CHARGE, charge + GameConfig.KICK_CHARGE_RATE);
        }
    }

    public void resetCharge() {
        this.charge = 0.0;
        this.charging = false;
    }

    /**
     * Check if player is close enough to ball to kick.
     */
    public boolean canKick(Vector2D ballPosition) {
        return Vector2D.distance(position, ballPosition) < radius + GameConfig.BALL_RADIUS + GameConfig.KICK_DETECTION_RADIUS;
    }

    /**
     * Check if player has ball (closer range).
     */
    public boolean hasBall(Vector2D ballPosition) {
        return Vector2D.distance(position, ballPosition) < radius + GameConfig.BALL_RADIUS + 40;
    }

    public void reset(Vector2D startPosition) {
        this.position = startPosition;
        this.velocity = Vector2D.ZERO;
        this.charge = 0.0;
        this.charging = false;
        this.inputDirection = Vector2D.ZERO;
    }
}