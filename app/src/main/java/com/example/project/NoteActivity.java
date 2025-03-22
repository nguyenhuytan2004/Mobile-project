package com.example.project;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class NoteActivity extends AppCompatActivity {
    ImageView icon4;
    TextView txtDate;
    TextView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note);

        txtDate = findViewById(R.id.txtDate);
        icon4 = findViewById(R.id.icon4);
        btnBack = findViewById(R.id.textView);

        icon4.setOnClickListener(view -> {
            CalendarDialogFragment dialog = new CalendarDialogFragment();
            dialog.setOnDateSelectedListener(date -> {
                txtDate.setText(date);

                // Chuyển đổi ngày được chọn thành LocalDate (API 26+)
                LocalDate selectedDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                LocalDate today = LocalDate.now();

                int day = selectedDate.getDayOfMonth();
                int month = selectedDate.getMonthValue();
                txtDate.setText("Ngày " + day + ", tháng " + month);

                // Đổi màu text
                if (selectedDate.isBefore(today)) {
                    txtDate.setTextColor(getResources().getColor(R.color.red)); // Ngày trong quá khứ
                } else {
                    txtDate.setTextColor(getResources().getColor(R.color.statistics_blue)); // Ngày hôm nay hoặc tương lai
                }
            });
            dialog.show(getSupportFragmentManager(), "CalendarDialog");
        });
        btnBack.setOnClickListener(view -> {
            Intent intent = new Intent(NoteActivity.this, MainActivity.class);
            startActivity(intent);
        });
    }
}
