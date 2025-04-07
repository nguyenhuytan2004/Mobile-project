package com.example.project;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TaskStatistics extends AppCompatActivity {

    private static final String TAG = "TaskStatistics";

    // UI components
    private TextView tvCompletedTasksCount, tvCompletedFromYesterday;
    private TextView tvCompletionRate, tvCompletionRateYesterday;
    private TextView tvOverdueCount, tvOnTimeCount, tvUndateCount, tvUncompleteCount;
    private TextView tvPercentage, tvPercentage2;
    private TextView tvDate;
    private ProgressBar progressBar, progressBar2;
    private ImageView closeButton;

    // Data fields
    private int totalTasks = 0;
    private int completedTasks = 0;
    private int overdueTasks = 0;
    private int onTimeTasks = 0;
    private int undatedTasks = 0;
    private int uncompletedTasks = 0;

    // Date handling
    private Calendar currentDate = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_statistics);

        initializeViews();
        setupListeners();
        loadStatisticsForToday();
    }

    private void initializeViews() {
        // Card 1: Completed Tasks
        tvCompletedTasksCount = findViewById(R.id.textView30);
        tvCompletedFromYesterday = findViewById(R.id.textView29);

        // Card 2: Completion Rate
        tvCompletionRate = findViewById(R.id.textView12);
        tvCompletionRateYesterday = findViewById(R.id.textView11);

        // Card 3: Distribution
        tvOverdueCount = findViewById(R.id.textView15);
        tvOnTimeCount = findViewById(R.id.textView17);
        tvUndateCount = findViewById(R.id.textView19);
        tvUncompleteCount = findViewById(R.id.textView22);

        tvPercentage = findViewById(R.id.txtPercentage2);
        progressBar = findViewById(R.id.progressBar);

        // Date navigation
        tvDate = findViewById(R.id.textView8);

        // List stats
        tvPercentage2 = findViewById(R.id.txtPercentage3);
        progressBar2 = findViewById(R.id.progressBar2);

        // Close button
        closeButton = findViewById(R.id.close);
    }

    private void setupListeners() {
        // Close button
        closeButton.setOnClickListener(v -> finish());

        // Previous day button
        findViewById(R.id.textView7).setOnClickListener(v -> {
            currentDate.add(Calendar.DAY_OF_MONTH, -1);
            updateDateDisplay();
            loadStatisticsForDate(dateFormat.format(currentDate.getTime()));
        });

        // Next day button
        findViewById(R.id.textView9).setOnClickListener(v -> {
            currentDate.add(Calendar.DAY_OF_MONTH, 1);
            updateDateDisplay();
            loadStatisticsForDate(dateFormat.format(currentDate.getTime()));
        });
    }

    private void updateDateDisplay() {
        // Check if the date is today
        Calendar today = Calendar.getInstance();
        if (currentDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                currentDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
            tvDate.setText("Today");
        } else {
            SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
            tvDate.setText(displayFormat.format(currentDate.getTime()));
        }
    }

    private void loadStatisticsForToday() {
        currentDate = Calendar.getInstance();
        updateDateDisplay();
        loadStatisticsForDate(dateFormat.format(currentDate.getTime()));
    }

    private void loadStatisticsForDate(String date) {
        resetStatistics();

        DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
        SQLiteDatabase db = dbHelper.openDatabase();

        try {
            // Get user ID from session
            LoginSessionManager loginSessionManager = LoginSessionManager.getInstance(this);
            int userId = loginSessionManager.getUserId();

            // Get statistics for current date
            loadTaskStatistics(db, date, userId);

            // Get statistics for previous date for comparison
            Calendar previousDay = (Calendar) currentDate.clone();
            previousDay.add(Calendar.DAY_OF_MONTH, -1);
            String previousDate = dateFormat.format(previousDay.getTime());
            loadComparisonStatistics(db, previousDate, userId);

            // Update UI with statistics
            updateUI();

            // Load list-specific statistics
            loadListStatistics(db, date, userId);

        } catch (Exception e) {
            Log.e(TAG, "Error loading statistics: " + e.getMessage(), e);
        } finally {
            dbHelper.closeDatabase();
        }
    }

    private void resetStatistics() {
        totalTasks = 0;
        completedTasks = 0;
        overdueTasks = 0;
        onTimeTasks = 0;
        undatedTasks = 0;
        uncompletedTasks = 0;
    }

    private void loadTaskStatistics(SQLiteDatabase db, String date, int userId) {
        String startOfDay = date + " 00:00:00";
        String endOfDay = date + " 23:59:59";

        // Query to get all tasks for the day
        String query = "SELECT t.id, t.is_completed, r.date AS reminder_date " +
                "FROM tbl_task t " +
                "LEFT JOIN tbl_task_reminder r ON t.id = r.task_id " +
                "WHERE t.user_id = ? AND " +
                "(r.date BETWEEN ? AND ? OR " +
                "(t.created_at BETWEEN ? AND ? AND r.date IS NULL))";

        Cursor cursor = db.rawQuery(query, new String[]{
                String.valueOf(userId),
                startOfDay, endOfDay,
                startOfDay, endOfDay
        });

        if (cursor != null) {
            try {
                totalTasks = cursor.getCount();

                if (cursor.moveToFirst()) {
                    do {
                        int completedIndex = cursor.getColumnIndex("is_completed");
                        int reminderIndex = cursor.getColumnIndex("reminder_date");

                        boolean isCompleted = (completedIndex >= 0) && cursor.getInt(completedIndex) == 1;
                        String reminderDate = (reminderIndex >= 0 && !cursor.isNull(reminderIndex))
                                ? cursor.getString(reminderIndex) : null;

                        if (isCompleted) {
                            completedTasks++;

                            // Check if completed on time
                            if (reminderDate != null) {
                                try {
                                    Date dueDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                                            .parse(reminderDate);
                                    Date completionDate = Calendar.getInstance().getTime();

                                    if (completionDate.after(dueDate)) {
                                        overdueTasks++;
                                    } else {
                                        onTimeTasks++;
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing date: " + e.getMessage());
                                    onTimeTasks++; // Default to on-time if date parsing fails
                                }
                            } else {
                                undatedTasks++;
                            }
                        } else {
                            uncompletedTasks++;
                        }
                    } while (cursor.moveToNext());
                }
            } finally {
                cursor.close();
            }
        }
    }

    private int previousDayCompletedTasks = 0;
    private double previousDayCompletionRate = 0;

    private void loadComparisonStatistics(SQLiteDatabase db, String date, int userId) {
        String startOfDay = date + " 00:00:00";
        String endOfDay = date + " 23:59:59";

        // Query to get completed tasks for the previous day
        String query = "SELECT COUNT(*) as completed_count, " +
                "(SELECT COUNT(*) FROM tbl_task t2 " +
                "LEFT JOIN tbl_task_reminder r2 ON t2.id = r2.task_id " +
                "WHERE t2.user_id = ? AND " +
                "(r2.date BETWEEN ? AND ? OR " +
                "(t2.created_at BETWEEN ? AND ? AND r2.date IS NULL))) as total_count " +
                "FROM tbl_task t " +
                "LEFT JOIN tbl_task_reminder r ON t.id = r.task_id " +
                "WHERE t.user_id = ? AND t.is_completed = 1 AND " +
                "(r.date BETWEEN ? AND ? OR " +
                "(t.created_at BETWEEN ? AND ? AND r.date IS NULL))";

        Cursor cursor = db.rawQuery(query, new String[]{
                String.valueOf(userId), startOfDay, endOfDay, startOfDay, endOfDay,
                String.valueOf(userId), startOfDay, endOfDay, startOfDay, endOfDay
        });

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    int completedIndex = cursor.getColumnIndex("completed_count");
                    int totalIndex = cursor.getColumnIndex("total_count");

                    previousDayCompletedTasks = cursor.getInt(completedIndex);
                    int previousDayTotalTasks = cursor.getInt(totalIndex);

                    if (previousDayTotalTasks > 0) {
                        previousDayCompletionRate = (previousDayCompletedTasks * 100.0) / previousDayTotalTasks;
                    }
                }
            } finally {
                cursor.close();
            }
        }
    }

    private void loadListStatistics(SQLiteDatabase db, String date, int userId) {
        String startOfDay = date + " 00:00:00";
        String endOfDay = date + " 23:59:59";

        // Query to get statistics by list
        String query = "SELECT l.id, l.name, COUNT(t.id) as total_tasks, " +
                "SUM(CASE WHEN t.is_completed = 1 THEN 1 ELSE 0 END) as completed_tasks " +
                "FROM tbl_list l " +
                "JOIN tbl_category c ON l.id = c.list_id " +
                "LEFT JOIN tbl_task t ON c.id = t.category_id " +
                "LEFT JOIN tbl_task_reminder r ON t.id = r.task_id " +
                "WHERE t.user_id = ? AND " +
                "(r.date BETWEEN ? AND ? OR " +
                "(t.created_at BETWEEN ? AND ? AND r.date IS NULL)) " +
                "GROUP BY l.id " +
                "ORDER BY completed_tasks DESC " +
                "LIMIT 1"; // Get the list with most completed tasks

        Cursor cursor = db.rawQuery(query, new String[]{
                String.valueOf(userId), startOfDay, endOfDay, startOfDay, endOfDay
        });

        if (cursor != null && cursor.moveToFirst()) {
            try {
                int nameIndex = cursor.getColumnIndex("name");
                int completedIndex = cursor.getColumnIndex("completed_tasks");

                if (nameIndex >= 0 && completedIndex >= 0) {
                    String listName = cursor.getString(nameIndex);
                    int listCompletedTasks = cursor.getInt(completedIndex);

                    // Update list statistics UI
                    TextView tvListName = findViewById(R.id.textView20);
                    TextView tvListCompletedTasks = findViewById(R.id.textView26);
                    TextView tvListCompletedTasksCircle = findViewById(R.id.txtPercentage3);

                    tvListName.setText(listName);
                    tvListCompletedTasks.setText(String.valueOf(listCompletedTasks));
                    tvListCompletedTasksCircle.setText(String.valueOf(listCompletedTasks));
                }
            } finally {
                cursor.close();
            }
        }
    }

    private void updateUI() {
        // Update completed tasks
        tvCompletedTasksCount.setText(String.valueOf(completedTasks));
        int taskDifference = completedTasks - previousDayCompletedTasks;
        tvCompletedFromYesterday.setText(Math.abs(taskDifference) +
                (taskDifference >= 0 ? " more than" : " fewer than") + " yesterday");

        // Update completion rate
        double completionRate = (totalTasks > 0) ? (completedTasks * 100.0 / totalTasks) : 0;
        String formattedRate = String.format(Locale.getDefault(), "%.2f%%", completionRate);
        tvCompletionRate.setText(formattedRate);

        double rateDifference = completionRate - previousDayCompletionRate;
        tvCompletionRateYesterday.setText(String.format(Locale.getDefault(), "%.2f%%", Math.abs(rateDifference)) +
                (rateDifference >= 0 ? " more than" : " less than") + " yesterday");

        // Update progress bar
        progressBar.setProgress((int) completionRate);
        tvPercentage.setText(formattedRate);

        // Update distribution counts
        tvOverdueCount.setText(String.valueOf(overdueTasks));
        tvOnTimeCount.setText(String.valueOf(onTimeTasks));
        tvUndateCount.setText(String.valueOf(undatedTasks));
        tvUncompleteCount.setText(String.valueOf(uncompletedTasks));
    }
}