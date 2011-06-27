package org.georemindme.community.controller.appserver;

import org.georemindme.community.controller.Controller;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import static org.georemindme.community.controller.ControllerProtocol.*;

public class UpdateService extends Service
{
	private static final String LOG = "SERVICE";
	
	private Controller controller;
	
	public int onStartCommand(Intent intent, int f, int sID)
	{
		controller = Controller.getInstace(getApplicationContext());
		
		Message msg = Message.obtain(controller.getInboxHandler(), S_REQUEST_UPDATE);
		msg.sendToTarget();
		
		stopSelf();
		
		return START_NOT_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}
	
}
