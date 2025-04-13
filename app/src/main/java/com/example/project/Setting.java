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
    LinearLayout btnMusic_Notify, profileLayout;
    TextView btnBack, btnUpgrade, nameTextView;
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

        avatarImage = findViewById(R.id.avatar);
        nameTextView = findViewById(R.id.name);

        sessionManager = LoginSessionManager.getInstance(this);
        loadUserProfile();

        if (DatabaseHelper.isPremiumUser(this)) {
            btnUpgrade.setText("GIA HẠN");
        }
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
                    nameTextView.setText("Người dùng");
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