package org.georemindme.community.view;


import org.georemindme.community.R;
import org.georemindme.community.controller.Controller;
import org.georemindme.community.controller.PreferencesController;
import org.georemindme.community.view.custom.MyPositionLayer;
import org.georemindme.community.view.custom.MyPositionLayer.UserSetNewLocationListener;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.franciscojavierfernandez.android.libraries.mvcframework.view.MVCViewComponent;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

import static org.georemindme.community.controller.ControllerProtocol.*;


public class MapDialogActivity extends MapActivity implements
		UserSetNewLocationListener
{
	private Controller			controller		= null;
	private MVCViewComponent	connector		= null;
	
	private double				latitude, longitude;
	private String				address;
	
	private TextView			addressTextView;
	private Button				okButton;
	private Button				positionButton;
	
	private MapView				map;
	private MapController		mapController;
	private GeoPoint			gp;
	
	private MyPositionLayer		positionOverlay;
	
	private Location			locationUsed;
	private Location			userLocation	= null;
	
	private Bundle				data			= null;
	
	private int					mode			= 0;
	
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mapdialogactivity);
		
		data = getIntent().getExtras();
		if (data != null)
		{
			locationUsed = new Location("unknown");
			locationUsed.setLatitude(data.getDouble("LATITUDE"));
			locationUsed.setLongitude(data.getDouble("LONGITUDE"));
		}
		
		controller = Controller.getInstace(getApplicationContext());
		connector = new MVCViewComponent(controller)
		{
			
			@Override
			public boolean handleMessage(Message msg)
			{
				// TODO Auto-generated method stub
				switch (msg.what)
				{
					case RESPONSE_LAST_LOCATION:
					case RESPONSE_LOCATION_CHANGED:
						userLocation = (Location) msg.obj;
						return true;
					case RESPONSE_NO_LAST_LOCATION_AVAILABLE:

						return true;
					case RESPONSE_GETTING_ADDRESS_STARTED:
						address = getString(R.string.address_error_finding_your_address);
						addressTextView.setText(getString(R.string.address)
								+ ": " + address);
						return true;
					case RESPONSE_GETTING_ADDRESS_FINISHED:
						if (msg.obj != null)
						{
							address = ((Address) msg.obj).getAddressLine(0);
							addressTextView.setText(getString(R.string.address)
									+ ": " + address);
						}
						else
						{
							address = getString(R.string.not_available_right_now);
							addressTextView.setText(getString(R.string.address)
									+ ": " + address);
						}
						return true;
					case RESPONSE_GETTING_ADDRESS_FAILED:
						address = getString(R.string.address_error_finding_your_address);
						addressTextView.setText(getString(R.string.address)
								+ ": " + address);
						return true;
					case RESPONSE_COORDINATES_FROM_ADDRESS_FINISHED:
						setPosition((Location) msg.obj);
						return true;
					case RESPONSE_COORDINATES_FROM_ADDRESS_FAILED:
						Log.i("Conversi—n err—nea", ".... ups!....");
						return true;
					case RESPONSE_COORDINATES_FROM_ADDRESS_STARTED:
						Log.i("Conversi—n empezada", ".... esperando....");
						return true;
				}
				return false;
			}
		};
		controller.registerMVCComponent(connector);
		
		controller.sendMessage(REQUEST_LAST_LOCATION);
		
		okButton = (Button) findViewById(R.id.mapdialogactivity_okbutton);
		okButton.setOnClickListener(new View.OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				Intent data = null;
				if (locationUsed != null)
				{
					data = new Intent();
					Bundle b = new Bundle();
					
					b.putDouble("LATITUDE", locationUsed.getLatitude());
					b.putDouble("LONGITUDE", locationUsed.getLongitude());
					b.putString("ADDRESS", address);
					data.putExtras(b);
				}
				
				// PreferencesController.setZoom(map.getZoomLevel());
				
				setResult(RESULT_OK, data);
				finish();
			}
		});
		
		positionButton = (Button) findViewById(R.id.mapdialogactivity_getpositionbutton);
		positionButton.setOnClickListener(new View.OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				setCenterAtUserLocation();
			}
		});
		
		map = (MapView) findViewById(R.id.mapdialogactivity_map);
		mapController = map.getController();
		
		map.setBuiltInZoomControls(true);
		map.setTraffic(PreferencesController.isTraffic());
		map.setSatellite(PreferencesController.isSatellite());
		final GestureDetector mapGestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener()
		{
			public void onLongPress(MotionEvent e)
			{
			}
			

			public boolean onDoubleTap(MotionEvent e)
			{
				
				GeoPoint doubletap_gp = map.getProjection().fromPixels((int) e.getX(), (int) e.getY());
				locationUsed.setLatitude(doubletap_gp.getLatitudeE6() / 1E6);
				locationUsed.setLongitude(doubletap_gp.getLongitudeE6() / 1E6);
				userSetNewLocationListener(doubletap_gp);
				return super.onDoubleTap(e);
			}
			

			public boolean onSingleTapUp(MotionEvent e)
			{
				
				return super.onDown(e);
			}
		});
		map.setOnTouchListener(new View.OnTouchListener()
		{
			
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				// TODO Auto-generated method stub
				mapGestureDetector.onTouchEvent(event);
				return false;
			}
		});
		addressTextView = (TextView) findViewById(R.id.mapdialogactivity_address);
		
		if (savedInstanceState == null)
			setData(data);
		else
			setData(savedInstanceState);
		
	}
	

	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mapdialogactivity_menu, menu);
		
		return true;
	}
	

	public boolean onOptionsItemSelected(MenuItem item)
	{
		super.onOptionsItemSelected(item);
		
		switch (item.getItemId())
		{
			case (R.id.mapdialogactivity_menu_findaddress):
			{
				android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(this);
				
				alert.setTitle(R.string.direccion);
				alert.setMessage(R.string.direccion_a_buscar);
				
				// Set an EditText view to get user input
				final EditText input = new EditText(this);
				alert.setView(input);
				
				alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int whichButton)
					{
						String value = input.getText().toString();
						controller.sendMessage(REQUEST_COORDINATES_FROM_ADDRESS, value);
					}
				});
				
				alert.show();
				break;
			}
				
			case (R.id.mapdialogactivity_menu_setalertatuserlocation):
			{
				locationUsed = userLocation;
				GeoPoint point = new GeoPoint((int) (locationUsed.getLatitude() * 1E6), (int) (locationUsed.getLongitude() * 1E6));
				userSetNewLocationListener(point);
				break;
			}
		}
		return true;
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
			
			addressTextView.setText(getString(R.string.address) + ": "
					+ address);
			
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
			
			positionOverlay.setDraggable(true);
			
		}
		
	}
	
	private void setPosition(Location l)
	{
		if(l != null)
		{
			GeoPoint tmp = new GeoPoint((int) (l.getLatitude() * 1E6), (int) (l.getLongitude() * 1E6));
			mapController.animateTo(tmp);
		}
	}
	private void setCenterAtUserLocation()
	{
		if (userLocation != null)
		{
			gp = new GeoPoint((int) (userLocation.getLatitude() * 1E6), (int) (userLocation.getLongitude() * 1E6));
			mapController.animateTo(gp);
		}
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
		
		controller.sendMessage(REQUEST_ADDRESS, data);
	}
	

	public void onStop()
	{
		super.onStop();
		controller.unregisterMVCComponent(connector);
	}
}
