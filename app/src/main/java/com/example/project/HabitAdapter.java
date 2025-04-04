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
    private LayoutInflater inflater;

    public HabitAdapter(Context context, ArrayList<Habit> habitList) {
        this.context = context;
        this.habitList = habitList;
        this.inflater = LayoutInflater.from(context);
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
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.habit_item, parent, false);
            holder = new ViewHolder();
            holder.tvHabitName = convertView.findViewById(R.id.tvHabitName);
            holder.tvHabitQuote = convertView.findViewById(R.id.tvHabitQuote);
            holder.tvFrequency = convertView.findViewById(R.id.tvFrequency);
            holder.tvSection = convertView.findViewById(R.id.tvSection);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Habit habit = habitList.get(position);
        holder.tvHabitName.setText(habit.getName());
        holder.tvHabitQuote.setText(habit.getQuote());
        
        // Set frequency
        if (habit.getFrequency() != null) {
            holder.tvFrequency.setText(habit.getFrequency());
        } else {
            holder.tvFrequency.setText("Daily");
        }
        
        // Set section
        if (habit.getSection() != null) {
            holder.tvSection.setText(habit.getSection());
        } else {
            holder.tvSection.setText("Others");
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView tvHabitName;
        TextView tvHabitQuote;
        TextView tvFrequency;
        TextView tvSection;
    }

    // Method to update the habit list
    public void updateData(ArrayList<Habit> newHabits) {
        this.habitList = newHabits;
        notifyDataSetChanged();
    }
}