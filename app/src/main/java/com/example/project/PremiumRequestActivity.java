package com.example.project;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class PremiumRequestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.request_premium);

        Button upgradeButton = findViewById(R.id.upgradeButton);
        Button cancelButton = findViewById(R.id.cancelButton);

        upgradeButton.setOnClickListener(v -> {
            Toast.makeText(this, "Processing payment...", Toast.LENGTH_SHORT).show();
            // Implement your payment gateway integration here
            // After successful payment, update user status in database
        });

        cancelButton.setOnClickListener(v -> {
            finish(); // Return to previous screen
        });
    }
}