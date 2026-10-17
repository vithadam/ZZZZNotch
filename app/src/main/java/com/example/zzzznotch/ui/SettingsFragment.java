package com.example.zzzznotch.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.zzzznotch.R;
import com.example.zzzznotch.utils.PermissionUtils;

public class SettingsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        setupListeners(view);
        return view;
    }

    private void setupListeners(View view) {
        view.findViewById(R.id.btn_battery_optimization).setOnClickListener(v -> {
            PermissionUtils.openAppInfoSettings(requireContext());
        });

        view.findViewById(R.id.btn_permissions_status).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), PermissionsActivity.class));
        });

        view.findViewById(R.id.btn_recording_settings).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), RecordingSettingsActivity.class));
        });

        view.findViewById(R.id.btn_recording_library).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), RecordingLibraryActivity.class));
        });

        view.findViewById(R.id.btn_backup_restore).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), BackupRestoreActivity.class));
        });

        view.findViewById(R.id.btn_diagnostics).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), AboutDiagnosticActivity.class));
        });

        view.findViewById(R.id.btn_about).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), AboutDiagnosticActivity.class));
        });
    }
}
