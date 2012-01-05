package me.kennydude.speaker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class NewMessageReciever extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context cntxt, Intent intent) {
		Log.i("f", "Intent recieved: " + intent.getAction());
		SharedPreferences sp = SpeakerShared.getPrefs(cntxt);
		if(sp.getBoolean("readSMS", true) == false)
			return; // We don't access anything the user doesn't want us to
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Object[] pdus = (Object[])bundle.get("pdus");
            for (int i = 0; i < pdus.length; i++) {
                SmsMessage msg = SmsMessage.createFromPdu((byte[])pdus[i]);
                String name = cntxt.getString(R.string.unknown);
                if(msg.getDisplayOriginatingAddress() != null){
                	name = SpeakerShared.getNameByPhone(msg.getDisplayOriginatingAddress(), cntxt);
                }
                Log.d("f", "Sending message to speaker");
                try{
                	String text = sp.getString("smsOutput", null);
                	if(text == null)
                		text = cntxt.getResources().getString(R.string.sms_readout);
                	text = text.replace("{from}", name);
                	String message = msg.getDisplayMessageBody();
                	// Now strip out anything that will make the voice go nutty
                	message = message.replaceAll("(?i)(https?|ftp|file)://[-A-Z0-9+&@#/%?=~_|$!:,.;]*[A-Z0-9+&@#/%=~_|$]",
                			cntxt.getResources().getString(R.string.link));
                	message = message.replace(":)", cntxt.getResources().getText(R.string.happy_face));
                	message = message.replace(":-)", cntxt.getResources().getText(R.string.happy_face));
                	message = message.replace(":D", cntxt.getResources().getText(R.string.happy_face));
                	message = message.replace(":-D", cntxt.getResources().getText(R.string.happy_face));
                	message = message.replace(":')", cntxt.getResources().getText(R.string.happy_face));
                	message = message.replace(":'-)", cntxt.getResources().getText(R.string.happy_face));
                	message = message.replace(":(", cntxt.getResources().getText(R.string.sad_face));
                	message = message.replace(":-(", cntxt.getResources().getText(R.string.sad_face));
                	message = message.replace(":'(", cntxt.getResources().getText(R.string.sad_face));
                	message = message.replace(":'-(", cntxt.getResources().getText(R.string.sad_face));
                	text = text.replace("{message}", message);
                	Intent service = new Intent(cntxt, SpeakService.class);
                	service.putExtra("speak", text);
                	cntxt.startService(service);
                } catch(Exception e){
                	e.printStackTrace();
                }
            }
        }
	}

}
