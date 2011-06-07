package org.georemindme.community.controller;


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
	
	int	S_REQUEST_UPDATE						= 301;	// empty
														
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
	int P_3G_LOCATION = 411;
	
	int LS_LOCATION_CHANGED = 501;
	int LS_NO_PROVIDER_AVAILABLE = 502;
}
