package com.example.project;


import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.HashMap;
import android.graphics.Paint;


public class Matrix_Eisenhower extends AppCompatActivity {

    private FloatingActionButton addButton;
    private HashMap<Integer, LinearLayout> priorityMap;


    ImageView focusTab, calendarTab, homeTab, habitTab;
    private ArrayList<Task> taskList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.matrix_priority);


        calendarTab = findViewById(R.id.calendarTab2);
        focusTab = findViewById(R.id.focusTab);
        homeTab = findViewById(R.id.homeTab);
        habitTab = findViewById(R.id.habitTabCalender);


        focusTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Matrix_Eisenhower.this, FocusTab.class);
                startActivity(intent);
            }
        });


        calendarTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Matrix_Eisenhower.this, CalendarTab.class);
                startActivity(intent);
            }
        });


        habitTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Matrix_Eisenhower.this, HabitActivity.class);
                startActivity(intent);
            }
        });


        homeTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Matrix_Eisenhower.this, MainActivity.class);
                startActivity(intent);
            }
        });


        // Ánh xạ các LinearLayout tương ứng với từng mức độ ưu tiên
        priorityMap = new HashMap<>();
        priorityMap.put(1, findViewById(R.id.priority_1));
        priorityMap.put(2, findViewById(R.id.priority_2));
        priorityMap.put(3, findViewById(R.id.priority_3));
        priorityMap.put(4, findViewById(R.id.priority_4));


        // Khởi tạo danh sách task
        taskList = new ArrayList<>();


        // Set up click listeners for all priority quadrants
        setupPriorityClickListeners();
        // Nút thêm task
        addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TaskDialogHelper.showInputDialog(Matrix_Eisenhower.this, new TaskDialogHelper.TaskDialogCallback() {
                    @Override
                    public void onTaskAdded(Task task) {
                        // Add task to UI
                        addTaskToUI(task);
                        // Add task to the list
                        taskList.add(task);
                        // Save task to database
                    }
                });
            }
        });
        // Load tasks from database
        loadTasksFromDatabase();
    }


    // Set up click listeners for priority quadrants
    private void setupPriorityClickListeners() {
        Log.d("Matrix_Eisenhower", "Setting up click listeners for priority quadrants");


        // For Priority 1: Khẩn cấp và Quan trọng
        LinearLayout priority1Layout = priorityMap.get(1);
        if (priority1Layout != null) {
            priority1Layout.setOnClickListener(v -> navigateToSingleMatrix(1, "Khẩn cấp và Quan trọng"));
        }


        // For Priority 2: Không gấp mà quan trọng
        LinearLayout priority2Layout = priorityMap.get(2);
        if (priority2Layout != null) {
            priority2Layout.setOnClickListener(v -> navigateToSingleMatrix(2, "Không gấp mà quan trọng"));
        }


        // For Priority 3: Khẩn cấp nhưng không quan trọng
        LinearLayout priority3Layout = priorityMap.get(3);
        if (priority3Layout != null) {
            priority3Layout.setOnClickListener(v -> navigateToSingleMatrix(3, "Khẩn cấp nhưng không quan trọng"));
        }


        // For Priority 4: Không cấp bách và không quan trọng
        LinearLayout priority4Layout = priorityMap.get(4);
        if (priority4Layout != null) {
            priority4Layout.setOnClickListener(v -> navigateToSingleMatrix(4, "Không cấp bách và không quan trọng"));
        }
    }

    // Navigate to SingleMatrix activity with the selected priority
    private void navigateToSingleMatrix(int priority, String title) {
        Intent intent = new Intent(Matrix_Eisenhower.this, SingleMatrix.class);
        intent.putExtra("priority", priority);
        intent.putExtra("title", title);
        startActivity(intent);
    }

    private void loadTasksFromDatabase() {
        SQLiteDatabase db = DatabaseHelper.getInstance(this).openDatabase();
        if (db == null) {
            Log.e("Matrix_Eisenhower", "Database không tồn tại hoặc không thể mở");
            return;
        }


        try {
            Cursor cursor = db.rawQuery("SELECT * FROM tbl_task", null);
            if (cursor.moveToFirst()) {
                do {
                    int titleIndex = cursor.getColumnIndex("title");
                    String title = (titleIndex >= 0) ? cursor.getString(titleIndex) : "";


                    int descriptionIndex = cursor.getColumnIndex("description");
                    String description = (descriptionIndex >= 0) ? cursor.getString(descriptionIndex) : "";


                    int priorityIndex = cursor.getColumnIndex("priority");
                    int priority = (cursor.getInt(priorityIndex));


                    int reminderDateIndex = cursor.getColumnIndex("reminder_date");
                    String reminderDate = (reminderDateIndex >= 0) ? cursor.getString(reminderDateIndex) : "";


                    int completeIndex = cursor.getColumnIndex("is_completed");
                    boolean isCompleted = (completeIndex >= 0) && cursor.getInt(completeIndex) == 1;


                    Task task = new Task(title, description, priority);
                    task.setReminderDate(reminderDate);
                    task.setCompleted(isCompleted);


                    addTaskToUI(task);
                    taskList.add(task);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            Log.e("Matrix_Eisenhower", "Error loading tasks: " + e.getMessage(), e);
        } finally {
            DatabaseHelper.getInstance(this).closeDatabase();
        }
    }

    // Hàm thêm task vào UI theo priority
    private void addTaskToUI(Task task) {
        LinearLayout priorityLayout = priorityMap.get(task.getPriority());
        if (priorityLayout != null) {
            // Tạo một LinearLayout
            LinearLayout taskContainer = new LinearLayout(this);
            taskContainer.setOrientation(LinearLayout.HORIZONTAL);
            taskContainer.setPadding(8, 4, 8, 4);


            // Tạo container cho text
            LinearLayout textContainer = new LinearLayout(this);
            textContainer.setOrientation(LinearLayout.VERTICAL);
            textContainer.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            textContainer.setPadding(8, 0, 0, 0);


            // Tạo TextView cho Task Name
            TextView taskNameView = new TextView(this);
            taskNameView.setText(task.getTitle());
            taskNameView.setTextColor(getResources().getColor(android.R.color.white));
            taskNameView.setTextSize(16);


            // Tạo TextView cho Task Description
            TextView taskDescView = new TextView(this);
            taskDescView.setText(task.getDescription());
            taskDescView.setTextColor(getResources().getColor(android.R.color.darker_gray));
            taskDescView.setTextSize(14);


            // Tạo CheckBox cho Task
            CheckBox taskCheckBox = new CheckBox(this);
            taskCheckBox.setButtonDrawable(R.drawable.custom_checkbox);


            // Apply strike-through text if the task is already completed
            if (task.isCompleted()) {
                taskCheckBox.setChecked(true);
                taskNameView.setPaintFlags(taskNameView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }

            // In the addTaskToUI method, update the checkbox listener:
            taskCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                // Handle checkbox state change
                if (isChecked) {
                    setComplted(task, true);
                    taskNameView.setPaintFlags(taskNameView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                } else {
                    setComplted(task, false);
                    taskNameView.setPaintFlags(taskNameView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                }
            });


            // Thêm TextView vào textContainer
            textContainer.addView(taskNameView);
            textContainer.addView(taskDescView);


            // Thêm các view vào taskContainer
            taskContainer.addView(taskCheckBox);
            taskContainer.addView(textContainer);


            // Thêm taskContainer vào priorityLayout
            priorityLayout.addView(taskContainer);
        }
    }

    private void setComplted(Task task, boolean isCompleted) {
        SQLiteDatabase db = DatabaseHelper.getInstance(this).openDatabase();
        if (db == null) {
            Log.e("Matrix_Eisenhower", "Database không tồn tại hoặc không thể mở");
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
                Log.e("Matrix_Eisenhower", "Lỗi khi cập nhật trạng thái task vào database");
            } else {
                Log.d("Matrix_Eisenhower", "Task đã được cập nhật trạng thái thành công: " +
                        (isCompleted ? "đã hoàn thành" : "chưa hoàn thành"));
            }
        } catch (Exception e) {
            Log.e("Matrix_Eisenhower", "Lỗi: " + e.getMessage(), e);
        } finally {
            DatabaseHelper.getInstance(this).closeDatabase();
        }
    }
}
