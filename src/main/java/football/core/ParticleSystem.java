package football.core;

import football.model.Particle;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages particle effects (explosions, etc.).
 */
public final class ParticleSystem {

    private final List<Particle> particles = new CopyOnWriteArrayList<>();

    public void addBurst(football.model.Vector2D position, java.awt.Color color, int count,
                         double speedMin, double speedMax) {
        for (int i = 0; i < count; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double speed = speedMin + Math.random() * (speedMax - speedMin);
            football.model.Vector2D velocity = new football.model.Vector2D(
                    Math.cos(angle) * speed, Math.sin(angle) * speed
            );
            particles.add(new Particle(position, velocity, color));
        }
    }

    public void update() {
        particles.forEach(Particle::update);
        particles.removeIf(p -> !p.isAlive());
    }

    public List<Particle> particles() {
        return particles;
    }

    public void clear() {
        particles.clear();
    }
}