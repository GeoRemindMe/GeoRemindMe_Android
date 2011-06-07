package org.georemindme.community.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import static org.georemindme.community.controller.ControllerProtocol.*;

public class Booter extends BroadcastReceiver
{
	private static final String LOG = "BOOTER";
	@Override
	public void onReceive(Context context, Intent intent)
	{
		Log.v(LOG, "onReceive " + intent.getAction());
	}
	
}
