package org.georemindme.community.model;


import java.io.Serializable;


public class Alert implements Serializable
{
	private long	id_server;
	private long	id;
	private long	done_when;
	private long	ends;
	private long	starts;
	private long	created;
	private boolean	done;
	private String	name;
	private boolean	active;
	private long	modified;
	private double	latitude;
	private double	longitude;
	private String	description;
	
	
	public Alert(long id, long id_server, long done_when, long ends,
			long starts, long created, boolean done, String name,
			String description, boolean active, long modified, double latitude,
			double longitude)
	{
		this.id = id;
		this.id_server = id_server;
		this.done_when = done_when;
		this.ends = ends;
		this.starts = starts;
		this.created = created;
		this.done = done;
		this.name = name;
		this.description = description;
		this.active = active;
		this.modified = modified;
		this.latitude = latitude;
		this.longitude = longitude;
	}
	

	public Alert()
	{
		this.id = this.id_server = this.done_when = this.ends = this.starts = this.created = this.modified = 0;
		this.latitude = this.longitude = 0;
		this.done = this.active = false;
		this.name = this.description = "";
	}
	

	/**
	 * @return the id_server
	 */
	public long getIdServer()
	{
		return id_server;
	}
	

	/**
	 * @param id_server
	 *            the id_server to set
	 */
	public void setIdServer(long id)
	{
		this.id_server = id;
	}
	

	/**
	 * @return the id
	 */
	public long getId()
	{
		return id;
	}
	

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(long id)
	{
		this.id = id;
	}
	

	/**
	 * @return the done_when
	 */
	public long getDone_when()
	{
		return done_when;
	}
	

	/**
	 * @param done_when
	 *            the done_when to set
	 */
	public void setDone_when(long done_when)
	{
		this.done_when = done_when;
	}
	

	/**
	 * @return the ends
	 */
	public long getEnds()
	{
		return ends;
	}
	

	/**
	 * @param ends
	 *            the ends to set
	 */
	public void setEnds(long ends)
	{
		this.ends = ends;
	}
	

	/**
	 * @return the starts
	 */
	public long getStarts()
	{
		return starts;
	}
	

	/**
	 * @param starts
	 *            the starts to set
	 */
	public void setStarts(long starts)
	{
		this.starts = starts;
	}
	

	/**
	 * @return the created
	 */
	public long getCreated()
	{
		return created;
	}
	

	/**
	 * @param created
	 *            the created to set
	 */
	public void setCreated(long created)
	{
		this.created = created;
	}
	

	/**
	 * @return the done
	 */
	public boolean isDone()
	{
		return done;
	}
	

	/**
	 * @param done
	 *            the done to set
	 */
	public void setDone(boolean done)
	{
		this.done = done;
		if(done == true)
		{
			this.setDone_when(System.currentTimeMillis() / 1000);
		}
	}
	

	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}
	

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}
	

	public String getDescription()
	{
		return description;
	}
	

	public void setDescription(String description)
	{
		this.description = description;
	}
	

	/**
	 * @return the active
	 */
	public boolean isActive()
	{
		return active;
	}
	

	/**
	 * @param active
	 *            the active to set
	 */
	public void setActive(boolean active)
	{
		this.active = active;
	}
	

	/**
	 * @return the modified
	 */
	public long getModified()
	{
		return modified;
	}
	

	/**
	 * @param modified
	 *            the modified to set
	 */
	public void setModified(long modified)
	{
		this.modified = modified;
	}
	

	/**
	 * @return the latitude
	 */
	public double getLatitude()
	{
		return latitude;
	}
	

	/**
	 * @param latitude
	 *            the latitude to set
	 */
	public void setLatitude(double latitude)
	{
		this.latitude = latitude;
	}
	

	/**
	 * @return the longitude
	 */
	public double getLongitude()
	{
		return longitude;
	}
	

	/**
	 * @param longitude
	 *            the longitude to set
	 */
	public void setLongitude(double longitude)
	{
		this.longitude = longitude;
	}
	
}
