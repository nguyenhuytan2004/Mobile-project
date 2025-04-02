package com.example.project;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
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
    ImageView homeTab, focusTab, matrixTab;
    CalendarView calendarView;
    LinearLayout notesContainer;
    private Map<String, List<NoteInfo>> dateToNotesMap = new HashMap<>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_calendar);

        homeTab = findViewById(R.id.homeTab);
        focusTab = findViewById(R.id.focusTab);
        matrixTab = findViewById(R.id.matrixTab);
        calendarView = findViewById(R.id.calendarView);
        notesContainer = findViewById(R.id.notesContainer);

        // Tải tất cả các ghi chú từ cơ sở dữ liệu
        loadNotesFromDatabase();

        // Thiết lập lịch để đánh dấu ngày có ghi chú
        setupCalendarEvents();

        // Xử lý sự kiện khi người dùng chọn một ngày
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%d",
                    dayOfMonth, (month + 1), year);
            displayNotesForDate(selectedDate);
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Tải lại dữ liệu khi quay lại màn hình
        dateToNotesMap.clear();
        loadNotesFromDatabase();
        displayCurrentDateNotes();
    }

    private void loadNotesFromDatabase() {
        SQLiteDatabase db = DatabaseHelper.getInstance(this).openDatabase();
        try {
            // Truy vấn để lấy tất cả các ghi chú có nhắc nhở
            Cursor cursor = db.rawQuery(
                    "SELECT n.id, n.title, n.content, r.date, r.time " +
                            "FROM tbl_note n " +
                            "JOIN tbl_note_reminder r ON n.id = r.note_id " +
                            "WHERE r.date IS NOT NULL AND r.date != ''",
                    null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String noteId = cursor.getString(cursor.getColumnIndexOrThrow("id"));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                    String time = "";
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
                        dateToNotesMap.get(formattedDate).add(new NoteInfo(noteId, title, time));
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

    private String formatDateString(String dateStr) {
        Pattern pattern = Pattern.compile("(\\d+)");
        Matcher matcher = pattern.matcher(dateStr);

        int day = 0, month = 0;
        if (matcher.find()) {
            day = Integer.parseInt(matcher.group(1));
        }
        if (matcher.find()) {
            month = Integer.parseInt(matcher.group(1));
        }
        int year = Calendar.getInstance().get(Calendar.YEAR);

        String formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%d", day, month, year);
        try {
            Date date = dateFormat.parse(formattedDate);
            if (date != null) {
                return dateFormat.format(date);
            }
        } catch (ParseException e) {
            Log.e("CalendarTab", "Error parsing date", e);
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
            title.setText("Ghi chú");
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
                    startActivity(intent);
                });

                notesContainer.addView(noteView);
            }
        } else {
            // Hiển thị thông báo không có ghi chú
            TextView emptyText = new TextView(this);
            emptyText.setText("Không có ghi chú cho ngày này");
            emptyText.setTextColor(Color.parseColor("#d5d5d5"));
            emptyText.setTextSize(16);
            emptyText.setPadding(20, 20, 20, 20);
            notesContainer.addView(emptyText);
        }
    }

    // Lớp để lưu trữ thông tin ghi chú
    private static class NoteInfo {
        String id;
        String title;
        String time;

        NoteInfo(String id, String title, String time) {
            this.id = id;
            this.title = title;
            this.time = time;
        }
    }
}