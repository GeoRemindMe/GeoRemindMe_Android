package org.georemindme.community.view.adapters;


import org.georemindme.community.R;
import org.georemindme.community.model.Timeline;
import org.georemindme.community.model.TimelineEvent;
import org.georemindme.community.model.TimelinePage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


public class TimelineAdapter extends ArrayAdapter
{
	private Context		context;
	private TimelinePage	timelinePage;
	
	
	public TimelineAdapter(Context context, int textViewResourceId,
			TimelinePage timelinePage)
	{
		super(context, textViewResourceId, timelinePage.getContainer());
		// TODO Auto-generated constructor stub
		
		this.context = context;
		this.timelinePage = timelinePage;
	}
	

	public View getView(int position, View convertView, ViewGroup parent)
	{
		View v = convertView;
		
		if (v == null)
		{
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.timeline_list_item, parent, false);
		}
		
		TimelineEvent event = timelinePage.getTimelineEventAtPosition(position);
		
		TextView name = (TextView) v.findViewById(R.id.timeline_list_item_user);
		TextView message = (TextView) v.findViewById(R.id.timeline_list_item_message);
		TextView date = (TextView) v.findViewById(R.id.timeline_list_item_date);
		
		name.setText(event.getUserIdentifier());
		message.setText(event.getMessage());
		date.setText("" + event.getDate());
		
		return v;
	}
	
}
