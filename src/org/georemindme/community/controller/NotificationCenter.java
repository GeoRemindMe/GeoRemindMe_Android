package org.georemindme.community.controller;


import java.util.ArrayList;
import java.util.List;

import org.georemindme.community.model.Alert;
import org.georemindme.community.view.AlertDialog;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;


public class NotificationCenter
{
	private static Controller			controller;
	private static NotificationCenter	singleton				= null;
	
	private static final int			UNIQUE_NOTIFICATION_ID	= 1;
	
	
	public static NotificationCenter setUp(Controller controller)
	{
		if (singleton == null)
			singleton = new NotificationCenter(controller);
		
		return singleton;
	}
	
	private List<Alert>			alertCenter;
	
	private Handler				controllerInbox;
	private NotificationManager	notificationManager;
	
	private NotificationCenter(Controller controller)
	{
		this.controller = controller;
		
		notificationManager = (NotificationManager) controller.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
		
		alertCenter = new ArrayList<Alert>();
		
	}
	

	public void cancelAlert(long id)
	{
		if (existAlertValue(id) != -1)
		{
			deleteAlertAtPosition(id);
			refreshNotification();
		}
	}
	

	public void cancelAllAlerts()
	{
		alertCenter.clear();
		notificationManager.cancelAll();
	}
	

	private void deleteAlertAtPosition(long i)
	{
		if (i >= 0 && i < alertCenter.size())
			alertCenter.remove(i);
	}
	

	private int existAlertValue(long id)
	{
		int exist = -1;
		
		int size = alertCenter.size();
		for (int i = 0; i < size && exist == -1; i++)
		{
			if (alertCenter.get(i).getId() == id)
			{
				exist = i;
			}
		}
		
		return exist;
	}

	public void notifyAlert(Alert a)
	{
		if (existAlertValue(a.getId()) == -1)
		{
			alertCenter.add(a);
			
		}
	}
	

	public void refreshNotification()
	{
		Log.w("NOTIFICATION CENTER", "Refrescando alerta");
		Intent i = null;
		if (alertCenter.size() == 1)
		{
			
			i = new Intent(controller.getContext(), AlertDialog.class);
			Bundle extras = new Bundle();
			extras.putSerializable("ALERT", alertCenter.get(0));
			i.putExtras(extras);
		}
		else if (alertCenter.size() > 1)
		{
			Log.w("NOTIFICATION CENTER", "Hay m‡s de una alerta a modificar");
			i = new Intent(controller.getContext(), AlertDialog.class);
		}
		
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		controller.getContext().startActivity(i);
		
	}
}
