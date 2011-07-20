package org.georemindme.community.controller.location;


import static org.georemindme.community.controller.ControllerProtocol.*;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.georemindme.community.controller.Controller;
import org.georemindme.community.controller.PreferencesController;

import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;


/**
 * Clase que representa el motor de localizaci�n de la aplicaci�n.
 * 
 * @author franciscojavierfernandeztoro
 * @version 1.0
 */
public class LocationServer
{
	/**
	 * Gestor de localizaci�n suministrado por el framework Android.
	 */
	private static LocationManager	locationManager;
	
	/**
	 * Unica instancia del LocationServer en el sistema. Patr�n de dise�o:
	 * Singleton.
	 */
	private static LocationServer	singleton	= null;
	
	
	/**
	 * Metodo para obtener la �nica instancia del location server que existe en
	 * el sistema.
	 * 
	 * @param controller
	 *            Controlador el cual recibir� mensajes del esta clase.
	 * @return Instancia local del LocationServer que existe.
	 */
	public static LocationServer getInstance(Controller controller)
	{
		if (singleton == null)
			singleton = new LocationServer(controller);
		
		return singleton;
	}
	
	/**
	 * Interfaz que escuchar� los cambios en el mejor proveedor de localizaci�n
	 * seleccionador por el sistema seg�n las preferencias establecidas por el
	 * usuario.
	 */
	private LocationListener	bestLocationListener;
	
	/**
	 * Nombre del mejor proveedor de localizaci�n encontrado en el sistema seg�n
	 * las preferencias establecidas por el usuario en las preferencias del
	 * sistema.
	 */
	private String				bestLocationProvider	= null;
	
	/**
	 * Controlador del sistema con el cual recibir� mensajes de esta clase.
	 */
	private Controller			controller;
	
	/**
	 * Valor que representa la distancia en metros a la cual se debe refrescar
	 * el proveedor de localizaci�n. Se obtiene de las preferencias del sistema.
	 */
	private int					distanceToRefresh;
	
	/**
	 * Ultima localizaci�n conocida por el sistema. No tiene porqu� estar
	 * vinculada al bestLocationListener.
	 */
	private Location			lastKnownLocation		= null;
	
	/**
	 * Listado de los proveedores de localizaci�n del sistema.
	 */
	private List<String>		locationProviders		= null;
	
	/**
	 * Interfaz que escuchar� cambios producidos en los proveedores de
	 * localizaci�n diferentes al bestLocationProvider.
	 */
	private LocationListener	temporalLocationListener;
	
	/**
	 * Valor del tiempo m�nimo en, minutos, con el cual se debe refrescar el
	 * proveedor de localizaci�n. Se obtiene de las preferencias del sistema.
	 */
	private int					timeToRefresh;
	
	/**
	 * Configuraci�n establecida por el usuario en las preferencias del sistema
	 * indicando las caracter�sticas deseadas para el proveedor de localizaci�n
	 * favorito.
	 */
	private Criteria			userCriteria			= null;
	
	
	/**
	 * Constructor el cual inicializa el motor de localizaci�n para su uso en el
	 * sistema.
	 * 
	 * @param controller
	 *            Controlador el cual recibir� mensajes del esta clase.
	 */
	private LocationServer(Controller controller)
	{
		this.controller = controller;
		Log.i("LOCATION SERVER", "Constructor");
		
		locationManager = (LocationManager) controller.getContext().getSystemService(Context.LOCATION_SERVICE);
		
		setTimeToRefresh();
		setDistanceToRefresh();
		
		bestLocationListener = new LocationListener()
		{
			// Cuando cambia la localizaci�n en el proveedor favorito, se pide
			// una actualizaci�n de la misma.
			@Override
			public void onLocationChanged(Location location)
			{
				// TODO Auto-generated method stub
				updateLocation(location);
			}
			

			// Si el proveedor favorito est� desabilitado, se piden
			// actualizaciones de localizaci�n al resto de
			// proveedores del sistema.
			@Override
			public void onProviderDisabled(String provider)
			{
				// TODO Auto-generated method stub
				locationManager.removeUpdates(temporalLocationListener);
				for (String s : locationProviders)
					locationManager.requestLocationUpdates(s, timeToRefresh, 0, temporalLocationListener);
			}
			

			// Si el proveedor favorito est� activo, se eliminan las
			// actulizaciones de localizaci�n
			// del resto de proveedores del sistema.
			@Override
			public void onProviderEnabled(String provider)
			{
				// TODO Auto-generated method stub
				locationManager.removeUpdates(temporalLocationListener);
			}
			

			// Cuando cambia el estado del proveedor favorito, si est�
			// temporalmente inalcanzable, se piden actualizaciones
			// al resto de proveedores del sistema; en caso de que se haya
			// vuelto a activar, se eliminan las actualizaciones
			// del resto de proveedores.
			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras)
			{
				// TODO Auto-generated method stub
				switch (status)
				{
					case LocationProvider.OUT_OF_SERVICE:

						break;
					case LocationProvider.TEMPORARILY_UNAVAILABLE:
						locationManager.removeUpdates(temporalLocationListener);
						for (String s : locationProviders)
							locationManager.requestLocationUpdates(s, timeToRefresh, distanceToRefresh, temporalLocationListener);
						break;
					case LocationProvider.AVAILABLE:
						locationManager.removeUpdates(temporalLocationListener);
						break;
				}
			}
			
		};
		
		temporalLocationListener = new LocationListener()
		{
			// Cuando cambia la localizacion en algunos de los proveedores no
			// favoritos, se pide una actualizaci�n.
			@Override
			public void onLocationChanged(Location location)
			{
				// TODO Auto-generated method stub
				updateLocation(location);
			}
			

			@Override
			public void onProviderDisabled(String provider)
			{
				// TODO Auto-generated method stub
				
			}
			

			@Override
			public void onProviderEnabled(String provider)
			{
				// TODO Auto-generated method stub
				
			}
			

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras)
			{
				// TODO Auto-generated method stub
				
			}
		};
		
	}
	

	/**
	 * M�todo para obtener la direcci�n f�sica aproximada a un par de
	 * coordenadas. La llamada al m�todo se hace en background.
	 * 
	 * @param double1
	 *            Latitud de la coordenada.
	 * @param double2
	 *            Longitud de la coordenada.
	 */
	public void getAddress(final Double double1, final Double double2)
	{
		// TODO Auto-generated method stub
		Thread t = new Thread("AddressThread")
		{
			public void run()
			{
				Geocoder gc = new Geocoder(controller.getContext().getApplicationContext(), Locale.getDefault());
				try
				{
					List<Address> addresses = null;
					addresses = gc.getFromLocation(double1, double2, 5);
					
					if (!addresses.isEmpty())
					{
						controller.sendMessage(RESPONSE_GETTING_ADDRESS_FINISHED, addresses.get(0));
					}
					else
					{
						controller.sendMessage(RESPONSE_GETTING_ADDRESS_FAILED);
					}
				}
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					controller.sendMessage(RESPONSE_GETTING_ADDRESS_FAILED);
					e.printStackTrace();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		};
		t.start();
		controller.sendMessage(RESPONSE_GETTING_ADDRESS_STARTED);
	}
	
	/**
	 * M�todo para obtener las coordinadas aproximadas de una direcci�n pasada por par�metro.
	 * La llamada al m�todo se hace en background.
	 * 
	 * @param address Direcci�n f�sica de la que buscar sus coordinadas.
	 */
	public void getCoordinates(final String address)
	{
		Thread t = new Thread("getCoordinatesThread")
		{
			public void run()
			{
				Geocoder gc = new Geocoder(controller.getContext().getApplicationContext(), Locale.getDefault());
				try
				{
					List<Address> addresses = null;
					addresses = gc.getFromLocationName(address, 5);
					
					if (!addresses.isEmpty())
					{
						Address bestCoordinates = addresses.get(0);
						Location loc = new Location("unknown");
						loc.setLatitude(bestCoordinates.getLatitude());
						loc.setLongitude(bestCoordinates.getLongitude());
						controller.sendMessage(RESPONSE_COORDINATES_FROM_ADDRESS_FINISHED, loc);
					}
					else
					{
						controller.sendMessage(RESPONSE_COORDINATES_FROM_ADDRESS_FAILED);
					}
				}
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					controller.sendMessage(RESPONSE_COORDINATES_FROM_ADDRESS_FAILED);
					e.printStackTrace();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		};
		t.start();
		controller.sendMessage(RESPONSE_COORDINATES_FROM_ADDRESS_STARTED);
	}

	/**
	 * M�todo que devuelve la direcci�n fisica aproximada a la ultima
	 * localizaci�n generada por el sistema. La llamada al m�todo se hace en
	 * background.
	 */
	public void getLastKnownAddress()
	{
		Thread t = new Thread("AddressThread")
		{
			public void run()
			{
				Geocoder gc = new Geocoder(controller.getContext().getApplicationContext(), Locale.getDefault());
				try
				{
					List<Address> addresses = null;
					if (lastKnownLocation != null)
						addresses = gc.getFromLocation(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(), 5);
					else
						controller.sendMessage(RESPONSE_GETTING_ADDRESS_FAILED);
					if (addresses != null && !addresses.isEmpty())
					{
						controller.sendMessage(RESPONSE_GETTING_ADDRESS_FINISHED, addresses.get(0));
					}
					else
					{
						controller.sendMessage(RESPONSE_GETTING_ADDRESS_FINISHED);
					}
				}
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					controller.sendMessage(RESPONSE_GETTING_ADDRESS_FAILED);
					e.printStackTrace();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		};
		t.start();
		controller.sendMessage(RESPONSE_GETTING_ADDRESS_STARTED);
	}
	

	/**
	 * M�todo que devuelve la �ltima localizaci�n conocida en el sistema.
	 * 
	 * @return
	 */
	public Location getLastKnownLocation()
	{
		if (bestLocationProvider == null)
		{
			controller.sendMessage(RESPONSE_NO_PROVIDER_AVAILABLE);
			return null;
		}
		
		Location bestProvider = locationManager.getLastKnownLocation(bestLocationProvider);
		// ESTE COMENTARIO SOLUCIONA EL ERROR DE LOCALIZACI�N QUE SE PRODUC�A
		// POR ARRASTRAR LA POSICI�N
		// DEL USUARIO EN EL MAPA.
		
		// SOLUCION PROVISIONAL.
		
		/*
		 * Log.i("getLastKnownLocation - LocationServer",
		 * bestProvider.toString()); if (isBetterLocation(bestProvider,
		 * lastKnownLocation)) lastKnownLocation = bestProvider;
		 */
		lastKnownLocation = bestProvider;
		return lastKnownLocation;
	}
	

	/**
	 * M�todo para decidir si una localizaci�n es mejor que otra localizaci�n y
	 * establecerla como �ltima localizaci�n del sistema.
	 * 
	 * @param location
	 *            Nueva localizaci�n a comprobar.
	 * @param currentBestLocation
	 *            Ultima mejor localizaci�n conocida en el sistema.
	 * @return
	 */
	private boolean isBetterLocation(Location location,
			Location currentBestLocation)
	{
		if (currentBestLocation == null)
		{
			return true;
		}
		
		if (location != null && currentBestLocation != null)
		{
			long timeDelta = location.getTime() - currentBestLocation.getTime();
			boolean isSignificantlyNewer = timeDelta > (2 * 60 * 1000);
			boolean isSignificantlyOlder = timeDelta < -(2 * 60 * 1000);
			
			boolean isNewer = timeDelta > 0;
			/*
			 * if(isSignificantlyNewer) { return true; } else
			 * if(isSignificantlyOlder) { return false; }
			 */
			int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
			boolean isLessAccurate = accuracyDelta > 0;
			boolean isMoreAccurate = accuracyDelta < 0;
			boolean isSignificantlyLessAccurate = accuracyDelta > 200;
			
			// Check if the old and new location are from the same provider
			boolean isFromSameProvider = isSameProvider(location.getProvider(), currentBestLocation.getProvider());
			
			// Determine location quality using a combination of timeliness and
			// accuracy
			if (isMoreAccurate)
			{
				return true;
			}
			else if (isNewer && !isLessAccurate)
			{
				return true;
			}
			else if (isNewer && !isSignificantlyLessAccurate
					&& isFromSameProvider)
			{
				return true;
			}
		}
		return false;
	}
	

	/**
	 * M�todo para comprobar si dos proveedores de localizaci�n son iguales.
	 * 
	 * @param provider1
	 *            Primer proveedor.
	 * @param provider2
	 *            Segundo proveedor.
	 * @return True si son iguales, false en otro caso.
	 */
	private boolean isSameProvider(String provider1, String provider2)
	{
		if (provider1 == null)
		{
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}
	

	/**
	 * M�todo para establecer la distancia, en metros, a la cual el proveedor de
	 * localizaci�n se debe actualizar. Se obtiene de las preferencias del
	 * sistema.
	 */
	private void setDistanceToRefresh()
	{
		// TODO Auto-generated method stub
		distanceToRefresh = PreferencesController.getRadius();
		
		distanceToRefresh = 0;
	}
	

	/**
	 * M�todo para obtener el listado de proveedores de localizaci�n activos de
	 * los cuales dispone el sistema. Se establece el mejor proveedor y el
	 * listado de los proveedores auxiliares.
	 * 
	 * Las preferencias con las cuales se dedide el mejor se obtienen de las
	 * preferencias del sistema.
	 * 
	 * @throws LocationProviderUnavailableException
	 */
	private void setLocationProviders()
			throws LocationProviderUnavailableException
	{
		// TODO Auto-generated method stub
		userCriteria = new Criteria();
		userCriteria.setAccuracy(PreferencesController.getLocationProviderAccuracy());
		userCriteria.setPowerRequirement(PreferencesController.getLocationProviderPower());
		
		bestLocationProvider = locationManager.getBestProvider(userCriteria, true);
		
		if (bestLocationProvider == null)
			throw new LocationProviderUnavailableException();
		
		locationProviders = locationManager.getProviders(true);
		if (locationProviders != null && !locationProviders.isEmpty()
				&& bestLocationProvider != null)
			locationProviders.remove(bestLocationProvider);
	}
	

	/**
	 * M�todo para establecer el tiempo, en minutos, por el cual el proveedor de
	 * localizaci�n se debe actualizar.
	 */
	private void setTimeToRefresh()
	{
		// TODO Auto-generated method stub
		timeToRefresh = PreferencesController.getTime();
	}
	

	/**
	 * M�todo para comenzar el tracking de posiciones.
	 */
	public void startTrackingPosition()
	{
		setTimeToRefresh();
		setDistanceToRefresh();
		try
		{
			setLocationProviders();
			locationManager.requestLocationUpdates(bestLocationProvider, timeToRefresh, distanceToRefresh, bestLocationListener);
		}
		catch (LocationProviderUnavailableException e)
		{
			// TODO Auto-generated catch block
			// Send message to controller.
			controller.sendMessage(RESPONSE_NO_PROVIDER_AVAILABLE);
			e.printStackTrace();
		}
		
	}
	

	/**
	 * M�todo para detener el tracking de posiciones.
	 */
	public void stopTrackingPosition()
	{
		locationManager.removeUpdates(bestLocationListener);
		locationManager.removeUpdates(temporalLocationListener);
	}
	

	/**
	 * M�todo para establecer una nueva localizaci�n en caso de que sea mejor
	 * que la anterior.
	 * 
	 * @param location
	 */
	private synchronized void updateLocation(Location location)
	{
		Log.i("LOCATION SERVER", "updateLocation " + location.getLatitude()
				+ " // " + location.getLongitude());
		// TODO Auto-generated method stub
		if (isBetterLocation(location, lastKnownLocation))
			lastKnownLocation = location;
		
		controller.sendMessage(RESPONSE_LOCATION_CHANGED, lastKnownLocation);
	}
	
}
