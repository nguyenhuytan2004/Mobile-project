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
    }
    
    // Set up click listeners for priority quadrants
    private void setupPriorityClickListeners() {
        // For Priority 1: Khẩn cấp và Quan trọng
        priorityMap.get(1).setOnClickListener(v -> navigateToSingleMatrix(1, "Khẩn cấp và Quan trọng"));
        
        // For Priority 2: Không gấp mà quan trọng
        priorityMap.get(2).setOnClickListener(v -> navigateToSingleMatrix(2, "Không gấp mà quan trọng"));
        
        // For Priority 3: Khẩn cấp nhưng không quan trọng
        priorityMap.get(3).setOnClickListener(v -> navigateToSingleMatrix(3, "Khẩn cấp nhưng không quan trọng"));
        
        // For Priority 4: Không cấp bách và không quan trọng
        priorityMap.get(4).setOnClickListener(v -> navigateToSingleMatrix(4, "Không cấp bách và không quan trọng"));
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
