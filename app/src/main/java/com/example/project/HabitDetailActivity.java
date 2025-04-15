package com.example.project;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HabitDetailActivity extends AppCompatActivity {

    private ImageButton btnBack, btnEdit, btnDelete;
    private TextView tvHabitName, tvQuote, tvFrequency, tvGoal;
    private TextView tvGoalDays, tvStartDate, tvSection, tvReminderTime;
    private Switch switchAutoPopup;
    private TextView labelSelectedDays;
    private ChipGroup chipGroupDays;
    private Chip chipSun, chipMon, chipTue, chipWed, chipThu, chipFri, chipSat;
    
    private long habitId;
    private Habit habit;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.habit_detail);
        
        // Get habit ID from intent
        habitId = getIntent().getIntExtra("habit_id", -1);
        if (habitId == -1) {
            Toast.makeText(this, "Error: Habit not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Initialize database helper
        dbHelper = DatabaseHelper.getInstance(this);
        
        // Initialize UI components
        initializeUI();
        
        // Load habit details
        loadHabitDetails();
        
        // Setup listeners
        setupListeners();
    }
    
    private void initializeUI() {
        btnBack = findViewById(R.id.btnBack);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);
        
        tvHabitName = findViewById(R.id.tvHabitName);
        tvQuote = findViewById(R.id.tvQuote);
        tvFrequency = findViewById(R.id.tvFrequency);
        tvGoal = findViewById(R.id.tvGoal);
        tvGoalDays = findViewById(R.id.tvGoalDays);
        tvStartDate = findViewById(R.id.tvStartDate);
        tvSection = findViewById(R.id.tvSection);
        tvReminderTime = findViewById(R.id.tvReminderTime);
        
        switchAutoPopup = findViewById(R.id.switchAutoPopup);
        
        labelSelectedDays = findViewById(R.id.labelSelectedDays);
        chipGroupDays = findViewById(R.id.chipGroupDays);
        
        chipSun = findViewById(R.id.chipSun);
        chipMon = findViewById(R.id.chipMon);
        chipTue = findViewById(R.id.chipTue);
        chipWed = findViewById(R.id.chipWed);
        chipThu = findViewById(R.id.chipThu);
        chipFri = findViewById(R.id.chipFri);
        chipSat = findViewById(R.id.chipSat);
    }
    
    @SuppressLint("RestrictedApi")
    private void loadHabitDetails() {
        SQLiteDatabase db = DatabaseHelper.getInstance(this).openDatabase();
        
        try {
            // Query the database for the habit with this ID
            Cursor cursor = db.rawQuery("SELECT * FROM tbl_habit WHERE id = ?", 
                    new String[]{String.valueOf(habitId)});
            
            if (cursor != null && cursor.moveToFirst()) {
                habit = new Habit();

                int idIndex = cursor.getColumnIndex("id");
                habit.setId(idIndex != -1 ? cursor.getInt(idIndex) : -1);

                int nameIndex = cursor.getColumnIndex("name");
                habit.setName(nameIndex != -1 ? cursor.getString(nameIndex) : "");

                int quoteIndex = cursor.getColumnIndex("quote");
                habit.setQuote(quoteIndex != -1 ? cursor.getString(quoteIndex) : "");

                int frequencyIndex = cursor.getColumnIndex("frequency");
                habit.setFrequency(frequencyIndex != -1 ? cursor.getString(frequencyIndex) : "");

                int goalIndex = cursor.getColumnIndex("goal");
                habit.setGoal(goalIndex != -1 ? cursor.getString(goalIndex) : "");

                int goalDaysIndex = cursor.getColumnIndex("goal_days");
                habit.setGoalDays(goalDaysIndex != -1 ? cursor.getString(goalDaysIndex) : "");

                // Parse date from database
                int startDateIndex = cursor.getColumnIndex("start_date");
                String startDateStr = startDateIndex != -1 ? cursor.getString(startDateIndex) : null;
                if (startDateStr != null && !startDateStr.isEmpty()) {
                    habit.setStartDate(stringToDate(startDateStr));
                }

                int sectionIndex = cursor.getColumnIndex("section");
                habit.setSection(sectionIndex != -1 ? cursor.getString(sectionIndex) : "");

                int reminderIndex = cursor.getColumnIndex("reminder");
                habit.setReminder(reminderIndex != -1 ? cursor.getString(reminderIndex) : "");

                // Parse auto popup as boolean (0/1)
                int autoPopupIndex = cursor.getColumnIndex("auto_popup");
                int autoPopupValue = autoPopupIndex != -1 ? cursor.getInt(autoPopupIndex) : 0;
                habit.setAutoPopup(autoPopupValue == 1);

                // Parse weekdays boolean array from string
                int weekDaysIndex = cursor.getColumnIndex("week_days");
                String weekDaysStr = weekDaysIndex != -1 ? cursor.getString(weekDaysIndex) : null;
                boolean[] weekDays = stringToBooleanArray(weekDaysStr);
                habit.setWeekDays(weekDays);
                // Close the cursor

                cursor.close();
                updateUI();
            } else {
                // No habit found with this ID
                Toast.makeText(this, "Error: Habit" +habitId + " not found in database", Toast.LENGTH_SHORT).show();
                finish();
            }
            
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error loading habit details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        } finally {
            dbHelper.closeDatabase();
        }
    }
    
    private void updateUI() {
        if (habit == null) return;
        
        // Set basic habit details
        tvHabitName.setText(habit.getName());
        tvQuote.setText(habit.getQuote());
        tvFrequency.setText(habit.getFrequency());
        tvGoal.setText(habit.getGoal());
        tvGoalDays.setText(habit.getGoalDays());
        tvSection.setText(habit.getSection());
        tvReminderTime.setText(habit.getReminder());
        
        // Format and set date
        if (habit.getStartDate() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.US);
            tvStartDate.setText(dateFormat.format(habit.getStartDate()));
        }
        
        // Set auto popup switch
        switchAutoPopup.setChecked(habit.isAutoPopup());
        
        // Handle weekday chips based on frequency
        if ("Weekly".equals(habit.getFrequency())) {
            labelSelectedDays.setVisibility(View.VISIBLE);
            chipGroupDays.setVisibility(View.VISIBLE);
            
            // Set chip checked state based on weekdays array
            boolean[] weekDays = habit.getWeekDays();
            if (weekDays != null) {
                chipSun.setChecked(weekDays[0]);
                chipMon.setChecked(weekDays[1]);
                chipTue.setChecked(weekDays[2]);
                chipWed.setChecked(weekDays[3]);
                chipThu.setChecked(weekDays[4]);
                chipFri.setChecked(weekDays[5]);
                chipSat.setChecked(weekDays[6]);
            }
        } else {
            labelSelectedDays.setVisibility(View.GONE);
            chipGroupDays.setVisibility(View.GONE);
        }
    }
    
    private void setupListeners() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the edit activity
                Intent intent = new Intent(HabitDetailActivity.this, NewHabitAimActivity.class);
                intent.putExtra("habit", habit);
                intent.putExtra("isEditing", true);
                startActivity(intent);
                finish(); // Close this activity as we'll return to HabitActivity after edit
            }
        });
        
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog();
            }
        });
    }
    
    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Habit");
        builder.setMessage("Are you sure you want to delete this habit?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteHabit();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void deleteHabit() {
        SQLiteDatabase db = dbHelper.openDatabase();
        
        try {
            int rowsAffected = db.delete("tbl_habit", "id = ?", new String[]{String.valueOf(habitId)});
            
            if (rowsAffected > 0) {
                Toast.makeText(this, "Habit deleted successfully", Toast.LENGTH_SHORT).show();
                // Return to habit list
                Intent intent = new Intent(HabitDetailActivity.this, HabitActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Failed to delete habit", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error deleting habit: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            dbHelper.closeDatabase();
        }
    }
    
    // Helper method to convert date string to Date object
    @SuppressLint("RestrictedApi")
    private Date stringToDate(String dateStr) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            return dateFormat.parse(dateStr);
        } catch (Exception e) {
            return new Date(); // Return current date if parsing fails
        }
    }
    
    // Helper method to convert string to boolean array
    private boolean[] stringToBooleanArray(String str) {
        boolean[] result = new boolean[7];
        if (str == null || str.isEmpty() || str.length() < 7) {
            return result;
        }
        
        for (int i = 0; i < 7 && i < str.length(); i++) {
            result[i] = str.charAt(i) == '1';
        }
        
        return result;
    }
}