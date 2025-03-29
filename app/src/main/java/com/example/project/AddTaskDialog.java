package com.example.project;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class AddTaskDialog extends BottomSheetDialogFragment {

    private EditText etTaskName, etTaskDescription;
    private ImageView btnDate, btnTag, btnAttach, btnMore;
    private ImageButton btnAddTask;
    private TextView tvReminderInfo;
    private OnTaskAddedListener listener;
    private String reminderDate = ""; // To store selected reminder date
    private int priority = 0; // Default priority

    public interface OnTaskAddedListener {
        void onTaskAdded(Task task);
    }

    public AddTaskDialog(OnTaskAddedListener listener, int priority) {
        this.listener = listener;
        this.priority = priority;
    }

    public AddTaskDialog(OnTaskAddedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_popup_add_task, container, false);

        etTaskName = view.findViewById(R.id.et_task_name);
        etTaskDescription = view.findViewById(R.id.et_task_description);
        btnDate = view.findViewById(R.id.btn_date);
        btnTag = view.findViewById(R.id.btn_tag);
        btnAttach = view.findViewById(R.id.btn_attach);
        btnMore = view.findViewById(R.id.btn_more);
        btnAddTask = view.findViewById(R.id.btn_add_task);
        tvReminderInfo = view.findViewById(R.id.tv_reminder_info);

        // Set up date button to open reminder dialog
        btnDate.setOnClickListener(v -> {
            SetReminderDialogFragment reminderDialog = new SetReminderDialogFragment();

            reminderDialog.setOnDateSelectedListener(new SetReminderDialogFragment.OnDateSelectedListener() {
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

            reminderDialog.show(getParentFragmentManager(), "reminder_dialog");
        });

        btnAddTask.setOnClickListener(v -> {
            String taskName = etTaskName.getText().toString().trim();
            String taskDesc = etTaskDescription.getText().toString().trim();

            if (!taskName.isEmpty()) {
                Task newTask = new Task(taskName, taskDesc, priority);
                if (!reminderDate.isEmpty()) {
                    newTask.setReminderDate(reminderDate);
                }
                listener.onTaskAdded(newTask);
                dismiss();
            } else {
                etTaskName.setError("Nhập tên công việc");
            }
        });

        return view;
    }
}