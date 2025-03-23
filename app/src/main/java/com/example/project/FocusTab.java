package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class FocusTab extends AppCompatActivity {
    ImageView homeTab, calendarTab;
    RelativeLayout front, behind;
    Button btnStart;
    ImageView btnEnd, btnPause, btnPlay, btnWhiteNoise;
    LinearLayout pauseLayout, playLayout;
    TextView btnOption;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.focus);

        front = findViewById(R.id.front);
        behind = findViewById(R.id.behind);

        homeTab = findViewById(R.id.homeTab);
        calendarTab = findViewById(R.id.calendarTab);

        btnStart = findViewById(R.id.btnStart);
        btnEnd = findViewById(R.id.end_button);
        btnPause = findViewById(R.id.pause_button);
        btnPlay = findViewById(R.id.play_button);
        btnWhiteNoise = findViewById(R.id.white_noise_button);
        btnOption = findViewById(R.id.textView53);

        pauseLayout = findViewById(R.id.pauseLayout);
        playLayout = findViewById(R.id.playLayout);


        homeTab.setOnClickListener(view -> {
            startActivity(new Intent(FocusTab.this, MainActivity.class));
        });

        calendarTab.setOnClickListener(view -> {
            startActivity(new Intent(FocusTab.this, CalendarTab.class));
        });

        btnStart.setOnClickListener(view -> {
            front.setVisibility(View.GONE);
            behind.setVisibility(View.VISIBLE);
        });

        btnEnd.setOnClickListener(view -> {
            front.setVisibility(View.VISIBLE);
            behind.setVisibility(View.GONE);
        });

        btnPause.setOnClickListener(view -> {
            pauseLayout.setVisibility(View.GONE);
            playLayout.setVisibility(View.VISIBLE);
        });

        btnPlay.setOnClickListener(view -> {
            pauseLayout.setVisibility(View.VISIBLE);
            playLayout.setVisibility(View.GONE);
        });

        btnWhiteNoise.setOnClickListener(view -> {
            startActivity(new Intent(FocusTab.this, WhiteNoise.class));
        });

        btnOption.setOnClickListener(view -> {
            View popupView = LayoutInflater.from(FocusTab.this).inflate(R.layout.option_in_focus, null);
            PopupWindow popupWindow = new PopupWindow(popupView, 500, LinearLayout.LayoutParams.WRAP_CONTENT, true);
            popupWindow.showAsDropDown(view, -430, -100);

            popupView.findViewById(R.id.focusSetting).setOnClickListener(v -> {
                Toast.makeText(FocusTab.this, "Cài đặt tập trung", Toast.LENGTH_SHORT).show();
                popupWindow.dismiss();
            });

            popupView.findViewById(R.id.addDedicatedRecord).setOnClickListener(v -> {
                Toast.makeText(FocusTab.this, "Thêm bản ghi chuyên tâm", Toast.LENGTH_SHORT).show();
                popupWindow.dismiss();
            });
        });
    }
}
