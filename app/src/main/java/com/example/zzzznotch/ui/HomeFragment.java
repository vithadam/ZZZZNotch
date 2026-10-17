package com.example.zzzznotch.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.zzzznotch.R;
import com.example.zzzznotch.data.models.NotchAppearance;
import com.example.zzzznotch.data.preferences.AppPreferences;
import com.example.zzzznotch.service.NotchOverlayService;
import com.example.zzzznotch.utils.DisplayUtils;
import com.example.zzzznotch.utils.Logger;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.InputStream;

public class HomeFragment extends Fragment {

    private AppPreferences preferences;
    private SwitchMaterial switchMaster;
    private TextView tvStatusBadge;
    private TextView tvDesignName;

    private FrameLayout previewNotchContainer;
    private ImageView imgPreviewLogo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        preferences = AppPreferences.getInstance(requireContext());

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
        switchMaster = view.findViewById(R.id.switch_master);
        tvStatusBadge = view.findViewById(R.id.tv_status_badge);
        tvDesignName = view.findViewById(R.id.tv_design_name);

        previewNotchContainer = view.findViewById(R.id.preview_notch_container);
        imgPreviewLogo = view.findViewById(R.id.img_preview_logo);
    }

    private void setupListeners(View view) {
        switchMaster.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.setMasterEnabled(isChecked);
            updateUI();
            NotchOverlayService.updateServiceState(requireContext());
        });

        view.findViewById(R.id.btn_settings_shortcut).setOnClickListener(v -> {
            // Select Settings tab
            BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.nav_settings);
            }
        });

        view.findViewById(R.id.btn_change_design).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), NotchAppearanceActivity.class));
        });

        view.findViewById(R.id.btn_customize).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), NotchAppearanceActivity.class));
        });

        view.findViewById(R.id.row_touch_shortcuts).setOnClickListener(v -> {
            // Select Gestures tab
            BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.nav_gestures);
            }
        });

        view.findViewById(R.id.row_lock_screen_notch).setOnClickListener(v -> {
            // For now point to settings or somewhere relevant
            BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.nav_settings);
            }
        });
    }

    private void updateUI() {
        boolean isMasterEnabled = preferences.isMasterEnabled();
        switchMaster.setChecked(isMasterEnabled);

        if (isMasterEnabled) {
            tvStatusBadge.setText("ACTIVE");
            tvStatusBadge.setTextColor(requireContext().getColor(R.color.coral_red));
        } else {
            tvStatusBadge.setText("DISABLED");
            tvStatusBadge.setTextColor(requireContext().getColor(R.color.muted_grey_secondary));
        }

        updatePreview();
    }

    private void updatePreview() {
        NotchAppearance appearance = preferences.getNotchAppearance();

        int wPx = DisplayUtils.dpToPx(requireContext(), appearance.getWidthDp());
        int hPx = DisplayUtils.dpToPx(requireContext(), appearance.getHeightDp());

        ViewGroup.LayoutParams params = previewNotchContainer.getLayoutParams();
        if (params != null) {
            params.width = wPx;
            params.height = hPx;
            previewNotchContainer.setLayoutParams(params);
        }

        boolean hasLogo = appearance.getPhotoOverlayMode() != NotchAppearance.PhotoOverlayMode.DISABLED &&
                appearance.getPhotoUri() != null && !appearance.getPhotoUri().isEmpty();

        if (hasLogo) {
            tvDesignName.setText("Custom Image");
        } else {
            tvDesignName.setText(appearance.getShapeType().getDisplayName());
        }

        if (hasLogo) {
            try {
                Uri uri = Uri.parse(appearance.getPhotoUri());
                InputStream is = requireContext().getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                if (is != null) is.close();

                if (bitmap != null) {
                    imgPreviewLogo.setImageBitmap(bitmap);
                    imgPreviewLogo.setVisibility(View.VISIBLE);
                    int paddingPx = DisplayUtils.dpToPx(requireContext(), appearance.getImagePaddingDp());
                    imgPreviewLogo.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
                } else {
                    imgPreviewLogo.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                imgPreviewLogo.setVisibility(View.GONE);
            }
        } else {
            imgPreviewLogo.setVisibility(View.GONE);
        }
    }
}
