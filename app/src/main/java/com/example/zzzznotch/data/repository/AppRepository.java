package com.example.zzzznotch.data.repository;

import android.content.Context;

import com.example.zzzznotch.data.database.AppDatabase;
import com.example.zzzznotch.data.database.NoteDao;
import com.example.zzzznotch.data.database.RuleDao;
import com.example.zzzznotch.data.models.GestureConfig;
import com.example.zzzznotch.data.models.GestureType;
import com.example.zzzznotch.data.models.Note;
import com.example.zzzznotch.data.models.NotchAppearance;
import com.example.zzzznotch.data.models.RuleConfig;
import com.example.zzzznotch.data.models.WidgetConfig;
import com.example.zzzznotch.data.preferences.AppPreferences;

import java.util.ArrayList;
import java.util.List;

public class AppRepository {

    private static volatile AppRepository INSTANCE;
    private final AppPreferences preferences;
    private final NoteDao noteDao;
    private final RuleDao ruleDao;

    private AppRepository(Context context) {
        Context appContext = context.getApplicationContext();
        this.preferences = AppPreferences.getInstance(appContext);
        AppDatabase db = AppDatabase.getInstance(appContext);
        this.noteDao = db.noteDao();
        this.ruleDao = db.ruleDao();
    }

    public static AppRepository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AppRepository(context);
                }
            }
        }
        return INSTANCE;
    }

    public AppPreferences getPreferences() {
        return preferences;
    }

    public List<GestureConfig> getAllGestureConfigs() {
        List<GestureConfig> list = new ArrayList<>();
        for (GestureType type : GestureType.values()) {
            list.add(preferences.getGestureConfig(type));
        }
        return list;
    }

    public GestureConfig getGestureConfig(GestureType type) {
        return preferences.getGestureConfig(type);
    }

    public void saveGestureConfig(GestureConfig config) {
        preferences.saveGestureConfig(config);
    }

    public NotchAppearance getNotchAppearance() {
        return preferences.getNotchAppearance();
    }

    public void saveNotchAppearance(NotchAppearance appearance) {
        preferences.saveNotchAppearance(appearance);
    }

    public WidgetConfig getWidgetConfig() {
        return preferences.getWidgetConfig();
    }

    public void saveWidgetConfig(WidgetConfig config) {
        preferences.saveWidgetConfig(config);
    }

    // Notes
    public List<Note> getAllNotes() {
        return noteDao.getAllNotes();
    }

    public Note getNoteById(long id) {
        return noteDao.getNoteById(id);
    }

    public long saveNote(Note note) {
        if (note.getId() > 0) {
            noteDao.updateNote(note);
            return note.getId();
        } else {
            return noteDao.insertNote(note);
        }
    }

    public void deleteNote(Note note) {
        if (note != null) {
            noteDao.deleteNote(note);
        }
    }

    // Rules
    public List<RuleConfig> getAllRules() {
        return ruleDao.getAllRules();
    }

    public List<RuleConfig> getEnabledRules() {
        return ruleDao.getEnabledRules();
    }

    public long saveRule(RuleConfig rule) {
        if (rule.getId() > 0) {
            ruleDao.updateRule(rule);
            return rule.getId();
        } else {
            return ruleDao.insertRule(rule);
        }
    }

    public void deleteRule(RuleConfig rule) {
        if (rule != null) {
            ruleDao.deleteRule(rule);
        }
    }
}
