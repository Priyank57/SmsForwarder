package com.smsforwarder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Listens for every incoming SMS and forwards it to the configured target number
 * via a new outgoing SMS (no internet required — perfect for JioBharat feature phones).
 *
 * Forwarded message format:
 *   [FWD] From: <sender>
 *   <original message body>
 *
 * Long messages are automatically split into multiple SMS parts by SmsManager.
 */
public class SmsReceiver extends BroadcastReceiver {

    private static final String TAG = "SmsForwarder";
    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!SMS_RECEIVED.equals(intent.getAction())) return;

        PrefsHelper prefs = new PrefsHelper(context);

        // Check if forwarding is turned on
        if (!prefs.isForwardingEnabled()) {
            Log.d(TAG, "Forwarding is disabled — ignoring incoming SMS.");
            return;
        }

        String targetNumber = prefs.getTargetNumber();
        if (targetNumber == null || targetNumber.isEmpty()) {
            Log.w(TAG, "No target number configured — cannot forward.");
            return;
        }

        // Parse all PDUs (protocol data units) from the intent bundle
        Bundle bundle = intent.getExtras();
        if (bundle == null) return;

        Object[] pdus = (Object[]) bundle.get("pdus");
        if (pdus == null || pdus.length == 0) return;

        String format = bundle.getString("format"); // "3gpp" or "3gpp2"

        StringBuilder fullBody = new StringBuilder();
        String sender = null;

        for (Object pdu : pdus) {
            SmsMessage smsMessage;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                smsMessage = SmsMessage.createFromPdu((byte[]) pdu, format);
            } else {
                smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
            }
            if (smsMessage == null) continue;

            if (sender == null) {
                sender = smsMessage.getDisplayOriginatingAddress();
            }
            fullBody.append(smsMessage.getMessageBody());
        }

        if (sender == null) sender = "Unknown";

        String prefix = prefs.getForwardPrefix();
        // Build the forwarded message; keep it readable on a small feature phone screen
        String forwardedText = prefix + " From: " + sender + "\n" + fullBody.toString();

        Log.d(TAG, "Forwarding SMS from " + sender + " to " + targetNumber);

        sendSms(context, targetNumber, forwardedText);
    }

    /**
     * Sends an SMS to the target. Automatically splits messages longer than 160 characters
     * into multipart SMS so the full content reaches the feature phone.
     */
    private void sendSms(Context context, String targetNumber, String message) {
        try {
            SmsManager smsManager;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                smsManager = context.getSystemService(SmsManager.class);
            } else {
                smsManager = SmsManager.getDefault();
            }

            if (message.length() > 160) {
                // Split into multiple SMS parts automatically
                ArrayList<String> parts = smsManager.divideMessage(message);
                smsManager.sendMultipartTextMessage(targetNumber, null, parts, null, null);
                Log.d(TAG, "Sent multipart SMS (" + parts.size() + " parts) to " + targetNumber);
            } else {
                smsManager.sendTextMessage(targetNumber, null, message, null, null);
                Log.d(TAG, "Sent SMS to " + targetNumber);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to forward SMS: " + e.getMessage(), e);
            Toast.makeText(context, "SMS forward failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
