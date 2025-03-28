package com.example.project;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AlertDialog;

public class TaskDialogHelper {

    public interface TaskDialogCallback {
        void onTaskAdded(Task task);
    }

    public static void showInputDialog(Context context, TaskDialogCallback callback) {
        showInputDialog(context, -1, callback);
    }

    public static void showInputDialog(Context context, int preSelectedPriority, TaskDialogCallback callback) {
        // Inflate the dialog layout
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_task, null);
        
        // Get references to views
        EditText edtTaskName = dialogView.findViewById(R.id.edt_task_name);
        EditText edtTaskDescription = dialogView.findViewById(R.id.edt_task_description);
        RadioGroup rgPriority = dialogView.findViewById(R.id.rg_priority);
        
        // If a priority is pre-selected, select the corresponding radio button
        if (preSelectedPriority > 0 && preSelectedPriority <= 4) {
            int radioButtonId = 0;
            switch (preSelectedPriority) {
                case 1:
                    radioButtonId = R.id.rb_priority1;
                    break;
                case 2:
                    radioButtonId = R.id.rb_priority2;
                    break;
                case 3:
                    radioButtonId = R.id.rb_priority3;
                    break;
                case 4:
                    radioButtonId = R.id.rb_priority4;
                    break;
            }
            
            if (radioButtonId != 0) {
                rgPriority.check(radioButtonId);
            }
        }
        
        // Build the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Add Task");
        builder.setView(dialogView);
        
        // Add buttons
        builder.setPositiveButton("Add", (dialog, which) -> {
            // Get task name and description
            String taskName = edtTaskName.getText().toString().trim();
            String taskDescription = edtTaskDescription.getText().toString().trim();
            
            // Determine priority from selected radio button
            int selectedId = rgPriority.getCheckedRadioButtonId();
            int priority = 1; // Default to priority 1
            
            // Map radio button ID to priority
            if (selectedId == R.id.rb_priority1) {
                priority = 1;
            } else if (selectedId == R.id.rb_priority2) {
                priority = 2;
            } else if (selectedId == R.id.rb_priority3) {
                priority = 3;
            } else if (selectedId == R.id.rb_priority4) {
                priority = 4;
            }
            
            // Create task object
            Task task = new Task(taskName, taskDescription, priority);
            
            // Notify callback
            if (callback != null) {
                callback.onTaskAdded(task);
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        
        // Show the dialog
        builder.create().show();
    }
}
