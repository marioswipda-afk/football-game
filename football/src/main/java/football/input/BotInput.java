package football.input;

import football.config.GameConfig;
import football.model.Ball;
import football.model.Player;
import football.model.Vector2D;

/**
 * AI bot input for player 2.
 * Single balanced difficulty.
 */
public final class BotInput {

    public BotInput() {}

    public void update(Player bot, Ball ball) {
        // Balanced bot speed (slightly below max player speed)
        double maxSpeed = GameConfig.PLAYER_MAX_SPEED * 0.9;

        Vector2D target = calculateTarget(bot, ball);
        Vector2D delta = target.subtract(bot.position());

        double mx = Math.abs(delta.x()) > 10 ? Math.signum(delta.x()) : 0;
        double my = Math.abs(delta.y()) > 10 ? Math.signum(delta.y()) : 0;

        bot.setInputDirection(new Vector2D(mx, my));

        // Cap bot speed
        Vector2D vel = bot.velocity();
        double speed = vel.length();
        if (speed > maxSpeed) {
            bot.setVelocity(vel.multiply(maxSpeed / speed));
        }

        // Kick logic - balanced
        double ballDistance = Vector2D.distance(bot.position(), ball.position());
        double kickRange = bot.radius() + ball.radius() + GameConfig.KICK_DETECTION_RADIUS;

        if (ballDistance < kickRange) {
            if (!bot.charging()) {
                bot.setCharging(true);
            }
            // Release at ~60% charge for balanced play
            if (bot.charge() >= 0.6) {
                // Kick will be triggered by input system
            }
        } else if (bot.charging()) {
            bot.resetCharge();
        }
    }

    private Vector2D calculateTarget(Player bot, Ball ball) {
        boolean chaseBall = ball.position().x() > GameConfig.WINDOW_WIDTH * 0.35 || Math.abs(ball.velocity().x()) > 3;

        if (chaseBall) {
            // Approach ball from behind (opposite of our goal) to shoot towards goal
            Vector2D ourGoal = new Vector2D(GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT / 2.0);
            Vector2D toGoal = ball.position().subtract(ourGoal);
            double distance = toGoal.length();
            if (distance < 1) distance = 1;
            Vector2D behindBall = toGoal.normalized().multiply(26);
            return ball.position().add(behindBall);
        } else {
            // Defensive position
            double tx = Math.min(ball.position().x() + 130, GameConfig.WINDOW_WIDTH - 80);
            double ty = GameConfig.WINDOW_HEIGHT / 2.0 + (ball.position().y() - GameConfig.WINDOW_HEIGHT / 2.0) * 0.5;
            return new Vector2D(tx, ty);
        }
    }
}