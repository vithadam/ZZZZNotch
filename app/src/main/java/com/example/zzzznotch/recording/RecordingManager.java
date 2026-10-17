package com.example.zzzznotch.recording;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;

import com.example.zzzznotch.utils.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecordingManager {

    private static volatile RecordingManager instance;

    public static RecordingManager getInstance() {
        if (instance == null) {
            synchronized (RecordingManager.class) {
                if (instance == null) {
                    instance = new RecordingManager();
                }
            }
        }
        return instance;
    }

    public File getRecordingDirectory(Context context) {
        File dir = new File(context.getExternalFilesDir(null), "recordings");
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            Logger.d("Recordings directory created: " + created);
        }
        return dir;
    }

    public File createNewVideoFile(Context context) {
        File dir = getRecordingDirectory(context);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        return new File(dir, "VID_" + timeStamp + ".mp4");
    }

    public File createNewAudioFile(Context context) {
        File dir = getRecordingDirectory(context);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        return new File(dir, "AUD_" + timeStamp + ".m4a");
    }

    public List<File> getAllRecordings(Context context) {
        File dir = getRecordingDirectory(context);
        File[] files = dir.listFiles();
        List<File> list = new ArrayList<>();
        if (files != null) {
            for (File f : files) {
                if (f.isFile() && (f.getName().endsWith(".mp4") || f.getName().endsWith(".m4a"))) {
                    list.add(f);
                }
            }
            Collections.sort(list, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
        }
        return list;
    }

    public boolean exportRecordingToUri(Context context, File sourceFile, OutputStream destinationOutputStream) {
        try (InputStream in = new FileInputStream(sourceFile);
             OutputStream out = destinationOutputStream) {
            byte[] buffer = new byte[8192];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            out.flush();
            Logger.i("Successfully exported recording: " + sourceFile.getName());
            return true;
        } catch (Exception e) {
            Logger.e("Failed to export recording: " + sourceFile.getName(), e);
            return false;
        }
    }

    public boolean deleteRecording(File file) {
        if (file != null && file.exists()) {
            boolean deleted = file.delete();
            Logger.d("Deleted recording " + file.getName() + ": " + deleted);
            return deleted;
        }
        return false;
    }

    public void deleteAllRecordings(Context context) {
        List<File> files = getAllRecordings(context);
        for (File f : files) {
            deleteRecording(f);
        }
    }

    public long getAvailableStorageMb(Context context) {
        try {
            File dir = getRecordingDirectory(context);
            StatFs stat = new StatFs(dir.getPath());
            long bytesAvailable = stat.getAvailableBytes();
            return bytesAvailable / (1024 * 1024);
        } catch (Exception e) {
            return 1000;
        }
    }
}
