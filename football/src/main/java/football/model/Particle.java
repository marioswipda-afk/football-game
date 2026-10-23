package football.model;

import football.config.GameConfig;
import java.awt.Color;

/**
 * Particle for visual effects (explosions, etc.).
 */
public final class Particle {

    private Vector2D position;
    private Vector2D velocity;
    private int life;
    private final Color color;

    public Particle(Vector2D position, Vector2D velocity, Color color) {
        this.position = position;
        this.velocity = velocity;
        this.life = GameConfig.PARTICLE_LIFE;
        this.color = color;
    }

    public Vector2D position() { return position; }
    public Vector2D velocity() { return velocity; }
    public int life() { return life; }
    public Color color() { return color; }

    public void update() {
        position = position.add(velocity);
        velocity = velocity.multiply(GameConfig.PARTICLE_FRICTION);
        life--;
    }

    public boolean isAlive() {
        return life > 0;
    }
}