package org.georemindme.community.controller.appserver;


import static org.georemindme.community.controller.ControllerProtocol.*;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alexd.jsonrpc.JSONRPCClient;
import org.alexd.jsonrpc.JSONRPCException;
import org.georemindme.community.controller.Controller;
import org.georemindme.community.model.Alert;
import org.georemindme.community.model.Database;
import org.georemindme.community.model.User;
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


public class Server implements Serializable
{
	
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;
	
	private static final String	LOG					= "SERVER_DEBUG";
	
	private static final String	URL					= "http://3.georemindme.appspot.com/service/";
	private static int			connectionTimeout	= 10000;
	
	public static Server getInstance(Context context, Handler controllerInbox)
	{
		if (instance == null)
			instance = new Server(context, controllerInbox);
		
		return instance;
	}
	
	private JSONRPCClient		connection;
	
	private String				sessionId			= null;
	
	private static Server		instance			= null;
	
	private Database			db;
	
	private Handler				controllerInbox;
	
	private Thread				loginThread;
	
	
	private User				user				= null;
	

	Server(Context context, Handler controllerInbox)
	{
		this.controllerInbox = controllerInbox;
		
		// openConnection();
		
		db = Database.getDatabaseInstance(context);
		// Elimino la apertura ya que cada llamada a la base de datos a abre
		// impl’citamente.
		// db.open();
		sessionId = null;
	}
	

	private org.json.JSONArray alertsModifiedToJSON(long last_sync)
	{
		Cursor c = db.getModifiedAlerts(last_sync);
		org.json.JSONArray dictionary = new org.json.JSONArray();
		
		if (c != null)
		{
			
			if (c.moveToFirst())
			{
				do
				{
					org.json.JSONObject obj = new org.json.JSONObject();
					
					long c_done_when = c.getLong(c.getColumnIndex(Database.ALERT_DONE));
					try
					{
						if (c_done_when != 0)
							obj.put("done_when", c_done_when);
						
						String c_name = c.getString(c.getColumnIndex(Database.ALERT_NAME));
						obj.put("name", c_name);
						
						long c_created = c.getLong(c.getColumnIndex(Database.ALERT_CREATE));
						obj.put("created", c_created);
						
						long c_starts = c.getLong(c.getColumnIndex(Database.ALERT_START));
						obj.put("starts", c_starts);
						
						long c_ends = c.getLong(c.getColumnIndex(Database.ALERT_END));
						obj.put("ends", c_ends);
						
						long c_modified = c.getLong(c.getColumnIndex(Database.ALERT_MODIFY));
						obj.put("modified", c_modified);
						
						double c_latitude = c.getDouble(c.getColumnIndex(Database.LATITUDE));
						double c_longitude = c.getDouble(c.getColumnIndex(Database.LONGITUDE));
						
						obj.put("x", c_latitude);
						obj.put("y", c_longitude);
						
						int c_active = c.getInt(c.getColumnIndex(Database.ALERT_ACTIVE));
						boolean c_active_processed;
						if (c_active == 0)
							c_active_processed = false;
						else
							c_active_processed = true;
						obj.put("active", c_active_processed);
						Log.w("SYNC", c_name + " lleva como valor: "
								+ c_active_processed);
						long c_id = c.getLong(c.getColumnIndex(Database.SERVER_ID));
						if (c_id != 0)
							obj.put("id", c_id);
						else
						{
							long c_clientId = c.getLong(c.getColumnIndex(Database._ID));
							obj.put("client_id", c_clientId);
						}
						
						String c_description = c.getString(c.getColumnIndex(Database.ALERT_DESCRIPTION));
						obj.put("description", c_description);
						
					}
					catch (JSONException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					dictionary.put(obj);
					// dictionary.add(obj);
				}
				while (c.moveToNext());
			}
			
			c.close();
		}
		
		return dictionary;
	}
	

	private org.json.JSONArray alertsToDeleteJSON()
	{
		Cursor c = db.getAlertsToDeleteInServer();
		org.json.JSONArray dictionary = new org.json.JSONArray();
		
		if (c != null)
		{
			
			if (c.moveToFirst())
			{
				do
				{
					org.json.JSONObject obj = new org.json.JSONObject();
					
					long c_id = c.getLong(c.getColumnIndex(Database.SERVER_ID));
					try
					{
						obj.put("id", c_id);
					}
					catch (JSONException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					dictionary.put(obj);
					// dictionary.add(obj);
				}
				while (c.moveToNext());
			}
			
			c.close();
			
		}
		
		return dictionary;
	}
	

	public void changeAlertActive(boolean active, int id)
	{
		db.changeAlertActive(active, id);
		controllerInbox.obtainMessage(C_ALERT_CHANGED).sendToTarget();
	}
	

	public void changeAlertDone(boolean done, int id)
	{
		db.setAlertDone(id, done);
		controllerInbox.obtainMessage(C_ALERT_CHANGED).sendToTarget();
	}
	

	private void closeConnection()
	{
		connection = null;
	}
	

	public void deleteAlert(Alert obj)
	{
		// TODO Auto-generated method stub
		db.removeAlert(obj);
		controllerInbox.obtainMessage(C_ALERT_DELETED).sendToTarget();
	}
	

	private void deleteAlertsFromServer(JSONArray data)
	{
		List<Alert> alertList = new ArrayList<Alert>();
		
		for (int i = 0; i < data.size(); i++)
		{
			JSONObject alert = (JSONObject) data.get(i);
			
			long id_server = (Long) alert.get("id");
			
			Alert tmp = new Alert();
			tmp.setIdServer(id_server);
			
			alertList.add(tmp);
		}
		
		db.deleteAlerts(alertList);
	}
	

	private Alert getAlertAtActualPosition(Cursor c)
	{
		Alert alertSelected = new Alert();
		int active = c.getInt(c.getColumnIndex(Database.ALERT_ACTIVE));
		if (active == 0)
			alertSelected.setActive(false);
		else
			alertSelected.setActive(true);
		alertSelected.setCreated(c.getLong(c.getColumnIndex(Database.ALERT_CREATE)));
		alertSelected.setDescription(c.getString(c.getColumnIndex(Database.ALERT_DESCRIPTION)));
		long done_when = c.getLong(c.getColumnIndex(Database.ALERT_DONE));
		alertSelected.setDone_when(done_when);
		if (done_when != 0)
			alertSelected.setDone(true);
		else
			alertSelected.setDone(false);
		alertSelected.setEnds(c.getLong(c.getColumnIndex(Database.ALERT_END)));
		alertSelected.setId(c.getLong(c.getColumnIndex(Database._ID)));
		alertSelected.setIdServer(c.getLong(c.getColumnIndex(Database.SERVER_ID)));
		alertSelected.setLatitude(c.getDouble(c.getColumnIndex(Database.LATITUDE)));
		alertSelected.setLongitude(c.getDouble(c.getColumnIndex(Database.LONGITUDE)));
		alertSelected.setModified(c.getLong(c.getColumnIndex(Database.ALERT_MODIFY)));
		alertSelected.setName(c.getString(c.getColumnIndex(Database.ALERT_NAME)));
		alertSelected.setStarts(c.getLong(c.getColumnIndex(Database.ALERT_START)));
		
		return alertSelected;
	}
	

	int getConnectionTimeout()
	{
		return connectionTimeout;
	}
	

	public final User getDatabaseUser()
	{
		// TODO Auto-generated method stub
		final User u = db.getUser();
		
		return u;
	}
	

	String getURL()
	{
		return URL;
	}
	

	public synchronized User getUser()
	{
		return user;
	}
	

	public synchronized boolean isUserlogin()
	{
		if (sessionId == null)
			return false;
		
		return true;
	}
	

	public final void login(final User user)
	{
		loginThread = new Thread("Login thread")
		{
			public void run()
			{
				try
				{
					openConnection();
					user.setName(user.getName().toLowerCase());
					sessionId = connection.callString("login", user.getName(), user.getPass());
					if (user != null)
						db.setUser(user);
					closeConnection();
				}
				catch (JSONRPCException e)
				{
					controllerInbox.sendEmptyMessage(C_LOGIN_FAILED);
					// controller.notifyOutboxHandlers(C_LOGIN_FAILED, 0, 0,
					// null);
					e.printStackTrace();
				}
				catch (Exception e)
				{
					controllerInbox.sendEmptyMessage(C_UPDATE_FAILED);
					// controller.notifyOutboxHandlers(C_LOGIN_FAILED, 0, 0,
					// null);
					e.printStackTrace();
				}
				finally
				{
					if (sessionId != null)
						controllerInbox.post(new Runnable()
						{
							
							@Override
							public void run()
							{
								Message msg = controllerInbox.obtainMessage(C_LOGIN_FINISHED, user);
								msg.sendToTarget();
								// controller.notifyOutboxHandlers(C_LOGIN_FINISHED,
								// 0, 0, user);
								Server.this.user = user;
							}
						});
				}
			}
		};
		
		loginThread.start();
		controllerInbox.sendEmptyMessage(C_LOGIN_STARTED);
		// controller.notifyOutboxHandlers(C_LOGIN_STARTED, 0, 0, null);
	}
	

	public final void loginfromdatabase()
	{
		final User u = db.getUser();
		
		login(u);
	}
	

	public final void logout()
	{
		sessionId = null;
		user = null;
		Thread t = new Thread("Logout thread")
		{
			public void run()
			{
				try
				{
					db.flush();
					sessionId = null;
				}
				catch (Exception e)
				{
				}
				finally
				{
					controllerInbox.sendEmptyMessage(C_LOGOUT_FINISHED);
					// controller.notifyOutboxHandlers(C_LOGOUT_FINISHED, 0, 0,
					// null);
				}
			}
		};
		
		t.start();
		controllerInbox.sendEmptyMessage(C_LOGOUT_STARTED);
		// controller.notifyOutboxHandlers(C_LOGOUT_STARTED, 0, 0, null);
	}
	

	private void modifyAlerts(JSONArray data)
	{
		List<Alert> alertList = new ArrayList<Alert>();
		
		for (int i = 0; i < data.size(); i++)
		{
			JSONObject alert = (JSONObject) data.get(i);
			
			long id_server = (Long) alert.get("id");
			long done_when = (Long) alert.get("done_when");
			long ends = (Long) alert.get("ends");
			long starts = (Long) alert.get("starts");
			
			long created = (Long) alert.get("created");
			
			String description = (String) alert.get("description");
			
			boolean done = (Boolean) alert.get("done");
			String name = (String) alert.get("name");
			
			boolean active = (Boolean) alert.get("active");
			Log.w("SYNC", name + " trae como valor: " + active);
			double latitude = (Double) alert.get("x");
			double longitude = (Double) alert.get("y");
			
			long modified = (Long) alert.get("modified");
			
			long client_id = 0;
			Object client_id_tmp = alert.get("client_id");
			if (client_id_tmp != null)
				client_id = (Long) client_id_tmp;
			
			Alert tmp = new Alert(client_id, id_server, done_when, ends, starts, created, done, name, description, active, modified, latitude, longitude);
			
			Log.v("Refrescando alerta", "SERVERID: " + id_server + " X: "
					+ latitude + " Y: " + longitude);
			
			alertList.add(tmp);
		}
		
		db.refreshAlerts(alertList);
	}
	

	private void openConnection()
	{
		connection = JSONRPCClient.create(Server.URL, sessionId);
		connection.setConnectionTimeout(Server.connectionTimeout);
		connection.setSoTimeout(Server.connectionTimeout);
	}
	

	public void requestAlarmsNear(double latE6, double lngE6, int meters)
	{
		
		Cursor c = db.getAlertsToNotify(latE6, lngE6, meters);
		
		// Aqui voy disparando los eventos para que el notificador los detecte.
		if (c != null && c.moveToFirst())
		{
			do
			{
				Alert a = getAlertAtActualPosition(c);
				controllerInbox.obtainMessage(S_ALERT_NEAR, a).sendToTarget();
			}
			while (c.moveToNext());
		}
		else
		{
			Log.w("Notification", "No hay alertas cerca");
		}
	}
	

	public void requestAllDoneAlerts()
	{
		Cursor c = db.getAlertsDone();
		controllerInbox.obtainMessage(C_ALL_DONE_ALERTS, c).sendToTarget();
	}
	

	public void requestAllMutedAlerts()
	{
		Cursor c = db.getAlertsInactive();
		controllerInbox.obtainMessage(C_ALL_MUTED_ALERTS, c).sendToTarget();
	}
	

	public void requestAllUndoneAlerts()
	{
		// TODO Auto-generated method stub
		Cursor c = db.getAlertsUndone();
		controllerInbox.obtainMessage(C_ALL_UNDONE_ALERTS, c).sendToTarget();
	}
	

	public void requestAllUndoneNearestAlerts(double latitude,
			double longitude, int radio)
	{
		// TODO Auto-generated method stub
		Cursor c = db.getNearestAlertsUndone(latitude, longitude, radio);
		controllerInbox.obtainMessage(C_ALL_UNDONE_ALERTS, c).sendToTarget();
	}
	

	public void saveAlert(Alert obj)
	{
		// TODO Auto-generated method stub
		db.addAlert(obj);
		controllerInbox.obtainMessage(C_ALERT_SAVED).sendToTarget();
	}
	

	void setConnectionTimeout(int connectionTimeout)
	{
		this.connectionTimeout = connectionTimeout;
		
		connection.setConnectionTimeout(connectionTimeout);
		connection.setSoTimeout(connectionTimeout);
	}
	

	// Debo de unificar todo a org.json y dejar de usar org.simple.json.
	public void sync_data()
	{
		if (isUserlogin())
		{
			Thread t = new Thread("Sync thread")
			{
				public void run()
				{
					
					long since_last_sync = db.lastsync();
					
					try
					{
						Date now = new Date();
						long local_sync = now.getTime();
						local_sync /= 1000;
						
						org.json.JSONArray alertsModified = alertsModifiedToJSON(since_last_sync);
						org.json.JSONArray alertsDeleted = alertsToDeleteJSON();
						
						Log.v("Modificar en el server", alertsModified.toString());
						Log.v("Eliminar en el server", alertsDeleted.toString());
						
						openConnection();
						String data = connection.callString("sync_alert", since_last_sync, alertsModified, alertsDeleted);
						closeConnection();
						JSONParser parser = new JSONParser();
						
						Log.v("Datos del server", data);
						Object object = parser.parse(data);
						JSONArray array = (JSONArray) object;
						
						JSONArray alertsToModify = (JSONArray) array.get(1);
						JSONArray alertsToDelete = (JSONArray) array.get(2);
						
						modifyAlerts(alertsToModify);
						deleteAlertsFromServer(alertsToDelete);
						db.deleteAlerts();
						
						db.setLastsync((Long) array.get(0));
						
						controllerInbox.sendEmptyMessage(C_UPDATE_FINISHED);
						// controller.notifyOutboxHandlers(C_UPDATE_FINISHED, 0,
						// 0, null);
						
					}
					catch (JSONRPCException e)
					{
						e.printStackTrace();
						
						controllerInbox.sendEmptyMessage(C_UPDATE_FAILED);
						// controller.notifyOutboxHandlers(C_UPDATE_FAILED, 0,
						// 0, e);
						
					}
					catch (ParseException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
						
						controllerInbox.sendEmptyMessage(C_UPDATE_FAILED);
						// controller.notifyOutboxHandlers(C_UPDATE_FAILED, 0,
						// 0, e);
					}
				}
				
			};
			
			t.start();
			controllerInbox.sendEmptyMessage(C_UPDATE_STARTED);
			// controller.notifyOutboxHandlers(C_UPDATE_STARTED, 0, 0, null);
		}
		else
		{
			controllerInbox.sendEmptyMessage(C_UPDATE_FAILED);
			// controller.notifyOutboxHandlers(C_UPDATE_FAILED, 0, 0, null);
		}
	}
	

	public void updateAlert(Alert alert)
	{
		db.refreshAlert(alert);
		controllerInbox.obtainMessage(C_ALERT_SAVED).sendToTarget();
	}
}
