package org.georemindme.community.controller;


import static org.georemindme.community.controller.ControllerProtocol.*;

import java.util.ArrayList;
import java.util.List;

import org.georemindme.community.controller.appserver.Server;
import org.georemindme.community.controller.appserver.UpdateService;
import org.georemindme.community.controller.location.LocationServer;
import org.georemindme.community.model.Alert;
import org.georemindme.community.model.User;
import org.georemindme.community.tools.Logger;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;


public class Controller
{
	private static final String		LOG							= "Controller-debug";
	
	private Server					server;
	private LocationServer			locationServer;
	private NotificationCenter notificationCenter;
	
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
		preferencesController = new PreferencesController(context);
		
		
		int millis = PreferencesController.getTime();
		int meters = PreferencesController.getRadius();
		
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
		
		locationServer = locationServer.getInstance(this);
		locationServer.startTrackingPosition();
		
		alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManagerIntent = new Intent(context, UpdateService.class);
		alarmManagerPendingIntent = PendingIntent.getService(context, 0, alarmManagerIntent, 0);
		
		notificationCenter = NotificationCenter.setUp(this);
	}
	

	private void handleMessage(Message msg)
	{
		// TODO Auto-generated method stub
		if (!state.handleMessage(msg))
		{
			Log.e(LOG, "Unknown message " + msg.toString());
		}
	}
	
	public Context getContext()
	{
		return context;
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
	

	final synchronized void notifyOutboxHandlers(int what, int arg1, int arg2, Object obj)
	{
		if (outboxHandlers.isEmpty())
		{
			Logger.write(this, "There is no outbox handlers available");
		}
		else
		{
			for (Handler h : outboxHandlers)
			{
				Message msg = Message.obtain(h, what, arg1, arg2, obj);
				msg.sendToTarget();
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
		locationServer.stopTrackingPosition();
	}
	

	final void setPeriodicalUpdates()
	{
		boolean autoupdate = preferencesController.isAutoupdate();
		if (autoupdate)
		{
			cancelPeriodicalUpdates();
			
			int time = preferencesController.getSyncRate();
			alarmManager.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(), time * 1000, alarmManagerPendingIntent);
		}
		else
		{
			cancelPeriodicalUpdates();
		}
	}
	

	final void cancelPeriodicalUpdates()
	{
		if (alarmManagerPendingIntent != null)
		{
			alarmManager.cancel(alarmManagerPendingIntent);
		}
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
		if (server.isUserlogin())
			notifyOutboxHandlers(C_IS_LOGGED, 0, 0, server.getUser());
		else
		{
			notifyOutboxHandlers(C_IS_NOT_LOGGED, 0, 0, server.getDatabaseUser());
			
		}
	}


	public Server getServerInstance()
	{
		// TODO Auto-generated method stub
		return server;
	}


	public void getLastLocation()
	{
		// TODO Auto-generated method stub
		Location l = locationServer.getLastKnownLocation();
		if(l == null)
			notifyOutboxHandlers(C_NO_LAST_LOCATION_AVAILABLE, 0, 0, null);
		else
			notifyOutboxHandlers(C_LAST_LOCATION, 0, 0, l);
	}


	public final void restartLocationServer()
	{
		// TODO Auto-generated method stub
		locationServer.stopTrackingPosition();
		locationServer.startTrackingPosition();
	}


	public void getLastKnownAddress()
	{
		// TODO Auto-generated method stub
		notifyOutboxHandlers(V_REQUEST_LAST_KNOWN_ADDRESS, 0, 0, null);
	}


	void getAddress(Double double1, Double double2)
	{
		// TODO Auto-generated method stub
		locationServer.getAddress(double1, double2);
	}


	void saveAlert(Alert alert)
	{
		// TODO Auto-generated method stub
		server.saveAlert(alert);
	}

	void updateAlert(Alert alert)
	{
		server.updateAlert(alert);
	}
	
	void requestAllUndoneAlerts()
	{
		// TODO Auto-generated method stub
		
		Location lastLocation = locationServer.getLastKnownLocation();
		if(lastLocation != null)
		{
			server.requestAllUndoneNearestAlerts(lastLocation.getLatitude(), lastLocation.getLongitude(), (int)1E6);
		}
		else
		{
			server.requestAllUndoneAlerts();
		}
	}
	
	void requestAllDoneAlerts()
	{
		server.requestAllDoneAlerts();
	}
	
	void requestAllMutedAlerts()
	{
		server.requestAllMutedAlerts();
	}
	
	void changeAlertActive(boolean active, int id)
	{
		server.changeAlertActive(active, id);
	}
	
	void changeAlertDone(boolean done, int id)
	{
		server.changeAlertDone(done, id);
	}
	
	void requestAlarmsNear()
	{
		Location l = locationServer.getLastKnownLocation();
		
		double latE6 = l.getLatitude();
		double lngE6 = l.getLongitude();
		int meters = PreferencesController.getRadius();
		
		server.requestAlarmsNear(latE6, lngE6, meters);
	}
	
	void notifyAlert(Alert a)
	{
		notificationCenter.notifyAlert(a);
	}
	
	
	public void removeNotification(int id)
	{
		notificationCenter.cancelAlert(id);
	}

}
