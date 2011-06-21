package org.georemindme.community.view.adapters;


import java.text.DecimalFormat;

import org.georemindme.community.R;
import org.georemindme.community.controller.Controller;
import org.georemindme.community.model.Database;
import org.georemindme.community.view.custom.SoundButton;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.location.Location;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;

import static org.georemindme.community.controller.ControllerProtocol.*;


public class AlertAdapter extends SimpleCursorAdapter
{
	private Cursor			c;
	private Context			context;
	//private long			serverID;
	
	private CheckBox		cbDone;
	private ToggleButton		soundButton;
	private Controller		controller;
	private Location		actualLocation;
	
	private DecimalFormat	decimalFormat;
	
	
	public AlertAdapter(Context context, int layout, Cursor c, String[] from,
			int[] to, Controller controller, Location actualLocation)
	{
		super(context, layout, c, from, to);
		// TODO Auto-generated constructor stub
		
		this.controller = controller;
		this.actualLocation = actualLocation;
		this.c = c;
		this.context = context;
		
		decimalFormat = new DecimalFormat("0.00");
	}
	

	public View getView(int pos, View inView, ViewGroup parentGroup)
	{
		View v = inView;
		
		if (v == null)
		{
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.alert_list_item, null);
		}
		
		if (c.moveToPosition(pos))
		{
			String name = c.getString(c.getColumnIndex(Database.ALERT_NAME));
			String description = c.getString(c.getColumnIndex(Database.ALERT_DESCRIPTION));
			final int id = c.getInt(c.getColumnIndex(Database._ID));
			TextView tvName = (TextView) v.findViewById(R.id.alert_name);
			TextView tvDescription = (TextView) v.findViewById(R.id.alert_description);
			
			final long serverID = c.getLong(c.getColumnIndex(Database.SERVER_ID));
			
			
			
			
			soundButton = (ToggleButton) v.findViewById(R.id.alert_list_item_soundButton);
			soundButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
			{
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
				{
					// TODO Auto-generated method stub
					Log.w("SoundButton", soundButton.isChecked() + "");
					Object[] data = new Object[2];
					data[0] = new Boolean(soundButton.isChecked());
					data[1] = new Integer(id);
					controller.getInboxHandler().obtainMessage(V_REQUEST_CHANGE_ALERT_ACTIVE, data).sendToTarget();

				}
			});
			int active = c.getInt(c.getColumnIndex(Database.ALERT_ACTIVE));
			if(active == 0)
			{
				//No est‡ activa.
				soundButton.setChecked(false);
			}
			else
			{
				soundButton.setChecked(true);
			}
			
			
					
			
			
			
			
			cbDone = (CheckBox) v.findViewById(R.id.alert_done);
			cbDone.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
			{
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
				{
					// TODO Auto-generated method stub
					Object[] data = new Object[2];
					data[0] = new Boolean(isChecked);
					data[1] = new Integer(id);
					Log.w("El checkbox est‡: ", isChecked + "");
					controller.getInboxHandler().obtainMessage(V_REQUEST_CHANGE_ALERT_DONE, data).sendToTarget();
				}
			});
			int done = c.getInt(c.getColumnIndex(Database.ALERT_DONE));
			if (done == 0)
			{
				// No est‡ hecha.
				/* Calculo la distancia a la que est‡ */
				if (actualLocation != null)
				{
					double lat = c.getDouble(c.getColumnIndex(Database.POINT_X));
					double lng = c.getDouble(c.getColumnIndex(Database.POINT_Y));
					
					Location l = new Location("unknown provider");
					l.setLatitude(lat);
					l.setLongitude(lng);
					
					double distance = l.distanceTo(actualLocation);
					
					if (distance > 1000)
					{
						tvDescription.setText((decimalFormat.format(distance / 1000))
								+ " kms aprox.");
					}
					else if (distance < 10)
					{
						tvDescription.setText("It's here!");
					}
					else
					{
						tvDescription.setText((decimalFormat.format(distance))
								+ " mts aprox.");
					}
					
				}
				else
				{
					tvDescription.setText("Unknown aprox. distance");
				}
				cbDone.setChecked(false);
			}
			else
			{
				cbDone.setChecked(true);
				tvDescription.setVisibility(View.GONE);
			}
			
			
			tvName.setText(name);
		}
		
		return v;
	}
	
}
