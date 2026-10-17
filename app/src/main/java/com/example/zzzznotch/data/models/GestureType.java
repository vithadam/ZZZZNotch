package com.example.zzzznotch.data.models;

public enum GestureType {
    SINGLE_TAP("Single Tap", "Triggered by a single quick tap"),
    DOUBLE_TAP("Double Tap", "Triggered by two quick consecutive taps"),
    TRIPLE_TAP("Triple Tap", "Triggered by three quick consecutive taps"),
    FOUR_TAPS("Four Taps", "Triggered by four quick consecutive taps"),
    FIVE_TAPS("Five Taps", "Triggered by five quick consecutive taps"),
    LONG_PRESS("Long Press", "Triggered by holding touch on the notch area"),
    SWIPE_UP("Swipe Up", "Triggered by swiping upward"),
    SWIPE_DOWN("Swipe Down", "Triggered by swiping downward"),
    SWIPE_LEFT("Swipe Left", "Triggered by swiping leftward"),
    SWIPE_RIGHT("Swipe Right", "Triggered by swiping rightward"),
    SWIPE_HOLD_UP("Swipe Up & Hold", "Swipe upward and hold position"),
    SWIPE_HOLD_DOWN("Swipe Down & Hold", "Swipe downward and hold position"),
    SWIPE_HOLD_LEFT("Swipe Left & Hold", "Swipe leftward and hold position"),
    SWIPE_HOLD_RIGHT("Swipe Right & Hold", "Swipe rightward and hold position");

    private final String displayName;
    private final String description;

    GestureType(String displayName, String description) {
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
