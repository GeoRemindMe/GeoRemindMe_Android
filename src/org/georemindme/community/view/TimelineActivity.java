package org.georemindme.community.view;


import static org.georemindme.community.controller.ControllerProtocol.RESPONSE_NEXT_TIMELINE_PAGE_FINISHED;
import static org.georemindme.community.controller.ControllerProtocol.REQUEST_NEXT_TIMELINE_PAGE;

import org.georemindme.community.R;
import org.georemindme.community.controller.Controller;
import org.georemindme.community.model.Timeline;
import org.georemindme.community.model.TimelinePage;
import org.georemindme.community.view.adapters.TimelineAdapter;

import com.franciscojavierfernandez.android.libraries.mvcframework.view.MVCViewComponent;

import android.app.ListActivity;
import android.os.Bundle;
import android.os.Message;
import android.widget.ListView;


public class TimelineActivity extends ListActivity
{
	private Controller			controller	= null;
	private MVCViewComponent	connector	= null;
	
	private Timeline			timeline	= null;
	private TimelineAdapter		adapter		= null;
	
	private ListView			list;
	
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.timelineactivity);
		
		controller = Controller.getInstace(getApplicationContext());
		connector = new MVCViewComponent(controller)
		{
			
			@Override
			public boolean handleMessage(Message msg)
			{
				// TODO Auto-generated method stub
				switch (msg.what)
				{
					case RESPONSE_NEXT_TIMELINE_PAGE_FINISHED:
						TimelinePage page = (TimelinePage) msg.obj;
						timeline = new Timeline(page);
						processData();
						controller.sendMessage(REQUEST_NEXT_TIMELINE_PAGE);
						return true;
				}
				return false;
			}
		};
		
		list = (ListView) findViewById(android.R.id.list);
	}
	

	public void onResume()
	{
		super.onResume();
		
		controller.registerMVCComponent(connector);
		controller.sendMessage(REQUEST_NEXT_TIMELINE_PAGE);
	}
	

	public void onPause()
	{
		super.onPause();
		controller.unregisterMVCComponent(connector);
	}
	

	public void onStop()
	{
		super.onStop();
	}
	
	
	private void processData()
	{
		adapter = new TimelineAdapter(this, R.layout.timeline_list_item, timeline.getActualTimelinePage());
		setListAdapter(adapter);
		adapter.notifyDataSetChanged();
		list.invalidate();
	}
}
