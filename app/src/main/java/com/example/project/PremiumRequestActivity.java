package com.example.project;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
            if (!DatabaseHelper.isPremiumUser(this)) {
                LocalDateTime premiumExpirationDate = LocalDateTime.now().plusDays(30);
                String premiumExpirationDateString = premiumExpirationDate.format(DateTimeFormatter.ofPattern("dd - MM - yyyy"));

                database = DatabaseHelper.getInstance(this).openDatabase();
                database.execSQL("UPDATE tbl_user SET is_premium = 1, premium_expiration_date = ? WHERE id = ?",
                        new Object[]{premiumExpirationDateString, sessionManager.getUserId()});

                Toast.makeText(this, getResources().getString(R.string.register_premium_success), Toast.LENGTH_SHORT).show();
            } else {
                database = DatabaseHelper.getInstance(this).openDatabase();
                Cursor cursor = database.rawQuery("SELECT premium_expiration_date FROM tbl_user WHERE id = ?", new String[]{String.valueOf(sessionManager.getUserId())});
                if (cursor.moveToFirst()) {
                    String premiumExpirationDate = cursor.getString(0);
                    LocalDate currentExpirationDate = LocalDate.parse(premiumExpirationDate, DateTimeFormatter.ofPattern("dd - MM - yyyy"));
                    LocalDate newPremiumExpirationDate = currentExpirationDate.plusDays(30);
                    String newPremiumExpirationDateString = newPremiumExpirationDate.format(DateTimeFormatter.ofPattern("dd - MM - yyyy"));

                    database.execSQL("UPDATE tbl_user SET premium_expiration_date = ? WHERE id = ?", new Object[]{newPremiumExpirationDateString, sessionManager.getUserId()});
                }
                cursor.close();
                Toast.makeText(this, getResources().getString(R.string.extend_premium_success), Toast.LENGTH_SHORT).show();
            }
            finish();
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