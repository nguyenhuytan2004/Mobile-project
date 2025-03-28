package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton fabAdd;
    private ConstraintLayout taskInputLayout;
    private EditText etTaskName, etTaskDescription;
    private Button btnAddTask;
    private RecyclerView rvTaskList;

    private ArrayList<Task> taskList;
    private TaskAdapter taskAdapter;

    ImageView focusTab;
    ImageView calendarTab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ánh xạ các thành phần trong giao diện
        fabAdd = findViewById(R.id.fab_add);
        taskInputLayout = findViewById(R.id.task_input_layout);
        etTaskName = findViewById(R.id.et_task_name);
        etTaskDescription = findViewById(R.id.et_task_description);
        btnAddTask = findViewById(R.id.btn_add_task);
        rvTaskList = findViewById(R.id.rv_task_list);
        focusTab = findViewById(R.id.focusTab);
        calendarTab = findViewById(R.id.calendarTab);

        // Khởi tạo danh sách công việc và adapter
        taskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(taskList);
        rvTaskList.setLayoutManager(new LinearLayoutManager(this));
        rvTaskList.setAdapter(taskAdapter);

        // Sự kiện khi nhấn Floating Action Button (FAB)
        fabAdd.setOnClickListener(v -> {
            AddTaskDialog dialog = new AddTaskDialog(newTask -> {
                taskList.add(newTask);
                taskAdapter.notifyDataSetChanged();
            });
            dialog.show(getSupportFragmentManager(), "AddTaskDialog");
        });

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

        // Sự kiện khi nhấn nút "Thêm"
//        btnAddTask.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String taskName = etTaskName.getText().toString().trim();
//                String taskDescription = etTaskDescription.getText().toString().trim();
//
//                if (!taskName.isEmpty()) {
//                    // Thêm công việc vào danh sách
//                    Task newTask = new Task(taskName, taskDescription);
//                    taskList.add(newTask);
//                    taskAdapter.notifyDataSetChanged(); // Cập nhật RecyclerView
//
//                    // Ẩn form nhập công việc và hiện lại FAB
//                    taskInputLayout.setVisibility(View.GONE);
//                    fabAdd.setVisibility(View.VISIBLE);
//
//                    // Xóa nội dung trong EditText sau khi thêm
//                    etTaskName.setText("");
//                    etTaskDescription.setText("");
//                } else {
//                    // Thông báo khi ô nhập liệu trống
//                    Toast.makeText(MainActivity.this, "Vui lòng nhập tên công việc!", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
    }
}
