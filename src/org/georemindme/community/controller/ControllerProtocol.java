package org.georemindme.community.controller;


import org.georemindme.community.R;


public interface ControllerProtocol
{
	
	int	PREFERENCE_3G_LOCATION							= 101;
	int	PREFERENCE_AUTOUPDATE_CHANGED					= 102;
	int	PREFERENCE_LOCATION_PROVIDER_ACCURACY_CHANGED	= 103;
	int	PREFERENCE_LOCATION_PROVIDER_POWER_CHANGED		= 104;
	int	PREFERENCE_LOCATION_UPDATE_RADIUS_CHANGED		= 105;
	int	PREFERENCE_LOCATION_UPDATE_RATE_CHANGED			= 106;
	int	PREFERENCE_PREFERENCE_CHANGED					= 107;	// obj =
																// (Integer)
	int	PREFERENCE_SHOW_SATELLITE_CHANGED				= 108;
	int	PREFERENCE_SHOW_TRAFFIC_CHANGED					= 109;
	int	PREFERENCE_START_ON_BOOT_CHANGED				= 110;
	int	PREFERENCE_SYNC_RATE_CHANGED					= 111;	// P_XXX_YYY_CHANGED
	int	PREFERENCE_ZOOM_LEVEL_CHANGED					= 112;
	
	int	REQUEST_ADDRESS									= 201;	// obj = new
	int	REQUEST_ALL_DONE_ALERTS							= 202;
	int	REQUEST_ALL_MUTED_ALERTS						= 203;	// Double[latitude,longitude]
	int	REQUEST_ALL_UNDONE_ALERTS						= 204;
	int	REQUEST_AUTOLOGIN								= 205;	// empty alert.
	int	REQUEST_CHANGE_ALERT_ACTIVE						= 206;	// obj =
																// (Object[])
																// on/off | id;
	int	REQUEST_CHANGE_ALERT_DONE						= 207;
	int	REQUEST_CREATE_NEW_USER							= 208;	// obj -> {name,
																// pass}
	int	REQUEST_DELETE_ALERT							= 209;	// Object =
																// alert.
	int	REQUEST_IS_LOGGED								= 210;
	int	REQUEST_LAST_KNOW_ADDRESS						= 211;
	int	REQUEST_LAST_LOCATION							= 212;
	int	REQUEST_LOGIN									= 213;	// obj = (User)
																// data
	int	REQUEST_LOGOUT									= 214;	// empty
	int	REQUEST_NEXT_TIMELINE_PAGE						= 215;
	int	REQUEST_PERIODICAL_UPDATES_OFF					= 216;	// empty
	int	REQUEST_PERIODICAL_UPDATES_ON					= 217;	// empty
	int	REQUEST_QUIT									= 218;	// empty
	int	REQUEST_RESET_LOCATION_PROVIDERS				= 219;
	int	REQUEST_SAVE_ALERT								= 220;	// obj = (Alert)
																// new
	int	REQUEST_UPDATE									= 221;	// empty
	int	REQUEST_UPDATE_ALERT							= 222;	// Object =
																// Alert.
	int	REQUEST_COORDINATES_FROM_ADDRESS				= 223;	// Object =
																// String
																// (address)
																
	int	RESPONSE_ALERT_CHANGED							= 301;
	int	RESPONSE_ALERT_DELETED							= 302;	// lastlocation
	int	RESPONSE_ALERT_NEAR								= 303;	// Object alert
	int	RESPONSE_ALERT_SAVED							= 304;
	int	RESPONSE_ALL_DONE_ALERTS						= 305;	// obj =
																// (Cursor) data
	int	RESPONSE_ALL_MUTED_ALERTS						= 306;	// obj =
	int	RESPONSE_ALL_UNDONE_ALERTS						= 307;	// obj =
	int	RESPONSE_CREATE_NEW_USER_FAILED					= 308;	// obj -> excp.
	int	RESPONSE_CREATE_NEW_USER_FINISHED				= 309;	// Obj ->
	int	RESPONSE_CREATE_NEW_USER_STARTED				= 310;
	int	RESPONSE_GETTING_ADDRESS_FAILED					= 311;
	int	RESPONSE_GETTING_ADDRESS_FINISHED				= 312;
	int	RESPONSE_GETTING_ADDRESS_STARTED				= 313;
	int	RESPONSE_IS_LOGGED								= 314;	// obj = (User)
	int	RESPONSE_IS_NOT_LOGGED							= 315;
	int	RESPONSE_LAST_LOCATION							= 316;	// obj =
	int	RESPONSE_LOCATION_CHANGED						= 317;	// obj =
	int	RESPONSE_LOGIN_FAILED							= 318;	// empty
	int	RESPONSE_LOGIN_FINISHED							= 319;	// empty
	int	RESPONSE_LOGIN_STARTED							= 320;	// empty
	int	RESPONSE_LOGOUT_FINISHED						= 321;	// obj = (User)
	int	RESPONSE_LOGOUT_STARTED							= 322;	// empty
	int	RESPONSE_NEXT_TIMELINE_PAGE_FAILED				= 323;
	int	RESPONSE_NEXT_TIMELINE_PAGE_FINISHED			= 324;
	int	RESPONSE_NEXT_TIMELINE_PAGE_STARTED				= 325;
	int	RESPONSE_NO_LAST_LOCATION_AVAILABLE				= 326;
	int	RESPONSE_NO_PROVIDER_AVAILABLE					= 327;
	int	RESPONSE_QUIT									= 328;	// empty
	int	RESPONSE_REQUEST_ALERTS_NEAR					= 329;
	int	RESPONSE_UPDATE_FAILED							= 330;
	int	RESPONSE_UPDATE_FINISHED						= 331;
	int	RESPONSE_UPDATE_STARTED							= 332;
	int	RESPONSE_COORDINATES_FROM_ADDRESS_STARTED		= 333;
	int	RESPONSE_COORDINATES_FROM_ADDRESS_FAILED		= 334;
	int	RESPONSE_COORDINATES_FROM_ADDRESS_FINISHED		= 335; // Obj -> Location
	
}