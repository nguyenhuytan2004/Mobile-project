package com.example.project;

import com.example.project.Task;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.*;

public class HomeActivity extends AppCompatActivity {

    private FloatingActionButton fabAdd;
    ImageView focusTab, calendarTab, sideBarView, matrixView, habitTab;
    private SQLiteDatabase db;
    private LoginSessionManager loginSessionManager;

    LocalDateTime now = LocalDateTime.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    TextView tvWelcome;

    private LinearLayout categoryContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home); // Layout chính sau đăng nhập

        loginSessionManager = LoginSessionManager.getInstance(this);
        if (!loginSessionManager.isLoggedIn()) {
            loginSessionManager.createSession(1); // tạo giả user
        }

        ReminderService.scheduleReminders(this);

        // Ánh xạ UI
        fabAdd = findViewById(R.id.fab_add);
        focusTab = findViewById(R.id.focusTab);
        calendarTab = findViewById(R.id.calendarTab);
        sideBarView = findViewById(R.id.sidebarView);
        matrixView = findViewById(R.id.matrixView);
        habitTab = findViewById(R.id.habitTab);

        tvWelcome = findViewById(R.id.tv_welcome);
        categoryContainer = findViewById(R.id.category_container);

        loadWelcomeCategoriesAndTasks(3); // khởi đầu là danh mục Welcom


        // FAB
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, NoteActivity.class);
            intent.putExtra("noteId", "-1");
            startActivity(intent);
        });

        // Sidebar
        sideBarView.setOnClickListener(v -> {
            Toast.makeText(this, "Sidebar clicked", Toast.LENGTH_SHORT).show();

        });

        // Tab
        focusTab.setOnClickListener(v -> startActivity(new Intent(this, FocusTab.class)));
        calendarTab.setOnClickListener(v -> startActivity(new Intent(this, CalendarTab.class)));
        matrixView.setOnClickListener(v -> startActivity(new Intent(this, Matrix_Eisenhower.class)));
        habitTab.setOnClickListener(v -> startActivity(new Intent(this, HabitActivity.class)));


    }

    @Override
    protected void onResume() {
        super.onResume();
        loadWelcomeCategoriesAndTasks(3);
    }

    private void loadWelcomeCategoriesAndTasks(int listId) {
        int userId = loginSessionManager.getUserId();

        categoryContainer.removeAllViews(); // clear UI trước

        try {
            db = DatabaseHelper.getInstance(this).openDatabase();

            Cursor categoryCursor = db.rawQuery(
                    "SELECT c.id, c.name FROM tbl_category c WHERE c.list_id = ?",
                    new String[]{String.valueOf(listId)}
            );

            while (categoryCursor.moveToNext()) {
                int categoryId = categoryCursor.getInt(0);
                String categoryName = categoryCursor.getString(1);

                // Inflate layout cho từng category
                View categoryView = LayoutInflater.from(this).inflate(R.layout.item_category_block, categoryContainer, false);

                TextView categoryTitle = categoryView.findViewById(R.id.category_title);
                LinearLayout taskListLayout = categoryView.findViewById(R.id.task_list_layout);

                categoryTitle.setText(categoryName);

                // Truy vấn các note trong category đó
                Cursor noteCursor = db.rawQuery(
                        "SELECT n.id, n.title, n.content, r.date " +
                                "FROM tbl_note n " +
                                "LEFT JOIN tbl_note_reminder r ON n.id = r.note_id " +
                                "WHERE n.user_id = ? AND n.category_id = ?",
                        new String[]{String.valueOf(userId), String.valueOf(categoryId)}
                );

                while (noteCursor.moveToNext()) {
                    int noteId = noteCursor.getInt(0);
                    String title = noteCursor.getString(1);
                    String content = noteCursor.isNull(2) ? "" : noteCursor.getString(2);
                    String date = noteCursor.isNull(3) ? "" : noteCursor.getString(3);

                    // Sử dụng lại hàm addNoteView đã có
                    addNoteView(taskListLayout, noteId, title, content, date);
                }

                noteCursor.close();

                // Thêm categoryView vào container
                categoryContainer.addView(categoryView);
            }

            categoryCursor.close();
        } catch (Exception e) {
            Log.e("HomeActivity", "Lỗi load category/note", e);
        } finally {
            if (db != null) DatabaseHelper.getInstance(this).closeDatabase();
        }
    }


    private void addNoteView(LinearLayout targetLayout, int noteId, String title, String content, String date) {
        View noteView = LayoutInflater.from(this).inflate(R.layout.note_item_in_main, targetLayout, false);
        TextView titleTextView = noteView.findViewById(R.id.note_title);
        TextView contentTextView = noteView.findViewById(R.id.note_content);
        TextView dateTextView = noteView.findViewById(R.id.note_date);

        titleTextView.setText(title);

        if (content.isEmpty()) {
            contentTextView.setVisibility(View.GONE);
        } else {
            int numLines = content.split("\n").length;
            if (content.length() > 33) {
                content = content.substring(0, 33) + "...";
            } else if (numLines > 2) {
                content = content.split("\n")[0] + "\n" + content.split("\n")[1] + "...";
            }
            contentTextView.setText(content);
        }

        if (date.isEmpty()) {
            dateTextView.setVisibility(View.GONE);
        } else {
            dateTextView.setText(date);
            Pattern pattern = Pattern.compile("(\\d+)");
            Matcher matcher = pattern.matcher(date);
            int day = matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
            int month = matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;

            LocalDate noteDate = LocalDate.of(LocalDate.now().getYear(), month, day);
            dateTextView.setTextColor(getResources().getColor(noteDate.isBefore(LocalDate.now()) ?
                    R.color.red : R.color.statistics_blue));
        }

        noteView.setOnClickListener(v -> {
            Intent intent = new Intent(this, NoteActivity.class);
            intent.putExtra("noteId", String.valueOf(noteId));
            startActivity(intent);
        });

        targetLayout.addView(noteView);
    }
}
