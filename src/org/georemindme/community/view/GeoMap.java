package org.georemindme.community.view;


import java.util.List;

import org.georemindme.community.controller.GeoRemindMe;

import android.content.Context;
import android.location.Location;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;


public class GeoMap extends MapView
{
	private Context			context;
	private GestureDetector	gestureDetector;
	private AlertsMap		activity;
	
	
	public GeoMap(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}
	

	public GeoMap(Context arg0, String arg1)
	{
		super(arg0, arg1);
	}
	

	public GeoMap(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		// TODO Auto-generated constructor stub
		
		this.context = context;
		
		gestureDetector = new GestureDetector((OnGestureListener) context);
		gestureDetector.setOnDoubleTapListener((OnDoubleTapListener) this.context);
	}
	

	public void setActivity(AlertsMap am)
	{
		activity = am;
	}
	

	public boolean onTouchEvent(MotionEvent ev)
	{
		if (this.gestureDetector.onTouchEvent(ev))
		{
			return true;
		}
		else
		{
			return super.onTouchEvent(ev);
		}
	}
	

	public void setMyLocation()
	{
		Location location = activity.getLastLocation();
		if (location != null)
		{
			GeoPoint gp = new GeoPoint((int) (location.getLatitude() * 1E6), (int) (location.getLongitude() * 1E6));
			
			MyLocationOverlay mL = new MyLocationOverlay(context, gp);
			if(this.getOverlays().size() > 0)
				this.getOverlays().remove(0);
			this.getOverlays().add(0, mL);
			
			postInvalidate();
			//list.g
			//invalidate();
		}
		else
		{
			Log.v("Location error", "Location not found");
		}
	}
	
	public void deletePoints()
	{
		this.getOverlays().clear();
	}
	public void addAlertPoint(double lat, double lng, long serverID)
	{
		GeoPoint alertPoint = new GeoPoint(((int) (lat * 1E6)), ((int) (lng * 1E6)));
		this.getOverlays().add(new AlertOverlay(getContext(), alertPoint, serverID));
		invalidate();
	}
	
}
