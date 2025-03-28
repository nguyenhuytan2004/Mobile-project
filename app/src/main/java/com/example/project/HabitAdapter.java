package com.example.project;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class HabitAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Habit> habitList;

    public HabitAdapter(Context context, ArrayList<Habit> habitList) {
        this.context = context;
        this.habitList = habitList;
    }

    @Override
    public int getCount() {
        return habitList.size();
    }

    @Override
    public Object getItem(int position) {
        return habitList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.habit_item, parent, false);
        }

        Habit habit = habitList.get(position);

        TextView tvHabitName = convertView.findViewById(R.id.tvHabitName);
        TextView tvHabitQuote = convertView.findViewById(R.id.tvHabitQuote);

        tvHabitName.setText(habit.getName());
        tvHabitQuote.setText(habit.getQuote());

        return convertView;
    }
}