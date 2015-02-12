package com.example.socketio.android;

import java.net.MalformedURLException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.example.socketio.lib.IOAcknowledge;
import com.example.socketio.lib.IOCallback;
import com.example.socketio.lib.SocketIO;
import com.example.socketio.lib.SocketIOException;

public class SocketIOAndroidClient
{
	// socketIO
	private final static String TAG = "SockeIOAndroidClient";
	private final static String URL = "YourServerURL:Port";
	private static SocketIO mSocketIO = null;
	private static IOCallback mIOCallback = null;
	// SocketIOPingSender
	private volatile static boolean mHasStarted = false;
	private static PendingIntent mPendingIntent;
	private static BroadcastReceiver mAlarmReceiver;
	private static SocketIOService mService;
	private static int Delaytime;
	private static int QuickReconnectDelaytime = 5000;// 5sec , ms, need to add heartbeats timeout
	private static int NormalReconnectDelaytime = 1800000;// 30 min ,ms, need to add heartbeats timeout
	private static int count;
	private static int MAXQuickReconnectCount = 8;// 5 + heartbeats timeout sec * 8 = Quickreconnect timeout

	// ===================
	// == Public Function
	// ===================
	public static void init()
	{
		mSocketIO = new SocketIO();
		mIOCallback = new IOCallback()
		{
			@Override
			public void onMessage(JSONObject json, IOAcknowledge ack)
			{
				Log.e(TAG, "onMessage1");
				try
				{
					Log.e(TAG, "Server said:" + json.toString(2));
				} catch (JSONException e)
				{
					Log.e(TAG, e.toString());
					e.printStackTrace();
				}
			}

			@Override
			public void onMessage(String data, IOAcknowledge ack)
			{
				Log.e(TAG, "onMessage2");
				Log.e(TAG, data);
				Log.e(TAG, ack.toString());
			}

			@Override
			public void onError(SocketIOException socketIOException)
			{
				Trace_OnError(socketIOException);
			}

			@Override
			public void onDisconnect()
			{
				Trace_OnDisconnect();
			}

			@Override
			public void onConnect()
			{
				Trace_OnConnect();
			}

			@Override
			public void on(String event, IOAcknowledge ack, Object... args)
			{
				Trace_OnEvent(event, args);
			}
		};
	}

	// ========================
	// ===Public Open Function
	// ========================
	public static void connect(Context context)
	{
		Log.e(TAG, "connect");
		try
		{
			mSocketIO.connect(URL, mIOCallback, context);
		} catch (MalformedURLException e)
		{
			e.printStackTrace();
			Log.e(TAG, "connect Error:" + e.toString());
		}
	}

	public static void reconnect(Context context)
	{
		Log.e(TAG, "reconnect");
		init();
		connect(context);
	}

	public static boolean isSocketIORunnning()
	{
		if (mSocketIO == null || mIOCallback == null)
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	public static void ClientOffline()
	{
		Log.e(TAG, "Clientoffline");
		if (mSocketIO != null && mIOCallback != null)
		{
			mSocketIO.disconnect();
		}
		mSocketIO = null;
		mIOCallback = null;
	}

	public static boolean isConnect()
	{
		return mSocketIO.isConnected();
	}

	public static void initSocketIOPingSender(SocketIOService service)
	{
		mAlarmReceiver = new AlarmReceiver();
		mService = service;
		Delaytime = QuickReconnectDelaytime;
		count = 0;
	}

	public static void startReconnect()
	{
		count++;
		Log.e(TAG, "COUNT=" + String.valueOf(count));
		if (count >= MAXQuickReconnectCount)
		{
			Delaytime = NormalReconnectDelaytime;
			count = MAXQuickReconnectCount;
		}
		String action = SocketIOAction.SOCKETIO_PING_SENDER;
		Log.e(TAG, "Register alarmreceiver to " + action);
		mService.registerReceiver(mAlarmReceiver, new IntentFilter(action));
		mPendingIntent = PendingIntent.getBroadcast(mService, 0, new Intent(action), PendingIntent.FLAG_UPDATE_CURRENT);
		schedule(Delaytime);
		mHasStarted = true;
	}

	public static void stopReconnect()
	{
		AlarmManager alarmManager = (AlarmManager) mService.getSystemService(Service.ALARM_SERVICE);
		alarmManager.cancel(mPendingIntent);
		Log.e(TAG, "Unregister alarmreceiver to SOCKETIO_PING_SENDER");
		if (mHasStarted)
		{
			mHasStarted = false;
			try
			{
				mService.unregisterReceiver(mAlarmReceiver);
			} catch (IllegalArgumentException e)
			{
				// Ignore unregister errors.
			}
		}
		Delaytime = QuickReconnectDelaytime;
		count = 0;
		System.gc();
	}

	private static void schedule(long delayInMilliseconds)
	{
		Log.e(TAG, "schedule");
		long nextAlarmInMilliseconds = System.currentTimeMillis() + delayInMilliseconds;
		Log.e(TAG, "Schedule next alarm at " + nextAlarmInMilliseconds);
		AlarmManager alarmManager = (AlarmManager) mService.getSystemService(Service.ALARM_SERVICE);
		alarmManager.set(AlarmManager.RTC_WAKEUP, nextAlarmInMilliseconds, mPendingIntent);
	}

	public static class AlarmReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			Log.e(TAG, "AlarmReceiver +onReceive");
			ClientOffline();
			reconnect(context);
		}
	}

	// ====================
	// =SocketIO Function
	// ====================
	private static void Trace_OnEvent(String event, Object... args)
	{
		Log.e(TAG, "onEvent");
		Log.e(TAG, "event=" + event.toString());
		Log.e(TAG, "Content=" + args[0].toString());
	}

	private static void Trace_OnError(SocketIOException socketIOException)
	{
		Log.e(TAG, "onError");
		Log.e(TAG, socketIOException.toString());
	}

	private static void Trace_OnConnect()
	{
		Log.e(TAG, "onConnect");
		stopReconnect();
	}

	private static void Trace_OnDisconnect()
	{
		Log.e(TAG, "onDisconnect");
	}
}
