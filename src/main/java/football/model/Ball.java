package football.model;

import football.config.GameConfig;

/**
 * Ball entity with position and velocity.
 * Mutable for game loop performance.
 */
public final class Ball {

    private Vector2D position;
    private Vector2D velocity;
    private final double radius;

    public Ball(Vector2D position) {
        this.position = position;
        this.velocity = Vector2D.ZERO;
        this.radius = GameConfig.BALL_RADIUS;
    }

    public Vector2D position() { return position; }
    public Vector2D velocity() { return velocity; }
    public double radius() { return radius; }

    public void setPosition(Vector2D position) { this.position = position; }
    public void setVelocity(Vector2D velocity) { this.velocity = velocity; }

    public void reset(Vector2D centerPosition) {
        this.position = centerPosition;
        this.velocity = Vector2D.ZERO;
    }
}