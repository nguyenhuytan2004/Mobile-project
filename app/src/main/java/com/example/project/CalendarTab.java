package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class CalendarTab extends AppCompatActivity {
    ImageView homeTab, focusTab;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_calendar);

        homeTab = findViewById(R.id.homeTab);
        focusTab = findViewById(R.id.focusTab);

        homeTab.setOnClickListener(view -> {
            startActivity(new Intent(CalendarTab.this, MainActivity.class));
        });

        focusTab.setOnClickListener(view -> {
            startActivity(new Intent(CalendarTab.this, FocusTab.class));
        });
    }
}
