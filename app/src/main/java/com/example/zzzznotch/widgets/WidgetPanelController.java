package com.example.zzzznotch.widgets;

import android.content.Context;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.zzzznotch.R;
import com.example.zzzznotch.actions.ActionExecutor;
import com.example.zzzznotch.data.models.ActionType;
import com.example.zzzznotch.data.models.GestureConfig;
import com.example.zzzznotch.data.preferences.AppPreferences;
import com.example.zzzznotch.utils.Logger;
import com.example.zzzznotch.utils.PermissionUtils;

public class WidgetPanelController {

    private final Context context;
    private final WindowManager windowManager;
    private View panelView;
    private boolean isShowing = false;

    // Media
    private TextView tvMediaTitle;
    private TextView tvMediaArtist;
    private ImageButton btnMediaPrev;
    private ImageButton btnMediaPlayPause;
    private ImageButton btnMediaNext;

    // Sliders
    private SeekBar seekVolume;
    private SeekBar seekBrightness;

    // Timer
    private TextView tvTimerDisplay;
    private Button btnTimerStart;
    private Button btnTimerReset;
    private CountDownTimer countDownTimer;
    private long timerTimeLeftMs = 300000; // 5 min default
    private boolean isTimerRunning = false;

    // Stopwatch
    private TextView tvStopwatchDisplay;
    private Button btnStopwatchStart;
    private Button btnStopwatchReset;
    private final Handler stopwatchHandler = new Handler(Looper.getMainLooper());
    private long stopwatchStartTimeMs = 0L;
    private long stopwatchTimeBuffMs = 0L;
    private boolean isStopwatchRunning = false;

    public WidgetPanelController(Context context) {
        this.context = context;
        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    public synchronized void togglePanel() {
        if (isShowing) {
            hidePanel();
        } else {
            showPanel();
        }
    }

    public synchronized void showPanel() {
        if (isShowing) return;

        try {
            LayoutInflater inflater = LayoutInflater.from(context);
            panelView = inflater.inflate(R.layout.layout_widget_panel, null);

            setupViews(panelView);

            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                    PixelFormat.TRANSLUCENT
            );
            params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            params.y = 80;

            panelView.setOnTouchListener((v, event) -> {
                if (event.getAction() == android.view.MotionEvent.ACTION_OUTSIDE) {
                    hidePanel();
                    return true;
                }
                return false;
            });

            windowManager.addView(panelView, params);
            isShowing = true;
            Logger.d("Widget panel displayed");
        } catch (Exception e) {
            Logger.e("Failed to show widget panel", e);
        }
    }

    public synchronized void hidePanel() {
        if (!isShowing || panelView == null) return;
        try {
            windowManager.removeView(panelView);
            panelView = null;
            isShowing = false;
            Logger.d("Widget panel hidden");
        } catch (Exception e) {
            Logger.e("Failed to hide widget panel", e);
        }
    }

    private void setupViews(View v) {
        // Media controls
        tvMediaTitle = v.findViewById(R.id.tv_media_title);
        tvMediaArtist = v.findViewById(R.id.tv_media_artist);
        btnMediaPrev = v.findViewById(R.id.btn_media_prev);
        btnMediaPlayPause = v.findViewById(R.id.btn_media_play_pause);
        btnMediaNext = v.findViewById(R.id.btn_media_next);

        btnMediaPrev.setOnClickListener(view -> ActionExecutor.executeAction(context, new GestureConfig(null, ActionType.MEDIA_PREVIOUS, "", "", true)));
        btnMediaPlayPause.setOnClickListener(view -> ActionExecutor.executeAction(context, new GestureConfig(null, ActionType.MEDIA_PLAY_PAUSE, "", "", true)));
        btnMediaNext.setOnClickListener(view -> ActionExecutor.executeAction(context, new GestureConfig(null, ActionType.MEDIA_NEXT, "", "", true)));

        // Volume Seekbar
        seekVolume = v.findViewById(R.id.seek_volume);
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (am != null) {
            int maxVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            int curVol = am.getStreamVolume(AudioManager.STREAM_MUSIC);
            seekVolume.setMax(maxVol);
            seekVolume.setProgress(curVol);
            seekVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        am.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        }

        // Brightness Seekbar
        seekBrightness = v.findViewById(R.id.seek_brightness);
        try {
            int curBright = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 128);
            seekBrightness.setMax(255);
            seekBrightness.setProgress(curBright);
            seekBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        if (PermissionUtils.hasWriteSettingsPermission(context)) {
                            Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, Math.max(10, progress));
                        } else {
                            PermissionUtils.requestWriteSettingsPermission(context);
                        }
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        } catch (Exception ignored) {
        }

        // Timer
        tvTimerDisplay = v.findViewById(R.id.tv_timer_display);
        btnTimerStart = v.findViewById(R.id.btn_timer_start);
        btnTimerReset = v.findViewById(R.id.btn_timer_reset);

        btnTimerStart.setOnClickListener(view -> {
            if (isTimerRunning) {
                pauseTimer();
            } else {
                startTimer();
            }
        });

        btnTimerReset.setOnClickListener(view -> resetTimer());

        // Stopwatch
        tvStopwatchDisplay = v.findViewById(R.id.tv_stopwatch_display);
        btnStopwatchStart = v.findViewById(R.id.btn_stopwatch_start);
        btnStopwatchReset = v.findViewById(R.id.btn_stopwatch_reset);

        btnStopwatchStart.setOnClickListener(view -> {
            if (isStopwatchRunning) {
                pauseStopwatch();
            } else {
                startStopwatch();
            }
        });

        btnStopwatchReset.setOnClickListener(view -> resetStopwatch());

        // Close Panel Button
        v.findViewById(R.id.btn_close_panel).setOnClickListener(view -> hidePanel());

        // Quick Tools Buttons
        v.findViewById(R.id.btn_tool_flashlight).setOnClickListener(view -> ActionExecutor.executeAction(context, new GestureConfig(null, ActionType.FLASHLIGHT, "", "", true)));
        v.findViewById(R.id.btn_tool_notes).setOnClickListener(view -> ActionExecutor.executeAction(context, new GestureConfig(null, ActionType.OPEN_NOTES, "", "", true)));
        v.findViewById(R.id.btn_tool_qr).setOnClickListener(view -> ActionExecutor.executeAction(context, new GestureConfig(null, ActionType.OPEN_QR_SCANNER, "", "", true)));
        v.findViewById(R.id.btn_tool_settings).setOnClickListener(view -> ActionExecutor.executeAction(context, new GestureConfig(null, ActionType.OPEN_SETTINGS_PAGE, "", "", true)));
    }

    public void updateMediaInfo(String title, String artist, boolean isPlaying) {
        if (tvMediaTitle != null && title != null && !title.isEmpty()) {
            tvMediaTitle.setText(title);
        }
        if (tvMediaArtist != null && artist != null) {
            tvMediaArtist.setText(artist);
        }
        if (btnMediaPlayPause != null) {
            btnMediaPlayPause.setImageResource(isPlaying ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
        }
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(timerTimeLeftMs, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerTimeLeftMs = millisUntilFinished;
                updateTimerText();
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;
                btnTimerStart.setText("Start");
                Toast.makeText(context, "Timer Finished!", Toast.LENGTH_LONG).show();
            }
        }.start();

        isTimerRunning = true;
        btnTimerStart.setText("Pause");
    }

    private void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isTimerRunning = false;
        btnTimerStart.setText("Start");
    }

    private void resetTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        timerTimeLeftMs = 300000;
        isTimerRunning = false;
        btnTimerStart.setText("Start");
        updateTimerText();
    }

    private void updateTimerText() {
        int minutes = (int) (timerTimeLeftMs / 1000) / 60;
        int seconds = (int) (timerTimeLeftMs / 1000) % 60;
        if (tvTimerDisplay != null) {
            tvTimerDisplay.setText(String.format("%02d:%02d", minutes, seconds));
        }
    }

    private final Runnable updateStopwatchRunnable = new Runnable() {
        @Override
        public void run() {
            long timeInMs = SystemClock.uptimeMillis() - stopwatchStartTimeMs;
            long updatedTimeMs = stopwatchTimeBuffMs + timeInMs;
            int secs = (int) (updatedTimeMs / 1000);
            int mins = secs / 60;
            secs = secs % 60;
            int milliseconds = (int) (updatedTimeMs % 1000) / 10;
            if (tvStopwatchDisplay != null) {
                tvStopwatchDisplay.setText(String.format("%02d:%02d:%02d", mins, secs, milliseconds));
            }
            stopwatchHandler.postDelayed(this, 30);
        }
    };

    private void startStopwatch() {
        stopwatchStartTimeMs = SystemClock.uptimeMillis();
        stopwatchHandler.postDelayed(updateStopwatchRunnable, 0);
        isStopwatchRunning = true;
        btnStopwatchStart.setText("Pause");
    }

    private void pauseStopwatch() {
        stopwatchTimeBuffMs += SystemClock.uptimeMillis() - stopwatchStartTimeMs;
        stopwatchHandler.removeCallbacks(updateStopwatchRunnable);
        isStopwatchRunning = false;
        btnStopwatchStart.setText("Start");
    }

    private void resetStopwatch() {
        stopwatchStartTimeMs = 0L;
        stopwatchTimeBuffMs = 0L;
        stopwatchHandler.removeCallbacks(updateStopwatchRunnable);
        isStopwatchRunning = false;
        btnStopwatchStart.setText("Start");
        if (tvStopwatchDisplay != null) {
            tvStopwatchDisplay.setText("00:00:00");
        }
    }
}
