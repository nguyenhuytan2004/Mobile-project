package com.example.project;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
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
    private ArrayList<Task> taskList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.matrix_priority);

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
                        addTaskToUI(task);
                        taskList.add(task);  // Add task to the list
                    }
                });
            }
        });
        // Load tasks from database
        loadTasksFromDatabase();
    }

    private void loadTasksFromDatabase() {
        SQLiteDatabase db = DatabaseHelper.getInstance(this).openDatabase();
        if (db == null) {
            Log.e("Matrix_Eisenhower", "Database không tồn tại hoặc không thể mở");
            return;
        }

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

                Task task = new Task(title, description, priority);
                task.setReminderDate(reminderDate);
                addTaskToUI(task);
                taskList.add(task);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
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
            taskCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                // Handle checkbox state change
                if (isChecked) {
                    taskNameView.setPaintFlags(taskNameView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                } else {
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
}
