package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.HashMap;

public class Matrix_Eisenhower extends AppCompatActivity {

    private FloatingActionButton addButton;
    private LinearLayout navigateTo;
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

        // Chuyển hướng đến trang đơn
        navigateTo = findViewById(R.id.priority_1);
        navigateTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Matrix_Eisenhower.this, SingleMatrix.class);
                startActivity(intent);
            }
        });


        // Nút thêm task
        addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TaskDialogHelper.showInputDialog(Matrix_Eisenhower.this, new TaskDialogHelper.TaskDialogCallback() {
                    @Override
                    public void onTaskAdded(Task task) {
                        addTaskToUI(task);
                    }
                });
            }
        });
    }

    // Hàm thêm task vào UI theo priority
    private void addTaskToUI(Task task) {
        LinearLayout priorityLayout = priorityMap.get(task.getPriority());
        if (priorityLayout != null) {
            // Tạo một LinearLayout
            LinearLayout taskContainer = new LinearLayout(this);
            taskContainer.setOrientation(LinearLayout.VERTICAL);
            taskContainer.setPadding(8, 4, 8, 4);

            // Tạo TextView cho Task Name
            TextView taskNameView = new TextView(this);
            taskNameView.setText("• " + task.getTaskName());
            taskNameView.setTextColor(getResources().getColor(android.R.color.white));
            taskNameView.setTextSize(16);

            // Tạo TextView cho Task Description
            TextView taskDescView = new TextView(this);
            taskDescView.setText(task.getTaskDescription());
            taskDescView.setTextColor(getResources().getColor(android.R.color.darker_gray));
            taskDescView.setTextSize(14);
            taskDescView.setPadding(24, 0, 0, 0);

            // Thêm các TextView vào taskContainer
            taskContainer.addView(taskNameView);
            taskContainer.addView(taskDescView);

            // Thêm taskContainer vào priorityLayout
            priorityLayout.addView(taskContainer);
        }
    }
}
