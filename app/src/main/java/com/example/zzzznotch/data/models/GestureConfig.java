package com.example.zzzznotch.data.models;

import java.util.Objects;

public class GestureConfig {
    private GestureType gestureType;
    private ActionType actionType;
    private String targetPackage;
    private String extraData;
    private boolean enabled;

    public GestureConfig() {
        this.gestureType = GestureType.SINGLE_TAP;
        this.actionType = ActionType.NONE;
        this.targetPackage = "";
        this.extraData = "";
        this.enabled = true;
    }

    public GestureConfig(GestureType gestureType, ActionType actionType, String targetPackage, String extraData, boolean enabled) {
        this.gestureType = gestureType;
        this.actionType = actionType;
        this.targetPackage = targetPackage != null ? targetPackage : "";
        this.extraData = extraData != null ? extraData : "";
        this.enabled = enabled;
    }

    public GestureType getGestureType() {
        return gestureType;
    }

    public void setGestureType(GestureType gestureType) {
        this.gestureType = gestureType;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public String getTargetPackage() {
        return targetPackage;
    }

    public void setTargetPackage(String targetPackage) {
        this.targetPackage = targetPackage != null ? targetPackage : "";
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GestureConfig that = (GestureConfig) o;
        return enabled == that.enabled &&
                gestureType == that.gestureType &&
                actionType == that.actionType &&
                Objects.equals(targetPackage, that.targetPackage) &&
                Objects.equals(extraData, that.extraData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gestureType, actionType, targetPackage, extraData, enabled);
    }
}
