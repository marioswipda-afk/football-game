package football.core;

import football.model.Ring;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages expanding ring effects (super kick visual).
 */
public final class RingSystem {

    private final List<Ring> rings = new CopyOnWriteArrayList<>();

    public void addRing(football.model.Vector2D position, java.awt.Color color) {
        rings.add(new Ring(position, color));
    }

    public void update() {
        rings.forEach(Ring::update);
        rings.removeIf(r -> !r.isAlive());
    }

    public List<Ring> rings() {
        return rings;
    }

    public void clear() {
        rings.clear();
    }
}