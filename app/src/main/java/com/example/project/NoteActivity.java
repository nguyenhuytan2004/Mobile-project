package com.example.project;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class NoteActivity extends AppCompatActivity {
    ImageView icon4;
    TextView txtDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note);

        txtDate = findViewById(R.id.txtDate);
        icon4 = findViewById(R.id.icon4);

        icon4.setOnClickListener(v -> {
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
    }
}
