package com.example.project;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class AddTaskDialog extends BottomSheetDialogFragment {

    private EditText etTaskName, etTaskDescription;
    private ImageView btnDate, btnTag, btnAttach, btnMore;
    private ImageButton btnAddTask;
    private OnTaskAddedListener listener;

    public interface OnTaskAddedListener {
        void onTaskAdded(Task task);
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

        btnAddTask.setOnClickListener(v -> {
            String taskName = etTaskName.getText().toString().trim();
            String taskDesc = etTaskDescription.getText().toString().trim();

            if (!taskName.isEmpty()) {
                Task newTask = new Task(taskName, taskDesc);
                listener.onTaskAdded(newTask);
                dismiss();
            } else {
                etTaskName.setError("Nhập tên công việc");
            }
        });

        return view;
    }
}