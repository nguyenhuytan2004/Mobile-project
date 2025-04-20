package com.example.project;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class Setting extends Activity {
    LinearLayout btnWidget;
    LinearLayout btnMusic_Notify, profileLayout, btnTaskStatistic;
    TextView btnBack, btnUpgrade, btnLogout, nameTextView;
    ImageView avatarImage;
    SQLiteDatabase db;
    LoginSessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);

        btnWidget = findViewById(R.id.btnWidget);
        btnBack = findViewById(R.id.btnBack);
        btnMusic_Notify = findViewById(R.id.btnMusicNotify);
        btnUpgrade = findViewById(R.id.btnUpgrade);
        profileLayout = findViewById(R.id.profileLayout);
        btnTaskStatistic = findViewById(R.id.btnTaskStatistic);
        btnLogout = findViewById(R.id.btnLogout);

        avatarImage = findViewById(R.id.avatar);
        nameTextView = findViewById(R.id.name);

        sessionManager = LoginSessionManager.getInstance(this);
        loadUserProfile();

        if (DatabaseHelper.isPremiumUser(this)) {
            btnUpgrade.setText(getResources().getString(R.string.setting_extend_premium));
        }
        btnUpgrade.setOnClickListener(v -> {
            Intent intent = new Intent(Setting.this, PremiumRequestActivity.class);
            startActivity(intent);
        });

        profileLayout.setOnClickListener(v -> {
            Intent intent = new Intent(Setting.this, Profile.class);
            startActivity(intent);
        });
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
        btnTaskStatistic.setOnClickListener(v -> {
            // Check if the user is a premium user
            if (!DatabaseHelper.isPremiumUser(this)) {
                Intent premiumIntent = new Intent(this, PremiumRequestActivity.class);
                startActivity(premiumIntent);
            }
            // If the user is a premium user, proceed to TaskStatistics
            else {
                Intent intent = new Intent(Setting.this, TaskStatistics.class);
                startActivity(intent);
            }
        });
        btnLogout.setOnClickListener(v -> {
            sessionManager.logout(Setting.this); // Truyền context vào logout()

            Intent intent = new Intent(Setting.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear activity stack
            startActivity(intent);

            finish();
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile();
    }

    private void loadUserProfile() {
        try {
            db = DatabaseHelper.getInstance(this).openDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM tbl_user_information WHERE user_id = ?",
                    new String[]{String.valueOf(sessionManager.getUserId())});

            if (cursor.moveToFirst()) {
                String avatarPath = cursor.getString(cursor.getColumnIndexOrThrow("avatar"));
                if (avatarPath != null && !avatarPath.isEmpty()) {
                    try {
                        File imgFile = new File(avatarPath);
                        if (imgFile.exists()) {
                            Uri imageUri = Uri.fromFile(imgFile);
                            avatarImage.setImageURI(imageUri);
                            avatarImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        } else {
                            avatarImage.setImageResource(R.drawable.ic_user_avatar);
                        }
                    } catch (Exception e) {
                        avatarImage.setImageResource(R.drawable.ic_user_avatar);
                    }
                } else {
                    avatarImage.setImageResource(R.drawable.ic_user_avatar);
                }

                // Load name
                String fullName = cursor.getString(cursor.getColumnIndexOrThrow("full_name"));
                if (fullName != null && !fullName.isEmpty()) {
                    nameTextView.setText(fullName);
                } else {
                    nameTextView.setText(getResources().getString(R.string.user));
                }
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("Setting", "Lỗi khi tải thông tin người dùng");
        } finally {
            if (db != null) {
                DatabaseHelper.getInstance(this).closeDatabase();
            }
        }
    }
}