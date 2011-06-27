package org.georemindme.community.view.custom;


import org.georemindme.community.controller.PreferencesController;
import org.georemindme.community.view.AddAlarmActivity;
import org.georemindme.community.view.MapDialogActivity;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.View;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;

import com.google.android.maps.MapView;


public class DummyMap extends MapView
{
	private Context			context;
	private GestureDetector	gestureDetector;
	
	private AddAlarmActivity parentActivity;
	
	public DummyMap(Context context, String apiKey)
	{
		super(context, apiKey);
		// TODO Auto-generated constructor stub
		_init(context);
	}
	

	public DummyMap(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		_init(context);
	}
	

	public DummyMap(Context arg0, AttributeSet arg1)
	{
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
		_init(context);
	}
	
	public void addParentActivity(AddAlarmActivity activity)
	{
		parentActivity = activity;
	}
	
	private void _init(final Context context)
	{
		this.context = context;
		this.setTraffic(PreferencesController.isTraffic());
		this.setSatellite(PreferencesController.isSatellite());
		
		gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener()
		{
			public void onLongPress(MotionEvent e)
			{
			}
			
			public boolean onDoubleTap(MotionEvent e)
			{
				return super.onDoubleTap(e);
			}
			
			public boolean onSingleTapUp(MotionEvent e)
			{
				parentActivity.launchMapActivity();
				return super.onDown(e);
			}
		}
		);
		gestureDetector.setIsLongpressEnabled(true);
		
		this.setOnTouchListener(new View.OnTouchListener()
		{
			
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				// TODO Auto-generated method stub
				gestureDetector.onTouchEvent(event);
				return true;
			}
		});
	}

}
