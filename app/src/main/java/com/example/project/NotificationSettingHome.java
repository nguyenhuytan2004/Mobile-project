package com.example.project;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class NotificationSettingHome extends Activity {

    TextView btnBack;

    private LinearLayout dailyNotifyLayout, reminderSoundLayout, setReminderSoundLayout, emailNotifyLayout, hapticsAppLayout;
    private Switch switchRepeatReminder, switchHaptics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notify_setting_home); // Đảm bảo tên file XML là music_and_notification.xml

        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> {
            finish();
        });

        // Ánh xạ view
        dailyNotifyLayout = findViewById(R.id.dailyNotifyLayout); // bạn có thể cần thêm id trong XML
        reminderSoundLayout = findViewById(R.id.reminderSoundLayout); // tương tự, thêm id trong XML nếu cần
        setReminderSoundLayout = findViewById(R.id.setReminderSoundLayout);
        emailNotifyLayout = findViewById(R.id.emailNotifyLayout);
        hapticsAppLayout = findViewById(R.id.hapticsAppLayout);
        switchRepeatReminder = findViewById(R.id.switchRepeatReminder); // nên đặt id trong XML: switchRepeatReminder
        switchHaptics = findViewById(R.id.switchHaptics); // id: switchHaptics

        // Sự kiện cho các mục
        setReminderSoundLayout.setOnClickListener(v -> {
            // Chuyển đến cài đặt âm thanh hệ thống (nếu muốn mở chọn nhạc chuông)
            Intent intent = new Intent(Settings.ACTION_SOUND_SETTINGS);
            startActivity(intent);
        });

        dailyNotifyLayout.setOnClickListener(v -> {
            Intent intent = new Intent(NotificationSettingHome.this, NotificationSettingDaily.class);
            startActivity(intent);
        });

        emailNotifyLayout.setOnClickListener(v -> {
            Toast.makeText(this, "Tính năng gửi email chưa được bật", Toast.LENGTH_SHORT).show();
        });

        // Switch nhắc nhở liên tục
        switchRepeatReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Toast.makeText(this, "Nhắc nhở liên tục đã bật", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Nhắc nhở liên tục đã tắt", Toast.LENGTH_SHORT).show();
            }
            // Có thể lưu trạng thái vào SharedPreferences nếu muốn duy trì
        });

        // Switch Haptics
        switchHaptics.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Toast.makeText(this, "Haptics đã bật", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Haptics đã tắt", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
