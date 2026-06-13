package com.smsforwarder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Ensures the app re-registers after the phone boots or quick-boots (MIUI).
 * The SmsReceiver is a static receiver declared in the manifest, so it
 * re-activates automatically — this class just logs the boot event.
 */
public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "SmsForwarder";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) ||
                "android.intent.action.QUICKBOOT_POWERON".equals(action)) {
            Log.d(TAG, "Device booted — SmsForwarder is active.");
            // No extra work needed; static BroadcastReceivers survive reboots.
        }
    }
}
