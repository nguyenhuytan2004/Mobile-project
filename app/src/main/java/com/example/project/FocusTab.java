package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class FocusTab extends AppCompatActivity {
    ImageView homeTab, calendarTab;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.focus);

        homeTab = findViewById(R.id.homeTab);
        calendarTab = findViewById(R.id.calendarTab);

        homeTab.setOnClickListener(view -> {
            startActivity(new Intent(FocusTab.this, MainActivity.class));
        });

        calendarTab.setOnClickListener(view -> {
            startActivity(new Intent(FocusTab.this, CalendarTab.class));
        });
    }
}
