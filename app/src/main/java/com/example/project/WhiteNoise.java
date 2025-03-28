package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class WhiteNoise extends AppCompatActivity {
    TextView textView32;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.white_noise);

        textView32 = findViewById(R.id.textView32);

        textView32.setOnClickListener(view -> {
            finish();
        });
    }
}
