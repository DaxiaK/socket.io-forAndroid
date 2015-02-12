package com.example.socketio.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.socketiotest.GlobalVariable;

public class BootupBroadcast extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		if (!GlobalVariable.getServiceType())
		{
			Intent serviceintent = new Intent();
			serviceintent.setClass(context, SocketIOService.class);
			context.startService(serviceintent);
		}
	}
}
