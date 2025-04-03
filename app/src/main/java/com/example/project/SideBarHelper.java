package com.example.project;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import android.view.ViewGroup;

public class SideBarHelper {

    private static AlertDialog dialog;

    public interface SideBarCallback {
        void onTaskCategorySelected(String category);
    }

    // Interface to get tasks
    public interface TaskProvider {
        List<Task> getAllTasks();
    }

    public static void showSideBar(Context context, SideBarCallback callback, TaskProvider taskProvider) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.sidde_bar, null);

        RecyclerView rvTaskTitles = dialogView.findViewById(R.id.rv_task_titles);
        LinearLayout btnAddList = dialogView.findViewById(R.id.btn_add_list);

        // Set up RecyclerView
        rvTaskTitles.setLayoutManager(new LinearLayoutManager(context));

        // Get unique categories from tasks
        List<String> taskCategories = getUniqueCategories(taskProvider.getAllTasks());

        // Add a "All Tasks" option
        if (!taskCategories.contains("Tất cả công việc")) {
            taskCategories.add(0, "Tất cả công việc");
        }

        // Set the adapter with custom layout
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
            showAddCategoryDialog(context);
        });

        builder.setView(dialogView);
        dialog = builder.create();

        // Show the dialog
        dialog.show();

        // Configure the dialog window
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.gravity = Gravity.START | Gravity.TOP;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.width = (int)(context.getResources().getDisplayMetrics().widthPixels * 0.85);
        params.y = 0;
        params.x = 0;
        dialog.getWindow().setAttributes(params);
    }

    // Get unique categories from all tasks
    private static List<String> getUniqueCategories(List<Task> allTasks) {
        Set<String> uniqueCategories = new HashSet<>();

        for (Task task : allTasks) {
            String category = task.getCategory();
            if (category != null && !category.isEmpty()) {
                uniqueCategories.add(category);
            }
        }

        return new ArrayList<>(uniqueCategories);
    }

    // Show dialog to add a new category
    private static void showAddCategoryDialog(Context context) {
        // This would allow users to create a new category without a task
        Toast.makeText(context, "Chức năng thêm danh mục mới", Toast.LENGTH_SHORT).show();
    }

    // Adapter using the custom layout
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

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Use custom layout for categories
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_task_category, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            String category = categories.get(position);
            holder.tvCategoryName.setText(category);

            // Set icon tint based on position
            int[] colors = {0xFF3498DB, 0xFFE74C3C, 0xFF2ECC71, 0xFFF39C12, 0xFF9B59B6, 0xFF1ABC9C};
            int colorIndex = position % colors.length;
            holder.ivCategoryIcon.setColorFilter(colors[colorIndex]);

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
            TextView tvCategoryName;
            ImageView ivCategoryIcon;

            ViewHolder(View itemView) {
                super(itemView);
                tvCategoryName = itemView.findViewById(R.id.tv_category_name);
                ivCategoryIcon = itemView.findViewById(R.id.iv_category_icon);
            }
        }
    }
}