package com.example.project;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class NoteActivity extends AppCompatActivity {
    ImageView icon4;
    TextView txtDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note);

        txtDate = findViewById(R.id.txtDate);
        icon4 = findViewById(R.id.icon4);

        icon4.setOnClickListener(v -> {
            CalendarDialogFragment dialog = new CalendarDialogFragment();
            dialog.setOnDateSelectedListener(date -> txtDate.setText(date));
            dialog.show(getSupportFragmentManager(), "CalendarDialog");
        });
    }
}
