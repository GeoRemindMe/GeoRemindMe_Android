package org.georemindme.community.model;


import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.georemindme.community.R;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


/**
 * This class is the database controller. It is going to react like a
 * 'singleton'. Every query we need is going to be a method in here.
 * 
 * @author fj.
 * 
 */
public class Database
{
	// ///////////////////////////////////////////////////////////
	// ///////////////////////////////////////////////////////////
	// ///////////////////////////////////////////////////////////
	/**
	 * This class is a private helper to manage the internal database. It is a
	 * must.
	 * 
	 * @author fj.
	 * 
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper
	{
		
		public DatabaseHelper(Context context)
		{
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
		

		public DatabaseHelper(Context context, String name,
				CursorFactory factory, int version)
		{
			super(context, name, factory, version);
			// TODO Auto-generated constructor stub
		}
		

		public void deleteTables(SQLiteDatabase db)
		{
			db.execSQL("DROP TABLE IF EXISTS " + USER_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + ALERT_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + ERROR_TABLE);
		}
		

		@Override
		public void onCreate(SQLiteDatabase db)
		{
			// TODO Auto-generated method stub
			db.execSQL(CREATE_USER_TABLE);
			db.execSQL(CREATE_ALERT_TABLE);
			db.execSQL(CREATE_ERROR_TABLE);
		}
		

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
		{
			// TODO Auto-generated method stub
			deleteTables(db);
			onCreate(db);
		}
		
	}
	
	private static Database	instance;
	
	public static final String	DATABASE_ACTION_ALERTS_UPDATED			= "DATABASE_ACTION_ALERTS_UPDATED";
	public static final String	DATABASE_ACTION_ALERT_DONE				= "DATABASE_ACTION_ALERT_DONE";
	public static final String	DATABASE_ACTION_USER_SET				= "DATABASE_ACTION_USER_SET";
	
	public static final String	DATABASE_ACTION_DATA_FLUSH				= "DATABASE_ACTION_DATA_FLUSH";
	public static final String	DATABASE_ACTION_DATA_SYNCHRONIZED		= "DATABASE_ACTION_DATA_SYNCHRONIZED";
	public static final String	DATABASE_ACTION_DATA_IS_SYNCHRONIZING	= "DATABASE_ACTION_DATA_IS_SYNCHRONIZING";
	
	/**
	 * Static fields representing database's version and database's name.
	 * 
	 * @author fj.
	 */
	private static final int	DATABASE_VERSION						= 3;
	private static final String	DATABASE_NAME							= "grm_ce_database";
	/**
	 * Static fields which represent database's tables' fields.
	 * 
	 * @author fj.
	 */
	
	// Common fields
	public static final String	_ID										= "_id";
	
	// Alert table fields.
	public static final String	SERVER_ID								= "server_id";
	public static final String	ALERT_NAME								= "alert_name";
	public static final String	ALERT_START								= "alert_start";
	public static final String	ALERT_END								= "alert_end";
	public static final String	ALERT_DESCRIPTION						= "alert_description";
	public static final String	ALERT_CREATE							= "alert_create";
	public static final String	ALERT_MODIFY							= "alert_modify";
	public static final String	ALERT_DONE								= "alert_done";
	public static final String	ALERT_PRIORITY							= "alert_priority";
	public static final String	ALERT_ACTIVE							= "alert_active";
	public static final String	ALERT_RANGE								= "alert_range";
	public static final String	ALERT_X									= "alert_x";
	public static final String	ALERT_Y									= "alert_y";
	public static final String	ALERT_DELETED							= "alert_deleted";
	
	// Error table fields.
	public static final String	ERROR_MESSAGE							= "error_message";
	public static final String	ERROR_DATE								= "error_date";
	
	// User table fields.
	public static final String	USER_CREATION							= "user_creation";
	public static final String	USER_EMAIL								= "user_email";
	public static final String	USER_PASSWORD							= "user_password";
	public static final String	USER_LASTSYNC							= "user_lastsync";
	
	// Point table fields.
	public static final String	LATITUDE								= "latitude";
	public static final String	LONGITUDE								= "longitude";
	public static final String	POINT_NAME								= "point_name";
	public static final String	POINT_BOOKMARKED						= "point_bookmarked";
	public static final String	POINT_PHYSICALADDRESS					= "point_physicaladdress";
	
	/**
	 * Static fields which represent database's tables' names.
	 * 
	 * @author fj.
	 */
	public static final String	POINT_TABLE								= "point_table";
	public static final String	USER_TABLE								= "user_table";
	public static final String	ALERT_TABLE								= "alert_table";
	public static final String	ERROR_TABLE								= "error_table";
	
	/**
	 * Static fields to make easier database tables creation.
	 * 
	 * @author fj.
	 */
	private static final String	CREATE_USER_TABLE						= "create table "
																				+ USER_TABLE
																				+ "( "
																				+ USER_CREATION
																				+ " real not null, "
																				+ USER_EMAIL
																				+ " text not null unique, "
																				+ USER_PASSWORD
																				+ " text not null, "
																				+ USER_LASTSYNC
																				+ " real, "
																				+ _ID
																				+ " integer primary key autoincrement"
																				+ ")";
	private static final String	CREATE_ALERT_TABLE						= "create table "
																				+ ALERT_TABLE
																				+ "("
																				+ _ID
																				+ " integer primary key autoincrement, "
																				+ SERVER_ID
																				+ " real not null, "
																				+ ALERT_START
																				+ " real not null, "
																				+ ALERT_END
																				+ " real not null, "
																				+ ALERT_DESCRIPTION
																				+ " text, "
																				+ ALERT_CREATE
																				+ " real not null, "
																				+ ALERT_MODIFY
																				+ " real not null, "
																				+ ALERT_ACTIVE
																				+ " integer not null default 1, "
																				+ ALERT_DONE
																				+ " real not null, "
																				+ ALERT_NAME
																				+ " text not null, "
																				+ LATITUDE
																				+ " real not null, "
																				+ LONGITUDE
																				+ " real not null, "
																				+ ALERT_DELETED
																				+ " real not null default 0"
																				+ ")";
	
	private static final String	CREATE_ERROR_TABLE						= "create table "
																				+ ERROR_TABLE
																				+ "("
																				+ _ID
																				+ " integer primary key autoincrement, "
																				+ ERROR_MESSAGE
																				+ " text not null, "
																				+ ERROR_DATE
																				+ " real not null"
																				+ ")";
	public static Database getDatabaseInstance(Context context)
	{
		if (instance == null)
			instance = new Database(context);
		
		return instance;
	}
	private final Context		context;
	private DatabaseHelper		dbHelper;
	
	
	private SQLiteDatabase		db;
	

	private Database(Context ctx)
	{
		context = ctx;
		dbHelper = new DatabaseHelper(context);
	}
	

	public synchronized void addAlert(Alert a)
	{
		this.open();
		
		ContentValues cv = new ContentValues();
		
		cv.put(SERVER_ID, a.getIdServer());
		cv.put(ALERT_START, a.getStarts());
		cv.put(ALERT_END, a.getEnds());
		cv.put(ALERT_DESCRIPTION, a.getDescription());
		cv.put(ALERT_CREATE, a.getCreated());
		cv.put(ALERT_MODIFY, a.getModified());
		if (a.isDone())
		{
			cv.put(ALERT_DONE, a.getDone_when());
			// Log.v("ALERT_DONE", "La alerta est‡ hecha");
		}
		else
		{
			cv.put(ALERT_DONE, 0);
			// Log.v("ALERT_DONE", "La alerta est‡ sin hacer");
		}
		
		if (a.isActive())
		{
			cv.put(ALERT_ACTIVE, 1);
		}
		else
		{
			cv.put(ALERT_ACTIVE, 0);
		}
		
		cv.put(ALERT_NAME, a.getName());
		cv.put(LATITUDE, a.getLatitude());
		cv.put(LONGITUDE, a.getLongitude());
		
		db.insert(ALERT_TABLE, null, cv);
		
		this.close();
	}
	
	public synchronized void addError(Error e)
	{
		this.open();
		
		ContentValues cv = new ContentValues();
		cv.put(ERROR_MESSAGE, e.getMessage());
		cv.put(ERROR_DATE, e.getDate());
		
		db.insert(ERROR_TABLE, null, cv);
		
		this.close();
	}
	
	public synchronized Cursor getAllErrors()
	{
		this.open();
		
		Cursor c = null;
		
		String sql = "Select * from " + ERROR_TABLE;
		
		c = db.rawQuery(sql, null);
		return c;
	}

	public synchronized void changeAlertActive(boolean active, long id)
	{
		this.open();
		
		ContentValues cv = new ContentValues();
		
		if (active)
		{
			cv.put(ALERT_ACTIVE, 1);
		}
		else
		{
			cv.put(ALERT_ACTIVE, 0);
		}
		
		Date now = new Date();
		
		long time = now.getTime();
		time /= 1000;
		
		cv.put(ALERT_MODIFY, time);
		
		db.update(ALERT_TABLE, cv, _ID + " = " + id, null);
		
		this.close();
	}
	

	public void close()
	{
		dbHelper.close();
	}


	private synchronized void deleteAlert(Alert a)
	{
		this.open();
		db.delete(ALERT_TABLE, SERVER_ID + " = " + a.getIdServer(), null);
		this.close();
	}
	

	public synchronized void deleteAlerts()
	{
		this.open();
		
		db.delete(ALERT_TABLE, ALERT_DELETED + " > 0", null);
		
		this.close();
	}
	

	public synchronized void deleteAlerts(List<Alert> list)
	{
		int limit = list.size();
		for (int i = 0; i < limit; i++)
		{
			deleteAlert(list.get(i));
		}
	}
	

	public synchronized void flush()
	{
		removeUser();
		removeAlerts();
		removeErrors();
	}
	

	public synchronized Cursor getAlert(double lat, double lng)
	{
		this.open();
		String sql = "Select * from " + ALERT_TABLE + " where " + LATITUDE
				+ "=" + lat + " AND " + LONGITUDE + " = " + lng;
		
		Cursor c = db.rawQuery(sql, null);
		
		// Log.v("DATABASE - getAlert", sql);
		
		this.close();
		return c;
	}
	

	public synchronized Alert getAlertByServerID(long serverID)
	{
		this.open();
		String sql = "Select * from " + ALERT_TABLE + " where " + SERVER_ID
				+ " = " + serverID;
		// Log.v("getAlertByServerID", sql);
		Cursor c = db.rawQuery(sql, null);
		if (c != null)
		{
			c.moveToFirst();
			boolean done_b;
			String name = c.getString(c.getColumnIndex(Database.ALERT_NAME));
			String description = c.getString(c.getColumnIndex(Database.ALERT_DESCRIPTION));
			long start = c.getInt(c.getColumnIndex(Database.ALERT_START));
			long end = c.getInt(c.getColumnIndex(Database.ALERT_END));
			long done = c.getInt(c.getColumnIndex(Database.ALERT_DONE));
			if (done == 0)
				done_b = false;
			else
				done_b = true;
			
			double lat = c.getDouble(c.getColumnIndex(Database.LATITUDE));
			double lng = c.getDouble(c.getColumnIndex(Database.LONGITUDE));
			
			// Log.v("Antes de crear la alerta", "");
			Alert a = new Alert(0, 0l, 0l, end, start, 0l, done_b, name, description, true, 0, lat, lng);
			c.close();
			return a;
		}
		this.close();
		return null;
	}
	

	public synchronized Cursor getAlertsCoordinates()
	{
		this.open();
		String sql = "Select " + LATITUDE + ", " + LONGITUDE + " from "
				+ ALERT_TABLE;
		
		Cursor c = db.rawQuery(sql, null);
		this.close();
		return c;
	}
	

	public synchronized Cursor getAlertsDone()
	{
		this.open();
		String sql = "Select * from " + ALERT_TABLE + " where " + ALERT_DONE
				+ " > 0 order by " + ALERT_DONE;
		// Log.w("DATABASE", sql);
		Cursor c = db.rawQuery(sql, null);
		// this.close();
		return c;
	}
	

	public synchronized Cursor getAlertsInactive()
	{
		this.open();
		String sql = "Select * from " + ALERT_TABLE + " where " + ALERT_DONE
				+ " = 0 and " + ALERT_ACTIVE + " = 0";
		// Log.w("DATABASE", sql);
		Cursor c = db.rawQuery(sql, null);
		// this.close();
		return c;
	}
	

	public synchronized Cursor getAlertsToDelete()
	{
		this.open();
		
		String sql = "Select * from " + ALERT_TABLE + " where " + ALERT_DELETED
				+ " > 0";
		
		return db.rawQuery(sql, null);
	}
	

	public synchronized Cursor getAlertsToDeleteInServer()
	{
		this.open();
		
		String sql = "Select * from " + ALERT_TABLE + " where " + ALERT_DELETED
				+ " > 0 and " + SERVER_ID + " > 0";
		
		return db.rawQuery(sql, null);
	}
	

	public synchronized Cursor getAlertsToNotify(double latE6, double lngE6,
			int meters)
	{
		this.open();
		Cursor c = null;
		double latitudeOffset = (meters / 110574.61);
		double longitudeOffset = (meters / 111302.62);
		
		// Log.v("Latitude offset", latitudeOffset + "");
		// Log.v("Longitude offset", longitudeOffset + "");
		
		// Log.v("Latitude", latE6 + "");
		// Log.v("Longitude", lngE6 + "");
		
		// PROBABLEMENTE ESTE METODO FALLE. TENDRƒ QUE USAR METODOS MATEMçTICOS
		// DE SQLITE3
		
		String sql = "Select *, " + "(" + latE6 + " - " + LATITUDE + ") * ("
				+ latE6 + " - " + LATITUDE + ") + " + "(" + lngE6 + " - "
				+ LONGITUDE + ") * (" + lngE6 + " - " + LONGITUDE
				+ ") as distance " + "from " + ALERT_TABLE + " where "
				+ ALERT_DONE + "= 0 AND " + ALERT_ACTIVE + " = 1 AND "
				+ (latitudeOffset + latE6) + " > " + LATITUDE + " and "
				+ LATITUDE + " > " + (-1 * latitudeOffset + latE6) + " and "
				+ (longitudeOffset + lngE6) + " > " + LONGITUDE + " and "
				+ LONGITUDE + " > " + (-1 * longitudeOffset + lngE6) + " and "
				+ ALERT_START + " <= strftime('%s','now') and (" + ALERT_END
				+ " >= strftime('%s', 'now') or " + ALERT_END + " = 0) AND "
				+ ALERT_DELETED + "= 0 order by distance";
		
		// Log.v("SQL ALERT QUERY", sql);
		c = db.rawQuery(sql, null);
		// this.close();
		return c;
	}
	

	public synchronized Cursor getAlertsUndone()
	{
		this.open();
		
		/*
		 * String sql = "Select * from " + ALERT_TABLE + " where " + ALERT_DONE
		 * + " = 0 and " + ALERT_ACTIVE + " = 1";
		 */
		String sql = "Select * from " + ALERT_TABLE + " where " + ALERT_DONE
				+ " = 0 and " + ALERT_DELETED + " = 0";
		// Log.w("DATABASE", sql);
		Cursor c = db.rawQuery(sql, null);
		// this.close();
		return c;
	}
	

	public synchronized Cursor getAlertsUndoneCoordinates()
	{
		this.open();
		String sql = "Select " + SERVER_ID + ", " + LATITUDE + ", " + LONGITUDE
				+ " from " + ALERT_TABLE + " where " + ALERT_DONE + " = 0";
		
		Cursor c = db.rawQuery(sql, null);
		this.close();
		return c;
	}
	
	public Alert getAlertWithID(long id)
	{
		this.open();
		String sql = "Select * from " + ALERT_TABLE + " where " + _ID
				+ " = " + id;
		// Log.v("getAlertByServerID", sql);
		Cursor c = db.rawQuery(sql, null);
		if (c != null)
		{
			c.moveToFirst();
			boolean done_b;
			String name = c.getString(c.getColumnIndex(Database.ALERT_NAME));
			String description = c.getString(c.getColumnIndex(Database.ALERT_DESCRIPTION));
			long start = c.getInt(c.getColumnIndex(Database.ALERT_START));
			long end = c.getInt(c.getColumnIndex(Database.ALERT_END));
			long done = c.getInt(c.getColumnIndex(Database.ALERT_DONE));
			if (done == 0)
				done_b = false;
			else
				done_b = true;
			
			double lat = c.getDouble(c.getColumnIndex(Database.LATITUDE));
			double lng = c.getDouble(c.getColumnIndex(Database.LONGITUDE));
			
			// Log.v("Antes de crear la alerta", "");
			Alert a = new Alert(0, 0l, 0l, end, start, 0l, done_b, name, description, true, 0, lat, lng);
			c.close();
			return a;
		}
		this.close();
		return null;
	}
	
	public Cursor getAlertsWithID(ArrayList ids)
	{
		this.open();
		Cursor c = null;
		
		StringBuffer stb = new StringBuffer("Select * from " + ALERT_TABLE
				+ " where ");
		
		for (int i = 0; i < ids.size() - 1; i++)
		{
			stb.append(SERVER_ID + " = " + ids.get(i) + " or ");
		}
		
		if (ids.size() > 0)
			stb.append(SERVER_ID + " = " + ids.get(ids.size() - 1));
		
		// Log.v("Consulta", stb.toString());
		synchronized (this)
		{
			c = db.rawQuery(stb.toString(), null);
		}
		this.close();
		return c;
	}
	

	public synchronized Cursor getModifiedAlerts(long sync)
	{
		this.open();
		String sql = "Select * from " + ALERT_TABLE + " where " + ALERT_MODIFY
				+ " > " + sync;
		
		Cursor c = db.rawQuery(sql, null);
		// Log.v("DATABASE - getmodifiedAlerts", c.getCount() + "");
		// this.close();
		return c;
	}
	

	public synchronized Cursor getNearestAlertsUndone(double latE6,
			double lngE6, int meters)
	{
		this.open();
		Cursor c = null;
		double latitudeOffset = (meters / 110574.61);
		double longitudeOffset = (meters / 111302.62);
		
		// Log.v("Latitude offset", latitudeOffset + "");
		// Log.v("Longitude offset", longitudeOffset + "");
		
		// Log.v("Latitude", latE6 + "");
		// Log.v("Longitude", lngE6 + "");
		
		// PROBABLEMENTE ESTE METODO FALLE. TENDRƒ QUE USAR METODOS MATEMçTICOS
		// DE SQLITE3
		/*
		 * String sql = "Select *, " + "(" + latE6 + " - " + POINT_X + ") * (" +
		 * latE6 + " - " + POINT_X + ") + " + "(" + lngE6 + " - " + POINT_Y +
		 * ") * (" + lngE6 + " - " + POINT_Y + ") as distance " + "from " +
		 * ALERT_TABLE + " where " + ALERT_DONE + "= 0 AND " + (latitudeOffset +
		 * latE6) + " > " + POINT_X + " and " + POINT_X + " > " + (-1 *
		 * latitudeOffset + latE6) + " and " + (longitudeOffset + lngE6) + " > "
		 * + POINT_Y + " and " + POINT_Y + " > " + (-1 * longitudeOffset +
		 * lngE6) + " order by distance";
		 */
		String sql = "Select *, " + "(" + latE6 + " - " + LATITUDE + ") * ("
				+ latE6 + " - " + LATITUDE + ") + " + "(" + lngE6 + " - "
				+ LONGITUDE + ") * (" + lngE6 + " - " + LONGITUDE
				+ ") as distance " + "from " + ALERT_TABLE + " where "
				+ ALERT_DONE + "= 0 and " + ALERT_DELETED
				+ " = 0 order by distance";
		
		Log.v("Latitude offset: ", latitudeOffset + "");
		Log.v("Longitude offset: ", longitudeOffset + "");
		Log.v("SQL ALERT QUERY", sql);
		c = db.rawQuery(sql, null);
		// this.close();
		return c;
	}
	

	public synchronized User getUser()
	{
		this.open();
		User user = null;
		
		String sql = "Select " + USER_EMAIL + ", " + USER_PASSWORD + " from "
				+ USER_TABLE;
		
		Cursor c = db.rawQuery(sql, null);
		
		if (c != null)
		{
			if (c.moveToFirst())
			{
				user = new User(c.getString(c.getColumnIndex(USER_EMAIL)), c.getString(c.getColumnIndex(USER_PASSWORD)));
			}
		}
		
		c.close();
		this.close();
		return user;
	}
	

	public synchronized long lastsync()
	{
		this.open();
		// TODO Auto-generated method stub
		long result = 0;
		String sql = "Select " + USER_LASTSYNC + " from " + USER_TABLE;
		
		Cursor c = db.rawQuery(sql, null);
		if (c != null)
		{
			if (c.moveToFirst())
			{
				result = c.getLong(c.getColumnIndex(USER_LASTSYNC));
			}
			
			c.close();
		}
		
		this.close();
		return result;
	}
	

	public Database open() throws SQLException
	{
		db = dbHelper.getWritableDatabase();
		return this;
	}
	

	public synchronized void refreshAlert(Alert a)
	{
		this.open();
		
		ContentValues cv = new ContentValues();
		
		cv.put(SERVER_ID, a.getIdServer());
		cv.put(ALERT_START, a.getStarts());
		cv.put(ALERT_END, a.getEnds());
		cv.put(ALERT_DESCRIPTION, a.getDescription());
		cv.put(ALERT_CREATE, a.getCreated());
		cv.put(ALERT_MODIFY, a.getModified());
		if (a.isDone())
		{
			cv.put(ALERT_DONE, a.getDone_when());
			// Log.v("ALERT_DONE", "La alerta est‡ hecha");
		}
		else
		{
			cv.put(ALERT_DONE, 0);
			// Log.v("ALERT_DONE", "La alerta est‡ sin hacer");
		}
		
		if (a.isActive())
		{
			cv.put(ALERT_ACTIVE, 1);
			Log.v("ALERT_DONE", "La alerta est‡ activa");
		}
		else
		{
			cv.put(ALERT_ACTIVE, 0);
			Log.v("ALERT_DONE", "La alerta est‡ inactiva");
		}
		
		cv.put(ALERT_NAME, a.getName());
		cv.put(LATITUDE, a.getLatitude());
		cv.put(LONGITUDE, a.getLongitude());
		
		if (db.update(ALERT_TABLE, cv, SERVER_ID + " = ?", new String[] { ""
				+ a.getIdServer() }) == 0)
		{
			if (db.update(ALERT_TABLE, cv, _ID + " = ?", new String[] { ""
					+ a.getId() }) == 0)
			{
				db.insert(ALERT_TABLE, null, cv);
			}
		}
		
		this.close();
	}
	

	public void refreshAlerts(List<Alert> alertList)
	{
		for (Alert a : alertList)
		{
			refreshAlert(a);
		}
	}
	

	public synchronized void removeAlert(Alert a)
	{
		this.open();
		ContentValues cv = new ContentValues();
		Date now = new Date();
		long time = now.getTime();
		time /= 1000;
		cv.put(ALERT_DELETED, time);
		
		db.update(ALERT_TABLE, cv, _ID + " = " + a.getId(), null);
		
		this.close();
	}
	

	private synchronized void removeAlerts()
	{
		this.open();
		db.execSQL("Delete from " + ALERT_TABLE);
		this.close();
	}
	

	public synchronized void removeCreatedAlertsAndSynced()
	{
		this.open();
		
		String sql = " delete from " + ALERT_TABLE + " where " + SERVER_ID
				+ " = 0";
		
		db.execSQL(sql);
		
		this.close();
	}
	

	public void removeErrors()
	{
		// TODO Auto-generated method stub
		this.open();
		db.execSQL("Delete from " + ERROR_TABLE);
		this.close();
	}
	

	private synchronized void removeUser()
	{
		this.open();
		db.execSQL("Delete from " + USER_TABLE);
		// Log.v("deleteUser", "Executed!");
		this.close();
	}
	

	public synchronized void setAlertDone(long id, boolean done)
	{
		// Log.v("Setting alert done", "Start");
		this.open();
		ContentValues cv = new ContentValues();
		
		Date now = new Date();
		
		long time = now.getTime();
		time /= 1000;
		
		// Log.v("Setting alert done", "Time: " + time);
		
		if (done)
		{
			cv.put(ALERT_DONE, time);
		}
		else
		{
			cv.put(ALERT_DONE, 0);
		}
		cv.put(ALERT_MODIFY, time);
		
		db.update(ALERT_TABLE, cv, _ID + " = " + id, null);
		
		this.close();
		
		now = null;
		
	}
	

	public synchronized void setLastsync(long lastsync)
	{
		this.open();
		
		ContentValues cv = new ContentValues();
		
		cv.put(USER_LASTSYNC, lastsync);
		
		db.update(USER_TABLE, cv, null, null);
		
		this.close();
		
	}
	

	public synchronized void setUser(User u)
	{
		this.open();
		ContentValues cV = new ContentValues();
		cV.put(USER_EMAIL, u.getName().toLowerCase());
		cV.put(USER_PASSWORD, u.getPass());
		cV.put(USER_LASTSYNC, 0);
		
		Date now = new Date();
		cV.put(USER_CREATION, now.getTime() / 1000);
		
		try
		{
			if (db.update(USER_TABLE, cV, null, null) == 0)
			{
				db.delete(USER_TABLE, null, null);
				db.insert(USER_TABLE, null, cV);
				
				Log.i("User saved", "Successful");
			}
		}
		catch (Exception e)
		{
			Log.e("Saving user", e.getStackTrace().toString());
		}
		
		this.close();
	}
	
}
