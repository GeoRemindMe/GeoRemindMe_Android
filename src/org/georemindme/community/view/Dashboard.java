package org.georemindme.community.view;


import static org.georemindme.community.controller.ControllerProtocol.*;

import org.georemindme.community.R;
import org.georemindme.community.controller.Controller;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout.LayoutParams;


public class Dashboard extends Activity implements OnClickListener, Callback
{
	private final static String	LOG	= "Dashboard-debug";
	
	private Button				mode;
	private Button				map;
	private Button				list;
	private Button				settings;
	private Button addAlertButton;
	
	private Dialog				loginDialog;
	
	private Controller			controller;
	private Handler				inboxHandler;
	
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Log.v("DASHBOARD", "onCreate");
		setContentView(R.layout.dashboard);
		
		// Get buttons from UI
		mode = (Button) findViewById(R.id.modebutton);
		mode.setOnClickListener(this);
		map = (Button) findViewById(R.id.mapbutton);
		map.setOnClickListener(this);
		list = (Button) findViewById(R.id.listbutton);
		list.setOnClickListener(this);
		settings = (Button) findViewById(R.id.preferencesbutton);
		settings.setOnClickListener(this);
		addAlertButton = (Button) findViewById(R.id.createalertButton);
		addAlertButton.setOnClickListener(this);
		
		// Create login dialog.
		loginDialog = new Dialog(Dashboard.this);
		loginDialog.setTitle("Change mode");
		loginDialog.setContentView(R.layout.logindialog);
		android.view.WindowManager.LayoutParams params = loginDialog.getWindow().getAttributes();
		params.width = LayoutParams.FILL_PARENT;
		loginDialog.getWindow().setAttributes(params);
		
		controller = Controller.getInstace(getApplicationContext());
		
		
		/*
		 * Message msg = Message.obtain(controller.getInboxHandler(),
		 * V_REQUEST_AUTOLOGIN, null); msg.sendToTarget();
		 */
		
	}
	

	public void onResume()
	{
		super.onResume();
		Log.v("DASHBOARD", "onResume");
		inboxHandler = new Handler(this);
		controller.addOutboxHandler(inboxHandler);
		controller.getInboxHandler().sendEmptyMessage(V_REQUEST_IS_LOGGED);
	}
	

	public void onStop()
	{
		super.onStop();
		Log.v("DASHBOARD", "onStop");
		controller.removeOutboxHandler(inboxHandler);
		
	}
	

	@Override
	public void onClick(View v)
	{
		Intent i;
		// TODO Auto-generated method stub
		switch(v.getId())
		{
			case R.id.modebutton:
				i = new Intent(getApplicationContext(), LoginActivity.class);
				startActivity(i);
				break;
			case R.id.preferencesbutton:
				i = new Intent(getApplicationContext(), Settings.class);
				startActivity(i);
				break;
			case R.id.createalertButton:
				Intent intent = new Intent(getApplicationContext(), AddAlarmActivity.class);
				startActivity(intent);
				break;
		}
	}
	

	@Override
	public boolean handleMessage(Message msg)
	{
		Log.v(LOG, "Message received: " + msg.toString());
		switch (msg.what)
		{
			case C_LOGIN_STARTED:
				mode.setText("Logging");
				return true;
			case C_IS_LOGGED:
				mode.setText("Connected");
				return true;
			case C_IS_NOT_LOGGED:
				mode.setText("Disconnected");
				return true;
			case C_LOGIN_FINISHED:
				mode.setText("Connected");
				Log.v(LOG, "Login has successed");
				Message m = Message.obtain(controller.getInboxHandler(), V_REQUEST_UPDATE);
				m.sendToTarget();
				return true;
			case C_LOGIN_FAILED:
				mode.setText("login failed");
				Log.v(LOG, "Login has failed");
				return true;
			case C_UPDATE_STARTED:
				Log.v(LOG, "Update started");
				return true;
			case C_UPDATE_FINISHED:
				Log.v(LOG, "Update success");
				return true;
			case C_UPDATE_FAILED:
				Log.v(LOG, "Update failed");
				return true;
		}
		return false;
	}
	
}
