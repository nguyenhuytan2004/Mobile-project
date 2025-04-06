package com.example.project;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton fabAdd;
    private LinearLayout taskListLayout;
    ImageView focusTab, calendarTab, sideBarView, matrixView, habitTab;
    private LoginSessionManager loginSessionManager;
    private SQLiteDatabase db;

    LocalDateTime now = LocalDateTime.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    String formattedCurrentDate = now.format(formatter);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ReminderService.scheduleReminders(this);

        loginSessionManager = LoginSessionManager.getInstance(this);
        if (loginSessionManager.isLoggedIn()) {
            Log.d("MainActivity", "User is logged in");
        }
        else {
            Log.d("MainActivity", "User is not logged in");
            loginSessionManager.createSession(1);
            Log.d("MainActivity", "User session created with ID: " + loginSessionManager.getUserId());
        }

        // Ánh xạ các thành phần trong giao diện
        fabAdd = findViewById(R.id.fab_add);
        taskListLayout = findViewById(R.id.task_list_layout);
        focusTab = findViewById(R.id.focusTab);
        calendarTab = findViewById(R.id.calendarTab);
        sideBarView = findViewById(R.id.sidebarView);
        matrixView = findViewById(R.id.matrixView);
        habitTab = findViewById(R.id.habitTab);


        sideBarView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sideBarView.setOnClickListener(p -> {
                    SideBarHelper.showSideBar(MainActivity.this, category -> {
                        // Handle category selection
                        if (category.equals("Tất cả công việc")) {
                            loadAllTasks();
                        } else if (category.equals("Hôm nay")) {
                            loadTaskByDate(formattedCurrentDate);
                        } else {
                            loadTasksByCategory(category);
                        }
                    }, () -> getAllTasksFromDatabase());
                });
            }
        });


        // Sự kiện khi nhấn Floating Action Button (FAB)
        fabAdd.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("noteId", "-1");

            Intent intent = new Intent(MainActivity.this, NoteActivity.class);
            intent.putExtras(bundle);

            startActivity(intent);
        });

        matrixView.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  Intent intent = new Intent(MainActivity.this, Matrix_Eisenhower.class);
                  startActivity(intent);
              }
          }
        );

        focusTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FocusTab.class);
                startActivity(intent);
            }
        });

        calendarTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CalendarTab.class);
                startActivity(intent);
            }
        });

        habitTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HabitActivity.class);
                startActivity(intent);
            }
        });

        loadNotes();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotes();
    }

    private void loadNotes() {
        // Clear previous notes
        taskListLayout.removeAllViews();

        try {
            // Open database
            db = DatabaseHelper.getInstance(this).openDatabase();

            // Query for notes belonging to current user
            int userId = loginSessionManager.getUserId();
            String query = "SELECT n.id, n.title, n.content, r.date " +
                    "FROM tbl_note n " +
                    "LEFT JOIN tbl_note_reminder r ON n.id = r.note_id " +
                    "WHERE n.user_id = ? ";

            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    int noteId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));

                    String content = "";
                    int contentColumnIndex = cursor.getColumnIndexOrThrow("content");
                    if (!cursor.isNull(contentColumnIndex)) {
                        content = cursor.getString(contentColumnIndex);
                    }

                    String date = "";
                    int dateColumnIndex = cursor.getColumnIndexOrThrow("date");
                    if (!cursor.isNull(dateColumnIndex)) {
                        date = cursor.getString(dateColumnIndex);
                    }

                    addNoteView(noteId, title, content, date);
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Error loading notes", e);
        } finally {
            if (db != null) {
                DatabaseHelper.getInstance(this).closeDatabase();
            }
        }
    }

    private void addNoteView(int noteId, String title, String content, String date) {
        View noteView = LayoutInflater.from(this).inflate(R.layout.note_item_in_main, taskListLayout, false);

        TextView titleTextView = noteView.findViewById(R.id.note_title);
        TextView contentTextView = noteView.findViewById(R.id.note_content);
        TextView dateTextView = noteView.findViewById(R.id.note_date);

        titleTextView.setText(title);
        if (content.isEmpty()) {
            Log.d("123", "123");
            contentTextView.setVisibility(View.GONE);
        }
        else {
            int numLines = content.split("\n").length;
            if (content.length() > 33) {
                content = content.substring(0, 33) + "...";
            }
            else if (numLines > 2) {
                content = content.split("\n")[0] + "\n" + content.split("\n")[1] + "...";
            }
            contentTextView.setText(content);
        }
        if (date.isEmpty()) {
            contentTextView.setVisibility(View.GONE);
        }
        else {
            dateTextView.setText(date);
        }
        if (!date.isEmpty()) {
            Pattern pattern = Pattern.compile("(\\d+)");
            Matcher matcher = pattern.matcher(date);

            int day = 0, month = 0;
            if (matcher.find()) {
                day = Integer.parseInt(matcher.group(1));
            }
            if (matcher.find()) {
                month = Integer.parseInt(matcher.group(1));
            }

            LocalDate noteDate = LocalDate.of(LocalDate.now().getYear(), month, day);

            if (noteDate.isBefore(LocalDate.now())) {
                dateTextView.setTextColor(getResources().getColor(R.color.red));
            }
            else {
                dateTextView.setTextColor(getResources().getColor(R.color.statistics_blue));
            }
        }

        // Make the note clickable to open for editing
        noteView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("noteId", String.valueOf(noteId));
            Intent intent = new Intent(MainActivity.this, NoteActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        });

        taskListLayout.addView(noteView);
    }
    // Method 1: Load all tasks from the database
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

                    Task task = new Task( title, description, priority);
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
                    Task categoryTask = new Task();
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
