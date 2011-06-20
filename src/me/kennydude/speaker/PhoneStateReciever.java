package me.kennydude.speaker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;

public class PhoneStateReciever extends BroadcastReceiver {

	@Override
	public void onReceive(Context cntxt, Intent intent) {
		String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
		String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
		if (TelephonyManager.EXTRA_STATE_RINGING.equals(state)){
			SharedPreferences sp = SpeakerShared.getPrefs(cntxt);
			if(sp.getBoolean("notifyCalls", true) == false)
				return;
		    String number = SpeakerShared.getNameByPhone(incomingNumber, cntxt);
			String text = sp.getString("callOutput", null);
	    	if(text == null)
	    		text = cntxt.getResources().getString(R.string.call_readout);
	    	text = text.replace("{from}", number);
	    	Intent service = new Intent(cntxt, SpeakService.class);
	    	service.putExtra("speak", text);
	    	cntxt.startService(service);
		}
	}

}
