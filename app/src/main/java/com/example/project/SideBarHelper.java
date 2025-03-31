package com.example.project;

import android.app.AlertDialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import android.view.ViewGroup;
import android.widget.Toast;

public class SideBarHelper {

    private static AlertDialog dialog;

    public interface SideBarCallback {
        void onTaskCategorySelected(String category);
    }

    public static void showSideBar(Context context, SideBarCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.sidde_bar, null);

        RecyclerView rvTaskTitles = dialogView.findViewById(R.id.rv_task_titles);
        LinearLayout btnAddList = dialogView.findViewById(R.id.btn_add_list);

        // Set up RecyclerView
        rvTaskTitles.setLayoutManager(new LinearLayoutManager(context));

        // Sample data - you could replace this with data from your database
        List<String> taskCategories = new ArrayList<>();
        taskCategories.add("Công việc hàng ngày");
        taskCategories.add("Công việc tuần này");
        taskCategories.add("Dự án cá nhân");
        taskCategories.add("Mua sắm");

        // Set the adapter
        TaskCategoryAdapter adapter = new TaskCategoryAdapter(taskCategories, taskCategory -> {
            if (callback != null) {
                callback.onTaskCategorySelected(taskCategory);
            }
            // Dismiss the dialog when a category is selected
            if (dialog != null) dialog.dismiss();
        });

        rvTaskTitles.setAdapter(adapter);

        // Set up the add button
        btnAddList.setOnClickListener(v -> {
            // Show a dialog to add a new category (simplified for this example)
            Toast.makeText(context, "Chức năng thêm danh sách mới", Toast.LENGTH_SHORT).show();
        });

        builder.setView(dialogView);
        dialog = builder.create();

        // Show the dialog
        dialog.show();

        // Configure the dialog window to appear at the left edge and full height
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.gravity = Gravity.START | Gravity.TOP;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.width = (int)(context.getResources().getDisplayMetrics().widthPixels * 0.85); // 85% of screen width
        params.y = 0; // Position at top
        params.x = 0; // Position at start (left)
        dialog.getWindow().setAttributes(params);
    }

    // Adapter for the RecyclerView
    private static class TaskCategoryAdapter extends RecyclerView.Adapter<TaskCategoryAdapter.ViewHolder> {

        private final List<String> categories;
        private final OnCategoryClickListener listener;

        public interface OnCategoryClickListener {
            void onCategoryClick(String category);
        }

        public TaskCategoryAdapter(List<String> categories, OnCategoryClickListener listener) {
            this.categories = categories;
            this.listener = listener;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            String category = categories.get(position);
            holder.textView.setText(category);
            holder.textView.setTextColor(holder.itemView.getResources().getColor(android.R.color.white));

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCategoryClick(category);
                }
            });
        }

        @Override
        public int getItemCount() {
            return categories.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;

            ViewHolder(View itemView) {
                super(itemView);
                textView = itemView.findViewById(android.R.id.text1);
            }
        }
    }
}