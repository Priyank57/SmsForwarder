package com.smsforwarder;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefsHelper {

    private static final String PREFS_NAME = "SmsForwarderPrefs";
    private static final String KEY_TARGET_NUMBER = "target_number";
    private static final String KEY_FORWARDING_ENABLED = "forwarding_enabled";
    private static final String KEY_FORWARD_PREFIX = "forward_prefix";

    private final SharedPreferences prefs;

    public PrefsHelper(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public String getTargetNumber() {
        return prefs.getString(KEY_TARGET_NUMBER, "");
    }

    public void setTargetNumber(String number) {
        prefs.edit().putString(KEY_TARGET_NUMBER, number.trim()).apply();
    }

    public boolean isForwardingEnabled() {
        return prefs.getBoolean(KEY_FORWARDING_ENABLED, false);
    }

    public void setForwardingEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_FORWARDING_ENABLED, enabled).apply();
    }

    /** Optional prefix prepended to every forwarded message, e.g. "[FWD]" */
    public String getForwardPrefix() {
        return prefs.getString(KEY_FORWARD_PREFIX, "[FWD]");
    }

    public void setForwardPrefix(String prefix) {
        prefs.edit().putString(KEY_FORWARD_PREFIX, prefix).apply();
    }
}
