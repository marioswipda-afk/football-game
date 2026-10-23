package football.model;

import football.config.GameConfig;
import java.awt.Color;

/**
 * Expanding ring effect for super kicks.
 */
public final class Ring {

    private final Vector2D position;
    private double radius;
    private int life;
    private final Color color;

    public Ring(Vector2D position, Color color) {
        this.position = position;
        this.radius = GameConfig.RING_INITIAL_RADIUS;
        this.life = GameConfig.RING_LIFE;
        this.color = color;
    }

    public Vector2D position() { return position; }
    public double radius() { return radius; }
    public int life() { return life; }
    public Color color() { return color; }

    public void update() {
        radius += GameConfig.RING_EXPANSION_RATE;
        life--;
    }

    public boolean isAlive() {
        return life > 0;
    }

    public int alpha() {
        return Math.max(0, life * GameConfig.RING_ALPHA_FACTOR);
    }
}