package football.physics;

import football.config.GameConfig;
import football.model.Ball;
import football.model.Player;
import football.model.Vector2D;

/**
 * Handles all collision detection and resolution.
 */
public final class CollisionDetector {

    private CollisionDetector() {}

    /**
     * Resolve collision between two players.
     */
    public static void resolvePlayerPlayer(Player p1, Player p2) {
        Vector2D delta = p2.position().subtract(p1.position());
        double distance = delta.length();
        double minDistance = p1.radius() + p2.radius();

        if (distance > 0 && distance < minDistance) {
            double push = (minDistance - distance) / 2.0;
            Vector2D normal = delta.normalized();

            p1.setPosition(p1.position().subtract(normal.multiply(push)));
            p2.setPosition(p2.position().add(normal.multiply(push)));

            double relativeVelocity = p2.velocity().subtract(p1.velocity()).dot(normal);
            if (relativeVelocity < 0) {
                Vector2D impulse = normal.multiply(relativeVelocity);
                p1.setVelocity(p1.velocity().add(impulse));
                p2.setVelocity(p2.velocity().subtract(impulse));
            }
        }
    }

    /**
     * Resolve collision between ball and player.
     * Returns true if collision occurred.
     */
    public static boolean resolveBallPlayer(Ball ball, Player player) {
        Vector2D delta = ball.position().subtract(player.position());
        double distance = delta.length();
        double minDistance = player.radius() + ball.radius();

        if (distance > 0 && distance < minDistance) {
            Vector2D normal = delta.normalized();
            ball.setPosition(ball.position().add(normal.multiply(minDistance - distance)));

            double relativeVelocity = ball.velocity().subtract(player.velocity()).dot(normal);
            if (relativeVelocity < 0) {
                Vector2D impulse = normal.multiply(relativeVelocity * GameConfig.COLLISION_RESTITUTION);
                ball.setVelocity(ball.velocity().subtract(impulse));
            }
            return true;
        }
        return false;
    }

    /**
     * Check and resolve ball collision with goal posts.
     */
    public static void resolveBallPosts(Ball ball) {
        double r = ball.radius();
        // Left posts
        resolvePostCollision(ball, 0, GameConfig.GOAL_TOP, r);
        resolvePostCollision(ball, 0, GameConfig.GOAL_BOTTOM, r);
        // Right posts
        resolvePostCollision(ball, GameConfig.WINDOW_WIDTH, GameConfig.GOAL_TOP, r);
        resolvePostCollision(ball, GameConfig.WINDOW_WIDTH, GameConfig.GOAL_BOTTOM, r);
    }

    private static void resolvePostCollision(Ball ball, double px, double py, double ballRadius) {
        Vector2D postPos = new Vector2D(px, py);
        Vector2D delta = ball.position().subtract(postPos);
        double distance = delta.length();

        if (distance > 0 && distance < ballRadius) {
            Vector2D normal = delta.normalized();
            ball.setPosition(ball.position().add(normal.multiply(ballRadius - distance)));

            double dot = ball.velocity().dot(normal);
            ball.setVelocity(ball.velocity().subtract(normal.multiply(2 * dot)).multiply(GameConfig.BALL_POST_RESTITUTION));
        }
    }

    /**
     * Check if ball scored a goal.
     * Returns 1 if player 1 scored, 2 if player 2 scored, 0 if no goal.
     */
    public static int checkGoal(Ball ball) {
        double r = ball.radius();
        double bx = ball.position().x();
        double by = ball.position().y();

        // Left goal (player 2 scores)
        if (bx < r) {
            if (by > GameConfig.GOAL_TOP + r && by < GameConfig.GOAL_BOTTOM - r) {
                return 2;
            }
        }

        // Right goal (player 1 scores)
        if (bx > GameConfig.WINDOW_WIDTH - r) {
            if (by > GameConfig.GOAL_TOP + r && by < GameConfig.GOAL_BOTTOM - r) {
                return 1;
            }
        }

        return 0;
    }

    /**
     * Resolve ball collision with walls (top/bottom/sides except goals).
     * Includes corner escape boost to prevent ball getting stuck.
     */
    public static void resolveBallWalls(Ball ball) {
        double r = ball.radius();
        double bx = ball.position().x();
        double by = ball.position().y();
        double vx = ball.velocity().x();
        double vy = ball.velocity().y();

        boolean hitTop = by < r;
        boolean hitBottom = by > GameConfig.WINDOW_HEIGHT - r;
        boolean hitLeft = bx < r;
        boolean hitRight = bx > GameConfig.WINDOW_WIDTH - r;

        // Check if in corner (near goal posts)
        boolean nearLeftPost = hitLeft && (by < GameConfig.GOAL_TOP + r + 20 || by > GameConfig.GOAL_BOTTOM - r - 20);
        boolean nearRightPost = hitRight && (by < GameConfig.GOAL_TOP + r + 20 || by > GameConfig.GOAL_BOTTOM - r - 20);
        boolean nearTopCorner = hitTop && (bx < r + 20 || bx > GameConfig.WINDOW_WIDTH - r - 20);
        boolean nearBottomCorner = hitBottom && (bx < r + 20 || bx > GameConfig.WINDOW_WIDTH - r - 20);

        boolean inCorner = nearLeftPost || nearRightPost || nearTopCorner || nearBottomCorner;
        double cornerBoost = inCorner ? GameConfig.CORNER_ESCAPE_BOOST : 1.0;

        // Top wall
        if (hitTop) {
            ball.setPosition(ball.position().withY(r));
            ball.setVelocity(ball.velocity().withY(-vy * GameConfig.BALL_WALL_RESTITUTION * cornerBoost));
        }
        // Bottom wall
        if (hitBottom) {
            ball.setPosition(ball.position().withY(GameConfig.WINDOW_HEIGHT - r));
            ball.setVelocity(ball.velocity().withY(-vy * GameConfig.BALL_WALL_RESTITUTION * cornerBoost));
        }
        // Left wall (except goal)
        if (hitLeft) {
            if (!(by > GameConfig.GOAL_TOP + r && by < GameConfig.GOAL_BOTTOM - r)) {
                ball.setPosition(ball.position().withX(r));
                ball.setVelocity(ball.velocity().withX(-vx * GameConfig.BALL_WALL_RESTITUTION * cornerBoost));
            }
        }
        // Right wall (except goal)
        if (hitRight) {
            if (!(by > GameConfig.GOAL_TOP + r && by < GameConfig.GOAL_BOTTOM - r)) {
                ball.setPosition(ball.position().withX(GameConfig.WINDOW_WIDTH - r));
                ball.setVelocity(ball.velocity().withX(-vx * GameConfig.BALL_WALL_RESTITUTION * cornerBoost));
            }
        }

        // Extra corner nudge - push ball away from corner
        if (inCorner) {
            Vector2D center = new Vector2D(GameConfig.WINDOW_WIDTH / 2.0, GameConfig.WINDOW_HEIGHT / 2.0);
            Vector2D toCenter = center.subtract(ball.position()).normalized();
            ball.setPosition(ball.position().add(toCenter.multiply(2.0)));
        }
    }
}