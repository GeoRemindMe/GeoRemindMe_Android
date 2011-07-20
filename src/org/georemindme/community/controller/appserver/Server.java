package org.georemindme.community.controller.appserver;


import static org.georemindme.community.controller.ControllerProtocol.RESPONSE_ALERT_CHANGED;
import static org.georemindme.community.controller.ControllerProtocol.RESPONSE_ALERT_DELETED;
import static org.georemindme.community.controller.ControllerProtocol.RESPONSE_ALERT_NEAR;
import static org.georemindme.community.controller.ControllerProtocol.RESPONSE_ALERT_SAVED;
import static org.georemindme.community.controller.ControllerProtocol.RESPONSE_ALL_DONE_ALERTS;
import static org.georemindme.community.controller.ControllerProtocol.RESPONSE_ALL_MUTED_ALERTS;
import static org.georemindme.community.controller.ControllerProtocol.RESPONSE_ALL_UNDONE_ALERTS;
import static org.georemindme.community.controller.ControllerProtocol.RESPONSE_CREATE_NEW_USER_FAILED;
import static org.georemindme.community.controller.ControllerProtocol.RESPONSE_CREATE_NEW_USER_FINISHED;
import static org.georemindme.community.controller.ControllerProtocol.RESPONSE_CREATE_NEW_USER_STARTED;
import static org.georemindme.community.controller.ControllerProtocol.RESPONSE_LOGIN_FAILED;
import static org.georemindme.community.controller.ControllerProtocol.RESPONSE_LOGIN_FINISHED;
import static org.georemindme.community.controller.ControllerProtocol.RESPONSE_LOGIN_STARTED;
import static org.georemindme.community.controller.ControllerProtocol.RESPONSE_LOGOUT_FINISHED;
import static org.georemindme.community.controller.ControllerProtocol.RESPONSE_LOGOUT_STARTED;
import static org.georemindme.community.controller.ControllerProtocol.RESPONSE_NEXT_TIMELINE_PAGE_FAILED;
import static org.georemindme.community.controller.ControllerProtocol.RESPONSE_NEXT_TIMELINE_PAGE_FINISHED;
import static org.georemindme.community.controller.ControllerProtocol.RESPONSE_NEXT_TIMELINE_PAGE_STARTED;
import static org.georemindme.community.controller.ControllerProtocol.RESPONSE_UPDATE_FAILED;
import static org.georemindme.community.controller.ControllerProtocol.RESPONSE_UPDATE_FINISHED;
import static org.georemindme.community.controller.ControllerProtocol.RESPONSE_UPDATE_STARTED;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alexd.jsonrpc.JSONRPCClient;
import org.alexd.jsonrpc.JSONRPCException;
import org.georemindme.community.R;
import org.georemindme.community.controller.Controller;
import org.georemindme.community.model.Alert;
import org.georemindme.community.model.Database;
import org.georemindme.community.model.Error;
import org.georemindme.community.model.Timeline;
import org.georemindme.community.model.TimelineEvent;
import org.georemindme.community.model.TimelinePage;
import org.georemindme.community.model.User;
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.franciscojavierfernandez.android.libraries.mvcframework.*;
/**
 * Clase que representa una instancia del servidor de GeoRemindMe! con la cual
 * interactuar para obtener y enviar datos.
 * 
 * @author franciscojavierfernandeztoro.
 * @version 1.0
 */
@SuppressWarnings("serial")
public class Server implements Serializable
{
	/**
	 * URL en la que se encuentra el servidor del sistema.
	 */
	private static final String	SERVER_URL					= "https://3.georemindme.appspot.com/service/";
	
	/**
	 * Tiempo maximo (en milisegundos) para intentar la conexion con el servidor
	 */
	private static int			SERVER_CONNECTION_TIMEOUT	= 10000;
	
	
	/**
	 * Método para obtener una instancia del servidor para poder interactuar con
	 * el mismo.
	 * 
	 * @param context
	 *            Contexto en el cual se encuentra la base de datos.
	 * @param controller
	 *            Controlador el cual recibira mensajes del server.
	 * @return Una unica instancia del servidor. Patrón de diseño: singleton.
	 */
	public static Server getServerInstance(Context context,
			Controller controller)
	{
		if (instance == null)
			instance = new Server(context, controller);
		
		return instance;
	}
	
	/**
	 * Timeline que almacena los ultimos eventos relacionados con el usuario que
	 * se encuentra loggeado.
	 * 
	 * @see Timeline
	 */
	private Timeline		user_timeline	= null;
	
	/**
	 * Cliente RPC-JSON para interactuar con el servidor.
	 * 
	 * @see JSONRPCClient
	 */
	private JSONRPCClient	connection;
	
	/**
	 * Variable que almacena el sessionID asociado al usuario tras su loggeado.
	 */
	private String			sessionId		= null;
	
	/**
	 * Unica instancia del servidor que se ejecutara. Patron de diseño:
	 * singleton.
	 */
	private static Server	instance		= null;
	
	/**
	 * Base de datos asociada al servidor para almacenar una cache local de
	 * datos.
	 */
	private Database		db;
	
	/**
	 * Hebra para iniciar el proceso de login en background.
	 */
	private Thread			loginThread;
	
	/**
	 * Usuario loggeado en el sistema.
	 */
	private User			user			= null;
	
	/**
	 * Controlador de la aplicación.
	 * 
	 * @see Controller
	 */
	private Controller		controller;
	
	
	/**
	 * Constructor que genera una instancia del servidor lista para ser usada.
	 * 
	 * @param context
	 *            Contexto en el cual se encuentra la base de datos.
	 * @param controller
	 *            Controlador el cual recibira mensajes del server.
	 */
	private Server(Context context, Controller controller)
	{
		this.controller = controller;
		db = Database.getDatabaseInstance(context);
		sessionId = null;
	}
	

	/**
	 * Metodo para obtener una pagína del timeline del usuario registrado y
	 * establecerla como página actual del timeline. Se hace una llamada en
	 * background al servidor.
	 * 
	 * @param page_index
	 *            Indice de la pagina establecer como página actual.
	 */
	private void _setTimelinePage(final int page_index)
	{
		Thread thread = new Thread("_getTimelinePage_Thread")
		{
			String	data	= "";
			
			
			public void run()
			{
				openServerConnection();
				try
				{
					if (user_timeline.isCached())
						data = connection.callString("view_timeline", user.getName(), new Long(user_timeline.getTimelineId()), new Integer(page_index));
					else
						data = connection.callString("view_timeline", user.getName());
					Log.w("DATA for timeline " + page_index, data);
					if (!data.equals("null"))
					{
						JSONParser parser = new JSONParser();
						JSONArray jsondata = (JSONArray) parser.parse(data);
						
						long id_query = (Long) jsondata.get(0);
						JSONArray jsonTimelineEvents = (JSONArray) jsondata.get(1);
						
						TimelinePage timelinePage = new TimelinePage();
						
						for (int i = 0; i < jsonTimelineEvents.size(); i++)
						{
							JSONObject element = (JSONObject) jsonTimelineEvents.get(i);
							long id = (Long) element.get("id");
							long created = (Long) element.get("created");
							String msg = (String) element.get("msg");
							String username = (String) element.get("username");
							
							TimelineEvent timelineEvent = new TimelineEvent(id, created, msg, username);
							timelinePage.queueTimelineEvent(timelineEvent);
						}
						
						if (user_timeline == null)
							user_timeline = new Timeline();
						
						user_timeline.setTimelineId(id_query);
						user_timeline.setTimelinePageAtPosition(timelinePage, page_index);
					}
					controller.sendMessage(RESPONSE_NEXT_TIMELINE_PAGE_FINISHED, user_timeline.getActualTimelinePage());
				}
				catch (JSONRPCException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
					controller.sendMessage(RESPONSE_NEXT_TIMELINE_PAGE_FAILED, e);
				}
				catch (ParseException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
					controller.sendMessage(RESPONSE_NEXT_TIMELINE_PAGE_FAILED, e);
				}
				closeServerConnection();
			}
		};
		
		thread.start();
		controller.sendMessage(RESPONSE_NEXT_TIMELINE_PAGE_STARTED);
	}
	

	/**
	 * Metodo para obtener todas las alertas eliminadas en la cache de datos
	 * desde la ultima sincronizacion.
	 * 
	 * @param last_sync
	 *            Fecha en formato UNIX de la ultima sincronizacion.
	 * @return Coleccion de datos JSON con las alertas eliminadas en la caché
	 *         para eliminar en el servidor.
	 */
	
	private org.json.JSONArray alertsDeleted()
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
	

	/**
	 * Metodo para obtener todas las alertas modificadas en la cache de datos
	 * desde la ultima sincronizacion.
	 * 
	 * @param last_sync
	 *            Fecha en formato UNIX de la ultima sincronizacion.
	 * @return Coleccion de datos JSON con las alertas modificadas.
	 */
	private org.json.JSONArray alertsModified(long last_sync)
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
	

	/**
	 * Metodo que activa/desactiva una alerta para que no suene en caso de
	 * proximidad.
	 * 
	 * @param active
	 *            True si se quiere activar, false en otro caso.
	 * @param id
	 *            Identificador de la alerta en la cache local.
	 */
	public void changeAlertActive(boolean active, long id)
	{
		db.changeAlertActive(active, id);
		Alert a = db.getAlertWithID(id);
		controller.sendMessage(RESPONSE_ALERT_CHANGED, a);
	}
	

	/**
	 * Metodo que marca como hecha/pendiente una alerta.
	 * 
	 * @param active
	 *            True si se quiere marcar como hecha, false en otro caso.
	 * @param id
	 *            Identificador de la alerta en la cache local.
	 */
	public void changeAlertDone(boolean done, long id)
	{
		db.setAlertDone(id, done);
		Alert a = db.getAlertWithID(id);
		controller.sendMessage(RESPONSE_ALERT_CHANGED, a);
	}
	

	/**
	 * Metodo para invalidar el cliente RPC-JSON
	 */
	private void closeServerConnection()
	{
		connection = null;
	}
	

	/**
	 * Metodo para crear un nuevo usuario en la plataforma. Se hace mediante una
	 * llamada en background.
	 * 
	 * ATENCION: El método aún no está depurado. Simplemente lo crea pero no
	 * muestra los diferentes errores que se pueden producir durante su creación
	 * ya que esto implica recodificar el librería RPCJSON.
	 * 
	 * @param string
	 *            Correo electrónico del usuario a crear.
	 * @param string2
	 *            Contraseña del usuario a crear.
	 */
	public void createNewUser(final String string, final String string2)
	{
		Thread thread = new Thread("createUserThread")
		{
			boolean	data;
			
			
			public void run()
			{
				openServerConnection();
				try
				{
					data = connection.callBoolean("register", string, string2);
					
					if (!data)
					{
						controller.sendMessage(RESPONSE_CREATE_NEW_USER_FAILED, R.string.el_usuario_no_se_ha_podido_registrar);
					}
					else
					{
						controller.sendMessage(RESPONSE_CREATE_NEW_USER_FINISHED);
					}
				}
				catch (JSONRPCException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
					controller.sendMessage(RESPONSE_CREATE_NEW_USER_FAILED, e.getMessage());
				}
				closeServerConnection();
			}
		};
		
		thread.start();
		controller.sendMessage(RESPONSE_CREATE_NEW_USER_STARTED);
	}
	

	/**
	 * Metodo para eliminar una alerta de la cache local. (Realmente no se
	 * elimina de la cache hasta que ocurre la sincronización)
	 * 
	 * @param obj
	 *            Alerta que se va a eliminar.
	 */
	public void deleteAlert(Alert obj)
	{
		// TODO Auto-generated method stub
		db.removeAlert(obj);
		controller.sendMessage(RESPONSE_ALERT_DELETED);
	}
	

	/**
	 * Metodo que elimina alertas de la cache local recibidas por el servidor.
	 * 
	 * @param data
	 *            Coleccion de datos JSON
	 */
	private void deleteAlerts(JSONArray data)
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
	

	/**
	 * Metodo para convertir la posicion actual del cursor a la alerta que
	 * representa.
	 * 
	 * @param c
	 *            Cursor que almacena los datos.
	 * @return Alerta que esta representada en el cursor en la posicion actual
	 *         del mismo.
	 * @see Alert
	 */
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
	

	/**
	 * Metodo para obtener el ultimo usuario que se registro en la cache local y
	 * no cerro su sesion.
	 * 
	 * @return Usuario registrado en la cache local.
	 * 
	 * @see User
	 */
	public final User getLocalUser()
	{
		// TODO Auto-generated method stub
		final User u = db.getUser();
		
		return u;
	}
	

	/**
	 * Metodo para obtener el tiempo máximo permitido para establecer la
	 * conexión con el servidor.
	 * 
	 * @return Tiempo máximo en segundos para establecer la conexión con el
	 *         servidor.
	 */
	int getServerConnectionTimeout()
	{
		return SERVER_CONNECTION_TIMEOUT;
	}
	

	/**
	 * Metodo para obtener la dirección URL del servidor del servicio.
	 * 
	 * @return Direccion URL del servidor.
	 */
	String getServerURL()
	{
		return SERVER_URL;
	}
	

	/**
	 * Metodo que devuelve el usuario que esta loggeado.
	 * 
	 * @return Usuario que se encuentra loggeado en este momento.
	 */
	public synchronized User getUser()
	{
		return user;
	}
	

	/**
	 * Metodo para saber si hay algun usuario loggeado en este momento
	 * 
	 * @return True si se encuentra loggeado, false en otro caso.
	 */
	public synchronized boolean isUserlogin()
	{
		if (sessionId == null)
			return false;
		
		return true;
	}
	

	/**
	 * Metodo que loggea en el servidor remoto el usuario. Se establecen
	 * sessionId y user ademas de cachearlo en la cache de datos local. El
	 * metodo notificara al controlador de cualquier evento ocurrido durante el
	 * proceso.
	 * 
	 * @param user
	 *            Usuario a loggear en el servidor remoto.
	 * @see User
	 */
	public final void login(final User user)
	{
		loginThread = new Thread("Login thread")
		{
			public void run()
			{
				try
				{
					openServerConnection();
					user.setName(user.getName().toLowerCase());
					sessionId = connection.callString("login", user.getName(), user.getPass());
					if (user != null)
						db.setUser(user);
					closeServerConnection();
				}
				catch (JSONRPCException e)
				{
					Error dbError = new Error("Error JSONRPCException trying to login", System.currentTimeMillis() / 1000);
					db.addError(dbError);
					controller.sendMessage(RESPONSE_LOGIN_FAILED);
					// controller.notifyOutboxHandlers(C_LOGIN_FAILED, 0, 0,
					// null);
					e.printStackTrace();
				}
				catch (Exception e)
				{
					Error dbError = new Error("Login error due maybe autologin and db empty - Not important", System.currentTimeMillis() / 1000);
					db.addError(dbError);
					controller.sendMessage(RESPONSE_UPDATE_FAILED);
					// controller.notifyOutboxHandlers(C_LOGIN_FAILED, 0, 0,
					// null);
					e.printStackTrace();
				}
				finally
				{
					if (sessionId != null)
					{
						controller.sendMessage(RESPONSE_LOGIN_FINISHED, user);
						Server.this.user = user;
					}
					
				}
			}
		};
		
		loginThread.start();
		controller.sendMessage(RESPONSE_LOGIN_STARTED);
		// controller.notifyOutboxHandlers(C_LOGIN_STARTED, 0, 0, null);
	}
	

	/**
	 * Metodo para invocar el login usando el usuario que se encuentra cacheado
	 * en local.
	 */
	public final void loginWithLocalUser()
	{
		final User u = db.getUser();
		
		login(u);
	}
	

	/**
	 * Metodo para invalidar al usuario en la cache local. Proceso equivalente a
	 * logout en el servidor remoto. El controlador es notificado de cualquier
	 * evento ocurrido durante el proceso.
	 */
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
					controller.sendMessage(RESPONSE_LOGOUT_FINISHED);
					// controller.notifyOutboxHandlers(C_LOGOUT_FINISHED, 0, 0,
					// null);
				}
			}
		};
		
		t.start();
		controller.sendMessage(RESPONSE_LOGOUT_STARTED);
		// controller.notifyOutboxHandlers(C_LOGOUT_STARTED, 0, 0, null);
	}
	

	/**
	 * Metodo para modificar una coleccion de alertas en la cache local.
	 * 
	 * @param data
	 *            Coleccion de datos JSON que representan las alertas a
	 *            modificar.
	 */
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
	Log.v("Address: ", (String) alert.get("address"));		
			String address = (String) alert.get("address");
			Alert tmp = new Alert(client_id, id_server, done_when, ends, starts, 
					created, done, name, description, active, modified, latitude, longitude, address);
			
			alertList.add(tmp);
		}
		
		db.refreshAlerts(alertList);
	}
	

	/**
	 * Metodo para establecer conexion remota con el servidor del servicio.
	 */
	private void openServerConnection()
	{
		connection = JSONRPCClient.create(Server.SERVER_URL, sessionId);
		connection.setConnectionTimeout(Server.SERVER_CONNECTION_TIMEOUT);
		connection.setSoTimeout(Server.SERVER_CONNECTION_TIMEOUT);
	}
	

	/**
	 * Metodo para obtener las alertas activas y pendientes de finalización
	 * cercanas a unas coordenadas con un area expresada en metros. Se hace una
	 * cuadratura de los puntos. El metodo no es eficaz al 100%.
	 * 
	 * @param lat
	 *            Latitud de las coordenada del punto central en formato
	 *            flotante.
	 * @param lng
	 *            Longitud de la coordenada del punto central en formato
	 *            flotante.
	 * @param meters
	 *            Metros de radio a partir del punto central.
	 */
	public void requestAlarmsNear(double lat, double lng, int meters)
	{
		
		Cursor c = db.getAlertsToNotify(lat, lng, meters);
		
		if (c != null && c.moveToFirst())
		{
			List<Alert> listado = new ArrayList<Alert>();
			do
			{
				Alert a = getAlertAtActualPosition(c);
				listado.add(a);
				
			}
			while (c.moveToNext());
			
			controller.sendMessage(RESPONSE_ALERT_NEAR, listado);
		}
		else
		{
			Log.w("Notification", "No hay alertas cerca");
		}
	}
	

	/**
	 * Metodo para obtener todas las alertas registradas en la cache local
	 * terminadas.
	 */
	public void requestAllDoneAlerts()
	{
		Cursor c = db.getAlertsDone();
		controller.sendMessage(RESPONSE_ALL_DONE_ALERTS, c);
	}
	

	/**
	 * Metodo para obtener todas las alertas registradas en la cache local
	 * desactivadas.
	 */
	public void requestAllMutedAlerts()
	{
		Cursor c = db.getAlertsInactive();
		controller.sendMessage(RESPONSE_ALL_MUTED_ALERTS, c);
	}
	

	/**
	 * Metodo para obtener todas las alertas registradas en la cache local
	 * pendientes de terminacion.
	 */
	public void requestAllUndoneAlerts()
	{
		// TODO Auto-generated method stub
		Cursor c = db.getAlertsUndone();
		controller.sendMessage(RESPONSE_ALL_UNDONE_ALERTS, c);
	}
	

	/**
	 * Metodo para obtener la coleccion de alertas sin terminar cercanas a un
	 * punto central con un radio.
	 * 
	 * @param latitude
	 *            Latitud del punto central en punto flotante
	 * @param longitude
	 *            Longitud del punto central en punto flotante.
	 * @param radio
	 *            Metros en los cuales buscar la alerta.
	 */
	public void requestAllUndoneNearestAlerts(double latitude,
			double longitude, int radio)
	{
		// TODO Auto-generated method stub
		Cursor c = db.getNearestAlertsUndone(latitude, longitude, radio);
		controller.sendMessage(RESPONSE_ALL_UNDONE_ALERTS, c);
	}
	

	/**
	 * Metodo para obtener la siguiente página de timeline del usuario loggeado.
	 * En caso de que el timeline no se haya generado aún, se obtendrá la
	 * primera página.
	 */
	public void requestNextTimelinePage()
	{
		if (isUserlogin())
			if (user_timeline == null)
			{
				user_timeline = new Timeline();
				_setTimelinePage(user_timeline.getPageIndex());
			}
			else
			{
				_setTimelinePage(user_timeline.getPageIndex() + 1);
			}
		
	}
	

	/**
	 * Metodo que guarda una alerta en la cache de datos local.
	 * 
	 * @param obj
	 *            Alerta a guardar en la cache.
	 */
	public void saveAlert(Alert obj)
	{
		// TODO Auto-generated method stub
		db.addAlert(obj);
		controller.sendMessage(RESPONSE_ALERT_SAVED);
	}
	

	/**
	 * Metodo para enviar el registro de errores cacheado en local al servidor
	 * remoto. En caso de que la operación sea satisfactoria elimina los errores
	 * de la cache local.
	 */
	private final void sendErrorsToServer()
	{
		Cursor c = db.getAllErrors();
		org.json.JSONArray dictionary = new org.json.JSONArray();
		
		if (c != null)
		{
			
			if (c.moveToFirst())
			{
				do
				{
					org.json.JSONObject obj = new org.json.JSONObject();
					
					if (user != null)
					{
						long c_error_when = c.getLong(c.getColumnIndex(Database.ERROR_DATE));
						String c_error_message = c.getString(c.getColumnIndex(Database.ERROR_MESSAGE));
						
						try
						{
							obj.put("email", user.getName());
							obj.put("msg", c_error_message);
							obj.put("datetime", c_error_when);
						}
						catch (JSONException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
					else
					{
						Error e = new Error("Error because system tried to send error without valid user", System.currentTimeMillis() / 1000);
						db.addError(e);
					}
					
					dictionary.put(obj);
					// dictionary.add(obj);
				}
				while (c.moveToNext());
			}
			
			c.close();
		}
		
		// Aqui tengo que enviar el diccionario al server.
		openServerConnection();
		boolean response = false;
		try
		{
			Log.i("Error list", dictionary.toString());
			response = connection.callBoolean("report_bug", dictionary);
			if (response == true)
			{
				db.removeErrors();
			}
			else
			{
				Error dbError = new Error("Server error procesing errors list. Operation cancel", System.currentTimeMillis() / 1000);
				db.addError(dbError);
			}
		}
		catch (JSONRPCException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			Error dbError = new Error("Error JSONRPCException trying to send errors.", System.currentTimeMillis() / 1000);
			db.addError(dbError);
		}
		closeServerConnection();
		
	}
	

	/**
	 * Metodo que establece un nuevo tiempo máximo de conexión con el servidor
	 * remoto
	 * 
	 * @param connectionTimeout
	 *            Nuevo tiempo máximo de conexión.
	 */
	void setConnectionTimeout(int connectionTimeout)
	{
		Server.SERVER_CONNECTION_TIMEOUT = connectionTimeout;
		
		connection.setConnectionTimeout(connectionTimeout);
		connection.setSoTimeout(connectionTimeout);
	}
	

	/**
	 * Metodo para sincronizar los datos de la caché local con los datos
	 * almacenados en el servidor. Se actualizará la caché de datos y el
	 * servidor.
	 */
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
						
						org.json.JSONArray alertsModified = alertsModified(since_last_sync);
						org.json.JSONArray alertsDeleted = alertsDeleted();
						
						Log.v("Modificar en el server", alertsModified.toString());
						Log.v("Eliminar en el server", alertsDeleted.toString());
						
						openServerConnection();
						String data = connection.callString("sync_alert", since_last_sync, alertsModified, alertsDeleted);
						closeServerConnection();
						JSONParser parser = new JSONParser();
						
						Log.v("Datos del server", data);
						Object object = parser.parse(data);
						JSONArray array = (JSONArray) object;
						
						JSONArray alertsToModify = (JSONArray) array.get(1);
						JSONArray alertsToDelete = (JSONArray) array.get(2);
						
						modifyAlerts(alertsToModify);
						deleteAlerts(alertsToDelete);
						db.deleteAlerts();
						
						db.setLastsync((Long) array.get(0));
						
						sendErrorsToServer();
						
						controller.sendMessage(RESPONSE_UPDATE_FINISHED);
						// controller.notifyOutboxHandlers(C_UPDATE_FINISHED, 0,
						// 0, null);
						
					}
					catch (JSONRPCException e)
					{
						e.printStackTrace();
						
						controller.sendMessage(RESPONSE_UPDATE_FAILED);
						
						Error dbError = new Error("Error JSONRPCException trying to update data", System.currentTimeMillis() / 1000);
						db.addError(dbError);
						// controller.notifyOutboxHandlers(C_UPDATE_FAILED, 0,
						// 0, e);
						
					}
					catch (ParseException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
						
						controller.sendMessage(RESPONSE_UPDATE_FAILED);
						// controller.notifyOutboxHandlers(C_UPDATE_FAILED, 0,
						// 0, e);
						Error dbError = new Error("Error ParseException trying to parse data from server", System.currentTimeMillis() / 1000);
						db.addError(dbError);
					}
				}
				
			};
			
			t.start();
			controller.sendMessage(RESPONSE_UPDATE_STARTED);
			// controller.notifyOutboxHandlers(C_UPDATE_STARTED, 0, 0, null);
		}
		else
		{
			controller.sendMessage(RESPONSE_UPDATE_FAILED);
			// controller.notifyOutboxHandlers(C_UPDATE_FAILED, 0, 0, null);
			
			Error dbError = new Error("Update failed. User tried to update without being log in", System.currentTimeMillis() / 1000);
			db.addError(dbError);
		}
	}
	

	/**
	 * Metodo para actualizar los valores de una alerta.
	 * 
	 * @param alert
	 *            Alerta a actualizar (se usa para identificarla en la cache de
	 *            datos su id)
	 */
	public void updateAlert(Alert alert)
	{
		db.refreshAlert(alert);
		controller.sendMessage(RESPONSE_ALERT_SAVED);
	}
}
