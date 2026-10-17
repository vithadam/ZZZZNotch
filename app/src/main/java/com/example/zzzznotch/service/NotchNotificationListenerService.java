package com.example.zzzznotch.service;

import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import com.example.zzzznotch.rules.RuleEngine;
import com.example.zzzznotch.utils.Logger;

import java.util.List;

public class NotchNotificationListenerService extends NotificationListenerService {

    private static volatile NotchNotificationListenerService instance;

    public static NotchNotificationListenerService getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Logger.i("NotchNotificationListenerService created");
        checkActiveMediaSessions();
    }

    @Override
    public void onDestroy() {
        instance = null;
        super.onDestroy();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        checkActiveMediaSessions();
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        checkActiveMediaSessions();
    }

    public void checkActiveMediaSessions() {
        try {
            MediaSessionManager msm = (MediaSessionManager) getSystemService(MEDIA_SESSION_SERVICE);
            if (msm != null) {
                List<MediaController> controllers = msm.getActiveSessions(null);
                boolean anyPlaying = false;
                for (MediaController controller : controllers) {
                    PlaybackState state = controller.getPlaybackState();
                    if (state != null && state.getState() == PlaybackState.STATE_PLAYING) {
                        anyPlaying = true;
                        MediaMetadata metadata = controller.getMetadata();
                        if (metadata != null) {
                            String title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE);
                            String artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST);
                            Logger.d("Active Media: " + title + " - " + artist);
                        }
                        break;
                    }
                }
                RuleEngine.setMediaPlaying(anyPlaying);
            }
        } catch (Exception e) {
            Logger.e("Error checking media sessions", e);
        }
    }
}
