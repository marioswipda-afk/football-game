package football.rendering;

import football.config.GameConfig;
import football.model.Particle;
import football.model.Ring;
import football.model.Vector2D;

import java.awt.*;
import java.util.List;

/**
 * Renders particles and rings for visual effects.
 */
public final class ParticleRenderer {

    ParticleRenderer() {}

    public static void draw(Graphics2D g, List<Particle> particles, List<Ring> rings,
                            BallState ballState, int flash, int shake) {
        // Flash effect
        if (flash > 0) {
            g.setColor(new Color(255, 255, 120, Math.min(255, flash * GameConfig.FLASH_ALPHA_FACTOR)));
            g.fillOval((int) ballState.position().x() - GameConfig.FLASH_RADIUS,
                    (int) ballState.position().y() - GameConfig.FLASH_RADIUS,
                    GameConfig.FLASH_RADIUS * 2, GameConfig.FLASH_RADIUS * 2);
        }

        // Rings
        for (Ring ring : rings) {
            if (ring.isAlive()) {
                int alpha = ring.alpha();
                g.setColor(new Color(ring.color().getRed(), ring.color().getGreen(),
                        ring.color().getBlue(), alpha));
                g.setStroke(new BasicStroke(4));
                int r = (int) ring.radius();
                g.drawOval((int) ring.position().x() - r, (int) ring.position().y() - r, 2 * r, 2 * r);
            }
        }

        // Particles
        for (Particle p : particles) {
            if (p.isAlive()) {
                g.setColor(p.color());
                g.fillOval((int) p.position().x() - 3, (int) p.position().y() - 3, 6, 6);
            }
        }
    }

    /**
     * Minimal ball state needed for rendering effects.
     */
    public record BallState(Vector2D position) {}
}