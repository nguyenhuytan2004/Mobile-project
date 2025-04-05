package com.example.project;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Setting extends Activity {
    LinearLayout btnWidget;
    TextView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);

        btnWidget = findViewById(R.id.btnWidget);
        btnBack = findViewById(R.id.btnBack);

        btnWidget.setOnClickListener(v -> {
            Intent intent = new Intent(Setting.this, Widget.class);
            startActivity(intent);
        });
        btnBack.setOnClickListener(v -> {
            finish();
        });
    }
}
