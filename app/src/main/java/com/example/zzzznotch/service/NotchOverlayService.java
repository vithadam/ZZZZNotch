package com.example.zzzznotch.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.core.app.NotificationCompat;

import com.example.zzzznotch.R;
import com.example.zzzznotch.actions.ActionExecutor;
import com.example.zzzznotch.data.models.GestureConfig;
import com.example.zzzznotch.data.models.GestureType;
import com.example.zzzznotch.data.models.NotchAppearance;
import com.example.zzzznotch.data.preferences.AppPreferences;
import com.example.zzzznotch.gestures.GestureDetectorEngine;
import com.example.zzzznotch.gestures.GestureListener;
import com.example.zzzznotch.rules.RuleEngine;
import com.example.zzzznotch.ui.MainActivity;
import com.example.zzzznotch.utils.DisplayUtils;
import com.example.zzzznotch.utils.DistractionFreeManager;
import com.example.zzzznotch.utils.Logger;
import com.example.zzzznotch.utils.PermissionUtils;
import com.example.zzzznotch.widgets.WidgetPanelController;

import java.io.InputStream;

public class NotchOverlayService extends Service implements GestureListener {

    public static final String ACTION_UPDATE_OVERLAY = "com.example.zzzznotch.UPDATE_OVERLAY";
    public static final String ACTION_TOGGLE_MASTER = "com.example.zzzznotch.TOGGLE_MASTER";
    public static final String ACTION_TOGGLE_WIDGET_PANEL = "com.example.zzzznotch.TOGGLE_WIDGET_PANEL";

    private static final String CHANNEL_ID = "notch_service_channel";
    private static final int NOTIFICATION_ID = 1001;

    private WindowManager windowManager;
    private View overlayView;
    private RelativeLayout notchContainer;
    private ImageView imgLogo;

    private GestureDetectorEngine gestureEngine;
    private WidgetPanelController widgetPanelController;
    private AppPreferences preferences;

    public static void updateServiceState(Context context) {
        if (context == null) return;
        Intent intent = new Intent(context, NotchOverlayService.class);
        intent.setAction(ACTION_UPDATE_OVERLAY);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent);
            } else {
                context.startService(intent);
            }
        } catch (Exception e) {
            Logger.e("Failed to start/update NotchOverlayService", e);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.i("NotchOverlayService created");
        preferences = AppPreferences.getInstance(this);
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        widgetPanelController = new WidgetPanelController(this);
        gestureEngine = new GestureDetectorEngine(this, this);

        createNotificationChannel();
        startForeground(NOTIFICATION_ID, buildNotification());

        if (PermissionUtils.hasOverlayPermission(this)) {
            setupOverlay();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            Logger.d("NotchOverlayService received command: " + action);

            if (ACTION_TOGGLE_MASTER.equals(action)) {
                boolean newState = !preferences.isMasterEnabled();
                preferences.setMasterEnabled(newState);
                updateNotification();
            } else if (ACTION_TOGGLE_WIDGET_PANEL.equals(action)) {
                if (widgetPanelController != null) {
                    widgetPanelController.togglePanel();
                }
            }
        }

        if (overlayView == null && PermissionUtils.hasOverlayPermission(this)) {
            setupOverlay();
        }

        refreshOverlayAppearance();
        return START_STICKY;
    }

    private void setupOverlay() {
        if (overlayView != null) return;

        try {
            LayoutInflater inflater = LayoutInflater.from(this);
            overlayView = inflater.inflate(R.layout.layout_notch_overlay, null);

            notchContainer = overlayView.findViewById(R.id.notch_container);
            imgLogo = overlayView.findViewById(R.id.img_logo);

            overlayView.setOnTouchListener((v, event) -> {
                if (!preferences.isMasterEnabled() || DistractionFreeManager.getInstance().isDistractionFreeActive()) {
                    return false;
                }

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    notchContainer.animate().scaleX(0.92f).scaleY(0.92f).setDuration(100).start();
                    if (preferences.isHapticFeedbackEnabled()) {
                        v.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    notchContainer.animate().scaleX(1.0f).scaleY(1.0f).setDuration(150).start();
                }

                gestureEngine.onTouchEvent(event);
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.performClick();
                }
                return true;
            });

            WindowManager.LayoutParams params = createLayoutParams();
            windowManager.addView(overlayView, params);
            Logger.i("Overlay added to WindowManager");
        } catch (Exception e) {
            Logger.e("Failed to setup overlay", e);
        }
    }

    private WindowManager.LayoutParams createLayoutParams() {
        NotchAppearance appearance = preferences.getNotchAppearance();

        int widthPx = DisplayUtils.dpToPx(this, appearance.getWidthDp());
        int heightPx = DisplayUtils.dpToPx(this, appearance.getHeightDp());
        int offsetXPx = DisplayUtils.dpToPx(this, appearance.getOffsetXDp());
        int offsetYPx = DisplayUtils.dpToPx(this, appearance.getOffsetYDp());

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                widthPx,
                heightPx,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        params.x = offsetXPx;
        params.y = offsetYPx;

        return params;
    }

    private void refreshOverlayAppearance() {
        if (overlayView == null || notchContainer == null) return;

        boolean isEnabled = preferences.isMasterEnabled();
        boolean isDistractionFree = DistractionFreeManager.getInstance().isDistractionFreeActive();
        NotchAppearance appearance = preferences.getNotchAppearance();

        gestureEngine.configure(
                preferences.getRecognitionWindowMs(),
                preferences.getMovementToleranceDp(),
                this
        );

        if (!isEnabled) {
            overlayView.setVisibility(View.GONE);
            return;
        }

        if (isDistractionFree) {
            // Subtle 10% opacity in gaming/movie mode
            overlayView.setVisibility(View.VISIBLE);
            notchContainer.setAlpha(0.1f);
        } else {
            overlayView.setVisibility(View.VISIBLE);
            notchContainer.setAlpha(appearance.getAlpha());
        }

        if (preferences.isDisableInLandscapeEnabled()) {
            if (getResources().getConfiguration().orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
                overlayView.setVisibility(View.GONE);
            }
        }

        // Update WindowManager LayoutParams dimensions
        try {
            WindowManager.LayoutParams params = createLayoutParams();
            windowManager.updateViewLayout(overlayView, params);
        } catch (Exception e) {
            Logger.e("Error updating overlay layout params", e);
        }

        boolean hasLogo = appearance.getPhotoOverlayMode() != NotchAppearance.PhotoOverlayMode.DISABLED &&
                appearance.getPhotoUri() != null && !appearance.getPhotoUri().isEmpty();

        // Apply Shape & Background
        GradientDrawable drawable = new GradientDrawable();
        switch (appearance.getShapeType()) {
            case OVAL:
                drawable.setShape(GradientDrawable.OVAL);
                break;
            case PILL:
                drawable.setShape(GradientDrawable.RECTANGLE);
                drawable.setCornerRadius(DisplayUtils.dpToPx(this, appearance.getHeightDp() / 2f));
                break;
            case ROUNDED_RECTANGLE:
            default:
                drawable.setShape(GradientDrawable.RECTANGLE);
                drawable.setCornerRadius(DisplayUtils.dpToPx(this, appearance.getCornerRadiusDp()));
                break;
        }

        if (hasLogo) {
            drawable.setColor(Color.TRANSPARENT);
        } else {
            drawable.setColor(appearance.getBackgroundColor());
        }
        
        if (preferences.isShowTouchAreaEnabled() && !hasLogo) {
            drawable.setStroke(DisplayUtils.dpToPx(this, 2), Color.RED);
        }
        
        notchContainer.setBackground(drawable);

        // Apply Custom Logo / Photo Overlay
        if (hasLogo) {
            try {
                Uri uri = Uri.parse(appearance.getPhotoUri());
                InputStream is = getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                if (is != null) is.close();

                if (bitmap != null) {
                    imgLogo.setImageBitmap(bitmap);
                    imgLogo.setVisibility(View.VISIBLE);
                    int paddingPx = DisplayUtils.dpToPx(this, appearance.getImagePaddingDp());
                    imgLogo.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
                } else {
                    imgLogo.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                Logger.e("Error loading custom logo bitmap", e);
                imgLogo.setVisibility(View.GONE);
            }
        } else {
            imgLogo.setVisibility(View.GONE);
        }
    }

    @Override
    public void onGestureDetected(GestureType gestureType) {
        if (!preferences.isMasterEnabled() || DistractionFreeManager.getInstance().isDistractionFreeActive()) return;

        Logger.d("Gesture detected on Notch Service: " + gestureType.name());

        GestureConfig defaultConfig = preferences.getGestureConfig(gestureType);
        if (defaultConfig != null && defaultConfig.isEnabled()) {
            GestureConfig evaluatedConfig = RuleEngine.evaluateRules(this, defaultConfig);
            ActionExecutor.executeAction(this, evaluatedConfig);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "zzzznotch Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Maintains zzzznotch overlay & gesture listener");
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) {
                nm.createNotificationChannel(channel);
            }
        }
    }

    private Notification buildNotification() {
        boolean masterEnabled = preferences.isMasterEnabled();

        Intent appIntent = new Intent(this, MainActivity.class);
        PendingIntent pApp = PendingIntent.getActivity(this, 0, appIntent, PendingIntent.FLAG_IMMUTABLE);

        Intent toggleIntent = new Intent(this, NotchOverlayService.class);
        toggleIntent.setAction(ACTION_TOGGLE_MASTER);
        PendingIntent pToggle = PendingIntent.getService(this, 1, toggleIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String title = "zzzznotch is " + (masterEnabled ? "Active" : "Disabled");
        String contentText = masterEnabled ? "Touch the notch to trigger configured actions" : "Notch gestures paused. Tap to re-enable.";

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(contentText)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pApp)
                .addAction(
                        android.R.drawable.ic_menu_manage,
                        masterEnabled ? "Disable Notch" : "Enable Notch",
                        pToggle
                )
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void updateNotification() {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (nm != null && PermissionUtils.hasPostNotificationsPermission(this)) {
            nm.notify(NOTIFICATION_ID, buildNotification());
        }
    }

    @Override
    public void onDestroy() {
        if (overlayView != null && windowManager != null) {
            try {
                windowManager.removeView(overlayView);
            } catch (Exception ignored) {
            }
            overlayView = null;
        }
        Logger.i("NotchOverlayService destroyed");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
