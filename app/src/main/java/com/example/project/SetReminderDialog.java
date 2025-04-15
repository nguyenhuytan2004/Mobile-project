package com.example.project;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;


import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SetReminderDialog extends BottomSheetDialogFragment {

    CalendarView calendarView;
    TextView timeValue, reminderValue, removeReminder;
    SwitchCompat switchRepeatReminder;
    private OnReminderSettingsListener listener;
    private String noteId;
    private String selectedDate = "";
    private String mTimeValue = null;
    private int mDaysBefore = 0;
    private boolean mIsRepeat = false;

    public SetReminderDialog(String noteId) {
        this.noteId = noteId;
    }

    public interface OnReminderSettingsListener {
        void onReminderSet(String date, String time, int daysBefore, boolean isRepeat);
    }

    public void setOnDateSelectedListener(OnReminderSettingsListener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.set_reminder, container, false);

        calendarView = view.findViewById(R.id.calendarView);
        timeValue = view.findViewById(R.id.timeValue);
        reminderValue = view.findViewById(R.id.reminderValue);
        removeReminder = view.findViewById(R.id.removeReminder);

        loadNoteReminder(noteId);

        // Custom weekday names
        try {
            // Get the instance of internal Calendar from CalendarView
            Field dateTextAppearanceField = CalendarView.class.getDeclaredField("mDateTextAppearanceResId");
            dateTextAppearanceField.setAccessible(true);
            dateTextAppearanceField.set(calendarView, R.style.CustomCalendarDate);

            // Get weekday name style field
            Field weekDayTextAppearanceField = CalendarView.class.getDeclaredField("mWeekDayTextAppearanceResId");
            weekDayTextAppearanceField.setAccessible(true);
            weekDayTextAppearanceField.set(calendarView, R.style.CustomCalendarWeekDay);

            // Set custom weekday names
            Field field = calendarView.getClass().getDeclaredField("mDayNamesShort");
            field.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!selectedDate.isEmpty()) {
            // Parse the date and set calendar
            try {
                Pattern pattern = Pattern.compile("(\\d+)");
                Matcher matcher = pattern.matcher(selectedDate);
                int day = 0, month = 0, year = Calendar.getInstance().get(Calendar.YEAR);
                if (matcher.find()) {
                    day = Integer.parseInt(matcher.group(1));
                }
                if (matcher.find()) {
                    month = Integer.parseInt(matcher.group(1)) - 1;
                }
                selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%d", day, month + 1, year);

                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, day);
                calendarView.setDate(calendar.getTimeInMillis());

                removeReminder.setVisibility(View.VISIBLE);

            } catch (Exception e) {
                Log.e("SetReminderDialog", "Error parsing date: " + selectedDate, e);
            }
        } else {
            selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%d",
                    Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
                    Calendar.getInstance().get(Calendar.MONTH) + 1,
                    Calendar.getInstance().get(Calendar.YEAR));
        }

        // Apply time value if available
        if (mTimeValue != null) {
            timeValue.setText(mTimeValue);
            timeValue.setTextColor(getResources().getColor(R.color.statistics_blue));
            timeValue.setTextSize(18);
        }

        // Apply days before value if available
        if (mDaysBefore > 0 && mDaysBefore <= 7) {
            String[] reminderOptions = {getResources().getString(R.string.time_reminder_text1),
                    getResources().getString(R.string.time_reminder_text2),
                    getResources().getString(R.string.time_reminder_text3),
                    getResources().getString(R.string.time_reminder_text4),
                    getResources().getString(R.string.time_reminder_text5),
                    getResources().getString(R.string.time_reminder_text6),
                    getResources().getString(R.string.time_reminder_text7)};
            reminderValue.setText(reminderOptions[mDaysBefore - 1]);
            reminderValue.setTextColor(getResources().getColor(R.color.statistics_blue));
            reminderValue.setTextSize(18);
        }

        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year);

            removeReminder.setVisibility(View.VISIBLE);
        });

        timeValue.setOnClickListener(v -> {
            final int[] time = {12, 0};
            if (!timeValue.getText().toString().equals(getResources().getString(R.string.note_none))) {
                time[0] = Integer.parseInt(timeValue.getText().toString().substring(0, 2));
                time[1] = Integer.parseInt(timeValue.getText().toString().substring(3, 5));
            }

            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    new ContextThemeWrapper(getContext(), R.style.CustomTimePickerDialog),
                    (view12, hourOfDay, minute) -> {
                        String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                        timeValue.setText(selectedTime);
                        timeValue.setTextColor(getResources().getColor(R.color.statistics_blue));
                        timeValue.setTextSize(18);

                        removeReminder.setVisibility(View.VISIBLE);
                    },
                    time[0], time[1], true
            );
            timePickerDialog.show();
        });

        reminderValue.setOnClickListener(v -> {
            View reminderView = LayoutInflater.from(getContext()).inflate(R.layout.reminder_dialog, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setView(reminderView);

            TextView text1 = reminderView.findViewById(R.id.text1);
            TextView text2 = reminderView.findViewById(R.id.text2);
            TextView text3 = reminderView.findViewById(R.id.text3);
            TextView text4 = reminderView.findViewById(R.id.text4);
            TextView text5 = reminderView.findViewById(R.id.text5);
            TextView text6 = reminderView.findViewById(R.id.text6);
            TextView text7 = reminderView.findViewById(R.id.text7);

            switchRepeatReminder = reminderView.findViewById(R.id.switchRepeatReminder);
            switchRepeatReminder.setChecked(mIsRepeat);

            AlertDialog dialog = builder.create();

            TextView[] textViews = {text1, text2, text3, text4, text5, text6, text7};
            if (!reminderValue.getText().toString().equals(getResources().getString(R.string.note_none))) {
                textViews[Integer.parseInt(reminderValue.getText().toString().substring(0, 1)) - 1].setTextColor(getResources().getColor(R.color.statistics_blue));
            }

            for (int i = 0; i < textViews.length; i++) {
                int finalI = i;
                textViews[i].setOnClickListener(v1 -> {
                    String selectedReminder = textViews[finalI].getText().toString();
                    reminderValue.setText(selectedReminder);
                    reminderValue.setTextColor(getResources().getColor(R.color.statistics_blue));
                    reminderValue.setTextSize(18);
                    removeReminder.setVisibility(View.VISIBLE);

                    dialog.dismiss();
                });
            }

            dialog.show();
        });

        TextView removeReminder = view.findViewById(R.id.removeReminder);
        removeReminder.setOnClickListener(v -> {
            listener.onReminderSet("", "", 0, false);

            selectedDate = "";

            timeValue.setText(getResources().getString(R.string.note_none));
            timeValue.setTextColor(getResources().getColor(R.color.gray));
            timeValue.setTextSize(16);

            reminderValue.setText(getResources().getString(R.string.note_none));
            reminderValue.setTextColor(getResources().getColor(R.color.gray));
            reminderValue.setTextSize(16);

            removeReminder.setVisibility(View.INVISIBLE);
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        }
    }

    private void loadNoteReminder(String noteId) {
        SQLiteDatabase db = DatabaseHelper.getInstance(getContext()).openDatabase();
        try {
            Cursor cursor = db.rawQuery(
                    "SELECT date, time, days_before, is_repeat FROM tbl_note_reminder WHERE note_id = ?",
                    new String[]{noteId}
            );

            if (cursor != null && cursor.moveToFirst()) {
                String dateStr = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                if (dateStr != null && !dateStr.isEmpty()) {
                    selectedDate = dateStr;
                }

                // Store time value to be set after views are initialized
                String timeStr = cursor.getString(cursor.getColumnIndexOrThrow("time"));
                if (timeStr != null && !timeStr.isEmpty()) {
                    mTimeValue = timeStr;
                }

                // Store days before value
                int daysBefore = cursor.getInt(cursor.getColumnIndexOrThrow("days_before"));
                if (daysBefore > 0 && daysBefore <= 7) {
                    mDaysBefore = daysBefore;
                }

                // Store repeat status
                mIsRepeat = cursor.getInt(cursor.getColumnIndexOrThrow("is_repeat")) == 1;

                cursor.close();
            }
        } catch (Exception e) {
            Log.e("SetReminderDialog", "Error loading note reminder", e);
        } finally {
            DatabaseHelper.getInstance(getContext()).closeDatabase();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (listener != null) {
            String date = selectedDate;
            String time = !timeValue.getText().toString().equals(getResources().getString(R.string.note_none)) ? timeValue.getText().toString() : "";
            int daysBefore = 0;
            if (!reminderValue.getText().toString().substring(0, 1).equals(String.valueOf(getResources().getString(R.string.note_none).charAt(0))))
            {
                daysBefore = Integer.parseInt(reminderValue.getText().toString().substring(0, 1));
            }
            boolean isRepeat = switchRepeatReminder != null && switchRepeatReminder.isChecked();

            listener.onReminderSet(date, time, daysBefore, isRepeat);
        }
    }
}

