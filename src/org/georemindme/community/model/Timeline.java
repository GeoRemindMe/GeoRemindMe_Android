package org.georemindme.community.model;

public class Timeline
{
	private TimelinePage page;
	private int page_index;
	private long query_id;
	
	
	public Timeline()
	{
		page = new TimelinePage();
		page_index = 1;
		query_id = 0;
	}
	
	public Timeline(TimelineEvent timelineEvent)
	{
		page = new TimelinePage(timelineEvent);
		page_index = 1;
		query_id = 0;
	}
	
	public Timeline(TimelinePage timelinePage)
	{
		page = timelinePage;
		page_index = 1;			//??
		query_id = 0;			//??
	}
	
	public void addNewTimelinePage(TimelinePage timelinePage)
	{
		setTimelinePageAtPosition(timelinePage, page_index + 1);
	}
	
	public TimelineEvent getTimelineEventAtPosition(int position)
	{
		return page.getTimelineEventAtPosition(position);
	}
	
	public TimelineEvent getNextTimelineEvent()
	{
		return page.getNextTimelineEvent();
	}
	
	public TimelineEvent getPreviousTimelineEvent()
	{
		return page.getNextTimelineEvent();
	}
	
	public int getPageIndex()
	{
		return page_index;
	}
	
	public TimelinePage getActualTimelinePage()
	{
		return page;
	}
	
	public long getTimelineId()
	{
		return query_id;
	}
	
	public void setTimelineId(long id)
	{
		query_id = id;
	}
	
	public boolean isCached()
	{
		if(query_id == 0)
			return false;
		return true;
	}
	
	public void setTimelinePageAtPosition(TimelinePage timelinePage, int position)
	{
		page.cleanPage();
		page = timelinePage;
		page_index = position;
	}
}
