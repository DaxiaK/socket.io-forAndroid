package com.example.socketiotest;

import com.example.socketio.android.SocketIOService;

import android.app.Activity;
import android.content.Intent;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity
{
	int Uid = android.os.Process.myUid();
	TextView textView2;
	Handler handler;
	boolean isstop = false;
	Button btn_reconnect;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		textView2 = (TextView) findViewById(R.id.textView2);
		handler = new Handler();
		handler.post(runnable);
		// socketIo
		if (!GlobalVariable.getServiceType())
		{
			Intent serviceintent = new Intent();
			serviceintent.setClass(MainActivity.this, SocketIOService.class);
			startService(serviceintent);
		}
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		isstop = true;
		// android.os.Process.killProcess(android.os.Process.myPid());
	}

	// Show Traffic
	private Runnable runnable = new Runnable()
	{
		public void run()
		{
			long traffic = TrafficStats.getUidRxBytes(Uid) + TrafficStats.getUidTxBytes(Uid);
			textView2.setText(String.valueOf(traffic));
			if (!isstop)
			{
				handler.postDelayed(runnable, 1000);
			}
		}
	};
}
