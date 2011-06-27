package org.georemindme.community.controller.location;


import org.georemindme.community.tools.Logger;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;


public class TemporalLocationListener implements LocationListener
{
	private static int				counter	= 0;
	private static int				limit;
	private Handler					handler;
	private static LocationManager	locManager;
	
	
	public TemporalLocationListener(int limit, LocationManager loc)
	{
		this.limit = limit;
		locManager = loc;
	}
	

	public void onStart()
	{
		startTiming();
	}
	

	@Override
	public void onLocationChanged(Location location)
	{
		// TODO Auto-generated method stub
		stopTiming();
	}
	

	@Override
	public void onProviderDisabled(String provider)
	{
		// TODO Auto-generated method stub
		
	}
	

	@Override
	public void onProviderEnabled(String provider)
	{
		// TODO Auto-generated method stub
		
	}
	

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras)
	{
		// TODO Auto-generated method stub
		
	}
	

	private void stopTiming()
	{
		counter = 0;
		locManager.removeUpdates(this);
	}
	

	private void startTiming()
	{
		handler = new Handler();
		handler.postDelayed(new Timer(limit), 1000L);
	}
	
	class Timer implements Runnable
	{
		private int	timeout;
		
		
		Timer(int limit)
		{
			timeout = limit / 1000;
		}
		

		public void run()
		{
			counter++;
			if (counter > timeout)
			{
				// STOP
				stopTiming();
			}
			else
				handler.postDelayed(this, 1000L);
		}
	}
}
