package org.georemindme.community.controller.appserver;


import static org.georemindme.community.controller.ControllerProtocol.REQUEST_UPDATE;

import org.georemindme.community.controller.Controller;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


/**
 * Clase que representa el servicio del sistema que se encargar� de
 * auto-actualizar la cach� de datos si el usuario as� lo requiere.
 * 
 * @author franciscojavierfernandeztoro
 * @version 1.0
 */
public class UpdateService extends Service
{
	/**
	 * Controlador de la aplicaci�n.
	 * 
	 * @see Controller
	 */
	private Controller	controller;
	
	
	@Override
	public IBinder onBind(Intent arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}
	

	public int onStartCommand(Intent intent, int f, int sID)
	{
		controller = Controller.getInstace(getApplicationContext());
		
		controller.sendMessage(REQUEST_UPDATE);
		stopSelf();
		
		return START_NOT_STICKY;
	}
	
}
