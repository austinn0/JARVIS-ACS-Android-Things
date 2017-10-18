package com.mgenio.jarvisofficedoor.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mgenio.jarvisofficedoor.R;
import com.mgenio.jarvisofficedoor.models.Door;
import com.mgenio.jarvisofficedoor.utils.Utils;

public class SimpleWidgetProvider extends AppWidgetProvider {

    private DatabaseReference mDatabase;

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int count = appWidgetIds.length;

        for (int i = 0; i < count; i++) {
            int widgetId = appWidgetIds[i];

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_open_door);

            Intent intent = new Intent(context, SimpleWidgetProvider.class);
            //intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
            remoteViews.setOnClickPendingIntent(R.id.layout_widget, pendingIntent);

            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (null == intent.getAction()) {
            Toast.makeText(context, "Open Door", Toast.LENGTH_SHORT).show();
            String accessKeyName = Utils.getSetting(context.getString(R.string.preferences_active_access_key_name), "", context);
            String activeLocation = Utils.getSetting(context.getString(R.string.preferences_active_location), "", context);
            String activeAccessKey = Utils.getSetting(context.getString(R.string.preferences_active_access_key), "", context);

            Door door = new Door(activeAccessKey, accessKeyName, activeLocation);
            mDatabase = FirebaseDatabase.getInstance().getReference();
            mDatabase.child("doors").push().setValue(door);
        }
    }
}