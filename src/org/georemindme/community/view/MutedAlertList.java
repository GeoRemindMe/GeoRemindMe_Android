package org.georemindme.community.view;


import static org.georemindme.community.controller.ControllerProtocol.*;

import org.georemindme.community.R;
import org.georemindme.community.controller.Controller;
import org.georemindme.community.model.Database;
import org.georemindme.community.view.adapters.AlertAdapter;

import com.franciscojavierfernandez.android.libraries.mvcframework.view.MVCViewComponent;

import android.app.ListActivity;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Handler.Callback;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;


public class MutedAlertList extends ListActivity implements
		OnItemClickListener
{
	private Cursor				c			= null;
	private ListView			list;
	private Bundle				data;
	
	private MVCViewComponent	connector	= null;
	private Controller			controller	= null;
	
	private Location			location	= null;
	
	private AlertAdapter		adapter		= null;
	
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tasklist);
		
		list = (ListView) findViewById(android.R.id.list);
		list.setOnItemClickListener(this);
		
		controller = Controller.getInstace(getApplicationContext());
		connector = new MVCViewComponent(controller)
		{
			
			@Override
			public boolean handleMessage(Message msg)
			{
				// TODO Auto-generated method stub
				switch (msg.what)
				{
					case RESPONSE_ALL_MUTED_ALERTS:
						c = (Cursor) msg.obj;
						processData();
						return true;
					case RESPONSE_LAST_LOCATION:
						location = (Location) msg.obj;
						processData();
						return true;
					case RESPONSE_ALERT_CHANGED:
						// controllerInbox.obtainMessage(V_REQUEST_ALL_DONE_ALERTS).sendToTarget();
						return true;
				}
				return false;
			}
		};
	}
	

	public void onResume()
	{
		super.onResume();
		controller.registerMVCComponent(connector);
		
		controller.sendMessage(REQUEST_ALL_MUTED_ALERTS);
		if (location == null)
			controller.sendMessage(REQUEST_LAST_LOCATION);
	}
	

	public void onStop()
	{
		super.onStop();
		controller.unregisterMVCComponent(connector);
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
	}


	private void processData()
	{
		// startManagingCursor(c);
		
		if (adapter != null)
		{
			adapter.notifyDataSetChanged();
		}
		
		String from[] = { Database.ALERT_NAME, Database.ALERT_DESCRIPTION };
		int to[] = { R.id.alert_name, R.id.alert_description };
		
		adapter = new AlertAdapter(this, R.layout.alert_list_item, c, from, to, controller, location);
		setListAdapter(adapter);
		// list.invalidate();
		
	}
}
