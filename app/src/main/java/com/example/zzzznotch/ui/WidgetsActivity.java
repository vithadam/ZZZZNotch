package com.example.zzzznotch.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zzzznotch.R;
import com.example.zzzznotch.data.models.WidgetConfig;
import com.example.zzzznotch.data.repository.AppRepository;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class WidgetsActivity extends AppCompatActivity {

    private AppRepository repository;
    private WidgetConfig config;

    private SwitchMaterial switchMedia;
    private SwitchMaterial switchVolume;
    private SwitchMaterial switchBrightness;
    private SwitchMaterial switchTimer;
    private SwitchMaterial switchStopwatch;
    private SwitchMaterial switchQuickTools;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widgets);

        repository = AppRepository.getInstance(this);
        config = repository.getWidgetConfig();

        initViews();
        loadValues();
    }

    private void initViews() {
        switchMedia = findViewById(R.id.switch_media);
        switchVolume = findViewById(R.id.switch_volume);
        switchBrightness = findViewById(R.id.switch_brightness);
        switchTimer = findViewById(R.id.switch_timer);
        switchStopwatch = findViewById(R.id.switch_stopwatch);
        switchQuickTools = findViewById(R.id.switch_quick_tools);

        Button btnSave = findViewById(R.id.btn_save_widgets);
        btnSave.setOnClickListener(v -> saveConfig());
    }

    private void loadValues() {
        switchMedia.setChecked(config.isShowMediaWidget());
        switchVolume.setChecked(config.isShowVolumeSlider());
        switchBrightness.setChecked(config.isShowBrightnessSlider());
        switchTimer.setChecked(config.isShowTimerWidget());
        switchStopwatch.setChecked(config.isShowStopwatchWidget());
        switchQuickTools.setChecked(config.isShowQuickTools());
    }

    private void saveConfig() {
        config.setShowMediaWidget(switchMedia.isChecked());
        config.setShowVolumeSlider(switchVolume.isChecked());
        config.setShowBrightnessSlider(switchBrightness.isChecked());
        config.setShowTimerWidget(switchTimer.isChecked());
        config.setShowStopwatchWidget(switchStopwatch.isChecked());
        config.setShowQuickTools(switchQuickTools.isChecked());

        repository.saveWidgetConfig(config);
        Toast.makeText(this, "Widget preferences saved!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
