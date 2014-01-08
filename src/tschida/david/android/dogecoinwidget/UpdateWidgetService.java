package tschida.david.android.dogecoinwidget;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

public class UpdateWidgetService extends Service
{
	private static final String TAG = "tschida.david.android.dogecoinwidget.UpdateWidgetService";
	protected static final String DATA = "tschida.david.android.dogecoinwidget.DATA";
	protected static final String BALANCE = "tschida.david.android.dogecoinwidget.BALANCE";
	int[] allWidgetIds;
	String[] addresses;
	RetrieveBalanceTask task;
	
	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			if (intent.getAction().equals("MESSAGE"))
			{
				Toast.makeText(context, intent.getStringExtra("MESSAGE"),
						Toast.LENGTH_LONG).show();
			} else
			{
				updateWidget(context, intent);
			}
		}
	};
	
	@Override
	public void onCreate()
	{
		LocalBroadcastManager.getInstance(this).registerReceiver(
				mMessageReceiver, new IntentFilter(DATA));
		LocalBroadcastManager.getInstance(this).registerReceiver(
				mMessageReceiver, new IntentFilter("MESSAGE"));
		
	}
	
	protected void updateWidget(Context context, Intent intent)
	{
		
		double[] balances = new double[addresses.length];
		double totalBalance = 0;
		for (int i = 0; i < addresses.length; i++)
		{
			balances[i] = intent.getDoubleExtra(BALANCE + addresses[i], 0);
			totalBalance += balances[i];
		}
		
		AppWidgetManager appWidgetManager = AppWidgetManager
				.getInstance(UpdateWidgetService.this.getApplicationContext());
		if(allWidgetIds == null)
			allWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
		for (int widgetId : allWidgetIds)
		{
			RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
					R.layout.widget_layout);
			Log.w("WidgetExample", "Balance: " + totalBalance);
			
			remoteViews.setTextViewText(R.id.pool, "Total: " + totalBalance);
			
			Intent clickIntent = new Intent(context, UpdateWidgetService.class);
	        
			clickIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
			clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
					allWidgetIds);
			
			PendingIntent pendingIntent = PendingIntent.getService(context, 0, clickIntent, 0);
			Log.d(TAG, "Pending Intent = " + pendingIntent.toString());
			remoteViews.setOnClickPendingIntent(R.id.layout, pendingIntent);
			
			appWidgetManager.updateAppWidget(widgetId, remoteViews);
		}
		
		RemoteViews remoteViews = new RemoteViews(getApplicationContext()
				.getPackageName(), R.layout.widget_layout);
		remoteViews.setViewVisibility(R.id.progress, View.INVISIBLE);
		
		for(int id : allWidgetIds)
		{
			appWidgetManager.updateAppWidget(id, remoteViews);
		}
		
		LocalBroadcastManager.getInstance(this).unregisterReceiver(
				mMessageReceiver);
		stopSelf();
	}
	
	@Override
	@Deprecated
	public void onStart(Intent intent, int startId)
	{
		handleStart(intent);
	}
	
	private void handleStart(Intent intent)
	{
		Log.i(TAG, "Called");
		
		allWidgetIds = intent
				.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
		
		AssetManager am = getApplicationContext().getAssets();
		ArrayList<String> addr = new ArrayList<String>();
		try
		{
			InputStream is = am.open("addresses");
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			String mLine;
		    while ((mLine = reader.readLine()) != null) {
		       addr.add(mLine);
		    }
		    reader.close();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		addresses = addr.toArray(new String[1]);
		
		
		//Logging...
		String logStr = "{ ";
		for(String s : addresses)
		{
			logStr += s + ", ";
		}
		Log.e(TAG, "\n\n\n" + logStr + " }\n\n\n");
		//end logging
		
		RemoteViews remoteViews = new RemoteViews(getApplicationContext()
				.getPackageName(), R.layout.widget_layout);
		
		remoteViews.setViewVisibility(R.id.progress, View.VISIBLE);
		remoteViews.setProgressBar(R.id.progress, addresses.length, 0, true);
		
		AppWidgetManager appWidgetManager = AppWidgetManager
				.getInstance(UpdateWidgetService.this.getApplicationContext());
		for(int id : allWidgetIds)
		{
			appWidgetManager.updateAppWidget(id, remoteViews);
		}
		
		if (task == null)
		{
			task = new RetrieveBalanceTask();
		}
		task.execute(addresses);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		handleStart(intent);
		return START_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}
	
	public boolean isOnline()
	{
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		return networkInfo != null && networkInfo.isConnected();
	}
	
	class RetrieveBalanceTask extends AsyncTask<String, Void, Intent>
	{
		@Override
		protected Intent doInBackground(String... addresses)
		{
			
			RemoteViews remoteViews = new RemoteViews(getApplicationContext()
					.getPackageName(), R.layout.widget_layout);
			
			remoteViews
					.setProgressBar(R.id.progress, addresses.length, 0, true);
			
			Intent intent = new Intent(DATA);
			
			for (String address : addresses)
			{
				Log.d(TAG, "Running...");
				double balance;
				if (isOnline())
				{
					balance = DogeChainApi.addressbalance(address);
					
					double oldBalance = Double
							.longBitsToDouble(PreferenceManager
									.getDefaultSharedPreferences(
											UpdateWidgetService.this).getLong(
											address, (long) -1l));
					
					/*if (oldBalance != Double.longBitsToDouble(-1l)
							&& balance != oldBalance)
					{
						// Transaction...
						double difference = balance - oldBalance;
						
						String change = (difference < 0) ? "decreased"
								: "increased";
						
						LocalBroadcastManager.getInstance(
								UpdateWidgetService.this).sendBroadcast(
								new Intent("MESSAGE").putExtra("MESSAGE",
										"Your balance has " + change + " by "
												+ Math.abs(difference)
												+ " DOGE."));
					}*/
					PreferenceManager
							.getDefaultSharedPreferences(
									UpdateWidgetService.this)
							.edit()
							.putLong(address,
									Double.doubleToRawLongBits(balance))
							.commit();
				} else
				{
					balance = Double.longBitsToDouble(PreferenceManager
							.getDefaultSharedPreferences(
									UpdateWidgetService.this).getLong(address,
									0l));
				}
				intent.putExtra(BALANCE + address, balance);
			}
			
			return intent;
		}
		
		@Override
		protected void onPostExecute(Intent intent)
		{
			LocalBroadcastManager.getInstance(UpdateWidgetService.this)
					.sendBroadcast(intent);
		}
	}
	
}