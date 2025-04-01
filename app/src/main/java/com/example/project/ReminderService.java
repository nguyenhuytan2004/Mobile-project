package com.example.project;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReminderService extends BroadcastReceiver {
    private static final String CHANNEL_ID = "note_reminders";
    private static final String TAG = "ReminderService";

    @Override
    public void onReceive(Context context, Intent intent) {
        String noteId = intent.getStringExtra("noteId");
        String noteTitle = intent.getStringExtra("noteTitle");

        if (noteId != null) {
            showNotification(context, noteId, noteTitle);
        }
    }

    public static void scheduleReminders(Context context) {
        SQLiteDatabase db = DatabaseHelper.getInstance(context).openDatabase();
        try {
            String query = "SELECT n.id, n.title, r.date, r.time, r.days_before, r.is_repeat " +
                    "FROM tbl_note n " +
                    "JOIN tbl_note_reminder r ON n.id = r.note_id " +
                    "WHERE r.date IS NOT NULL AND r.date != ''";

            Cursor cursor = db.rawQuery(query, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String noteId = cursor.getString(cursor.getColumnIndexOrThrow("id"));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                    String dateStr = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                    String timeStr = cursor.getString(cursor.getColumnIndexOrThrow("time"));
                    int daysBefore = cursor.getInt(cursor.getColumnIndexOrThrow("days_before"));
                    boolean isRepeat = cursor.getInt(cursor.getColumnIndexOrThrow("is_repeat")) == 1;

                    if (dateStr != null && !dateStr.isEmpty()) {
                        scheduleNoteReminder(context, noteId, title, dateStr, timeStr, daysBefore, isRepeat);
                    }
                } while (cursor.moveToNext());
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling reminders", e);
        } finally {
            DatabaseHelper.getInstance(context).closeDatabase();
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    public static void scheduleNoteReminder(Context context, String noteId, String title,
                                            String dateStr, String timeStr, int daysBefore, boolean isRepeat) {
        try {
            // Parse date from "Ngày X, tháng Y" format
            Pattern pattern = Pattern.compile("(\\d+)");
            Matcher matcher = pattern.matcher(dateStr);

            int day = 0, month = 0;
            if (matcher.find()) {
                day = Integer.parseInt(matcher.group(1));
            }
            if (matcher.find()) {
                month = Integer.parseInt(matcher.group(1));
            }

            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);

            // Set date to reminder date
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month - 1); // Calendar months are 0-based
            calendar.set(Calendar.DAY_OF_MONTH, day);

            // Apply days before
            if (daysBefore > 0) {
                calendar.add(Calendar.DAY_OF_MONTH, -daysBefore);
            }

            // Set time if available
            if (timeStr != null && !timeStr.isEmpty()) {
                String[] timeParts = timeStr.split(":");
                if (timeParts.length == 2) {
                    calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeParts[0]));
                    calendar.set(Calendar.MINUTE, Integer.parseInt(timeParts[1]));
                    calendar.set(Calendar.SECOND, 0);
                }
            } else {
                // Default to 09:00 AM if no time specified
                calendar.set(Calendar.HOUR_OF_DAY, 9);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
            }

            long triggerTime = calendar.getTimeInMillis();

            // Only schedule if reminder time is in the future
            if (triggerTime > System.currentTimeMillis()) {
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

                Intent intent = new Intent(context, ReminderService.class);
                intent.putExtra("noteId", noteId);
                intent.putExtra("noteTitle", title);

                int requestCode = Integer.parseInt(noteId);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        context,
                        requestCode,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                // Schedule the alarm
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                    );
                } else {
                    alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                    );
                }

                Log.d(TAG, "Reminder scheduled for note: " + title + " at " +
                        calendar.getTime().toString());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling individual reminder", e);
        }
    }

    private static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Note Reminders";
            String description = "Notifications for scheduled note reminders";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private static void showNotification(Context context, String noteId, String noteTitle) {
        createNotificationChannel(context);

        Intent intent = new Intent(context, NoteActivity.class);
        intent.putExtra("noteId", noteId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                Integer.parseInt(noteId),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ticktick_icon)
                .setContentTitle("TickTick Reminder")
                .setContentText(noteTitle)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            notificationManager.notify(Integer.parseInt(noteId), builder.build());
        } catch (SecurityException e) {
            Log.e(TAG, "Missing notification permission", e);
        }
    }

    public static void cancelReminder(Context context, String noteId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderService.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                Integer.parseInt(noteId),
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
            Log.d(TAG, "Reminder cancelled for note ID: " + noteId);
        }
    }
}
