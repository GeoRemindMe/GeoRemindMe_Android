package org.georemindme.community.view;


import static org.georemindme.community.controller.ControllerProtocol.RESPONSE_ALERT_CHANGED;
import static org.georemindme.community.controller.ControllerProtocol.RESPONSE_ALERT_DELETED;
import static org.georemindme.community.controller.ControllerProtocol.RESPONSE_ALL_UNDONE_ALERTS;
import static org.georemindme.community.controller.ControllerProtocol.RESPONSE_LAST_LOCATION;
import static org.georemindme.community.controller.ControllerProtocol.REQUEST_ALL_UNDONE_ALERTS;
import static org.georemindme.community.controller.ControllerProtocol.REQUEST_DELETE_ALERT;
import static org.georemindme.community.controller.ControllerProtocol.REQUEST_LAST_LOCATION;

import org.georemindme.community.R;
import org.georemindme.community.controller.Controller;
import org.georemindme.community.model.Alert;
import org.georemindme.community.model.Database;
import org.georemindme.community.view.adapters.AlertAdapter;

import com.franciscojavierfernandez.android.libraries.mvcframework.view.MVCViewComponent;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;


public class UndoneAlertList extends ListActivity implements
		OnItemClickListener
{
	
	private Cursor				c			= null;
	private ListView			list;
	
	private MVCViewComponent	connector	= null;
	private Controller			controller	= null;
	
	private Location			location	= null;
	
	private AlertAdapter		adapter		= null;
	
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Log.i("UAL", "onCreate()");
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
					case RESPONSE_ALL_UNDONE_ALERTS:
						if (c != null)
							c.close();
						c = (Cursor) msg.obj;
						processData();
						return true;
					case RESPONSE_LAST_LOCATION:
						location = (Location) msg.obj;
						processData();
						return true;
					case RESPONSE_ALERT_DELETED:
					case RESPONSE_ALERT_CHANGED:
						controller.sendMessage(REQUEST_ALL_UNDONE_ALERTS);
						return true;
				}
				return false;
			}
		};
		
		registerForContextMenu(list);
		
		controller.removeAllNotification();
	}
	

	public void onResume()
	{
		super.onResume();
		Log.i("UAL", "onResume()");
		controller.registerMVCComponent(connector);
		
		controller.sendMessage(REQUEST_ALL_UNDONE_ALERTS).sendMessage(REQUEST_LAST_LOCATION);
		
	}
	

	public void onPause()
	{
		super.onPause();
		Log.i("UAL", "onPause()");
		controller.unregisterMVCComponent(connector);
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
		
		launchActivityToEditAlert(alert);
		
	}
	
	private void launchActivityToEditAlert(Alert a)
	{
		Intent i = new Intent(UndoneAlertList.this, AddAlarmActivity.class);
		Bundle extras = new Bundle();
		extras.putSerializable("ALERT", a);
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
			alertSelected.setAddress(c.getString(c.getColumnIndex(Database.ALERT_ADDRESS)));
			
			return alertSelected;
		}
		
		return null;
	}


	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo)
	{
		if (v.equals(list))
		{
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.alert_context_menu, menu);
		}
	}
	

	public boolean onContextItemSelected(MenuItem item)
	{
		AdapterContextMenuInfo adaptercontextmenu = (AdapterContextMenuInfo) item.getMenuInfo();
		Alert a = null;
		switch (item.getItemId())
		{
			case R.id.menu_item_delete_alert:
				a = convertCursorPositionToAlert(c, adaptercontextmenu.position);
				controller.sendMessage(REQUEST_DELETE_ALERT, a);
				break;
			
			case R.id.menu_item_view_edit_alert:
				a = convertCursorPositionToAlert(c, adaptercontextmenu.position);
				launchActivityToEditAlert(a);
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
