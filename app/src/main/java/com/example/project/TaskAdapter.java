package com.example.project;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private ArrayList<com.example.project.Task> taskList;

    public TaskAdapter(ArrayList<com.example.project.Task> taskList) {
        this.taskList = taskList;
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position) {
        com.example.project.Task task = taskList.get(position);
        holder.tvTaskName.setText(task.getTaskName());
        //holder.tvTaskDescription.setText(task.getTaskDescription());
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTaskName, tvTaskDescription;

        public TaskViewHolder(View itemView) {
            super(itemView);
            tvTaskName = itemView.findViewById(R.id.tv_task_name);
            //tvTaskDescription = itemView.findViewById(R.id.tv_task_description);
        }
    }
}
