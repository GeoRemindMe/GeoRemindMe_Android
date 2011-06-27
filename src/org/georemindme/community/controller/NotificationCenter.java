package org.georemindme.community.controller;

import org.georemindme.community.R;
import org.georemindme.community.model.Alert;
import org.georemindme.community.view.AddAlarmActivity;
import org.georemindme.community.view.UndoneAlertList;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;

import static org.georemindme.community.controller.ControllerProtocol.*;

public class NotificationCenter implements Callback
{
	private static NotificationCenter singleton;
	private static Controller controller;
	
	private NotificationManager notificationManager;
	
	private Handler controllerInbox;
	private Handler ownInbox;
	
	public static NotificationCenter setUp(Controller controller)
	{
		if(singleton == null)
			singleton = new NotificationCenter(controller);
		
		return singleton;
	}
	
	public void notifyAlert(Alert a)
	{
		Notification note = new Notification(R.drawable.icon, a.getName() + "\n" + a.getDescription(), System.currentTimeMillis());
		
		Intent i = new Intent(controller.getContext(), AddAlarmActivity.class);
		Bundle extras = new Bundle();
		extras.putSerializable("ALERT", a);
		i.putExtras(extras);
		
		PendingIntent pendingIntent = PendingIntent.getActivity(controller.getContext(), (int)a.getId(), i, 0);
		
		note.setLatestEventInfo(controller.getContext(), a.getName(), a.getDescription(), pendingIntent);
		notificationManager.notify((int)a.getId(), note);
		
	}
	
	public void cancelAlert(int id)
	{
		notificationManager.cancel(id);
	}
	
	private NotificationCenter(Controller controller)
	{
		this.controller = controller;
		ownInbox = new Handler(this);
		controllerInbox = controller.getInboxHandler();
		controller.addOutboxHandler(ownInbox);
		
		notificationManager = (NotificationManager) controller.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
	}

	@Override
	public boolean handleMessage(Message msg)
	{
		// TODO Auto-generated method stub
		switch(msg.what)
		{
			case LS_LOCATION_CHANGED:
				/*
				 * Con cada cambio de localizaci—n vamos a comprobar las alertas que tenemos cerca que cumplen
				 * las restricciones.
				 */
				controllerInbox.obtainMessage(NS_REQUEST_ALERTS_NEAR).sendToTarget();
				return true;
		}
		return false;
	}
}
