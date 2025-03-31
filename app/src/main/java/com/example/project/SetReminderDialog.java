package com.example.project;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
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

public class SetReminderDialog extends BottomSheetDialogFragment {

    TextView timeValue, reminderValue, removeReminder;
    SwitchCompat switchRepeatReminder;
    private OnReminderSettingsListener listener;

    String selectedDate = "";

    public interface OnReminderSettingsListener {
        void onReminderSet(String date, String time, int daysBefore, boolean isRepeat);
    }

    public void setOnDateSelectedListener(OnReminderSettingsListener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.set_reminder, container, false);

        CalendarView calendarView = view.findViewById(R.id.calendarView);
        calendarView.setFirstDayOfWeek(Calendar.MONDAY);

        removeReminder = view.findViewById(R.id.removeReminder);

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
            String[] vietnameseWeekDays = new String[]{"", "Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "CN"};
            Field field = calendarView.getClass().getDeclaredField("mDayNamesShort");
            field.setAccessible(true);
            field.set(calendarView, vietnameseWeekDays);
        } catch (Exception e) {
            e.printStackTrace();
        }

        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year);

            removeReminder.setVisibility(View.VISIBLE);
        });

        timeValue = view.findViewById(R.id.timeValue);
        timeValue.setOnClickListener(v -> {
            final int[] time = {12, 0};
            if (!timeValue.getText().toString().equals("Không có >")) {
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

        reminderValue = view.findViewById(R.id.reminderValue);
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

            AlertDialog dialog = builder.create();

            TextView[] textViews = {text1, text2, text3, text4, text5, text6, text7};
            if (!reminderValue.getText().toString().equals("Không có >")) {
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

            switchRepeatReminder.setOnClickListener(v1 -> {
                boolean isChecked = switchRepeatReminder.isChecked();
                Log.d("SetReminderDialog", isChecked ? "Repeat reminder enabled" : "Repeat reminder disabled");
            });

            dialog.show();
        });

        TextView removeReminder = view.findViewById(R.id.removeReminder);
        removeReminder.setOnClickListener(v -> {
            listener.onReminderSet("", "", 0, false);

            timeValue.setText("Không có >");
            timeValue.setTextColor(getResources().getColor(R.color.gray));
            timeValue.setTextSize(16);

            reminderValue.setText("Không có >");
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
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (listener != null) {
            String date = selectedDate;
            String time = timeValue.getText().toString();
            int daysBefore = Integer.parseInt(reminderValue.getText().toString().substring(0, 1));
            boolean isRepeat = switchRepeatReminder.isChecked();

            listener.onReminderSet(date, time, daysBefore, isRepeat);
        }
    }
}

