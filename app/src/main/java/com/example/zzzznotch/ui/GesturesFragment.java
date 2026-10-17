package com.example.zzzznotch.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zzzznotch.R;
import com.example.zzzznotch.data.models.ActionType;
import com.example.zzzznotch.data.models.GestureConfig;
import com.example.zzzznotch.data.preferences.AppPreferences;
import com.example.zzzznotch.data.repository.AppRepository;
import com.example.zzzznotch.service.NotchOverlayService;
import com.example.zzzznotch.utils.DistractionFreeManager;
import com.example.zzzznotch.utils.PermissionUtils;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.List;

public class GesturesFragment extends Fragment {

    private static final int REQUEST_CODE_APP_PICKER = 2001;

    private AppRepository repository;
    private AppPreferences preferences;

    private SwitchMaterial switchGesturesMaster;
    private SwitchMaterial switchGamingMode;
    private LinearLayout cardAccessibilityWarning;
    private Button btnGrantAccessibility;
    private RecyclerView rvGestures;

    private GestureAdapter adapter;
    private List<GestureConfig> gestureConfigs;
    private GestureConfig pendingAppPickerConfig;

    private TextView tvWindowLabel;
    private SeekBar seekWindow;
    private TextView tvToleranceLabel;
    private SeekBar seekTolerance;

    private SwitchMaterial switchShowTouchArea;
    private SwitchMaterial switchHapticFeedback;
    private SwitchMaterial switchDisableLandscape;
    private SwitchMaterial switchDisableCalls;
    private SwitchMaterial switchDisableLockscreen;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gestures, container, false);
        repository = AppRepository.getInstance(requireContext());
        preferences = repository.getPreferences();

        initViews(view);
        setupListeners(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    private void initViews(View view) {
        switchGesturesMaster = view.findViewById(R.id.switch_gestures_master);
        switchGamingMode = view.findViewById(R.id.switch_gaming_mode);
        cardAccessibilityWarning = view.findViewById(R.id.card_accessibility_warning);
        btnGrantAccessibility = view.findViewById(R.id.btn_grant_accessibility);
        rvGestures = view.findViewById(R.id.rv_gestures);

        rvGestures.setLayoutManager(new LinearLayoutManager(requireContext()));

        tvWindowLabel = view.findViewById(R.id.tv_window_label);
        seekWindow = view.findViewById(R.id.seek_window);
        tvToleranceLabel = view.findViewById(R.id.tv_tolerance_label);
        seekTolerance = view.findViewById(R.id.seek_tolerance);

        switchShowTouchArea = view.findViewById(R.id.switch_show_touch_area);
        switchHapticFeedback = view.findViewById(R.id.switch_haptic_feedback);
        switchDisableLandscape = view.findViewById(R.id.switch_disable_landscape);
        switchDisableCalls = view.findViewById(R.id.switch_disable_calls);
        switchDisableLockscreen = view.findViewById(R.id.switch_disable_lockscreen);
    }

    private void setupListeners(View view) {
        switchGesturesMaster.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.setMasterEnabled(isChecked);
            NotchOverlayService.updateServiceState(requireContext());
        });

        switchGamingMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            DistractionFreeManager.getInstance().setManualModeActive(isChecked, requireContext());
        });

        btnGrantAccessibility.setOnClickListener(v -> {
            PermissionUtils.openAccessibilitySettings(requireContext());
        });

        view.findViewById(R.id.row_blacklisted_apps).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), RulesActivity.class));
        });

        switchShowTouchArea.setOnCheckedChangeListener((btn, isChecked) -> {
            preferences.setShowTouchAreaEnabled(isChecked);
            NotchOverlayService.updateServiceState(requireContext());
        });

        switchHapticFeedback.setOnCheckedChangeListener((btn, isChecked) -> {
            preferences.setHapticFeedbackEnabled(isChecked);
        });

        switchDisableLandscape.setOnCheckedChangeListener((btn, isChecked) -> {
            preferences.setDisableInLandscapeEnabled(isChecked);
            NotchOverlayService.updateServiceState(requireContext());
        });

        switchDisableCalls.setOnCheckedChangeListener((btn, isChecked) -> {
            preferences.setDisableDuringCallEnabled(isChecked);
        });

        switchDisableLockscreen.setOnCheckedChangeListener((btn, isChecked) -> {
            preferences.setDisableOnLockScreenEnabled(isChecked);
        });

        seekWindow.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int windowMs = Math.max(150, progress);
                tvWindowLabel.setText(getString(R.string.window_label, windowMs));
                if (fromUser) {
                    preferences.setRecognitionWindowMs(windowMs);
                    NotchOverlayService.updateServiceState(requireContext());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        seekTolerance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int tolDp = Math.max(10, progress);
                tvToleranceLabel.setText(getString(R.string.tolerance_label, tolDp));
                if (fromUser) {
                    preferences.setMovementToleranceDp(tolDp);
                    NotchOverlayService.updateServiceState(requireContext());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void updateUI() {
        switchGesturesMaster.setChecked(preferences.isMasterEnabled());
        switchGamingMode.setChecked(DistractionFreeManager.getInstance().isDistractionFreeActive());

        if (PermissionUtils.isAccessibilityServiceEnabled(requireContext())) {
            cardAccessibilityWarning.setVisibility(View.GONE);
        } else {
            cardAccessibilityWarning.setVisibility(View.VISIBLE);
        }

        int curWindow = preferences.getRecognitionWindowMs();
        int curTol = preferences.getMovementToleranceDp();
        seekWindow.setProgress(curWindow);
        seekTolerance.setProgress(curTol);
        tvWindowLabel.setText(getString(R.string.window_label, curWindow));
        tvToleranceLabel.setText(getString(R.string.tolerance_label, curTol));

        switchShowTouchArea.setChecked(preferences.isShowTouchAreaEnabled());
        switchHapticFeedback.setChecked(preferences.isHapticFeedbackEnabled());
        switchDisableLandscape.setChecked(preferences.isDisableInLandscapeEnabled());
        switchDisableCalls.setChecked(preferences.isDisableDuringCallEnabled());
        switchDisableLockscreen.setChecked(preferences.isDisableOnLockScreenEnabled());

        gestureConfigs = repository.getAllGestureConfigs();
        if (adapter == null) {
            adapter = new GestureAdapter(gestureConfigs, this::showActionPickerDialog);
            rvGestures.setAdapter(adapter);
        } else {
            adapter.list.clear();
            adapter.list.addAll(gestureConfigs);
            adapter.notifyDataSetChanged();
        }
    }

    private void showActionPickerDialog(GestureConfig config) {
        ActionType[] actions = ActionType.values();
        String[] items = new String[actions.length];
        for (int i = 0; i < actions.length; i++) {
            items[i] = actions[i].getDisplayName();
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Select Action for " + config.getGestureType().getDisplayName())
                .setItems(items, (dialog, which) -> {
                    ActionType selectedAction = actions[which];
                    if (selectedAction == ActionType.LAUNCH_APP) {
                        pendingAppPickerConfig = config;
                        Intent intent = new Intent(requireContext(), AppPickerActivity.class);
                        startActivityForResult(intent, REQUEST_CODE_APP_PICKER);
                    } else {
                        config.setActionType(selectedAction);
                        config.setTargetPackage("");
                        repository.saveGestureConfig(config);
                        adapter.notifyDataSetChanged();
                        NotchOverlayService.updateServiceState(requireContext());
                        Toast.makeText(requireContext(), "Updated " + config.getGestureType().getDisplayName() + " action", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_APP_PICKER && resultCode == Activity.RESULT_OK && data != null) {
            String pkg = data.getStringExtra("selected_package");
            if (pkg != null && pendingAppPickerConfig != null) {
                pendingAppPickerConfig.setActionType(ActionType.LAUNCH_APP);
                pendingAppPickerConfig.setTargetPackage(pkg);
                repository.saveGestureConfig(pendingAppPickerConfig);
                adapter.notifyDataSetChanged();
                NotchOverlayService.updateServiceState(requireContext());
                Toast.makeText(requireContext(), "App assigned to " + pendingAppPickerConfig.getGestureType().getDisplayName(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private interface OnGestureClickListener {
        void onClick(GestureConfig config);
    }

    private static class LocalGestureViewHolder extends RecyclerView.ViewHolder {
        android.widget.TextView tvGestureTitle;
        android.widget.TextView tvActionTitle;

        public LocalGestureViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGestureTitle = itemView.findViewById(R.id.tv_gesture_title);
            tvActionTitle = itemView.findViewById(R.id.tv_action_title);
        }
    }

    private static class GestureAdapter extends RecyclerView.Adapter<LocalGestureViewHolder> {
        private final List<GestureConfig> list;
        private final OnGestureClickListener listener;

        public GestureAdapter(List<GestureConfig> list, OnGestureClickListener listener) {
            this.list = list;
            this.listener = listener;
        }

        @NonNull
        @Override
        public LocalGestureViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gesture_config, parent, false);
            return new LocalGestureViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull LocalGestureViewHolder holder, int position) {
            GestureConfig config = list.get(position);
            holder.tvGestureTitle.setText(config.getGestureType().getDisplayName());
            String actionText = "Action: " + config.getActionType().getDisplayName();
            if (config.getActionType() == ActionType.LAUNCH_APP && !config.getTargetPackage().isEmpty()) {
                actionText += " (" + config.getTargetPackage() + ")";
            }
            holder.tvActionTitle.setText(actionText);
            holder.itemView.setOnClickListener(v -> listener.onClick(config));
        }

        @Override
        public int getItemCount() {
            return list != null ? list.size() : 0;
        }
    }
}
