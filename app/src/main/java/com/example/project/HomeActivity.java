package com.example.project;

import com.example.project.Task;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.*;

public class HomeActivity extends AppCompatActivity {

    private FloatingActionButton fabAdd;
    private LinearLayout taskListLayout;
    ImageView focusTab, calendarTab, sideBarView, matrixView, habitTab;
    private SQLiteDatabase db;
    private LoginSessionManager loginSessionManager;

    LocalDateTime now = LocalDateTime.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    String formattedCurrentDate = now.format(formatter);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home); // Layout chính sau đăng nhập

        loginSessionManager = LoginSessionManager.getInstance(this);
        if (!loginSessionManager.isLoggedIn()) {
            loginSessionManager.createSession(1); // tạo giả user
        }

        ReminderService.scheduleReminders(this);

        // Ánh xạ UI
        fabAdd = findViewById(R.id.fab_add);
        taskListLayout = findViewById(R.id.task_list_layout);
        focusTab = findViewById(R.id.focusTab);
        calendarTab = findViewById(R.id.calendarTab);
        sideBarView = findViewById(R.id.sidebarView);
        matrixView = findViewById(R.id.matrixView);
        habitTab = findViewById(R.id.habitTab);

        // FAB
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, NoteActivity.class);
            intent.putExtra("noteId", "-1");
            startActivity(intent);
        });

        // Sidebar
        sideBarView.setOnClickListener(v -> {
            SideBarHelper.showSideBar(this, category -> {
                if (category.equals("Tất cả công việc")) loadAllTasks();
                else if (category.equals("Hôm nay")) loadTaskByDate(formattedCurrentDate);
                else loadTasksByCategory(category);
            }, this::getAllTasksFromDatabase);
        });

        // Tab
        focusTab.setOnClickListener(v -> startActivity(new Intent(this, FocusTab.class)));
        calendarTab.setOnClickListener(v -> startActivity(new Intent(this, CalendarTab.class)));
        matrixView.setOnClickListener(v -> startActivity(new Intent(this, Matrix_Eisenhower.class)));
        habitTab.setOnClickListener(v -> startActivity(new Intent(this, HabitActivity.class)));

        loadNotes();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotes();
    }

    private void loadNotes() {
        taskListLayout.removeAllViews();

        try {
            db = DatabaseHelper.getInstance(this).openDatabase();
            int userId = loginSessionManager.getUserId();

            String query = "SELECT n.id, n.title, n.content, r.date " +
                    "FROM tbl_note n " +
                    "LEFT JOIN tbl_note_reminder r ON n.id = r.note_id " +
                    "WHERE n.user_id = ?";
            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    int noteId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                    String content = cursor.isNull(cursor.getColumnIndexOrThrow("content")) ? "" :
                            cursor.getString(cursor.getColumnIndexOrThrow("content"));
                    String date = cursor.isNull(cursor.getColumnIndexOrThrow("date")) ? "" :
                            cursor.getString(cursor.getColumnIndexOrThrow("date"));
                    addNoteView(noteId, title, content, date);
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e("HomeActivity", "Error loading notes", e);
        } finally {
            if (db != null) DatabaseHelper.getInstance(this).closeDatabase();
        }
    }

    private void addNoteView(int noteId, String title, String content, String date) {
        View noteView = LayoutInflater.from(this).inflate(R.layout.note_item_in_main, taskListLayout, false);
        TextView titleTextView = noteView.findViewById(R.id.note_title);
        TextView contentTextView = noteView.findViewById(R.id.note_content);
        TextView dateTextView = noteView.findViewById(R.id.note_date);

        titleTextView.setText(title);

        if (content.isEmpty()) contentTextView.setVisibility(View.GONE);
        else {
            int numLines = content.split("\n").length;
            if (content.length() > 33) content = content.substring(0, 33) + "...";
            else if (numLines > 2) content = content.split("\n")[0] + "\n" + content.split("\n")[1] + "...";
            contentTextView.setText(content);
        }

        if (date.isEmpty()) dateTextView.setVisibility(View.GONE);
        else {
            dateTextView.setText(date);
            Pattern pattern = Pattern.compile("(\\d+)");
            Matcher matcher = pattern.matcher(date);
            int day = matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
            int month = matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;

            LocalDate noteDate = LocalDate.of(LocalDate.now().getYear(), month, day);
            dateTextView.setTextColor(getResources().getColor(noteDate.isBefore(LocalDate.now()) ?
                    R.color.red : R.color.statistics_blue));
        }

        noteView.setOnClickListener(v -> {
            Intent intent = new Intent(this, NoteActivity.class);
            intent.putExtra("noteId", String.valueOf(noteId));
            startActivity(intent);
        });

        taskListLayout.addView(noteView);
    }

    private void loadAllTasks() {
        taskListLayout.removeAllViews();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = DatabaseHelper.getInstance(this).openDatabase();

            String query = "SELECT id, title, description, priority, reminder_date, category " +
                    "FROM tbl_task ORDER BY priority ASC";

            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    int taskId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));

                    String description = "";
                    int descColumnIndex = cursor.getColumnIndexOrThrow("description");
                    if (!cursor.isNull(descColumnIndex)) {
                        description = cursor.getString(descColumnIndex);
                    }

                    String date = "";
                    int dateColumnIndex = cursor.getColumnIndexOrThrow("reminder_date");
                    if (!cursor.isNull(dateColumnIndex)) {
                        date = cursor.getString(dateColumnIndex);
                    }

                    addNoteView(taskId, title, description, date);
                }
            } else {
                TextView emptyText = new TextView(this);
                emptyText.setText("Không có công việc nào");
                emptyText.setTextColor(getResources().getColor(android.R.color.white));
                emptyText.setPadding(16, 16, 16, 16);
                taskListLayout.addView(emptyText);
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Error loading tasks", e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) DatabaseHelper.getInstance(this).closeDatabase();
        }
    }

    // Method 2: Load tasks filtered by category
    private void loadTasksByCategory(String category) {
        taskListLayout.removeAllViews();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = DatabaseHelper.getInstance(this).openDatabase();

            String query = "SELECT id, title, description, priority, reminder_date " +
                    "FROM tbl_task WHERE category = ? " +
                    "ORDER BY priority ASC";

            cursor = db.rawQuery(query, new String[]{category});

            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    int taskId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));

                    String description = "";
                    int descColumnIndex = cursor.getColumnIndexOrThrow("description");
                    if (!cursor.isNull(descColumnIndex)) {
                        description = cursor.getString(descColumnIndex);
                    }

                    String date = "";
                    int dateColumnIndex = cursor.getColumnIndexOrThrow("reminder_date");
                    if (!cursor.isNull(dateColumnIndex)) {
                        date = cursor.getString(dateColumnIndex);
                    }

                    addNoteView(taskId, title, description, date);
                }
            } else {
                TextView emptyText = new TextView(this);
                emptyText.setText("Không có công việc nào trong danh mục này");
                emptyText.setTextColor(getResources().getColor(android.R.color.white));
                emptyText.setPadding(16, 16, 16, 16);
                taskListLayout.addView(emptyText);
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Error loading tasks by category", e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) DatabaseHelper.getInstance(this).closeDatabase();
        }
    }

    private List<Task> loadTaskByDate(String date){
        List<Task> tasks = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = DatabaseHelper.getInstance(this).openDatabase();

            String query = "SELECT id, title, description, priority, reminder_date " +
                    "FROM tbl_task WHERE reminder_date = ? " +
                    "ORDER BY priority ASC";

            cursor = db.rawQuery(query, new String[]{date});

            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    int priority = cursor.getInt(cursor.getColumnIndexOrThrow("priority"));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));

                    String description = "";
                    int descColumnIndex = cursor.getColumnIndexOrThrow("description");
                    if (!cursor.isNull(descColumnIndex)) {
                        description = cursor.getString(descColumnIndex);
                    }

                    Task task = new Task(title, description, priority); // ✅ Đây là class bạn tạo
                    String reminderDate = cursor.getString(cursor.getColumnIndexOrThrow("reminder_date"));
                    task.setReminderDate(reminderDate); // Gán reminder date nếu cần
                    tasks.add(task);
                }
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Error loading tasks by date", e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) DatabaseHelper.getInstance(this).closeDatabase();
        }

        return tasks;
    }


    // Method 3: Get categories from database for the sidebar
    private List<Task> getAllTasksFromDatabase() {
        List<Task> categories = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = DatabaseHelper.getInstance(this).openDatabase();

            // Get unique categories from tbl_task
            String query = "SELECT DISTINCT category FROM tbl_task WHERE category IS NOT NULL AND category != ''";
            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String categoryName = cursor.getString(0);
                    Task categoryTask = new Task(); // ✅ Dùng class bạn định nghĩa
                    categoryTask.setCategory(categoryName);
                    categories.add(categoryTask);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Error getting categories", e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) DatabaseHelper.getInstance(this).closeDatabase();
        }

        return categories;
    }

}
