package org.georemindme.community.view;


import java.util.Date;

import org.georemindme.community.R;
import org.georemindme.community.controller.Controller;
import org.georemindme.community.controller.NotificationCenter;
import org.georemindme.community.controller.PreferencesController;
import org.georemindme.community.model.Alert;
import org.georemindme.community.model.Time;
import org.georemindme.community.view.custom.DummyMap;
import org.georemindme.community.view.custom.MyPositionLayer;
import org.georemindme.community.view.custom.PickTimeDateDialog;
import org.georemindme.community.view.custom.PickTimeDateDialog.PickTimeDateSetListener;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.OverlayItem;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
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
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import static org.georemindme.community.controller.ControllerProtocol.*;


public class AddAlarmActivity extends MapActivity implements OnClickListener,
		PickTimeDateSetListener, Callback
{
	private Handler				controllerInbox;
	private Handler				ownInbox;
	
	private static final int	PICK_DATE_START		= 1;
	private static final int	PICK_DATE_END		= 2;
	private static final int	MAP_DIALOG_ACTIVITY	= 3;
	
	private ImageButton			dashboardButton;
	private Button				saveButton;
	private Button				setdoneButton;
	private Button				startTimeButton;
	private Button				endTimeButton;
	
	private EditText			name;
	private EditText			description;
	
	private TextView			addressView;
	
	private DummyMap			map;
	private MapController		mapController;
	private MyPositionLayer		positionOverlay;
	
	private Time				start;
	private Time				end;
	
	private String				lastAddress			= null;
	private Location			lastLocation		= null;
	private GeoPoint			gp					= null;
	
	private boolean				userSetNewLocation	= false;
	
	private static final int	EDIT				= 0;
	private static final int	ADD					= 1;
	
	private int					mode;
	private Alert				alert				= null;
	
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.addtaskactivity);
		
		ownInbox = new Handler(this);
		controllerInbox = Controller.getInstace(getApplicationContext()).getInboxHandler();
		Controller.getInstace(getApplicationContext()).addOutboxHandler(ownInbox);
		
		dashboardButton = (ImageButton) findViewById(R.id.homeButton);
		dashboardButton.setOnClickListener(this);
		saveButton = (Button) findViewById(R.id.addtaskactivity_okButton);
		saveButton.setOnClickListener(this);
		setdoneButton = (Button) findViewById(R.id.resetButton);
		setdoneButton.setOnClickListener(this);
		startTimeButton = (Button) findViewById(R.id.startButton);
		startTimeButton.setOnClickListener(this);
		endTimeButton = (Button) findViewById(R.id.endButton);
		endTimeButton.setOnClickListener(this);
		
		addressView = (TextView) findViewById(R.id.addressTextView);
		
		map = (DummyMap) findViewById(R.id.dummymap);
		map.addParentActivity(this);
		mapController = map.getController();
		
		name = (EditText) findViewById(R.id.nameEditText);
		description = (EditText) findViewById(R.id.descriptionEditText);
		
		if (savedInstanceState != null)
		{
			userSetNewLocation = savedInstanceState.getBoolean("USERSETLOCATION");
			lastLocation = new Location("unknown");
			lastLocation.setLatitude(savedInstanceState.getDouble("LATITUDE"));
			lastLocation.setLongitude(savedInstanceState.getDouble("LONGITUDE"));
			lastAddress = savedInstanceState.getString("ADDRESS");
			
			start = (Time) savedInstanceState.getSerializable("START");
			end = (Time) savedInstanceState.getSerializable("END");
			
			addressView.setText(lastAddress);
			setPosition();
			
		}
		else
		{
			// Aqui tengo que meter la comprobaci—n de si vienen datos para que
			// entre en modo EDIT.
			Bundle data = getIntent().getExtras();
			if (data != null)
			{
				setdoneButton.setVisibility(View.VISIBLE);
				mode = EDIT;
				alert = (Alert) data.get("ALERT");
				
				name.setText(alert.getName());
				description.setText(alert.getDescription());
				long s = alert.getStarts();
				if (s == 0)
					start = new Time();
				else
					start = new Time(s);
				
				long e = alert.getEnds();
				if (e == 0)
					end = new Time();
				else
					end = new Time(e);
				
				if (alert.isDone())
					setdoneButton.setText(getString(R.string.set_pending));
				else
					setdoneButton.setText(getString(R.string.set_done));
				// As’ evito cambios de localizaci—n en la actividad.
				userSetNewLocation = true;
				lastAddress = "";
				lastLocation = new Location("unknown");
				lastLocation.setLatitude(alert.getLatitude());
				lastLocation.setLongitude(alert.getLongitude());
				controllerInbox.obtainMessage(V_REQUEST_ADDRESS, new Double[] {
						alert.getLatitude(), alert.getLongitude() }).sendToTarget();
				
				Controller.getInstace(getApplicationContext()).removeNotification(alert.getId());
			}
			else
			{
				setdoneButton.setVisibility(View.GONE);
				mode = ADD;
				alert = new Alert();
				
				start = new Time();
				end = new Time();
			}
			
			if (lastAddress == null)
			{
				controllerInbox.sendEmptyMessage(V_REQUEST_LAST_KNOWN_ADDRESS);
			}
			else
			{
				addressView.setText(lastAddress);
			}
			
			if (lastLocation == null)
			{
				controllerInbox.sendEmptyMessage(V_REQUEST_LAST_LOCATION);
			}
			else
			{
				setPosition();
			}
			
		}
		
		mapController.setZoom(PreferencesController.getZoom());
	}
	

	public void onResume()
	{
		super.onResume();
		
		Controller.getInstace(getApplicationContext()).addOutboxHandler(ownInbox);
		
		if (lastAddress == null)
		{
			if (mode != EDIT)
				controllerInbox.sendEmptyMessage(V_REQUEST_LAST_KNOWN_ADDRESS);
		}
		else
		{
			addressView.setText(lastAddress);
		}
		
		setPosition();
	}
	

	public void onSaveInstanceState(Bundle b)
	{
		if (lastLocation != null)
		{
			b.putBoolean("USERSETLOCATION", userSetNewLocation);
			b.putDouble("LATITUDE", lastLocation.getLatitude());
			b.putDouble("LONGITUDE", lastLocation.getLongitude());
			b.putString("ADDRESS", lastAddress);
			b.putSerializable("START", start);
			b.putSerializable("END", end);
		}
		super.onSaveInstanceState(b);
	}
	

	@Override
	protected boolean isRouteDisplayed()
	{
		// TODO Auto-generated method stub
		return false;
	}
	

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		switch (v.getId())
		{
			case R.id.homeButton:
				Intent intent = new Intent(getApplicationContext(), Dashboard.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				finish();
				startActivity(intent);
				break;
			case R.id.addtaskactivity_okButton:
				// Save to database. Send message to controller.
				if (!name.getText().toString().equals(""))
				{
					alert.setModified(System.currentTimeMillis() / 1000);
					alert.setEnds(end.getUnixTime());
					alert.setStarts(this.start.getUnixTime());
					alert.setName(name.getText().toString());
					alert.setDescription(description.getText().toString());
					
					if (lastLocation != null)
					{
						double latitude = lastLocation.getLatitude();
						double longitude = lastLocation.getLongitude();
						
						alert.setLatitude(latitude);
						alert.setLongitude(longitude);
					}
					else
					{
						AlertDialog.Builder builder = new AlertDialog.Builder(this);
						builder.setMessage(R.string.location_not_available);
						builder.setCancelable(true);
						builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
						{
							
							@Override
							public void onClick(DialogInterface dialog,
									int which)
							{
								// TODO Auto-generated method stub
								name.requestFocus();
								
							}
						});
						builder.create().show();
					}
					if (mode == ADD)
					{
						alert.setIdServer(0);
						alert.setDone_when(0);
						
						alert.setDone(false);
						alert.setActive(true);
						alert.setCreated(System.currentTimeMillis() / 1000);
						
						controllerInbox.obtainMessage(V_REQUEST_SAVE_ALERT, alert).sendToTarget();
					}
					else if (mode == EDIT)
					{
						controllerInbox.obtainMessage(V_REQUEST_UPDATE_ALERT, alert).sendToTarget();
					}
					
				}
				else
				{
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setMessage(R.string.alert_needs_to_have_a_name);
					builder.setCancelable(true);
					builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
					{
						
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							// TODO Auto-generated method stub
							name.requestFocus();
							
						}
					});
					builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener()
					{
						
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							// TODO Auto-generated method stub
							dialog.cancel();
						}
					});
					builder.create().show();
				}
				break;
			case R.id.resetButton:
				Object[] data = new Object[2];
				if (alert.isDone())
				{
					data[0] = false;
				}
				else
					data[0] = true;
				
				data[1] = alert.getId();
				controllerInbox.obtainMessage(V_REQUEST_CHANGE_ALERT_DONE, data).sendToTarget();
				break;
			case R.id.startButton:
				showDialog(PICK_DATE_START);
				break;
			case R.id.endButton:
				showDialog(PICK_DATE_END);
				break;
		}
	}
	

	protected Dialog onCreateDialog(int d)
	{
		Dialog dialog = null;
		switch (d)
		{
			case PICK_DATE_START:
				return new PickTimeDateDialog(AddAlarmActivity.this, PickTimeDateDialog.IS_START, start.isUndefined(), start, new PickTimeDateSetListener()
				{
					
					@Override
					public void pickTimerDateSet(Time time)
					{
						// TODO Auto-generated method stub
						start = time;
					}
				});
			case PICK_DATE_END:
				return new PickTimeDateDialog(AddAlarmActivity.this, PickTimeDateDialog.IS_END, end.isUndefined(), end, new PickTimeDateSetListener()
				{
					
					@Override
					public void pickTimerDateSet(Time time)
					{
						// TODO Auto-generated method stub
						end = time;
					}
				});
		}
		
		return null;
	}
	

	@Override
	public void pickTimerDateSet(Time time)
	{
		// TODO Auto-generated method stub
		Log.v("Time", time.log());
	}
	

	@Override
	public boolean handleMessage(Message msg)
	{
		// TODO Auto-generated method stub
		switch (msg.what)
		{
			case LS_GETTING_ADDRESS_STARTED:
				addressView.setText(R.string.address_finding_your_address);
				return true;
			case LS_GETTING_ADDRESS_FAILED:
				addressView.setText(R.string.address_error_finding_your_address);
				return true;
			case LS_GETTING_ADDRESS_FINISHED:
				if (msg.obj != null)
				{
					lastAddress = ((Address) msg.obj).getAddressLine(0);
					addressView.setText(getString(R.string.address) + ": "
							+ lastAddress);
				}
				else
					addressView.setText(R.string.address_not_available_right_now);
				return true;
			case LS_LOCATION_CHANGED:
			case C_LAST_LOCATION:
				if (!userSetNewLocation)
				{
					controllerInbox.obtainMessage(V_REQUEST_LAST_KNOWN_ADDRESS).sendToTarget();
					lastLocation = (Location) msg.obj;
					setPosition();
				}
				return true;
			case C_ALERT_CHANGED:
			case C_ALERT_SAVED:

				finish();
				return true;
		}
		
		return false;
	}
	

	public void setPosition()
	{
		if (lastLocation != null)
		{
			GeoPoint gp = new GeoPoint((int) (lastLocation.getLatitude() * 1E6), (int) (lastLocation.getLongitude() * 1E6));
			mapController.animateTo(gp);
			mapController.setCenter(gp);
			
			OverlayItem overlayitem = new OverlayItem(gp, "My Position", "Actual location");
			positionOverlay = new MyPositionLayer(getResources().getDrawable(R.drawable.icon), overlayitem, null);
			
			map.getOverlays().clear();
			map.getOverlays().add(positionOverlay);
		}
	}
	

	public void onStop()
	{
		super.onStop();
		Controller.getInstace(getApplicationContext()).removeOutboxHandler(ownInbox);
	}
	

	public void launchMapActivity()
	{
		// TODO Auto-generated method stub
		Intent i = new Intent(AddAlarmActivity.this, MapDialogActivity.class);
		
		if (mode == EDIT)
		{
			Bundle b = new Bundle();
			b.putDouble("LATITUDE", lastLocation.getLatitude());
			b.putDouble("LONGITUDE", lastLocation.getLongitude());
			i.putExtra("MODE_FROM_INTENT", MapDialogActivity.MODE_EDIT);
			i.putExtras(b);
		}
		else
		{
			i.putExtra("MODE_FROM_INTENT", MapDialogActivity.MODE_SELECT_ADDRESS);
		}
		startActivityForResult(i, MAP_DIALOG_ACTIVITY);
	}
	

	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == MAP_DIALOG_ACTIVITY)
		{
			if (resultCode == RESULT_OK)
			{
				userSetNewLocation = true;
				
				Bundle b = data.getExtras();
				lastLocation.setLatitude(b.getDouble("LATITUDE"));
				lastLocation.setLongitude(b.getDouble("LONGITUDE"));
				
				lastAddress = b.getString("ADDRESS");
				
				setPosition();
				addressView.setText(lastAddress);
			}
			else
			{
				userSetNewLocation = false;
			}
		}
	}
}
