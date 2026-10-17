package com.example.zzzznotch.utils;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.core.content.ContextCompat;

import com.example.zzzznotch.service.NotchAccessibilityService;

public class PermissionUtils {

    public static boolean hasOverlayPermission(Context context) {
        if (context == null) return false;
        return Settings.canDrawOverlays(context);
    }

    public static void requestOverlayPermission(Context context) {
        if (context == null) return;
        Intent intent = new Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + context.getPackageName())
        );
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static boolean isAccessibilityServiceEnabled(Context context) {
        if (context == null) return false;

        // If service instance is already active and bound, it is definitely enabled
        if (NotchAccessibilityService.getInstance() != null) {
            return true;
        }

        int accessibilityEnabled = 0;
        final String service1 = context.getPackageName() + "/" + NotchAccessibilityService.class.getCanonicalName();
        final String service2 = context.getPackageName() + "/." + NotchAccessibilityService.class.getSimpleName();

        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    context.getApplicationContext().getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED
            );
        } catch (Settings.SettingNotFoundException ignored) {
        }

        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(
                    context.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            );
            if (settingValue != null) {
                String[] services = settingValue.split(":");
                for (String s : services) {
                    if (s.equalsIgnoreCase(service1) || s.equalsIgnoreCase(service2)
                            || (s.contains(context.getPackageName()) && s.contains("NotchAccessibilityService"))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static void openAccessibilitySettings(Context context) {
        if (context == null) return;
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void openAppInfoSettings(Context context) {
        if (context == null) return;
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static boolean isNotificationListenerEnabled(Context context) {
        if (context == null) return false;
        String packageName = context.getPackageName();
        String flat = Settings.Secure.getString(context.getContentResolver(), "enabled_notification_listeners");
        if (!TextUtils.isEmpty(flat)) {
            String[] names = flat.split(":");
            for (String name : names) {
                ComponentName cn = ComponentName.unflattenFromString(name);
                if (cn != null && TextUtils.equals(packageName, cn.getPackageName())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void openNotificationListenerSettings(Context context) {
        if (context == null) return;
        Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static boolean hasWriteSettingsPermission(Context context) {
        if (context == null) return false;
        return Settings.System.canWrite(context);
    }

    public static void requestWriteSettingsPermission(Context context) {
        if (context == null) return;
        Intent intent = new Intent(
                Settings.ACTION_MANAGE_WRITE_SETTINGS,
                Uri.parse("package:" + context.getPackageName())
        );
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static boolean hasCameraPermission(Context context) {
        if (context == null) return false;
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasAudioPermission(Context context) {
        if (context == null) return false;
        return ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasPostNotificationsPermission(Context context) {
        if (context == null) return true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }
}
