package org.georemindme.community.view;


import org.georemindme.community.R;
import org.georemindme.community.controller.Controller;
import org.georemindme.community.controller.PreferencesController;
import org.georemindme.community.view.custom.MyPositionLayer;
import org.georemindme.community.view.custom.MyPositionLayer.UserSetNewLocationListener;

import android.content.Intent;
import android.content.res.Resources;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

import static org.georemindme.community.controller.ControllerProtocol.*;


public class MapDialogActivity extends MapActivity implements Callback,
		UserSetNewLocationListener
{
	private Handler			controllerInbox;
	private Handler			ownInbox;
	
	private double			latitude, longitude;
	private String			address;
	
	private TextView		addressTextView;
	private Button			okButton;
	
	private MapView			map;
	private MapController	mapController;
	private GeoPoint		gp;
	
	private MyPositionLayer	positionOverlay;
	
	private Location		locationUsed;
	
	private Intent			intent;
	
	public static final int	MODE_SHOW			= 0;
	public static final int	MODE_SELECT_ADDRESS	= 1;
	public static final int	MODE_EDIT			= 2;
	
	private int				mode				= 0;
	
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mapdialogactivity);
		
		intent = getIntent();
		if (intent != null)
		{
			mode = intent.getIntExtra("MODE_FROM_INTENT", MODE_SHOW);
		}
		
		if (mode == MODE_EDIT)
		{
			Bundle data = getIntent().getExtras();
			locationUsed = new Location("unknown");
			locationUsed.setLatitude(data.getDouble("LATITUDE"));
			locationUsed.setLongitude(data.getDouble("LONGITUDE"));
		}
		
		controllerInbox = Controller.getInstace(getApplicationContext()).getInboxHandler();
		ownInbox = new Handler(this);
		Controller.getInstace(getApplicationContext()).addOutboxHandler(ownInbox);
		
		okButton = (Button) findViewById(R.id.mapdialogactivity_okbutton);
		okButton.setOnClickListener(new View.OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				Intent data = new Intent();
				Bundle b = new Bundle();
				
				b.putDouble("LATITUDE", locationUsed.getLatitude());
				b.putDouble("LONGITUDE", locationUsed.getLongitude());
				b.putString("ADDRESS", address);
				
				data.putExtras(b);
				
				PreferencesController.setZoom(map.getZoomLevel());
				
				setResult(RESULT_OK, data);
				finish();
			}
		});
		map = (MapView) findViewById(R.id.mapdialogactivity_map);
		mapController = map.getController();
		
		map.setBuiltInZoomControls(true);
		map.setTraffic(PreferencesController.isTraffic());
		map.setSatellite(PreferencesController.isSatellite());
		
		addressTextView = (TextView) findViewById(R.id.mapdialogactitivy_address);
		
		setData(savedInstanceState);
		
	}
	

	private void setData(Bundle b)
	{
		if (b != null)
		{
			latitude = b.getDouble("LATITUDE");
			longitude = b.getDouble("LONGITUDE");
			address = b.getString("ADDRESS");
			
			locationUsed = new Location("unknown");
			locationUsed.setLatitude(latitude);
			locationUsed.setLongitude(longitude);
			
			addressTextView.setText("Address: " + address);
			
		}
		else
		{
			if (mode != MODE_EDIT)
			{
				controllerInbox.sendEmptyMessage(V_REQUEST_LAST_LOCATION);
				controllerInbox.sendEmptyMessage(V_REQUEST_LAST_KNOWN_ADDRESS);
			}
			else
			{
				controllerInbox.obtainMessage(V_REQUEST_ADDRESS, new Double[]{locationUsed.getLatitude(),
						locationUsed.getLongitude()}).sendToTarget();
			}
		}
		
		setPosition();
		
	}
	

	private void setPosition()
	{
		if (locationUsed != null)
		{
			gp = new GeoPoint((int) (locationUsed.getLatitude() * 1E6), (int) (locationUsed.getLongitude() * 1E6));
			mapController.animateTo(gp);
			
			OverlayItem overlayitem = new OverlayItem(gp, "My Position", "Actual location");
			
			positionOverlay = new MyPositionLayer(getResources().getDrawable(R.drawable.icon), overlayitem, null);
			positionOverlay.addUserSetNewLocationListener(this);
			map.getOverlays().clear();
			map.getOverlays().add(positionOverlay);
			
			if (mode != MODE_SHOW)
			{
				
				positionOverlay.setDraggable(true);
			}
			else
			{
				putAllAlerts();
				positionOverlay.setDraggable(false);
			}
		}
		
	}
	

	private void putAllAlerts()
	{
		
	}
	

	public void onSaveInstanceState(Bundle b)
	{
		if (gp != null)
		{
			b.putDouble("LATITUDE", gp.getLatitudeE6() / 1E6 * 1.0);
			b.putDouble("LONGITUDE", gp.getLongitudeE6() / 1E6 * 1.0);
			b.putString("ADDRESS", address);
		}
		
		super.onSaveInstanceState(b);
	}
	

	/*
	 * public void onRestoreInstanceState(Bundle b) {
	 * super.onRestoreInstanceState(b);
	 * 
	 * setData(b); }
	 */

	@Override
	protected boolean isRouteDisplayed()
	{
		// TODO Auto-generated method stub
		return false;
	}
	

	@Override
	public boolean handleMessage(Message msg)
	{
		// TODO Auto-generated method stub
		switch (msg.what)
		{
			case C_LAST_LOCATION:
				if (mode != MODE_EDIT)
				{
					Location l = (Location) msg.obj;
					if (l != null)
					{
						locationUsed = l;
						setPosition();
					}
				}
				return true;
			case C_NO_LAST_LOCATION_AVAILABLE:

				return true;
			case LS_GETTING_ADDRESS_STARTED:
				address = "finding your address...";
				addressTextView.setText("Address: " + address);
				return true;
			case LS_GETTING_ADDRESS_FINISHED:
				if (msg.obj != null)
				{
					address = ((Address) msg.obj).getAddressLine(0);
					addressTextView.setText("Address: " + address);
				}
				else
				{
					address = "not available right now.";
					addressTextView.setText("Address: " + address);
				}
				return true;
			case LS_GETTING_ADDRESS_FAILED:
				address = "error finding your address.";
				addressTextView.setText("Address: " + address);
				return true;
		}
		return false;
	}
	

	@Override
	public void userSetNewLocationListener(GeoPoint gp)
	{
		// TODO Auto-generated method stub
		locationUsed = new Location("unknown");
		locationUsed.setLatitude(gp.getLatitudeE6() / 1E6 * 1.0);
		locationUsed.setLongitude(gp.getLongitudeE6() / 1E6 * 1.0);
		
		setPosition();
		Double[] data = new Double[2];
		data[0] = locationUsed.getLatitude();
		data[1] = locationUsed.getLongitude();
		
		controllerInbox.obtainMessage(V_REQUEST_ADDRESS, data).sendToTarget();
	}
	

	public void onStop()
	{
		super.onStop();
		Controller.getInstace(getApplicationContext()).removeOutboxHandler(ownInbox);
	}
}
