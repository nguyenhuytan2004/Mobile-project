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
import android.widget.ImageView;
import android.widget.TextView;


import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.w3c.dom.Text;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Locale;

public class CalendarDialog extends BottomSheetDialogFragment {

    TextView timeValue, reminderValue, removeReminder;
    private OnDateSelectedListener listener;

    public interface OnDateSelectedListener {
        void onDateSelected(String date);
    }

    public void setOnDateSelectedListener(OnDateSelectedListener listener) {
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
            String selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year);
            if (listener != null) {
                listener.onDateSelected(selectedDate);
            }

            removeReminder.setVisibility(View.VISIBLE);
        });

        TextView removeReminder = view.findViewById(R.id.removeReminder);
        removeReminder.setOnClickListener(v -> {
            listener.onDateSelected("");

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
}