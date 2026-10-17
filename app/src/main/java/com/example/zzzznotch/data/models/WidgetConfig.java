package com.example.zzzznotch.data.models;

public class WidgetConfig {
    private boolean showMediaWidget;
    private boolean showVolumeSlider;
    private boolean showBrightnessSlider;
    private boolean showTimerWidget;
    private boolean showStopwatchWidget;
    private boolean showQuickTools;

    public WidgetConfig() {
        this.showMediaWidget = true;
        this.showVolumeSlider = true;
        this.showBrightnessSlider = true;
        this.showTimerWidget = true;
        this.showStopwatchWidget = true;
        this.showQuickTools = true;
    }

    public boolean isShowMediaWidget() {
        return showMediaWidget;
    }

    public void setShowMediaWidget(boolean showMediaWidget) {
        this.showMediaWidget = showMediaWidget;
    }

    public boolean isShowVolumeSlider() {
        return showVolumeSlider;
    }

    public void setShowVolumeSlider(boolean showVolumeSlider) {
        this.showVolumeSlider = showVolumeSlider;
    }

    public boolean isShowBrightnessSlider() {
        return showBrightnessSlider;
    }

    public void setShowBrightnessSlider(boolean showBrightnessSlider) {
        this.showBrightnessSlider = showBrightnessSlider;
    }

    public boolean isShowTimerWidget() {
        return showTimerWidget;
    }

    public void setShowTimerWidget(boolean showTimerWidget) {
        this.showTimerWidget = showTimerWidget;
    }

    public boolean isShowStopwatchWidget() {
        return showStopwatchWidget;
    }

    public void setShowStopwatchWidget(boolean showStopwatchWidget) {
        this.showStopwatchWidget = showStopwatchWidget;
    }

    public boolean isShowQuickTools() {
        return showQuickTools;
    }

    public void setShowQuickTools(boolean showQuickTools) {
        this.showQuickTools = showQuickTools;
    }
}
