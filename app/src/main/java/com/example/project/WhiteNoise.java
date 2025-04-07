package com.example.project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class WhiteNoise extends AppCompatActivity {
    TextView btnBack;
    ImageView noneSound, clockSound, fireSound, rainSound, stormSound, boilingSound;
    private String currentSound = "none";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.white_noise);

        btnBack = findViewById(R.id.btnBack);
        noneSound = findViewById(R.id.noneSound);
        clockSound = findViewById(R.id.clockSound);
        fireSound = findViewById(R.id.fireSound);
        rainSound = findViewById(R.id.rainSound);
        stormSound = findViewById(R.id.stormSound);
        boilingSound = findViewById(R.id.boilingSound);

        // Load current sound setting
        SharedPreferences sharedPreferences = getSharedPreferences("whiteNoise", MODE_PRIVATE);
        currentSound = sharedPreferences.getString("storage_sound", "none");

        // Highlight the currently selected sound
        highlightCurrentSound();

        btnBack.setOnClickListener(view -> {
            setWhiteNoiseSound(currentSound);

            finish();
        });

        noneSound.setOnClickListener(view -> {
            setWhiteNoiseSound("none");
        });

        clockSound.setOnClickListener(view -> {
            setWhiteNoiseSound("clock");
        });

        fireSound.setOnClickListener(view -> {
            setWhiteNoiseSound("fire");
        });

        rainSound.setOnClickListener(view -> {
            setWhiteNoiseSound("rain");
        });

        stormSound.setOnClickListener(view -> {
            setWhiteNoiseSound("storm");
        });

        boilingSound.setOnClickListener(view -> {
            setWhiteNoiseSound("boiling");
        });
    }

    private void setWhiteNoiseSound(String sound) {
        currentSound = sound;

        // Save to SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("whiteNoise", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("sound", sound);
        editor.putString("storage_sound", sound);
        editor.apply();

        highlightCurrentSound();

        finish();
    }

    private void highlightCurrentSound() {
        // Reset all icons to normal state
        noneSound.setAlpha(0.5f);
        clockSound.setAlpha(0.5f);
        fireSound.setAlpha(0.5f);
        rainSound.setAlpha(0.5f);
        stormSound.setAlpha(0.5f);
        boilingSound.setAlpha(0.5f);

        // Highlight selected icon
        switch (currentSound) {
            case "none":
                noneSound.setAlpha(1.0f);
                break;
            case "clock":
                clockSound.setAlpha(1.0f);
                break;
            case "fire":
                fireSound.setAlpha(1.0f);
                break;
            case "rain":
                rainSound.setAlpha(1.0f);
                break;
            case "storm":
                stormSound.setAlpha(1.0f);
                break;
            case "boiling":
                boilingSound.setAlpha(1.0f);
                break;
        }
    }
}