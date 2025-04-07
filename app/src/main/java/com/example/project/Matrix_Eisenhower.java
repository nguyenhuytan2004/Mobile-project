package com.example.project;


import android.content.ContentValues;
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

        addButton = findViewById(R.id.addButton);


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
        // Update the add button click listener
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Adding the missing defaultPriority parameter (using 1 as default)
                TaskDialogHelper.showInputDialog(Matrix_Eisenhower.this, 1, new TaskDialogHelper.TaskDialogCallback() {
                    @Override
                    public void onTaskAdded(Task task) {
                        Matrix_Eisenhower.this.onTaskAdded(task);
                    }
                });
            }
        });
        // Load tasks from database
        loadTasksFromDatabase();
    }


    public void onTaskAdded(Task task) {
        // Add the task to database
        addTaskToDatabase(task);

        // Add the task to UI
        addTaskToUI(task);

        // Add to task list
        taskList.add(task);
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
            // Get user ID from login session
            LoginSessionManager loginSessionManager = LoginSessionManager.getInstance(this);
            int userId = loginSessionManager.getUserId();
            
            // Use JOIN to get reminder_date from task_reminder table
            String query = "SELECT t.id, t.title, t.content, t.priority, r.date, " +
                    "t.is_completed, t.category_id " +
                    "FROM tbl_task t " +
                    "LEFT JOIN tbl_task_reminder r ON t.id = r.task_id " +
                    "WHERE t.user_id = ?";
                    
            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
            
            Log.d("Matrix_Eisenhower", "Found " + cursor.getCount() + " tasks in database");
            
            if (cursor.moveToFirst()) {
                do {
                    int idIndex = cursor.getColumnIndex("id");
                    int id = (idIndex >= 0) ? cursor.getInt(idIndex) : -1;
                    
                    int titleIndex = cursor.getColumnIndex("title");
                    String title = (titleIndex >= 0) ? cursor.getString(titleIndex) : "";

                    int contentIndex = cursor.getColumnIndex("content");
                    String content = (contentIndex >= 0) ? cursor.getString(contentIndex) : "";

                    int priorityIndex = cursor.getColumnIndex("priority");
                    int priority = (priorityIndex >= 0) ? cursor.getInt(priorityIndex) : 4;  // Default to lowest priority if missing

                    int categoryIdIndex = cursor.getColumnIndex("category_id");
                    int categoryId = (categoryIdIndex >= 0) ? cursor.getInt(categoryIdIndex) : 1;  // Default category_id = 1

                    int dateIndex = cursor.getColumnIndex("date");
                    String reminderDate = (dateIndex >= 0 && !cursor.isNull(dateIndex)) ? cursor.getString(dateIndex) : "";

                    int completeIndex = cursor.getColumnIndex("is_completed");
                    boolean isCompleted = (completeIndex >= 0) && cursor.getInt(completeIndex) == 1;

                    // Create a task object with all needed fields
                    Task task = new Task(id, title, categoryId);
                    task.setDescription(content);
                    task.setPriority(priority);
                    task.setReminderDate(reminderDate);
                    task.setCompleted(isCompleted);

                    // Add to UI and task list
                    addTaskToUI(task);
                    taskList.add(task);
                    
                    Log.d("Matrix_Eisenhower", "Loaded task: " + title + ", priority: " + priority + 
                          ", completed: " + isCompleted);
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
                    setCompleted(task, true);
                    taskNameView.setPaintFlags(taskNameView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                } else {
                    setCompleted(task, false);
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
    // Method to add task to database
    private void addTaskToDatabase(Task task) {
        SQLiteDatabase db = DatabaseHelper.getInstance(this).openDatabase();
        try {
            LoginSessionManager loginSessionManager = LoginSessionManager.getInstance(this);
            int userId = loginSessionManager.getUserId();
            
            ContentValues values = new ContentValues();
            values.put("user_id", userId);
            values.put("title", task.getTitle());
            values.put("content", task.getDescription());  // Note: uses content field in DB
            values.put("priority", task.getPriority());
            values.put("is_completed", task.isCompleted() ? 1 : 0);
            values.put("category_id", task.getCategoryId() > 0 ? task.getCategoryId() : 1); // Default to 1 if not set

            long taskId = db.insert("tbl_task", null, values);
            
            if (taskId == -1) {
                Log.e("Matrix_Eisenhower", "Error adding task to database");
            } else {
                // If task has a reminder date, add it to the reminder table
                if (task.hasReminder()) {
                    ContentValues reminderValues = new ContentValues();
                    reminderValues.put("task_id", taskId);
                    reminderValues.put("date", task.getReminderDate());
                    
                    long reminderId = db.insert("tbl_task_reminder", null, reminderValues);
                    
                    if (reminderId == -1) {
                        Log.e("Matrix_Eisenhower", "Error adding task reminder to database");
                    }
                }
                
                Log.d("Matrix_Eisenhower", "Task added successfully to database with ID: " + taskId);
            }
        } catch (Exception e) {
            Log.e("Matrix_Eisenhower", "Error: " + e.getMessage(), e);
        } finally {
            DatabaseHelper.getInstance(this).closeDatabase();
        }
    }


    private void setCompleted(Task task, boolean isCompleted) {
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

            // Update database using task ID which is more reliable
            String whereClause = "id = ?";
            String[] whereArgs = {String.valueOf(task.getId())};

            long result = db.update("tbl_task", values, whereClause, whereArgs);

            if (result == -1) {
                Log.e("Matrix_Eisenhower", "Lỗi khi cập nhật trạng thái task vào database");
            } else if (result == 0) {
                // If no rows were updated, try the old method as fallback
                Log.w("Matrix_Eisenhower", "Không tìm thấy task với ID " + task.getId() + ", thử dùng title và content");
                
                whereClause = "title = ? AND content = ? AND priority = ?";
                whereArgs = new String[]{task.getTitle(), task.getDescription(), String.valueOf(task.getPriority())};
                
                result = db.update("tbl_task", values, whereClause, whereArgs);
                
                if (result > 0) {
                    Log.d("Matrix_Eisenhower", "Task đã được cập nhật bằng phương thức thay thế");
                } else {
                    Log.e("Matrix_Eisenhower", "Vẫn không thể cập nhật task");
                }
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
