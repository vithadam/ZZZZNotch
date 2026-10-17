package com.example.zzzznotch.ui;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.zzzznotch.R;
import com.example.zzzznotch.utils.Logger;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraQrActivity extends AppCompatActivity {

    private static final int PERMISSION_CAMERA_REQUEST = 5001;

    private PreviewView previewView;
    private LinearLayout containerResult;
    private TextView tvQrResult;
    private Button btnCopy;
    private Button btnOpenLink;

    private ExecutorService cameraExecutor;
    private BarcodeScanner barcodeScanner;
    private String lastScannedCode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_qr);

        previewView = findViewById(R.id.preview_view);
        containerResult = findViewById(R.id.container_qr_result);
        tvQrResult = findViewById(R.id.tv_qr_result);
        btnCopy = findViewById(R.id.btn_copy_qr);
        btnOpenLink = findViewById(R.id.btn_open_qr_link);

        cameraExecutor = Executors.newSingleThreadExecutor();
        barcodeScanner = BarcodeScanning.getClient();

        btnCopy.setOnClickListener(v -> copyToClipboard());
        btnOpenLink.setOnClickListener(v -> openLink());

        if (checkCameraPermission()) {
            startCamera();
        } else {
            requestCameraPermission();
        }
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_CAMERA_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CAMERA_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission required for QR Scanner", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, new QrAnalyzer());

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

            } catch (ExecutionException | InterruptedException e) {
                Logger.e("Camera initialization failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private class QrAnalyzer implements ImageAnalysis.Analyzer {

        @Override
        @androidx.camera.core.ExperimentalGetImage
        public void analyze(@NonNull ImageProxy imageProxy) {
            android.media.Image mediaImage = imageProxy.getImage();
            if (mediaImage != null) {
                InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
                barcodeScanner.process(image)
                        .addOnSuccessListener(barcodes -> {
                            for (Barcode barcode : barcodes) {
                                String rawValue = barcode.getRawValue();
                                if (rawValue != null && !rawValue.isEmpty() && !rawValue.equals(lastScannedCode)) {
                                    lastScannedCode = rawValue;
                                    runOnUiThread(() -> displayScanResult(rawValue));
                                    break;
                                }
                            }
                        })
                        .addOnFailureListener(e -> Logger.e("QR analysis error", e))
                        .addOnCompleteListener(task -> imageProxy.close());
            } else {
                imageProxy.close();
            }
        }
    }

    private void displayScanResult(String result) {
        containerResult.setVisibility(View.VISIBLE);
        tvQrResult.setText(result);
        if (result.startsWith("http://") || result.startsWith("https://")) {
            btnOpenLink.setVisibility(View.VISIBLE);
        } else {
            btnOpenLink.setVisibility(View.GONE);
        }
    }

    private void copyToClipboard() {
        if (!lastScannedCode.isEmpty()) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Scanned Code", lastScannedCode);
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openLink() {
        if (lastScannedCode.startsWith("http://") || lastScannedCode.startsWith("https://")) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(lastScannedCode));
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Unable to open URL", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
        super.onDestroy();
    }
}
