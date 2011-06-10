package org.georemindme.community.controller;

import org.georemindme.community.model.Alert;
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
			case V_REQUEST_SAVE_ALERT:
				controller.saveAlert((Alert) (msg.obj));
				return true;
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
			case V_REQUEST_LAST_LOCATION:
				controller.getLastLocation();
				return true;
			case V_REQUEST_LAST_KNOWN_ADDRESS:
				controller.getLastKnownAddress();
				return true;
			case V_REQUEST_ADDRESS:
				Double[] data = (Double[])msg.obj;
				controller.getAddress(data[0], data[1]);
			case S_REQUEST_UPDATE:
			case V_REQUEST_UPDATE:
				controller.getServerInstance().sync_data();
				return true;
			case V_RESET_LOCATION_PROVIDERS:
				controller.restartLocationServer();
				return true;
			case V_REQUEST_ALL_UNDONE_ALERTS:
				controller.requestAllUndoneAlerts();
				return true;
			case V_REQUEST_ALL_DONE_ALERTS:
				controller.requestAllDoneAlerts();
				return true;
			case V_REQUEST_ALL_MUTED_ALERTS:
				controller.requestAllMutedAlerts();
				return true;
			case V_REQUEST_CHANGE_ALERT_ACTIVE:
				Object[] obj = (Object[])msg.obj;
				
				controller.changeAlertActive((Boolean)obj[0], (Integer)obj[1]);
				return true;
			case P_PREFERENCE_CHANGED:
				controller.preferencesChanged((Integer)msg.obj);
				switch((Integer)msg.obj)
				{
					case P_LOCATION_PROVIDER_ACCURACY_CHANGED:
					case P_LOCATION_PROVIDER_POWER_CHANGED:
					case P_LOCATION_UPDATE_RADIUS_CHANGED:
					case P_LOCATION_UPDATE_RATE_CHANGED:
						controller.restartLocationServer();
						break;
					case P_AUTOUPDATE_CHANGED:
					case P_SYNC_RATE_CHANGED:
						controller.setPeriodicalUpdates();
						break;
				}
				
				return true;
			case C_LOGIN_STARTED:
			case C_LOGIN_FAILED:
			case C_LOGIN_FINISHED:
			case C_LOGOUT_FINISHED:
			case C_LOGOUT_STARTED:
				controller.notifyOutboxHandlers(msg.what, msg.arg1, msg.arg2, msg.obj);
				return true;
			case C_ALERT_SAVED:
				controller.notifyOutboxHandlers(msg.what, msg.arg1, msg.arg2, msg.obj);
				return true;
			case C_ALERT_CHANGED:
				controller.notifyOutboxHandlers(msg.what, msg.arg1, msg.arg2, msg.obj);
				return true;
			case C_ALL_UNDONE_ALERTS:
			case C_ALL_DONE_ALERTS:
			case C_ALL_MUTED_ALERTS:
				controller.notifyOutboxHandlers(msg.what, msg.arg1, msg.arg2, msg.obj);
				return true;
			case LS_LOCATION_CHANGED:
				Log.e("NEW LOCATION", msg.obj.toString());
				controller.notifyOutboxHandlers(msg.what, msg.arg1, msg.arg2, msg.obj);
				return true;
			case LS_NO_PROVIDER_AVAILABLE:
				Log.e("No provider available.", " Please check settings");
				controller.notifyOutboxHandlers(msg.what, msg.arg1, msg.arg2, msg.obj);
				return true;
			case LS_GETTING_ADDRESS_STARTED:
			case LS_GETTING_ADDRESS_FAILED:
			case LS_GETTING_ADDRESS_FINISHED:
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
