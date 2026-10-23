package football.physics;

import football.config.GameConfig;
import football.events.EventManager;
import football.model.Ball;
import football.model.Player;
import football.model.Vector2D;
import java.util.Collections;
import java.util.List;

/**
 * Main physics simulation engine.
 * Updates ball, players, handles collisions.
 */
public final class PhysicsEngine {

    public void update(Player p1, Player p2, Ball ball, EventManager eventManager) {
        // Update players
        updatePlayer(p1);
        updatePlayer(p2);

        // Player-player collision
        CollisionDetector.resolvePlayerPlayer(p1, p2);

        // Ball magnet effect - ball slows/stops near players
        applyBallMagnetEffect(ball, p1, p2);

        // Update ball
        updateBall(ball, eventManager);

        // Ball-player collisions
        CollisionDetector.resolveBallPlayer(ball, p1);
        CollisionDetector.resolveBallPlayer(ball, p2);

        // Ball-post collisions
        CollisionDetector.resolveBallPosts(ball);

        // Ball-wall collisions (with corner escape)
        CollisionDetector.resolveBallWalls(ball);
    }

    private void applyBallMagnetEffect(Ball ball, Player p1, Player p2) {
        Vector2D ballPos = ball.position();
        Vector2D ballVel = ball.velocity();

        // Check distance to each player
        double dist1 = Vector2D.distance(ballPos, p1.position());
        double dist2 = Vector2D.distance(ballPos, p2.position());

        double minDist = Math.min(dist1, dist2);

        // If ball is close to any player and moving slowly, apply stopping force
        if (minDist < GameConfig.BALL_STOP_MAX_DIST && ballVel.length() < 8.0) {
            double factor = 1.0 - (minDist / GameConfig.BALL_STOP_MAX_DIST);
            factor = Math.max(0, factor) * GameConfig.BALL_STOP_FORCE;

            // Apply damping to ball velocity
            ball.setVelocity(ballVel.multiply(1.0 - factor));
        }
    }

    private void updatePlayer(Player player) {
        Vector2D input = player.inputDirection();
        double ax = input.x();
        double ay = input.y();

        // Normalize diagonal movement
        if (ax != 0 && ay != 0) {
            ax *= GameConfig.DIAGONAL_SPEED_FACTOR;
            ay *= GameConfig.DIAGONAL_SPEED_FACTOR;
        }

        // Apply acceleration
        Vector2D newVelocity = player.velocity().add(new Vector2D(ax, ay).multiply(GameConfig.PLAYER_ACCELERATION));

        // Clamp to max speed
        newVelocity = newVelocity.clamp(GameConfig.PLAYER_MAX_SPEED);

        // Apply friction
        newVelocity = newVelocity.multiply(GameConfig.PLAYER_FRICTION);

        player.setVelocity(newVelocity);
        player.setPosition(player.position().add(newVelocity));

        // Clamp to field bounds
        double r = player.radius();
        double clampedX = Math.max(r, Math.min(GameConfig.WINDOW_WIDTH - r, player.position().x()));
        double clampedY = Math.max(r, Math.min(GameConfig.WINDOW_HEIGHT - r, player.position().y()));
        player.setPosition(new Vector2D(clampedX, clampedY));

        // Update charge
        player.increaseCharge();
    }

    private void updateBall(Ball ball, EventManager eventManager) {
        Vector2D pos = ball.position();
        Vector2D vel = ball.velocity();

        // Apply velocity
        Vector2D newPos = pos.add(vel);

        // Apply friction
        double friction = eventManager.ballFriction();
        Vector2D newVel = vel.multiply(friction);

        // Stop very slow movement
        if (Math.abs(newVel.x()) < GameConfig.MIN_VELOCITY) newVel = newVel.withX(0);
        if (Math.abs(newVel.y()) < GameConfig.MIN_VELOCITY) newVel = newVel.withY(0);

        ball.setPosition(newPos);
        ball.setVelocity(newVel);
    }

    public List<Vector2D> trail() {
        return Collections.emptyList();
    }

    public void clearTrail() {
        // No trail anymore
    }
}