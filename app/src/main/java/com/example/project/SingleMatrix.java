package com.example.project;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class SingleMatrix extends AppCompatActivity {

    private FloatingActionButton fabAdd;
    private RecyclerView rvTaskList;
    private ArrayList<Task> taskList;
    private TaskAdapter taskAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_matrix);  // Set the layout to activity_single_matrix.xml

        // Initialize views
        fabAdd = findViewById(R.id.floatingActionButton2);
        rvTaskList = findViewById(R.id.recyclerView);  // RecyclerView instead of GridView

        // Set up RecyclerView with a vertical LinearLayoutManager
        rvTaskList.setLayoutManager(new LinearLayoutManager(this));

        // Initialize task list and adapter
        taskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(taskList);  // Using TaskAdapter to display tasks

        // Set the adapter to the RecyclerView
        rvTaskList.setAdapter(taskAdapter);

        // Set up the FloatingActionButton click listener to add new tasks
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Display input dialog to add task name and description
                showInputDialog();
            }
        });
    }

    private void showInputDialog() {
        // Tạo một AlertDialog mới
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Task");

        // Tạo một LinearLayout để chứa các trường nhập liệu
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16, 16, 16, 16);
        //layout.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));  // Màu xám nền cho hộp thoại

        // Tạo các EditText cho tên và mô tả công việc
        final EditText taskNameInput = new EditText(this);
        taskNameInput.setHint("Enter Task Name");

        final EditText taskDescriptionInput = new EditText(this);
        taskDescriptionInput.setHint("Enter Task Description");

        // Tạo Spinner để chọn mức độ ưu tiên (Priority)
        final Spinner prioritySpinner = new Spinner(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.priority_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        prioritySpinner.setAdapter(adapter);

        // Thêm các trường nhập liệu vào LinearLayout
        layout.addView(taskNameInput);
        layout.addView(taskDescriptionInput);
        layout.addView(prioritySpinner);  // Thêm Spinner vào layout

        // Thiết lập layout cho dialog
        builder.setView(layout);

        // Thêm nút xác nhận để thêm task
        builder.setPositiveButton("Add Task", (dialog, which) -> {
            String taskName = taskNameInput.getText().toString();
            String taskDescription = taskDescriptionInput.getText().toString();
            int priority = prioritySpinner.getSelectedItemPosition() + 1;  // Lấy mức độ ưu tiên (1 đến 4)

            if (!taskName.isEmpty() && !taskDescription.isEmpty()) {
                addTask(taskName, taskDescription, priority);  // Thêm task vào danh sách
                Toast.makeText(SingleMatrix.this, "Task Added", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(SingleMatrix.this, "Please enter both task name and description", Toast.LENGTH_SHORT).show();
            }
        });

        // Thêm nút hủy
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();  // Hiển thị dialog
    }

    // Method to add a task to the task list
    private void addTask(String taskName, String taskDescription, int priority) {
        taskList.add(new Task(taskName, taskDescription, priority));  // Thêm task với mức độ ưu tiên
        taskAdapter.notifyDataSetChanged();  // Cập nhật lại RecyclerView
    }
}
