package org.georemindme.community.model;

public class User
{
	private String name;
	private String pass;
	
	public User()
	{
		this("no name", "no pass");
	}
	
	public User(String n, String p)
	{
		name = n;
		pass = p;
	}

	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @return the pass
	 */
	public String getPass()
	{
		return pass;
	}

	/**
	 * @param pass the pass to set
	 */
	public void setPass(String pass)
	{
		this.pass = pass;
	}
	
	
}
