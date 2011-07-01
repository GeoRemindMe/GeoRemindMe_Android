package org.georemindme.community.controller.location;


import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.TimerTask;

import org.apache.http.client.CircularRedirectException;
import org.georemindme.community.R;
import org.georemindme.community.controller.Controller;
import org.georemindme.community.controller.PreferencesController;
import org.georemindme.community.tools.Logger;

import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;

import static org.georemindme.community.controller.ControllerProtocol.*;


public class LocationServer implements Callback
{
	private static final String		LOG						= "LocationServer";
	
	private static LocationServer	singleton				= null;
	
	private Controller				controller;
	private static LocationManager	locationManager;
	
	private String					bestLocationProvider	= null;
	private List<String>			locationProviders		= null;
	private Criteria				userCriteria			= null;
	private int						timeToRefresh;
	private int						distanceToRefresh;
	
	private LocationListener		bestLocationListener;
	private LocationListener		temporalLocationListener;
	
	private Location				lastKnownLocation		= null;
	
	private Handler					controllerInbox;
	private Handler					ownInbox;
	
	
	public static LocationServer getInstance(Controller controller)
	{
		if (singleton == null)
			singleton = new LocationServer(controller);
		
		return singleton;
	}
	

	private LocationServer(Controller controller)
	{
		this.controller = controller;
Log.i("LOCATION SERVER", "Constructor");		
		ownInbox = new Handler(this);
		controllerInbox = controller.getInboxHandler();
		controller.addOutboxHandler(ownInbox);
		
		locationManager = (LocationManager) controller.getContext().getSystemService(Context.LOCATION_SERVICE);
		
		setTimeToRefresh();
		setDistanceToRefresh();
		
		bestLocationListener = new LocationListener()
		{
			
			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras)
			{
				// TODO Auto-generated method stub
				switch (status)
				{
					case LocationProvider.OUT_OF_SERVICE:

						break;
					case LocationProvider.TEMPORARILY_UNAVAILABLE:
						locationManager.removeUpdates(temporalLocationListener);
						for (String s : locationProviders)
							locationManager.requestLocationUpdates(s, timeToRefresh, distanceToRefresh, temporalLocationListener);
						break;
					case LocationProvider.AVAILABLE:
						locationManager.removeUpdates(temporalLocationListener);
						break;
				}
			}
			

			@Override
			public void onProviderEnabled(String provider)
			{
				// TODO Auto-generated method stub
				locationManager.removeUpdates(temporalLocationListener);
			}
			

			@Override
			public void onProviderDisabled(String provider)
			{
				// TODO Auto-generated method stub
				locationManager.removeUpdates(temporalLocationListener);
				for (String s : locationProviders)
					locationManager.requestLocationUpdates(s, timeToRefresh, 0, temporalLocationListener);
			}
			

			@Override
			public void onLocationChanged(Location location)
			{
				// TODO Auto-generated method stub
				updateLocation(location);
			}
			
		};
		
		temporalLocationListener = new LocationListener()
		{
			
			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras)
			{
				// TODO Auto-generated method stub
				
			}
			

			@Override
			public void onProviderEnabled(String provider)
			{
				// TODO Auto-generated method stub
				
			}
			

			@Override
			public void onProviderDisabled(String provider)
			{
				// TODO Auto-generated method stub
				
			}
			

			@Override
			public void onLocationChanged(Location location)
			{
				// TODO Auto-generated method stub
				updateLocation(location);
			}
		};
		
	}
	

	public Location getLastKnownLocation()
	{
		if (bestLocationProvider == null)
		{
			controllerInbox.obtainMessage(LS_NO_PROVIDER_AVAILABLE).sendToTarget();
			return null;
		}
		
		Location bestProvider = locationManager.getLastKnownLocation(bestLocationProvider);
		// ESTE COMENTARIO SOLUCIONA EL ERROR DE LOCALIZACIÓN QUE SE PRODUCÍA
		// POR ARRASTRAR LA POSICIÓN
		// DEL USUARIO EN EL MAPA.
		
		// SOLUCION PROVISIONAL.
		
		/*
		 * Log.i("getLastKnownLocation - LocationServer",
		 * bestProvider.toString()); if (isBetterLocation(bestProvider,
		 * lastKnownLocation)) lastKnownLocation = bestProvider;
		 */
		lastKnownLocation = bestProvider;
		return lastKnownLocation;
	}
	

	private synchronized void updateLocation(Location location)
	{
Log.i("LOCATION SERVER", "updateLocation " + location.getLatitude() + " // "+ location.getLongitude());
		// TODO Auto-generated method stub
		if (isBetterLocation(location, lastKnownLocation))
			lastKnownLocation = location;
		
		controllerInbox.obtainMessage(LS_LOCATION_CHANGED, lastKnownLocation).sendToTarget();
	}
	

	private void setLocationProviders()
			throws LocationProviderUnavailableException
	{
		// TODO Auto-generated method stub
		userCriteria = new Criteria();
		userCriteria.setAccuracy(PreferencesController.getLocationProviderAccuracy());
		userCriteria.setPowerRequirement(PreferencesController.getLocationProviderPower());
		
		bestLocationProvider = locationManager.getBestProvider(userCriteria, true);
		
		if (bestLocationProvider == null)
			throw new LocationProviderUnavailableException();
		
		locationProviders = locationManager.getProviders(true);
		if (locationProviders != null && !locationProviders.isEmpty()
				&& bestLocationProvider != null)
			locationProviders.remove(bestLocationProvider);
	}
	

	public void getLastKnownAddress()
	{
		Thread t = new Thread("AddressThread")
		{
			public void run()
			{
				Geocoder gc = new Geocoder(controller.getContext().getApplicationContext(), Locale.getDefault());
				try
				{
					List<Address> addresses = null;
					if (lastKnownLocation != null)
						addresses = gc.getFromLocation(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(), 5);
					else
						controllerInbox.obtainMessage(LS_GETTING_ADDRESS_FAILED).sendToTarget();
					if (addresses != null && !addresses.isEmpty())
					{
						controllerInbox.obtainMessage(LS_GETTING_ADDRESS_FINISHED, addresses.get(0)).sendToTarget();
					}
					else
					{
						controllerInbox.obtainMessage(LS_GETTING_ADDRESS_FINISHED, null).sendToTarget();
					}
				}
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					controllerInbox.obtainMessage(LS_GETTING_ADDRESS_FAILED).sendToTarget();
					e.printStackTrace();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		};
		t.start();
		controllerInbox.sendEmptyMessage(LS_GETTING_ADDRESS_STARTED);
	}
	

	public void startTrackingPosition()
	{
		setTimeToRefresh();
		setDistanceToRefresh();
		try
		{
			setLocationProviders();
			locationManager.requestLocationUpdates(bestLocationProvider, timeToRefresh, distanceToRefresh, bestLocationListener);
		}
		catch (LocationProviderUnavailableException e)
		{
			// TODO Auto-generated catch block
			// Send message to controller.
			controllerInbox.obtainMessage(LS_NO_PROVIDER_AVAILABLE).sendToTarget();
			e.printStackTrace();
		}
		
	}
	

	public void stopTrackingPosition()
	{
		locationManager.removeUpdates(bestLocationListener);
		locationManager.removeUpdates(temporalLocationListener);
	}
	

	private void setDistanceToRefresh()
	{
		// TODO Auto-generated method stub
		distanceToRefresh = PreferencesController.getRadius();
		
		distanceToRefresh = 0;
	}
	

	private void setTimeToRefresh()
	{
		// TODO Auto-generated method stub
		timeToRefresh = PreferencesController.getTime();
	}
	

	private boolean isBetterLocation(Location location,
			Location currentBestLocation)
	{
		if (currentBestLocation == null)
		{
			return true;
		}
		
		if (location != null && currentBestLocation != null)
		{
			long timeDelta = location.getTime() - currentBestLocation.getTime();
			boolean isSignificantlyNewer = timeDelta > (2 * 60 * 1000);
			boolean isSignificantlyOlder = timeDelta < -(2 * 60 * 1000);
			
			boolean isNewer = timeDelta > 0;
			/*
			 * if(isSignificantlyNewer) { return true; } else
			 * if(isSignificantlyOlder) { return false; }
			 */
			int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
			boolean isLessAccurate = accuracyDelta > 0;
			boolean isMoreAccurate = accuracyDelta < 0;
			boolean isSignificantlyLessAccurate = accuracyDelta > 200;
			
			// Check if the old and new location are from the same provider
			boolean isFromSameProvider = isSameProvider(location.getProvider(), currentBestLocation.getProvider());
			
			// Determine location quality using a combination of timeliness and
			// accuracy
			if (isMoreAccurate)
			{
				return true;
			}
			else if (isNewer && !isLessAccurate)
			{
				return true;
			}
			else if (isNewer && !isSignificantlyLessAccurate
					&& isFromSameProvider)
			{
				return true;
			}
		}
		return false;
	}
	

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2)
	{
		if (provider1 == null)
		{
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}
	

	@Override
	public boolean handleMessage(Message msg)
	{
		// TODO Auto-generated method stub
		switch (msg.what)
		{
			case V_REQUEST_LAST_KNOWN_ADDRESS:
				getLastKnownAddress();
				return true;
		}
		return false;
	}
	

	public void getAddress(final Double double1, final Double double2)
	{
		// TODO Auto-generated method stub
		Thread t = new Thread("AddressThread")
		{
			public void run()
			{
				Geocoder gc = new Geocoder(controller.getContext().getApplicationContext(), Locale.getDefault());
				try
				{
					List<Address> addresses = null;
					addresses = gc.getFromLocation(double1, double2, 5);
					
					if (!addresses.isEmpty())
					{
						controllerInbox.obtainMessage(LS_GETTING_ADDRESS_FINISHED, addresses.get(0)).sendToTarget();
					}
					else
					{
						controllerInbox.obtainMessage(LS_GETTING_ADDRESS_FAILED, null);
					}
				}
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					controllerInbox.obtainMessage(LS_GETTING_ADDRESS_FAILED).sendToTarget();
					e.printStackTrace();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		};
		t.start();
		controllerInbox.sendEmptyMessage(LS_GETTING_ADDRESS_STARTED);
	}
	
}
