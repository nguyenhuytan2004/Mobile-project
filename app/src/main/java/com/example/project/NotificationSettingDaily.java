package com.example.project;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class NotificationSettingDaily extends AppCompatActivity {

    TextView btnBack;
    private Switch switchDailyNotify, switchOverdueTasks, switchAllDayTasks;
    private TextView timeTextView, addTimeTextView;

    // Ngày trong tuần
    private TextView[] dayTextViews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notify_setting_daily);

        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> {
            finish();
        });

        // Gắn view
        switchDailyNotify = findViewById(R.id.switchDailyNotify);
        switchOverdueTasks = findViewById(R.id.switchOverdueTasks);
        switchAllDayTasks = findViewById(R.id.switchAllDayTasks);

        timeTextView = findViewById(R.id.timeTextView);
        addTimeTextView = findViewById(R.id.addTimeTextView);

        // Gắn các TextView của ngày trong tuần
        dayTextViews = new TextView[]{
                findViewById(R.id.daySunday),
                findViewById(R.id.dayMonday),
                findViewById(R.id.dayTuesday),
                findViewById(R.id.dayWednesday),
                findViewById(R.id.dayThursday),
                findViewById(R.id.dayFriday),
                findViewById(R.id.daySaturday)
        };

        // Click để đổi trạng thái ngày trong tuần
        for (TextView day : dayTextViews) {
            day.setOnClickListener(v -> toggleDaySelection((TextView) v));
        }

        // Click giờ
        timeTextView.setOnClickListener(v -> {
            Toast.makeText(this, "Hiển thị chọn giờ ở đây", Toast.LENGTH_SHORT).show();
            // TODO: Show TimePickerDialog
        });

        // Thêm giờ
        addTimeTextView.setOnClickListener(v -> {
            Toast.makeText(this, "Thêm khung giờ mới", Toast.LENGTH_SHORT).show();
            // TODO: Thêm time view hoặc mở hộp thoại
        });

        // Các switch toggle
        switchDailyNotify.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Toast.makeText(this, "Thông báo hàng ngày: " + isChecked, Toast.LENGTH_SHORT).show();
        });

        switchOverdueTasks.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Toast.makeText(this, "Nhiệm vụ quá hạn: " + isChecked, Toast.LENGTH_SHORT).show();
        });

        switchAllDayTasks.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Toast.makeText(this, "Nhiệm vụ cả ngày: " + isChecked, Toast.LENGTH_SHORT).show();
        });
    }

    private void toggleDaySelection(TextView dayView) {
        int selectedColor = 0xFFFF8800;
        int unselectedColor = 0xFF333333;
        int selectedTextColor = 0xFFFFFFFF;
        int unselectedTextColor = 0xFF888888;

        int bgColor = ((ColorDrawable) dayView.getBackground()).getColor();
        if (bgColor == selectedColor) {
            dayView.setBackgroundColor(unselectedColor);
            dayView.setTextColor(unselectedTextColor);
        } else {
            dayView.setBackgroundColor(selectedColor);
            dayView.setTextColor(selectedTextColor);
        }
    }
}
