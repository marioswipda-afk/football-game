package football.events;

/**
 * Types of random events that can occur during gameplay.
 */
public enum EventType {
    NONE(""),
    WIND("VENTO"),
    MUD("LAMA"),
    ICE("GELO");

    private final String displayName;

    EventType(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public String eventMessage() {
        return switch (this) {
            case WIND -> "EVENTO: VENTO FORTE!";
            case MUD -> "EVENTO: LAMA NO CAMPO!";
            case ICE -> "EVENTO: CAMPO GELADO!";
            default -> "";
        };
    }
}