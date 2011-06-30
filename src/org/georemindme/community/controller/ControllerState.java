package org.georemindme.community.controller;

import android.os.Message;
import org.georemindme.community.R;

public interface ControllerState
{
	boolean handleMessage(Message msg);
}
