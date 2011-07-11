package org.georemindme.community.model;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;


public class TimelinePage
{
	private List<TimelineEvent>			container;
	
	private ListIterator<TimelineEvent>	position;
	
	private static final int			PAGE_SIZE	= 42;
	
	
	public TimelinePage()
	{
		container = new ArrayList<TimelineEvent>(PAGE_SIZE);
		position = container.listIterator();
	}
	

	public TimelinePage(TimelineEvent event)
	{
		this();
		container.add(event);
	}
	

	public TimelinePage(List<TimelineEvent> list)
	{
		container = new ArrayList<TimelineEvent>(list);
	}
	

	public void cleanPage()
	{
		container.clear();
	}
	

	public TimelineEvent getNextTimelineEvent()
	{
		try
		{
			return position.next();
		}
		catch (NoSuchElementException e)
		{
			return null;
		}
	}
	
	public TimelineEvent getPreviousTimelineEvent()
	{
		try
		{
			return position.previous();
		}
		catch(NoSuchElementException e)
		{
			return null;
		}
	}
	
	public TimelineEvent getTimelineEventAtPosition(int position)
	{
		try
		{
			return container.get(position);
		}
		catch(IndexOutOfBoundsException e)
		{
			return null;
		}
	}
	
	public void queueTimelineEvent(TimelineEvent timelineEvent)
	{
		container.add(timelineEvent);
	}
	
	public List<TimelineEvent> getContainer()
	{
		return container;
	}
	
}
