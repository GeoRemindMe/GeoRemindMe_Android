package org.georemindme.community.view.adapters;


import org.georemindme.community.R;
import org.georemindme.community.controller.GeoRemindMe;
import org.georemindme.community.model.Database;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;


public class AlertAdapter extends SimpleCursorAdapter
{
	private Cursor		c;
	private Context		context;
	private long		serverID;
	
	private CheckBox	cbDone;
	private Database db;
	
	public AlertAdapter(Context context, int layout, Cursor c, String[] from,
			int[] to, Database db)
	{
		super(context, layout, c, from, to);
		// TODO Auto-generated constructor stub
		
		this.db = db;
		this.c = c;
		this.context = context;
	}
	

	public View getView(int pos, View inView, ViewGroup parentGroup)
	{
		View v = inView;
		
		if (v == null)
		{
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.alert_list_item, null);
		}
		
		if (c.moveToPosition(pos))
		{
			String name = c.getString(c.getColumnIndex(Database.ALERT_NAME));
			String description = c.getString(c.getColumnIndex(Database.ALERT_DESCRIPTION));
			
			TextView tvName = (TextView) v.findViewById(R.id.alert_name);
			TextView tvDescription = (TextView) v.findViewById(R.id.alert_description);
			cbDone = (CheckBox) v.findViewById(R.id.alert_done);
			serverID = c.getLong(c.getColumnIndex(Database.SERVER_ID));
			cbDone.setOnClickListener(new ClickGesture(serverID, cbDone, db));
			
			tvName.setText(name);
			tvDescription.setText(description);
		}
		
		return v;
	}
	

	private class ClickGesture implements OnClickListener
	{
		private long serverID;
		private CheckBox cbDone;
		
		ClickGesture(long serverID, CheckBox cbDone, Database db)
		{
			this.serverID = serverID;
			this.cbDone = cbDone;
		}
		
		@Override
		public void onClick(View v)
		{
			// TODO Auto-generated method stub
			Log.v("Setting done", serverID + "");
			db.setAlertDone(serverID, cbDone.isChecked());
		}
		
	}
	
}
