package com.example.project;


import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;


public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {


    private final ArrayList<Task> taskList;
    private final TaskCompletionListener completionListener;


    public interface TaskCompletionListener {
        void onTaskCompletionChanged(Task task, boolean isCompleted);
    }


    public TaskAdapter(ArrayList<Task> taskList, TaskCompletionListener listener) {
        this.taskList = taskList;
        this.completionListener = listener;
    }


    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);


        // Set task title and description
        holder.tvTaskTitle.setText(task.getTitle());
        holder.tvTaskDesc.setText(task.getDescription());


        // Show/hide reminder date if exists
        if (task.hasReminder() && holder.tvTaskReminder != null) {
            holder.tvTaskReminder.setVisibility(View.VISIBLE);
            holder.tvTaskReminder.setText("⏰ " + task.getReminderDate());
        } else if (holder.tvTaskReminder != null) {
            holder.tvTaskReminder.setVisibility(View.GONE);
        }


        // Set checkbox state based on task completion status
        holder.cbTaskComplete.setChecked(task.isCompleted());


        // Apply strike-through formatting if task is completed
        if (task.isCompleted()) {
            holder.tvTaskTitle.setPaintFlags(holder.tvTaskTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.tvTaskTitle.setPaintFlags(holder.tvTaskTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }


        // Set checkbox listener
        holder.cbTaskComplete.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Update the strikethrough formatting
            if (isChecked) {
                holder.tvTaskTitle.setPaintFlags(holder.tvTaskTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                holder.tvTaskTitle.setPaintFlags(holder.tvTaskTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }


            // Notify the activity about task completion status change
            if (completionListener != null) {
                completionListener.onTaskCompletionChanged(task, isChecked);
            }
        });
    }


    @Override
    public int getItemCount() {
        return taskList.size();
    }


    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTaskTitle;
        TextView tvTaskDesc;
        CheckBox cbTaskComplete;
        TextView tvTaskReminder;


        TaskViewHolder(View itemView) {
            super(itemView);
            tvTaskTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvTaskDesc = itemView.findViewById(R.id.tvTaskDescription);
            cbTaskComplete = itemView.findViewById(R.id.cb_task_complete);
            tvTaskReminder = itemView.findViewById(R.id.tvTaskReminder);
        }
    }
}
