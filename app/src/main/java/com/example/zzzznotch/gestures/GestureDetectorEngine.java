package com.example.zzzznotch.gestures;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;

import com.example.zzzznotch.data.models.GestureType;
import com.example.zzzznotch.utils.DisplayUtils;
import com.example.zzzznotch.utils.Logger;

public class GestureDetectorEngine {

    private final GestureListener listener;
    private final Handler handler;

    private int tapTimeoutMs = 300;
    private int longPressTimeoutMs = 500;
    private int touchSlopPx = 24;
    private int swipeMinDistancePx = 48;

    private int tapCount = 0;
    private float downX = 0f;
    private float downY = 0f;
    private long downTimeMs = 0L;
    private boolean isLongPressTriggered = false;
    private boolean isSwipeGesture = false;

    private final Runnable tapEvaluatorRunnable = new Runnable() {
        @Override
        public void run() {
            if (tapCount > 0 && !isSwipeGesture) {
                GestureType type;
                switch (tapCount) {
                    case 1:
                        type = GestureType.SINGLE_TAP;
                        break;
                    case 2:
                        type = GestureType.DOUBLE_TAP;
                        break;
                    case 3:
                        type = GestureType.TRIPLE_TAP;
                        break;
                    case 4:
                        type = GestureType.FOUR_TAPS;
                        break;
                    case 5:
                    default:
                        type = GestureType.FIVE_TAPS;
                        break;
                }
                Logger.d("Gesture recognized: " + type.name() + " (Tap count: " + tapCount + ")");
                if (listener != null) {
                    listener.onGestureDetected(type);
                }
            }
            tapCount = 0;
        }
    };

    private final Runnable longPressRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isSwipeGesture && tapCount <= 1) {
                isLongPressTriggered = true;
                tapCount = 0;
                Logger.d("Gesture recognized: LONG_PRESS");
                if (listener != null) {
                    listener.onGestureDetected(GestureType.LONG_PRESS);
                }
            }
        }
    };

    public GestureDetectorEngine(Context context, GestureListener listener) {
        this.listener = listener;
        this.handler = new Handler(Looper.getMainLooper());
        if (context != null) {
            this.touchSlopPx = DisplayUtils.dpToPx(context, 24);
            this.swipeMinDistancePx = DisplayUtils.dpToPx(context, 48);
        }
    }

    public void configure(int recognitionWindowMs, int movementToleranceDp, Context context) {
        this.tapTimeoutMs = Math.max(150, Math.min(1000, recognitionWindowMs));
        if (context != null && movementToleranceDp > 0) {
            this.touchSlopPx = DisplayUtils.dpToPx(context, movementToleranceDp);
            this.swipeMinDistancePx = DisplayUtils.dpToPx(context, movementToleranceDp * 2);
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event == null) return false;

        float x = event.getRawX();
        float y = event.getRawY();

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downX = x;
                downY = y;
                downTimeMs = System.currentTimeMillis();
                isSwipeGesture = false;
                isLongPressTriggered = false;

                handler.removeCallbacks(tapEvaluatorRunnable);
                handler.postDelayed(longPressRunnable, longPressTimeoutMs);
                return true;

            case MotionEvent.ACTION_MOVE:
                float dx = x - downX;
                float dy = y - downY;
                double dist = Math.hypot(dx, dy);

                if (dist > touchSlopPx) {
                    isSwipeGesture = true;
                    handler.removeCallbacks(longPressRunnable);
                }
                return true;

            case MotionEvent.ACTION_UP:
                handler.removeCallbacks(longPressRunnable);

                if (isLongPressTriggered) {
                    return true;
                }

                float upDx = x - downX;
                float upDy = y - downY;
                double totalDist = Math.hypot(upDx, upDy);
                long durationMs = System.currentTimeMillis() - downTimeMs;

                if (totalDist >= swipeMinDistancePx) {
                    // It is a swipe gesture
                    handler.removeCallbacks(tapEvaluatorRunnable);
                    tapCount = 0;
                    GestureType swipeType = evaluateSwipeType(upDx, upDy, durationMs);
                    if (swipeType != null && listener != null) {
                        Logger.d("Gesture recognized: " + swipeType.name());
                        listener.onGestureDetected(swipeType);
                    }
                    return true;
                }

                if (!isSwipeGesture) {
                    tapCount++;
                    if (tapCount >= 5) {
                        handler.removeCallbacks(tapEvaluatorRunnable);
                        tapEvaluatorRunnable.run();
                    } else {
                        handler.removeCallbacks(tapEvaluatorRunnable);
                        handler.postDelayed(tapEvaluatorRunnable, tapTimeoutMs);
                    }
                }
                return true;

            case MotionEvent.ACTION_CANCEL:
                handler.removeCallbacks(longPressRunnable);
                handler.removeCallbacks(tapEvaluatorRunnable);
                tapCount = 0;
                isSwipeGesture = false;
                return true;
        }

        return false;
    }

    private GestureType evaluateSwipeType(float dx, float dy, long durationMs) {
        boolean isHold = durationMs >= 400;

        if (Math.abs(dx) > Math.abs(dy)) {
            // Horizontal swipe
            if (dx > 0) {
                return isHold ? GestureType.SWIPE_HOLD_RIGHT : GestureType.SWIPE_RIGHT;
            } else {
                return isHold ? GestureType.SWIPE_HOLD_LEFT : GestureType.SWIPE_LEFT;
            }
        } else {
            // Vertical swipe
            if (dy > 0) {
                return isHold ? GestureType.SWIPE_HOLD_DOWN : GestureType.SWIPE_DOWN;
            } else {
                return isHold ? GestureType.SWIPE_HOLD_UP : GestureType.SWIPE_UP;
            }
        }
    }
}
