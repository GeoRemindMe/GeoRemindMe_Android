package org.georemindme.community.view.custom;


import org.georemindme.community.R;
import org.georemindme.community.model.Time;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.TimePicker;


public class PickTimeDateDialog extends Dialog implements
		android.view.View.OnClickListener
{
	public static final int			IS_START		= 1;
	public static final int			IS_END			= 2;
	
	private static String		IS_START_TEXT;
	private static String		IS_END_TEXT;
	
	private Context					context;
	
	private DatePicker				datepicker;
	private TimePicker				timepicker;
	private CheckBox				isForeverCheckBox;
	private Button					okButton;
	
	private int						mode;
	private boolean					checked;
	private Time					data;
	
	private PickTimeDateSetListener	listener;
	
	
	public PickTimeDateDialog(Context context, int mode, boolean checked,
			Time data, PickTimeDateSetListener listener)
	{
		super(context);
		// TODO Auto-generated constructor stub
		this.context = context;
		this.listener = listener;
		
		this.mode = mode;
		this.data = data;
		this.checked = checked;
		
		IS_START_TEXT = context.getString(R.string.start_time_undefined);
		IS_END_TEXT = context.getString(R.string.end_time_undefined);
	}
	

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pickdayandtimedialog);
		
		setTitle(context.getString(R.string.day_and_time));
		
		datepicker = (DatePicker) findViewById(R.id.datepicker);
		timepicker = (TimePicker) findViewById(R.id.timepicker);
		isForeverCheckBox = (CheckBox) findViewById(R.id.foreverCheckBox);
		okButton = (Button) findViewById(R.id.okButtonDialogTime);
		
		okButton.setOnClickListener(this);
		isForeverCheckBox.setOnClickListener(this);
		
		if (mode == IS_START)
			isForeverCheckBox.setText(IS_START_TEXT);
		else
			isForeverCheckBox.setText(IS_END_TEXT);
	}
	

	public void onStart()
	{
		super.onStart();
		
		if (data != null)
		{
			if (!data.isUndefined())
			{
				datepicker.init(data.getYear(), data.getMonth() - 1, data.getDayOfMonth(), null);
				timepicker.setCurrentHour(data.getHour());
				timepicker.setCurrentMinute(data.getMinute());
			}
		}
		isForeverCheckBox.setChecked(checked);
		if (isForeverCheckBox.isChecked())
		{
			setEnabledComponents(false);
		}
		
	}
	

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		if (v.equals(okButton))
		{
			Time t = null;
			if (isForeverCheckBox.isChecked())
			{
				t = new Time();
			}
			else
			{
				t = new Time(datepicker.getYear(), datepicker.getMonth(), datepicker.getDayOfMonth(), timepicker.getCurrentHour(), timepicker.getCurrentMinute());
				
			}
			
			listener.pickTimerDateSet(t);
			dismiss();
			try
			{
				finalize();
			}
			catch (Throwable e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if (v.equals(isForeverCheckBox))
		{
			setEnabledComponents(!isForeverCheckBox.isChecked());
			checked = isForeverCheckBox.isChecked();
		}
	}
	

	private void setEnabledComponents(boolean flag)
	{
		datepicker.setEnabled(flag);
		timepicker.setEnabled(flag);
	}
	
	public interface PickTimeDateSetListener
	{
		public void pickTimerDateSet(Time time);
	}
}
