package org.georemindme.community.model;

import java.io.Serializable;
import java.util.Date;
import org.georemindme.community.R;

public class Error implements Serializable
{
	private static final long	serialVersionUID	= -1272290422040630062L;
	private String message;
	private long date;
	
	public Error()
	{
		message = "";
		date = 0;
	}
	
	public Error(String message)
	{
		this.message = message;
		date = System.currentTimeMillis() / 1000;
	}
	
	public Error(String message, Date date)
	{
		this.message = message;
		this.date = date.getTime() / 1000; // Divided by 1000 because I work with seconds, not miliseconds.
	}
	
	public Error(String message, long date)
	{
		this.message = message;
		this.date = date;
	}
	
	public String getMessage()
	{
		return message;
	}
	
	public long getDate()
	{
		return date;
	}
	
	public void setMessage(String message)
	{
		this.message = message;
	}
	
	public void setDate(long date)
	{
		this.date = date;
	}
	
	public void setDate(Date date)
	{
		this.date = date.getTime() / 1000;
	}
}
