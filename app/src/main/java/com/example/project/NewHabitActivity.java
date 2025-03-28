package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class NewHabitActivity extends AppCompatActivity {

    private EditText etHabitName, etQuote;
    private Button btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_habit);

        // Initialize UI components
        etHabitName = findViewById(R.id.et_habit_name);
        etQuote = findViewById(R.id.et_quote);
        btnNext = findViewById(R.id.btn_next);

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a new Habit object and set basic data
                Habit newHabit = new Habit();
                newHabit.setName(etHabitName.getText().toString().trim());
                newHabit.setQuote(etQuote.getText().toString().trim());

                // Validate inputs
                if (newHabit.getName().isEmpty()) {
                    etHabitName.setError("Please enter a habit name");
                    return;
                }

                // Pass data to the next activity
                Intent intent = new Intent(NewHabitActivity.this, NewHabitAimActivity.class);
                intent.putExtra("habit", newHabit);
                startActivity(intent);
            }
        });
    }
}