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
        fabAdd.setOnClickListener(v -> {
            TaskDialogHelper.showInputDialog(SingleMatrix.this, currentPriority, new TaskDialogHelper.TaskDialogCallback() {
                @Override
                public void onTaskAdded(Task task) {
                    // Save the task to the database
                    Toast.makeText(SingleMatrix.this, "Thêm thành công", Toast.LENGTH_SHORT).show();
                    // Add the task to the list and notify the adapter
                    taskList.add(task);
                    taskAdapter.notifyDataSetChanged();
                }
            });
        });
    }

    // Load tasks for the selected priority from the database
    private void loadTasksForPriority(int priority) {
        SQLiteDatabase db = DatabaseHelper.getInstance(this).openDatabase();
        if (db == null) {
            Log.e("SingleMatrix", "Database không tồn tại hoặc không thể mở");
            return;
        }

        try {
            Cursor cursor = db.rawQuery("SELECT * FROM tbl_task WHERE priority = ?",
                    new String[]{String.valueOf(priority)});
            if (cursor.moveToFirst()) {
                do {
                    int titleIndex = cursor.getColumnIndex("title");
                    String title = (titleIndex >= 0) ? cursor.getString(titleIndex) : "";


                    int descriptionIndex = cursor.getColumnIndex("description");
                    String description = (descriptionIndex >= 0) ? cursor.getString(descriptionIndex) : "";


                    int reminderDateIndex = cursor.getColumnIndex("reminder_date");
                    String reminderDate = (reminderDateIndex >= 0) ? cursor.getString(reminderDateIndex) : "";


                    int completeIndex = cursor.getColumnIndex("is_completed");
                    boolean isCompleted = (completeIndex >= 0) && cursor.getInt(completeIndex) == 1;


                    Task task = new Task(title, description, priority);
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

            // Use the correct query based on identifying fields
            String whereClause = "title = ? AND description = ? AND priority = ?";
            String[] whereArgs = {task.getTitle(), task.getDescription(), String.valueOf(task.getPriority())};

            long result = db.update("tbl_task", values, whereClause, whereArgs);

            if (result == -1) {
                Log.e("SingleMatrix", "Lỗi khi cập nhật trạng thái task vào database");
            } else if (result == 0) {
                Log.e("SingleMatrix", "Không tìm thấy task để cập nhật");
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
