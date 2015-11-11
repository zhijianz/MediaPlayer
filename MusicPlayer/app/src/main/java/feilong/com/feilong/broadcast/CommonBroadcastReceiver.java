package com.feilong.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class CommonBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context arg0, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
            System.out.println("À´µç");
		}
	}

}
