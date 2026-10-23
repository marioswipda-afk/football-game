package football.model;

/**
 * Immutable 2D vector with basic math operations.
 * Using a record for immutability and concise syntax (Java 21).
 */
public record Vector2D(double x, double y) {

    public static final Vector2D ZERO = new Vector2D(0, 0);

    public double length() {
        return Math.hypot(x, y);
    }

    public double lengthSquared() {
        return x * x + y * y;
    }

    public Vector2D add(Vector2D other) {
        return new Vector2D(x + other.x, y + other.y);
    }

    public Vector2D subtract(Vector2D other) {
        return new Vector2D(x - other.x, y - other.y);
    }

    public Vector2D multiply(double scalar) {
        return new Vector2D(x * scalar, y * scalar);
    }

    public Vector2D divide(double scalar) {
        return new Vector2D(x / scalar, y / scalar);
    }

    public Vector2D normalized() {
        double len = length();
        return len > 0 ? divide(len) : ZERO;
    }

    public double dot(Vector2D other) {
        return x * other.x + y * other.y;
    }

    public Vector2D withX(double newX) {
        return new Vector2D(newX, y);
    }

    public Vector2D withY(double newY) {
        return new Vector2D(x, newY);
    }

    public Vector2D clamp(double maxLength) {
        double len = length();
        return len > maxLength ? multiply(maxLength / len) : this;
    }

    public static double distance(Vector2D a, Vector2D b) {
        return a.subtract(b).length();
    }
}