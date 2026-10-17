package com.example.zzzznotch.rules;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import com.example.zzzznotch.data.models.ActionType;
import com.example.zzzznotch.data.models.GestureConfig;
import com.example.zzzznotch.data.models.RuleConfig;
import com.example.zzzznotch.data.repository.AppRepository;
import com.example.zzzznotch.utils.Logger;

import java.util.List;

public class RuleEngine {

    private static String currentForegroundPackage = "";
    private static boolean isMediaPlaying = false;

    public static void setForegroundPackage(String pkg) {
        if (pkg != null) {
            currentForegroundPackage = pkg;
        }
    }

    public static void setMediaPlaying(boolean playing) {
        isMediaPlaying = playing;
    }

    public static GestureConfig evaluateRules(Context context, GestureConfig defaultConfig) {
        if (context == null || defaultConfig == null) return defaultConfig;

        AppRepository repository = AppRepository.getInstance(context);
        List<RuleConfig> rules = repository.getEnabledRules();

        if (rules == null || rules.isEmpty()) {
            return defaultConfig;
        }

        for (RuleConfig rule : rules) {
            if (rule == null || !rule.isEnabled()) continue;

            if (isRuleConditionMet(context, rule)) {
                Logger.d("Rule matched: " + rule.getName() + " -> Overriding action with " + rule.getOverrideAction().name());
                GestureConfig overridden = new GestureConfig(
                        defaultConfig.getGestureType(),
                        rule.getOverrideAction(),
                        rule.getConditionValue(),
                        rule.getExtraData(),
                        true
                );
                return overridden;
            }
        }

        return defaultConfig;
    }

    private static boolean isRuleConditionMet(Context context, RuleConfig rule) {
        RuleConfig.ConditionType type = rule.getConditionType();
        if (type == null) return false;

        switch (type) {
            case FOREGROUND_APP:
                String targetPkg = rule.getConditionValue();
                return targetPkg != null && !targetPkg.isEmpty() && targetPkg.equalsIgnoreCase(currentForegroundPackage);

            case CHARGING_STATE:
                return isDeviceCharging(context);

            case MEDIA_PLAYING:
                return isMediaPlaying;

            case BATTERY_LOW:
                return getBatteryLevel(context) <= 20;

            default:
                return false;
        }
    }

    private static boolean isDeviceCharging(Context context) {
        try {
            IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, filter);
            if (batteryStatus != null) {
                int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                return status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private static int getBatteryLevel(Context context) {
        try {
            IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, filter);
            if (batteryStatus != null) {
                int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                if (level >= 0 && scale > 0) {
                    return (int) ((level / (float) scale) * 100);
                }
            }
        } catch (Exception ignored) {
        }
        return 100;
    }
}
