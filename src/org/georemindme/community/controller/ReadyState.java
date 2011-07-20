package org.georemindme.community.controller;

import static org.georemindme.community.controller.ControllerProtocol.PREFERENCE_AUTOUPDATE_CHANGED;
import static org.georemindme.community.controller.ControllerProtocol.PREFERENCE_LOCATION_PROVIDER_ACCURACY_CHANGED;
import static org.georemindme.community.controller.ControllerProtocol.PREFERENCE_LOCATION_PROVIDER_POWER_CHANGED;
import static org.georemindme.community.controller.ControllerProtocol.PREFERENCE_LOCATION_UPDATE_RADIUS_CHANGED;
import static org.georemindme.community.controller.ControllerProtocol.PREFERENCE_LOCATION_UPDATE_RATE_CHANGED;
import static org.georemindme.community.controller.ControllerProtocol.PREFERENCE_PREFERENCE_CHANGED;
import static org.georemindme.community.controller.ControllerProtocol.PREFERENCE_SYNC_RATE_CHANGED;
import static org.georemindme.community.controller.ControllerProtocol.REQUEST_ADDRESS;
import static org.georemindme.community.controller.ControllerProtocol.REQUEST_ALL_DONE_ALERTS;
import static org.georemindme.community.controller.ControllerProtocol.REQUEST_ALL_MUTED_ALERTS;
import static org.georemindme.community.controller.ControllerProtocol.REQUEST_ALL_UNDONE_ALERTS;
import static org.georemindme.community.controller.ControllerProtocol.REQUEST_AUTOLOGIN;
import static org.georemindme.community.controller.ControllerProtocol.REQUEST_CHANGE_ALERT_ACTIVE;
import static org.georemindme.community.controller.ControllerProtocol.REQUEST_CHANGE_ALERT_DONE;
import static org.georemindme.community.controller.ControllerProtocol.REQUEST_COORDINATES_FROM_ADDRESS;
import static org.georemindme.community.controller.ControllerProtocol.REQUEST_CREATE_NEW_USER;
import static org.georemindme.community.controller.ControllerProtocol.REQUEST_DELETE_ALERT;
import static org.georemindme.community.controller.ControllerProtocol.REQUEST_IS_LOGGED;
import static org.georemindme.community.controller.ControllerProtocol.REQUEST_LAST_KNOW_ADDRESS;
import static org.georemindme.community.controller.ControllerProtocol.REQUEST_LAST_LOCATION;
import static org.georemindme.community.controller.ControllerProtocol.REQUEST_LOGIN;
import static org.georemindme.community.controller.ControllerProtocol.REQUEST_LOGOUT;
import static org.georemindme.community.controller.ControllerProtocol.REQUEST_NEXT_TIMELINE_PAGE;
import static org.georemindme.community.controller.ControllerProtocol.REQUEST_PERIODICAL_UPDATES_OFF;
import static org.georemindme.community.controller.ControllerProtocol.REQUEST_PERIODICAL_UPDATES_ON;
import static org.georemindme.community.controller.ControllerProtocol.REQUEST_QUIT;
import static org.georemindme.community.controller.ControllerProtocol.REQUEST_RESET_LOCATION_PROVIDERS;
import static org.georemindme.community.controller.ControllerProtocol.REQUEST_UPDATE;
import static org.georemindme.community.controller.ControllerProtocol.REQUEST_UPDATE_ALERT;
import static org.georemindme.community.controller.ControllerProtocol.RESPONSE_ALERT_CHANGED;
import static org.georemindme.community.controller.ControllerProtocol.RESPONSE_ALERT_DELETED;
import static org.georemindme.community.controller.ControllerProtocol.RESPONSE_ALERT_NEAR;
import static org.georemindme.community.controller.ControllerProtocol.RESPONSE_ALERT_SAVED;
import static org.georemindme.community.controller.ControllerProtocol.RESPONSE_ALL_DONE_ALERTS;
import static org.georemindme.community.controller.ControllerProtocol.RESPONSE_ALL_MUTED_ALERTS;
import static org.georemindme.community.controller.ControllerProtocol.RESPONSE_ALL_UNDONE_ALERTS;
import static org.georemindme.community.controller.ControllerProtocol.RESPONSE_COORDINATES_FROM_ADDRESS_FAILED;
import static org.georemindme.community.controller.ControllerProtocol.RESPONSE_COORDINATES_FROM_ADDRESS_FINISHED;
import static org.georemindme.community.controller.ControllerProtocol.RESPONSE_COORDINATES_FROM_ADDRESS_STARTED;
import static org.georemindme.community.controller.ControllerProtocol.RESPONSE_GETTING_ADDRESS_FAILED;
import static org.georemindme.community.controller.ControllerProtocol.RESPONSE_GETTING_ADDRESS_FINISHED;
import static org.georemindme.community.controller.ControllerProtocol.RESPONSE_GETTING_ADDRESS_STARTED;
import static org.georemindme.community.controller.ControllerProtocol.RESPONSE_LOCATION_CHANGED;
import static org.georemindme.community.controller.ControllerProtocol.RESPONSE_LOGIN_FAILED;
import static org.georemindme.community.controller.ControllerProtocol.RESPONSE_LOGIN_FINISHED;
import static org.georemindme.community.controller.ControllerProtocol.RESPONSE_LOGIN_STARTED;
import static org.georemindme.community.controller.ControllerProtocol.RESPONSE_LOGOUT_FINISHED;
import static org.georemindme.community.controller.ControllerProtocol.RESPONSE_LOGOUT_STARTED;
import static org.georemindme.community.controller.ControllerProtocol.RESPONSE_NEXT_TIMELINE_PAGE_FINISHED;
import static org.georemindme.community.controller.ControllerProtocol.RESPONSE_NO_PROVIDER_AVAILABLE;
import static org.georemindme.community.controller.ControllerProtocol.RESPONSE_REQUEST_ALERTS_NEAR;
import static org.georemindme.community.controller.ControllerProtocol.RESPONSE_UPDATE_FAILED;
import static org.georemindme.community.controller.ControllerProtocol.RESPONSE_UPDATE_FINISHED;
import static org.georemindme.community.controller.ControllerProtocol.RESPONSE_UPDATE_STARTED;

import java.util.List;

import org.georemindme.community.model.Alert;
import org.georemindme.community.model.User;

import android.os.Message;
import android.util.Log;

import com.franciscojavierfernandez.android.libraries.mvcframework.controller.MVCControllerStateInterface;

public class ReadyState implements MVCControllerStateInterface
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
			case ControllerProtocol.REQUEST_SAVE_ALERT:
				controller.saveAlert((Alert) (msg.obj));
				return true;
				
			case REQUEST_UPDATE_ALERT:
				controller.updateAlert((Alert) msg.obj);
				return true;
				
			case REQUEST_QUIT:
				onRequestQuit();
				return true;
				
			case REQUEST_LOGIN:
				controller.getServerInstance().login((User) msg.obj);
				return true;
				
			case REQUEST_AUTOLOGIN:
				controller.getServerInstance().loginWithLocalUser();
				return true;
				
			case REQUEST_IS_LOGGED:
				controller.isLogged();
				return true;
				
			case REQUEST_LOGOUT:
				controller.getServerInstance().sync_data();
				controller.getServerInstance().logout();
				return true;
				
			case REQUEST_PERIODICAL_UPDATES_ON:
				controller.setPeriodicalUpdates();
				return true;
				
			case REQUEST_PERIODICAL_UPDATES_OFF:
				controller.cancelPeriodicalUpdates();
				return true;
			case REQUEST_LAST_LOCATION:
				controller.getLastLocation();
				return true;
			case REQUEST_LAST_KNOW_ADDRESS:
				controller.getLastKnownAddress();
				return true;
			case REQUEST_ADDRESS:
				Double[] data = (Double[])msg.obj;
				controller.getAddress(data[0], data[1]);
				return true;
			case REQUEST_UPDATE:
				controller.getServerInstance().sync_data();
				return true;
			case REQUEST_RESET_LOCATION_PROVIDERS:
				controller.restartLocationServer();
				return true;
			case REQUEST_ALL_UNDONE_ALERTS:
				controller.requestAllUndoneAlerts();
				return true;
			case REQUEST_ALL_DONE_ALERTS:
				controller.requestAllDoneAlerts();
				return true;
			case REQUEST_ALL_MUTED_ALERTS:
				controller.requestAllMutedAlerts();
				return true;
			case REQUEST_CHANGE_ALERT_ACTIVE:
				Object[] obj = (Object[])msg.obj;
				
				controller.changeAlertActive((Boolean)obj[0], (Long)obj[1]);
				return true;
			case REQUEST_CHANGE_ALERT_DONE:
				Object[] obj2 = (Object[])msg.obj;
				controller.changeAlertDone((Boolean)obj2[0], (Long)obj2[1]);
				return true;
			case REQUEST_DELETE_ALERT:
				controller.deleteAlert((Alert) msg.obj);
				return true;
			case REQUEST_CREATE_NEW_USER:
				Object[] obj3 = (Object[]) msg.obj;
				controller.createNewUser((String) obj3[0], (String) obj3[1]);
			case REQUEST_NEXT_TIMELINE_PAGE:
				controller.requestNextTimelinePage();
				return true;
			case REQUEST_COORDINATES_FROM_ADDRESS:
				controller.getCoordinatesFromAddress((String) msg.obj);
				return true;
			case PREFERENCE_PREFERENCE_CHANGED:
				controller.preferencesChanged((Integer)msg.obj);
				switch((Integer)msg.obj)
				{
					case PREFERENCE_LOCATION_PROVIDER_ACCURACY_CHANGED:
					case PREFERENCE_LOCATION_PROVIDER_POWER_CHANGED:
					case PREFERENCE_LOCATION_UPDATE_RADIUS_CHANGED:
					case PREFERENCE_LOCATION_UPDATE_RATE_CHANGED:
						controller.restartLocationServer();
						break;
					case PREFERENCE_AUTOUPDATE_CHANGED:
					case PREFERENCE_SYNC_RATE_CHANGED:
						controller.setPeriodicalUpdates();
						break;
				}
				
				return true;
			case RESPONSE_LOGIN_STARTED:
			case RESPONSE_LOGIN_FAILED:
			case RESPONSE_LOGIN_FINISHED:
			case RESPONSE_LOGOUT_FINISHED:
			case RESPONSE_LOGOUT_STARTED:
				controller.broadcastMessage(msg);
				return true;
			case RESPONSE_ALERT_SAVED:
				controller.broadcastMessage(msg);
				return true;
			case RESPONSE_ALERT_CHANGED:
				controller.broadcastMessage(msg);
				return true;
			case RESPONSE_ALERT_DELETED:
				controller.broadcastMessage(msg);
				return true;
			case RESPONSE_ALL_UNDONE_ALERTS:
			case RESPONSE_ALL_DONE_ALERTS:
			case RESPONSE_ALL_MUTED_ALERTS:
				controller.broadcastMessage(msg);
				return true;
			case RESPONSE_LOCATION_CHANGED:
				Log.e("NEW LOCATION", msg.obj.toString());
				controller.broadcastMessage(msg);
				return true;
			case RESPONSE_NO_PROVIDER_AVAILABLE:
				Log.e("No provider available.", " Please check settings");
				controller.broadcastMessage(msg);
				return true;
			case RESPONSE_GETTING_ADDRESS_STARTED:
			case RESPONSE_GETTING_ADDRESS_FAILED:
			case RESPONSE_GETTING_ADDRESS_FINISHED:
				controller.broadcastMessage(msg);
				return true;
			case RESPONSE_REQUEST_ALERTS_NEAR:
				controller.requestAlarmsNear();
				return true;
			case RESPONSE_ALERT_NEAR:
				controller.notifyAlert((List<Alert>) msg.obj);
				return true;
			case RESPONSE_NEXT_TIMELINE_PAGE_FINISHED:
				controller.broadcastMessage(msg);
				return true;
			case RESPONSE_UPDATE_STARTED:
				controller.broadcastMessage(msg);
				return true;
			case RESPONSE_UPDATE_FAILED:
				controller.broadcastMessage(msg);
				return true;
			case RESPONSE_UPDATE_FINISHED:
				controller.broadcastMessage(msg);
				return true;
			case RESPONSE_COORDINATES_FROM_ADDRESS_FAILED:
				controller.broadcastMessage(msg);
				return true;
			case RESPONSE_COORDINATES_FROM_ADDRESS_FINISHED:
				controller.broadcastMessage(msg);
				return true;
			case RESPONSE_COORDINATES_FROM_ADDRESS_STARTED:
				controller.broadcastMessage(msg);
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
