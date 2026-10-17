package com.example.zzzznotch.actions;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.KeyEvent;
import android.widget.Toast;

import com.example.zzzznotch.data.models.ActionType;
import com.example.zzzznotch.data.models.GestureConfig;
import com.example.zzzznotch.data.preferences.AppPreferences;
import com.example.zzzznotch.recording.AudioRecordingService;
import com.example.zzzznotch.recording.VideoRecordingService;
import com.example.zzzznotch.service.NotchAccessibilityService;
import com.example.zzzznotch.service.NotchOverlayService;
import com.example.zzzznotch.ui.BackupRestoreActivity;
import com.example.zzzznotch.ui.CameraQrActivity;
import com.example.zzzznotch.ui.NotesActivity;
import com.example.zzzznotch.ui.RecordingLibraryActivity;
import com.example.zzzznotch.utils.DistractionFreeManager;
import com.example.zzzznotch.utils.Logger;
import com.example.zzzznotch.utils.PermissionUtils;

public class ActionExecutor {

    private static boolean isFlashlightOn = false;

    public static boolean executeAction(Context context, GestureConfig config) {
        if (context == null || config == null) return false;
        ActionType type = config.getActionType();
        if (type == null || type == ActionType.NONE) return false;

        Logger.d("Executing action: " + type.name());

        switch (type) {
            case TOGGLE_MASTER:
                return executeToggleMaster(context);

            case TOGGLE_DISTRACTION_FREE:
                DistractionFreeManager.getInstance().toggleManualMode(context);
                return true;

            case OPEN_WIDGET_PANEL:
                return executeOpenWidgetPanel(context);

            case LAUNCH_APP:
                return executeLaunchApp(context, config.getTargetPackage());

            case OPEN_SETTINGS_PAGE:
                return executeOpenSettings(context, Settings.ACTION_SETTINGS);

            case OPEN_SETTINGS_WIFI:
                return executeOpenSettings(context, Settings.ACTION_WIFI_SETTINGS);

            case OPEN_SETTINGS_BLUETOOTH:
                return executeOpenSettings(context, Settings.ACTION_BLUETOOTH_SETTINGS);

            case OPEN_SETTINGS_DISPLAY:
                return executeOpenSettings(context, Settings.ACTION_DISPLAY_SETTINGS);

            case OPEN_SETTINGS_BATTERY:
                return executeOpenSettings(context, Settings.ACTION_BATTERY_SAVER_SETTINGS);

            case OPEN_SETTINGS_SOUND:
                return executeOpenSettings(context, Settings.ACTION_SOUND_SETTINGS);

            case OPEN_SETTINGS_APPS:
                return executeOpenSettings(context, Settings.ACTION_APPLICATION_SETTINGS);

            case FLASHLIGHT:
                return executeFlashlight(context);

            case MEDIA_PLAY_PAUSE:
                return sendMediaKeyEvent(context, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);

            case MEDIA_NEXT:
                return sendMediaKeyEvent(context, KeyEvent.KEYCODE_MEDIA_NEXT);

            case MEDIA_PREVIOUS:
                return sendMediaKeyEvent(context, KeyEvent.KEYCODE_MEDIA_PREVIOUS);

            case VOLUME_UP:
                return adjustVolume(context, AudioManager.ADJUST_RAISE);

            case VOLUME_DOWN:
                return adjustVolume(context, AudioManager.ADJUST_LOWER);

            case VOLUME_MUTE:
                return adjustVolume(context, AudioManager.ADJUST_TOGGLE_MUTE);

            case BRIGHTNESS_UP:
                return adjustBrightness(context, 25);

            case BRIGHTNESS_DOWN:
                return adjustBrightness(context, -25);

            case TOGGLE_AUTO_ROTATE:
                return toggleAutoRotate(context);

            case TAKE_SCREENSHOT:
                return executeAccessibilityAction(context, AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT, "Take Screenshot");

            case GLOBAL_BACK:
                return executeAccessibilityAction(context, AccessibilityService.GLOBAL_ACTION_BACK, "Back");

            case GLOBAL_HOME:
                return executeAccessibilityAction(context, AccessibilityService.GLOBAL_ACTION_HOME, "Home");

            case GLOBAL_RECENTS:
                return executeAccessibilityAction(context, AccessibilityService.GLOBAL_ACTION_RECENTS, "Recents");

            case GLOBAL_NOTIFICATIONS:
                return executeAccessibilityAction(context, AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS, "Notifications");

            case GLOBAL_QUICK_SETTINGS:
                return executeAccessibilityAction(context, AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS, "Quick Settings");

            case GLOBAL_LOCK_SCREEN:
                return executeAccessibilityAction(context, AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN, "Lock Screen");

            case SIMULATE_SWIPE_UP:
                return executeSimulatedSwipe(context, 0, "Swipe Up");

            case SIMULATE_SWIPE_DOWN:
                return executeSimulatedSwipe(context, 1, "Swipe Down");

            case SIMULATE_SWIPE_LEFT:
                return executeSimulatedSwipe(context, 2, "Swipe Left");

            case SIMULATE_SWIPE_RIGHT:
                return executeSimulatedSwipe(context, 3, "Swipe Right");

            case OPEN_NOTES:
                return launchActivity(context, NotesActivity.class);

            case OPEN_QR_SCANNER:
                return launchActivity(context, CameraQrActivity.class);

            case TOGGLE_RECORDING_VIDEO:
                if (!PermissionUtils.hasCameraPermission(context)) {
                    Toast.makeText(context, "Camera permission required for Video Recording", Toast.LENGTH_LONG).show();
                    return false;
                }
                VideoRecordingService.toggleRecording(context);
                return true;

            case TOGGLE_RECORDING_AUDIO:
                AudioRecordingService.toggleRecording(context);
                return true;

            case OPEN_RECORDING_LIBRARY:
                return launchActivity(context, RecordingLibraryActivity.class);

            case BACKUP_RESTORE_SETTINGS:
                return launchActivity(context, BackupRestoreActivity.class);

            default:
                return false;
        }
    }

    private static boolean executeToggleMaster(Context context) {
        AppPreferences prefs = AppPreferences.getInstance(context);
        boolean newState = !prefs.isMasterEnabled();
        prefs.setMasterEnabled(newState);
        Toast.makeText(context, "Notch Interactions " + (newState ? "ENABLED" : "DISABLED"), Toast.LENGTH_SHORT).show();
        NotchOverlayService.updateServiceState(context);
        return true;
    }

    private static boolean executeOpenWidgetPanel(Context context) {
        Intent intent = new Intent(context, NotchOverlayService.class);
        intent.setAction(NotchOverlayService.ACTION_TOGGLE_WIDGET_PANEL);
        context.startService(intent);
        return true;
    }

    private static boolean executeLaunchApp(Context context, String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            Toast.makeText(context, "No app selected for gesture", Toast.LENGTH_SHORT).show();
            return false;
        }
        try {
            Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(launchIntent);
                return true;
            } else {
                Toast.makeText(context, "App not found: " + packageName, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Logger.e("Failed to launch app: " + packageName, e);
        }
        return false;
    }

    private static boolean executeOpenSettings(Context context, String actionStr) {
        try {
            Intent intent = new Intent(actionStr != null ? actionStr : Settings.ACTION_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            Intent fallback = new Intent(Settings.ACTION_SETTINGS);
            fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(fallback);
            return true;
        }
    }

    private static boolean executeFlashlight(Context context) {
        try {
            CameraManager cm = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            if (cm != null) {
                String[] cameraIdList = cm.getCameraIdList();
                if (cameraIdList.length > 0) {
                    String cameraId = cameraIdList[0];
                    isFlashlightOn = !isFlashlightOn;
                    cm.setTorchMode(cameraId, isFlashlightOn);
                    Toast.makeText(context, "Flashlight " + (isFlashlightOn ? "ON" : "OFF"), Toast.LENGTH_SHORT).show();
                    return true;
                }
            }
        } catch (Exception e) {
            Logger.e("Flashlight toggle error", e);
            Toast.makeText(context, "Flashlight unavailable: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private static boolean sendMediaKeyEvent(Context context, int keyCode) {
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (am == null) return false;

        long eventTime = SystemClock.uptimeMillis();
        KeyEvent downEvent = new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, keyCode, 0);
        KeyEvent upEvent = new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP, keyCode, 0);

        am.dispatchMediaKeyEvent(downEvent);
        am.dispatchMediaKeyEvent(upEvent);
        return true;
    }

    private static boolean adjustVolume(Context context, int direction) {
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (am == null) return false;
        am.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, AudioManager.FLAG_SHOW_UI);
        return true;
    }

    private static boolean adjustBrightness(Context context, int delta) {
        if (!PermissionUtils.hasWriteSettingsPermission(context)) {
            Toast.makeText(context, "Write Settings permission required for Brightness", Toast.LENGTH_LONG).show();
            PermissionUtils.requestWriteSettingsPermission(context);
            return false;
        }
        try {
            int currentBrightness = Settings.System.getInt(
                    context.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS,
                    128
            );
            int newBrightness = Math.max(10, Math.min(255, currentBrightness + delta));
            Settings.System.putInt(
                    context.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS,
                    newBrightness
            );
            Toast.makeText(context, "Brightness: " + (int) ((newBrightness / 255f) * 100) + "%", Toast.LENGTH_SHORT).show();
            return true;
        } catch (Exception e) {
            Logger.e("Brightness adjustment failed", e);
            return false;
        }
    }

    private static boolean toggleAutoRotate(Context context) {
        if (!PermissionUtils.hasWriteSettingsPermission(context)) {
            Toast.makeText(context, "Write Settings permission required for Auto-Rotate", Toast.LENGTH_LONG).show();
            PermissionUtils.requestWriteSettingsPermission(context);
            return false;
        }
        try {
            int autoRotate = Settings.System.getInt(
                    context.getContentResolver(),
                    Settings.System.ACCELEROMETER_ROTATION,
                    0
            );
            int newState = (autoRotate == 1) ? 0 : 1;
            Settings.System.putInt(
                    context.getContentResolver(),
                    Settings.System.ACCELEROMETER_ROTATION,
                    newState
            );
            Toast.makeText(context, "Auto-Rotate " + (newState == 1 ? "ENABLED" : "DISABLED"), Toast.LENGTH_SHORT).show();
            return true;
        } catch (Exception e) {
            Logger.e("Auto-Rotate toggle failed", e);
            return false;
        }
    }

    private static boolean executeAccessibilityAction(Context context, int globalAction, String actionName) {
        NotchAccessibilityService service = NotchAccessibilityService.getInstance();
        if (service != null) {
            boolean success = service.performGlobalAction(globalAction);
            if (!success) {
                Toast.makeText(context, "Action " + actionName + " failed", Toast.LENGTH_SHORT).show();
            }
            return success;
        }

        if (PermissionUtils.isAccessibilityServiceEnabled(context)) {
            Toast.makeText(context, "Accessibility Service connecting... Try again", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            Toast.makeText(context, "Accessibility Service required for " + actionName, Toast.LENGTH_LONG).show();
            PermissionUtils.openAccessibilitySettings(context);
            return false;
        }
    }

    private static boolean executeSimulatedSwipe(Context context, int direction, String actionName) {
        NotchAccessibilityService service = NotchAccessibilityService.getInstance();
        if (service != null) {
            boolean success = service.performSimulatedSwipe(direction);
            if (!success) {
                Toast.makeText(context, "Simulated " + actionName + " failed", Toast.LENGTH_SHORT).show();
            }
            return success;
        }

        if (PermissionUtils.isAccessibilityServiceEnabled(context)) {
            Toast.makeText(context, "Accessibility Service connecting... Try again", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            Toast.makeText(context, "Accessibility Service required for simulated swipe", Toast.LENGTH_LONG).show();
            PermissionUtils.openAccessibilitySettings(context);
            return false;
        }
    }

    private static boolean launchActivity(Context context, Class<?> activityClass) {
        try {
            Intent intent = new Intent(context, activityClass);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            Logger.e("Failed to launch " + activityClass.getSimpleName(), e);
            return false;
        }
    }
}
