package com.firebirdberlin.nightdream.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.firebirdberlin.nightdream.NightDreamActivity;
import com.firebirdberlin.nightdream.Settings;
import com.firebirdberlin.nightdream.Utility;
import com.firebirdberlin.nightdream.repositories.BatteryStats;

public class ScreenReceiver extends BroadcastReceiver {
    private static final String TAG = "ScreenReceiver";

    public static ScreenReceiver register(Context ctx) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        ScreenReceiver receiver = new ScreenReceiver();
        ctx.registerReceiver(receiver, filter);
        return receiver;
    }

    public static void unregister(Context ctx, BroadcastReceiver receiver) {
        if (receiver != null) {
            ctx.unregisterReceiver(receiver);
        }
    }

    public static void conditionallyActivateAlwaysOn(Context context, boolean turnScreenOn) {
        Settings settings = new Settings(context);
        if ( shallActivateStandby(context, settings) ) {
            if (turnScreenOn) {
                Utility.turnScreenOn(context);
            }
            NightDreamActivity.start(context, "start standby mode");
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            Log.i(TAG, "ACTION_SCREEN_OFF");
            conditionallyActivateAlwaysOn(context, false);
        }
    }

    public static boolean shallActivateStandby(Context context, Settings settings) {
        if (Utility.isConfiguredAsDaydream(context)) return false;

        BatteryStats battery = new BatteryStats(context);
        if (battery.reference.isCharging && settings.handle_power && settings.standbyEnabledWhileConnected) {
            return PowerConnectionReceiver.shallAutostart(context, settings);
        }

        if ( !battery.reference.isCharging && settings.standbyEnabledWhileDisconnected ) {
            return true;
        }

        return false;
    }
}
