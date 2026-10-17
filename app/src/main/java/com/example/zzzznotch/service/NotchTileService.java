package com.example.zzzznotch.service;

import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import com.example.zzzznotch.data.preferences.AppPreferences;
import com.example.zzzznotch.utils.Logger;

public class NotchTileService extends TileService {

    @Override
    public void onStartListening() {
        super.onStartListening();
        updateTileState();
    }

    @Override
    public void onClick() {
        super.onClick();
        AppPreferences prefs = AppPreferences.getInstance(this);
        boolean newState = !prefs.isMasterEnabled();
        prefs.setMasterEnabled(newState);
        Logger.d("Quick Settings Tile clicked -> Master Notch set to " + newState);

        NotchOverlayService.updateServiceState(this);
        updateTileState();
    }

    private void updateTileState() {
        Tile tile = getQsTile();
        if (tile != null) {
            boolean isEnabled = AppPreferences.getInstance(this).isMasterEnabled();
            tile.setState(isEnabled ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
            tile.setLabel("zzzznotch");
            tile.updateTile();
        }
    }
}
