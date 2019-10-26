package com.firebirdberlin.nightdream.repositories;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

public class VibrationHandler {
    static String TAG = "VibrationHandler";

    Context context = null;
    Vibrator vibrator = null;

    public static boolean hasVibrator(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        return vibrator.hasVibrator();
    }

    public VibrationHandler(Context context) {
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    public void startVibration() {
        Log.i(TAG, "startvibration");
        if (vibrator == null || !vibrator.hasVibrator()) {
            return;
        }
        // Start without a delay
        // Vibrate for 1000 milliseconds
        // Sleep for 100 milliseconds
        long[] pattern = {0, 1000, 100, 1000, 100};
        int[] amplitudes = {0, 255, 0, 128, 0};

        // The '0' here means to repeat indefinitely
        // '0' is actually the index at which the pattern keeps repeating from (the start)
        // To repeat the pattern from any other point, you could increase the index, e.g. '1'
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            vibrator.vibrate(pattern, 0);
        } else {
            VibrationEffect effect = VibrationEffect.createWaveform(pattern, amplitudes, 0);
            vibrator.vibrate(effect);
        }
    }

    public void stopVibration() {
        vibrator.cancel();
    }
}
