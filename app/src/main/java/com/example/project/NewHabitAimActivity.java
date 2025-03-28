package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class NewHabitAimActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private Button btnSave;
    private Switch switchAutoPopup;
    private Chip chipDaily, chipWeekly, chipInterval;
    private Chip chipSun, chipMon, chipTue, chipWed, chipThu, chipFri, chipSat;
    private Habit habit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_habit_aim);

        // Get habit data from previous activity
        habit = (Habit) getIntent().getSerializableExtra("habit");
        if (habit == null) {
            habit = new Habit();
        }

        // Initialize UI components
        initializeUI();

        // Set default values
        setDefaultValues();

        // Set up listeners
        setupListeners();
    }

    private void initializeUI() {
        btnBack = findViewById(R.id.btnBack);
        btnSave = findViewById(R.id.btnSave);
        switchAutoPopup = findViewById(R.id.switchAutoPopup);

        // Frequency chips
        chipDaily = findViewById(R.id.chip8);
        chipWeekly = findViewById(R.id.chip9);
        chipInterval = findViewById(R.id.chip10);

        // Day of week chips
        chipSun = findViewById(R.id.chip);
        chipMon = findViewById(R.id.chip2);
        chipTue = findViewById(R.id.chip3);
        chipWed = findViewById(R.id.chip4);
        chipThu = findViewById(R.id.chip5);
        chipFri = findViewById(R.id.chip6);
        chipSat = findViewById(R.id.chip7);
    }

    private void setDefaultValues() {
        // Set default frequency
        chipDaily.setChecked(true);
        habit.setFrequency("Daily");

        // Set default start date to today
        Calendar calendar = Calendar.getInstance();
        Date today = calendar.getTime();
        habit.setStartDate(today);

        // Default goal
        habit.setGoal("5 Page daily");
        habit.setGoalDays("Forever");
        habit.setSection("Others");
        habit.setReminder("13:00");

        // Auto pop-up default to false
        switchAutoPopup.setChecked(false);
        habit.setAutoPopup(false);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveHabit();
            }
        });

        // Frequency chip group
        chipDaily.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    habit.setFrequency("Daily");
                    chipWeekly.setChecked(false);
                    chipInterval.setChecked(false);
                }
            }
        });

        chipWeekly.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    habit.setFrequency("Weekly");
                    chipDaily.setChecked(false);
                    chipInterval.setChecked(false);
                }
            }
        });

        chipInterval.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    habit.setFrequency("Interval");
                    chipDaily.setChecked(false);
                    chipWeekly.setChecked(false);
                }
            }
        });

        // Day of week chips
        setupDayChipListener(chipSun, 0);
        setupDayChipListener(chipMon, 1);
        setupDayChipListener(chipTue, 2);
        setupDayChipListener(chipWed, 3);
        setupDayChipListener(chipThu, 4);
        setupDayChipListener(chipFri, 5);
        setupDayChipListener(chipSat, 6);

        // Auto popup switch
        switchAutoPopup.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                habit.setAutoPopup(isChecked);
            }
        });
    }

    private void setupDayChipListener(final Chip chip, final int dayIndex) {
        chip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                boolean[] weekDays = habit.getWeekDays();
                weekDays[dayIndex] = isChecked;
                habit.setWeekDays(weekDays);
            }
        });
    }

    private void saveHabit() {
        // Here you would typically save the habit to your database
        // For demo purposes, we'll just go back to the HabitActivity

        // You could add this habit to a static list or pass it back via intent
        // StaticHabitList.getInstance().addHabit(habit);

        // Show confirmation
        android.widget.Toast.makeText(this, "Habit saved successfully!",
                android.widget.Toast.LENGTH_SHORT).show();

        // Close all activities and go back to the HabitActivity
        Intent intent = new Intent(this, HabitActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}