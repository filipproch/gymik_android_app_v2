package com.jacktech.gymik.widget;

import com.jacktech.gymik.R;
import com.jacktech.gymik.SuplovDetailsActivity;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

public class SuplovWidgetProvider extends AppWidgetProvider{

	public static String EXTRA_DETAILS = "com.jacktech.gymik.widget.Suplov.ViewDetails";
	
	@Override
	public void onUpdate(Context c, AppWidgetManager manager, int[] appWidgetIds){
		for(int i = 0;i<appWidgetIds.length;i++){
			Intent intent = new Intent(c, SuplovWidgetService.class);
			
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
			intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
			
			RemoteViews widget = new RemoteViews(c.getPackageName(), R.layout.widget_suplov_layout);
			widget.setRemoteAdapter(appWidgetIds[i], R.id.widget_suplov_listView, intent);
			
			Intent clickIntent = new Intent(c, SuplovDetailsActivity.class);
			PendingIntent clickPI = PendingIntent.getActivity(c, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			
			widget.setPendingIntentTemplate(R.id.widget_suplov_listView, clickPI);
			
			manager.updateAppWidget(appWidgetIds[i], widget);
		}
		
		super.onUpdate(c, manager, appWidgetIds);
	}
	
}
