package org.georemindme.community.view;


import static org.georemindme.community.controller.ControllerProtocol.*;

import org.georemindme.community.R;
import org.georemindme.community.controller.Controller;

import com.franciscojavierfernandez.android.libraries.mvcframework.view.MVCViewComponent;

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


public class Dashboard extends Activity implements OnClickListener
{
	private final static String	LOG				= "Dashboard-debug";
	
	private Button 				timelineButton;
	private Button				map;
	private Button				list;
	private Button				settings;
	private Button				addAlertButton;
	
	private Dialog				loginDialog;
	
	private Controller			controller;
	private Handler				inboxHandler;
	
	private boolean				flag_location	= false;
	
	private MVCViewComponent connector = null;
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dashboard);
		
		list = (Button) findViewById(R.id.listbutton);
		list.setOnClickListener(this);
		settings = (Button) findViewById(R.id.preferencesbutton);
		settings.setOnClickListener(this);
		addAlertButton = (Button) findViewById(R.id.createalertButton);
		addAlertButton.setOnClickListener(this);
		timelineButton = (Button) findViewById(R.id.timelinebutton);
		timelineButton.setOnClickListener(this);
		
		// Create login dialog.
		loginDialog = new Dialog(Dashboard.this);
		loginDialog.setTitle(R.string.change_mode);
		loginDialog.setContentView(R.layout.logindialog);
		android.view.WindowManager.LayoutParams params = loginDialog.getWindow().getAttributes();
		params.width = LayoutParams.FILL_PARENT;
		loginDialog.getWindow().setAttributes(params);
		
		controller = Controller.getInstace(getApplicationContext());
		connector = new MVCViewComponent(controller)
		{
			
			@Override
			public boolean handleMessage(Message msg)
			{
				switch (msg.what)
				{
					case RESPONSE_LOGIN_STARTED:
						return true;
					case RESPONSE_IS_LOGGED:
						return true;
					case RESPONSE_IS_NOT_LOGGED:
						return true;
					case RESPONSE_LOGIN_FINISHED:
						controller.sendMessage(REQUEST_UPDATE);
						return true;
					case RESPONSE_LOGIN_FAILED:
						return true;
					case RESPONSE_UPDATE_STARTED:
						
						return true;
					case RESPONSE_UPDATE_FINISHED:

						return true;
					case RESPONSE_UPDATE_FAILED:

						return true;
					case RESPONSE_NO_PROVIDER_AVAILABLE:
						// Aqui tengo que ofrecer al usuario la opcion de habilitar la
						// localizacion.!!!!
						AlertDialog.Builder builder = new AlertDialog.Builder(Dashboard.this);
						builder.setMessage(R.string.do_you_want_to_enable_any_location_provider);
						builder.setCancelable(true);
						builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
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
						builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener()
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
						return true;
					case RESPONSE_LAST_LOCATION:

						return true;
					case RESPONSE_NO_LAST_LOCATION_AVAILABLE:

						return true;
				}
				return false;
			}
		};
		
		controller.sendMessage(REQUEST_LAST_LOCATION);
		
	}
	

	public void onResume()
	{
		super.onResume();
		Log.v("DASHBOARD", "onResume");
		
		controller.registerMVCComponent(connector);
		controller.sendMessage(REQUEST_IS_LOGGED);
		
		if(flag_location)
		{
			flag_location = false;
			controller.sendMessage(REQUEST_RESET_LOCATION_PROVIDERS);
			
		}
		
		controller.sendMessage(REQUEST_LAST_LOCATION);
	}
	

	public void onStop()
	{
		super.onStop();
		controller.unregisterMVCComponent(connector);
	}
	

	@Override
	public void onClick(View v)
	{
		Intent i;
		// TODO Auto-generated method stub
		switch (v.getId())
		{
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
			case R.id.timelinebutton:
				i = new Intent(getApplicationContext(), TimelineActivity.class);
				startActivity(i);
				break;
		}
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
				controller.sendMessage(REQUEST_UPDATE);
				break;
			}
				
			case (R.id.menu_item_exit):
			{
				System.exit(0);
				break;
			}
			case (R.id.main_menu_login):
			{
				Intent i = new Intent(getApplicationContext(), LoginActivity.class);
				startActivity(i);
				break;
			}
		
		}
		return true;
	}
}
