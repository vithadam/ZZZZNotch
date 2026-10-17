package com.example.zzzznotch.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.zzzznotch.R;
import com.example.zzzznotch.utils.PermissionUtils;

public class PermissionsActivity extends AppCompatActivity {

    private static final int PERMISSION_REQ = 6001;

    private TextView tvStatusOverlay;
    private TextView tvStatusAccessibility;
    private TextView tvStatusNotification;
    private TextView tvStatusWriteSettings;
    private TextView tvStatusCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions);

        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshStatus();
    }

    private void initViews() {
        tvStatusOverlay = findViewById(R.id.tv_status_overlay);
        tvStatusAccessibility = findViewById(R.id.tv_status_accessibility);
        tvStatusNotification = findViewById(R.id.tv_status_notification);
        tvStatusWriteSettings = findViewById(R.id.tv_status_write_settings);
        tvStatusCamera = findViewById(R.id.tv_status_camera);

        Button btnOverlay = findViewById(R.id.btn_grant_overlay);
        Button btnAccessibility = findViewById(R.id.btn_grant_accessibility);
        Button btnAppInfo = findViewById(R.id.btn_open_app_info);
        Button btnNotification = findViewById(R.id.btn_grant_notification);
        Button btnWriteSettings = findViewById(R.id.btn_grant_write_settings);
        Button btnCamera = findViewById(R.id.btn_grant_camera);

        btnOverlay.setOnClickListener(v -> PermissionUtils.requestOverlayPermission(this));
        btnAccessibility.setOnClickListener(v -> PermissionUtils.openAccessibilitySettings(this));
        btnAppInfo.setOnClickListener(v -> PermissionUtils.openAppInfoSettings(this));
        btnNotification.setOnClickListener(v -> PermissionUtils.openNotificationListenerSettings(this));
        btnWriteSettings.setOnClickListener(v -> PermissionUtils.requestWriteSettingsPermission(this));
        btnCamera.setOnClickListener(v -> ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, PERMISSION_REQ));
    }

    private void refreshStatus() {
        updateStatusText(tvStatusOverlay, PermissionUtils.hasOverlayPermission(this));
        updateStatusText(tvStatusAccessibility, PermissionUtils.isAccessibilityServiceEnabled(this));
        updateStatusText(tvStatusNotification, PermissionUtils.isNotificationListenerEnabled(this));
        updateStatusText(tvStatusWriteSettings, PermissionUtils.hasWriteSettingsPermission(this));
        updateStatusText(tvStatusCamera, PermissionUtils.hasCameraPermission(this) && PermissionUtils.hasAudioPermission(this));
    }

    private void updateStatusText(TextView tv, boolean isGranted) {
        if (isGranted) {
            tv.setText("GRANTED");
            tv.setTextColor(0xFF34C759);
        } else {
            tv.setText("DISABLED");
            tv.setTextColor(0xFFFF3B30);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQ) {
            refreshStatus();
        }
    }
}
