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

import com.franciscojavierfernandez.android.libraries.mvcframework.view.MVCViewComponent;
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


/**
 * Actividad empleada para la creación de nuevas alertas y el editado de las
 * mismas.
 * 
 * @author franciscojavierfernandeztoro
 * @version 1.0
 */
public class AddAlarmActivity extends MapActivity implements OnClickListener,
		PickTimeDateSetListener
{
	/**
	 * Instancia del controlador de la aplicación.
	 */
	private Controller			controller			= null;
	
	/**
	 * Componente capaz de recibir mensajes del controlador.
	 */
	private MVCViewComponent	connector			= null;
	
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
	
	private String				actual_address		= null;
	private Location			actual_location		= null;
	
	private String				selected_address	= null;
	private Location			selected_location	= null;
	
	private GeoPoint			gp					= null;
	
	private static final int	EDITING_MODE		= 0;
	private static final int	CREATION_MODE		= 1;
	
	private int					mode;
	private Alert				alert				= null;
	
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.addtaskactivity);
		
		_initUIComponents();
		
		controller = Controller.getInstace(getApplicationContext());
		connector = new MVCViewComponent(controller)
		{
			
			@Override
			public boolean handleMessage(Message msg)
			{
				// TODO Auto-generated method stub
				switch (msg.what)
				{
					case RESPONSE_GETTING_ADDRESS_STARTED:
						addressView.setText(R.string.address_finding_your_address);
						return true;
						
					case RESPONSE_GETTING_ADDRESS_FAILED:
						addressView.setText(R.string.address_error_finding_your_address);
						return true;
						
					case RESPONSE_GETTING_ADDRESS_FINISHED:
						String address_to_show = getString(R.string.address_not_available_right_now);
						if (msg.obj != null)
						{
							switch (mode)
							{
								case EDITING_MODE:
									if (selected_address == null
											|| selected_address.equals(""))
									{
										selected_address = ((Address) msg.obj).getAddressLine(0);
									}
									address_to_show = selected_address;
									break;
								case CREATION_MODE:
									actual_address = ((Address) msg.obj).getAddressLine(0);
									address_to_show = actual_address;
									break;
							}
							
						}
						else
						{
							actual_address = getString(R.string.address_not_available_right_now);
							address_to_show = actual_address;
						}
						
						addressView.setText(getString(R.string.address) + ": "
								+ address_to_show);
						return true;
						
					case RESPONSE_LOCATION_CHANGED:
					case RESPONSE_LAST_LOCATION:
						boolean actual_location_was_empty;
						if (actual_location == null)
							actual_location_was_empty = true;
						else
							actual_location_was_empty = false;
						actual_location = (Location) msg.obj;
						
						if (actual_location_was_empty && mode == CREATION_MODE)
							setPosition(actual_location);
						return true;
						
					case RESPONSE_ALERT_CHANGED:
					case RESPONSE_ALERT_SAVED:

						finish();
						return true;
				}
				
				return false;
			}
		};
		
		if (savedInstanceState != null)
		{
			// Hay datos guardados anterioremente.
			_setSavedInstanceStateData(savedInstanceState);
			
		}
		else
		{
			// Aqui tengo que meter la comprobación de si vienen datos para que
			// entre en modo EDIT.
			Bundle data = getIntent().getExtras();
			if (data != null)
			{
				_setModeEdit(data);
				
				Controller.getInstace(getApplicationContext()).removeNotification(alert.getId());
			}
			else
			{
				setdoneButton.setVisibility(View.GONE);
				mode = CREATION_MODE;
				// alert = new Alert();
				
				start = new Time();
				end = new Time();
			}
			
			if (actual_address == null)
			{
				controller.sendMessage(REQUEST_LAST_KNOW_ADDRESS);
			}
			else
			{
				addressView.setText(actual_address);
			}
			
			if (actual_location == null)
			{
				controller.sendMessage(REQUEST_LAST_LOCATION);
			}
			else
			{
				setPosition(actual_location);
			}
			
		}
		
		mapController.setZoom(PreferencesController.getZoom());
	}
	

	private void _setModeEdit(Bundle data)
	{
		setdoneButton.setVisibility(View.VISIBLE);
		mode = EDITING_MODE;
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
		
		selected_address = alert.getAddress();
		selected_location = new Location("unknown");
		selected_location.setLatitude(alert.getLatitude());
		selected_location.setLongitude(alert.getLongitude());
		
		if (selected_address.equals(""))
			controller.sendMessage(REQUEST_ADDRESS, new Double[] {
					alert.getLatitude(), alert.getLongitude() });
	}
	

	private void _setSavedInstanceStateData(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		mode = savedInstanceState.getInt("MODE");
		switch (mode)
		{
			case EDITING_MODE:
				selected_location = new Location("unknown");
				selected_location.setLatitude(savedInstanceState.getDouble("LATITUDE"));
				selected_location.setLongitude(savedInstanceState.getDouble("LONGITUDE"));
				selected_address = savedInstanceState.getString("ADDRESS");
				addressView.setText(selected_address);
				setPosition(selected_location);
				break;
			case CREATION_MODE:
				actual_location = new Location("unknown");
				actual_location.setLatitude(savedInstanceState.getDouble("LATITUDE"));
				actual_location.setLongitude(savedInstanceState.getDouble("LONGITUDE"));
				actual_address = savedInstanceState.getString("ADDRESS");
				addressView.setText(actual_address);
				setPosition(actual_location);
				break;
		}
		
		start = (Time) savedInstanceState.getSerializable("START");
		end = (Time) savedInstanceState.getSerializable("END");
		
	}
	

	private void _initUIComponents()
	{
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
	}
	

	public void onResume()
	{
		super.onResume();
		
		controller.registerMVCComponent(connector);
		
		if (actual_address == null)
		{
			if (mode != EDITING_MODE)
				controller.sendMessage(REQUEST_LAST_KNOW_ADDRESS);
		}
		else
		{
			if (selected_address == null)
				addressView.setText(actual_address);
			else
				addressView.setText(selected_address);
		}
		
		switch (mode)
		{
			case EDITING_MODE:
				setPosition(selected_location);
				break;
			case CREATION_MODE:
				setPosition(actual_location);
				break;
		}
		
	}
	

	public void onSaveInstanceState(Bundle b)
	{
		switch (mode)
		{
			case EDITING_MODE:
				if (selected_location != null)
				{
					b.putDouble("LATITUDE", selected_location.getLatitude());
					b.putDouble("LONGITUDE", selected_location.getLongitude());
					b.putString("ADDRESS", selected_address);
				}
				break;
			case CREATION_MODE:
				if (actual_location != null)
				{
					b.putDouble("LATITUDE", actual_location.getLatitude());
					b.putDouble("LONGITUDE", actual_location.getLongitude());
					b.putString("ADDRESS", actual_address);
				}
				break;
		}
		b.putInt("MODE", mode);
		
		b.putSerializable("START", start);
		b.putSerializable("END", end);
		
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
					
					if (selected_location != null)
					{
						double latitude = selected_location.getLatitude();
						double longitude = selected_location.getLongitude();
						
						alert.setAddress(selected_address);
						alert.setLatitude(latitude);
						alert.setLongitude(longitude);
					}
					else
					{
						//AQUI TENGO QUE CONTROLAR LO DE AÑADIR UNA DIRECCIÓN A PELO.
						//VAYA MOVIDA....
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
					if (mode == CREATION_MODE)
					{
						alert.setIdServer(0);
						alert.setDone_when(0);
						
						alert.setDone(false);
						alert.setActive(true);
						alert.setCreated(System.currentTimeMillis() / 1000);
						
						controller.sendMessage(REQUEST_SAVE_ALERT, alert);
					}
					else if (mode == EDITING_MODE)
					{
						controller.sendMessage(REQUEST_UPDATE_ALERT, alert);
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
				controller.sendMessage(REQUEST_CHANGE_ALERT_DONE, data);
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
	

	public void setPosition(Location l)
	{
		if (l != null)
		{
			GeoPoint gp = new GeoPoint((int) (l.getLatitude() * 1E6), (int) (l.getLongitude() * 1E6));
			mapController.animateTo(gp);
			mapController.setCenter(gp);
			
			OverlayItem overlayitem = new OverlayItem(gp, "", "");
			positionOverlay = new MyPositionLayer(getResources().getDrawable(R.drawable.blue_small), overlayitem, null);
			
			map.getOverlays().clear();
			map.getOverlays().add(positionOverlay);
		}
	}
	

	public void onStop()
	{
		super.onStop();
		controller.unregisterMVCComponent(connector);
	}
	

	public void launchMapActivity()
	{
		// TODO Auto-generated method stub
		Intent i = new Intent(AddAlarmActivity.this, MapDialogActivity.class);
		Bundle b = new Bundle();
		if (alert == null)
		{
			if(selected_location != null)
			{
				b.putDouble("LATITUDE", selected_location.getLatitude());
				b.putDouble("LONGITUDE", selected_location.getLongitude());
				b.putString("ADDRESS", selected_address);
			}
			else if (actual_location != null)
			{
				b.putDouble("LATITUDE", actual_location.getLatitude());
				b.putDouble("LONGITUDE", actual_location.getLongitude());
				b.putString("ADDRESS", actual_address);
			}
			else
				Log.w("LOCALIZACION ACTUAL: ", "No existe");
		}
		else
		{
			b.putDouble("LATITUDE", alert.getLatitude());
			b.putDouble("LONGITUDE", alert.getLongitude());
			b.putString("ADDRESS", alert.getAddress());
		}
		i.putExtras(b);
		/*
		 * if (mode == EDIT) { Bundle b = new Bundle(); b.putDouble("LATITUDE",
		 * lastLocation.getLatitude()); b.putDouble("LONGITUDE",
		 * lastLocation.getLongitude()); i.putExtra("MODE_FROM_INTENT",
		 * MapDialogActivity.MODE_EDIT); i.putExtras(b); } else {
		 * i.putExtra("MODE_FROM_INTENT",
		 * MapDialogActivity.MODE_SELECT_ADDRESS); }
		 */
		startActivityForResult(i, MAP_DIALOG_ACTIVITY);
	}
	

	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == MAP_DIALOG_ACTIVITY)
		{
			if (resultCode == RESULT_OK)
			{
				mode = EDITING_MODE;
				
				Bundle b = data.getExtras();
				if (b != null)
				{
					selected_location = new Location("unknown");
					selected_location.setLatitude(b.getDouble("LATITUDE"));
					selected_location.setLongitude(b.getDouble("LONGITUDE"));
					
					selected_address = b.getString("ADDRESS");
					
					setPosition(selected_location);
					addressView.setText(selected_address);
				}
			}
		}
	}
}
