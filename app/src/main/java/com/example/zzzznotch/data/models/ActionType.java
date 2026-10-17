package com.example.zzzznotch.data.models;

public enum ActionType {
    NONE("None", "No action assigned"),
    TOGGLE_MASTER("Toggle Master Notch", "Enable or disable notch touch interactions"),
    TOGGLE_DISTRACTION_FREE("Toggle Gaming/Movie Mode", "Pause gestures for distraction-free gaming & movies"),
    OPEN_WIDGET_PANEL("Open Quick Widget Panel", "Expand media controls, volume, timer & shortcuts"),
    LAUNCH_APP("Launch App", "Open a selected application"),
    OPEN_SETTINGS_PAGE("Open System Settings", "Open main Android Settings screen"),
    OPEN_SETTINGS_WIFI("Wi-Fi Settings", "Open Wi-Fi settings page"),
    OPEN_SETTINGS_BLUETOOTH("Bluetooth Settings", "Open Bluetooth settings page"),
    OPEN_SETTINGS_DISPLAY("Display Settings", "Open Display settings page"),
    OPEN_SETTINGS_BATTERY("Battery Settings", "Open Battery settings page"),
    OPEN_SETTINGS_SOUND("Sound Settings", "Open Sound & Vibration settings page"),
    OPEN_SETTINGS_APPS("Apps Manager", "Open Apps settings page"),
    FLASHLIGHT("Toggle Flashlight", "Turn device flashlight on or off"),
    MEDIA_PLAY_PAUSE("Media Play / Pause", "Toggle media playback"),
    MEDIA_NEXT("Next Track", "Skip to next media track"),
    MEDIA_PREVIOUS("Previous Track", "Return to previous media track"),
    VOLUME_UP("Volume Up", "Increase media volume"),
    VOLUME_DOWN("Volume Down", "Decrease media volume"),
    VOLUME_MUTE("Mute / Unmute", "Toggle media volume mute state"),
    BRIGHTNESS_UP("Brightness Up", "Increase screen brightness"),
    BRIGHTNESS_DOWN("Brightness Down", "Decrease screen brightness"),
    TOGGLE_AUTO_ROTATE("Toggle Auto Rotate", "Enable or disable screen auto-rotation"),
    TAKE_SCREENSHOT("Take Screenshot", "Capture screen content"),
    GLOBAL_BACK("Back", "Perform global Back action"),
    GLOBAL_HOME("Home", "Navigate to Home screen"),
    GLOBAL_RECENTS("Recents", "Open recent apps overview"),
    GLOBAL_NOTIFICATIONS("Notification Shade", "Expand notification panel"),
    GLOBAL_QUICK_SETTINGS("Quick Settings", "Expand quick settings panel"),
    GLOBAL_LOCK_SCREEN("Lock Screen", "Turn off screen / lock device"),
    SIMULATE_SWIPE_UP("Simulate Swipe Up", "Simulates an upward swipe on the screen (e.g. scroll down)"),
    SIMULATE_SWIPE_DOWN("Simulate Swipe Down", "Simulates a downward swipe on the screen (e.g. scroll up)"),
    SIMULATE_SWIPE_LEFT("Simulate Swipe Left", "Simulates a leftward swipe on the screen"),
    SIMULATE_SWIPE_RIGHT("Simulate Swipe Right", "Simulates a rightward swipe on the screen"),
    OPEN_NOTES("Quick Notes", "Open or view quick notes"),
    OPEN_QR_SCANNER("QR Scanner", "Launch camera preview and QR barcode scanner"),
    TOGGLE_RECORDING_VIDEO("Record Video", "Start/Stop background camera video recording"),
    TOGGLE_RECORDING_AUDIO("Record Audio", "Start/Stop background voice audio recording"),
    OPEN_RECORDING_LIBRARY("Recording Library", "View, preview and export private recordings"),
    BACKUP_RESTORE_SETTINGS("Backup & Restore", "Export or import configuration settings");

    private final String displayName;
    private final String description;

    ActionType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
