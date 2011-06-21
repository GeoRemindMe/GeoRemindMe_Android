package org.georemindme.community.view;


import java.security.acl.LastOwnerException;
import java.util.ArrayList;
import java.util.List;

import org.georemindme.community.R;
import org.georemindme.community.controller.Controller;
import org.georemindme.community.controller.GeoRemindMe;
import org.georemindme.community.model.Database;
import org.georemindme.community.view.adapters.AlertAdapter;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ProgressBar;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import static org.georemindme.community.controller.ControllerProtocol.*;


public class UndoneAlertList extends ListActivity implements
		OnItemClickListener, Callback
{
	
	private Cursor			c			= null;
	private ListView		list;
	private Bundle			data;
	
	private Handler			controllerInbox;
	private Handler			ownInbox;
	private Controller		controller;
	
	private Location		location	= null;
	
	private AlertAdapter	adapter		= null;
	
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tasklist);
		
		list = (ListView) findViewById(android.R.id.list);
		list.setOnItemClickListener(this);
		
		controller = Controller.getInstace(getApplicationContext());
		controllerInbox = controller.getInboxHandler();
		ownInbox = new Handler(this);
		
	}
	

	public void onResume()
	{
		super.onResume();
		controller.removeOutboxHandler(ownInbox);
		controller.addOutboxHandler(ownInbox);
		
		controllerInbox.obtainMessage(V_REQUEST_ALL_UNDONE_ALERTS).sendToTarget();
		if (location == null)
			controllerInbox.obtainMessage(V_REQUEST_LAST_LOCATION).sendToTarget();
	}
	

	public void onPause()
	{
		super.onPause();
		controller.removeOutboxHandler(ownInbox);
	}
	

	public void onDestroy()
	{
		if (c != null)
			c.close();
		super.onDestroy();
	}
	

	@Override
	public void onItemClick(AdapterView<?> list, View v, int position, long id)
	{
		// TODO Auto-generated method stub
		Log.v("click on", "position: " + position);
	}
	

	@Override
	public boolean handleMessage(Message msg)
	{
		// TODO Auto-generated method stub
		switch (msg.what)
		{
			case C_ALL_UNDONE_ALERTS:
				c = (Cursor) msg.obj;
				processData();
				return true;
			case C_LAST_LOCATION:
				location = (Location) msg.obj;
				processData();
				return true;
			case C_ALERT_CHANGED:
				//controllerInbox.obtainMessage(V_REQUEST_ALL_UNDONE_ALERTS).sendToTarget();
				return true;
		}
		return false;
	}
	

	private void processData()
	{
		// startManagingCursor(c);
		
		if (adapter != null)
		{
			Log.w("Undone alert list", "Notifiy data set changed gets called");
			adapter.notifyDataSetChanged();
		}
		
		String from[] = { Database.ALERT_NAME, Database.ALERT_DESCRIPTION };
		int to[] = { R.id.alert_name, R.id.alert_description };
		
		adapter = new AlertAdapter(this, R.layout.alert_list_item, c, from, to, controller, location);
		setListAdapter(adapter);
		//list.invalidate();
		
		
	}
}
