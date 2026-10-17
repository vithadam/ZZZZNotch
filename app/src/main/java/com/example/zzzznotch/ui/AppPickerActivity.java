package com.example.zzzznotch.ui;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zzzznotch.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AppPickerActivity extends AppCompatActivity {

    private AppAdapter adapter;
    private List<AppItem> allApps = new ArrayList<>();
    private List<AppItem> filteredApps = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_picker);

        RecyclerView rv = findViewById(R.id.rv_apps);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AppAdapter();
        rv.setAdapter(adapter);

        EditText etSearch = findViewById(R.id.et_search_app);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterApps(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        loadInstalledApps();
    }

    private void loadInstalledApps() {
        new Thread(() -> {
            PackageManager pm = getPackageManager();
            List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
            List<AppItem> items = new ArrayList<>();

            for (ApplicationInfo app : apps) {
                if (pm.getLaunchIntentForPackage(app.packageName) != null) {
                    String name = app.loadLabel(pm).toString();
                    Drawable icon = app.loadIcon(pm);
                    items.add(new AppItem(name, app.packageName, icon));
                }
            }

            Collections.sort(items, (o1, o2) -> o1.name.compareToIgnoreCase(o2.name));

            runOnUiThread(() -> {
                allApps = items;
                filteredApps = new ArrayList<>(items);
                adapter.notifyDataSetChanged();
            });
        }).start();
    }

    private void filterApps(String query) {
        filteredApps.clear();
        if (query == null || query.isEmpty()) {
            filteredApps.addAll(allApps);
        } else {
            String lower = query.toLowerCase().trim();
            for (AppItem item : allApps) {
                if (item.name.toLowerCase().contains(lower) || item.packageName.toLowerCase().contains(lower)) {
                    filteredApps.add(item);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private static class AppItem {
        String name;
        String packageName;
        Drawable icon;

        AppItem(String name, String packageName, Drawable icon) {
            this.name = name;
            this.packageName = packageName;
            this.icon = icon;
        }
    }

    private class AppAdapter extends RecyclerView.Adapter<AppViewHolder> {

        @NonNull
        @Override
        public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app_info, parent, false);
            return new AppViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
            AppItem item = filteredApps.get(position);
            holder.tvName.setText(item.name);
            holder.tvPackage.setText(item.packageName);
            if (item.icon != null) {
                holder.imgIcon.setImageDrawable(item.icon);
            }

            holder.itemView.setOnClickListener(v -> {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("selected_package", item.packageName);
                setResult(RESULT_OK, resultIntent);
                finish();
            });
        }

        @Override
        public int getItemCount() {
            return filteredApps != null ? filteredApps.size() : 0;
        }
    }

    private static class AppViewHolder extends RecyclerView.ViewHolder {
        ImageView imgIcon;
        TextView tvName;
        TextView tvPackage;

        public AppViewHolder(@NonNull View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.img_app_icon);
            tvName = itemView.findViewById(R.id.tv_app_name);
            tvPackage = itemView.findViewById(R.id.tv_package_name);
        }
    }
}
