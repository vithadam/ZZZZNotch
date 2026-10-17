package com.example.zzzznotch.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zzzznotch.R;
import com.example.zzzznotch.data.models.ActionType;
import com.example.zzzznotch.data.models.RuleConfig;
import com.example.zzzznotch.data.repository.AppRepository;

import java.util.ArrayList;
import java.util.List;

public class RulesActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PICK_APP_FOR_RULE = 4001;

    private AppRepository repository;
    private RuleAdapter adapter;
    private List<RuleConfig> rulesList = new ArrayList<>();

    private EditText dialogEtConditionValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rules);

        repository = AppRepository.getInstance(this);

        RecyclerView rv = findViewById(R.id.rv_rules);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new RuleAdapter();
        rv.setAdapter(adapter);

        findViewById(R.id.btn_add_rule).setOnClickListener(v -> showAddRuleDialog());

        loadRules();
    }

    private void loadRules() {
        rulesList = repository.getAllRules();
        adapter.notifyDataSetChanged();
    }

    private void showAddRuleDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create New Context Rule");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        EditText etName = new EditText(this);
        etName.setHint("Rule Name (e.g. Gaming Mode)");
        layout.addView(etName);

        TextView tvCondHeader = new TextView(this);
        tvCondHeader.setText("Condition Type:");
        tvCondHeader.setPadding(0, 20, 0, 5);
        layout.addView(tvCondHeader);

        Spinner spinnerCondition = new Spinner(this);
        RuleConfig.ConditionType[] condTypes = RuleConfig.ConditionType.values();
        String[] condNames = new String[condTypes.length];
        for (int i = 0; i < condTypes.length; i++) {
            condNames[i] = condTypes[i].getDisplayName();
        }
        ArrayAdapter<String> condAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, condNames);
        condAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCondition.setAdapter(condAdapter);
        layout.addView(spinnerCondition);

        dialogEtConditionValue = new EditText(this);
        dialogEtConditionValue.setHint("Target Package / Value (optional)");
        layout.addView(dialogEtConditionValue);

        TextView tvActionHeader = new TextView(this);
        tvActionHeader.setText("Override Action:");
        tvActionHeader.setPadding(0, 20, 0, 5);
        layout.addView(tvActionHeader);

        Spinner spinnerAction = new Spinner(this);
        ActionType[] actions = ActionType.values();
        String[] actionNames = new String[actions.length];
        for (int i = 0; i < actions.length; i++) {
            actionNames[i] = actions[i].getDisplayName();
        }
        ArrayAdapter<String> actionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, actionNames);
        actionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAction.setAdapter(actionAdapter);
        layout.addView(spinnerAction);

        builder.setView(layout);

        builder.setPositiveButton("Save Rule", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) name = "Rule #" + (rulesList.size() + 1);

            RuleConfig.ConditionType selCond = condTypes[spinnerCondition.getSelectedItemPosition()];
            ActionType selAction = actions[spinnerAction.getSelectedItemPosition()];
            String condVal = dialogEtConditionValue.getText().toString().trim();

            RuleConfig rule = new RuleConfig(name, selCond, condVal, selAction, "", true);
            repository.saveRule(rule);
            loadRules();
            Toast.makeText(this, "Rule created!", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private class RuleAdapter extends RecyclerView.Adapter<RuleViewHolder> {

        @NonNull
        @Override
        public RuleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rule_config, parent, false);
            return new RuleViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RuleViewHolder holder, int position) {
            RuleConfig rule = rulesList.get(position);
            holder.tvName.setText(rule.getName());

            String desc = "When " + rule.getConditionType().getDisplayName();
            if (!rule.getConditionValue().isEmpty()) {
                desc += " (" + rule.getConditionValue() + ")";
            }
            desc += " -> " + rule.getOverrideAction().getDisplayName();
            holder.tvDesc.setText(desc);

            holder.btnDelete.setOnClickListener(v -> {
                repository.deleteRule(rule);
                loadRules();
                Toast.makeText(RulesActivity.this, "Rule deleted", Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public int getItemCount() {
            return rulesList != null ? rulesList.size() : 0;
        }
    }

    private static class RuleViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvDesc;
        ImageButton btnDelete;

        public RuleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_rule_name);
            tvDesc = itemView.findViewById(R.id.tv_rule_desc);
            btnDelete = itemView.findViewById(R.id.btn_delete_rule);
        }
    }
}
