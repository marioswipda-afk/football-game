package football.core;

import football.config.GameConfig;
import football.model.Ball;
import football.model.Player;

/**
 * Manages goal replay system - stores snapshots and plays them back.
 */
public final class ReplaySystem {

    private final double[][] frames = new double[GameConfig.MAX_SNAPSHOTS][6];
    private int frameCount = 0;
    private int currentIndex = 0;
    private boolean recording = false;

    public void startRecording() {
        recording = true;
        frameCount = 0;
    }

    public void stopRecording() {
        recording = false;
    }

    public void recordSnapshot(Player p1, Player p2, football.model.Ball ball) {
        if (!recording || frameCount >= GameConfig.MAX_SNAPSHOTS) return;

        frames[frameCount][0] = p1.position().x();
        frames[frameCount][1] = p1.position().y();
        frames[frameCount][2] = p2.position().x();
        frames[frameCount][3] = p2.position().y();
        frames[frameCount][4] = ball.position().x();
        frames[frameCount][5] = ball.position().y();
        frameCount++;
    }

    public void startReplay() {
        currentIndex = 0;
    }

    public void advanceReplay() {
        currentIndex++;
    }

    public boolean isReplayFinished(int extraFrames) {
        return currentIndex >= frameCount + extraFrames;
    }

    public double[][] frames() {
        // Return only recorded frames
        double[][] result = new double[frameCount][6];
        System.arraycopy(frames, 0, result, 0, frameCount);
        return result;
    }

    public int currentIndex() {
        return currentIndex;
    }

    public int frameCount() {
        return frameCount;
    }

    public void reset() {
        frameCount = 0;
        currentIndex = 0;
        recording = false;
    }

    public boolean isRecording() {
        return recording;
    }
}