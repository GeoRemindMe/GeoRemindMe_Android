package org.georemindme.community.model;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.util.Log;


public class Time implements Serializable
{
	private int		year, month, dayofmonth, hour, minute;
	private boolean	undefined;
	
	
	public Time(int year, int month, int dayofmonth, int hour, int minute)
	{
		undefined = false;
		
		this.year = year;
		this.month = month;
		this.dayofmonth = dayofmonth;
		this.hour = hour;
		this.minute = minute;
	}
	

	public Time()
	{
		setUndefined();
	}
	
	public Time(long timestamp)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timestamp * 1000);
		
		year = calendar.get(Calendar.YEAR);
		month = calendar.get(Calendar.MONTH) + 1;
		dayofmonth = calendar.get(Calendar.DAY_OF_MONTH);
		hour = calendar.get(Calendar.HOUR_OF_DAY);
		minute = calendar.get(Calendar.MINUTE);
		
		undefined = false;
	}

	public boolean isUndefined()
	{
		return undefined;
	}
	

	public int getYear()
	{
		return year;
	}
	

	public int getMonth()
	{
		return month;
	}
	

	public int getDayOfMonth()
	{
		return dayofmonth;
	}
	

	public int getHour()
	{
		return hour;
	}
	

	public int getMinute()
	{
		return minute;
	}
	

	public long getUnixTime()
	{
		if(!undefined)
		{
			GregorianCalendar gC = new GregorianCalendar(year, month, dayofmonth, hour, minute);
			return gC.getTime().getTime() / 1000;
		}
		
		return 0;
	}
	
	public String log()
	{
		StringBuilder stb = new StringBuilder();
		if (!undefined)
			stb.append(year + "/" + month + "/" + dayofmonth + " (" + hour
					+ ":" + minute + ")");
		else
			stb.append("Time is undefined");
		return stb.toString();
	}
	

	public void setUndefined()
	{
		undefined = true;
		year = month = dayofmonth = hour = minute = 0;
	}
}
