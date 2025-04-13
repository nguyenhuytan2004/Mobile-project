package com.example.project;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class HabitNotificationHelper {
    private static final String TAG = "HabitNotificationHelper";
    private static final String CHANNEL_ID = "habit_reminders";
    private static final String CHANNEL_NAME = "Habit Reminders";
    private static final int NOTIFICATION_ID_BASE = 1000;

    public static void scheduleHabitReminders(Context context) {
        SQLiteDatabase db = DatabaseHelper.getInstance(context).openDatabase();
        
        try {
            // Query all habits with reminders
            Cursor cursor = db.rawQuery(
                    "SELECT id, name, reminder, auto_popup FROM tbl_habit WHERE reminder IS NOT NULL", 
                    null);
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndex("id"));
                    String name = cursor.getString(cursor.getColumnIndex("name"));
                    String reminderTime = cursor.getString(cursor.getColumnIndex("reminder"));
                    boolean autoPopup = cursor.getInt(cursor.getColumnIndex("auto_popup")) == 1;
                    
                    // Schedule reminder for this habit
                    scheduleReminder(context, id, name, reminderTime, autoPopup);
                    
                } while (cursor.moveToNext());
                
                cursor.close();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling habit reminders", e);
        } finally {
            DatabaseHelper.getInstance(context).closeDatabase();
        }
    }
    
    private static void scheduleReminder(Context context, int habitId, String habitName, 
            String reminderTime, boolean autoPopup) {
        try {
            // Parse reminder time (HH:mm)
            String[] timeParts = reminderTime.split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);
            
            // Create calendar for today with this time
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            
            // If time has already passed today, schedule for tomorrow
            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }
            
            // Create intent for alarm
            Intent intent = new Intent(context, HabitAlarmReceiver.class);
            intent.putExtra("habit_id", habitId);
            intent.putExtra("habit_name", habitName);
            intent.putExtra("auto_popup", autoPopup);
            
            // Create unique request code based on habit ID
            int requestCode = NOTIFICATION_ID_BASE + habitId;
            
            // Create pending intent
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            
            // Get alarm manager
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            
            // Schedule exact alarm
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, 
                        calendar.getTimeInMillis(), pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, 
                        calendar.getTimeInMillis(), pendingIntent);
            }
            
            Log.d(TAG, "Habit reminder scheduled: " + habitName + " at " + 
                    hour + ":" + minute + " (" + calendar.getTimeInMillis() + ")");
                    
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling reminder for habit: " + habitName, e);
        }
    }
    
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            
            channel.setDescription("Reminders for your habits");
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 250, 250, 250});
            
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    
    public static void showHabitReminder(Context context, int habitId, String habitName, boolean autoPopup) {
        // Create an intent to open HabitDetailActivity when notification is tapped
        Intent intent = new Intent(context, HabitDetailActivity.class);
        intent.putExtra("habit_id", habitId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, habitId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_habit) // You'll need to create/use an appropriate icon
                .setContentTitle("Habit Reminder")
                .setContentText("Time for your habit: " + habitName)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(new long[]{0, 250, 250, 250})
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);
        
        // Add complete action
        Intent completeIntent = new Intent(context, HabitActionReceiver.class);
        completeIntent.putExtra("action", "complete");
        completeIntent.putExtra("habit_id", habitId);
        PendingIntent completePendingIntent = PendingIntent.getBroadcast(
                context, habitId + 100, completeIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        builder.addAction(R.drawable.checkbox_checked, "Complete", completePendingIntent);
        
        // Show notification
        NotificationManager notificationManager = 
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID_BASE + habitId, builder.build());
        
        // Schedule next reminder for tomorrow
        rescheduleHabitReminder(context, habitId, habitName, autoPopup);
    }
    
    private static void rescheduleHabitReminder(Context context, int habitId, 
            String habitName, boolean autoPopup) {
        SQLiteDatabase db = DatabaseHelper.getInstance(context).openDatabase();
        
        try {
            // Get reminder time for this habit
            Cursor cursor = db.rawQuery(
                    "SELECT reminder FROM tbl_habit WHERE id = ?", 
                    new String[] { String.valueOf(habitId) });
            
            if (cursor != null && cursor.moveToFirst()) {
                String reminderTime = cursor.getString(cursor.getColumnIndex("reminder"));
                cursor.close();
                
                // Schedule for tomorrow
                scheduleReminder(context, habitId, habitName, reminderTime, autoPopup);
            }
        } finally {
            DatabaseHelper.getInstance(context).closeDatabase();
        }
    }
}