package com.example.project;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

public class HabitActionReceiver extends BroadcastReceiver {
    private static final String TAG = "HabitActionReceiver";
    private static final int NOTIFICATION_ID_BASE = 1000;
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getStringExtra("action");
        int habitId = intent.getIntExtra("habit_id", -1);
        
        if (habitId == -1) return;
        
        if ("complete".equals(action)) {
            markHabitAsCompleted(context, habitId);
            
            // Cancel the notification
            NotificationManager notificationManager = 
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(NOTIFICATION_ID_BASE + habitId);
        }
    }
    
    private void markHabitAsCompleted(Context context, int habitId) {
        // Here you would update your database to mark this habit instance as completed
        // This is just a placeholder - you'll need to implement the actual tracking
        SQLiteDatabase db = DatabaseHelper.getInstance(context).openDatabase();
        
        try {
            // Example: Insert a record into a habit_progress table
            /* 
            ContentValues values = new ContentValues();
            values.put("habit_id", habitId);
            values.put("completion_date", getCurrentDateString());
            values.put("is_completed", 1);
            db.insert("habit_progress", null, values);
            */
            
            // For now, just show a toast
            Toast.makeText(context, "Habit marked as completed", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Habit " + habitId + " marked as completed");
            
        } catch (Exception e) {
            Log.e(TAG, "Error marking habit as completed", e);
        } finally {
            DatabaseHelper.getInstance(context).closeDatabase();
        }
    }
}