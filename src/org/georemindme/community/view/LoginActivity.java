package org.georemindme.community.view;


import org.georemindme.community.R;
import org.georemindme.community.controller.Controller;
import org.georemindme.community.model.User;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.Editable;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import static org.georemindme.community.controller.ControllerProtocol.*;


public class LoginActivity extends Activity implements Callback
{
	private static final String	LOG			= "Login-Debug";
	
	private Controller			controller;
	private Handler				controllerInbox;
	private Handler				myHandler;
	
	private Button				registerButton, createButton;
	private EditText			name, pass;
	
	private EditText			createName, createPass, createPassConfirm;
	
	private Button				showLogin, showCreate;
	
	private LinearLayout		registerLayout, createLayout, buttonsLayout;
	
	private boolean				islogged	= false;
	
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loginactivity);
		
		name = (EditText) findViewById(R.id.loginactivity_login_name);
		pass = (EditText) findViewById(R.id.loginactivity_login_pass);
		
		registerLayout = (LinearLayout) findViewById(R.id.loginactivity_login_layout);
		createLayout = (LinearLayout) findViewById(R.id.loginactivity_createuser_layout);
		buttonsLayout = (LinearLayout) findViewById(R.id.loginactivity_buttons_layout);
		
		showLogin = (Button) findViewById(R.id.loginactivity_show_login);
		showLogin.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				setScreenFocusOnRegister();
			}
		});
		showCreate = (Button) findViewById(R.id.loginactivity_show_register);
		showCreate.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				setScreenFocusOnCreate();
			}
		});
		registerButton = (Button) findViewById(R.id.loginactivity_login_login);
		registerButton.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				if (islogged)
				{
					controllerInbox.sendEmptyMessage(V_REQUEST_LOGOUT);
				}
				else
				{
					controllerInbox.obtainMessage(V_REQUEST_LOGIN, 
							new User(name.getText().toString(), pass.getText().toString())).sendToTarget();
					
				}
				
			}
		});
		
		createName = (EditText) findViewById(R.id.loginactivity_create_name);
		createPass = (EditText) findViewById(R.id.loginactivity_create_pass);
		createPassConfirm = (EditText) findViewById(R.id.loginactivity_create_pass_repeat);
		createButton = (Button) findViewById(R.id.loginactivity_create_login);
		
		createButton.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				if(!createName.getText().toString().equals(""))
				{
					if(createPass.getText().toString().equals(createPassConfirm.getText().toString()) 
							&& !createPassConfirm.getText().toString().equals(""))
					{
						Object[] data = new Object[] { createName.getText().toString(),
								createPassConfirm.getText().toString() };
						controllerInbox.obtainMessage(V_REQUEST_CREATE_NEW_USER, data).sendToTarget();
					}
					else
					{
						//Alerta diciendo que tienen que los pass ser iguales.
						showAlertDialogPasswordsNotEquals();
					}
				}
				else
				{
					//Alerta diciendo que tiene que introducir un email.
					showAlertDialogEmailEmpty();
				}
				
			}
		});
		controller = Controller.getInstace(getApplicationContext());
		controllerInbox = controller.getInboxHandler();
		myHandler = new Handler(this);
		
	}
	

	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
		{
			if (buttonsLayout.getVisibility() == View.GONE)
			{
				setScreenFocusDefault();
				return true;
			}
			else
				return super.onKeyDown(keyCode, event);
		}
		
		return super.onKeyDown(keyCode, event);
	}
	

	public void onStart()
	{
		super.onStart();
		
	}
	

	public void onResume()
	{
		super.onResume();
		controller.addOutboxHandler(myHandler);
		controllerInbox.sendEmptyMessage(V_REQUEST_IS_LOGGED);
	}
	

	public void onPause()
	{
		super.onPause();
	}
	

	public void onStop()
	{
		super.onStop();
		controller.removeOutboxHandler(myHandler);
	}
	

	public void onDestroy()
	{
		super.onDestroy();
	}
	

	@Override
	public boolean handleMessage(Message msg)
	{
		// TODO Auto-generated method stub
		switch (msg.what)
		{
			case C_IS_LOGGED:
				setUserIsLoggedIn((User) msg.obj);
				setScreenFocusOnRegister();
				setButtonsAsLogged();
				return true;
			case C_IS_NOT_LOGGED:
				setUserIsLoggedOut((User) msg.obj);
				return true;
			case C_LOGIN_STARTED:
				Toast.makeText(getApplicationContext(), R.string.log_in_started, Toast.LENGTH_SHORT).show();
				return true;
			case C_LOGIN_FAILED:
				Toast.makeText(getApplicationContext(), R.string.log_in_failed, Toast.LENGTH_SHORT).show();
				setScreenFocusOnRegister();
				return true;
			case C_LOGIN_FINISHED:
				Toast.makeText(getApplicationContext(), R.string.login_successful, Toast.LENGTH_SHORT).show();
				setUserIsLoggedIn((User) msg.obj);
				controllerInbox.obtainMessage(V_REQUEST_UPDATE).sendToTarget();
				finish();
				return true;
			case C_LOGOUT_STARTED:
				// Toast.makeText(getApplicationContext(), "Log out started",
				// Toast.LENGTH_SHORT).show();
				return true;
			case C_LOGOUT_FINISHED:
				// Toast.makeText(getApplicationContext(), "Log out finished",
				// Toast.LENGTH_SHORT).show();
				setUserIsLoggedOut(null);
				setButtonsAsLoggedOut();
				return true;
			case S_REQUEST_CREATE_NEW_USER_FINISHED:
				controllerInbox.obtainMessage(V_REQUEST_LOGIN, 
						new User(createName.getText().toString(), createPassConfirm.getText().toString())).sendToTarget();
				return true;
			case S_REQUEST_CREATE_NEW_USER_STARTED:
				Toast.makeText(getApplicationContext(), "Creando usuario", Toast.LENGTH_LONG).show();
				return true;
			case S_REQUEST_CREATE_NEW_USER_FAILED:
				Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
				return true;
		}
		return false;
	}
	

	private void setUserIsLoggedIn(User user)
	{
		islogged = true;
		registerButton.setText(R.string.log_out);
		if (user != null)
		{
			name.setText(user.getName());
			name.setEnabled(false);
			pass.setText(user.getPass());
			pass.setEnabled(false);
		}
	}
	

	private void setUserIsLoggedOut(User user)
	{
		islogged = false;
		registerButton.setText(R.string.log_in);
		if (user != null)
		{
			name.setText(user.getName());
			pass.setText(user.getPass());
		}
		name.setEnabled(true);
		pass.setEnabled(true);
	}
	

	private void setScreenFocusOnRegister()
	{
		buttonsLayout.setVisibility(View.GONE);
		createLayout.setVisibility(View.GONE);
		registerLayout.setVisibility(View.VISIBLE);
	}
	

	private void setScreenFocusOnCreate()
	{
		buttonsLayout.setVisibility(View.GONE);
		createLayout.setVisibility(View.VISIBLE);
		registerLayout.setVisibility(View.GONE);
	}
	

	private void setScreenFocusDefault()
	{
		buttonsLayout.setVisibility(View.VISIBLE);
		createLayout.setVisibility(View.GONE);
		registerLayout.setVisibility(View.GONE);
	}
	

	private void setButtonsAsLogged()
	{
		showLogin.setText("Est‡s conectado");
		showCreate.setEnabled(false);
	}
	

	private void setButtonsAsLoggedOut()
	{
		showLogin.setText(R.string.im_already_user);
		showCreate.setEnabled(true);
	}
	
	private void showAlertDialogEmailEmpty()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.debes_de_introducir_un_email_valido);
		builder.setCancelable(true);
		builder.setNeutralButton("Ok", new DialogInterface.OnClickListener()
		{
			
			public void onClick(DialogInterface dialog, int which)
			{
				// TODO Auto-generated method stub
				createName.requestFocus();
			}
		});
		builder.create().show();
	}
	
	private void showAlertDialogPasswordsNotEquals()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.no_coinciden_las_contrasenias);
		builder.setCancelable(true);
		builder.setNeutralButton("Ok", new DialogInterface.OnClickListener()
		{
			
			public void onClick(DialogInterface dialog, int which)
			{
				// TODO Auto-generated method stub
				createPass.setText("");
				createPass.requestFocus();
				createPassConfirm.setText("");
			}
		});
		builder.create().show();
	}
}
