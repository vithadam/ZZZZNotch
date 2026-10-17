package com.example.zzzznotch.actions;

import android.content.Context;

import com.example.zzzznotch.data.models.ActionType;
import com.example.zzzznotch.utils.PermissionUtils;

import java.util.ArrayList;
import java.util.List;

public class ActionRegistry {

    public static List<ActionInfo> getAllActions(Context context) {
        List<ActionInfo> list = new ArrayList<>();

        for (ActionType type : ActionType.values()) {
            String permission = getRequiredPermissionForAction(type);
            boolean available = isActionAvailable(context, type);
            list.add(new ActionInfo(type, type.getDisplayName(), type.getDescription(), permission, available));
        }

        return list;
    }

    public static String getRequiredPermissionForAction(ActionType type) {
        if (type == null) return "None";
        switch (type) {
            case GLOBAL_BACK:
            case GLOBAL_HOME:
            case GLOBAL_RECENTS:
            case GLOBAL_NOTIFICATIONS:
            case GLOBAL_QUICK_SETTINGS:
            case GLOBAL_LOCK_SCREEN:
            case TAKE_SCREENSHOT:
            case SIMULATE_SWIPE_UP:
            case SIMULATE_SWIPE_DOWN:
            case SIMULATE_SWIPE_LEFT:
            case SIMULATE_SWIPE_RIGHT:
                return "Accessibility Service";

            case BRIGHTNESS_UP:
            case BRIGHTNESS_DOWN:
            case TOGGLE_AUTO_ROTATE:
                return "Write Settings Permission";

            case OPEN_QR_SCANNER:
                return "Camera Permission";

            default:
                return "None";
        }
    }

    public static boolean isActionAvailable(Context context, ActionType type) {
        if (type == null) return false;
        if (type == ActionType.NONE) return true;

        switch (type) {
            case GLOBAL_BACK:
            case GLOBAL_HOME:
            case GLOBAL_RECENTS:
            case GLOBAL_NOTIFICATIONS:
            case GLOBAL_QUICK_SETTINGS:
            case GLOBAL_LOCK_SCREEN:
            case TAKE_SCREENSHOT:
            case SIMULATE_SWIPE_UP:
            case SIMULATE_SWIPE_DOWN:
            case SIMULATE_SWIPE_LEFT:
            case SIMULATE_SWIPE_RIGHT:
                return PermissionUtils.isAccessibilityServiceEnabled(context);

            case BRIGHTNESS_UP:
            case BRIGHTNESS_DOWN:
            case TOGGLE_AUTO_ROTATE:
                return PermissionUtils.hasWriteSettingsPermission(context);

            case OPEN_QR_SCANNER:
                return PermissionUtils.hasCameraPermission(context);

            default:
                return true;
        }
    }
}
