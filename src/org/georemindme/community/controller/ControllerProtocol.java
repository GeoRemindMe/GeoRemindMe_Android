package org.georemindme.community.controller;


import org.georemindme.community.R;


public interface ControllerProtocol
{
	int	V_REQUEST_QUIT							= 101;	// empty
	int	V_REQUEST_LOGIN							= 102;	// obj = (User) data
	int	V_REQUEST_AUTOLOGIN						= 103;	// empty
	int	V_REQUEST_LOGOUT						= 104;	// empty
	int	V_REQUEST_PERIODICAL_UPDATES			= 105;	// empty
	int	V_REQUEST_QUIT_PERIODICAL_UPDATES		= 106;	// empty
	int	V_REQUEST_UPDATE						= 107;	// empty
	int	V_REQUEST_IS_LOGGED						= 108;
	int	V_REQUEST_LAST_LOCATION					= 109;
	int	V_RESET_LOCATION_PROVIDERS				= 110;
	int	V_REQUEST_LAST_KNOWN_ADDRESS			= 111;
	int	V_REQUEST_ADDRESS						= 112;	// obj = new
														// Double[latitude,
														// longitude]
	int	V_REQUEST_ALL_UNDONE_ALERTS				= 113;
	int	V_REQUEST_ALL_DONE_ALERTS				= 114;
	int	V_REQUEST_ALL_MUTED_ALERTS				= 115;
	
	int	V_REQUEST_SAVE_ALERT					= 116;	// obj = (Alert) new
														// alert.
	int	V_REQUEST_CHANGE_ALERT_ACTIVE			= 117;	// obj = (Object[])
														// on/off | id;
	int	V_REQUEST_CHANGE_ALERT_DONE				= 118;
	int	V_REQUEST_UPDATE_ALERT					= 119;	// Object = Alert.
	int	V_REQUEST_DELETE_ALERT					= 120;	// Object = alert.
														
	int	V_REQUEST_NEXT_TIMELINE_PAGE			= 121;
	
	int	V_REQUEST_CREATE_NEW_USER				= 122;	// obj -> {name, pass}
														
	int	C_QUIT									= 201;	// empty
	int	C_LOGIN_STARTED							= 202;	// empty
	int	C_LOGIN_FINISHED						= 203;	// empty
	int	C_LOGOUT_STARTED						= 204;	// empty
	int	C_LOGOUT_FINISHED						= 205;	// obj = (User)
	int	C_LOGIN_FAILED							= 206;	// empty
	int	C_UPDATE_STARTED						= 207;
	int	C_UPDATE_FINISHED						= 208;
	int	C_UPDATE_FAILED							= 209;
	int	C_IS_LOGGED								= 210;	// obj = (User) data
	int	C_IS_NOT_LOGGED							= 211;
	int	C_NO_LAST_LOCATION_AVAILABLE			= 212;
	int	C_LAST_LOCATION							= 213;	// obj = (Location)
														// lastlocation
	int	C_ALERT_SAVED							= 214;
	int	C_ALL_UNDONE_ALERTS						= 215;	// obj = (Cursor) data
	int	C_ALL_DONE_ALERTS						= 216;	// obj = (Cursor) data
	int	C_ALL_MUTED_ALERTS						= 217;	// obj = (Cursor) data
	int	C_ALERT_CHANGED							= 218;
	int	C_ALERT_DELETED							= 219;
	
	int	S_REQUEST_UPDATE						= 301;	// empty
	int	S_ALERT_NEAR							= 302;	// Object alert
	int	S_RESPONSE_NEXT_TIMELINE_PAGE			= 303;	// Object ->
														// TimelinePage
	int	S_REQUEST_NEXT_TIMELINE_PAGE_STARTED	= 304;
	int	S_REQUEST_NEXT_TIMELINE_PAGE_FAILED		= 305;
	int	S_REQUEST_NEXT_TIMELINE_PAGE_FINISHED	= 306;
	
	int	S_REQUEST_CREATE_NEW_USER_STARTED		= 307;
	int	S_REQUEST_CREATE_NEW_USER_FAILED		= 308;	// obj -> excp.
	int	S_REQUEST_CREATE_NEW_USER_FINISHED		= 309;	// Obj -> true/false
														
	int	P_PREFERENCE_CHANGED					= 401;	// obj = (Integer)
														// P_XXX_YYY_CHANGED
	int	P_ZOOM_LEVEL_CHANGED					= 401;
	int	P_SHOW_SATELLITE_CHANGED				= 402;
	int	P_SHOW_TRAFFIC_CHANGED					= 403;
	int	P_LOCATION_UPDATE_RADIUS_CHANGED		= 404;
	int	P_LOCATION_UPDATE_RATE_CHANGED			= 405;
	int	P_LOCATION_PROVIDER_ACCURACY_CHANGED	= 406;
	int	P_LOCATION_PROVIDER_POWER_CHANGED		= 407;
	int	P_SYNC_RATE_CHANGED						= 408;
	int	P_START_ON_BOOT_CHANGED					= 409;
	int	P_AUTOUPDATE_CHANGED					= 410;
	int	P_3G_LOCATION							= 411;
	
	int	LS_LOCATION_CHANGED						= 501;	// obj = (Location)
														// lastKnownLocation
	int	LS_NO_PROVIDER_AVAILABLE				= 502;
	int	LS_GETTING_ADDRESS_STARTED				= 503;
	int	LS_GETTING_ADDRESS_FAILED				= 504;
	int	LS_GETTING_ADDRESS_FINISHED				= 505;
	
	int	NS_REQUEST_ALERTS_NEAR					= 601;
}