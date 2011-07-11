package org.georemindme.community.view;


import org.georemindme.community.R;
import org.georemindme.community.controller.Controller;
import org.georemindme.community.model.Alert;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import static org.georemindme.community.controller.ControllerProtocol.*;

public class AlertDialog extends Activity
{
	private MediaPlayer mp;
	
	private Button buttonHecho;
	private Button buttonSilenciar;
	private Button buttonVer;
	
	private TextView textviewName;
	
	private final static int MODE_MULTIPLE_ALERTS_NEAR = 1;
	private final static int MODE_SINGLE_ALERT_NEAR = 2;
	private int mode;
	
	private Alert alert;
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alertdialog);
		
		buttonHecho = (Button) findViewById(R.id.alertdialog_button_hecho);
		buttonSilenciar = (Button) findViewById(R.id.alertdialog_button_silenciar);
		buttonVer = (Button) findViewById(R.id.alertdialog_button_ver);
		
		textviewName = (TextView) findViewById(R.id.alertdialog_textview_nombre);
		
		Bundle extras = getIntent().getExtras();
		if(extras == null)
		{
			//Muchas alertas cerca de la posicion.
			mode = MODE_MULTIPLE_ALERTS_NEAR;
			buttonHecho.setVisibility(View.GONE);
			buttonSilenciar.setVisibility(View.GONE);
			
			textviewName.setText(R.string.hay_varias_tareas_cerca_de_ti);
		}
		else
		{
			//Una sola alerta. Vienen los datos.
			mode = MODE_SINGLE_ALERT_NEAR;
			alert = (Alert) extras.get("ALERT");
			
			textviewName.setText(alert.getName() + " " + getResources().getString(R.string.esta_cerca_tuya));
		}
		
		buttonHecho.setOnClickListener(new View.OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				if(v.equals(buttonHecho))
				{
					Object[] data = new Object[]{new Boolean("true"), alert.getId()};
					Controller.getInstace(getApplicationContext()).getInboxHandler()
					.obtainMessage(V_REQUEST_CHANGE_ALERT_DONE, data).sendToTarget();
					finish();
				}
			}
		});
		
		buttonSilenciar.setOnClickListener(new View.OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				if(v.equals(buttonSilenciar))
				{
					Object[] data = new Object[]{new Boolean("false"), alert.getId()};
					Controller.getInstace(getApplicationContext()).getInboxHandler()
					.obtainMessage(V_REQUEST_CHANGE_ALERT_ACTIVE, data).sendToTarget();
					finish();
				}
			}
		});
		
		buttonVer.setOnClickListener(new View.OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				if(v.equals(buttonVer))
				{
					Intent i = null;
					switch (mode)
					{
						case MODE_SINGLE_ALERT_NEAR:
							i = new Intent(AlertDialog.this, AddAlarmActivity.class);
							Bundle extras = new Bundle();
							extras.putSerializable("ALERT", alert);
							i.putExtras(extras);
							break;
						case MODE_MULTIPLE_ALERTS_NEAR:
							i = new Intent(AlertDialog.this, ListTabActivity.class);
							break;
					}
					
					startActivity(i);
					finish();
				}
				
			}
		});
		mp = MediaPlayer.create(AlertDialog.this, R.raw.bird);
		mp.start();
	}
	
	public void onPause()
	{
		super.onPause();
		mp.stop();
	}
	
	public void onBackPressed()
	{
		finish();
	}
}
