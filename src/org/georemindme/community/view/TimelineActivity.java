package org.georemindme.community.view;


import java.util.ArrayList;

import org.georemindme.community.R;
import org.georemindme.community.controller.Controller;
import org.georemindme.community.model.Timeline;
import org.georemindme.community.model.TimelinePage;
import org.georemindme.community.view.adapters.TimelineAdapter;

import android.app.ListActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.widget.ListView;

import static org.georemindme.community.controller.ControllerProtocol.*;

public class TimelineActivity extends ListActivity implements Callback
{
	private Controller		controller;
	private Handler			controllerInbox;
	private Handler			myInbox;
	
	private Timeline		timeline	= null;
	private TimelineAdapter	adapter		= null;
	
	private ListView list;
	
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.timelineactivity);
		
		controller = Controller.getInstace(getApplicationContext());
		controllerInbox = controller.getInboxHandler();
		myInbox = new Handler(this);
		
		list = (ListView) findViewById(android.R.id.list);
	}
	

	public void onResume()
	{
		super.onResume();
		
		controller.addOutboxHandler(myInbox);
		
		controllerInbox.obtainMessage(V_REQUEST_NEXT_TIMELINE_PAGE).sendToTarget();
	}
	

	public void onPause()
	{
		super.onPause();
		
		controller.removeOutboxHandler(myInbox);
	}
	

	public void onStop()
	{
		super.onStop();
	}
	

	@Override
	public boolean handleMessage(Message msg)
	{
		// TODO Auto-generated method stub
		switch (msg.what)
		{
			case S_REQUEST_NEXT_TIMELINE_PAGE_FINISHED:
				TimelinePage page = (TimelinePage) msg.obj;
				timeline = new Timeline(page);
				processData();
				return true;
		}
		return false;
	}
	

	private void processData()
	{
		adapter = new TimelineAdapter(this, R.layout.timeline_list_item, timeline.getActualTimelinePage());
		setListAdapter(adapter);
		adapter.notifyDataSetChanged();
		list.invalidate();
	}
}
