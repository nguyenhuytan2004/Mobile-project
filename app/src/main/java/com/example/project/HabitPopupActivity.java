package com.example.project;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class HabitPopupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Make activity appear as dialog
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_habit_popup);
        
        // Set window properties
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(params);
        
        // Get habit info from intent
        int habitId = getIntent().getIntExtra("habit_id", -1);
        String habitName = getIntent().getStringExtra("habit_name");
        
        // Set habit name in UI
        TextView tvHabitName = findViewById(R.id.tvHabitName);
        tvHabitName.setText(habitName);
        
        // Set up buttons
        Button btnComplete = findViewById(R.id.btnComplete);
        Button btnSkip = findViewById(R.id.btnSkip);
        
        btnComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Mark habit as completed
                // (Same logic as in HabitActionReceiver)
                finish();
            }
        });
        
        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}