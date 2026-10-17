package com.example.zzzznotch.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.zzzznotch.data.preferences.AppPreferences;
import com.example.zzzznotch.utils.Logger;
import com.example.zzzznotch.utils.PermissionUtils;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) return;
        String action = intent.getAction();
        Logger.i("BootReceiver received action: " + action);

        if (Intent.ACTION_BOOT_COMPLETED.equals(action) ||
                "android.intent.action.QUICKBOOT_POWERON".equals(action) ||
                Intent.ACTION_LOCKED_BOOT_COMPLETED.equals(action)) {

            AppPreferences prefs = AppPreferences.getInstance(context);
            if (prefs.isRunOnStartupEnabled() && prefs.isMasterEnabled()) {
                if (PermissionUtils.hasOverlayPermission(context)) {
                    Logger.i("Starting NotchOverlayService on boot");
                    NotchOverlayService.updateServiceState(context);
                } else {
                    Logger.i("Boot startup skipped: Overlay permission missing");
                }
            }
        }
    }
}
