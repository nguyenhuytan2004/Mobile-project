package com.example.project;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton fabAdd;
    private LinearLayout taskListLayout;
    ImageView focusTab, calendarTab;
    private LoginSessionManager loginSessionManager;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginSessionManager = LoginSessionManager.getInstance(this);
        if (loginSessionManager.isLoggedIn()) {
            Log.d("MainActivity", "User is logged in");
        }
        else {
            Log.d("MainActivity", "User is not logged in");
            loginSessionManager.createSession(1);
            Log.d("MainActivity", "User session created with ID: " + loginSessionManager.getUserId());
        }

        // Ánh xạ các thành phần trong giao diện
        fabAdd = findViewById(R.id.fab_add);
        taskListLayout = findViewById(R.id.task_list_layout);
        focusTab = findViewById(R.id.focusTab);
        calendarTab = findViewById(R.id.calendarTab);

        // Sự kiện khi nhấn Floating Action Button (FAB)
        fabAdd.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("noteId", "-1");

            Intent intent = new Intent(MainActivity.this, NoteActivity.class);
            intent.putExtras(bundle);

            startActivity(intent);
        });

        focusTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FocusTab.class);
                startActivity(intent);
            }
        });

        calendarTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CalendarTab.class);
                startActivity(intent);
            }
        });

        loadNotes();
    }

    private void loadNotes() {
        // Clear previous notes
        taskListLayout.removeAllViews();

        try {
            // Open database
            db = DatabaseHelper.getInstance(this).openDatabase();

            // Query for notes belonging to current user
            int userId = loginSessionManager.getUserId();
            String query = "SELECT n.id, n.title, n.content, r.date " +
                    "FROM tbl_note n " +
                    "LEFT JOIN tbl_note_reminder r ON n.id = r.note_id " +
                    "WHERE n.user_id = ? ";

            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    int noteId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));

                    String content = "";
                    int contentColumnIndex = cursor.getColumnIndexOrThrow("content");
                    if (!cursor.isNull(contentColumnIndex)) {
                        content = cursor.getString(contentColumnIndex);
                    }

                    String date = "";
                    int dateColumnIndex = cursor.getColumnIndexOrThrow("date");
                    if (!cursor.isNull(dateColumnIndex)) {
                        date = cursor.getString(dateColumnIndex);
                    }

                    addNoteView(noteId, title, content, date);
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Error loading notes", e);
        } finally {
            if (db != null) {
                DatabaseHelper.getInstance(this).closeDatabase();
            }
        }
    }

    private void addNoteView(int noteId, String title, String content, String date) {
        View noteView = LayoutInflater.from(this).inflate(R.layout.note_item, taskListLayout, false);

        TextView titleTextView = noteView.findViewById(R.id.note_title);
        TextView contentTextView = noteView.findViewById(R.id.note_content);
        TextView dateTextView = noteView.findViewById(R.id.note_date);

        titleTextView.setText(title);
        contentTextView.setText(content);
        dateTextView.setText(date);

        // Make the note clickable to open for editing
        noteView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("noteId", String.valueOf(noteId));
            Intent intent = new Intent(MainActivity.this, NoteActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        });

        taskListLayout.addView(noteView);
    }
}
