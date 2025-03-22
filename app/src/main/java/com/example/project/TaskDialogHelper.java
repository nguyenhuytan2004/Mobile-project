package com.example.project;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class TaskDialogHelper {

    public interface TaskDialogCallback {
        void onTaskAdded(Task task);
    }

    public static void showInputDialog(Context context, TaskDialogCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_task_input, null);
        builder.setView(view);

        EditText taskNameInput = view.findViewById(R.id.edit_task_name);
        EditText taskDescInput = view.findViewById(R.id.edit_task_description);
        Spinner prioritySpinner = view.findViewById(R.id.spinner_priority);
        Button addButton = view.findViewById(R.id.button_add_task);

        AlertDialog dialog = builder.create();
        dialog.show();

        addButton.setOnClickListener(v -> {
            String taskName = taskNameInput.getText().toString().trim();
            String taskDesc = taskDescInput.getText().toString().trim();
            String priorityText = prioritySpinner.getSelectedItem().toString().split(" - ")[0]; // Lấy phần số trước dấu " - "
            int priority = Integer.parseInt(priorityText);

            if (!taskName.isEmpty()) {
                Task newTask = new Task(taskName, taskDesc, priority);
                callback.onTaskAdded(newTask);
                dialog.dismiss();
            }
        });
    }
}
