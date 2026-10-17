package com.example.zzzznotch.recording;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.example.zzzznotch.R;
import com.example.zzzznotch.utils.Logger;
import com.example.zzzznotch.utils.PermissionUtils;

import java.io.File;

public class VideoRecordingService extends Service implements SurfaceHolder.Callback {

    public static final String ACTION_START_RECORDING = "com.example.zzzznotch.START_VIDEO_REC";
    public static final String ACTION_STOP_RECORDING = "com.example.zzzznotch.STOP_VIDEO_REC";

    private static final String CHANNEL_ID = "notch_video_rec_channel";
    private static final int NOTIFICATION_ID = 2002;

    private static boolean isRecording = false;

    private WindowManager windowManager;
    private SurfaceView surfaceView;
    private Camera camera;
    private MediaRecorder mediaRecorder;
    private File currentOutputFile;

    public static boolean isRecording() {
        return isRecording;
    }

    public static void toggleRecording(Context context) {
        Intent intent = new Intent(context, VideoRecordingService.class);
        if (isRecording) {
            intent.setAction(ACTION_STOP_RECORDING);
        } else {
            intent.setAction(ACTION_START_RECORDING);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            if (ACTION_START_RECORDING.equals(action)) {
                startForeground(NOTIFICATION_ID, buildNotification("Video Recording Active..."));
                setupHiddenSurface();
            } else if (ACTION_STOP_RECORDING.equals(action)) {
                stopVideoRecording();
                stopForeground(true);
                stopSelf();
            }
        }
        return START_STICKY;
    }

    private void setupHiddenSurface() {
        if (isRecording) return;
        try {
            if (!PermissionUtils.hasOverlayPermission(this)) {
                Logger.e("Overlay permission is required for background video recording on this OS version.");
                Toast.makeText(this, "Overlay permission required for Video Recording", Toast.LENGTH_LONG).show();
                stopSelf();
                return;
            }

            windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            surfaceView = new SurfaceView(this);
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    1, 1,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    PixelFormat.TRANSLUCENT
            );
            params.gravity = Gravity.TOP | Gravity.START;
            params.x = 0;
            params.y = 0;
            
            surfaceView.getHolder().addCallback(this);
            windowManager.addView(surfaceView, params);
        } catch (Exception e) {
            Logger.e("Failed to setup hidden surface", e);
            stopSelf();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        startVideoRecording(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {}

    private void startVideoRecording(SurfaceHolder holder) {
        try {
            RecordingManager manager = RecordingManager.getInstance();
            if (manager.getAvailableStorageMb(this) < 200) {
                Logger.e("Low storage space! Cannot start recording.");
                removeHiddenSurface();
                stopSelf();
                return;
            }

            if (!PermissionUtils.hasCameraPermission(this)) {
                Logger.e("Camera permission missing!");
                removeHiddenSurface();
                stopSelf();
                return;
            }

            currentOutputFile = manager.createNewVideoFile(this);

            camera = Camera.open();
            camera.unlock();

            mediaRecorder = new MediaRecorder();
            mediaRecorder.setCamera(camera);

            boolean hasAudio = PermissionUtils.hasAudioPermission(this);
            if (hasAudio) {
                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            }
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

            CamcorderProfile profile = CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_720P) ?
                    CamcorderProfile.get(CamcorderProfile.QUALITY_720P) :
                    CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);

            if (!hasAudio) {
                profile.audioCodec = 0;
            }

            mediaRecorder.setProfile(profile);
            mediaRecorder.setOutputFile(currentOutputFile.getAbsolutePath());
            mediaRecorder.setPreviewDisplay(holder.getSurface());

            mediaRecorder.prepare();
            mediaRecorder.start();

            isRecording = true;
            Logger.i("Video recording started: " + currentOutputFile.getName());

        } catch (Exception e) {
            Logger.e("Video recording failed to start", e);
            releaseCameraAndRecorder();
            removeHiddenSurface();
            stopSelf();
        }
    }

    private void stopVideoRecording() {
        if (!isRecording) return;

        try {
            if (mediaRecorder != null) {
                mediaRecorder.stop();
                mediaRecorder.reset();
            }
            Logger.i("Video recording saved: " + (currentOutputFile != null ? currentOutputFile.getName() : ""));
        } catch (Exception e) {
            Logger.e("Error stopping video recording", e);
        } finally {
            releaseCameraAndRecorder();
            removeHiddenSurface();
            isRecording = false;
        }
    }

    private void removeHiddenSurface() {
        if (surfaceView != null && windowManager != null) {
            try {
                windowManager.removeView(surfaceView);
            } catch (Exception ignored) {
            }
            surfaceView = null;
        }
    }

    private void releaseCameraAndRecorder() {
        if (mediaRecorder != null) {
            try {
                mediaRecorder.release();
            } catch (Exception ignored) {
            }
            mediaRecorder = null;
        }
        if (camera != null) {
            try {
                camera.lock();
                camera.release();
            } catch (Exception ignored) {
            }
            camera = null;
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Action Notch Video Recording",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Background Video Recording Notification");
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) {
                nm.createNotificationChannel(channel);
            }
        }
    }

    private Notification buildNotification(String text) {
        Intent stopIntent = new Intent(this, VideoRecordingService.class);
        stopIntent.setAction(ACTION_STOP_RECORDING);
        PendingIntent pStop = PendingIntent.getService(this, 2, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("zzzznotch - Video Recording")
                .setContentText(text)
                .setSmallIcon(R.mipmap.ic_launcher)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop & Save", pStop)
                .setOngoing(true)
                .build();
    }

    @Override
    public void onDestroy() {
        stopVideoRecording();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
