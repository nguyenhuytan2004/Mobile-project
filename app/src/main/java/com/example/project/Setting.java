package com.example.project;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Setting extends Activity {
    LinearLayout btnWidget;
    LinearLayout btnMusic_Notify;
    TextView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);

        btnWidget = findViewById(R.id.btnWidget);
        btnBack = findViewById(R.id.btnBack);
        btnMusic_Notify = findViewById(R.id.btnMusicNotify);

        btnWidget.setOnClickListener(v -> {
            Intent intent = new Intent(Setting.this, Widget.class);
            startActivity(intent);
        });
        btnBack.setOnClickListener(v -> {
            finish();
        });

        btnMusic_Notify.setOnClickListener(v -> {
            Intent intent = new Intent(Setting.this, NotificationSettingHome.class);
            startActivity(intent);
        });
    }
}
