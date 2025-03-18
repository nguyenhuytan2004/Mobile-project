package com.example.project;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

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
        setContentView(R.layout.single_matrix);  // Use the single_matrix layout

        // Initialize views
        fabAdd = findViewById(R.id.floatingActionButton2);
        rvTaskList = findViewById(R.id.recyclerView);  // RecyclerView instead of GridView

        // Set up RecyclerView with a LinearLayoutManager
        rvTaskList.setLayoutManager(new LinearLayoutManager(this));

        // Initialize task list and adapter
        taskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(taskList);  // Using TaskAdapter to display tasks

        // Set the adapter to the RecyclerView
        rvTaskList.setAdapter(taskAdapter);

        // Set FloatingActionButton click listener to add tasks
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add a task with a name and description when the FAB is clicked
                addTask("New Task " + (taskList.size() + 1));  // Example task name
                Toast.makeText(SingleMatrix.this, "Task Added", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to add a task to the task list
    private void addTask(String taskName) {
        taskList.add(new Task(taskName, "This is a description for " + taskName));  // Add task with name and description
        taskAdapter.notifyDataSetChanged();  // Notify the adapter to refresh the RecyclerView
    }
}
