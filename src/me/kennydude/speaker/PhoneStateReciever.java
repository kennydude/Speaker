package me.kennydude.speaker;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import android.util.Log;

public class PhoneStateReciever extends BroadcastReceiver {
	
	public String getSpokenText(SharedPreferences sp, String incomingNumber, Context cntxt){
		String number = SpeakerShared.getNameByPhone(incomingNumber, cntxt);
		String text = sp.getString("callOutput", null);
    	if(text == null)
    		text = cntxt.getResources().getString(R.string.call_readout);
    	text = text.replace("{from}", number);
    	return text;
	}

	@Override
	public void onReceive(Context cntxt, Intent intent) {
		String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
		String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
		if (TelephonyManager.EXTRA_STATE_RINGING.equals(state)){
			SharedPreferences sp = SpeakerShared.getPrefs(cntxt);
			if(sp.getBoolean("notifyCalls", true) == false)
				return;
		    String text = getSpokenText(sp, incomingNumber, cntxt);
	    	Intent service = new Intent(cntxt, SpeakService.class);
	    	service.putExtra("speak", text);
	    	cntxt.startService(service);
	    	if(sp.getBoolean("renotifyCalls", true) == true){
	    		int sec = 15;
		        Log.d("x", "added sayback");
	    		AlarmManager alm = (AlarmManager) cntxt.getSystemService(Context.ALARM_SERVICE);
	    		PendingIntent pI = PendingIntent.getService(cntxt, 0, service, PendingIntent.FLAG_UPDATE_CURRENT);
	    		alm.setRepeating(AlarmManager.ELAPSED_REALTIME, sec * 100 , sec * 1000, pI);
	    	}
		} else{
			// Remove the repeat if already done! :D
			SharedPreferences sp = SpeakerShared.getPrefs(cntxt);
			if(sp.getBoolean("renotifyCalls", true) == true){
				Log.d("x", "removed pending intent");
				String text = getSpokenText(sp, incomingNumber, cntxt);
		    	Intent service = new Intent(cntxt, SpeakService.class);
		    	service.putExtra("speak", text);
		    	PendingIntent pI = PendingIntent.getService(cntxt, 0, service, PendingIntent.FLAG_UPDATE_CURRENT);
		    	AlarmManager alm = (AlarmManager) cntxt.getSystemService(Context.ALARM_SERVICE);
		    	alm.cancel(pI);
			}
		}
	}

}
