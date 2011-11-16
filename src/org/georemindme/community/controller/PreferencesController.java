package org.georemindme.community.controller;


import static org.georemindme.community.controller.ControllerProtocol.PREFERENCE_3G_LOCATION;
import static org.georemindme.community.controller.ControllerProtocol.PREFERENCE_AUTOUPDATE_CHANGED;
import static org.georemindme.community.controller.ControllerProtocol.PREFERENCE_LOCATION_PROVIDER_ACCURACY_CHANGED;
import static org.georemindme.community.controller.ControllerProtocol.PREFERENCE_LOCATION_PROVIDER_POWER_CHANGED;
import static org.georemindme.community.controller.ControllerProtocol.PREFERENCE_LOCATION_UPDATE_RADIUS_CHANGED;
import static org.georemindme.community.controller.ControllerProtocol.PREFERENCE_LOCATION_UPDATE_RATE_CHANGED;
import static org.georemindme.community.controller.ControllerProtocol.PREFERENCE_PREFERENCE_CHANGED;
import static org.georemindme.community.controller.ControllerProtocol.PREFERENCE_SHOW_SATELLITE_CHANGED;
import static org.georemindme.community.controller.ControllerProtocol.PREFERENCE_SHOW_TRAFFIC_CHANGED;
import static org.georemindme.community.controller.ControllerProtocol.PREFERENCE_START_ON_BOOT_CHANGED;
import static org.georemindme.community.controller.ControllerProtocol.PREFERENCE_SYNC_RATE_CHANGED;
import static org.georemindme.community.controller.ControllerProtocol.PREFERENCE_ZOOM_LEVEL_CHANGED;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.location.Criteria;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;


public class PreferencesController implements OnSharedPreferenceChangeListener
{
	private static final String			LOG									= "PreferencesController";
	
	/*************************************************/
	
	public static final String			PREFS								= "PREFS";
	public static final String			PREF_DEFAULT_ZOOM_LEVEL				= "PREF_DEFAULT_ZOOM_LEVEL";
	public static final String			PREF_SHOW_SATELLITE					= "PREF_SHOW_SATELLITE";
	public static final String			PREF_LOCATION_UPDATE_RADIUS			= "PREF_LOCATION_UPDATE_RADIUS";
	public static final String			PREF_LOCATION_UPDATE_RATE			= "PREF_LOCATION_UPDATE_RATE";
	public static final String			PREF_LOCATION_PROVIDER_ACCURACY		= "PREF_LOCATION_PROVIDER_ACCURACY";
	public static final String			PREF_LOCATION_PROVIDER_POWER		= "PREF_LOCATION_PROVIDER_POWER";
	public static final String			PREF_SHOW_TRAFFIC					= "PREF_SHOW_TRAFFIC";
	public static final String			PREF_SYNC_RATE						= "PREF_SYNC_RATE";
	public static final String			PREF_START_ON_BOOT					= "PREF_SYNC_ON_BOOT";
	public static final String			PREF_AUTOUPDATE						= "PREF_AUTOUPDATE";
	public static final String			PREF_3G_LOCATION					= "PREF_3G_LOCATION";
	
	/*************************************************/
	
	public static int					DEFAULT_ZOOM_VALUE					= 16;
	public static String				DEFAULT_LOCATION_PROVIDER_ACCURACY	= "ACCURACY_FINE";
	public static String				DEFAULT_LOCATION_PROVIDER_POWER		= "POWER_MEDIUM";
	public static String				DEFAULT_LOCATION_UPDATE_RADIUS		= "10";
	public static String				DEFAULT_LOCATION_UPDATE_RATE		= "5";
	public static boolean				DEFAULT_SHOW_SATELLITE				= false;
	public static boolean				DEFAULT_SHOW_TRAFFIC				= false;
	public static final String			DEFAULT_SYNC_RATE					= "60";
	public static final boolean			DEFAULT_START_ON_BOOT				= false;
	public static final boolean			DEFAULT_AUTOUPDATE					= false;
	public static final boolean			DEFAULT_3G_LOCATION					= false;
	
	/*************************************************/
	
	private static int					pref_default_zoom_level;
	private static boolean				pref_show_satellite;
	private static int					pref_location_update_radius;
	private static int					pref_location_update_rate;
	private static int					pref_location_provider_accuracy;
	private static int					pref_location_provider_power;
	private static boolean				pref_show_traffic;
	private static int					pref_sync_rate;
	private static boolean				pref_start_on_boot;
	public static boolean				pref_autoupdate;
	public static boolean				pref_3g_location;
	
	private static SharedPreferences	prefs;
	private static Editor				editor;
	
	private Context						context;
	
	
	public PreferencesController(Context context)
	{
		this.context = context;
		
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
		prefs.registerOnSharedPreferenceChangeListener(this);
		
		loadPreferences();
	}
	

	public void loadPreferences()
	{
		pref_default_zoom_level = prefs.getInt(PreferencesController.PREF_DEFAULT_ZOOM_LEVEL, PreferencesController.DEFAULT_ZOOM_VALUE);
		
		String tmp = prefs.getString(PREF_LOCATION_PROVIDER_ACCURACY, DEFAULT_LOCATION_PROVIDER_ACCURACY);
		if (tmp.equals("ACCURACY_FINE"))
		{
			pref_location_provider_accuracy = Criteria.ACCURACY_FINE;
		}
		else
		{
			pref_location_provider_accuracy = Criteria.ACCURACY_COARSE;
		}
		
		tmp = prefs.getString(PREF_LOCATION_PROVIDER_POWER, DEFAULT_LOCATION_PROVIDER_POWER);
		if (tmp.equals("POWER_HIGH"))
		{
			pref_location_provider_power = Criteria.POWER_HIGH;
		}
		else if (tmp.equals("POWER_LOW"))
		{
			pref_location_provider_power = Criteria.POWER_LOW;
		}
		else
		{
			pref_location_provider_power = Criteria.POWER_MEDIUM;
		}
		pref_location_update_radius = (int) Float.parseFloat(prefs.getString(PREF_LOCATION_UPDATE_RADIUS, DEFAULT_LOCATION_UPDATE_RADIUS));
		pref_location_update_rate = Integer.parseInt(prefs.getString(PREF_LOCATION_UPDATE_RATE, DEFAULT_LOCATION_UPDATE_RATE));
		pref_location_update_rate *= 60;
		
		pref_show_satellite = prefs.getBoolean(PREF_SHOW_SATELLITE, PreferencesController.DEFAULT_SHOW_SATELLITE);
		pref_show_traffic = prefs.getBoolean(PreferencesController.PREF_SHOW_TRAFFIC, DEFAULT_SHOW_TRAFFIC);
		pref_sync_rate = Integer.parseInt(prefs.getString(PREF_SYNC_RATE, DEFAULT_SYNC_RATE));
		pref_sync_rate *= 60;
		
		pref_start_on_boot = prefs.getBoolean(PreferencesController.PREF_START_ON_BOOT, DEFAULT_START_ON_BOOT);
		pref_autoupdate = prefs.getBoolean(PreferencesController.PREF_AUTOUPDATE, DEFAULT_AUTOUPDATE);
		pref_3g_location = prefs.getBoolean(PREF_3G_LOCATION, PreferencesController.DEFAULT_3G_LOCATION);
		
	}
	

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key)
	{
		Controller controller = Controller.getInstace(context);
		Message msg = controller.obtainInboxMessage();
		
		Controller.getInstace(context).sendMessage(PREFERENCE_PREFERENCE_CHANGED);
		// TODO Auto-generated method stub
		if (key.compareTo(PreferencesController.PREF_DEFAULT_ZOOM_LEVEL) == 0)
		{
			pref_default_zoom_level = prefs.getInt(PreferencesController.PREF_DEFAULT_ZOOM_LEVEL, PreferencesController.DEFAULT_ZOOM_VALUE);
			// controllerInbox.sendEmptyMessage(P_ZOOM_LEVEL_CHANGED);
			msg.obj = new Integer(PREFERENCE_ZOOM_LEVEL_CHANGED);
		}
		
		else if (key.compareTo(PreferencesController.PREF_SHOW_SATELLITE) == 0)
		{
			pref_show_satellite = prefs.getBoolean(PREF_SHOW_SATELLITE, PreferencesController.DEFAULT_SHOW_SATELLITE);
			// controllerInbox.sendEmptyMessage(P_SHOW_SATELLITE_CHANGED);
			msg.obj = new Integer(PREFERENCE_SHOW_SATELLITE_CHANGED);
		}
		
		else if (key.compareTo(PreferencesController.PREF_SHOW_TRAFFIC) == 0)
		{
			pref_show_traffic = prefs.getBoolean(PREF_SHOW_TRAFFIC, PreferencesController.DEFAULT_SHOW_TRAFFIC);
			// controllerInbox.sendEmptyMessage(P_SHOW_TRAFFIC_CHANGED);
			msg.obj = new Integer(PREFERENCE_SHOW_TRAFFIC_CHANGED);
		}
		
		else if (key.compareTo(PreferencesController.PREF_LOCATION_UPDATE_RADIUS) == 0)
		{
			pref_location_update_radius = (int) Float.parseFloat(prefs.getString(PREF_LOCATION_UPDATE_RADIUS, PreferencesController.DEFAULT_LOCATION_UPDATE_RADIUS));
			// controllerInbox.sendEmptyMessage(P_LOCATION_UPDATE_RADIUS_CHANGED);
			msg.obj = new Integer(PREFERENCE_LOCATION_UPDATE_RADIUS_CHANGED);
		}
		
		else if (key.compareTo(PreferencesController.PREF_LOCATION_UPDATE_RATE) == 0)
		{
			pref_location_update_rate = Integer.parseInt(prefs.getString(PREF_LOCATION_UPDATE_RATE, PreferencesController.DEFAULT_LOCATION_UPDATE_RATE));
			// controllerInbox.sendEmptyMessage(P_LOCATION_UPDATE_RATE_CHANGED);
			pref_location_update_rate *= 60;
			msg.obj = new Integer(PREFERENCE_LOCATION_UPDATE_RATE_CHANGED);
		}
		
		else if (key.compareTo(PREF_LOCATION_PROVIDER_ACCURACY) == 0)
		{
			String tmp = prefs.getString(PREF_LOCATION_PROVIDER_ACCURACY, DEFAULT_LOCATION_PROVIDER_ACCURACY);
			if (tmp.equals("ACCURACY_FINE"))
			{
				pref_location_provider_accuracy = Criteria.ACCURACY_FINE;
			}
			else
			{
				pref_location_provider_accuracy = Criteria.ACCURACY_COARSE;
			}
			
			// controllerInbox.sendEmptyMessage(P_LOCATION_PROVIDER_ACCURACY_CHANGED);
			msg.obj = new Integer(PREFERENCE_LOCATION_PROVIDER_ACCURACY_CHANGED);
		}
		else if (key.compareTo(PREF_LOCATION_PROVIDER_POWER) == 0)
		{
			String tmp = prefs.getString(PREF_LOCATION_PROVIDER_POWER, DEFAULT_LOCATION_PROVIDER_POWER);
			if (tmp.equals("POWER_HIGH"))
			{
				pref_location_provider_power = Criteria.POWER_HIGH;
			}
			else if (tmp.equals("POWER_LOW"))
			{
				pref_location_provider_power = Criteria.POWER_LOW;
			}
			else
			{
				pref_location_provider_power = Criteria.POWER_MEDIUM;
			}
			
			// controllerInbox.sendEmptyMessage(P_LOCATION_PROVIDER_POWER_CHANGED);
			msg.obj = new Integer(PREFERENCE_LOCATION_PROVIDER_POWER_CHANGED);
		}
		else if (key.compareTo(PreferencesController.PREF_SYNC_RATE) == 0)
		{
			pref_sync_rate = Integer.parseInt(prefs.getString(PREF_SYNC_RATE, PreferencesController.DEFAULT_SYNC_RATE));
			// controllerInbox.sendEmptyMessage(P_SYNC_RATE_CHANGED);
			pref_sync_rate *= 60;
			msg.obj = new Integer(PREFERENCE_SYNC_RATE_CHANGED);
		}
		
		else if (key.compareTo(PreferencesController.PREF_START_ON_BOOT) == 0)
		{
			pref_start_on_boot = prefs.getBoolean(PreferencesController.PREF_START_ON_BOOT, PreferencesController.DEFAULT_START_ON_BOOT);
			// controllerInbox.sendEmptyMessage(P_START_ON_BOOT_CHANGED);
			msg.obj = new Integer(PREFERENCE_START_ON_BOOT_CHANGED);
		}
		
		else if (key.compareTo(PreferencesController.PREF_AUTOUPDATE) == 0)
		{
			pref_autoupdate = prefs.getBoolean(PreferencesController.PREF_AUTOUPDATE, PreferencesController.pref_autoupdate);
			// controllerInbox.sendEmptyMessage(P_AUTOUPDATE_CHANGED);
			msg.obj = new Integer(PREFERENCE_AUTOUPDATE_CHANGED);
		}
		
		else if (key.compareTo(PreferencesController.PREF_3G_LOCATION) == 0)
		{
			pref_3g_location = prefs.getBoolean(PreferencesController.PREF_3G_LOCATION, DEFAULT_3G_LOCATION);
			
			msg.obj = new Integer(PREFERENCE_3G_LOCATION);
		}
		Log.v(LOG, "onSharedPreferenceChanged - end");
		controller.sendMessage(msg.what, msg.obj);
	}
	

	/**
	 * @return the pref_default_zoom_level
	 */
	public static int getZoom()
	{
		return pref_default_zoom_level;
	}
	

	/**
	 * @param pref_default_zoom_level
	 *            the pref_default_zoom_level to set
	 */
	public static void setZoom(int pref_default_zoom_level)
	{
		PreferencesController.pref_default_zoom_level = pref_default_zoom_level;
		
		editor = prefs.edit();
		editor.putInt(PreferencesController.PREF_DEFAULT_ZOOM_LEVEL, PreferencesController.pref_default_zoom_level);
		editor.commit();
	}
	

	/**
	 * @return the pref_autoupdate
	 */
	public static boolean isAutoupdate()
	{
		return pref_autoupdate;
	}
	

	/**
	 * @param pref_autoupdate
	 *            the pref_autoupdate to set
	 */
	public static void setAutoupdate(boolean pref_autoupdate)
	{
		PreferencesController.pref_autoupdate = pref_autoupdate;
		
		editor = prefs.edit();
		editor.putBoolean(PreferencesController.PREF_AUTOUPDATE, PreferencesController.pref_autoupdate);
		editor.commit();
	}
	

	/**
	 * @return the pref_sync_rate
	 */
	public static int getSyncRate()
	{
		return pref_sync_rate;
	}
	

	/**
	 * @param pref_sync_rate
	 *            the pref_sync_rate to set
	 */
	public static void setSyncRate(int pref_sync_rate)
	{
		PreferencesController.pref_sync_rate = pref_sync_rate;
		
		editor = prefs.edit();
		editor.putInt(PreferencesController.PREF_SYNC_RATE, PreferencesController.pref_sync_rate);
		editor.commit();
	}
	

	/**
	 * @return the pref_start_on_boot
	 */
	public static boolean isStart_on_boot()
	{
		return pref_start_on_boot;
	}
	

	/**
	 * @param pref_start_on_boot
	 *            the pref_start_on_boot to set
	 */
	public static void setStartOnBoot(boolean pref_start_on_boot)
	{
		PreferencesController.pref_start_on_boot = pref_start_on_boot;
		
		editor = prefs.edit();
		editor.putBoolean(PreferencesController.PREF_START_ON_BOOT, PreferencesController.pref_start_on_boot);
		editor.commit();
	}
	

	/**
	 * @return the pref_show_satellite
	 */
	public static boolean isSatellite()
	{
		return pref_show_satellite;
	}
	

	/**
	 * @param pref_show_satellite
	 *            the pref_show_satellite to set
	 */
	public static void setSatellite(boolean pref_show_satellite)
	{
		PreferencesController.pref_show_satellite = pref_show_satellite;
		
		editor = prefs.edit();
		editor.putBoolean(PreferencesController.PREF_SHOW_SATELLITE, PreferencesController.pref_show_satellite);
		editor.commit();
	}
	

	/**
	 * @return the pref_show_traffic
	 */
	public static boolean isTraffic()
	{
		return pref_show_traffic;
	}
	

	/**
	 * @param pref_show_satellite
	 *            the pref_show_satellite to set
	 */
	public static void setTraffic(boolean pref_show_traffic)
	{
		PreferencesController.pref_show_traffic = pref_show_traffic;
		
		editor = prefs.edit();
		editor.putBoolean(PreferencesController.PREF_SHOW_TRAFFIC, PreferencesController.pref_show_traffic);
		editor.commit();
	}
	

	/**
	 * @return the pref_location_update_radius
	 */
	public static int getRadius()
	{
		return pref_location_update_radius;
	}
	

	/**
	 * @param pref_location_update_radius
	 *            the pref_location_update_radius to set
	 */
	public static void setRadius(int pref_location_update_radius)
	{
		PreferencesController.pref_location_update_radius = pref_location_update_radius;
		
		editor = prefs.edit();
		editor.putInt(PreferencesController.PREF_LOCATION_UPDATE_RADIUS, PreferencesController.pref_location_update_radius);
		editor.commit();
	}
	

	/**
	 * @return the pref_location_update_rate
	 */
	public static int getTime()
	{
		return pref_location_update_rate;
	}
	

	/**
	 * @param pref_location_update_rate
	 *            the pref_location_update_rate to set
	 */
	public static void setTime(int pref_location_update_rate)
	{
		PreferencesController.pref_location_update_rate = pref_location_update_rate;
		
		editor = prefs.edit();
		editor.putInt(PreferencesController.PREF_LOCATION_UPDATE_RATE, PreferencesController.pref_location_update_rate);
		editor.commit();
	}
	

	/**
	 * @return the pref_location_provider
	 */
	public static int getLocationProviderAccuracy()
	{
		return pref_location_provider_accuracy;
	}
	

	/**
	 * @param pref_location_provider_accuracy
	 *            the pref_location_provider_accuracy to set
	 */
	public static void setLocationProviderAccuracy(
			int pref_location_provider_accuracy)
	{
		String tmp;
		if (pref_location_provider_accuracy == Criteria.ACCURACY_FINE)
			tmp = "ACCURACY_FINE";
		else
			tmp = "ACCURACY_COARSE";
		
		PreferencesController.pref_location_provider_accuracy = pref_location_provider_accuracy;
		editor = prefs.edit();
		editor.putString(PreferencesController.PREF_LOCATION_PROVIDER_ACCURACY, tmp);
		editor.commit();
	}
	

	/**
	 * @return the pref_location_provider
	 */
	public static int getLocationProviderPower()
	{
		return pref_location_provider_power;
	}
	

	/**
	 * @param pref_location_provider_power
	 *            the pref_location_provider_power to set
	 */
	public static void setLocationProviderPower(int pref_location_provider_power)
	{
		String tmp;
		if (pref_location_provider_power == Criteria.POWER_HIGH)
			tmp = "POWER_HIGH";
		else if (pref_location_provider_power == Criteria.POWER_LOW)
			tmp = "POWER_LOW";
		else
			tmp = "POWER_MEDIUM";
		
		PreferencesController.pref_location_provider_power = pref_location_provider_power;
		editor = prefs.edit();
		editor.putString(PreferencesController.PREF_LOCATION_PROVIDER_POWER, tmp);
		editor.commit();
	}
	

	/*
	 * @return the pref_network_location
	 */
	public static boolean is3Location()
	{
		return pref_3g_location;
	}
	

	/**
	 * @param pref_autoupdate
	 *            the pref_autoupdate to set
	 */
	public static void set3GLocation(boolean pref_3g_location)
	{
		PreferencesController.pref_3g_location = pref_3g_location;
		
		editor = prefs.edit();
		editor.putBoolean(PreferencesController.PREF_3G_LOCATION, PreferencesController.pref_3g_location);
		editor.commit();
	}
}
