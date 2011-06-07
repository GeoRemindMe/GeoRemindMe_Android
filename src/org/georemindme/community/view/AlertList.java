package org.georemindme.community.view;


import java.util.ArrayList;
import java.util.List;

import org.georemindme.community.R;
import org.georemindme.community.controller.GeoRemindMe;
import org.georemindme.community.model.Database;
import org.georemindme.community.view.adapters.AlertAdapter;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;


public class AlertList extends ListActivity implements OnItemClickListener
{
	private Cursor				c					= null;
	private ListView			list;
	
	private boolean				processing_subset	= false;
	private boolean				refreshAvailable	= true;
	private Bundle				data;
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.tasklist);
		
		list = (ListView) findViewById(android.R.id.list);
		list.setOnItemClickListener(this);
		
		data = getIntent().getExtras();
		if (data != null)
		{
			processing_subset = true;
		}
		else
		{
			
			processing_subset = false;
		}
	}
	

	public void onResume()
	{
		super.onResume();
		
		refresh();
	}
	

	public void onStop()
	{
		super.onStop();
		
	}
	

	public void onDestroy()
	{
		super.onDestroy();
	}
	

	@Override
	public void onItemClick(AdapterView<?> list, View v, int position, long id)
	{
		// TODO Auto-generated method stub
		Log.v("click on", "position: " + position);
	}
	

	private void refresh()
	{
		
	}
}
