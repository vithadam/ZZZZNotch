package com.example.zzzznotch.utils;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Logger {
    private static final String TAG = "ZZZZNotch";
    private static final int MAX_LOGS = 100;
    private static final List<String> logHistory = Collections.synchronizedList(new ArrayList<>());

    public static void d(String message) {
        Log.d(TAG, message);
        addLog("D: " + message);
    }

    public static void i(String message) {
        Log.i(TAG, message);
        addLog("I: " + message);
    }

    public static void e(String message, Throwable t) {
        Log.e(TAG, message, t);
        addLog("E: " + message + (t != null ? " (" + t.getMessage() + ")" : ""));
    }

    public static void e(String message) {
        e(message, null);
    }

    private static void addLog(String log) {
        synchronized (logHistory) {
            logHistory.add(0, System.currentTimeMillis() + " - " + log);
            if (logHistory.size() > MAX_LOGS) {
                logHistory.remove(logHistory.size() - 1);
            }
        }
    }

    public static List<String> getLogHistory() {
        synchronized (logHistory) {
            return new ArrayList<>(logHistory);
        }
    }
}
