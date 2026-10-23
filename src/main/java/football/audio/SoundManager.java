package football.audio;

import football.config.GameConfig;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

/**
 * Manages game sound effects using generated tones.
 * Runs sounds on separate threads to avoid blocking the game loop.
 */
public final class SoundManager {

    private SoundManager() {}

    public static void playKick(double charge) {
        int frequency = (int) (140 + charge * 160);
        playToneAsync(frequency, 120, GameConfig.SOUND_VOLUME, 0);
    }

    public static void playGoal() {
        playToneAsync(523, 150, GameConfig.SOUND_VOLUME, 0);
        playToneAsync(659, 150, GameConfig.SOUND_VOLUME, 120);
        playToneAsync(784, 300, GameConfig.SOUND_VOLUME, 240);
    }

    public static void playWin() {
        int[] notes = {523, 659, 784, 1046, 784, 1046};
        for (int i = 0; i < notes.length; i++) {
            playToneAsync(notes[i], 200, GameConfig.SOUND_VOLUME, i * 160);
        }
    }

    public static void playEvent() {
        playToneAsync(880, 100, GameConfig.EVENT_SOUND_VOLUME, 0);
        playToneAsync(880, 100, GameConfig.EVENT_SOUND_VOLUME, 150);
    }

    private static void playToneAsync(int frequency, int durationMs, float volume, int delayMs) {
        new Thread(() -> {
            try {
                Thread.sleep(delayMs);
                playTone(frequency, durationMs, volume);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private static void playTone(int frequency, int durationMs, float volume) {
        try {
            int rate = GameConfig.SAMPLE_RATE;
            byte[] buffer = new byte[rate * durationMs / 1000];
            for (int i = 0; i < buffer.length; i++) {
                double t = (double) i / rate;
                double envelope = 1.0 - t / (durationMs / 1000.0);
                buffer[i] = (byte) (Math.sin(2 * Math.PI * frequency * t) * 127 * volume * envelope);
            }
            AudioFormat format = new AudioFormat(rate, 8, 1, true, false);
            Clip clip = AudioSystem.getClip();
            clip.open(format, buffer, 0, buffer.length);
            clip.start();
            // Don't wait for clip to finish - let it play async
        } catch (Exception ignored) {
            // Sound is optional, fail silently
        }
    }
}