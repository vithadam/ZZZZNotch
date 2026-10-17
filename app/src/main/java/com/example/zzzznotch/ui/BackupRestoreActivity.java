package com.example.zzzznotch.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.zzzznotch.R;
import com.example.zzzznotch.backup.BackupManager;
import com.example.zzzznotch.utils.Logger;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class BackupRestoreActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_CREATE_BACKUP = 8001;
    private static final int REQUEST_CODE_RESTORE_BACKUP = 8002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup_restore);

        Button btnExport = findViewById(R.id.btn_export_backup);
        Button btnImport = findViewById(R.id.btn_import_backup);

        btnExport.setOnClickListener(v -> createBackupFile());
        btnImport.setOnClickListener(v -> selectBackupFileToRestore());
    }

    private void createBackupFile() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, "ActionNotch_Backup.json");
        startActivityForResult(intent, REQUEST_CODE_CREATE_BACKUP);
    }

    private void selectBackupFileToRestore() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, REQUEST_CODE_RESTORE_BACKUP);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri fileUri = data.getData();

            if (requestCode == REQUEST_CODE_CREATE_BACKUP) {
                exportBackup(fileUri);
            } else if (requestCode == REQUEST_CODE_RESTORE_BACKUP) {
                confirmAndRestoreBackup(fileUri);
            }
        }
    }

    private void exportBackup(Uri uri) {
        try {
            String json = BackupManager.createBackupJson(this);
            if (json != null) {
                try (OutputStream os = getContentResolver().openOutputStream(uri)) {
                    if (os != null) {
                        os.write(json.getBytes(StandardCharsets.UTF_8));
                        os.flush();
                        Toast.makeText(this, "Backup exported successfully!", Toast.LENGTH_LONG).show();
                    }
                }
            } else {
                Toast.makeText(this, "Failed to generate backup data", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Logger.e("Backup export error", e);
            Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmAndRestoreBackup(Uri uri) {
        new AlertDialog.Builder(this)
                .setTitle("Restore Settings")
                .setMessage("Are you sure you want to restore settings from this backup? Current configuration will be overwritten.")
                .setPositiveButton("Restore Overwrite", (dialog, which) -> restoreBackup(uri))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void restoreBackup(Uri uri) {
        try (InputStream is = getContentResolver().openInputStream(uri)) {
            if (is != null) {
                byte[] bytes = new byte[is.available()];
                int read = is.read(bytes);
                String json = new String(bytes, 0, Math.max(0, read), StandardCharsets.UTF_8);

                boolean success = BackupManager.restoreBackupJson(this, json);
                if (success) {
                    Toast.makeText(this, "Settings restored successfully!", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(this, "Invalid or corrupted backup file", Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            Logger.e("Backup restore error", e);
            Toast.makeText(this, "Restore failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
