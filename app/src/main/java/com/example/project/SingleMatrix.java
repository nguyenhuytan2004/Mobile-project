package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;

public class SingleMatrix extends AppCompatActivity {

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
        
        // Load tasks for the current priority - this would be replaced by your database query
        // For now, we'll use dummy data
        loadTasksForPriority(currentPriority);
        
        taskAdapter = new TaskAdapter(taskList);
        rvTaskList.setAdapter(taskAdapter);

        // backBtn click listener
        backBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick (View v){
                finish(); // Close this activity and return to the previous one
            }
        });

        // FloatingActionButton click listener
        fabAdd.setOnClickListener(v -> {
            TaskDialogHelper.showInputDialog(SingleMatrix.this, currentPriority, new TaskDialogHelper.TaskDialogCallback() {
                @Override
                public void onTaskAdded(Task task) {
                    taskList.add(task);
                    taskAdapter.notifyItemInserted(taskList.size() - 1);
                }
            });
        });
    }
    
    // Load tasks for the selected priority
    private void loadTasksForPriority(int priority) {
        // In a real application, this would query your database
        // For now, we'll just create some example tasks
        
        // Clear existing tasks
        taskList.clear();
        
        // Example tasks (in a real app, you would load from a database or shared storage)
        if (priority == 1) {
            taskList.add(new Task("Submit project report", "Complete the project documentation", 1));
            taskList.add(new Task("Client meeting", "Prepare presentation for client", 1));
        } else if (priority == 2) {
            taskList.add(new Task("Learn Android development", "Complete the course on Udemy", 2));
            taskList.add(new Task("Weekly planning", "Set goals for the week", 2));
        } else if (priority == 3) {
            taskList.add(new Task("Reply to emails", "Check inbox and respond to messages", 3));
            taskList.add(new Task("Team update call", "Join the daily standup", 3));
        } else if (priority == 4) {
            taskList.add(new Task("Browse social media", "Check updates on Twitter", 4));
            taskList.add(new Task("Organize desk", "Clean up workspace", 4));
        }
    }
}