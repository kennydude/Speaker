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
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Object[] pdus = (Object[])bundle.get("pdus");
            for (int i = 0; i < pdus.length; i++) {
                SmsMessage msg = SmsMessage.createFromPdu((byte[])pdus[i]);
                String name = cntxt.getString(R.string.unknown);
                Boolean bypass = false, private_out = false;
                if(msg.getDisplayOriginatingAddress() != null){
                	String phoneNumber = SpeakerShared.formatPhoneNumber( msg.getDisplayOriginatingAddress() );
                	name = SpeakerShared.getNameByPhone(phoneNumber, cntxt);
                	
                	// pro
                    PerContactSettings.PhoneAndLabel pal = SpeakerShared.getPhoneAndLabel(phoneNumber, cntxt);
                    Log.d("pal", pal.label);
                    Log.d("lookup", phoneNumber);
                    if(pal.announce_sms == 2) // disabled
                    	return;
                    else if(pal.announce_sms == 0)
                    	bypass = true;
                    private_out = pal.sms_privacy;
                    Log.d("announce_sms", pal.announce_sms.toString());
                }
                // Moved here so Pro features can actually override ;)
                if(sp.getBoolean("readSMS", true) == false && bypass == false)
        			return; // We don't access anything the user doesn't want us to
                
                Log.d("f", "Sending message to speaker");
                try{
                	String text = sp.getString("smsOutput", null);
                	if(text == null)
                		text = cntxt.getResources().getString(R.string.sms_readout);
                	if(private_out == true)
                		text = cntxt.getResources().getString(R.string.sms_readout_priv);
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
