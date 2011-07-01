package org.georemindme.community.view;


import java.security.acl.LastOwnerException;
import java.util.ArrayList;
import java.util.List;

import org.georemindme.community.R;
import org.georemindme.community.controller.Controller;
import org.georemindme.community.controller.GeoRemindMe;
import org.georemindme.community.model.Alert;
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
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.CursorAdapter;
import android.widget.ProgressBar;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

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
		Log.i("UAL", "onCreate()");
		setContentView(R.layout.tasklist);
		
		list = (ListView) findViewById(android.R.id.list);
		list.setOnItemClickListener(this);
		
		controller = Controller.getInstace(getApplicationContext());
		controllerInbox = controller.getInboxHandler();
		ownInbox = new Handler(this);
		
		registerForContextMenu(list);
		
	}
	

	public void onResume()
	{
		super.onResume();
		Log.i("UAL", "onResume()");
		controller.removeOutboxHandler(ownInbox);
		controller.addOutboxHandler(ownInbox);
		
		controllerInbox.obtainMessage(V_REQUEST_ALL_UNDONE_ALERTS).sendToTarget();
		controllerInbox.obtainMessage(V_REQUEST_LAST_LOCATION).sendToTarget();
		
	}
	

	public void onPause()
	{
		super.onPause();
		Log.i("UAL", "onPause()");
		controller.removeOutboxHandler(ownInbox);
		
		if(c != null)
			c.close();
	}
	

	public void onDestroy()
	{
		if (c != null)
			c.close();
		Log.i("UAL", "onDestroy()");
		super.onDestroy();
	}
	

	@Override
	public void onItemClick(AdapterView<?> list, View v, int position, long id)
	{
		// TODO Auto-generated method stub
		
		// Para hacer que este método funcionase en el CheckBox y en el
		// ToggleButton he tenido que poner
		// que no puedan ser focusables para evitar el bug que hay conocido en
		// la plataforma.
		
		Alert alert = convertCursorPositionToAlert(this.c, position);
		
		Intent i = new Intent(UndoneAlertList.this, AddAlarmActivity.class);
		Bundle extras = new Bundle();
		extras.putSerializable("ALERT", alert);
		i.putExtras(extras);
		startActivity(i);
		
	}
	

	private Alert convertCursorPositionToAlert(Cursor c, int position)
	{
		if (c != null)
		{
			c.moveToPosition(position);
			Alert alertSelected = new Alert();
			int active = c.getInt(c.getColumnIndex(Database.ALERT_ACTIVE));
			if (active == 0)
				alertSelected.setActive(false);
			else
				alertSelected.setActive(true);
			alertSelected.setCreated(c.getLong(c.getColumnIndex(Database.ALERT_CREATE)));
			alertSelected.setDescription(c.getString(c.getColumnIndex(Database.ALERT_DESCRIPTION)));
			long done_when = c.getLong(c.getColumnIndex(Database.ALERT_DONE));
			alertSelected.setDone_when(done_when);
			if (done_when != 0)
				alertSelected.setDone(true);
			else
				alertSelected.setDone(false);
			alertSelected.setEnds(c.getLong(c.getColumnIndex(Database.ALERT_END)));
			alertSelected.setId(c.getLong(c.getColumnIndex(Database._ID)));
			alertSelected.setIdServer(c.getLong(c.getColumnIndex(Database.SERVER_ID)));
			alertSelected.setLatitude(c.getDouble(c.getColumnIndex(Database.LATITUDE)));
			alertSelected.setLongitude(c.getDouble(c.getColumnIndex(Database.LONGITUDE)));
			alertSelected.setModified(c.getLong(c.getColumnIndex(Database.ALERT_MODIFY)));
			alertSelected.setName(c.getString(c.getColumnIndex(Database.ALERT_NAME)));
			alertSelected.setStarts(c.getLong(c.getColumnIndex(Database.ALERT_START)));
			
			return alertSelected;
		}
		
		return null;
	}
	

	@Override
	public boolean handleMessage(Message msg)
	{
		// TODO Auto-generated method stub
		switch (msg.what)
		{
			case C_ALL_UNDONE_ALERTS:
				if(c != null)
					c.close();
				c = (Cursor) msg.obj;
				processData();
				return true;
			case C_LAST_LOCATION:
				location = (Location) msg.obj;
				processData();
				return true;
			case C_ALERT_DELETED:
			case C_ALERT_CHANGED:
				controllerInbox.obtainMessage(V_REQUEST_ALL_UNDONE_ALERTS).sendToTarget();
				return true;
		}
		return false;
	}
	
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{
		if(v.equals(list))
		{
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.alert_context_menu, menu);
		}
	}

	public boolean onContextItemSelected(MenuItem item)
	{
		AdapterContextMenuInfo adaptercontextmenu = (AdapterContextMenuInfo) item.getMenuInfo();
		
		switch(item.getItemId())
		{
			case R.id.menu_item_delete_alert:
				Alert a = convertCursorPositionToAlert(c, adaptercontextmenu.position);
				controllerInbox.obtainMessage(V_REQUEST_DELETE_ALERT, a).sendToTarget();
				break;
				
			case R.id.menu_item_view_edit_alert:
				
				break;
		}
		
		
		return true;
	}
	
	private void processData()
	{
		if (adapter != null)
		{
			adapter.notifyDataSetChanged();
		}
		
		String from[] = { Database.ALERT_NAME, Database.ALERT_DESCRIPTION };
		int to[] = { R.id.alert_name, R.id.alert_description };
		
		adapter = new AlertAdapter(this, R.layout.alert_list_item, c, from, to, controller, location);
		setListAdapter(adapter);
	}
}
