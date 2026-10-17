package com.example.zzzznotch.data.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.zzzznotch.data.models.ActionType;
import com.example.zzzznotch.data.models.GestureConfig;
import com.example.zzzznotch.data.models.GestureType;
import com.example.zzzznotch.data.models.NotchAppearance;
import com.example.zzzznotch.data.models.WidgetConfig;
import com.google.gson.Gson;

public class AppPreferences {

    private static final String PREF_NAME = "zzzznotch_prefs";

    private static final String KEY_MASTER_ENABLED = "master_enabled";
    private static final String KEY_RUN_ON_STARTUP = "run_on_startup";
    private static final String KEY_RECOGNITION_WINDOW_MS = "recognition_window_ms";
    private static final String KEY_MOVEMENT_TOLERANCE_DP = "movement_tolerance_dp";

    private static final String KEY_GESTURE_PREFIX = "gesture_cfg_";
    private static final String KEY_NOTCH_APPEARANCE = "notch_appearance";
    private static final String KEY_WIDGET_CONFIG = "widget_config";

    // Recording settings keys
    private static final String KEY_RECORDING_RESOLUTION = "rec_resolution";
    private static final String KEY_RECORDING_FPS = "rec_fps";
    private static final String KEY_RECORDING_AUDIO_QUALITY = "rec_audio_quality";

    // Advanced Touch settings keys
    private static final String KEY_SHOW_TOUCH_AREA = "show_touch_area";
    private static final String KEY_HAPTIC_FEEDBACK = "haptic_feedback";
    private static final String KEY_DISABLE_IN_LANDSCAPE = "disable_in_landscape";
    private static final String KEY_DISABLE_DURING_CALL = "disable_during_call";
    private static final String KEY_DISABLE_ON_LOCK_SCREEN = "disable_on_lock_screen";

    private static volatile AppPreferences INSTANCE;
    private final SharedPreferences prefs;
    private final Gson gson;

    private AppPreferences(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public static AppPreferences getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppPreferences.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AppPreferences(context);
                }
            }
        }
        return INSTANCE;
    }

    public boolean isMasterEnabled() {
        return prefs.getBoolean(KEY_MASTER_ENABLED, true);
    }

    public void setMasterEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_MASTER_ENABLED, enabled).apply();
    }

    public boolean isRunOnStartupEnabled() {
        return prefs.getBoolean(KEY_RUN_ON_STARTUP, true);
    }

    public void setRunOnStartupEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_RUN_ON_STARTUP, enabled).apply();
    }

    public int getRecognitionWindowMs() {
        return prefs.getInt(KEY_RECOGNITION_WINDOW_MS, 300); // 300ms default multi-tap window
    }

    public void setRecognitionWindowMs(int ms) {
        prefs.edit().putInt(KEY_RECOGNITION_WINDOW_MS, Math.max(100, Math.min(1000, ms))).apply();
    }

    public int getMovementToleranceDp() {
        return prefs.getInt(KEY_MOVEMENT_TOLERANCE_DP, 24); // 24dp tolerance default
    }

    public void setMovementToleranceDp(int dp) {
        prefs.edit().putInt(KEY_MOVEMENT_TOLERANCE_DP, Math.max(5, Math.min(100, dp))).apply();
    }

    public boolean isShowTouchAreaEnabled() {
        return prefs.getBoolean(KEY_SHOW_TOUCH_AREA, false);
    }

    public void setShowTouchAreaEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_SHOW_TOUCH_AREA, enabled).apply();
    }

    public boolean isHapticFeedbackEnabled() {
        return prefs.getBoolean(KEY_HAPTIC_FEEDBACK, true);
    }

    public void setHapticFeedbackEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_HAPTIC_FEEDBACK, enabled).apply();
    }

    public boolean isDisableInLandscapeEnabled() {
        return prefs.getBoolean(KEY_DISABLE_IN_LANDSCAPE, true);
    }

    public void setDisableInLandscapeEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_DISABLE_IN_LANDSCAPE, enabled).apply();
    }

    public boolean isDisableDuringCallEnabled() {
        return prefs.getBoolean(KEY_DISABLE_DURING_CALL, true);
    }

    public void setDisableDuringCallEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_DISABLE_DURING_CALL, enabled).apply();
    }

    public boolean isDisableOnLockScreenEnabled() {
        return prefs.getBoolean(KEY_DISABLE_ON_LOCK_SCREEN, false);
    }

    public void setDisableOnLockScreenEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_DISABLE_ON_LOCK_SCREEN, enabled).apply();
    }

    public GestureConfig getGestureConfig(GestureType gestureType) {
        String json = prefs.getString(KEY_GESTURE_PREFIX + gestureType.name(), null);
        if (json != null) {
            try {
                GestureConfig config = gson.fromJson(json, GestureConfig.class);
                if (config != null) return config;
            } catch (Exception ignored) {
            }
        }
        // Default mappings if none configured yet
        GestureConfig defaultConfig = new GestureConfig();
        defaultConfig.setGestureType(gestureType);

        switch (gestureType) {
            case SINGLE_TAP:
                defaultConfig.setActionType(ActionType.GLOBAL_BACK);
                break;
            case DOUBLE_TAP:
                defaultConfig.setActionType(ActionType.GLOBAL_RECENTS);
                break;
            case TRIPLE_TAP:
                defaultConfig.setActionType(ActionType.OPEN_WIDGET_PANEL);
                break;
            case FOUR_TAPS:
                defaultConfig.setActionType(ActionType.FLASHLIGHT);
                break;
            case FIVE_TAPS:
                defaultConfig.setActionType(ActionType.TOGGLE_MASTER);
                break;
            case LONG_PRESS:
                defaultConfig.setActionType(ActionType.GLOBAL_HOME);
                break;
            default:
                defaultConfig.setActionType(ActionType.NONE);
                break;
        }
        return defaultConfig;
    }

    public void saveGestureConfig(GestureConfig config) {
        if (config == null || config.getGestureType() == null) return;
        String json = gson.toJson(config);
        prefs.edit().putString(KEY_GESTURE_PREFIX + config.getGestureType().name(), json).apply();
    }

    public NotchAppearance getNotchAppearance() {
        String json = prefs.getString(KEY_NOTCH_APPEARANCE, null);
        if (json != null) {
            try {
                NotchAppearance appearance = gson.fromJson(json, NotchAppearance.class);
                if (appearance != null) return appearance;
            } catch (Exception ignored) {
            }
        }
        return new NotchAppearance();
    }

    public void saveNotchAppearance(NotchAppearance appearance) {
        if (appearance == null) return;
        String json = gson.toJson(appearance);
        prefs.edit().putString(KEY_NOTCH_APPEARANCE, json).apply();
    }

    public WidgetConfig getWidgetConfig() {
        String json = prefs.getString(KEY_WIDGET_CONFIG, null);
        if (json != null) {
            try {
                WidgetConfig config = gson.fromJson(json, WidgetConfig.class);
                if (config != null) return config;
            } catch (Exception ignored) {
            }
        }
        return new WidgetConfig();
    }

    public void saveWidgetConfig(WidgetConfig config) {
        if (config == null) return;
        String json = gson.toJson(config);
        prefs.edit().putString(KEY_WIDGET_CONFIG, json).apply();
    }

    public String getRecordingResolution() {
        return prefs.getString(KEY_RECORDING_RESOLUTION, "1080p");
    }

    public void setRecordingResolution(String res) {
        prefs.edit().putString(KEY_RECORDING_RESOLUTION, res).apply();
    }

    public int getRecordingFps() {
        return prefs.getInt(KEY_RECORDING_FPS, 30);
    }

    public void setRecordingFps(int fps) {
        prefs.edit().putInt(KEY_RECORDING_FPS, fps).apply();
    }

    public String getRecordingAudioQuality() {
        return prefs.getString(KEY_RECORDING_AUDIO_QUALITY, "High (128 kbps)");
    }

    public void setRecordingAudioQuality(String quality) {
        prefs.edit().putString(KEY_RECORDING_AUDIO_QUALITY, quality).apply();
    }
}
