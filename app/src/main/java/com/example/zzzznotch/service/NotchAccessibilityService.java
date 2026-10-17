package com.example.zzzznotch.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;

import com.example.zzzznotch.rules.RuleEngine;
import com.example.zzzznotch.utils.Logger;

public class NotchAccessibilityService extends AccessibilityService {

    private static volatile NotchAccessibilityService instance;

    public static NotchAccessibilityService getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Logger.i("NotchAccessibilityService created");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        Logger.i("NotchAccessibilityService connected & active");
    }

    public boolean performSimulatedSwipe(int direction) {
        try {
            int width = getResources().getDisplayMetrics().widthPixels;
            int height = getResources().getDisplayMetrics().heightPixels;

            int startX = width / 2;
            int startY = height / 2;
            int endX = startX;
            int endY = startY;

            int swipeDistance = Math.min(width, height) / 3;

            // 0=UP, 1=DOWN, 2=LEFT, 3=RIGHT
            switch (direction) {
                case 0:
                    endY = startY - swipeDistance;
                    break;
                case 1:
                    endY = startY + swipeDistance;
                    break;
                case 2:
                    endX = startX - swipeDistance;
                    break;
                case 3:
                    endX = startX + swipeDistance;
                    break;
            }

            Path path = new Path();
            path.moveTo(startX, startY);
            path.lineTo(endX, endY);

            GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(path, 0, 250);
            GestureDescription.Builder builder = new GestureDescription.Builder();
            builder.addStroke(stroke);

            return dispatchGesture(builder.build(), null, null);
        } catch (Exception e) {
            Logger.e("Failed to dispatch simulated swipe", e);
            return false;
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        try {
            if (event == null) return;
            if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                CharSequence packageName = event.getPackageName();
                if (packageName != null) {
                    RuleEngine.setForegroundPackage(packageName.toString());
                }
            }
        } catch (Throwable t) {
            Logger.e("Accessibility Event Error", t);
        }
    }

    @Override
    public void onInterrupt() {
        Logger.i("NotchAccessibilityService interrupted");
    }

    @Override
    public boolean onUnbind(android.content.Intent intent) {
        instance = null;
        Logger.i("NotchAccessibilityService unbound");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        instance = null;
        Logger.i("NotchAccessibilityService destroyed");
        super.onDestroy();
    }
}
