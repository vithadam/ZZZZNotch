package com.example.zzzznotch.recording;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.example.zzzznotch.R;
import com.example.zzzznotch.utils.Logger;
import com.example.zzzznotch.utils.PermissionUtils;

import java.io.File;

public class AudioRecordingService extends Service {

    public static final String ACTION_START_AUDIO_REC = "com.example.zzzznotch.START_AUDIO_REC";
    public static final String ACTION_STOP_AUDIO_REC = "com.example.zzzznotch.STOP_AUDIO_REC";

    private static final String CHANNEL_ID = "notch_audio_rec_channel";
    private static final int NOTIFICATION_ID = 2003;

    private static boolean isRecording = false;

    private MediaRecorder mediaRecorder;
    private File currentOutputFile;

    public static boolean isRecording() {
        return isRecording;
    }

    public static void toggleRecording(Context context) {
        Intent intent = new Intent(context, AudioRecordingService.class);
        if (isRecording) {
            intent.setAction(ACTION_STOP_AUDIO_REC);
        } else {
            intent.setAction(ACTION_START_AUDIO_REC);
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
            if (ACTION_START_AUDIO_REC.equals(action)) {
                startForeground(NOTIFICATION_ID, buildNotification("Audio Recording Active..."));
                startAudioRecording();
            } else if (ACTION_STOP_AUDIO_REC.equals(action)) {
                stopAudioRecording();
                stopForeground(true);
                stopSelf();
            }
        }
        return START_STICKY;
    }

    private void startAudioRecording() {
        if (isRecording) return;

        try {
            RecordingManager manager = RecordingManager.getInstance();
            if (manager.getAvailableStorageMb(this) < 50) {
                Toast.makeText(this, "Low storage space! Cannot start audio recording.", Toast.LENGTH_LONG).show();
                stopSelf();
                return;
            }

            currentOutputFile = manager.createNewAudioFile(this);

            if (!PermissionUtils.hasAudioPermission(this)) {
                Toast.makeText(this, "Microphone permission missing!", Toast.LENGTH_LONG).show();
                stopSelf();
                return;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                mediaRecorder = new MediaRecorder(this);
            } else {
                mediaRecorder = new MediaRecorder();
            }

            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setAudioSamplingRate(44100);
            mediaRecorder.setAudioEncodingBitRate(128000);
            mediaRecorder.setOutputFile(currentOutputFile.getAbsolutePath());

            mediaRecorder.prepare();
            mediaRecorder.start();

            isRecording = true;
            Logger.i("Audio recording started: " + currentOutputFile.getName());
            // Toast removed to not acknowledge recording start

        } catch (Exception e) {
            Logger.e("Audio recording failed to start", e);
            // Toast removed to not acknowledge recording error
            releaseRecorder();
            stopSelf();
        }
    }

    private void stopAudioRecording() {
        if (!isRecording) return;

        try {
            if (mediaRecorder != null) {
                mediaRecorder.stop();
                mediaRecorder.reset();
            }
            Logger.i("Audio recording saved: " + (currentOutputFile != null ? currentOutputFile.getName() : ""));
            // Toast removed to not acknowledge recording stop
        } catch (Exception e) {
            Logger.e("Error stopping audio recording", e);
        } finally {
            releaseRecorder();
            isRecording = false;
        }
    }

    private void releaseRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Notch Audio Recording",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Background Audio Recording Notification");
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) {
                nm.createNotificationChannel(channel);
            }
        }
    }

    private Notification buildNotification(String text) {
        Intent stopIntent = new Intent(this, AudioRecordingService.class);
        stopIntent.setAction(ACTION_STOP_AUDIO_REC);
        PendingIntent pStop = PendingIntent.getService(this, 3, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("zzzznotch - Audio Recording")
                .setContentText(text)
                .setSmallIcon(R.mipmap.ic_launcher)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop & Save", pStop)
                .setOngoing(true)
                .build();
    }

    @Override
    public void onDestroy() {
        stopAudioRecording();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
