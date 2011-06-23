package org.georemindme.community.controller.appserver;


import static org.georemindme.community.controller.ControllerProtocol.*;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alexd.jsonrpc.JSONRPCClient;
import org.alexd.jsonrpc.JSONRPCException;
import org.georemindme.community.controller.Controller;
import org.georemindme.community.model.Alert;
import org.georemindme.community.model.Database;
import org.georemindme.community.model.User;
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
	
	private JSONRPCClient		connection;
	
	private String				sessionId;
	
	private static Server		instance			= null;
	
	private Database			db;
	
	private Handler				controllerInbox;
	
	private Thread				loginThread;
	
	private User				user				= null;
	
	
	public static Server getInstance(Context context, Handler controllerInbox)
	{
		if (instance == null)
			instance = new Server(context, controllerInbox);
		
		return instance;
	}
	

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
	

	private void openConnection()
	{
		connection = JSONRPCClient.create(Server.URL);
		connection.setConnectionTimeout(Server.connectionTimeout);
		connection.setSoTimeout(Server.connectionTimeout);
	}
	

	private void closeConnection()
	{
		connection = null;
	}
	

	String getURL()
	{
		return URL;
	}
	

	int getConnectionTimeout()
	{
		return connectionTimeout;
	}
	

	void setConnectionTimeout(int connectionTimeout)
	{
		this.connectionTimeout = connectionTimeout;
		
		connection.setConnectionTimeout(connectionTimeout);
		connection.setSoTimeout(connectionTimeout);
	}
	

	public synchronized User getUser()
	{
		return user;
	}
	

	public final void loginfromdatabase()
	{
		final User u = db.getUser();
		
		login(u);
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
	

	public synchronized boolean isUserlogin()
	{
		if (sessionId == null)
			return false;
		
		return true;
	}
	

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
						JSONArray dataToSend = new JSONArray();
						
						Date now = new Date();
						long local_sync = now.getTime();
						local_sync /= 1000;
						
						Log.v("Session id:", "" + sessionId);
						// dataToSend.add(sessionId);
						
						Cursor c = db.getModifiedAlerts(since_last_sync);
						JSONArray dictionary = new JSONArray();
						if (c != null)
						{
							
							if (c.moveToFirst())
							{
								Log.v("", "El cursor trae datos...");
								do
								{
									JSONObject obj = new JSONObject();
									
									long c_done_when = c.getLong(c.getColumnIndex(Database.ALERT_DONE));
									obj.put("done_when", c_done_when);
									
									String c_name = c.getString(c.getColumnIndex(Database.ALERT_NAME));
								//c_name = encodeToUTF8(c_name);
									obj.put("name", c_name);
									
									long c_created = c.getLong(c.getColumnIndex(Database.ALERT_CREATE));
									obj.put("created", c_created);
									
									long c_starts = c.getLong(c.getColumnIndex(Database.ALERT_START));
									obj.put("starts", c_starts);
									
									long c_ends = c.getLong(c.getColumnIndex(Database.ALERT_END));
									obj.put("ends", c_ends);
									
									long c_modified = c.getLong(c.getColumnIndex(Database.ALERT_MODIFY));
									obj.put("modified", c_modified);
									
									double c_latitude = c.getDouble(c.getColumnIndex(Database.POINT_X));
									double c_longitude = c.getDouble(c.getColumnIndex(Database.POINT_Y));
									
									obj.put("x", c_latitude);
									obj.put("y", c_longitude);
									
									int c_active = c.getInt(c.getColumnIndex(Database.ALERT_ACTIVE));
									boolean c_active_processed;
									if (c_active == 0)
										c_active_processed = false;
									else
										c_active_processed = true;
									obj.put("active", c_active_processed);
									
									long c_id = c.getLong(c.getColumnIndex(Database.SERVER_ID));
									obj.put("id", c_id);
									
									String c_description = c.getString(c.getColumnIndex(Database.ALERT_DESCRIPTION));
								//c_description = encodeToUTF8(c_description);
									obj.put("description", c_description);
									
									/*
									 * Log.v("Datos modificados: ", "" +
									 * c_done_when + "\n" + c_name + "\n" +
									 * c_created + "\n" + c_starts + "\n" +
									 * c_ends + "\n" + c_modified + "\n" +
									 * c_latitude + "\n" + c_longitude + "\n" +
									 * c_active + "\n" + c_id);
									 */

									dictionary.add(obj);
								}
								while (c.moveToNext());
							}
							
							c.close();
						}
						
						dataToSend.add(dictionary);
						Log.v("Creando datos a subir", dictionary.toJSONString());
						
						String dataToUpload = dictionary.toJSONString();
					//dataToUpload = encodeToUTF8(dataToUpload);
						// Process data from server to local database.
						openConnection();
						String data = connection.callString("sync", sessionId, since_last_sync, dataToUpload);
						closeConnection();
						JSONParser parser = new JSONParser();
						
						Log.v("DATA lenght", ":" + data.length());
						
						Log.v("Datos del server", data);
						
						Object obj = parser.parse(data);
						JSONArray array = (JSONArray) obj;
						
						JSONArray alerts = (JSONArray) array.get(1);
						
						List<Alert> alertList = new ArrayList<Alert>();
						
						for (int i = 0; i < alerts.size(); i++)
						{
							JSONObject alert = (JSONObject) alerts.get(i);
							
							long id_server = (Long) alert.get("id");
							
							long done_when = 0;
							String done_when_s = (String) alert.get("done_when");
							if(!done_when_s.equals(""))
							{
								done_when = Long.parseLong(done_when_s);
							}
							
							long ends = 0;
							String ends_s = (String) alert.get("ends");
							if(!ends_s.equals(""))
							{
								ends = Long.parseLong(ends_s);
							}
							
							long starts = 0;
							String starts_s = (String) alert.get("starts");
							if(!starts_s.equals(""))
							{
								starts = Long.parseLong(starts_s);
							}
							
							long created = 0;
							String created_s = (String) alert.get("created");
							if(!created_s.equals(""))
							{
								created = Long.parseLong(created_s);
							}
							
							
							String description = (String) alert.get("description");
							
							boolean done = (Boolean) alert.get("done");
							String name = (String) alert.get("name");
							
							boolean active = (Boolean) alert.get("active");
							
							double latitude = (Double) alert.get("x");
							double longitude = (Double) alert.get("y");
							/*
							JSONArray points = (JSONArray) alert.get("points");
							JSONArray temp = (JSONArray) points.get(0);
							Object objc = temp.get(0);
							double latitude = (Double) temp.get(0);
							double longitude = (Double) temp.get(1);
							*/
							long modified = 0;
							String modified_s = (String) alert.get("modified");
							if(!modified_s.equals(""))
							{
								modified = Long.parseLong(modified_s);
							}
							
							
							Alert tmp = new Alert(0, id_server, done_when, ends, starts, created, done, name, description, active, modified, latitude, longitude);
							
							Log.v("Refrescando alerta", "SERVERID: " + id_server);
							
							alertList.add(tmp);
						}
						
						db.refreshAlerts(alertList);
						
						db.removeCreatedAlertsAndSynced();
						
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
	

	public final User getDatabaseUser()
	{
		// TODO Auto-generated method stub
		final User u = db.getUser();
		
		return u;
	}
	

	public void saveAlert(Alert obj)
	{
		// TODO Auto-generated method stub
		db.addAlert(obj);
		controllerInbox.obtainMessage(C_ALERT_SAVED).sendToTarget();
	}
	
	
	public void updateAlert(Alert alert)
	{
		db.refreshAlert(alert);
		controllerInbox.obtainMessage(C_ALERT_SAVED).sendToTarget();
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
	
	private String encodeToUTF8(String stringToEncode)
	{
		try
		{
			return new String(stringToEncode.getBytes("UTF-8"));
			//return new String(stringToEncode, "UTF-18");
		}
		catch (UnsupportedEncodingException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
}
