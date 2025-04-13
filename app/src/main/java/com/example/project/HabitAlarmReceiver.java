package com.example.project;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class HabitAlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "HabitAlarmReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        int habitId = intent.getIntExtra("habit_id", -1);
        String habitName = intent.getStringExtra("habit_name");
        boolean autoPopup = intent.getBooleanExtra("auto_popup", false);
        
        Log.d(TAG, "Alarm received for habit: " + habitName);
        
        // Show notification
        HabitNotificationHelper.showHabitReminder(context, habitId, habitName, autoPopup);
        
        // If auto popup is enabled, show floating window
        if (autoPopup) {
            Intent popupIntent = new Intent(context, HabitPopupActivity.class);
            popupIntent.putExtra("habit_id", habitId);
            popupIntent.putExtra("habit_name", habitName);
            popupIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(popupIntent);
        }
    }
}