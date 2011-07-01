package org.georemindme.community.controller;


import java.util.ArrayList;
import java.util.List;

import org.georemindme.community.R;
import org.georemindme.community.model.Alert;
import org.georemindme.community.view.AddAlarmActivity;
import org.georemindme.community.view.UndoneAlertList;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;

import static org.georemindme.community.controller.ControllerProtocol.*;


public class NotificationCenter implements Callback
{
	private static NotificationCenter	singleton				= null;
	private static Controller			controller;
	
	private NotificationManager			notificationManager;
	
	private Handler						controllerInbox;
	private Handler						ownInbox;
	
	private static final int			UNIQUE_NOTIFICATION_ID	= 1;
	private List<Alert>					alertCenter;
	
	
	public static NotificationCenter setUp(Controller controller)
	{
		if (singleton == null)
			singleton = new NotificationCenter(controller);
		
		return singleton;
	}
	

	public void notifyAlert(Alert a)
	{
		alertCenter.add(a);
		refreshNotification();
	}
	

	public void cancelAlert(int id)
	{
		
	}
	

	private void refreshNotification()
	{
		Intent i = null;
		if (alertCenter.size() == 1)
		{
			Log.w("NOTIFICATION CENTER", "Alerta a notificar: "
					+ alertCenter.get(0).getName());
			i = new Intent(controller.getContext(), AddAlarmActivity.class);
			Bundle extras = new Bundle();
			extras.putSerializable("ALERT", alertCenter.get(0));
			i.putExtras(extras);
		}
		else if (alertCenter.size() > 1)
		{
			Log.w("NOTIFICATION CENTER", "Hay m‡s de una alerta a modificar");
			i = new Intent(controller.getContext(), UndoneAlertList.class);
		}
		
		Notification note = new Notification(R.drawable.icon, controller.getContext().getString(R.string.alert_near), System.currentTimeMillis());
		
		PendingIntent pendingIntent = PendingIntent.getActivity(controller.getContext(), 0, i, 0);
		note.setLatestEventInfo(controller.getContext(), "Alerta cerca detectada", "Click para ver detalles", pendingIntent);
		
		notificationManager.cancel(UNIQUE_NOTIFICATION_ID);
		notificationManager.notify(UNIQUE_NOTIFICATION_ID, note);
	}
	

	private NotificationCenter(Controller controller)
	{
		this.controller = controller;
		
		notificationManager = (NotificationManager) controller.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
		
		ownInbox = new Handler(this);
		controllerInbox = controller.getInboxHandler();
		controller.addOutboxHandler(ownInbox);
		
		alertCenter = new ArrayList<Alert>();
		
	}
	

	@Override
	public boolean handleMessage(Message msg)
	{
		// TODO Auto-generated method stub
		switch (msg.what)
		{
			case LS_LOCATION_CHANGED:
				/*
				 * Con cada cambio de localizaci—n vamos a comprobar las alertas
				 * que tenemos cerca que cumplen las restricciones.
				 */
				controllerInbox.obtainMessage(NS_REQUEST_ALERTS_NEAR).sendToTarget();
				return true;
		}
		return false;
	}
}
