package tschida.david.android.dogecoinwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

public class MyWidgetProvider extends AppWidgetProvider
{
	
	private static final String LOG = "tschida.david.android.dogecoinwidget";
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds)
	{
		
		Log.w(LOG, "onUpdate method called");
		
		Toast.makeText(context, "Updating DogecoinWidget", Toast.LENGTH_SHORT).show();
		
		// Get all ids
		ComponentName thisWidget = new ComponentName(context,
				MyWidgetProvider.class);
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
		
		// Build the intent to call the service
		Intent intent = new Intent(context,
				UpdateWidgetService.class);
		intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
		
		/*PendingIntent pendingIntent = PendingIntent.getService(context, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		*/
		//RemoteViews views = new RemoteViews(context.getPackageName(),
		//		R.layout.widget_layout);
		
	    //views.setOnClickPendingIntent(R.id.layout, pendingIntent);
		
		// Update the widgets via the service
		context.startService(intent);
		
		//ComponentName myWidget = new ComponentName(context, AppWidgetProvider.class);
	    //appWidgetManager.updateAppWidget(myWidget, views);
	}
}