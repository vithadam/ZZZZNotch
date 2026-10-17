package com.example.zzzznotch.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.zzzznotch.R;
import com.example.zzzznotch.data.models.NotchAppearance;
import com.example.zzzznotch.data.repository.AppRepository;
import com.example.zzzznotch.service.NotchOverlayService;
import com.example.zzzznotch.utils.DisplayUtils;
import com.example.zzzznotch.utils.Logger;

import java.io.InputStream;

public class NotchAppearanceActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PICK_IMAGE = 3001;

    private AppRepository repository;
    private NotchAppearance appearance;

    private FrameLayout previewNotchContainer;
    private ImageView imgPreviewLogo;

    private TextView tvWidthLabel;
    private SeekBar seekWidth;
    private TextView tvHeightLabel;
    private SeekBar seekHeight;
    private TextView tvOffsetYLabel;
    private SeekBar seekOffsetY;

    private Spinner spinnerPhotoMode;
    private TextView tvLogoPaddingLabel;
    private SeekBar seekLogoPadding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notch_appearance);

        repository = AppRepository.getInstance(this);
        appearance = repository.getNotchAppearance();

        initViews();
        setupControls();
        updatePreview();
    }

    private void initViews() {
        previewNotchContainer = findViewById(R.id.preview_notch_container);
        imgPreviewLogo = findViewById(R.id.img_preview_logo);

        tvWidthLabel = findViewById(R.id.tv_width_label);
        seekWidth = findViewById(R.id.seek_width);

        tvHeightLabel = findViewById(R.id.tv_height_label);
        seekHeight = findViewById(R.id.seek_height);

        tvOffsetYLabel = findViewById(R.id.tv_offset_y_label);
        seekOffsetY = findViewById(R.id.seek_offset_y);

        spinnerPhotoMode = findViewById(R.id.spinner_photo_mode);
        tvLogoPaddingLabel = findViewById(R.id.tv_logo_padding_label);
        seekLogoPadding = findViewById(R.id.seek_logo_padding);

        Button btnPickPhoto = findViewById(R.id.btn_pick_photo);
        Button btnClearPhoto = findViewById(R.id.btn_clear_photo);
        Button btnSave = findViewById(R.id.btn_save_appearance);

        btnPickPhoto.setOnClickListener(v -> pickPhoto());
        btnClearPhoto.setOnClickListener(v -> clearPhoto());
        btnSave.setOnClickListener(v -> saveAppearance());
    }

    private void setupControls() {
        seekWidth.setProgress(appearance.getWidthDp());
        seekHeight.setProgress(appearance.getHeightDp());
        seekOffsetY.setProgress(appearance.getOffsetYDp());
        seekLogoPadding.setProgress(appearance.getImagePaddingDp());

        tvWidthLabel.setText(getString(R.string.width_label, appearance.getWidthDp()));
        tvHeightLabel.setText(getString(R.string.height_label, appearance.getHeightDp()));
        tvOffsetYLabel.setText(getString(R.string.offset_y_label, appearance.getOffsetYDp()));
        tvLogoPaddingLabel.setText(getString(R.string.logo_padding_label, appearance.getImagePaddingDp()));

        seekWidth.setOnSeekBarChangeListener(new SimpleSeekBarListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int w = Math.max(40, progress);
                appearance.setWidthDp(w);
                tvWidthLabel.setText(getString(R.string.width_label, w));
                updatePreview();
            }
        });

        seekHeight.setOnSeekBarChangeListener(new SimpleSeekBarListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int h = Math.max(20, progress);
                appearance.setHeightDp(h);
                tvHeightLabel.setText(getString(R.string.height_label, h));
                updatePreview();
            }
        });

        seekOffsetY.setOnSeekBarChangeListener(new SimpleSeekBarListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                appearance.setOffsetYDp(progress);
                tvOffsetYLabel.setText(getString(R.string.offset_y_label, progress));
                updatePreview();
            }
        });

        seekLogoPadding.setOnSeekBarChangeListener(new SimpleSeekBarListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                appearance.setImagePaddingDp(progress);
                tvLogoPaddingLabel.setText(getString(R.string.logo_padding_label, progress));
                updatePreview();
            }
        });

        String[] modes = new String[]{
                NotchAppearance.PhotoOverlayMode.ALWAYS_VISIBLE.getDisplayName(),
                NotchAppearance.PhotoOverlayMode.VISIBLE_ON_TOUCH.getDisplayName(),
                NotchAppearance.PhotoOverlayMode.DISABLED.getDisplayName()
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, modes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPhotoMode.setAdapter(adapter);

        int selectedIndex = appearance.getPhotoOverlayMode().ordinal();
        spinnerPhotoMode.setSelection(selectedIndex);

        spinnerPhotoMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                appearance.setPhotoOverlayMode(NotchAppearance.PhotoOverlayMode.values()[position]);
                updatePreview();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void updatePreview() {
        int wPx = DisplayUtils.dpToPx(this, appearance.getWidthDp());
        int hPx = DisplayUtils.dpToPx(this, appearance.getHeightDp());
        int offsetYPx = DisplayUtils.dpToPx(this, appearance.getOffsetYDp());

        ViewGroup.LayoutParams params = previewNotchContainer.getLayoutParams();
        if (params != null) {
            params.width = wPx;
            params.height = hPx;
            previewNotchContainer.setLayoutParams(params);
        }
        previewNotchContainer.setTranslationY(offsetYPx);

        boolean hasLogo = appearance.getPhotoOverlayMode() != NotchAppearance.PhotoOverlayMode.DISABLED &&
                appearance.getPhotoUri() != null && !appearance.getPhotoUri().isEmpty();

        GradientDrawable drawable = new GradientDrawable();
        switch (appearance.getShapeType()) {
            case OVAL:
                drawable.setShape(GradientDrawable.OVAL);
                break;
            case PILL:
                drawable.setShape(GradientDrawable.RECTANGLE);
                drawable.setCornerRadius(DisplayUtils.dpToPx(this, appearance.getHeightDp() / 2f));
                break;
            case ROUNDED_RECTANGLE:
            default:
                drawable.setShape(GradientDrawable.RECTANGLE);
                drawable.setCornerRadius(DisplayUtils.dpToPx(this, appearance.getCornerRadiusDp()));
                break;
        }

        if (hasLogo) {
            drawable.setColor(Color.TRANSPARENT);
        } else {
            drawable.setColor(appearance.getBackgroundColor());
        }
        previewNotchContainer.setBackground(drawable);

        if (hasLogo) {
            try {
                Uri uri = Uri.parse(appearance.getPhotoUri());
                InputStream is = getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                if (is != null) is.close();

                if (bitmap != null) {
                    imgPreviewLogo.setImageBitmap(bitmap);
                    imgPreviewLogo.setVisibility(View.VISIBLE);
                    int paddingPx = DisplayUtils.dpToPx(this, appearance.getImagePaddingDp());
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

    private void pickPhoto() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    private void clearPhoto() {
        appearance.setPhotoUri("");
        updatePreview();
        Toast.makeText(this, "Photo removed", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedUri = data.getData();
            try {
                getContentResolver().takePersistableUriPermission(
                        selectedUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                );
            } catch (Exception e) {
                Logger.e("Failed to take persistable URI permission", e);
            }
            appearance.setPhotoUri(selectedUri.toString());
            if (appearance.getPhotoOverlayMode() == NotchAppearance.PhotoOverlayMode.DISABLED) {
                appearance.setPhotoOverlayMode(NotchAppearance.PhotoOverlayMode.ALWAYS_VISIBLE);
                spinnerPhotoMode.setSelection(0);
            }
            updatePreview();
            Toast.makeText(this, "Photo selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveAppearance() {
        repository.saveNotchAppearance(appearance);
        NotchOverlayService.updateServiceState(this);
        Toast.makeText(this, "Appearance saved!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private abstract static class SimpleSeekBarListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {}
    }
}
