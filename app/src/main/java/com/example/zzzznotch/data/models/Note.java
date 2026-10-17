package com.example.zzzznotch.data.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notes")
public class Note {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private String title;
    private String content;
    private long timestamp;

    public Note() {
        this.title = "";
        this.content = "";
        this.timestamp = System.currentTimeMillis();
    }

    public Note(String title, String content, long timestamp) {
        this.title = title != null ? title : "";
        this.content = content != null ? content : "";
        this.timestamp = timestamp > 0 ? timestamp : System.currentTimeMillis();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title != null ? title : "";
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content != null ? content : "";
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
