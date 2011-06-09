package org.georemindme.community.tools;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;

import android.content.Context;
import android.os.Environment;
import android.util.Log;


public class Logger
{
	private static final String		FILE_NAME		= "_logger_";
	
	public static final int			MODE_APPEND		= Context.MODE_APPEND;
	public static final int			MODE_PRIVATE	= Context.MODE_PRIVATE;
	
	private static FileWriter		writter			= null;
	private static BufferedWriter	buffer;
	
	private static File				file;
	
	
	public static void start(Context context, int mode)
	{
		File sdCard = Environment.getExternalStorageDirectory();
		if (sdCard.canWrite())
		{
			file = new File(sdCard, FILE_NAME);
			try
			{
				writter = new FileWriter(file);
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e("LOGGER - Start", "Error: " + e.toString());
			}
			buffer = new BufferedWriter(writter);
		}
	}
	

	public static void finish()
	{
		try
		{
			buffer.flush();
			buffer.close();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e("LOGGER - Finish", "Error: " + e.toString());
		}
		
	}
	

	public static void write(Object o, String message)
	{
		if (writter != null && buffer != null)
		{
			Log.v("LOGGER", message);
			StringBuilder stb = new StringBuilder();
			stb.append(o.getClass().getName() + "\t");
			stb.append(new Date());
			stb.append("\t");
			stb.append(message);
			stb.append("\n");
			
			try
			{
				buffer.write(stb.toString());
				buffer.flush();
				
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				// e.printStackTrace();
				
				Log.e("Error writing to private logger", e.getCause().getLocalizedMessage());
			}
		}
		else
			Log.e("LOGGER", "I cannot write to the file. is null");
	}
}
