package com.example.project;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PremiumRequestActivity extends AppCompatActivity {
    SQLiteDatabase database;
    LoginSessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.request_premium);

        sessionManager = LoginSessionManager.getInstance(this);

        Button upgradeButton = findViewById(R.id.upgradeButton);
        Button cancelButton = findViewById(R.id.cancelButton);

        upgradeButton.setOnClickListener(v -> {
            database = DatabaseHelper.getInstance(PremiumRequestActivity.this).openDatabase();

            // Lấy email từ userId
            Cursor cursor = database.rawQuery("SELECT email FROM tbl_user WHERE id = ?", new String[]{String.valueOf(sessionManager.getUserId())});

            if (cursor != null && cursor.moveToFirst()) {
                String email = cursor.getString(0);
                cursor.close();

                OkHttpClient client = new OkHttpClient();
                JSONObject json = new JSONObject();

                try {
                    json.put("email", email); // Gửi email thay vì userId
                } catch (Exception e) {
                    e.printStackTrace();
                }

                RequestBody body = RequestBody.create(
                        json.toString(),
                        MediaType.parse("application/json")
                );

                Request request = new Request.Builder()
                        .url("https://ticktickserver.onrender.com/api/premium/upgrade")
                        .post(body)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(PremiumRequestActivity.this, "Không thể kết nối tới server", Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (!response.isSuccessful()) {
                            runOnUiThread(() -> Toast.makeText(PremiumRequestActivity.this, "Lỗi server: " + response.code(), Toast.LENGTH_SHORT).show());
                            return;
                        }

                        String resBody = response.body().string();

                        try {
                            JSONObject resJson = new JSONObject(resBody);
                            boolean success = resJson.getBoolean("success");

                            if (success) {
                                String expirationDate = resJson.has("expirationDate") ? resJson.getString("expirationDate") : null;

                                // Cập nhật local DB
                                if (expirationDate != null) {
                                    database.execSQL("UPDATE tbl_user SET is_premium = 1, premium_expiration_date = ? WHERE id = ?",
                                            new Object[]{expirationDate, sessionManager.getUserId()});
                                } else {
                                    database.execSQL("UPDATE tbl_user SET is_premium = 1 WHERE id = ?",
                                            new Object[]{sessionManager.getUserId()});
                                }

                                runOnUiThread(() -> {
                                    Toast.makeText(PremiumRequestActivity.this, getString(R.string.register_premium_success), Toast.LENGTH_SHORT).show();
                                    finish();
                                });
                            } else {
                                runOnUiThread(() -> Toast.makeText(PremiumRequestActivity.this, "Không thể nâng cấp. Thử lại sau.", Toast.LENGTH_SHORT).show());
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            runOnUiThread(() -> Toast.makeText(PremiumRequestActivity.this, "Lỗi khi xử lý phản hồi", Toast.LENGTH_SHORT).show());
                        }
                    }
                });

            } else {
                if (cursor != null) cursor.close();
                Toast.makeText(PremiumRequestActivity.this, "Không tìm thấy email người dùng", Toast.LENGTH_SHORT).show();
            }
        });



        cancelButton.setOnClickListener(v -> {
            finish(); // Return to previous screen
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        DatabaseHelper.getInstance(this).closeDatabase();
    }
}