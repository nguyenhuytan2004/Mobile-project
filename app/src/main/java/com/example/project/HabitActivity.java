package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class HabitActivity extends AppCompatActivity {

    private GridView gridView;
    private Button btnAll, btnCompleted, btnPending, btnAddHabit;
    private ImageButton btnBack;
    private ArrayList<Habit> habitList;
    private HabitAdapter habitAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.habit);

        // Initialize UI components
        gridView = findViewById(R.id.gridView);
        btnAll = findViewById(R.id.btnAll);
        btnCompleted = findViewById(R.id.btnCompleted);
        btnPending = findViewById(R.id.btnPending);
        btnAddHabit = findViewById(R.id.button);
        btnBack = findViewById(R.id.imageButton4);

        // Initialize habit list
        habitList = new ArrayList<>();
        loadSampleHabits();

        // Set up adapter
        habitAdapter = new HabitAdapter(this, habitList);
        gridView.setAdapter(habitAdapter);

        // Set click listener for adding new habit
        btnAddHabit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HabitActivity.this, NewHabitActivity.class);
                startActivity(intent);
            }
        });

        // Set click listener for back button
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Set click listeners for filter buttons
        btnAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterHabits("all");
            }
        });

        btnCompleted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterHabits("life");
            }
        });

        btnPending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterHabits("sport");
            }
        });

        // Set click listener for habit items
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Open habit detail screen
                Habit selectedHabit = habitList.get(position);
                // You can create a detail activity and open it here
            }
        });
    }

    private void loadSampleHabits() {
        // Sample data
        Habit habit1 = new Habit();
        habit1.setName("Reading");
        habit1.setQuote("A reader lives a thousand lives");
        habit1.setSection("life");

        Habit habit2 = new Habit();
        habit2.setName("Running");
        habit2.setQuote("Every step counts");
        habit2.setSection("sport");

        habitList.add(habit1);
        habitList.add(habit2);
    }

    private void filterHabits(String filter) {
        if (filter.equals("all")) {
            habitAdapter = new HabitAdapter(this, habitList);
        } else {
            ArrayList<Habit> filteredList = new ArrayList<>();
            for (Habit habit : habitList) {
                if (habit.getSection().equalsIgnoreCase(filter)) {
                    filteredList.add(habit);
                }
            }
            habitAdapter = new HabitAdapter(this, filteredList);
        }
        gridView.setAdapter(habitAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this screen
        habitAdapter.notifyDataSetChanged();
    }
}