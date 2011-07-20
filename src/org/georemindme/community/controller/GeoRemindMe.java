/**
 * 
 */
package org.georemindme.community.controller;


import java.util.ArrayList;
import java.util.List;

import org.georemindme.community.R;
import org.georemindme.community.model.Database;
import org.georemindme.community.model.User;
import org.georemindme.community.tools.Logger;

import com.franciscojavierfernandez.android.libraries.mvcframework.controller.MVCControllerMessages;

import static org.georemindme.community.controller.ControllerProtocol.*;

import android.app.Application;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;


/**
 * Default controller. This class initializes everything before any activity is
 * shown.
 * 
 * @extends Application
 * @see android.app.Application
 * @author fj.
 * 
 */
public class GeoRemindMe extends Application
{
	
	private static final String	LOG	= "GeoRemindMe";
	/**
	 * Class field that represents the controller' singleton.
	 * 
	 * @author fj.
	 */
	private static GeoRemindMe	singleton;
	
	private Controller			controller;
	
	
	// ---------------------------------------------------------
	// ---------------------------------------------------------
	
	/**
	 * First method invoked as soon as applications starts.
	 * 
	 * @author fj.
	 */
	public void onCreate()
	{
		super.onCreate();
		
		controller = Controller.getInstace(getApplicationContext());
		
		controller.sendMessage(REQUEST_AUTOLOGIN);
		if (PreferencesController.isStart_on_boot()
				&& PreferencesController.isAutoupdate())
			controller.sendMessage(REQUEST_PERIODICAL_UPDATES_ON);
		
		// Logger.start(getApplicationContext(), 0);
	}
	

	/**
	 * If system has memory issues and it has to kill processes, this callback
	 * will be triggered.
	 * 
	 * @author fj.
	 */
	public void onLowMemory()
	{
		super.onLowMemory();
		
	}
	

	/**
	 * If the applications is shutting down, this method will be called.
	 * 
	 * @author fj.
	 */
	public void onTerminate()
	{
		super.onTerminate();
		
		controller.cancelPeriodicalUpdates();
		controller.sendMessage(MVCControllerMessages.MVCCONTROLLER_QUIT);
		
		// Logger.finish();
	}
	
	// -------------------------------------------------------------
	// -------------------------------------------------------------
	
}
