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
                                Intent intent = new Intent(TaskByCategory.this, MainActivity.class);
                                startActivity(intent);
                                finish(); // Close this activity and return to the previous one
                                return true;
                            case "Chia sẻ":
                                // TODO: Thêm logic chia sẻ
                                Log.d("TAG", "Task list: " + taskList);
                                Intent shareIntent = new Intent(TaskByCategory.this, ShareTaskActivity.class);
                                shareIntent.putExtra("task_list", taskList); // tasks phải là ArrayList, không phải List
                                startActivity(shareIntent);
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


        // backBtn click listener
        sidebarView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sidebarView.setOnClickListener(p -> {
                    Toast.makeText(TaskByCategory.this, "Sidebar clicked", Toast.LENGTH_SHORT).show();
                });
            }
        });


        // FloatingActionButton click listener
        fabAdd.setOnClickListener(v -> {
            TaskDialogHelper.showInputDialog(TaskByCategory.this, defaultPriority, new TaskDialogHelper.TaskDialogCallback() {
                @Override
                public void onTaskAdded(Task task) {
                    // Save the task to the database
                    // Add the task to the list and notify the adapter
                    taskList.add(task);
                    taskAdapter.notifyDataSetChanged();
                }
            });
        });
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
            values.put("is_completed", isCompleted ? 1 : 0);


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
