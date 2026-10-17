package com.example.zzzznotch.backup;

import android.content.Context;

import com.example.zzzznotch.data.models.GestureConfig;
import com.example.zzzznotch.data.models.Note;
import com.example.zzzznotch.data.models.NotchAppearance;
import com.example.zzzznotch.data.models.RuleConfig;
import com.example.zzzznotch.data.models.WidgetConfig;
import com.example.zzzznotch.data.repository.AppRepository;
import com.example.zzzznotch.service.NotchOverlayService;
import com.example.zzzznotch.utils.Logger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

public class BackupManager {

    public static class BackupPayload {
        public int version = 1;
        public long timestamp = System.currentTimeMillis();
        public List<GestureConfig> gestureConfigs;
        public NotchAppearance notchAppearance;
        public WidgetConfig widgetConfig;
        public List<RuleConfig> rules;
        public List<Note> notes;
    }

    public static String createBackupJson(Context context) {
        try {
            AppRepository repo = AppRepository.getInstance(context);
            BackupPayload payload = new BackupPayload();
            payload.gestureConfigs = repo.getAllGestureConfigs();
            payload.notchAppearance = repo.getNotchAppearance();
            payload.widgetConfig = repo.getWidgetConfig();
            payload.rules = repo.getAllRules();
            payload.notes = repo.getAllNotes();

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            return gson.toJson(payload);
        } catch (Exception e) {
            Logger.e("Failed to create backup JSON", e);
            return null;
        }
    }

    public static boolean restoreBackupJson(Context context, String json) {
        if (json == null || json.trim().isEmpty()) return false;

        try {
            Gson gson = new Gson();
            BackupPayload payload = gson.fromJson(json, BackupPayload.class);
            if (payload == null) return false;

            AppRepository repo = AppRepository.getInstance(context);

            if (payload.gestureConfigs != null) {
                for (GestureConfig gc : payload.gestureConfigs) {
                    repo.saveGestureConfig(gc);
                }
            }

            if (payload.notchAppearance != null) {
                repo.saveNotchAppearance(payload.notchAppearance);
            }

            if (payload.widgetConfig != null) {
                repo.saveWidgetConfig(payload.widgetConfig);
            }

            if (payload.rules != null) {
                for (RuleConfig r : payload.rules) {
                    repo.saveRule(r);
                }
            }

            if (payload.notes != null) {
                for (Note n : payload.notes) {
                    repo.saveNote(n);
                }
            }

            NotchOverlayService.updateServiceState(context);
            Logger.i("Backup restored successfully!");
            return true;

        } catch (Exception e) {
            Logger.e("Failed to restore backup JSON", e);
            return false;
        }
    }
}
