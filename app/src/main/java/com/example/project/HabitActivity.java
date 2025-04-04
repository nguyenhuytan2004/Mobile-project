package com.example.project;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
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
    private Button btnAll, btnCompleted, btnPending, btnAddHabit;
    private ImageButton btnBack;
    ImageView focusTab, calendarTab, homeTab, matrixTab;
    private ArrayList<Habit> habitList;
    private HabitAdapter habitAdapter;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.habit);

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
    }

    private void initializeUI() {
        gridView = findViewById(R.id.gridView);
        btnAll = findViewById(R.id.btnAll);
        btnCompleted = findViewById(R.id.btnCompleted);
        btnPending = findViewById(R.id.btnPending);
        btnAddHabit = findViewById(R.id.button);
        btnBack = findViewById(R.id.imageButton4);
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

        // Back button listener
        btnBack.setOnClickListener(v -> finish());

        // Filter buttons listeners
        btnAll.setOnClickListener(v -> filterHabits("all"));
        btnCompleted.setOnClickListener(v -> filterHabits("life"));
        btnPending.setOnClickListener(v -> filterHabits("sport"));

        // GridView item click listener
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            Habit selectedHabit = habitList.get(position);
            // Show habit details
            Toast.makeText(HabitActivity.this, "Selected: " + selectedHabit.getName(), Toast.LENGTH_SHORT).show();
            
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
            addSampleHabitsToDatabase();
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

    private void addSampleHabitsToDatabase() {
        Log.d(TAG, "Adding sample habits to database");
        
        Habit habit1 = new Habit();
        habit1.setName("Reading");
        habit1.setQuote("A reader lives a thousand lives");
        habit1.setFrequency("Daily");
        habit1.setGoal("10 pages daily");
        habit1.setGoalDays("Forever");
        habit1.setSection("life");
        habit1.setWeekDays(new boolean[]{true, true, true, true, true, true, true});
        habit1.setReminder("08:00");
        saveHabit(habit1);

        Habit habit2 = new Habit();
        habit2.setName("Running");
        habit2.setQuote("Every step counts");
        habit2.setFrequency("Weekly");
        habit2.setGoal("5km per run");
        habit2.setGoalDays("90 days");
        habit2.setSection("sport");
        habit2.setWeekDays(new boolean[]{false, true, false, true, false, true, false});
        habit2.setReminder("17:30");
        saveHabit(habit2);
        
        Habit habit3 = new Habit();
        habit3.setName("Meditation");
        habit3.setQuote("Peace comes from within");
        habit3.setFrequency("Daily");
        habit3.setGoal("10 minutes daily");
        habit3.setGoalDays("30 days");
        habit3.setSection("life");
        habit3.setWeekDays(new boolean[]{true, true, true, true, true, true, true});
        habit3.setReminder("06:00");
        saveHabit(habit3);
    }

    // Save a new habit to the database
    @SuppressLint("RestrictedApi")
    public long saveHabit(Habit habit) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long newId = -1;

        try {
            ContentValues values = new ContentValues();
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

            newId = db.insert("tbl_habit", null, values);
            Log.d(TAG, "Habit saved with ID: " + newId);
        } catch (Exception e) {
            Log.e(TAG, "Error saving habit", e);
        }

        return newId;
    }

    // Get all habits from the database
    @SuppressLint("RestrictedApi")
    public List<Habit> getAllHabits() {
        List<Habit> habits = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try {
            Cursor cursor = db.query("tbl_habit", null, null, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Habit habit = new Habit();
                    
                    // Get column indexes to avoid issues with column order
                    int idIndex = cursor.getColumnIndex("id");
                    if (idIndex >= 0) habit.setId(cursor.getLong(idIndex));
                    
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
            Log.e(TAG, "Error getting habits", e);
        }

        return habits;
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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return sdf.format(date);
    }

    @SuppressLint("RestrictedApi")
    private Date stringToDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            return sdf.parse(dateStr);
        } catch (Exception e) {
            Log.e(TAG, "Error parsing date", e);
            return null;
        }
    }

    private void filterHabits(String filter) {
        ArrayList<Habit> filteredList = new ArrayList<>();
        
        if (filter.equals("all")) {
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