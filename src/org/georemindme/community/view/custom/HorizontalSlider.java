package org.georemindme.community.view.custom;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;


public class HorizontalSlider extends Preference implements
		OnSeekBarChangeListener
{
	
	public static int	maximum		= 21;
	public static int	interval	= 1;
	
	private float		oldValue	= 1;
	private TextView	monitorBox;
	
	
	public HorizontalSlider(Context context)
	{
		super(context);
	}
	

	public HorizontalSlider(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}
	

	public HorizontalSlider(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}
	

	@Override
	protected View onCreateView(ViewGroup parent)
	{
		
		LinearLayout mainLayout = new LinearLayout(getContext());
		LinearLayout barLayout = new LinearLayout(getContext());
		
		LinearLayout.LayoutParams zoom_text_layout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 
				LinearLayout.LayoutParams.WRAP_CONTENT);
		zoom_text_layout.gravity = Gravity.LEFT;
		zoom_text_layout.weight = 1.0f;
		
		LinearLayout.LayoutParams zoom_bar_layout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 
				LinearLayout.LayoutParams.WRAP_CONTENT);
		zoom_bar_layout.gravity = Gravity.CENTER;
		zoom_bar_layout.weight = 1.0f;
		zoom_bar_layout.setMargins(0, 15, 15, 15);
		
		LinearLayout.LayoutParams zoom_value_layout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 
				LinearLayout.LayoutParams.WRAP_CONTENT);
		zoom_value_layout.gravity = Gravity.CENTER;
		zoom_value_layout.setMargins(15, 15, 15, 15);
		
		mainLayout.setPadding(15, 5, 10, 5);
		mainLayout.setOrientation(LinearLayout.VERTICAL);
		
		//barLayout.setPadding(5, 5, 5, 5);
		barLayout.setOrientation(LinearLayout.HORIZONTAL);
		
		TextView view = new TextView(getContext());
		view.setText(getTitle());
		
		view.setTextSize(21);
		view.setTextColor(Color.WHITE);
		
		view.setGravity(Gravity.LEFT);
		view.setLayoutParams(zoom_text_layout);
		
		SeekBar bar = new SeekBar(getContext());
		bar.setMax(maximum);
		bar.setProgress((int) this.oldValue);
		bar.setLayoutParams(zoom_bar_layout);
		bar.setOnSeekBarChangeListener(this);
		
		this.monitorBox = new TextView(getContext());
		this.monitorBox.setTextSize(14);
		this.monitorBox.setTextColor(Color.WHITE);
		//this.monitorBox.setTypeface(Typeface.MONOSPACE, Typeface.ITALIC);
		this.monitorBox.setLayoutParams(zoom_value_layout);
		//this.monitorBox.setPadding(2, 5, 0, 0);
		this.monitorBox.setText(bar.getProgress() + "");
		
		mainLayout.addView(view);
		
		barLayout.addView(bar);
		barLayout.addView(this.monitorBox);
		/*
		mainLayout.addView(bar);
		mainLayout.addView(this.monitorBox);
		*/
		mainLayout.addView(barLayout);
		mainLayout.setId(android.R.id.widget_frame);
		
		return mainLayout;
	}
	

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser)
	{
		
		progress = Math.round(((float) progress) / interval) * interval;
		
		if (!callChangeListener(progress))
		{
			seekBar.setProgress((int) this.oldValue);
			return;
		}
		
		seekBar.setProgress(progress);
		this.oldValue = progress;
		this.monitorBox.setText(progress + "");
		updatePreference(progress);
		
		notifyChanged();
	}
	

	@Override
	public void onStartTrackingTouch(SeekBar seekBar)
	{
	}
	

	@Override
	public void onStopTrackingTouch(SeekBar seekBar)
	{
	}
	

	@Override
	protected Object onGetDefaultValue(TypedArray ta, int index)
	{
		
		int dValue = (int) ta.getInt(index, 50);
		
		return validateValue(dValue);
	}
	

	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue)
	{
		
		int temp = restoreValue ? getPersistedInt(50) : (Integer) defaultValue;
		
		if (!restoreValue)
			persistInt(temp);
		
		this.oldValue = temp;
	}
	

	private int validateValue(int value)
	{
		
		if (value > maximum)
			value = maximum;
		else if (value < 0)
			value = 0;
		else if (value % interval != 0)
			value = Math.round(((float) value) / interval) * interval;
		
		return value;
	}
	

	private void updatePreference(int newValue)
	{
		
		SharedPreferences.Editor editor = getEditor();
		editor.putInt(getKey(), newValue);
		editor.commit();
	}
	
}