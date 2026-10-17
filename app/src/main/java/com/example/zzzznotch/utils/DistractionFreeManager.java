package com.example.zzzznotch.utils;

import android.content.Context;
import android.widget.Toast;

import com.example.zzzznotch.data.preferences.AppPreferences;
import com.example.zzzznotch.service.NotchOverlayService;

import java.util.HashSet;
import java.util.Set;

public class DistractionFreeManager {

    private static volatile DistractionFreeManager instance;

    private boolean isManualModeActive = false;
    private boolean isAutoExcludedAppActive = false;
    private final Set<String> excludedPackages = new HashSet<>();

    private DistractionFreeManager() {
        // Default gaming & video app exclusions
        excludedPackages.add("com.google.android.youtube");
        excludedPackages.add("com.netflix.mediaclient");
        excludedPackages.add("com.mxtech.videoplayer.ad");
        excludedPackages.add("com.mxtech.videoplayer.pro");
    }

    public static DistractionFreeManager getInstance() {
        if (instance == null) {
            synchronized (DistractionFreeManager.class) {
                if (instance == null) {
                    instance = new DistractionFreeManager();
                }
            }
        }
        return instance;
    }

    public boolean isDistractionFreeActive() {
        return isManualModeActive || isAutoExcludedAppActive;
    }

    public boolean isManualModeActive() {
        return isManualModeActive;
    }

    public void setManualModeActive(boolean active, Context context) {
        this.isManualModeActive = active;
        if (context != null) {
            Toast.makeText(context, "Gaming/Movie Distraction-Free Mode " + (active ? "ON" : "OFF"), Toast.LENGTH_SHORT).show();
            NotchOverlayService.updateServiceState(context);
        }
    }

    public void toggleManualMode(Context context) {
        setManualModeActive(!isManualModeActive, context);
    }

    public void onForegroundAppChanged(String packageName, Context context) {
        if (packageName == null) return;
        boolean isExcluded = excludedPackages.contains(packageName.toLowerCase().trim());
        if (isExcluded != isAutoExcludedAppActive) {
            isAutoExcludedAppActive = isExcluded;
            Logger.d("Distraction-Free Auto-Exclusion state: " + isAutoExcludedAppActive + " for " + packageName);
            if (context != null) {
                NotchOverlayService.updateServiceState(context);
            }
        }
    }

    public Set<String> getExcludedPackages() {
        return new HashSet<>(excludedPackages);
    }

    public void addExcludedPackage(String pkg) {
        if (pkg != null && !pkg.isEmpty()) {
            excludedPackages.add(pkg.toLowerCase().trim());
        }
    }

    public void removeExcludedPackage(String pkg) {
        if (pkg != null) {
            excludedPackages.remove(pkg.toLowerCase().trim());
        }
    }
}
