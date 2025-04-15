package com.example.project;

import android.app.DatePickerDialog;
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
import java.util.HashMap;
import java.util.Locale;

public class TaskStatistics extends AppCompatActivity {

    private static final String TAG = "TaskStatistics";

    // UI components
    private TextView tvCompletedTasksCount, tvCompletedFromYesterday;
    private TextView tvCompletionRate, tvTotalTask;
    private TextView tvOverdueCount, tvOnTimeCount, tvUndateCount, tvUncompleteCount;
    private TextView tv_week, tv_month, tv_day, tv_overview;
    private TextView tvPercentage;
    private TextView tvDate;
    private ProgressBar progressBar;
    private ImageView closeButton;
    private TextView prevButton, nextButton;

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
    // Get current user's ID once in a class field
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_statistics);

        // Get the current user ID once
        currentUserId = LoginSessionManager.getInstance(this).getUserId();
        
        initializeViews();
        setupListeners();
        loadStatisticsForToday();
    }

    private void initializeViews() {
        // Card 0: Overview
        tv_overview = findViewById(R.id.tv_overview);
        tv_day = findViewById(R.id.tv_day);
        tv_week = findViewById(R.id.tv_week);
        tv_month = findViewById(R.id.tv_month);

        // Card 1: Completed Tasks
        tvCompletedTasksCount = findViewById(R.id.textView30);
        tvCompletedFromYesterday = findViewById(R.id.textView29);

        // Card 2: Completion Rate
        tvCompletionRate = findViewById(R.id.textView12);
        tvTotalTask = findViewById(R.id.textView11);

        // Card 3: Distribution
        tvOverdueCount = findViewById(R.id.textView15);
        tvOnTimeCount = findViewById(R.id.textView17);
        tvUndateCount = findViewById(R.id.textView19);
        tvUncompleteCount = findViewById(R.id.textView22);

        tvPercentage = findViewById(R.id.txtPercentage2);
        progressBar = findViewById(R.id.progressBar);

        // Date navigation
        tvDate = findViewById(R.id.textView8);


        // Close button
        closeButton = findViewById(R.id.close);

        // Navigation buttons
        prevButton = findViewById(R.id.textView7);
        nextButton = findViewById(R.id.textView9);
    }

    private void setupListeners() {
        // Close button
        closeButton.setOnClickListener(v -> finish());
        // Date navigation
        tvDate.setOnClickListener(v -> {
            // Show date picker dialog
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                currentDate.set(year, month, dayOfMonth);
                updateDateDisplay();
                loadStatisticsForToday();
            }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });
        // Load statistics for the week
        findViewById(R.id.tv_week).setOnClickListener(v -> {
            loadStatisticsForWeek();
            updateWeekDisplay();
            updateUI();
        });
        findViewById(R.id.tv_day).setOnClickListener(v -> {
            loadStatisticsForToday();
            updateDayDisplay();
            updateUI();
        });
        // Load statistics for the month
        findViewById(R.id.tv_month).setOnClickListener(v -> {
            loadStatisticsForMonth();
            updateMonthDisplay();
            updateUI();
        });
        // Load all statistics
        findViewById(R.id.tv_overview).setOnClickListener(v -> {
            loadStatisticsforForAll();
            updateOverviewDisplay();
            updateUI();
        });

        findViewById(R.id.textView7).setOnClickListener(v -> { // Previous button
            if (tv_week.getCurrentTextColor() == getResources().getColor(R.color.statistics_blue)) {
                // In week mode, go back one week
                currentDate.add(Calendar.WEEK_OF_YEAR, -1);
                loadStatisticsForWeek();
                updateWeekDisplay();
                updateUI();
            } else if (tv_day.getCurrentTextColor() == getResources().getColor(R.color.statistics_blue)) {
                // In day mode, go back one day
                currentDate.add(Calendar.DAY_OF_MONTH, -1);
                loadStatisticsForToday();
                updateDayDisplay();
                updateUI();
            } else if (tv_month.getCurrentTextColor() == getResources().getColor(R.color.statistics_blue)) {
                // In month mode, go back one month
                currentDate.add(Calendar.MONTH, -1);
                loadStatisticsForMonth();
                updateMonthDisplay();
                updateUI();
            }
        });

        findViewById(R.id.textView9).setOnClickListener(v -> { // Next button
            if (tv_week.getCurrentTextColor() == getResources().getColor(R.color.statistics_blue)) {
                // In week mode, go forward one week
                currentDate.add(Calendar.WEEK_OF_YEAR, 1);
                loadStatisticsForWeek();
                updateWeekDisplay();
                updateUI();
            } else if (tv_day.getCurrentTextColor() == getResources().getColor(R.color.statistics_blue)) {
                // In day mode, go forward one day
                currentDate.add(Calendar.DAY_OF_MONTH, 1);
                loadStatisticsForToday();
                updateDayDisplay();
                updateUI();
            } else if (tv_month.getCurrentTextColor() == getResources().getColor(R.color.statistics_blue)) {
                // In month mode, go forward one month
                currentDate.add(Calendar.MONTH, 1);
                loadStatisticsForMonth();
                updateMonthDisplay();
                updateUI();
            }
        });
    }

    // Update the updateDateDisplay method
    private void updateDateDisplay() {
        // Check if the date is today
        Calendar today = Calendar.getInstance();
        if (currentDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                currentDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
            tvDate.setText(getString(R.string.stats_today));
        } else {
            SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
            tvDate.setText(displayFormat.format(currentDate.getTime()));
        }
    }

    // Updated updateWeekDisplay method
    private void updateWeekDisplay() {
        // Update tab colors - set selected tab to blue, others to gray
        tv_week.setTextColor(getResources().getColor(R.color.statistics_blue));
        tv_day.setTextColor(getResources().getColor(R.color.gray));
        tv_month.setTextColor(getResources().getColor(R.color.gray));
        tv_overview.setTextColor(getResources().getColor(R.color.gray));
        
        // Calculate the week number based on the selected date (currentDate)
        int weekOfYear = currentDate.get(Calendar.WEEK_OF_YEAR);
        
        // Update the date display to show Week #
        tvDate.setText(getString(R.string.stats_week_number, weekOfYear));

        // Show navigation buttons
        prevButton.setVisibility(View.VISIBLE);
        nextButton.setVisibility(View.VISIBLE);
    }

    private void updateDayDisplay() {
        tv_day.setTextColor(getResources().getColor(R.color.statistics_blue));
        tv_week.setTextColor(getResources().getColor(R.color.gray));
        tv_month.setTextColor(getResources().getColor(R.color.gray));
        tv_overview.setTextColor(getResources().getColor(R.color.gray));
        
        // Show navigation buttons
        prevButton.setVisibility(View.VISIBLE);
        nextButton.setVisibility(View.VISIBLE);
        
        updateDateDisplay(); // Uses existing function to show the correct date
    }

    // Updated updateMonthDisplay method
    private void updateMonthDisplay() {
        tv_month.setTextColor(getResources().getColor(R.color.statistics_blue));
        tv_day.setTextColor(getResources().getColor(R.color.gray));
        tv_week.setTextColor(getResources().getColor(R.color.gray));
        tv_overview.setTextColor(getResources().getColor(R.color.gray));

        // Use currentDate instead of Calendar.getInstance()
        int month = currentDate.get(Calendar.MONTH) + 1; // Months are 0-indexed
        int year = currentDate.get(Calendar.YEAR);

        // Update the date display to show current month name and year
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        tvDate.setText(monthFormat.format(currentDate.getTime()));
        
        // Show navigation buttons
        prevButton.setVisibility(View.VISIBLE);
        nextButton.setVisibility(View.VISIBLE);
    }

    // Updated updateOverviewDisplay method
    private void updateOverviewDisplay() {
        tv_overview.setTextColor(getResources().getColor(R.color.statistics_blue));
        tv_day.setTextColor(getResources().getColor(R.color.gray));
        tv_week.setTextColor(getResources().getColor(R.color.gray));
        tv_month.setTextColor(getResources().getColor(R.color.gray));
        
        // Hide navigation buttons for overview
        prevButton.setVisibility(View.GONE);
        nextButton.setVisibility(View.GONE);
        
        // Set title for overview
        tvDate.setText(getString(R.string.stats_all_time));
    }

    private void resetStatistics() {
        totalTasks = 0;
        completedTasks = 0;
        overdueTasks = 0;
        onTimeTasks = 0;
        undatedTasks = 0;
        uncompletedTasks = 0;
    }

    private int previousDayCompletedTasks = 0;
    private double previousDayCompletionRate = 0;


    /**
     * Get detailed completion statistics in a date range for a specific user
     * @param context Android context
     * @param startDate Start date (yyyy-MM-dd)
     * @param endDate End date (yyyy-MM-dd)
     * @param userId The ID of the user to filter tasks
     * @return Map<String, Integer> with keys:
     *         total_tasks, completed_tasks, completed_on_time,
     *         completed_late, completed_without_deadline, uncompleted_tasks
     */
    public HashMap<String, Integer> getDetailedTaskStats(Context context, String startDate, String endDate, int userId) {
        SQLiteDatabase db = DatabaseHelper.getInstance(context).openDatabase();
        HashMap<String, Integer> stats = new HashMap<>();

        // Initialize all stats
        stats.put("total_tasks", 0);
        stats.put("completed_tasks", 0);
        stats.put("completed_on_time", 0);
        stats.put("completed_late", 0);
        stats.put("completed_without_deadline", 0);
        stats.put("uncompleted_tasks", 0);

        String query =
                "SELECT t.is_completed, t.completion_datetime, r.date AS deadline_date, r.time AS deadline_time " +
                        "FROM tbl_task t " +
                        "LEFT JOIN tbl_task_reminder r ON t.id = r.task_id " +
                        "WHERE t.user_id = ? AND (" +
                        "      date(substr(t.completion_datetime, 1, 10)) BETWEEN ? AND ? OR " +
                        "      (t.is_completed = 0 AND date('now') BETWEEN ? AND ?))";

        Cursor cursor = null;

        try {
            cursor = db.rawQuery(query, new String[]{
                    String.valueOf(userId), startDate, endDate, startDate, endDate
            });

            while (cursor.moveToNext()) {
                boolean isCompleted = cursor.getInt(0) == 1;
                String completionDatetime = cursor.getString(1);
                String deadlineDate = cursor.getString(2);
                String deadlineTime = cursor.getString(3);

                stats.put("total_tasks", stats.get("total_tasks") + 1);

                if (isCompleted) {
                    stats.put("completed_tasks", stats.get("completed_tasks") + 1);

                    if (deadlineDate == null) {
                        stats.put("completed_without_deadline", stats.get("completed_without_deadline") + 1);
                    } else {
                        String deadline = deadlineDate + " " + (deadlineTime != null ? deadlineTime : "00:00:00");
                        if (completionDatetime.compareTo(deadline) <= 0) {
                            stats.put("completed_on_time", stats.get("completed_on_time") + 1);
                        } else {
                            stats.put("completed_late", stats.get("completed_late") + 1);
                        }
                    }

                } else {
                    stats.put("uncompleted_tasks", stats.get("uncompleted_tasks") + 1);
                }
            }

        } catch (Exception e) {
            Log.e("Stats", "Error fetching stats", e);
        } finally {
            if (cursor != null) cursor.close();
            DatabaseHelper.getInstance(context).closeDatabase();
        }

        return stats;
    }

    /**
     * Get detailed completion statistics for all tasks of a specific user
     * @param context Android context
     * @param userId The ID of the user to filter tasks
     * @return Map<String, Integer> with keys:
     *         total_tasks, completed_tasks, completed_on_time,
     *         completed_late, completed_without_deadline, uncompleted_tasks
     */
    public HashMap<String, Integer> getAllDetailedTaskStats(Context context, int userId) {
        SQLiteDatabase db = DatabaseHelper.getInstance(context).openDatabase();
        HashMap<String, Integer> stats = new HashMap<>();

        // Initialize all stats
        stats.put("total_tasks", 0);
        stats.put("completed_tasks", 0);
        stats.put("completed_on_time", 0);
        stats.put("completed_late", 0);
        stats.put("completed_without_deadline", 0);
        stats.put("uncompleted_tasks", 0);

        String query =
                "SELECT t.is_completed, t.completion_datetime, r.date AS deadline_date, r.time AS deadline_time " +
                        "FROM tbl_task t " +
                        "LEFT JOIN tbl_task_reminder r ON t.id = r.task_id " +
                        "WHERE t.user_id = ?";

        Cursor cursor = null;

        try {
            cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

            while (cursor.moveToNext()) {
                boolean isCompleted = cursor.getInt(0) == 1;
                String completionDatetime = cursor.getString(1);
                String deadlineDate = cursor.getString(2);
                String deadlineTime = cursor.getString(3);

                stats.put("total_tasks", stats.get("total_tasks") + 1);

                if (isCompleted) {
                    stats.put("completed_tasks", stats.get("completed_tasks") + 1);

                    if (deadlineDate == null) {
                        stats.put("completed_without_deadline", stats.get("completed_without_deadline") + 1);
                    } else {
                        String deadline = deadlineDate + " " + (deadlineTime != null ? deadlineTime : "00:00:00");
                        if (completionDatetime.compareTo(deadline) <= 0) {
                            stats.put("completed_on_time", stats.get("completed_on_time") + 1);
                        } else {
                            stats.put("completed_late", stats.get("completed_late") + 1);
                        }
                    }

                } else {
                    stats.put("uncompleted_tasks", stats.get("uncompleted_tasks") + 1);
                }
            }

        } catch (Exception e) {
            Log.e("Stats", "Error fetching stats", e);
        } finally {
            if (cursor != null) cursor.close();
            DatabaseHelper.getInstance(context).closeDatabase();
        }

        return stats;
    }

    private void loadStatisticsForToday() {
        String startDate = dateFormat.format(currentDate.getTime());
        String endDate = dateFormat.format(currentDate.getTime());
        HashMap<String, Integer> todayStats = getDetailedTaskStats(this, startDate, endDate, currentUserId);
        totalTasks = todayStats.get("total_tasks");
        completedTasks = todayStats.get("completed_tasks");
        overdueTasks = todayStats.get("completed_late");
        onTimeTasks = todayStats.get("completed_on_time");
        undatedTasks = todayStats.get("completed_without_deadline");
        uncompletedTasks = todayStats.get("uncompleted_tasks");
        Log.d(TAG, "Today Statistics: " + todayStats.toString());
        updateUI();
    }

    // Updated loadStatisticsForWeek method
    private void loadStatisticsForWeek() {
        // Use currentDate instead of creating a new Calendar
        Calendar calendar = (Calendar) currentDate.clone();
        
        // Find the end of the week (Saturday)
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        calendar.add(Calendar.DAY_OF_WEEK, Calendar.SATURDAY - dayOfWeek);
        Date endDateObj = calendar.getTime();
        
        // Go back to start of the week (Sunday)
        calendar.add(Calendar.DAY_OF_WEEK, -(Calendar.SATURDAY - Calendar.SUNDAY));
        Date startDateObj = calendar.getTime();
        
        String startDate = dateFormat.format(startDateObj);
        String endDate = dateFormat.format(endDateObj);
        
        // Get userId from your login session
        HashMap<String, Integer> stats = getDetailedTaskStats(this, startDate, endDate, currentUserId);
        
        totalTasks = stats.get("total_tasks");
        completedTasks = stats.get("completed_tasks");
        overdueTasks = stats.get("completed_late");
        onTimeTasks = stats.get("completed_on_time");
        undatedTasks = stats.get("completed_without_deadline");
        uncompletedTasks = stats.get("uncompleted_tasks");
        
        Log.d(TAG, "Weekly Statistics: " + stats.toString());
        // Remove this updateUI() call as it's already called in the click listener
    }

    // Updated loadStatisticsForMonth method
    private void loadStatisticsForMonth() {
        // Use currentDate instead of Calendar.getInstance()
        Calendar calendar = (Calendar) currentDate.clone();
        
        // Set to last day of month
        int lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        calendar.set(Calendar.DAY_OF_MONTH, lastDay);
        Date endDateObj = calendar.getTime();

        // Set to first day of month
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        Date startDateObj = calendar.getTime();

        String startDate = dateFormat.format(startDateObj);
        String endDate = dateFormat.format(endDateObj);

        // Get userId from your login session
        HashMap<String, Integer> stats = getDetailedTaskStats(this, startDate, endDate, currentUserId);

        totalTasks = stats.get("total_tasks");
        completedTasks = stats.get("completed_tasks");
        overdueTasks = stats.get("completed_late");
        onTimeTasks = stats.get("completed_on_time");
        undatedTasks = stats.get("completed_without_deadline");
        uncompletedTasks = stats.get("uncompleted_tasks");

        Log.d(TAG, "Monthly Statistics: " + stats.toString());
    }

    private void loadStatisticsforForAll(){
        HashMap<String, Integer> allStats = getAllDetailedTaskStats(this, currentUserId);
        totalTasks = allStats.get("total_tasks");
        completedTasks = allStats.get("completed_tasks");
        overdueTasks = allStats.get("completed_late");
        onTimeTasks = allStats.get("completed_on_time");
        undatedTasks = allStats.get("completed_without_deadline");
        uncompletedTasks = allStats.get("uncompleted_tasks");
        Log.d(TAG, "All Statistics: " + allStats.toString());
        updateUI();
    }

    // Update the updateUI method
    private void updateUI() {
        // Update completed tasks
        tvCompletedTasksCount.setText(String.valueOf(completedTasks));
        int taskDifference = completedTasks - previousDayCompletedTasks;
        
        if (taskDifference >= 0) {
            tvCompletedFromYesterday.setText(getString(R.string.stats_more_than_yesterday, Math.abs(taskDifference)));
        } else {
            tvCompletedFromYesterday.setText(getString(R.string.stats_fewer_than_yesterday, Math.abs(taskDifference)));
        }

        // Update completion rate
        double completionRate = (totalTasks > 0) ? (completedTasks * 100.0 / totalTasks) : 0;
        String formattedRate = String.format(Locale.getDefault(), "%.2f%%", completionRate);
        tvCompletionRate.setText(formattedRate);

        double rateDifference = completionRate - previousDayCompletionRate;
        if (rateDifference >= 0) {
            tvTotalTask.setText(getString(R.string.stats_percent_more_than, Math.abs(rateDifference)));
        } else {
            tvTotalTask.setText(getString(R.string.stats_percent_less_than, Math.abs(rateDifference)));
        }

        // Update progress bar
        progressBar.setProgress((int) completionRate);
        tvPercentage.setText(formattedRate);

        // Update distribution counts
        tvOverdueCount.setText(String.valueOf(overdueTasks));
        tvOnTimeCount.setText(String.valueOf(onTimeTasks));
        tvUndateCount.setText(String.valueOf(undatedTasks));
        tvUncompleteCount.setText(String.valueOf(uncompletedTasks));

        // Update task counts
        tvCompletionRate.setText(String.valueOf(totalTasks));  // Display total tasks
        tvTotalTask.setText(getString(R.string.stats_completed_count, completedTasks));
    }
}