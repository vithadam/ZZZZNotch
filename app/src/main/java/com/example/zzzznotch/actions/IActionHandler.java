package com.example.zzzznotch.actions;

import android.content.Context;

import com.example.zzzznotch.data.models.GestureConfig;

public interface IActionHandler {
    boolean execute(Context context, GestureConfig config);
}
