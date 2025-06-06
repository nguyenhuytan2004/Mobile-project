package com.example.project;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class FocusWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    // In FocusWidgetProvider.java
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        // Create the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.focus_widget);

        // Intent mở FocusTab bình thường
        Intent openIntent = new Intent(context, FocusTab.class);
        openIntent.setAction("OPEN_FOCUS");
        openIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent openPendingIntent = PendingIntent.getActivity(context, 0, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Intent để auto start từ widget
        Intent startIntent = new Intent(context, FocusTab.class);
        startIntent.setAction("START_FOCUS");
        startIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent startPendingIntent = PendingIntent.getActivity(context, 1, startIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Set up click listeners
        views.setOnClickPendingIntent(R.id.widget_layout, openPendingIntent);
        views.setOnClickPendingIntent(R.id.widget_icon, openPendingIntent);
        views.setOnClickPendingIntent(R.id.widget_timer, openPendingIntent);
        views.setOnClickPendingIntent(R.id.widget_start_btn, startPendingIntent);

        // Update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}