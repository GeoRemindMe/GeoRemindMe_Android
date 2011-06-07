package org.georemindme.community.view;


import java.io.IOException;
import java.sql.Date;
import java.util.List;

import org.georemindme.community.R;
import org.georemindme.community.controller.GeoRemindMe;
import org.georemindme.community.model.Alert;
import org.georemindme.community.model.Database;

import android.app.Activity;
import android.app.ProgressDialog;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;


public class ViewAlert extends Activity implements OnClickListener
{
	private ProgressDialog	pd;
	private String			address	= null;
	
	private EditText		etAddress;
	private EditText		etName;
	private EditText		etDescription;
	private CheckBox		cbDone;
	
	private long			serverID;
	
	
	public void onCreate(Bundle bundle)
	{
		super.onCreate(bundle);
		setContentView(R.layout.viewtask);
		
		etAddress = (EditText) findViewById(R.id.edittext_address);
		etAddress.setText("No address available.");
		
		etName = (EditText) findViewById(R.id.edittext_name);
		etDescription = (EditText) findViewById(R.id.edittext_description);
		cbDone = (CheckBox) findViewById(R.id.checkbox_done);
		
		Bundle b = getIntent().getExtras();
		Log.v("El intent que viene es: ", getIntent().getExtras().toString());
		Log.v("ViewAlert", "Bundle not null");
		
		if (b != null)
		{
			Alert a = (Alert) b.get("alert");
			serverID = b.getLong("serverID");
			
			etName.setText(a.getName());
			etDescription.setText(a.getDescription());
			cbDone.setChecked(a.isDone());
			cbDone.setOnClickListener(this);
			
			long startTime = a.getStarts();
			Date startDate = new Date(startTime);
			Log.v("Start", startDate.toString());
			
			Log.v("ViewAlert", "" + a.getLatitude() + " " + a.getLongitude());
			new AddressConverter().execute(a.getLatitude(), a.getLongitude());
			
			getIntent().getExtras().clear();
		}
		
	}
	/*
	public void onBackPressed()
	{
		this.finish();
		super.onBackPressed();
		
	}
	*/
	private class AddressConverter extends AsyncTask<Double, Void, Void>
	{
		
		protected void onPreExecute()
		{
			pd = ProgressDialog.show(ViewAlert.this, "Getting Address...", "", true, false);
		}
		

		protected void onPostExecute(final Void unused)
		{
			pd.dismiss();
			if (address != null)
			{
				etAddress.setText(address);
			}
		}
		

		@Override
		protected Void doInBackground(Double... params)
		{
			// TODO Auto-generated method stub
			double lat = params[0];
			double lng = params[1];
			
			address = null;
			List<Address> addresses = null;
			try
			{
				Geocoder gC = new Geocoder(getApplicationContext());
				addresses = gC.getFromLocation(lat, lng, 1);
				
				if (addresses != null)
				{
					Address currentAddr = addresses.get(0);
					StringBuilder mSB = new StringBuilder();
					for (int i = 0; i < currentAddr.getMaxAddressLineIndex(); i++)
					{
						mSB.append(currentAddr.getAddressLine(i));
					}
					
					address = mSB.toString();
				}
			}
			catch (IOException e)
			{
				
			}
			return null;
		}
		
	}
	
	
	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		
	}
}
