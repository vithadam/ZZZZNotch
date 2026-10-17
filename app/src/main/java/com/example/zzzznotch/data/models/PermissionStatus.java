package com.example.zzzznotch.data.models;

public class PermissionStatus {
    private String title;
    private String description;
    private boolean isGranted;
    private boolean isRequired;

    public PermissionStatus(String title, String description, boolean isGranted, boolean isRequired) {
        this.title = title;
        this.description = description;
        this.isGranted = isGranted;
        this.isRequired = isRequired;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public boolean isGranted() {
        return isGranted;
    }

    public void setGranted(boolean granted) {
        isGranted = granted;
    }

    public boolean isRequired() {
        return isRequired;
    }
}
