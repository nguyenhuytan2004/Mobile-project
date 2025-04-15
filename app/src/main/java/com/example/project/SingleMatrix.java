package com.example.project;


import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import android.content.ContentValues;


public class SingleMatrix extends AppCompatActivity implements TaskAdapter.TaskCompletionListener {


    private FloatingActionButton fabAdd;
    private ImageButton backBtn;
    private RecyclerView rvTaskList;
    private ArrayList<Task> taskList;
    private TaskAdapter taskAdapter;
    private int currentPriority;
    private String currentTitle;
    private TextView titleTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_matrix);  // Set layout


        // Get priority information from intent
        Intent intent = getIntent();
        currentPriority = intent.getIntExtra("priority", 1); // Default to priority 1 if not specified
        currentTitle = intent.getStringExtra("title");


        // Initialize views
        fabAdd = findViewById(R.id.floatingActionButton2);
        rvTaskList = findViewById(R.id.recyclerView);
        backBtn = findViewById(R.id.backBtn);
        titleTextView = findViewById(R.id.textView3);


        // Set the title based on the passed priority
        if (currentTitle != null) {
            titleTextView.setText(currentTitle);
        }


        // Set up RecyclerView
        rvTaskList.setLayoutManager(new LinearLayoutManager(this));


        // Initialize task list and adapter
        taskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(taskList, this);
        rvTaskList.setAdapter(taskAdapter);


        // Load tasks from database for the current priority
        loadTasksForPriority(currentPriority);

        // backBtn click listener
        backBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick (View v){
                Intent intent = new Intent(SingleMatrix.this, Matrix_Eisenhower.class);
                startActivity(intent);
                finish(); // Close this activity and return to the previous one
            }
        });

        // FloatingActionButton click listener
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Adding the missing defaultPriority parameter (using 1 as default)
                TaskDialogHelper.showInputDialog(SingleMatrix.this, currentPriority, new TaskDialogHelper.TaskDialogCallback() {
                    @Override
                    public void onTaskAdded(Task task) {
                        SingleMatrix.this.onTaskAdded(task);
                    }
                });
            }
        });
    }

    public void onTaskAdded(Task task) {
        // Add the task to database
        addTaskToDatabase(task);

        // Set the task's priority to match the current matrix quadrant
        task.setPriority(currentPriority);

        // Add to task list
        taskList.add(task);

        // Notify adapter to refresh the UI
        taskAdapter.notifyDataSetChanged();

        // Optional: Scroll to the bottom to show the new task
        rvTaskList.smoothScrollToPosition(taskList.size() - 1);

        // Show confirmation to user
        Toast.makeText(this, "Đã thêm nhiệm vụ mới", Toast.LENGTH_SHORT).show();
    }

    // Load tasks for the selected priority from the database
    private void loadTasksForPriority(int priority) {
        SQLiteDatabase db = DatabaseHelper.getInstance(this).openDatabase();
        if (db == null) {
            Log.e("SingleMatrix", "Database không tồn tại hoặc không thể mở");
            return;
        }

        try {
            // Use proper JOIN with task_reminder table to get reminder dates
            String query = "SELECT t.id, t.title, t.content, t.priority, r.date, " +
                  "t.is_completed, t.category_id " +
                  "FROM tbl_task t " +
                  "LEFT JOIN tbl_task_reminder r ON t.id = r.task_id " +
                  "WHERE t.priority = ?";
                  
            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(priority)});
            
            Log.d("SingleMatrix", "Found " + cursor.getCount() + " tasks with priority " + priority);
            
            if (cursor.moveToFirst()) {
                do {
                    int idIndex = cursor.getColumnIndex("id");
                    int id = (idIndex >= 0) ? cursor.getInt(idIndex) : -1;
                    
                    int titleIndex = cursor.getColumnIndex("title");
                    String title = (titleIndex >= 0) ? cursor.getString(titleIndex) : "";

                    int contentIndex = cursor.getColumnIndex("content");
                    String content = (contentIndex >= 0) ? cursor.getString(contentIndex) : "";

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

                    taskList.add(task);
                    
                    Log.d("SingleMatrix", "Loaded task: " + title + ", priority: " + priority + 
                          ", completed: " + isCompleted);
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

    @Override
    public void onTaskCompletionChanged(Task task, boolean isCompleted) {
        updateTaskCompletionStatus(task, isCompleted);
    }


    private void updateTaskCompletionStatus(Task task, boolean isCompleted) {
        DatabaseHelper.getInstance(this).markTaskAsCompleted(task.getId(), isCompleted);
        SQLiteDatabase db = DatabaseHelper.getInstance(this).openDatabase();
        if (db == null) {
            Log.e("SingleMatrix", "Database không tồn tại hoặc không thể mở");
            return;
        }

        try {
            // Update the Task object's completion status
            task.setCompleted(isCompleted);

            // Create ContentValues to store task data
            ContentValues values = new ContentValues();
            values.put("is_completed", isCompleted ? 1 : 0);

            // First, try to update using the task ID (most reliable)
            String whereClause = "id = ?";
            String[] whereArgs = {String.valueOf(task.getId())};

            long result = db.update("tbl_task", values, whereClause, whereArgs);

            if (result == -1) {
                Log.e("SingleMatrix", "Lỗi khi cập nhật trạng thái task vào database");
            } else if (result == 0) {
                // If no rows were updated, try using title and content (fallback method)
                Log.w("SingleMatrix", "Không tìm thấy task với ID " + task.getId() + ", thử dùng title và content");
                
                whereClause = "title = ? AND content = ? AND priority = ?";
                whereArgs = new String[]{task.getTitle(), task.getDescription(), String.valueOf(task.getPriority())};
                
                result = db.update("tbl_task", values, whereClause, whereArgs);
                
                if (result > 0) {
                    Log.d("SingleMatrix", "Task đã được cập nhật bằng phương thức thay thế");
                } else {
                    Log.e("SingleMatrix", "Vẫn không thể cập nhật task");
                }
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
