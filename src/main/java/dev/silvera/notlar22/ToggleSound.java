package dev.silvera.notlar22;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ToggleSound {

    private static final float SAMPLE_RATE = 44100f;
    private static final AudioFormat FORMAT = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "Notlar22-Sound");
        t.setDaemon(true);
        return t;
    });

    private static byte[] onSound;
    private static byte[] offSound;

    private ToggleSound() {
    }

    public static void play(boolean on) {
        EXECUTOR.execute(() -> {
            try {
                byte[] data;
                if (on) {
                    if (onSound == null) {
                        onSound = render(new double[]{660.0, 990.0, 1320.0}, 130);
                    }
                    data = onSound;
                } else {
                    if (offSound == null) {
                        offSound = render(new double[]{880.0, 587.0, 392.0}, 130);
                    }
                    data = offSound;
                }
                Clip clip = AudioSystem.getClip();
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });
                clip.open(FORMAT, data, 0, data.length);
                clip.start();
            } catch (Throwable t) {
                Notlar22.LOGGER.debug("Ses çalınamadı: {}", t.toString());
            }
        });
    }

    private static byte[] render(double[] freqs, int noteMs) {
        int perNote = (int) (SAMPLE_RATE * noteMs / 1000.0);
        byte[] out = new byte[perNote * freqs.length * 2];
        int idx = 0;
        for (double f : freqs) {
            for (int i = 0; i < perNote; i++) {
                double t = i / (double) SAMPLE_RATE;
                double attack = Math.min(1.0, i / (SAMPLE_RATE * 0.004));
                double decay = Math.exp(-t * 24.0);
                double wave = (Math.sin(2.0 * Math.PI * f * t)
                        + 0.35 * Math.sin(4.0 * Math.PI * f * t)) / 1.35;
                short v = (short) (wave * attack * decay * 0.45 * Short.MAX_VALUE);
                out[idx++] = (byte) (v & 0xFF);
                out[idx++] = (byte) ((v >> 8) & 0xFF);
            }
        }
        return out;
    }
}
