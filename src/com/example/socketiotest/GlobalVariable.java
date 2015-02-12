package com.example.socketiotest;

import android.app.Application;

public class GlobalVariable extends Application
{
	private static boolean isServiceStarted = false;

	public static boolean getServiceType()
	{
		return isServiceStarted;
	}

	public static void setServiceType(boolean isServiceStarted)
	{
		GlobalVariable.isServiceStarted = isServiceStarted;
	}
}
