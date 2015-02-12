package com.example.socketio.android;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import com.example.socketiotest.GlobalVariable;

public class SocketIOService extends Service
{
	private final String TAG = "SocketIOService";
	private NetworkConnectionIntentReceiver mNetworkConnectionMonitor;
	private SocketIOErrorReceiver mSocketIOErrorReceiver;

	@Override
	public void onCreate()
	{
		super.onCreate();
		Log.e("SocketIO_MobileService", "onCreate");
		GlobalVariable.setServiceType(true);
		registerBroadcastReceivers();
		FirstConnect();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		Log.e(TAG, "Server onStartCommand");
		return START_STICKY;
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		Log.e(TAG, "onDestroy");
		unregisterBroadcastReceivers();
		GlobalVariable.setServiceType(false);
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	// =====================
	// === Public function
	// =====================
	public void connect()
	{
		SocketIOAndroidClient.init();
		SocketIOAndroidClient.connect(SocketIOService.this);
	}

	public void reconnect()
	{
		SocketIOAndroidClient.reconnect(SocketIOService.this);
	}

	public boolean isOnline()
	{
		ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected())
		{
			return true;
		}
		return false;
	}

	public void ClientOffline()
	{
		SocketIOAndroidClient.ClientOffline();
	}

	// =====================
	// === Private function
	// =====================
	private void FirstConnect()
	{
		// SetService
		SocketIOAndroidClient.initSocketIOPingSender(SocketIOService.this);
		// connect
		connect();
	}

	private void registerBroadcastReceivers()
	{
		if (mNetworkConnectionMonitor == null)
		{
			// Network
			mNetworkConnectionMonitor = new NetworkConnectionIntentReceiver();
			registerReceiver(mNetworkConnectionMonitor, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
			// Error
			mSocketIOErrorReceiver = new SocketIOErrorReceiver();
			IntentFilter SocketIoErrorIntentFilter = new IntentFilter();
			SocketIoErrorIntentFilter.addAction(SocketIOAction.SOCKETIO_ERRORMESSAGE_ERRORHANDSHAKING);
			SocketIoErrorIntentFilter.addAction(SocketIOAction.SOCKETIO_ERRORMESSAGE_HEARTBEATTIMEOUT);
			SocketIoErrorIntentFilter.addAction(SocketIOAction.SOCKETIO_ERRORMESSAGE_SERVERREJECTCONNECTION);
			registerReceiver(mSocketIOErrorReceiver, SocketIoErrorIntentFilter);
		}
	}

	private void unregisterBroadcastReceivers()
	{
		if (mNetworkConnectionMonitor != null)
		{
			// Network
			unregisterReceiver(mNetworkConnectionMonitor);
			mNetworkConnectionMonitor = null;
			// Error
			unregisterReceiver(mSocketIOErrorReceiver);
			mSocketIOErrorReceiver = null;
		}
	}

	// ===============================
	// ===BroadcastReceiver
	// ================================
	private class NetworkConnectionIntentReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
			WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SOCKETIO");
			wl.acquire();
			if (isOnline())
			{
				if (!SocketIOAndroidClient.isSocketIORunnning())
				{
					reconnect();
				}
			}
			else
			{
				ClientOffline();
			}
			wl.release();
		}
	}

	private class SocketIOErrorReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			Log.e(TAG, "SocketIOErrorReceiver onReceive");
			SocketIOAndroidClient.startReconnect();
		}
	}
}
