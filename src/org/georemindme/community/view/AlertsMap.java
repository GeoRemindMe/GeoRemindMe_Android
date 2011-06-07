package org.georemindme.community.view;


import java.util.ArrayList;
import java.util.List;

import org.georemindme.community.R;
import org.georemindme.community.controller.GeoRemindMe;
import org.georemindme.community.controller.PreferencesController;
import org.georemindme.community.model.Alert;
import org.georemindme.community.model.Database;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;


public class AlertsMap extends MapActivity implements OnGestureListener,
		OnDoubleTapListener
{
	private GeoMap					map;
	private MapController			controller;
	private LocationManager			locManager;
	private Location				lastLocation		= null;
	
	private MyLocationListener		locListener			= null;
	
	private int						time;
	private int						meters;
	private String					locationProvider	= null;
	
	private int						zoom;
	private boolean					satellite;
	private boolean					traffic;
	
	private LinearLayout			bottomPanel;
	
	private ProgressDialog			pd;
	
	private String					address;
	
	private PendingIntent			pendingIntent		= null;
	
	public void onCreate(Bundle bundle)
	{
		super.onCreate(bundle);
		setContentView(R.layout.mapactivity);
		
		map = (GeoMap) findViewById(R.id.map);
		bottomPanel = (LinearLayout) findViewById(R.id.bottompanel);
		bottomPanel.setVisibility(View.GONE);
		
		controller = map.getController();
		map.setActivity(this);
		map.setBuiltInZoomControls(true);
		
		
		locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locListener = new MyLocationListener();
		
	}
	

	public void onStop()
	{
		super.onStop();
		
		Log.v("OnStop", "OnStop called");

	}
	

	public void onResume()
	{
		super.onResume();
		
	}
	

	
	

	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		
		return true;
	}
	

	public boolean onOptionsItemSelected(MenuItem item)
	{
		super.onOptionsItemSelected(item);
		
		switch (item.getItemId())
		{
			case (R.id.menu_item_preferences):
			{
				Intent i = new Intent(this, org.georemindme.community.view.Settings.class);
				startActivity(i);
				break;
			}
				
			case (R.id.menu_item_location):
			{
				setMyPosition();
				break;
			}
			case (R.id.menu_item_sync):
			{
								break;
			}
			case (R.id.menu_item_list):
			{
				Intent i = new Intent(this, org.georemindme.community.view.AlertList.class);
				startActivity(i);
				break;
			}
				
		}
		return true;
	}
	

	private void refreshLocationManager()
	{
		if (locListener != null)
		{
			locManager.removeUpdates(locListener);
		}
		
		if (locManager.isProviderEnabled(locationProvider))
		{
			Log.v("Registering", locationProvider);
			// locManager.removeUpdates(this);
			
			locManager.requestLocationUpdates(locationProvider, time * 60000, 0, locListener);
			setMyPosition();
		}
		else
		{
			AlertDialog.Builder ad = new AlertDialog.Builder(AlertsMap.this);
			ad.setTitle("Provider disabled");
			ad.setMessage("Do you want to enabled it?");
			ad.setPositiveButton("OK", new OnClickListener()
			{
				
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					ComponentName toLaunch;
					Intent intent;
					toLaunch = new ComponentName("com.android.settings", "com.android.settings.SecuritySettings");
					intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
					intent.addCategory(Intent.CATEGORY_LAUNCHER);
					intent.setComponent(toLaunch);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);
					
					Log.v("Antes del refresh", "");
					refreshLocationManager();
					Log.v("Despues del refresh", "");
				}
			});
			ad.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
			{
				
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
				}
			});
			ad.show();
		}
	}
	

	private void setMyPosition()
	{
		// TODO Auto-generated method stub
		if (lastLocation != null)
		{
			controller.animateTo(new GeoPoint((int) (lastLocation.getLatitude() * 1e6), (int) (lastLocation.getLongitude() * 1E6)));
			map.setMyLocation();
		}
		else
		{
			Toast.makeText(AlertsMap.this, "Location unavailable now", Toast.LENGTH_SHORT).show();
			lastLocation = locManager.getLastKnownLocation(locationProvider);
			
			Log.v("Location Provider", locationProvider);
			if (lastLocation != null)
			{
				Log.v("Last Location", lastLocation.toString());
				setMyPosition();
			}
		}
	}
	

	@Override
	protected boolean isRouteDisplayed()
	{
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean onDoubleTap(MotionEvent e)
	{
		GeoPoint p = map.getProjection().fromPixels((int) e.getX(), (int) e.getY());
		controller.animateTo(p);
		controller.zoomIn();
		return false;
	}
	

	@Override
	public boolean onDoubleTapEvent(MotionEvent e)
	{
		// TODO Auto-generated method stub
		return false;
	}
	

	@Override
	public boolean onSingleTapConfirmed(MotionEvent e)
	{
		// TODO Auto-generated method stub
		return false;
	}
	

	@Override
	public boolean onDown(MotionEvent e)
	{
		// TODO Auto-generated method stub
		return false;
	}
	

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY)
	{
		// TODO Auto-generated method stub
		return false;
	}
	

	@Override
	public void onLongPress(MotionEvent e)
	{
		// TODO Auto-generated method stub
		
	}
	

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY)
	{
		// TODO Auto-generated method stub
		return false;
	}
	

	@Override
	public void onShowPress(MotionEvent e)
	{
		// TODO Auto-generated method stub
		
	}
	

	@Override
	public boolean onSingleTapUp(MotionEvent e)
	{
		/*
		 * // TODO Auto-generated method stub if (bottomPanel.getVisibility() ==
		 * View.GONE) { bottomPanel.setVisibility(View.VISIBLE);
		 * map.setBuiltInZoomControls(true); } else {
		 * bottomPanel.setVisibility(View.GONE);
		 * map.setBuiltInZoomControls(false); } return false;
		 */
		return false;
	}
	

	public Location getLastLocation()
	{
		return lastLocation;
	}
	
	private class MyLocationListener implements LocationListener
	{
		
		@Override
		public void onLocationChanged(Location location)
		{
			// TODO Auto-generated method stub
			lastLocation = location;
			// Toast.makeText(AlertsMap.this, "Location has changed",
			// Toast.LENGTH_SHORT).show();
			Log.v("MyLocationListener - OnLocationChanged", location.toString());
		}
		

		@Override
		public void onProviderDisabled(String provider)
		{
			// TODO Auto-generated method stub
			Log.v("MyLocationListener - onProviderDisabled", provider);
		}
		

		@Override
		public void onProviderEnabled(String provider)
		{
			// TODO Auto-generated method stub
			Log.v("MyLocationListener - onProviderEnabled", provider);
		}
		

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras)
		{
			// TODO Auto-generated method stub
			
		}
		
	}
	
	
	private void launchNotification(Intent notificationIntent,
			int amountOfAlarms)
	{
		String text = "";
		String title = "";
		
		NotificationManager notificationManager = (NotificationManager) AlertsMap.this.getSystemService(Context.NOTIFICATION_SERVICE);
		
		notificationManager.cancelAll();
		
		pendingIntent = PendingIntent.getActivity(AlertsMap.this.getBaseContext(), 0, notificationIntent, Intent.FLAG_ACTIVITY_NEW_TASK);
		
		Notification not = new Notification();
		not.when = System.currentTimeMillis();
		not.icon = R.drawable.icon;
		
		not.flags |= Notification.FLAG_AUTO_CANCEL;
		not.flags |= Notification.FLAG_SHOW_LIGHTS;
		not.defaults |= Notification.DEFAULT_VIBRATE;
		not.defaults |= Notification.DEFAULT_LIGHTS;
		not.ledARGB = Color.WHITE;
		not.ledOnMS = 1500;
		not.ledOffMS = 1500;
		
		title = "Alarms near";
		if (amountOfAlarms == 1)
		{
			text = amountOfAlarms + " alarm is near of your location";
		}
		else
		{
			text = amountOfAlarms + " alarms are near of your location";
		}
		
		not.setLatestEventInfo(AlertsMap.this.getBaseContext(), title, text, pendingIntent);
		notificationManager.cancel(1);
		notificationManager.notify(1, not);
	}
	
}
