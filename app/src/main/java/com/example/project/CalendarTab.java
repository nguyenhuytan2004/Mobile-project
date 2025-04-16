package com.example.project;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CalendarView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CalendarTab extends AppCompatActivity {
    ImageView homeTab, focusTab, matrixTab, habitTab;
    CalendarView calendarView;
    LinearLayout notesContainer, taskContainer;
    private Map<String, List<NoteInfo>> dateToNotesMap = new HashMap<>();
    private Map<String, List<Task>> dateToTasksMap = new HashMap<>();
    LocalDateTime now = LocalDateTime.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    String formattedCurrentDate = now.format(formatter);

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_calendar);

        homeTab = findViewById(R.id.homeTab);
        focusTab = findViewById(R.id.focusTab);
        matrixTab = findViewById(R.id.matrixTab);
        calendarView = findViewById(R.id.calendarView);
        habitTab = findViewById(R.id.habitTabCalender);
        notesContainer = findViewById(R.id.notesContainer);
        taskContainer = findViewById(R.id.taskContainer);

        // Tải tất cả các ghi chú từ cơ sở dữ liệu
        loadNotesFromDatabase();
        loadTasksFromDatabase();
        displayTaskForDate(formattedCurrentDate);

        // Thiết lập lịch để đánh dấu ngày có ghi chú
        setupCalendarEvents();

        // Xử lý sự kiện khi người dùng chọn một ngày
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%d",
                    dayOfMonth, (month + 1), year);
            displayNotesForDate(selectedDate);
            displayTaskForDate(selectedDate);
        });

        homeTab.setOnClickListener(view -> {
            startActivity(new Intent(CalendarTab.this, MainActivity.class));
        });

        matrixTab.setOnClickListener(view -> {
            startActivity(new Intent(CalendarTab.this, Matrix_Eisenhower.class));
        });

        focusTab.setOnClickListener(view -> {
            startActivity(new Intent(CalendarTab.this, FocusTab.class));
        });

        habitTab.setOnClickListener(view -> {
            startActivity(new Intent(CalendarTab.this, HabitActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Tải lại dữ liệu khi quay lại màn hình
        dateToNotesMap.clear();
        dateToTasksMap.clear();
        loadNotesFromDatabase();
        loadTasksFromDatabase();
        String currentDate = dateFormat.format(new Date());
        displayNotesForDate(currentDate);
        displayTaskForDate(currentDate);
    }

    private void loadNotesFromDatabase() {
        SQLiteDatabase db = DatabaseHelper.getInstance(this).openDatabase();
        try {
            // Truy vấn để lấy tất cả các ghi chú có nhắc nhở
            Cursor cursor = db.rawQuery(
                    "SELECT n.id, n.title, n.content, r.date, r.time, n.category_id, c.list_id " +
                            "FROM tbl_note n " +
                            "JOIN tbl_note_reminder r ON n.id = r.note_id " +
                            "JOIN tbl_category c ON n.category_id = c.id " +
                            "JOIN tbl_list l ON c.list_id = l.id " +
                            "WHERE r.date IS NOT NULL AND r.date != ''" +
                            "ORDER BY n.id",
                    null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String noteId = cursor.getString(cursor.getColumnIndexOrThrow("id"));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                    String time = "";
                    String categoryId = cursor.getString(cursor.getColumnIndexOrThrow("category_id"));
                    String listId = cursor.getString(cursor.getColumnIndexOrThrow("list_id"));
                    int timeCursor = cursor.getColumnIndexOrThrow("time");
                    if (timeCursor != -1) {
                        time = cursor.getString(timeCursor);
                    }

                    String dateStr = cursor.getString(cursor.getColumnIndexOrThrow("date"));

                    // Chuyển đổi định dạng ngày về dạng dd/MM/yyyy
                    String formattedDate = formatDateString(dateStr);

                    if (formattedDate != null) {
                        // Lưu trữ thông tin ghi chú theo ngày
                        if (!dateToNotesMap.containsKey(formattedDate)) {
                            dateToNotesMap.put(formattedDate, new ArrayList<>());
                        }
                        dateToNotesMap.get(formattedDate).add(new NoteInfo(noteId, title, time, categoryId, listId));
                    }
                } while (cursor.moveToNext());
                cursor.close();
            }
        } catch (Exception e) {
            Log.e("CalendarTab", "Error loading notes", e);
        } finally {
            DatabaseHelper.getInstance(this).closeDatabase();
        }
    }

    private void loadTasksFromDatabase() {
        SQLiteDatabase db = DatabaseHelper.getInstance(this).openDatabase();
        try {
            // Query all tasks with reminders
            Cursor cursor = db.rawQuery(
                    "SELECT t.id, t.title, t.content, t.priority, t.is_completed, " +
                    "t.category_id, r.date, r.time " +
                    "FROM tbl_task t " +
                    "JOIN tbl_task_reminder r ON t.id = r.task_id " +
                    "WHERE r.date IS NOT NULL AND r.date != '' " +
                    "ORDER BY t.id",
                    null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                    String content = cursor.isNull(cursor.getColumnIndexOrThrow("content")) ? 
                          "" : cursor.getString(cursor.getColumnIndexOrThrow("content"));
                    int priority = cursor.getInt(cursor.getColumnIndexOrThrow("priority"));
                    boolean isCompleted = cursor.getInt(cursor.getColumnIndexOrThrow("is_completed")) > 0;
                    int categoryId = cursor.getInt(cursor.getColumnIndexOrThrow("category_id"));
                    
                    String dateStr = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                    String timeStr = cursor.isNull(cursor.getColumnIndexOrThrow("time")) ? 
                          "" : cursor.getString(cursor.getColumnIndexOrThrow("time"));

                    // Format date string to standard format (dd/MM/yyyy)
                    String formattedDate = formatDateString(dateStr);
                    
                    if (formattedDate != null) {
                        // Create task object
                        Task task = new Task(id, title, categoryId);
                        task.setDescription(content);
                        task.setPriority(priority);
                        task.setCompleted(isCompleted);
                        task.setReminderDate(formattedDate);
                        
                        // Add to map by date
                        if (!dateToTasksMap.containsKey(formattedDate)) {
                            dateToTasksMap.put(formattedDate, new ArrayList<>());
                        }
                        dateToTasksMap.get(formattedDate).add(task);
                        
                        Log.d("CalendarTab", "Loaded task: " + title + " for date: " + formattedDate);
                    }
                } while (cursor.moveToNext());
                cursor.close();
            }
        } catch (Exception e) {
            Log.e("CalendarTab", "Error loading tasks: " + e.getMessage(), e);
        } finally {
            DatabaseHelper.getInstance(this).closeDatabase();
        }
    }

    private List<Task> loadTaskByDate(String date) {
        // First check our cached map - this will be faster if we already loaded the tasks
        if (dateToTasksMap.containsKey(date)) {
            return dateToTasksMap.get(date);
        }
        
        // If not in cache, query database directly
        List<Task> tasks = new ArrayList<>();
        SQLiteDatabase db = null;
        
        try {
            db = DatabaseHelper.getInstance(this).openDatabase();
            
            // Format the date for SQL query - look for exact date or pattern containing the date
            String formattedDate = date; // Already in dd/MM/yyyy format
            
            String query = "SELECT t.id, t.title, t.content, t.priority, " +
                    "t.is_completed, t.category_id, r.date, r.time " +
                    "FROM tbl_task t " +
                    "JOIN tbl_task_reminder r ON t.id = r.task_id " +
                    "WHERE r.date LIKE ? OR r.date LIKE ?";
            
            Cursor cursor = db.rawQuery(query, 
                    new String[]{ formattedDate + "%", "%" + formattedDate + "%" });
            
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                    String content = cursor.isNull(cursor.getColumnIndexOrThrow("content")) ? 
                          "" : cursor.getString(cursor.getColumnIndexOrThrow("content"));
                    int priority = cursor.getInt(cursor.getColumnIndexOrThrow("priority"));
                    boolean isCompleted = cursor.getInt(cursor.getColumnIndexOrThrow("is_completed")) > 0;
                    int categoryId = cursor.getInt(cursor.getColumnIndexOrThrow("category_id"));
                    
                    // Create task object
                    Task task = new Task(id, title, categoryId);
                    task.setDescription(content);
                    task.setPriority(priority);
                    task.setCompleted(isCompleted);
                    task.setReminderDate(date);
                    
                    tasks.add(task);
                    Log.d("CalendarTab", "Found task for date " + date + ": " + title);
                }
            } else {
                Log.d("CalendarTab", "No tasks found for date: " + date);
            }
            
            if (cursor != null) cursor.close();
            
            // Cache the results for future use
            if (!tasks.isEmpty()) {
                dateToTasksMap.put(date, tasks);
            }
            
        } catch (Exception e) {
            Log.e("CalendarTab", "Error loading tasks by date: " + e.getMessage(), e);
        } finally {
            if (db != null) DatabaseHelper.getInstance(this).closeDatabase();
        }
        
        return tasks;
    }

    private String formatDateString(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        
        try {
            // First, check if the date is already in dd/MM/yyyy format
            if (dateStr.matches("\\d{2}/\\d{2}/\\d{4}")) {
                return dateStr; // Already in the correct format
            }
            
            // Handle "Ngày X, tháng Y" format
            Pattern pattern = Pattern.compile("Ngày (\\d+),? tháng (\\d+)");
            Matcher matcher = pattern.matcher(dateStr);
            
            if (matcher.find()) {
                int day = Integer.parseInt(matcher.group(1));
                int month = Integer.parseInt(matcher.group(2));
                int year = Calendar.getInstance().get(Calendar.YEAR);
                
                // Format the date in dd/MM/yyyy
                return String.format(Locale.getDefault(), "%02d/%02d/%d", day, month, year);
            }
            
            // Handle numeric extraction as fallback
            pattern = Pattern.compile("(\\d+)");
            matcher = pattern.matcher(dateStr);
            
            int day = 0, month = 0;
            if (matcher.find()) {
                day = Integer.parseInt(matcher.group(1));
            }
            if (matcher.find()) {
                month = Integer.parseInt(matcher.group(1));
            }
            
            if (day > 0 && month > 0) {
                int year = Calendar.getInstance().get(Calendar.YEAR);
                String formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%d", day, month, year);
                
                // Validate the parsed date
                Date date = dateFormat.parse(formattedDate);
                if (date != null) {
                    return dateFormat.format(date);
                }
            }
            
            // Log the unhandled date format
            Log.w("CalendarTab", "Unrecognized date format: " + dateStr);
            
        } catch (ParseException e) {
            Log.e("CalendarTab", "Error parsing date: " + dateStr, e);
        } catch (Exception e) {
            Log.e("CalendarTab", "Error processing date: " + dateStr, e);
        }
        
        return null;
    }

    private void setupCalendarEvents() {
        // Đây sẽ là phần để thiết lập các dấu chấm cho các ngày có ghi chú
        // Vì CalendarView mặc định không hỗ trợ tùy chỉnh ngày như vậy,
        // bạn có thể cần sử dụng một thư viện bên thứ ba như MaterialCalendarView

        // Thay thế cho demo, chúng ta sẽ hiển thị ngày hiện tại
        displayCurrentDateNotes();
    }

    private void displayCurrentDateNotes() {
        String currentDate = dateFormat.format(new Date());
        displayNotesForDate(currentDate);
    }

    private void displayNotesForDate(String date) {
        // Xóa các ghi chú hiện tại
        notesContainer.removeAllViews();

        // Hiển thị các ghi chú cho ngày đã chọn
        List<NoteInfo> notes = dateToNotesMap.get(date);
        if (notes != null && !notes.isEmpty()) {
            TextView title = new TextView(this);
            title.setText(getResources().getString(R.string.calendar_note));
            title.setTextColor(Color.parseColor("#d5d5d5"));
            title.setTextSize(20);
            title.setPadding(20, 20, 0, 50);
            notesContainer.addView(title);
            for (NoteInfo note : notes) {
                View noteView = getLayoutInflater().inflate(R.layout.note_item_in_calendar_tab, null);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 0, 0, 40);
                noteView.setLayoutParams(params);

                TextView titleTextView = noteView.findViewById(R.id.note_title);
                titleTextView.setText(note.title);

                TextView timeTextView = noteView.findViewById(R.id.note_time);
                if (note.time != null && !note.time.isEmpty()) {
                    timeTextView.setText(note.time);
                    if (date != null) {
                        Pattern pattern = Pattern.compile("(\\d+)");
                        Matcher matcher = pattern.matcher(date);

                        int day = 0, month = 0;
                        if (matcher.find()) {
                            day = Integer.parseInt(matcher.group(1));
                        }
                        if (matcher.find()) {
                            month = Integer.parseInt(matcher.group(1));
                        }

                        LocalDate noteDate = LocalDate.of(LocalDate.now().getYear(), month, day);

                        if (noteDate.isBefore(LocalDate.now())) {
                            timeTextView.setTextColor(getResources().getColor(R.color.red));
                        } else {
                            timeTextView.setTextColor(getResources().getColor(R.color.statistics_blue));
                        }
                    }
                } else {
                    timeTextView.setVisibility(View.GONE);
                }

                noteView.setOnClickListener(v -> {
                    // Mở ghi chú khi người dùng nhấp vào
                    Intent intent = new Intent(CalendarTab.this, NoteActivity.class);
                    intent.putExtra("noteId", note.id);
                    intent.putExtra("categoryId", note.categoryId);
                    intent.putExtra("listId", note.listId);
                    startActivity(intent);
                });

                notesContainer.addView(noteView);
            }
        } else {
            // Hiển thị thông báo không có ghi chú
            TextView emptyText = new TextView(this);
            emptyText.setText(getResources().getString(R.string.calendar_note_none));
            emptyText.setTextColor(Color.parseColor("#d5d5d5"));
            emptyText.setTextSize(16);
            emptyText.setPadding(20, 20, 20, 20);
            notesContainer.addView(emptyText);
        }
    }

    private void displayTaskForDate(String date) {
        taskContainer.removeAllViews();

        List<Task> tasks = loadTaskByDate(date);
        if (tasks != null && !tasks.isEmpty()) {
            TextView title = new TextView(this);
            title.setText(getResources().getString(R.string.calendar_task));
            title.setTextColor(Color.parseColor("#d5d5d5"));
            title.setTextSize(20);
            title.setPadding(20, 20, 0, 30);
            taskContainer.addView(title);

            for (Task task : tasks) {
                LinearLayout taskRow = new LinearLayout(this);
                taskRow.setOrientation(LinearLayout.HORIZONTAL);
                taskRow.setPadding(20, 10, 20, 10);
                taskRow.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));

                // Tạo CheckBox
                android.widget.CheckBox checkBox = new android.widget.CheckBox(this);

                // Set trạng thái ban đầu (đã hoàn thành hay chưa)
                checkBox.setChecked(task.isCompleted());

                // Tùy chỉnh màu tic
                checkBox.setButtonTintList(getColorStateList(R.color.statistics_blue));

                // Lắng nghe sự kiện tick hoặc bỏ tick
                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        // Update task completion status
                        task.setCompleted(isChecked);
                        updateTaskCompletionStatus(task, isChecked);
                    }
                });

                // TextView
                TextView taskTitle = new TextView(this);
                taskTitle.setText(task.getTitle());
                taskTitle.setTextSize(18);
                taskTitle.setTextColor(Color.parseColor("#d5d5d5"));
                taskTitle.setPadding(16, 0, 0, 0);

                taskRow.addView(checkBox);
                taskRow.addView(taskTitle);
                taskContainer.addView(taskRow);
            }
        } else {
            TextView emptyText = new TextView(this);
            emptyText.setText(getResources().getString(R.string.calendar_task_none));
            emptyText.setTextColor(Color.parseColor("#d5d5d5"));
            emptyText.setTextSize(16);
            emptyText.setPadding(20, 20, 20, 20);
            taskContainer.addView(emptyText);
        }
    }

    private void updateTaskCompletionStatus(Task task, boolean isCompleted) {
        DatabaseHelper.getInstance(this).markTaskAsCompleted(task.getId(), isCompleted);
        SQLiteDatabase db = DatabaseHelper.getInstance(this).openDatabase();
        if (db == null) {
            Log.e("CalendarTab", "Database không tồn tại hoặc không thể mở");
            return;
        }

        try {
            // Update the Task object's completion status
            task.setCompleted(isCompleted);

            // Create ContentValues to store task data
            ContentValues values = new ContentValues();
            values.put("is_completed", isCompleted ? 1 : 0);

            // First try to update using task ID (most reliable)
            String whereClause = "id = ?";
            String[] whereArgs = {String.valueOf(task.getId())};

            long result = db.update("tbl_task", values, whereClause, whereArgs);

            if (result == -1) {
                Log.e("CalendarTab", "Lỗi khi cập nhật trạng thái task vào database");
            } else if (result == 0) {
                // If no rows updated, try with title and content as fallback
                Log.w("CalendarTab", "Không tìm thấy task với ID: " + task.getId());
                whereClause = "title = ? AND content = ?";
                whereArgs = new String[]{task.getTitle(), task.getDescription()};
                
                result = db.update("tbl_task", values, whereClause, whereArgs);
                
                if (result > 0) {
                    Log.d("CalendarTab", "Task đã được cập nhật bằng title và content");
                } else {
                    Log.e("CalendarTab", "Không thể cập nhật task");
                }
            } else {
                Log.d("CalendarTab", "Task đã được cập nhật trạng thái thành công: " +
                        (isCompleted ? "đã hoàn thành" : "chưa hoàn thành"));
            }
        } catch (Exception e) {
            Log.e("CalendarTab", "Lỗi: " + e.getMessage(), e);
        } finally {
            DatabaseHelper.getInstance(this).closeDatabase();
        }
    }
    // Lớp để lưu trữ thông tin ghi chú
    private static class NoteInfo {
        String id, categoryId, listId;
        String title;
        String time;

        NoteInfo(String id, String title, String time, String categoryId, String listId) {
            this.id = id;
            this.title = title;
            this.time = time;
            this.categoryId = categoryId;
            this.listId = listId;
        }
    }
}