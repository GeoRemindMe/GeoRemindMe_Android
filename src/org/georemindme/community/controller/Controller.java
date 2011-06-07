package org.georemindme.community.controller;


import static org.georemindme.community.controller.ControllerProtocol.*;

import java.util.ArrayList;
import java.util.List;

import org.georemindme.community.controller.appserver.Server;
import org.georemindme.community.controller.appserver.UpdateService;
import org.georemindme.community.controller.location.LocationServer;
import org.georemindme.community.model.User;
import org.georemindme.community.tools.Logger;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;


public class Controller
{
	private static final String		LOG							= "Controller-debug";
	
	private Server					server;
	private LocationServer			locationServer;
	
	private final HandlerThread		inboxHandlerThread;
	private final Handler			inboxHandler;
	private final List<Handler>		outboxHandlers				= new ArrayList<Handler>();
	
	private ControllerState			state;
	
	private Context					context;
	
	private static Controller		instance;
	
	private AlarmManager			alarmManager;
	private Intent					alarmManagerIntent			= null;
	private PendingIntent			alarmManagerPendingIntent	= null;
	private PreferencesController	preferencesController;
	
	
	public static Controller getInstace(Context context)
	{
		if (instance == null)
			instance = new Controller(context);
		
		return instance;
	}
	

	private Controller(Context context)
	{
		
		this.context = context;
		
		locationServer = locationServer.getInstance(context);
		Log.v("Iniciando location server", "STARTING");
		int millis = PreferencesController.getTime() * 1000; //Falta multiplicarlo por 60
		int meters = PreferencesController.getRadius();
		
		millis = 10000;
		meters = 0;
		
		Log.i("Millis: ", "" + millis);
		Log.i("Meters: ", "" + meters);
		
		locationServer.startTrackingPosition(millis, meters, 
				PreferencesController.getLocationProviderAccuracy(), PreferencesController.getLocationProviderPower(),
				PreferencesController.is3Location());
		
		inboxHandlerThread = new HandlerThread("Controller Inbox");
		inboxHandlerThread.start();
		
		state = new ReadyState(this);
		
		inboxHandler = new Handler(inboxHandlerThread.getLooper())
		{
			public void handleMessage(Message msg)
			{
				Controller.this.handleMessage(msg);
			}
		};
		
		server = Server.getInstance(context, inboxHandler);
		
		alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManagerIntent = new Intent(context, UpdateService.class);
		alarmManagerPendingIntent = PendingIntent.getService(context, 0, alarmManagerIntent, 0);
		
		preferencesController = new PreferencesController(context);
	}
	

	private void handleMessage(Message msg)
	{
		// TODO Auto-generated method stub
		Log.v(LOG, "Received message: " + msg.toString());
		Logger.write(this, "Message received: " + msg.what);
		if (!state.handleMessage(msg))
		{
			Log.v(LOG, "Unknown message " + msg.toString());
		}
	}
	

	public final Handler getInboxHandler()
	{
		return inboxHandler;
	}
	

	public final void addOutboxHandler(Handler handler)
	{
		outboxHandlers.add(handler);
	}
	

	public final void removeOutboxHandler(Handler handler)
	{
		outboxHandlers.remove(handler);
	}
	

	final void notifyOutboxHandlers(int what, int arg1, int arg2, Object obj)
	{
		if (outboxHandlers.isEmpty())
		{
			Log.v(LOG, "No outbox handlers available. Message: " + what);
			Logger.write(this, "There is no outbox handlers available");
		}
		else
		{
			for (Handler h : outboxHandlers)
			{
				Message msg = Message.obtain(h, what, arg1, arg2, obj);
				msg.sendToTarget();
				Logger.write(this, "Message: " + msg.what + " sended");
			}
		}
	}

	final void quit()
	{
		notifyOutboxHandlers(C_QUIT, 0, 0, null);
	}
	

	final void logout()
	{
		server.logout();
		locationServer.stopTrackingPosition();
	}
	

	final void login(User user)
	{
		server.login(user);
	}
	

	final void dispose()
	{
		inboxHandlerThread.getLooper().quit();
	}
	

	final void setPeriodicalUpdates()
	{
		boolean autoupdate = preferencesController.isAutoupdate();
		if (autoupdate)
		{
			cancelPeriodicalUpdates();
			
			int time = preferencesController.getSyncRate();
			Log.v("UPDATE", "TIME: " + time);
			alarmManager.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(), time * 1000, alarmManagerPendingIntent);
		}
	}
	

	final void cancelPeriodicalUpdates()
	{
		if (alarmManagerPendingIntent != null)
		{
			alarmManager.cancel(alarmManagerPendingIntent);
		}
	}
	

	final boolean isUserlogin()
	{
		return server.isUserlogin();
	}
	

	final void preferencesChanged(Integer obj)
	{
		// TODO Auto-generated method stub
		switch (obj)
		{
			case P_AUTOUPDATE_CHANGED:
				if (preferencesController.isAutoupdate())
					setPeriodicalUpdates();
				else
					cancelPeriodicalUpdates();
				break;
			case P_LOCATION_PROVIDER_ACCURACY_CHANGED:

				break;
			case P_LOCATION_PROVIDER_POWER_CHANGED:

				break;
			case P_LOCATION_UPDATE_RADIUS_CHANGED:

				break;
			case P_LOCATION_UPDATE_RATE_CHANGED:

				break;
			case P_SHOW_SATELLITE_CHANGED:

				break;
			case P_SHOW_TRAFFIC_CHANGED:

				break;
			case P_START_ON_BOOT_CHANGED:

				break;
			case P_SYNC_RATE_CHANGED:
				if (PreferencesController.isAutoupdate())
					setPeriodicalUpdates();
				break;
			case P_ZOOM_LEVEL_CHANGED:

				break;
		}
	}
	

	void isLogged()
	{
		if (!isUserlogin())
			notifyOutboxHandlers(C_IS_NOT_LOGGED, 0, 0, null);
		else
		{
			notifyOutboxHandlers(C_IS_LOGGED, 0, 0, server.getUser());
		}
	}


	public Server getServerInstance()
	{
		// TODO Auto-generated method stub
		return server;
	}
	
}
