package com.example.zzzznotch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.zzzznotch.data.models.ActionType;
import com.example.zzzznotch.data.models.GestureConfig;
import com.example.zzzznotch.data.models.GestureType;
import com.example.zzzznotch.data.models.NotchAppearance;

import org.junit.Test;

public class GestureEngineTest {

    @Test
    public void testGestureTypeEnum() {
        assertEquals("Single Tap", GestureType.SINGLE_TAP.getDisplayName());
        assertEquals("Double Tap", GestureType.DOUBLE_TAP.getDisplayName());
        assertEquals("Triple Tap", GestureType.TRIPLE_TAP.getDisplayName());
        assertEquals("Four Taps", GestureType.FOUR_TAPS.getDisplayName());
        assertEquals("Five Taps", GestureType.FIVE_TAPS.getDisplayName());
        assertEquals("Long Press", GestureType.LONG_PRESS.getDisplayName());
        assertEquals("Swipe Up", GestureType.SWIPE_UP.getDisplayName());
        assertEquals("Swipe Down", GestureType.SWIPE_DOWN.getDisplayName());
        assertEquals("Swipe Left", GestureType.SWIPE_LEFT.getDisplayName());
        assertEquals("Swipe Right", GestureType.SWIPE_RIGHT.getDisplayName());
        assertEquals("Swipe Up & Hold", GestureType.SWIPE_HOLD_UP.getDisplayName());
    }

    @Test
    public void testActionTypeEnum() {
        assertEquals("None", ActionType.NONE.getDisplayName());
        assertEquals("Toggle Master Notch", ActionType.TOGGLE_MASTER.getDisplayName());
        assertEquals("Toggle Gaming/Movie Mode", ActionType.TOGGLE_DISTRACTION_FREE.getDisplayName());
        assertEquals("Simulate Swipe Up", ActionType.SIMULATE_SWIPE_UP.getDisplayName());
        assertEquals("Simulate Swipe Down", ActionType.SIMULATE_SWIPE_DOWN.getDisplayName());
        assertEquals("Simulate Swipe Left", ActionType.SIMULATE_SWIPE_LEFT.getDisplayName());
        assertEquals("Simulate Swipe Right", ActionType.SIMULATE_SWIPE_RIGHT.getDisplayName());
    }

    @Test
    public void testGestureConfigDefaults() {
        GestureConfig config = new GestureConfig(GestureType.DOUBLE_TAP, ActionType.GLOBAL_HOME, "", "", true);
        assertEquals(GestureType.DOUBLE_TAP, config.getGestureType());
        assertEquals(ActionType.GLOBAL_HOME, config.getActionType());
        assertTrue(config.isEnabled());
    }

    @Test
    public void testNotchAppearanceDefaults() {
        NotchAppearance appearance = new NotchAppearance();
        assertEquals(120, appearance.getWidthDp());
        assertEquals(36, appearance.getHeightDp());
        assertEquals(0, appearance.getOffsetXDp());
        assertEquals(0, appearance.getOffsetYDp());
        assertNotNull(appearance.getShapeType());
    }
}
