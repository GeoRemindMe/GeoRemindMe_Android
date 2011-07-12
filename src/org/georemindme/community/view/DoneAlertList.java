package org.georemindme.community.view;


import static org.georemindme.community.controller.ControllerProtocol.C_ALERT_CHANGED;
import static org.georemindme.community.controller.ControllerProtocol.C_ALL_DONE_ALERTS;
import static org.georemindme.community.controller.ControllerProtocol.C_LAST_LOCATION;
import static org.georemindme.community.controller.ControllerProtocol.V_REQUEST_ALL_DONE_ALERTS;
import static org.georemindme.community.controller.ControllerProtocol.V_REQUEST_LAST_LOCATION;

import org.georemindme.community.R;
import org.georemindme.community.controller.Controller;
import org.georemindme.community.model.Database;
import org.georemindme.community.mvcandroidframework.view.MVCViewComponent;
import org.georemindme.community.view.adapters.AlertAdapter;

import android.app.ListActivity;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;


public class DoneAlertList extends ListActivity implements OnItemClickListener
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
					case C_ALL_DONE_ALERTS:
						c = (Cursor) msg.obj;
						processData();
						return true;
					case C_LAST_LOCATION:
						location = (Location) msg.obj;
						processData();
						return true;
					case C_ALERT_CHANGED:
						controller.sendMessage(V_REQUEST_ALL_DONE_ALERTS);
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
		
		controller.sendMessage(V_REQUEST_ALL_DONE_ALERTS);
		if (location == null)
			controller.sendMessage(V_REQUEST_LAST_LOCATION);
	}
	

	public void onPause()
	{
		super.onPause();
		
		if (c != null)
			c.close();
		
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
		list.invalidate();
		
	}
}
