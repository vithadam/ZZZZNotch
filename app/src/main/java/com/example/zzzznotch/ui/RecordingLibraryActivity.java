package com.example.zzzznotch.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zzzznotch.R;
import com.example.zzzznotch.recording.RecordingManager;
import com.example.zzzznotch.utils.Logger;

import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class RecordingLibraryActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_EXPORT = 7001;

    private RecordingManager recordingManager;
    private RecordingAdapter adapter;
    private List<File> recordingFiles = new ArrayList<>();
    private TextView tvStorageInfo;

    private File fileToExport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording_library);

        recordingManager = RecordingManager.getInstance();

        tvStorageInfo = findViewById(R.id.tv_storage_info);
        RecyclerView rv = findViewById(R.id.rv_recordings);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new RecordingAdapter();
        rv.setAdapter(adapter);

        findViewById(R.id.btn_delete_all).setOnClickListener(v -> confirmDeleteAll());

        loadRecordings();
    }

    private void loadRecordings() {
        recordingFiles = recordingManager.getAllRecordings(this);
        long availableMb = recordingManager.getAvailableStorageMb(this);
        tvStorageInfo.setText("Storage Available: " + availableMb + " MB (" + recordingFiles.size() + " recordings)");
        adapter.notifyDataSetChanged();
    }

    private void confirmDeleteAll() {
        if (recordingFiles.isEmpty()) return;
        new AlertDialog.Builder(this)
                .setTitle("Delete All Recordings")
                .setMessage("Are you sure you want to delete all temporary recordings?")
                .setPositiveButton("Delete All", (dialog, which) -> {
                    recordingManager.deleteAllRecordings(this);
                    loadRecordings();
                    Toast.makeText(this, "All recordings deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void startExport(File file) {
        this.fileToExport = file;
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        if (file.getName().endsWith(".mp4")) {
            intent.setType("video/mp4");
        } else {
            intent.setType("audio/mp4");
        }
        intent.putExtra(Intent.EXTRA_TITLE, file.getName());
        startActivityForResult(intent, REQUEST_CODE_EXPORT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_EXPORT && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri destUri = data.getData();
            if (fileToExport != null) {
                try (OutputStream os = getContentResolver().openOutputStream(destUri)) {
                    if (os != null) {
                        boolean success = recordingManager.exportRecordingToUri(this, fileToExport, os);
                        if (success) {
                            Toast.makeText(this, "Recording exported successfully!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Export failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (Exception e) {
                    Logger.e("Export failed", e);
                    Toast.makeText(this, "Export error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void playRecording(File file) {
        try {
            Uri fileUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            if (file.getName().endsWith(".mp4")) {
                intent.setDataAndType(fileUri, "video/mp4");
            } else {
                intent.setDataAndType(fileUri, "audio/*");
            }
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (Exception e) {
            Logger.e("Failed to play recording", e);
            Toast.makeText(this, "No media player found to play this file", Toast.LENGTH_SHORT).show();
        }
    }

    private class RecordingAdapter extends RecyclerView.Adapter<RecordingViewHolder> {

        @NonNull
        @Override
        public RecordingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recording_file, parent, false);
            return new RecordingViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecordingViewHolder holder, int position) {
            File file = recordingFiles.get(position);
            holder.tvFileName.setText(file.getName());

            boolean isVideo = file.getName().endsWith(".mp4");
            holder.imgIcon.setImageResource(isVideo ? android.R.drawable.ic_menu_camera : android.R.drawable.ic_btn_speak_now);

            String sizeStr = Formatter.formatShortFileSize(RecordingLibraryActivity.this, file.length());
            CharSequence dateStr = DateFormat.format("MMM dd, yyyy  hh:mm a", file.lastModified());
            holder.tvFileDetails.setText(sizeStr + " • " + dateStr);

            holder.itemView.setOnClickListener(v -> playRecording(file));
            holder.btnExport.setOnClickListener(v -> startExport(file));
            holder.btnDelete.setOnClickListener(v -> {
                recordingManager.deleteRecording(file);
                loadRecordings();
                Toast.makeText(RecordingLibraryActivity.this, "Deleted " + file.getName(), Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public int getItemCount() {
            return recordingFiles != null ? recordingFiles.size() : 0;
        }
    }

    private static class RecordingViewHolder extends RecyclerView.ViewHolder {
        ImageView imgIcon;
        TextView tvFileName;
        TextView tvFileDetails;
        Button btnExport;
        ImageButton btnDelete;

        public RecordingViewHolder(@NonNull View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.img_type_icon);
            tvFileName = itemView.findViewById(R.id.tv_file_name);
            tvFileDetails = itemView.findViewById(R.id.tv_file_details);
            btnExport = itemView.findViewById(R.id.btn_export_file);
            btnDelete = itemView.findViewById(R.id.btn_delete_file);
        }
    }
}
