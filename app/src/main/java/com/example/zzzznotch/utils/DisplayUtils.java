package com.example.zzzznotch.utils;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.DisplayCutout;
import android.view.WindowInsets;
import android.view.WindowManager;

import java.util.List;

public class DisplayUtils {

    public static int dpToPx(Context context, float dp) {
        if (context == null) return (int) dp;
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics));
    }

    public static int pxToDp(Context context, int px) {
        if (context == null) return px;
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return Math.round(px / (metrics.densityDpi / 160f));
    }

    public static int getScreenWidthPx(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (wm == null) return 1080;
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(metrics);
        return metrics.widthPixels;
    }

    public static int getScreenHeightPx(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (wm == null) return 2400;
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(metrics);
        return metrics.heightPixels;
    }

    public static Rect getDisplayCutoutBoundingRect(WindowInsets windowInsets) {
        if (windowInsets == null) return null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            DisplayCutout cutout = windowInsets.getDisplayCutout();
            if (cutout != null) {
                List<Rect> boundingRects = cutout.getBoundingRects();
                if (boundingRects != null && !boundingRects.isEmpty()) {
                    return boundingRects.get(0);
                }
            }
        }
        return null;
    }
}
