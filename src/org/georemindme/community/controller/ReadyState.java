package org.georemindme.community.controller;

import org.georemindme.community.model.User;
import org.georemindme.community.tools.Logger;

import android.os.Message;
import android.util.Log;

import static org.georemindme.community.controller.ControllerProtocol.*;

public class ReadyState implements ControllerState
{
	private final Controller controller;
	
	public ReadyState(Controller controller)
	{
		this.controller = controller;
	}
	
	@Override
	public boolean handleMessage(Message msg)
	{
		switch(msg.what)
		{
			case V_REQUEST_QUIT:
				onRequestQuit();
				return true;
				
			case V_REQUEST_LOGIN:
				controller.getServerInstance().login((User) msg.obj);
				return true;
				
			case V_REQUEST_AUTOLOGIN:
				controller.getServerInstance().loginfromdatabase();
				return true;
				
			case V_REQUEST_IS_LOGGED:
				controller.isLogged();
				return true;
				
			case V_REQUEST_LOGOUT:
				controller.getServerInstance().logout();
				return true;
				
			case V_REQUEST_PERIODICAL_UPDATES:
				controller.setPeriodicalUpdates();
				return true;
				
			case V_REQUEST_QUIT_PERIODICAL_UPDATES:
				controller.cancelPeriodicalUpdates();
				return true;
				
			case S_REQUEST_UPDATE:
			case V_REQUEST_UPDATE:
				controller.getServerInstance().sync_data();
				return true;
			case P_PREFERENCE_CHANGED:
				controller.preferencesChanged((Integer)msg.obj);
				return true;
			case C_LOGIN_STARTED:
			case C_LOGIN_FAILED:
			case C_LOGIN_FINISHED:
			case C_LOGOUT_FINISHED:
			case C_LOGOUT_STARTED:
				controller.notifyOutboxHandlers(msg.what, msg.arg1, msg.arg2, msg.obj);
				return true;
		}
		return false;
	}


	private void onRequestQuit()
	{
		// TODO Auto-generated method stub
		controller.quit();
	}
}
