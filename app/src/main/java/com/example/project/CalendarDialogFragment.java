package com.example.project;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Locale;

public class CalendarDialogFragment extends DialogFragment {
    public interface OnDateSelectedListener {
        void onDateSelected(String date);
    }

    private OnDateSelectedListener listener;

    public void setOnDateSelectedListener(OnDateSelectedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_calendar, null);

        CalendarView calendarView = view.findViewById(R.id.calendarView);
        calendarView.setFirstDayOfWeek(Calendar.MONDAY);

        Button btnClose = view.findViewById(R.id.btnClose);

        // Xử lý sự kiện chọn ngày
        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            String selectedDate = String.format(Locale.getDefault(), "Ngày %d, tháng %d", dayOfMonth, month + 1);
            if (listener != null) {
                listener.onDateSelected(selectedDate);
            }
            dismiss();
        });

        // Đóng dialog khi nhấn nút
        btnClose.setOnClickListener(v -> dismiss());

        builder.setView(view);
        return builder.create();
    }
}

