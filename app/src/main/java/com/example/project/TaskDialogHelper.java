package com.example.project;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.FragmentActivity;

public class TaskDialogHelper {

    private static String reminderDate = ""; // To store selected reminder date

    public interface TaskDialogCallback {
        void onTaskAdded(Task task);
    }

    public static void showInputDialog(Context context, int defaultPriority, TaskDialogCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_task_input, null);

        EditText etTitle = dialogView.findViewById(R.id.etTaskTitle);
        EditText etDescription = dialogView.findViewById(R.id.etTaskDescription);
        RadioGroup rgPriority = dialogView.findViewById(R.id.rgPriority);
        Button btnAdd = dialogView.findViewById(R.id.btnAddTask);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        ImageButton btnSetReminder = dialogView.findViewById(R.id.btnSetReminder);
        TextView tvReminderInfo = dialogView.findViewById(R.id.tvReminderInfo);

        // Set default priority
        switch (defaultPriority) {
            case 1:
                rgPriority.check(R.id.rbPriority1);
                break;
            case 2:
                rgPriority.check(R.id.rbPriority2);
                break;
            case 3:
                rgPriority.check(R.id.rbPriority3);
                break;
            case 4:
                rgPriority.check(R.id.rbPriority4);
                break;
        }

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Set reminder button click listener
        btnSetReminder.setOnClickListener(v -> {
            if (context instanceof FragmentActivity) {
                FragmentActivity activity = (FragmentActivity) context;
                CalendarDialog reminderDialog = new CalendarDialog();

                reminderDialog.setOnDateSelectedListener(new CalendarDialog.OnDateSelectedListener() {
                    @Override
                    public void onDateSelected(String date) {
                        reminderDate = date;
                        if (!date.isEmpty()) {
                            tvReminderInfo.setVisibility(View.VISIBLE);
                            tvReminderInfo.setText("Nhắc nhở: " + date);
                        } else {
                            tvReminderInfo.setVisibility(View.GONE);
                        }
                    }
                });

                reminderDialog.show(activity.getSupportFragmentManager(), "reminder_dialog");
            }
        });

        btnAdd.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String description = etDescription.getText().toString().trim();

            // Get selected priority (1-4)
            int priority = defaultPriority; // Use the default priority
            int selectedId = rgPriority.getCheckedRadioButtonId();

            if (selectedId == R.id.rbPriority1) {
                priority = 1;
            } else if (selectedId == R.id.rbPriority2) {
                priority = 2;
            } else if (selectedId == R.id.rbPriority3) {
                priority = 3;
            } else if (selectedId == R.id.rbPriority4) {
                priority = 4;
            }

            if (title.isEmpty()) {
                Toast.makeText(context, "Vui lòng nhập tiêu đề task", Toast.LENGTH_SHORT).show();
                return;
            }

            Task newTask = new Task(title, description, priority);
            if (!reminderDate.isEmpty()) {
                newTask.setReminderDate(reminderDate);
            }

            callback.onTaskAdded(newTask);
            reminderDate = ""; // Reset reminder date for next use
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    // Overloaded method for backward compatibility
    public static void showInputDialog(Context context, TaskDialogCallback callback) {
        showInputDialog(context, 1, callback); // Default priority is 1
    }
}