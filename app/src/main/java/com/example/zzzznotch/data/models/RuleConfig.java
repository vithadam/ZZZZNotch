package com.example.zzzznotch.data.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "rules")
public class RuleConfig {

    public enum ConditionType {
        FOREGROUND_APP("Foreground Application"),
        CHARGING_STATE("Device Charging"),
        MEDIA_PLAYING("Media Playing"),
        BATTERY_LOW("Battery Low (<20%)");

        private final String displayName;

        ConditionType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    @PrimaryKey(autoGenerate = true)
    private long id;
    private String name;
    private ConditionType conditionType;
    private String conditionValue;
    private ActionType overrideAction;
    private String extraData;
    private boolean enabled;

    public RuleConfig() {
        this.name = "New Rule";
        this.conditionType = ConditionType.FOREGROUND_APP;
        this.conditionValue = "";
        this.overrideAction = ActionType.NONE;
        this.extraData = "";
        this.enabled = true;
    }

    public RuleConfig(String name, ConditionType conditionType, String conditionValue, ActionType overrideAction, String extraData, boolean enabled) {
        this.name = name;
        this.conditionType = conditionType;
        this.conditionValue = conditionValue != null ? conditionValue : "";
        this.overrideAction = overrideAction;
        this.extraData = extraData != null ? extraData : "";
        this.enabled = enabled;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ConditionType getConditionType() {
        return conditionType;
    }

    public void setConditionType(ConditionType conditionType) {
        this.conditionType = conditionType;
    }

    public String getConditionValue() {
        return conditionValue;
    }

    public void setConditionValue(String conditionValue) {
        this.conditionValue = conditionValue != null ? conditionValue : "";
    }

    public ActionType getOverrideAction() {
        return overrideAction;
    }

    public void setOverrideAction(ActionType overrideAction) {
        this.overrideAction = overrideAction;
    }

    public String getExtraData() {
        return extraData;
    }

    public void setExtraData(String extraData) {
        this.extraData = extraData != null ? extraData : "";
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
