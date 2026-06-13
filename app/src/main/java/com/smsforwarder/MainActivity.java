package com.smsforwarder;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Main UI screen for SMS Forwarder.
 *
 * Features:
 *  - Enter & save the JioBharat target phone number
 *  - Toggle forwarding on / off
 *  - Request all required SMS permissions at first launch
 *  - Guide the user to disable MIUI battery optimisation (critical on Redmi)
 */
public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;

    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.SEND_SMS
    };

    private PrefsHelper prefs;
    private EditText etTargetNumber;
    private Switch swForwarding;
    private TextView tvStatus;
    private Button btnSave;
    private Button btnBatteryOpt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = new PrefsHelper(this);

        etTargetNumber = findViewById(R.id.et_target_number);
        swForwarding   = findViewById(R.id.sw_forwarding);
        tvStatus       = findViewById(R.id.tv_status);
        btnSave        = findViewById(R.id.btn_save);
        btnBatteryOpt  = findViewById(R.id.btn_battery_opt);

        // Load saved values
        etTargetNumber.setText(prefs.getTargetNumber());
        swForwarding.setChecked(prefs.isForwardingEnabled());
        updateStatusText();

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
            }
        });

        swForwarding.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked && !allPermissionsGranted()) {
                    // Turn the switch back off until permissions are granted
                    swForwarding.setChecked(false);
                    requestPermissions();
                    return;
                }
                if (isChecked && prefs.getTargetNumber().isEmpty()) {
                    swForwarding.setChecked(false);
                    Toast.makeText(MainActivity.this,
                            "Please enter and save a target number first.", Toast.LENGTH_SHORT).show();
                    return;
                }
                prefs.setForwardingEnabled(isChecked);
                updateStatusText();
            }
        });

        // Button to disable battery optimisation (required on MIUI / Redmi)
        btnBatteryOpt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBatteryOptimisationSettings();
            }
        });

        // Request permissions on first launch
        if (!allPermissionsGranted()) {
            requestPermissions();
        }

        // Warn if battery optimisation is still on
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (pm != null && !pm.isIgnoringBatteryOptimizations(getPackageName())) {
                showBatteryOptWarning();
            }
        }
    }

    private void saveSettings() {
        String number = etTargetNumber.getText().toString().trim();
        if (number.isEmpty()) {
            Toast.makeText(this, "Please enter the JioBharat phone number.", Toast.LENGTH_SHORT).show();
            return;
        }
        // Basic validation: must start with + or digit, length 7–15
        if (!number.matches("[+]?[0-9]{7,15}")) {
            Toast.makeText(this, "Enter a valid phone number (e.g. +919876543210).", Toast.LENGTH_SHORT).show();
            return;
        }
        prefs.setTargetNumber(number);
        Toast.makeText(this, "Target number saved: " + number, Toast.LENGTH_SHORT).show();
        updateStatusText();
    }

    private void updateStatusText() {
        String target = prefs.getTargetNumber();
        boolean enabled = prefs.isForwardingEnabled();

        if (enabled && !target.isEmpty()) {
            tvStatus.setText("✅ Forwarding ON → " + target);
            tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
        } else if (!enabled) {
            tvStatus.setText("⏸ Forwarding is OFF");
            tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
        } else {
            tvStatus.setText("⚠️ No target number set");
            tvStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_dark));
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Permission handling
    // ──────────────────────────────────────────────────────────────────────────

    private boolean allPermissionsGranted() {
        for (String perm : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                Toast.makeText(this, "All permissions granted ✓", Toast.LENGTH_SHORT).show();
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("Permissions Required")
                        .setMessage("SMS Forwarder needs Read SMS, Receive SMS, and Send SMS permissions to work. " +
                                "Please grant them in Settings → Apps → SMS Forwarder → Permissions.")
                        .setPositiveButton("Open Settings", (d, w) -> openAppSettings())
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        }
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", getPackageName(), null));
        startActivity(intent);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Battery optimisation (MIUI kills background apps aggressively)
    // ──────────────────────────────────────────────────────────────────────────

    private void showBatteryOptWarning() {
        new AlertDialog.Builder(this)
                .setTitle("⚠️ Battery Optimisation")
                .setMessage("MIUI (Redmi) may kill this app in the background, causing forwarding to stop.\n\n" +
                        "Tap 'Fix Now' → select 'No restrictions' to keep forwarding alive.")
                .setPositiveButton("Fix Now", (d, w) -> openBatteryOptimisationSettings())
                .setNegativeButton("Later", null)
                .show();
    }

    private void openBatteryOptimisationSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                // Try direct "ignore optimisations" request first
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                        Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            } catch (Exception e) {
                // Fallback: open general battery optimisation list
                try {
                    Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                    startActivity(intent);
                } catch (Exception ex) {
                    Toast.makeText(this,
                            "Go to Settings → Battery → App battery saver → SMS Forwarder → No restrictions",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
