package com.example.zzzznotch.actions;

import com.example.zzzznotch.data.models.ActionType;

public class ActionInfo {
    private final ActionType actionType;
    private final String title;
    private final String description;
    private final String requiredPermission;
    private final boolean isAvailable;

    public ActionInfo(ActionType actionType, String title, String description, String requiredPermission, boolean isAvailable) {
        this.actionType = actionType;
        this.title = title;
        this.description = description;
        this.requiredPermission = requiredPermission != null ? requiredPermission : "";
        this.isAvailable = isAvailable;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getRequiredPermission() {
        return requiredPermission;
    }

    public boolean isAvailable() {
        return isAvailable;
    }
}
