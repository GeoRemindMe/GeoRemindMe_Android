package org.georemindme.community.controller.location;


import org.georemindme.community.tools.Logger;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.util.Log;



public class LocationServer
{
	private static final String		LOG			= "LocationServer";
	
	private static LocationServer	singleton	= null;
	
	private Context					context;
	private static LocationManager	locationManager;
	
	private Handler handler;
	private Timer timer;

	
	private static final String[]	status		= { "Out of service",
			"Temporarily unavailable", "Available" };
	
	
	public static LocationServer getInstance(Context context)
	{
		if (singleton == null)
			singleton = new LocationServer(context);
		
		return singleton;
	}
	

	private LocationServer(Context context)
	{
		this.context = context;
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		
		
	}
	

	public void startTrackingPosition(int miliseconds, int meters,
			int accuracy_pattern, int power_pattern, boolean costAllowed)
	{
		/*
		timer = new Timer(miliseconds);
		handler = new Handler();
		handler.post(timer);
		*/
		TemporalLocationListener tll = new TemporalLocationListener(40000, locationManager);
		tll.onStart();
		locationManager.requestLocationUpdates("gps", 0, 0, tll);
		Location last = locationManager.getLastKnownLocation("gps");
		String tmp;
		if (last == null)
			tmp = "No location available";
		else
			tmp = last.toString();
		Logger.write(this, tmp);
	}
	

	public void stopTrackingPosition()
	{
		handler.removeCallbacks(timer);
		timer = null;
	}
	
	private boolean isBetterLocation(Location location, Location currentBestLocation)
	{
		if(currentBestLocation == null)
		{
			return true;
		}
		
		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > (2 * 60 * 1000);
		boolean isSignificantlyOlder = timeDelta < - (2 * 60 * 1000);
		
		boolean isNewer = timeDelta > 0;
		/*
		if(isSignificantlyNewer)
		{
			return true;
		}
		else if(isSignificantlyOlder)
		{
			return false;
		}
		*/
		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
	    boolean isLessAccurate = accuracyDelta > 0;
	    boolean isMoreAccurate = accuracyDelta < 0;
	    boolean isSignificantlyLessAccurate = accuracyDelta > 200;

	    // Check if the old and new location are from the same provider
	    boolean isFromSameProvider = isSameProvider(location.getProvider(),
	            currentBestLocation.getProvider());

	    // Determine location quality using a combination of timeliness and accuracy
	    if (isMoreAccurate) {
	        return true;
	    } else if (isNewer && !isLessAccurate) {
	        return true;
	    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
	        return true;
	    }
	    return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
	    if (provider1 == null) {
	      return provider2 == null;
	    }
	    return provider1.equals(provider2);
	}
	
	class Timer implements Runnable
	{

		private int period;
		
		Timer(int period)
		{
			this.period = period;
		}
		
		@Override
		public void run()
		{
			// TODO Auto-generated method stub
			Log.v("TIMER", "LOCSERVICE - running");
			handler.postDelayed(this, period);
		}
		
	}
}
