package org.georemindme.community.controller;


import static org.georemindme.community.controller.ControllerProtocol.*;

import java.util.ArrayList;
import java.util.List;

import org.georemindme.community.R;
import org.georemindme.community.controller.appserver.Server;
import org.georemindme.community.controller.appserver.UpdateService;
import org.georemindme.community.controller.location.LocationServer;
import org.georemindme.community.model.Alert;
import org.georemindme.community.model.User;
import com.franciscojavierfernandez.android.libraries.mvcframework.controller.MVCController;
import com.franciscojavierfernandez.android.libraries.mvcframework.controller.MVCControllerStateInterface;

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

/**
 * Clase que representa el controlador de la aplicaci�n siguiendo el modelo MVC. El controlador est� basado en un modelo
 * de estados por lo cual puede presentar diferentes respuestas a los mismos mensajes seg�n el estado en el que se
 * encuentre. Cada componente de la aplicaci�n que implemente la interfaz MVCViewComponent es capaz de recibir mensajes
 * de este y actuar en consecuencia. 
 * 
 * @author franciscojavierfernandeztoro
 * @version 1.0
 * @see MVCViewController, MVCControllerExceptionMessageUnknown, MVCControllerExceptionUnknown, MVCController.
 */
public class Controller extends MVCController
{
	/**
	 * Instancia del servidor de la aplicaci�n.
	 */
	private Server						server;
	
	/**
	 * Instancia del motor de localizaci�n.
	 */
	private LocationServer				locationServer;
	
	/**
	 * Instancia del sistema gestor de notificaciones de la aplicaci�n.
	 */
	private NotificationCenter			notificationCenter;
	
	/**
	 * Estado actual del controlador.
	 */
	private MVCControllerStateInterface	state;
	
	/**
	 * Contexto del controlador.
	 */
	private Context						context;
	
	/**
	 * Instancia del controlador. Patron de dise�o: singleton.
	 */
	private static Controller			instance;
	
	/**
	 * Gestor de alarmas del android framework.
	 */
	private AlarmManager				alarmManager;
	
	/**
	 * Intent a ejecutar cuando se dispare la alarma.
	 */
	private Intent						alarmManagerIntent			= null;
	private PendingIntent				alarmManagerPendingIntent	= null;
	
	/**
	 * Controlador de preferencias del sistema.
	 */
	private PreferencesController		preferencesController;
	
	
	public static Controller getInstace(Context context)
	{
		if (instance == null)
			instance = new Controller(context);
		
		return instance;
	}
	

	public LocationServer getLocationServer()
	{
		return locationServer;
	}
	

	private Controller(Context context)
	{
		super(context);
		this.context = context;
		preferencesController = new PreferencesController(context);
		
		state = new ReadyState(this);
		
		this.changeMVCState(state);
		
		locationServer = LocationServer.getInstance(this);
		locationServer.startTrackingPosition();
		
		server = Server.getServerInstance(context, this);
		
		alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManagerIntent = new Intent(context, UpdateService.class);
		alarmManagerPendingIntent = PendingIntent.getService(context, 0, alarmManagerIntent, 0);
		
		notificationCenter = NotificationCenter.setUp(this);
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
			case PREFERENCE_AUTOUPDATE_CHANGED:
				if (PreferencesController.isAutoupdate())
					setPeriodicalUpdates();
				else
					cancelPeriodicalUpdates();
				break;
			case PREFERENCE_LOCATION_PROVIDER_ACCURACY_CHANGED:

				break;
			case PREFERENCE_LOCATION_PROVIDER_POWER_CHANGED:

				break;
			case PREFERENCE_LOCATION_UPDATE_RADIUS_CHANGED:

				break;
			case PREFERENCE_LOCATION_UPDATE_RATE_CHANGED:

				break;
			case PREFERENCE_SHOW_SATELLITE_CHANGED:

				break;
			case PREFERENCE_SHOW_TRAFFIC_CHANGED:

				break;
			case PREFERENCE_START_ON_BOOT_CHANGED:

				break;
			case PREFERENCE_SYNC_RATE_CHANGED:
				if (PreferencesController.isAutoupdate())
					setPeriodicalUpdates();
				break;
			case PREFERENCE_ZOOM_LEVEL_CHANGED:

				break;
		}
	}
	

	void isLogged()
	{
		if (server.isUserlogin())
			notifyAllMVCComponents(RESPONSE_IS_LOGGED, 0, 0, server.getUser());
		else
		{
			notifyAllMVCComponents(RESPONSE_IS_NOT_LOGGED, 0, 0, server.getLocalUser());
		}
	}
	

	public Context getContext()
	{
		return context;
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
		if (l == null)
			notifyAllMVCComponents(RESPONSE_NO_LAST_LOCATION_AVAILABLE, 0, 0, null);
		else
			notifyAllMVCComponents(RESPONSE_LAST_LOCATION, 0, 0, l);
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
		locationServer.getLastKnownAddress();
	}
	

	void getAddress(Double double1, Double double2)
	{
		// TODO Auto-generated method stub
		locationServer.getAddress(double1, double2);
	}
	
	void getCoordinatesFromAddress(String address)
	{
		locationServer.getCoordinates(address);
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
		if (lastLocation != null)
		{
			server.requestAllUndoneNearestAlerts(lastLocation.getLatitude(), lastLocation.getLongitude(), (int) 1E6);
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
	

	void changeAlertActive(boolean active, long id)
	{
		server.changeAlertActive(active, id);
	}
	

	void changeAlertDone(boolean done, long id)
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
	

	void notifyAlert(List<Alert> listado)
	{
		for (Alert a : listado)
			notificationCenter.notifyAlert(a);
		
		notificationCenter.refreshNotification();
	}
	

	public void removeNotification(long id)
	{
		notificationCenter.cancelAlert(id);
	}
	

	public void removeAllNotification()
	{
		notificationCenter.cancelAllAlerts();
	}
	

	public void deleteAlert(Alert obj)
	{
		// TODO Auto-generated method stub
		server.deleteAlert(obj);
	}
	

	public void requestNextTimelinePage()
	{
		// TODO Auto-generated method stub
		server.requestNextTimelinePage();
	}
	

	public void createNewUser(String string, String string2)
	{
		// TODO Auto-generated method stub
		server.createNewUser(string, string2);
	}
	
}
