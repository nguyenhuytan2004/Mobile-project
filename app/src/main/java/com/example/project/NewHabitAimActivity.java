package com.example.project;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
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
    
    private EditText editTextGoal;
    private TextView tvStartDate;
    private Spinner spinnerGoalDays;
    private Spinner spinnerSection;
    private TextView tvReminderTime;
    private TextView tvAddReminder;
    
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_habit_aim);

        habit = (Habit) getIntent().getSerializableExtra("habit");
        if (habit == null) {
            habit = new Habit();
        }

        initializeUI();
        setDefaultValues();
        setupListeners();
    }

    private void initializeUI() {
        btnBack = findViewById(R.id.btnBack);
        btnSave = findViewById(R.id.btnSave);
        switchAutoPopup = findViewById(R.id.switchAutoPopup);

        chipDaily = findViewById(R.id.chip8);
        chipWeekly = findViewById(R.id.chip9);
        chipInterval = findViewById(R.id.chip10);

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
        tvAddReminder = findViewById(R.id.tvAddReminder);
        
        calendar = Calendar.getInstance();
    }

    private void setDefaultValues() {
        chipDaily.setChecked(true);
        habit.setFrequency("Daily");

        Calendar calendar = Calendar.getInstance();
        Date today = calendar.getTime();
        habit.setStartDate(today);

        habit.setGoal("5 Page daily");
        habit.setGoalDays("Forever");
        habit.setSection("Others");
        habit.setReminder("13:00");

        switchAutoPopup.setChecked(false);
        habit.setAutoPopup(false);
        
        editTextGoal.setText("5 Page daily");
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.US);
        String formattedDate = dateFormat.format(calendar.getTime());
        tvStartDate.setText(formattedDate);
        
        String[] goalDaysOptions = {"7 days", "14 days", "30 days", "90 days", "Forever"};
        ArrayAdapter<String> goalDaysAdapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_spinner_item, goalDaysOptions);
        goalDaysAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGoalDays.setAdapter(goalDaysAdapter);
        spinnerGoalDays.setSelection(4);
        
        String[] sectionOptions = {"Sport", "Daily life", "Learning", "Others"};
        ArrayAdapter<String> sectionAdapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_spinner_item, sectionOptions);
        sectionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSection.setAdapter(sectionAdapter);
        spinnerSection.setSelection(3);
        
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
        
        tvAddReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog();
            }
        });
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
                weekDays[dayIndex] = isChecked;
                habit.setWeekDays(weekDays);
            }
        });
    }

    private void saveHabit() {
        habit.setGoal(editTextGoal.getText().toString());
        habit.setGoalDays(spinnerGoalDays.getSelectedItem().toString());
        habit.setSection(spinnerSection.getSelectedItem().toString());
        
        long habitId = new HabitActivity().saveHabit(habit);

        if (habitId != -1) {
            Toast.makeText(this, "Habit saved successfully!", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(this, HabitActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Error saving habit", Toast.LENGTH_SHORT).show();
        }
    }
}