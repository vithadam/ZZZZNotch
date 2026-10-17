package com.example.zzzznotch.ui;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zzzznotch.R;
import com.example.zzzznotch.utils.Logger;

import java.util.List;

public class AboutDiagnosticActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_diagnostic);

        TextView tvLogs = findViewById(R.id.tv_logs);

        List<String> logs = Logger.getLogHistory();
        if (logs != null && !logs.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String line : logs) {
                sb.append(line).append("\n");
            }
            tvLogs.setText(sb.toString());
        } else {
            tvLogs.setText("No diagnostic logs recorded yet.");
        }
    }
}
