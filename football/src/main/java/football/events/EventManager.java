package football.events;

import football.config.GameConfig;
import football.model.Vector2D;
import java.util.Random;

/**
 * Manages random events during gameplay (wind, mud, ice).
 * Currently disabled per user request.
 */
public final class EventManager {

    private EventType activeEvent = EventType.NONE;
    private int eventTicksLeft = 0;
    private int nextEventTick = Integer.MAX_VALUE; // Never trigger
    private Vector2D windVector = Vector2D.ZERO;
    private String lastEventMessage = "Kick off!";
    private final Random random = new Random();

    public EventManager() {
        // Events disabled
    }

    public void update(int currentTick) {
        // Events disabled - no-op
    }

    public void reset() {
        activeEvent = EventType.NONE;
        eventTicksLeft = 0;
        windVector = Vector2D.ZERO;
        lastEventMessage = "Kick off!";
    }

    // Getters
    public EventType activeEvent() { return EventType.NONE; }
    public Vector2D windVector() { return Vector2D.ZERO; }
    public String lastEventMessage() { return lastEventMessage; }
    public boolean hasActiveEvent() { return false; }

    public double ballFriction() {
        return GameConfig.BALL_FRICTION_NORMAL;
    }
}