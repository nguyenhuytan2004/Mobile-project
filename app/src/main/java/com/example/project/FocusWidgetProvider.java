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

        // Set up the intent to open FocusTab activity when the widget is clicked
        Intent openIntent = new Intent(context, FocusTab.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent openPendingIntent = PendingIntent.getActivity(context, 0, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Set up intent for start button with auto-start flag
        Intent startIntent = new Intent(context, FocusTab.class);
        startIntent.putExtra("fromWidget", true);
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