package com.example.zzzznotch.data.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.zzzznotch.data.models.Note;

import java.util.List;

@Dao
public interface NoteDao {

    @Query("SELECT * FROM notes ORDER BY timestamp DESC")
    List<Note> getAllNotes();

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    Note getNoteById(long id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertNote(Note note);

    @Update
    int updateNote(Note note);

    @Delete
    int deleteNote(Note note);

    @Query("DELETE FROM notes WHERE id = :id")
    void deleteNoteById(long id);
}
