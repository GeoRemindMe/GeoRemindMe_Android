package org.georemindme.community.view;


import static org.georemindme.community.controller.ControllerProtocol.*;

import org.georemindme.community.R;
import org.georemindme.community.controller.Controller;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout.LayoutParams;


public class Dashboard extends Activity implements OnClickListener, Callback
{
	private final static String	LOG				= "Dashboard-debug";
	
	private Button				mode;
	private Button				map;
	private Button				list;
	private Button				settings;
	private Button				addAlertButton;
	
	private Dialog				loginDialog;
	
	private Controller			controller;
	private Handler				inboxHandler;
	
	private boolean				flag_location	= false;
	
	
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

		controller.getInboxHandler().sendEmptyMessage(V_REQUEST_LAST_LOCATION);
	}
	

	public void onResume()
	{
		super.onResume();
		Log.v("DASHBOARD", "onResume");
		inboxHandler = new Handler(this);
		controller.addOutboxHandler(inboxHandler);
		controller.getInboxHandler().sendEmptyMessage(V_REQUEST_IS_LOGGED);
		
		if(flag_location)
		{
			flag_location = false;
			controller.getInboxHandler().sendEmptyMessage(V_RESET_LOCATION_PROVIDERS);
			
		}
		
		controller.getInboxHandler().sendEmptyMessage(V_REQUEST_LAST_LOCATION);
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
		switch (v.getId())
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
				i = new Intent(getApplicationContext(), AddAlarmActivity.class);
				startActivity(i);
				break;
			case R.id.listbutton:
				i = new Intent(getApplicationContext(), ListTabActivity.class);
				startActivity(i);
				break;
			case R.id.mapbutton:
				i = new Intent(getApplicationContext(), MapDialogActivity.class);
				startActivity(i);
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
			case LS_NO_PROVIDER_AVAILABLE:
				// Aqui tengo que ofrecer al usuario la opcion de habilitar la
				// localizacion.!!!!
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage("Do you want to enable any location provider?");
				builder.setCancelable(true);
				builder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
				{
					
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						// TODO Auto-generated method stub
						flag_location = true;
						Intent settingsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
						settingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
						startActivity(settingsIntent);
					}
				});
				builder.setNegativeButton("No", new DialogInterface.OnClickListener()
				{
					
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						// TODO Auto-generated method stub
						flag_location = false;
						dialog.cancel();
					}
				});
				builder.create().show();
				Log.e("MESSAGE RECEIVED", "No hay providers");
				return true;
			case C_LAST_LOCATION:
				Log.i("Last Location is: ", ((Location) msg.obj).toString());
				return true;
			case C_NO_LAST_LOCATION_AVAILABLE:
				Log.w("No last location available: ", "No hay localizaci—n reciente");
				return true;
		}
		return false;
	}
	
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		super.onOptionsItemSelected(item);
		
		switch (item.getItemId())
		{
			case (R.id.menu_item_sync):
			{
				controller.getInboxHandler().obtainMessage(V_REQUEST_UPDATE).sendToTarget();
				break;
			}
				
			case (R.id.menu_item_exit):
			{
				controller.getInboxHandler().obtainMessage(V_REQUEST_UPDATE).sendToTarget();
				System.exit(0);
				break;
			}
		
		}
		return true;
	}
}
