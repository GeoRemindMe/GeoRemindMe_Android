package org.georemindme.community.model;

import java.util.ArrayList;
import java.util.List;

public class AlertList
{
	private List<Long> container;
	
	public AlertList()
	{
		container = new ArrayList<Long>();
	}
	
	public void addAlert(long id)
	{
		container.add(id);
	}
	
	public void addAlert(Alert a)
	{
		container.add(a.getId());
	}
	
	public long getAlertIdAtPosition(int position)
	{
		try
		{
			return container.get(position);
		}
		catch(IndexOutOfBoundsException e)
		{
			return -1;
		}
	}
}
