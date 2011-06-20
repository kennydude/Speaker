package me.kennydude.speaker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

public class DesktopReciever extends BroadcastReceiver {
	private static final String EXTRA_TITLE = "title";
	private static final String EXTRA_DESCRIPTION = "description";

	@Override
	public void onReceive(Context cntxt, Intent intent) {
		SharedPreferences sp = SpeakerShared.getPrefs(cntxt);
		if(intent.getAction() == "me.kennydude.speaker.SPEAK_MESSAGE"){
			if(sp.getBoolean("thirdParty", true) == false)
				return; // We don't access anything the user doesn't want us to
		} else{
			if(sp.getBoolean("androidDesktopNotifier", true) == false)
				return; // We don't access anything the user doesn't want us to
		}
		
		String title = null;
		String description = null;
		
		// Try to read extras from intent
		Bundle extras = intent.getExtras();
		if (extras != null) {
			title = extras.getString(EXTRA_TITLE);
			description = extras.getString(EXTRA_DESCRIPTION);
		}
		
		String to_say = "";
		if(title != null)
			to_say += title + ". ";
		if(description != null)
			to_say += description;
		Intent service = new Intent(cntxt, SpeakService.class);
    	service.putExtra("speak", to_say);
    	cntxt.startService(service);
	}

}
