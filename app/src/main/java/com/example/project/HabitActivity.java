package com.example.project;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HabitActivity extends AppCompatActivity {

    private static final String TAG = "HabitActivity";
    private GridView gridView;
    private Button btnAll, btnDailyLife, btnSport, btnAddHabit, btnLearning;
    ImageView focusTab, calendarTab, homeTab, matrixTab;
    private ArrayList<Habit> habitList;
    private HabitAdapter habitAdapter;
    private DatabaseHelper dbHelper;
    
    // Constants for habit sections to avoid hardcoding strings
    private String SECTION_ALL;
    private String SECTION_DAILY_LIFE;
    private String SECTION_SPORT;
    private String SECTION_LEARNING;
    private String SECTION_OTHERS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.habit);
        
        // Initialize section strings from resources
        initSectionStrings();

        // Initialize database helper
        dbHelper = DatabaseHelper.getInstance(this);
        SQLiteDatabase db = DatabaseHelper.getInstance(this).openDatabase();
        // Initialize UI components
        initializeUI();
        
        // Set click listeners
        setupClickListeners();

        // Initialize habit list
        habitList = new ArrayList<>();

        // Set up adapter
        habitAdapter = new HabitAdapter(this, habitList);
        gridView.setAdapter(habitAdapter);

        // Load habits from database
        loadHabitsFromDatabase();

        // Create notification channel
        HabitNotificationHelper.createNotificationChannel(this);

        // Schedule all habit reminders
        HabitNotificationHelper.scheduleHabitReminders(this);
    }
    
    // Initialize section strings from resources to support multiple languages
    private void initSectionStrings() {
        SECTION_ALL = "all"; // This is a special filter value, not displayed
        // Get localized section names from resources
        SECTION_DAILY_LIFE = getString(R.string.btn_daily_life);
        SECTION_SPORT = getString(R.string.btn_sport);
        SECTION_LEARNING = getString(R.string.btn_learning);
        SECTION_OTHERS = getString(R.string.others);
    }

    private void initializeUI() {
        gridView = findViewById(R.id.gridView);
        btnAll = findViewById(R.id.btnAll);
        btnDailyLife = findViewById(R.id.btnDailyLife);
        btnSport = findViewById(R.id.btnSport);
        btnLearning = findViewById(R.id.btnLearning);
        btnAddHabit = findViewById(R.id.button);
        calendarTab = findViewById(R.id.calendarTab2);
        focusTab = findViewById(R.id.focusTab);
        homeTab = findViewById(R.id.homeTab);
        matrixTab = findViewById(R.id.matrixTabHabit);
    }

    private void setupClickListeners() {
        // Navigation tab listeners
        focusTab.setOnClickListener(v -> {
            Intent intent = new Intent(HabitActivity.this, FocusTab.class);
            startActivity(intent);
        });

        calendarTab.setOnClickListener(v -> {
            Intent intent = new Intent(HabitActivity.this, CalendarTab.class);
            startActivity(intent);
        });

        matrixTab.setOnClickListener(v -> {
            Intent intent = new Intent(HabitActivity.this, Matrix_Eisenhower.class);
            startActivity(intent);
        });

        homeTab.setOnClickListener(v -> {
            Intent intent = new Intent(HabitActivity.this, MainActivity.class);
            startActivity(intent);
        });

        // Add new habit button listener
        btnAddHabit.setOnClickListener(v -> {
            Intent intent = new Intent(HabitActivity.this, NewHabitActivity.class);
            startActivity(intent);
        });

        // Filter buttons listeners - use constants instead of hardcoded strings
        btnAll.setOnClickListener(v -> filterHabits(SECTION_ALL));
        btnDailyLife.setOnClickListener(v -> filterHabits(SECTION_DAILY_LIFE));
        btnSport.setOnClickListener(v -> filterHabits(SECTION_SPORT));
        btnLearning.setOnClickListener(v -> filterHabits(SECTION_LEARNING));

        // GridView item click listener
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            Habit selectedHabit = habitList.get(position);
            // Show habit details with a localized message format
            String message = getString(R.string.selected_habit, selectedHabit.getName());
            Toast.makeText(HabitActivity.this, message, Toast.LENGTH_SHORT).show();
            
            // Here you can open a detail activity for the selected habit
            // Intent intent = new Intent(HabitActivity.this, HabitDetailActivity.class);
            // intent.putExtra("habitId", selectedHabit.getId());
            // startActivity(intent);
        });
    }

    private void loadHabitsFromDatabase() {
        // Clear existing list
        habitList.clear();

        // Get habits from database
        List<Habit> habits = getAllHabits();
        habitList.addAll(habits);

        // If no habits found, add sample habits
        if (habitList.isEmpty()) {
            habitList.addAll(getAllHabits());
        }

        // Notify adapter
        habitAdapter.notifyDataSetChanged();
        
        // Log loaded habits for debugging
        Log.d(TAG, "Loaded " + habitList.size() + " habits from database");
        for (Habit habit : habitList) {
            Log.d(TAG, "Habit: " + habit.getName() + ", Section: " + habit.getSection());
        }
    }

    // Update the saveHabit method to also take a context parameter for consistency
    @SuppressLint("RestrictedApi")
    public long saveHabit(Context context, Habit habit) {
        int userId = LoginSessionManager.getInstance(context).getUserId();
        SQLiteDatabase db = DatabaseHelper.getInstance(context).openDatabase();
        int newId = -1;

        try {
            ContentValues values = new ContentValues();
            values.put("user_id", userId);
            values.put("name", habit.getName());
            values.put("quote", habit.getQuote());
            values.put("frequency", habit.getFrequency());
            values.put("week_days", booleanArrayToString(habit.getWeekDays()));
            values.put("goal", habit.getGoal());
            values.put("start_date", dateToString(habit.getStartDate()));
            values.put("goal_days", habit.getGoalDays());
            values.put("section", habit.getSection());
            values.put("reminder", habit.getReminder());
            values.put("auto_popup", habit.isAutoPopup() ? 1 : 0);

            newId = (int) db.insert("tbl_habit", null, values);
            Log.d(TAG, "Habit saved with ID: " + newId);
        } catch (Exception e) {
            Log.e(TAG, "Error saving habit", e);
        } finally {
            DatabaseHelper.getInstance(context).closeDatabase();
        }

        return newId;
    }

    // Keep the original method for backward compatibility
    @SuppressLint("RestrictedApi")
    public long saveHabit(Habit habit) {
        return saveHabit(this, habit);
    }

    // Get habits from the database for the current user only
    @SuppressLint("RestrictedApi")
    public List<Habit> getAllHabits() {
        int userId = LoginSessionManager.getInstance(this).getUserId();
        List<Habit> habits = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try {
            // Add WHERE clause to filter by user_id
            String selection = "user_id = ?";
            String[] selectionArgs = {String.valueOf(userId)};

            Cursor cursor = db.query("tbl_habit", null, selection, selectionArgs, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Habit habit = new Habit();

                    // Get column indexes to avoid issues with column order
                    int idIndex = cursor.getColumnIndex("id");
                    if (idIndex >= 0) habit.setId(cursor.getInt(idIndex));

                    int nameIndex = cursor.getColumnIndex("name");
                    if (nameIndex >= 0) habit.setName(cursor.getString(nameIndex));

                    int quoteIndex = cursor.getColumnIndex("quote");
                    if (quoteIndex >= 0) habit.setQuote(cursor.getString(quoteIndex));

                    int frequencyIndex = cursor.getColumnIndex("frequency");
                    if (frequencyIndex >= 0) habit.setFrequency(cursor.getString(frequencyIndex));

                    int weekDaysIndex = cursor.getColumnIndex("week_days");
                    if (weekDaysIndex >= 0) habit.setWeekDays(stringToBooleanArray(cursor.getString(weekDaysIndex)));

                    int goalIndex = cursor.getColumnIndex("goal");
                    if (goalIndex >= 0) habit.setGoal(cursor.getString(goalIndex));

                    int startDateIndex = cursor.getColumnIndex("start_date");
                    if (startDateIndex >= 0) habit.setStartDate(stringToDate(cursor.getString(startDateIndex)));

                    int goalDaysIndex = cursor.getColumnIndex("goal_days");
                    if (goalDaysIndex >= 0) habit.setGoalDays(cursor.getString(goalDaysIndex));

                    int sectionIndex = cursor.getColumnIndex("section");
                    if (sectionIndex >= 0) habit.setSection(cursor.getString(sectionIndex));

                    int reminderIndex = cursor.getColumnIndex("reminder");
                    if (reminderIndex >= 0) habit.setReminder(cursor.getString(reminderIndex));

                    int autoPopupIndex = cursor.getColumnIndex("auto_popup");
                    if (autoPopupIndex >= 0) habit.setAutoPopup(cursor.getInt(autoPopupIndex) == 1);

                    habits.add(habit);
                } while (cursor.moveToNext());
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting habits for user " + userId, e);
        }

        return habits;
    }

    /**
     * Update an existing habit in the database
     * @param context The context to use for database operations
     * @param habit The habit to update
     * @return true if update was successful, false otherwise
     */
    @SuppressLint("RestrictedApi")
    public boolean editHabit(Context context, Habit habit) {
        if (habit == null || habit.getId() <= 0) {
            Log.e(TAG, "Cannot edit habit: Invalid habit or habit ID");
            return false;
        }

        SQLiteDatabase db = DatabaseHelper.getInstance(context).openDatabase();
        boolean success = false;

        try {
            ContentValues values = new ContentValues();
            // We don't update user_id as that shouldn't change
            values.put("name", habit.getName());
            values.put("quote", habit.getQuote());
            values.put("frequency", habit.getFrequency());
            values.put("week_days", booleanArrayToString(habit.getWeekDays()));
            values.put("goal", habit.getGoal());
            values.put("start_date", dateToString(habit.getStartDate()));
            values.put("goal_days", habit.getGoalDays());
            values.put("section", habit.getSection());
            values.put("reminder", habit.getReminder());
            values.put("auto_popup", habit.isAutoPopup() ? 1 : 0);

            // Update the record where id matches
            int rowsUpdated = db.update(
                "tbl_habit", 
                values, 
                "id = ?", 
                new String[] { String.valueOf(habit.getId()) }
            );

            success = (rowsUpdated > 0);
            Log.d(TAG, "Habit updated, ID: " + habit.getId() + ", Success: " + success);
            
            // If reminder changed, reschedule notifications
            HabitNotificationHelper.scheduleHabitReminders(context);
            
        } catch (Exception e) {
            Log.e(TAG, "Error updating habit", e);
        } finally {
            DatabaseHelper.getInstance(context).closeDatabase();
        }

        return success;
    }

    // Helper methods for data conversion
    private String booleanArrayToString(boolean[] array) {
        if (array == null) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < array.length; i++) {
            sb.append(array[i] ? "1" : "0");
            if (i < array.length - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private boolean[] stringToBooleanArray(String str) {
        if (str == null || str.equals("[]")) return new boolean[7];

        str = str.replace("[", "").replace("]", "");
        String[] parts = str.split(",");
        boolean[] result = new boolean[parts.length];

        for (int i = 0; i < parts.length; i++) {
            result[i] = parts[i].equals("1");
        }
        return result;
    }

    private String dateToString(Date date) {
        if (date == null) return "";
        // Use device locale for consistent date formatting
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(date);
    }

    @SuppressLint("RestrictedApi")
    private Date stringToDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
            // Use device locale for consistent date parsing
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return sdf.parse(dateStr);
        } catch (Exception e) {
            Log.e(TAG, "Error parsing date", e);
            return null;
        }
    }

    private void filterHabits(String filter) {
        ArrayList<Habit> filteredList = new ArrayList<>();
        
        if (filter.equals(SECTION_ALL)) {
            filteredList.addAll(habitList);
        } else {
            for (Habit habit : habitList) {
                if (habit.getSection() != null && habit.getSection().equalsIgnoreCase(filter)) {
                    filteredList.add(habit);
                }
            }
        }
        
        // Update the adapter with filtered data
        habitAdapter.updateData(filteredList);
        
        // Log filtered habits for debugging
        Log.d(TAG, "Filtered to " + filteredList.size() + " habits for filter: " + filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this screen
        loadHabitsFromDatabase();
    }
}