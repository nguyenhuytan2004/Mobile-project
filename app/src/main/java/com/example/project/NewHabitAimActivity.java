package com.example.project;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

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
    private ChipGroup weekdaysChipGroup;
    private Habit habit;
    
    private EditText editTextGoal;
    private TextView tvStartDate;
    private Spinner spinnerGoalDays;
    private Spinner spinnerSection;
    private TextView tvReminderTime;

    private Calendar calendar;

    // Add this as a class variable
    private boolean isEditing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_habit_aim);

        // Get the habit object and editing status from intent
        habit = (Habit) getIntent().getSerializableExtra("habit");
        isEditing = getIntent().getBooleanExtra("isEditing", false);
        
        if (habit == null) {
            habit = new Habit();
            // Initialize weekDays array with false values
            habit.setWeekDays(new boolean[]{false, false, false, false, false, false, false});
        }

        initializeUI();
        setDefaultValues();
        setupListeners();
        updateWeekdayChipGroupVisibility();
        
        // Update title if editing
        if (isEditing) {
            ((TextView) findViewById(R.id.new_habit)).setText(R.string.edit_habit);
        }
    }

    private void initializeUI() {
        btnBack = findViewById(R.id.btnBack);
        btnSave = findViewById(R.id.btnSave);
        switchAutoPopup = findViewById(R.id.switchAutoPopup);

        chipDaily = findViewById(R.id.chip8);
        chipWeekly = findViewById(R.id.chip9);
        chipInterval = findViewById(R.id.chip10);

        weekdaysChipGroup = (ChipGroup) findViewById(R.id.chip).getParent();
        
        chipSun = findViewById(R.id.chip);
        chipMon = findViewById(R.id.chip2);
        chipTue = findViewById(R.id.chip3);
        chipWed = findViewById(R.id.chip4);
        chipThu = findViewById(R.id.chip5);
        chipFri = findViewById(R.id.chip6);
        chipSat = findViewById(R.id.chip7);
        
        editTextGoal = findViewById(R.id.editTextGoal);
        tvStartDate = findViewById(R.id.tvStartDate);
        spinnerGoalDays = findViewById(R.id.spinnerGoalDays);
        spinnerSection = findViewById(R.id.spinnerSection);
        tvReminderTime = findViewById(R.id.tvReminderTime);
        
        calendar = Calendar.getInstance();
    }

    private void setDefaultValues() {
        chipDaily.setChecked(true);
        habit.setFrequency("Daily");

        Calendar calendar = Calendar.getInstance();
        Date today = calendar.getTime();
        habit.setStartDate(today);

        String defaultGoal = getString(R.string.default_goal);
        String defaultGoalDays = getString(R.string.forever);
        String defaultSection = getString(R.string.default_section);
        
        // Set default values using string resources
        habit.setGoal(defaultGoal);
        habit.setGoalDays(defaultGoalDays);
        habit.setSection(defaultSection);
        habit.setReminder("13:00");  // Time format can stay the same

        switchAutoPopup.setChecked(false);
        habit.setAutoPopup(false);
        
        editTextGoal.setText(defaultGoal);
        
        // Use device locale instead of hardcoded US locale
        Locale currentLocale = getResources().getConfiguration().locale;
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", currentLocale);
        String formattedDate = dateFormat.format(calendar.getTime());
        tvStartDate.setText(formattedDate);
        
        // Load arrays from resources
        String[] goalDaysOptions = getResources().getStringArray(R.array.goal_days_options);
        ArrayAdapter<String> goalDaysAdapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_spinner_item, goalDaysOptions);
        goalDaysAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGoalDays.setAdapter(goalDaysAdapter);
        spinnerGoalDays.setSelection(4);  // Default to "Forever"
        
        String[] sectionOptions = getResources().getStringArray(R.array.section_options);
        ArrayAdapter<String> sectionAdapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_spinner_item, sectionOptions);
        sectionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSection.setAdapter(sectionAdapter);
        spinnerSection.setSelection(3);  // Default to "Others"
        
        tvReminderTime.setText("13:00");
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

        chipDaily.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    habit.setFrequency("Daily");
                    chipWeekly.setChecked(false);
                    chipInterval.setChecked(false);
                    updateWeekdayChipGroupVisibility();
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
                    updateWeekdayChipGroupVisibility();
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
                    updateWeekdayChipGroupVisibility();
                }
            }
        });

        setupDayChipListener(chipSun, 0);
        setupDayChipListener(chipMon, 1);
        setupDayChipListener(chipTue, 2);
        setupDayChipListener(chipWed, 3);
        setupDayChipListener(chipThu, 4);
        setupDayChipListener(chipFri, 5);
        setupDayChipListener(chipSat, 6);

        switchAutoPopup.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                habit.setAutoPopup(isChecked);
            }
        });
        
        tvStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });
        
        tvReminderTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog();
            }
        });
        
    }
    
    private void updateWeekdayChipGroupVisibility() {
        Log.d("NewHabitAimActivity", "Updating weekday chip group visibility");
        if (chipWeekly.isChecked()) {
            weekdaysChipGroup.setVisibility(View.VISIBLE);
        } else if (chipDaily.isChecked()) {
            // For daily habits, select all days by default
            boolean[] allDaysSelected = {true, true, true, true, true, true, true};
            habit.setWeekDays(allDaysSelected);
            
            // Update UI to reflect selection but hide the group
            chipSun.setChecked(true);
            chipMon.setChecked(true);
            chipTue.setChecked(true);
            chipWed.setChecked(true);
            chipThu.setChecked(true);
            chipFri.setChecked(true);
            chipSat.setChecked(true);
            
            weekdaysChipGroup.setVisibility(View.GONE);
        } else if (chipInterval.isChecked()) {
            // For interval, we don't use weekdays
            boolean[] noDaysSelected = {false, false, false, false, false, false, false};
            habit.setWeekDays(noDaysSelected);
            
            weekdaysChipGroup.setVisibility(View.GONE);
        }
    }
    
    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        
                        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.US);
                        String formattedDate = dateFormat.format(calendar.getTime());
                        tvStartDate.setText(formattedDate);
                        
                        habit.setStartDate(calendar.getTime());
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }
    
    private void showTimePickerDialog() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                        tvReminderTime.setText(time);
                        
                        habit.setReminder(time);
                    }
                },
                13, 0, true
        );
        timePickerDialog.show();
    }

    private void setupDayChipListener(final Chip chip, final int dayIndex) {
        chip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                boolean[] weekDays = habit.getWeekDays();
                if (weekDays == null) {
                    weekDays = new boolean[7];
                }
                weekDays[dayIndex] = isChecked;
                habit.setWeekDays(weekDays);
            }
        });
    }

    private void saveHabit() {
        // Update all habit data from UI input fields
        habit.setGoal(editTextGoal.getText().toString());
        habit.setGoalDays(spinnerGoalDays.getSelectedItem().toString());
        habit.setSection(spinnerSection.getSelectedItem().toString());
        
        // Handle frequency-related data
        String daily = getString(R.string.chip_daily);
        String weekly = getString(R.string.chip_weekly);
        String interval = getString(R.string.chip_interval);
        
        String frequency = null;
        if (chipDaily.isChecked()) frequency = daily;
        else if (chipWeekly.isChecked()) frequency = weekly;
        else if (chipInterval.isChecked()) frequency = interval;
        habit.setFrequency(frequency);
        
        // Ensure weekdays are properly collected
        boolean[] weekDays = new boolean[7];
        weekDays[0] = chipSun.isChecked();
        weekDays[1] = chipMon.isChecked();
        weekDays[2] = chipTue.isChecked();
        weekDays[3] = chipWed.isChecked();
        weekDays[4] = chipThu.isChecked();
        weekDays[5] = chipFri.isChecked();
        weekDays[6] = chipSat.isChecked();
        habit.setWeekDays(weekDays);
        
        // Use the correct method based on isEditing flag
        HabitActivity habitActivity = new HabitActivity();
        boolean success;
        
        if (isEditing) {
            // Update existing habit
            success = habitActivity.editHabit(this, habit);
        } else {
            // Create new habit
            int habitId = (int) habitActivity.saveHabit(habit);
            success = (habitId != -1);
            if (success) {
                // Update the habit with the new ID
                habit.setId(habitId);
            }
        }

        if (success) {

            Intent intent = new Intent(this, HabitActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Failed to save habit", Toast.LENGTH_SHORT).show();
        }
    }
}