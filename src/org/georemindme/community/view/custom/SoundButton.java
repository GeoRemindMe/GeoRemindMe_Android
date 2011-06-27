package org.georemindme.community.view.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageButton;

public class SoundButton extends ImageButton
{
	private boolean on;
	
	public SoundButton(Context context)
	{
		super(context);
		// TODO Auto-generated constructor stub
		init();
	}

	public SoundButton(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init();
	}

	public SoundButton(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		init();
	}
	
	private void init()
	{
		setOn();
	}
	
	public boolean isOn()
	{
		return on;
	}
	
	public boolean setOn()
	{
		on = true;
		return on;
	}
	
	public boolean setOff()
	{
		on = false;
		return on;
	}
}
