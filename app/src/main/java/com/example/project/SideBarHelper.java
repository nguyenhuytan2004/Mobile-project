package com.example.project;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import android.view.ViewGroup;

import androidx.fragment.app.FragmentActivity;

public class SideBarHelper {

    private static AlertDialog dialog;

    public interface SideBarCallback {
        void onTaskCategorySelected(String category);
    }

    public interface TaskProvider {
        List<Task> getAllTasks();
    }

    public static void showSideBar(Context context, SideBarCallback callback, TaskProvider taskProvider) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.side_bar, null);

        LinearLayout btnAddList = dialogView.findViewById(R.id.btn_add_list);
        ImageView userAvatar = dialogView.findViewById(R.id.img_user_profile);
        LinearLayout categorySection = dialogView.findViewById(R.id.category_section); // Lấy đúng view

        ImageView btnSetting = dialogView.findViewById(R.id.btnSetting);
        btnSetting.setOnClickListener(v -> {
            Intent intent = new Intent(context, Setting.class);
            context.startActivity(intent);
        });

        List<String> taskCategories = getUniqueCategories(taskProvider.getAllTasks());
        if (!taskCategories.contains("Tất cả công việc") && !taskCategories.contains("Hôm nay")) {
            taskCategories.add(0, "Tất cả công việc");
            taskCategories.add(1, "Hôm nay");
        }
        Log.d("TAG", "Task Categories: " + taskCategories);
        // ✅ Gọi hàm addCategoryItems với context + categorySection
        addCategoryItems(context, categorySection, taskCategories, callback);

        btnAddList.setOnClickListener(v -> {
            Toast.makeText(context, "Thêm danh sách công việc", Toast.LENGTH_SHORT).show();
        });

        userAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(context, Setting.class);
            context.startActivity(intent);
        });

        builder.setView(dialogView);
        dialog = builder.create();

        dialog.show();

        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.gravity = Gravity.START | Gravity.TOP;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.width = (int)(context.getResources().getDisplayMetrics().widthPixels * 0.85);
        params.y = 0;
        params.x = 0;
        dialog.getWindow().setAttributes(params);
    }

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


    // ✅ Hàm static để thêm các category
    private static void addCategoryItems(Context context, LinearLayout categorySection, List<String> categories, SideBarCallback callback) {
        categorySection.removeAllViews();

        for (String categoryName : categories) {
            LinearLayout itemLayout = new LinearLayout(context);
            itemLayout.setOrientation(LinearLayout.HORIZONTAL);
            itemLayout.setPadding(0, dpToPx(context, 12), 0, dpToPx(context, 12));
            itemLayout.setClickable(true);

            // Icon
            ImageView icon = new ImageView(context);
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
                    dpToPx(context, 24), dpToPx(context, 24));
            iconParams.setMarginEnd(dpToPx(context, 16));
            icon.setLayoutParams(iconParams);
            icon.setImageResource(R.drawable.ic_task_list);

            // Text
            TextView text = new TextView(context);
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            text.setLayoutParams(textParams);
            text.setText(categoryName);
            text.setTextSize(16);
            text.setTextColor(Color.WHITE);
            text.setEllipsize(TextUtils.TruncateAt.END);
            text.setMaxLines(1);

            itemLayout.setOnClickListener(v -> {
                // Mở TaskByCategory activity
                Intent intent = new Intent(context, TaskByCategory.class);
                intent.putExtra("category", categoryName);
                intent.putExtra("title", categoryName); // Gửi thêm title nếu cần
                context.startActivity(intent);

                dialog.dismiss(); // Đóng sidebar
            });

            itemLayout.addView(icon);
            itemLayout.addView(text);
            categorySection.addView(itemLayout);
        }
    }

    private static int dpToPx(Context context, int dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }
}
