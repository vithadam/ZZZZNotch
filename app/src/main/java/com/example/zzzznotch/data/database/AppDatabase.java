package com.example.zzzznotch.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.zzzznotch.data.models.Note;
import com.example.zzzznotch.data.models.RuleConfig;

@Database(entities = {Note.class, RuleConfig.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    private static final String DB_NAME = "zzzznotch_db";
    private static volatile AppDatabase INSTANCE;

    public abstract NoteDao noteDao();
    public abstract RuleDao ruleDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    DB_NAME
                            )
                            .fallbackToDestructiveMigration()
                            .allowMainThreadQueries() // Allowed for small local dataset operations
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
