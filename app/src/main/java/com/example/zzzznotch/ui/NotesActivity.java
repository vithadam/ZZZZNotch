package com.example.zzzznotch.ui;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zzzznotch.R;
import com.example.zzzznotch.data.models.Note;
import com.example.zzzznotch.data.repository.AppRepository;

import java.util.ArrayList;
import java.util.List;

public class NotesActivity extends AppCompatActivity {

    private AppRepository repository;
    private NoteAdapter adapter;
    private List<Note> notesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        repository = AppRepository.getInstance(this);

        RecyclerView rv = findViewById(R.id.rv_notes);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new NoteAdapter();
        rv.setAdapter(adapter);

        findViewById(R.id.btn_add_note).setOnClickListener(v -> showNoteEditorDialog(null));

        loadNotes();
    }

    private void loadNotes() {
        notesList = repository.getAllNotes();
        adapter.notifyDataSetChanged();
    }

    private void showNoteEditorDialog(Note existingNote) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(existingNote == null ? "New Quick Note" : "Edit Note");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        EditText etTitle = new EditText(this);
        etTitle.setHint("Title");
        if (existingNote != null) etTitle.setText(existingNote.getTitle());
        layout.addView(etTitle);

        EditText etContent = new EditText(this);
        etContent.setHint("Content / Details...");
        etContent.setMinLines(3);
        if (existingNote != null) etContent.setText(existingNote.getContent());
        layout.addView(etContent);

        builder.setView(layout);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String title = etTitle.getText().toString().trim();
            String content = etContent.getText().toString().trim();

            if (title.isEmpty() && content.isEmpty()) {
                Toast.makeText(this, "Empty note discarded", Toast.LENGTH_SHORT).show();
                return;
            }

            if (existingNote != null) {
                existingNote.setTitle(title);
                existingNote.setContent(content);
                existingNote.setTimestamp(System.currentTimeMillis());
                repository.saveNote(existingNote);
            } else {
                Note note = new Note(title, content, System.currentTimeMillis());
                repository.saveNote(note);
            }

            loadNotes();
            Toast.makeText(this, "Note saved!", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private class NoteAdapter extends RecyclerView.Adapter<NoteViewHolder> {

        @NonNull
        @Override
        public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
            return new NoteViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
            Note note = notesList.get(position);
            holder.tvTitle.setText(note.getTitle().isEmpty() ? "Untitled Note" : note.getTitle());
            holder.tvContent.setText(note.getContent());

            CharSequence dateStr = DateFormat.format("MMM dd, yyyy  hh:mm a", note.getTimestamp());
            holder.tvDate.setText(dateStr);

            holder.itemView.setOnClickListener(v -> showNoteEditorDialog(note));

            holder.btnDelete.setOnClickListener(v -> {
                repository.deleteNote(note);
                loadNotes();
                Toast.makeText(NotesActivity.this, "Note deleted", Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public int getItemCount() {
            return notesList != null ? notesList.size() : 0;
        }
    }

    private static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvContent;
        TextView tvDate;
        ImageButton btnDelete;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_note_title);
            tvContent = itemView.findViewById(R.id.tv_note_content);
            tvDate = itemView.findViewById(R.id.tv_note_date);
            btnDelete = itemView.findViewById(R.id.btn_delete_note);
        }
    }
}
