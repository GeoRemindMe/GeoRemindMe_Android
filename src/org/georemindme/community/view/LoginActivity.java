package org.georemindme.community.view;


import org.georemindme.community.R;
import org.georemindme.community.controller.Controller;
import org.georemindme.community.model.User;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import static org.georemindme.community.controller.ControllerProtocol.*;


public class LoginActivity extends Activity implements Callback
{
	private static final String	LOG			= "Login-Debug";
	
	private Controller			controller;
	private Handler				controllerInbox;
	private Handler				myHandler;
	
	private Button				okButton;
	private EditText			name, pass;
	
	private boolean				islogged	= false;
	
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loginactivity);
		
		name = (EditText) findViewById(R.id.name);
		pass = (EditText) findViewById(R.id.pass);
		okButton = (Button) findViewById(R.id.okButton);
		okButton.setOnClickListener(new OnClickListener()
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
					Message msg = Message.obtain(controllerInbox, V_REQUEST_LOGIN, new User(name.getText().toString(), pass.getText().toString()));
					msg.sendToTarget();
				}
				
			}
		});
		controller = Controller.getInstace(getApplicationContext());
		controllerInbox = controller.getInboxHandler();
		myHandler = new Handler(this);
		
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
		Log.v(LOG, "Message received: " + msg.what);
		switch (msg.what)
		{
			case C_IS_LOGGED:
				setUserIsLoggedIn((User) msg.obj);
				return true;
			case C_IS_NOT_LOGGED:
				setUserIsLoggedOut((User) msg.obj);
				return true;
			case C_LOGIN_STARTED:
				Toast.makeText(getApplicationContext(), "Log in started", Toast.LENGTH_SHORT).show();
				return true;
			case C_LOGIN_FAILED:
				Toast.makeText(getApplicationContext(), "Log in failed!!", Toast.LENGTH_SHORT).show();
				return true;
			case C_LOGIN_FINISHED:
				Toast.makeText(getApplicationContext(), "Login successful", Toast.LENGTH_SHORT).show();
				setUserIsLoggedIn((User) msg.obj);
				finish();
				return true;
			case C_LOGOUT_STARTED:
				//Toast.makeText(getApplicationContext(), "Log out started", Toast.LENGTH_SHORT).show();
				return true;
			case C_LOGOUT_FINISHED:
				//Toast.makeText(getApplicationContext(), "Log out finished", Toast.LENGTH_SHORT).show();
				setUserIsLoggedOut(null);
				return true;
		}
		return false;
	}
	

	private void setUserIsLoggedIn(User user)
	{
		islogged = true;
		okButton.setText("Log out");
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
		okButton.setText("Log in");
		if (user != null)
		{
			name.setText(user.getName());
			pass.setText(user.getPass());
		}
		name.setEnabled(true);
		pass.setEnabled(true);
	}
}
