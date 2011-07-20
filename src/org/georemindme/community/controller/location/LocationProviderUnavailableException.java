package org.georemindme.community.controller.location;


/**
 * Clase para reprensentar la excepci—n ocurrida al no poder acceder al
 * proveedor de localizaci—n.
 * 
 * @author franciscojavierfernandeztoro
 * @version 1.0
 */

@SuppressWarnings("serial")
public class LocationProviderUnavailableException extends Exception
{
	public LocationProviderUnavailableException()
	{
		super();
	}
}
