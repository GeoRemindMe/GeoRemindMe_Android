package org.georemindme.community.view;


import org.georemindme.community.R;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.view.Window;
import android.view.WindowManager;


public class Settings extends PreferenceActivity
{
	/*
	private ListPreference		zoomLevel;
	private CheckBoxPreference	showSatellite;
	private CheckBoxPreference	fullScreen;
	private ListPreference		updateRadius;
	private ListPreference		updateRate;
	private ListPreference		locationProvider;
	*/
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.preferences);
		
	}
	
}
