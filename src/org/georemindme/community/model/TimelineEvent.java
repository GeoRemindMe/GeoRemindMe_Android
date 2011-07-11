package org.georemindme.community.model;

import java.io.Serializable;

public class TimelineEvent implements Serializable
{

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 8293684174380700361L;
	
	private long id;
	private long date;
	private String message;
	private String user_identifier;
	private long id_obj;
	
	public TimelineEvent()
	{
		this(0,0,"","",0);
	}
	
	public TimelineEvent(long id, long date, String message, String user_identifier)
	{
		this(id, date, message, user_identifier, 0);
	}
	
	public TimelineEvent(long id, long date, String message, String user_identifier, long id_obj)
	{
		this.id = id;
		this.date = date;
		this.message = message;
		this.user_identifier = user_identifier;
		this.id_obj = id_obj;
	}
	
	public void setId(long id)
	{
		this.id = id;
	}
	
	public long getId()
	{
		return id;
	}
	
	public void setDate(long date)
	{
		this.date = date;
	}
	
	public long getDate()
	{
		return date;
	}
	
	public void setMessage(String message)
	{
		this.message = message;
	}
	
	public String getMessage()
	{
		return message;
	}
	
	public void setUserIdentifier(String user_identifier)
	{
		this.user_identifier = user_identifier;
	}
	
	public String getUserIdentifier()
	{
		return user_identifier;
	}
	
	public void setIdObj(long idObj)
	{
		this.id_obj = idObj;
	}
	
	public long getIdObj()
	{
		return id_obj;
	}
}
