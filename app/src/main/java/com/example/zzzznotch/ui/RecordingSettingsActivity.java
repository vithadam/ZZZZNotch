package com.example.zzzznotch.ui;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zzzznotch.R;
import com.example.zzzznotch.data.preferences.AppPreferences;

public class RecordingSettingsActivity extends AppCompatActivity {

    private AppPreferences preferences;

    private Spinner spinnerResolution;
    private Spinner spinnerFps;
    private Spinner spinnerAudio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording_settings);

        preferences = AppPreferences.getInstance(this);

        initViews();
        setupSpinners();
    }

    private void initViews() {
        spinnerResolution = findViewById(R.id.spinner_resolution);
        spinnerFps = findViewById(R.id.spinner_fps);
        spinnerAudio = findViewById(R.id.spinner_audio);

        Button btnSave = findViewById(R.id.btn_save_recording_settings);
        btnSave.setOnClickListener(v -> saveSettings());
    }

    private void setupSpinners() {
        String[] resolutions = new String[]{"4K UHD (2160p)", "1080p Full HD", "720p HD", "480p SD"};
        ArrayAdapter<String> resAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, resolutions);
        resAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerResolution.setAdapter(resAdapter);

        String[] fpsList = new String[]{"60 FPS", "30 FPS", "24 FPS"};
        ArrayAdapter<String> fpsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fpsList);
        fpsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFps.setAdapter(fpsAdapter);

        String[] audioQualities = new String[]{"High (192 kbps)", "Medium (128 kbps)", "Low (64 kbps)"};
        ArrayAdapter<String> audioAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, audioQualities);
        audioAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAudio.setAdapter(audioAdapter);
    }

    private void saveSettings() {
        preferences.setRecordingResolution(spinnerResolution.getSelectedItem().toString());
        preferences.setRecordingFps(spinnerFps.getSelectedItemPosition() == 0 ? 60 : 30);
        preferences.setRecordingAudioQuality(spinnerAudio.getSelectedItem().toString());

        Toast.makeText(this, "Recording foundation preferences saved!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
