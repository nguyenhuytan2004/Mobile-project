package com.example.project;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class TaskByCategory extends AppCompatActivity implements TaskAdapter.TaskCompletionListener {

    private FloatingActionButton fabAdd;
    private ImageView sidebarView, focusTab, calendarTab, matrixView, habitTab, ivMore;
    private RecyclerView rvTaskList;
    private ArrayList<Task> taskList;
    private TaskAdapter taskAdapter;
    private String currentCategory;
    private int defaultPriority = 1; // Default priority
    private String currentTitle;
    private TextView titleTextView;
    LocalDateTime now = LocalDateTime.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    String formattedCurrentDate = now.format(formatter);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_by_category);  // Set layout


        // Get priority information from intent
        Intent intent = getIntent();
        currentCategory = intent.getStringExtra("category");
        currentTitle = intent.getStringExtra("title");


        // Initialize views
        fabAdd = findViewById(R.id.floatingActionButton2);
        rvTaskList = findViewById(R.id.recyclerView);
        sidebarView = findViewById(R.id.sidebarView);
        focusTab = findViewById(R.id.focusTab);
        calendarTab = findViewById(R.id.calendarTab);
        matrixView = findViewById(R.id.matrixView);
        habitTab = findViewById(R.id.habitTab);
        titleTextView = findViewById(R.id.tvCategory);
        ivMore = findViewById(R.id.ivMore);

        matrixView.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  Intent intent = new Intent(TaskByCategory.this, Matrix_Eisenhower.class);
                  startActivity(intent);
              }
          }
        );

        focusTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TaskByCategory.this, FocusTab.class);
                startActivity(intent);
            }
        });

        calendarTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TaskByCategory.this, CalendarTab.class);
                startActivity(intent);
            }
        });

        habitTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TaskByCategory.this, HabitActivity.class);
                startActivity(intent);
            }
        });

        // Set the title based on the passed priority
        if (currentTitle != null) {
            titleTextView.setText(currentTitle);
        }

        ivMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
                popupMenu.getMenu().add("Xoá");
                popupMenu.getMenu().add("Chia sẻ");

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        String title = item.getTitle().toString();
                        switch (title) {
                            case "Xoá":
                                // TODO: Thêm logic xoá
                                deleteTasksByCategory(currentCategory);
                                Intent intent = new Intent(TaskByCategory.this, MainActivity.class);
                                startActivity(intent);
                                finish(); // Close this activity and return to the previous one
                                return true;
                            case "Chia sẻ":
                                // TODO: Thêm logic chia sẻ
                                Toast.makeText(v.getContext(), "Đã chọn Chia sẻ", Toast.LENGTH_SHORT).show();
                                return true;
                            default:
                                return false;
                        }
                    }
                });

                popupMenu.show();
            }
        });

        // Set up RecyclerView
        rvTaskList.setLayoutManager(new LinearLayoutManager(this));

        // Initialize task list and adapter
        taskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(taskList, this);
        rvTaskList.setAdapter(taskAdapter);

        // Load tasks from database for the current priority
        loadTasksByCategory(currentCategory);

        // backBtn click listener
        sidebarView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sidebarView.setOnClickListener(p -> {
                    SideBarHelper.showSideBar(TaskByCategory.this, category -> {
                        // Handle category selection
                        if (category.equals("Tất cả công việc")) {
                            loadAllTasks();
                        } else if (category.equals("Hôm nay")) {
                            loadTasksByCategory("Hôm nay");
                        } else {
                            loadTasksByCategory(category);
                        }
                    }, () -> getAllTasksFromDatabase());
                });
            }
        });


        // FloatingActionButton click listener
        fabAdd.setOnClickListener(v -> {
            TaskDialogHelper.showInputDialog(TaskByCategory.this, defaultPriority, new TaskDialogHelper.TaskDialogCallback() {
                @Override
                public void onTaskAdded(Task task) {
                    // Save the task to the database
                    saveTaskToDatabase(task);

                    // Add the task to the list and notify the adapter
                    taskList.add(task);
                    taskAdapter.notifyDataSetChanged();
                }
            });
        });
    }

    private void loadAllTasks() {
        taskList.clear(); // Clear the current list
        loadTasksByCategory("Tất cả công việc"); // Load all tasks
        taskAdapter.notifyDataSetChanged(); // Notify the adapter
    }

    // Load all tasks from the database
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

    private void saveTaskToDatabase(Task task) {
        SQLiteDatabase db = DatabaseHelper.getInstance(this).openDatabase();
        if (db == null) {
            Log.e("Database", "Database không tồn tại hoặc không thể mở");
            return;
        }

        try {
            // Create ContentValues to store task data
            android.content.ContentValues values = new android.content.ContentValues();
            values.put("title", task.getTitle());
            values.put("description", task.getDescription());
            values.put("priority", task.getPriority());
            values.put("reminder_date", task.hasReminder() ? task.getReminderDate() : "");
            values.put("category", task.getCategory());

            // Insert into database
            long result = db.insert("tbl_task", null, values);

            if (result == -1) {
                Log.e("Matrix_Eisenhower", "Lỗi khi lưu task vào database");
            } else {
                Log.d("Matrix_Eisenhower", "Task đã được lưu vào database thành công");
            }
        } catch (Exception e) {
            Log.e("Matrix_Eisenhower", "Lỗi: " + e.getMessage());
        } finally {
            db.close();
        }
    }

    // Load tasks for the selected priority from the database
    private void loadTasksByCategory(String category) {
        SQLiteDatabase db = DatabaseHelper.getInstance(this).openDatabase();
        if (db == null) {
            Log.e("Database", "Database không tồn tại hoặc không thể mở");
            return;
        }

        try {
            Cursor cursor;
            if(Objects.equals(category, "Tất cả công việc")){
                 cursor = db.rawQuery("SELECT * FROM tbl_task ", null);
            } else if (Objects.equals(category, "Hôm nay")) {
                // Get today's date
                String todayDate = formattedCurrentDate;
                Log.d("TAG", "Today date: " + todayDate);
                cursor = db.rawQuery("SELECT * FROM tbl_task WHERE reminder_date = ?",
                        new String[]{todayDate});
            } else {
                 cursor = db.rawQuery("SELECT * FROM tbl_task WHERE category = ?",
                        new String[]{category});
            }
            if (cursor.moveToFirst()) {
                do {
                    int titleIndex = cursor.getColumnIndex("title");
                    String title = (titleIndex >= 0) ? cursor.getString(titleIndex) : "";


                    int descriptionIndex = cursor.getColumnIndex("description");
                    String description = (descriptionIndex >= 0) ? cursor.getString(descriptionIndex) : "";

                    int priorityIndex = cursor.getColumnIndex("priority");
                    int priority = (priorityIndex >= 0) ? cursor.getInt(priorityIndex) : defaultPriority;

                    int reminderDateIndex = cursor.getColumnIndex("reminder_date");
                    String reminderDate = (reminderDateIndex >= 0) ? cursor.getString(reminderDateIndex) : "";


                    int completeIndex = cursor.getColumnIndex("complete");
                    boolean isCompleted = (completeIndex >= 0) && cursor.getInt(completeIndex) == 1;


                    Task task = new Task(title, description, priority, category);
                    task.setReminderDate(reminderDate);
                    task.setCompleted(isCompleted);


                    taskList.add(task);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            Log.e("SingleMatrix", "Error loading tasks: " + e.getMessage(), e);
        } finally {
            DatabaseHelper.getInstance(this).closeDatabase();
        }


        // Notify the adapter that data has changed
        taskAdapter.notifyDataSetChanged();
    }

    private void deleteTasksByCategory(String category) {
        SQLiteDatabase db = DatabaseHelper.getInstance(this).openDatabase();
        if (db == null) {
            Log.e("TaskByCategory", "Database không tồn tại hoặc không thể mở");
            return;
        }

        try {
            int deletedRows;
            if (Objects.equals(category, "Tất cả công việc")) {
                deletedRows = db.delete("tbl_task", null, null); // Xoá toàn bộ
            } else if (Objects.equals(category, "Hôm nay")) {
                String todayDate = formattedCurrentDate;
                deletedRows = db.delete("tbl_task", "reminder_date = ?", new String[]{todayDate});
            } else {
                deletedRows = db.delete("tbl_task", "category = ?", new String[]{category});
            }

            Log.d("TaskByCategory", "Đã xoá " + deletedRows + " task trong category: " + category);
        } catch (Exception e) {
            Log.e("TaskByCategory", "Lỗi khi xoá task: " + e.getMessage(), e);
        } finally {
            DatabaseHelper.getInstance(this).closeDatabase();
        }

        // Cập nhật danh sách sau khi xoá
        taskList.clear();
        taskAdapter.notifyDataSetChanged();
    }


    @Override
    public void onTaskCompletionChanged(Task task, boolean isCompleted) {
        updateTaskCompletionStatus(task, isCompleted);
    }


    private void updateTaskCompletionStatus(Task task, boolean isCompleted) {
        SQLiteDatabase db = DatabaseHelper.getInstance(this).openDatabase();
        if (db == null) {
            Log.e("SingleMatrix", "Database không tồn tại hoặc không thể mở");
            return;
        }


        try {
            // Update the Task object's completion status
            task.setCompleted(isCompleted);


            // Create ContentValues to store task data
            android.content.ContentValues values = new android.content.ContentValues();
            values.put("complete", isCompleted ? 1 : 0);


            // Update database - using both title and description for more precise matching
            String whereClause = "title = ? AND description = ?";
            String[] whereArgs = {task.getTitle(), task.getDescription()};


            long result = db.update("tbl_task", values, whereClause, whereArgs);


            if (result == -1) {
                Log.e("SingleMatrix", "Lỗi khi cập nhật trạng thái task vào database");
            } else {
                Log.d("SingleMatrix", "Task đã được cập nhật trạng thái thành công: " +
                        (isCompleted ? "đã hoàn thành" : "chưa hoàn thành"));
            }
        } catch (Exception e) {
            Log.e("SingleMatrix", "Lỗi: " + e.getMessage(), e);
        } finally {
            DatabaseHelper.getInstance(this).closeDatabase();
        }
    }
}
