package com.example.project;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
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
        // Create dialog if it doesn't exist
        if (dialog == null || !dialog.isShowing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            View view = LayoutInflater.from(context).inflate(R.layout.side_bar, null);
            builder.setView(view);
            
            dialog = builder.create();
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            
            // Find views in the sidebar layout
            ImageView btnSetting = view.findViewById(R.id.btnSetting);
            LinearLayout categorySection = view.findViewById(R.id.category_section);
            LinearLayout btnAddList = view.findViewById(R.id.btn_add_list);
            
            // Load lists from database
            List<String> lists = getListsFromDatabase(context);
            addCategoryItems(context, categorySection, lists, callback);
            
            
            btnAddList.setOnClickListener(v -> {
                // Create dialog to get new list name
                final EditText input = new EditText(context);
                input.setHint("Tên danh sách mới");
                input.setTextColor(Color.WHITE);
                input.setHintTextColor(Color.GRAY);
                
                AlertDialog.Builder addListDialog = new AlertDialog.Builder(context);
                addListDialog.setTitle("Thêm danh sách mới");
                addListDialog.setView(input);
                
                // Add buttons
                addListDialog.setPositiveButton("Thêm", (dialogInterface, which) -> {
                    String listName = input.getText().toString().trim();
                    if (!listName.isEmpty()) {
                        // Add list to database
                        int newListId = addListToDatabase(context, listName);
                        if (newListId != -1) {
                            Toast.makeText(context, "Đã thêm danh sách: " + listName, Toast.LENGTH_SHORT).show();
                            
                            // Refresh sidebar
                            List<String> updatedLists = getListsFromDatabase(context);
                            addCategoryItems(context, categorySection, updatedLists, callback);
                        } else {
                            Toast.makeText(context, "Không thể thêm danh sách", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context, "Tên danh sách không thể trống", Toast.LENGTH_SHORT).show();
                    }
                });
                
                addListDialog.setNegativeButton("Hủy", (dialogInterface, which) -> dialogInterface.cancel());
                
                addListDialog.show();
            });
        }

        dialog.show();

        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.gravity = Gravity.START | Gravity.TOP;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.width = (int)(context.getResources().getDisplayMetrics().widthPixels * 0.85);
        params.y = 0;
        params.x = 0;
        dialog.getWindow().setAttributes(params);
    }
    
    // New method to add a list to the database
    private static int addListToDatabase(Context context, String listName) {
        SQLiteDatabase db = null;
        int newListId = -1;
        
        try {
            db = DatabaseHelper.getInstance(context).openDatabase();
            
            // Check if list with this name already exists
            Cursor cursor = db.rawQuery("SELECT id FROM tbl_list WHERE name = ?", new String[]{listName});
            boolean exists = cursor.getCount() > 0;
            cursor.close();
            
            if (exists) {
                // List already exists
                Toast.makeText(context, "Danh sách này đã tồn tại", Toast.LENGTH_SHORT).show();
                return -1;
            }
            
            // Insert the new list
            ContentValues values = new ContentValues();
            values.put("name", listName);
            values.put("icon", (String) null); // No icon initially
            
            newListId = (int) db.insert("tbl_list", null, values);
            
            // Create a default category for this list
            if (newListId != -1) {
                ContentValues categoryValues = new ContentValues();
                categoryValues.put("name", "Default");
                categoryValues.put("list_id", newListId);
                db.insert("tbl_category", null, categoryValues);
            }
            
        } catch (Exception e) {
            Log.e("SideBarHelper", "Error adding list to database", e);
            newListId = -1;
        } finally {
            if (db != null) {
                DatabaseHelper.getInstance(context).closeDatabase();
            }
        }
        
        return newListId;
    }

    // Method to get lists from the database
    private static List<String> getListsFromDatabase(Context context) {
        List<String> lists = new ArrayList<>();
        SQLiteDatabase db = null;
        
        try {
            db = DatabaseHelper.getInstance(context).openDatabase();
            Cursor cursor = db.rawQuery("SELECT name FROM tbl_list", null);
            
            while (cursor.moveToNext()) {
                String listName = cursor.getString(0);
                lists.add(listName);
            }
            
            cursor.close();
        } catch (Exception e) {
            Log.e("SideBarHelper", "Error loading lists from database", e);
        } finally {
            if (db != null) {
                DatabaseHelper.getInstance(context).closeDatabase();
            }
        }
        
        return lists;
    }

    // ✅ Method to add category items
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
                if (callback != null) {
                    callback.onTaskCategorySelected(categoryName);
                }
                
                // Get the list ID from the database using the name
                int listId = getListIdByName(context, categoryName);
                if (listId != -1) {
                    // Refresh the HomeActivity with tasks from this list
                    Intent intent = new Intent(context, HomeActivity.class);
                    intent.putExtra("listId", listId);
                    intent.putExtra("listName", categoryName);
                    context.startActivity(intent);
                }
                
                dialog.dismiss(); // Close sidebar
            });

            itemLayout.addView(icon);
            itemLayout.addView(text);
            categorySection.addView(itemLayout);
        }
    }
    
    // Method to get list ID by name
    private static int getListIdByName(Context context, String listName) {
        SQLiteDatabase db = null;
        int listId = -1;
        
        try {
            db = DatabaseHelper.getInstance(context).openDatabase();
            Cursor cursor = db.rawQuery("SELECT id FROM tbl_list WHERE name = ?", new String[]{listName});
            
            if (cursor.moveToFirst()) {
                listId = cursor.getInt(0);
            }
            
            cursor.close();
        } catch (Exception e) {
            Log.e("SideBarHelper", "Error getting list ID", e);
        } finally {
            if (db != null) {
                DatabaseHelper.getInstance(context).closeDatabase();
            }
        }
        
        return listId;
    }

    private static int dpToPx(Context context, int dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }
    
    // Method to dismiss sidebar if it's showing
    public static void dismissSideBar() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}
