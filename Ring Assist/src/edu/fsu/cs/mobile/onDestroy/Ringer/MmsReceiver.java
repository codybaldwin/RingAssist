package edu.fsu.cs.mobile.onDestroy.Ringer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class MmsReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
	      Toast.makeText(context, "MMS Received!", Toast.LENGTH_LONG).show();
	}

}
