package com.example.zzzznotch.data.database;

import androidx.room.TypeConverter;

import com.example.zzzznotch.data.models.ActionType;
import com.example.zzzznotch.data.models.RuleConfig;

public class Converters {

    @TypeConverter
    public static String fromConditionType(RuleConfig.ConditionType conditionType) {
        return conditionType == null ? null : conditionType.name();
    }

    @TypeConverter
    public static RuleConfig.ConditionType toConditionType(String name) {
        if (name == null || name.isEmpty()) return RuleConfig.ConditionType.FOREGROUND_APP;
        try {
            return RuleConfig.ConditionType.valueOf(name);
        } catch (IllegalArgumentException e) {
            return RuleConfig.ConditionType.FOREGROUND_APP;
        }
    }

    @TypeConverter
    public static String fromActionType(ActionType actionType) {
        return actionType == null ? null : actionType.name();
    }

    @TypeConverter
    public static ActionType toActionType(String name) {
        if (name == null || name.isEmpty()) return ActionType.NONE;
        try {
            return ActionType.valueOf(name);
        } catch (IllegalArgumentException e) {
            return ActionType.NONE;
        }
    }
}
