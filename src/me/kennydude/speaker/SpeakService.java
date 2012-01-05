package me.kennydude.speaker;


import java.util.HashMap;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.util.Log;
import android.widget.Toast;

public class SpeakService extends Service implements TextToSpeech.OnInitListener, OnUtteranceCompletedListener {
	TextToSpeech mTts;
	String spokenText;

    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
        	mTts.setOnUtteranceCompletedListener(this);
        	SharedPreferences sp = SpeakerShared.getPrefs(this);
        	HashMap<String, String> params = new HashMap<String, String>();
        	AudioManager audio = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        	int stream = AudioManager.STREAM_NOTIFICATION;
        	if(audio.getRingerMode() != AudioManager.RINGER_MODE_NORMAL && sp.getBoolean("skipSilent", false) == true){
        		stream = AudioManager.STREAM_MUSIC;
        	}
        	params.put(TextToSpeech.Engine.KEY_PARAM_STREAM, stream + "");
        	params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, spokenText.hashCode() + "");
            mTts.speak(spokenText, TextToSpeech.QUEUE_ADD, params);
        } else{
        	NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    		int icon = R.drawable.ic_stat_talking;
    		CharSequence tickerText = getResources().getText(R.string.tts_no_config);
    		long when = System.currentTimeMillis();
    		
    		mNotificationManager.cancel(spokenText.hashCode());
        	
        	Notification notification = new Notification(icon, tickerText, when);
    		Context context = getApplicationContext();
    		CharSequence contentTitle = getResources().getText(R.string.app_name);
    		CharSequence contentText = getResources().getText(R.string.tts_no_config);
    		Intent notificationIntent = new Intent();
    		notificationIntent.setAction(
                    TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
    		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
    		notification.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_AUTO_CANCEL;
    		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
    		mNotificationManager.notify(3395785, notification);
        }
    }

    public void onUtteranceCompleted(String uttId) {
    	NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(Integer.parseInt(uttId));
        AudioManager audio = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        
        if(Build.VERSION.SDK_INT >= 8){
			FroyoDucking.looseDucking(audio);
		} else{
			audio.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_VIBRATE);
			audio.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_VIBRATE);
		}
        stopSelf();
    }

    @Override
    public void onDestroy() {
    	/*try{
    		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(NOTIFY);
    	} catch(Exception e){ e.printStackTrace(); }*/
        if (mTts != null) {
            mTts.stop();
            mTts.shutdown();
        }
        super.onDestroy();
    }
	
	
	// mTts.speak(text, TextToSpeech.QUEUE_ADD, null);
	
	int NOTIFY = 324444;
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	static boolean bNotifiyIfCanceled = false;
	void notifyIfCanceled(){
		Log.d("s", "canceled");
		if(bNotifiyIfCanceled == true){
			Log.d("s", "display toast");
			Toast.makeText(this.getApplicationContext(), R.string.canceled, Toast.LENGTH_LONG).show();
		}
	}
	
	void start(String text){
		Log.d("s", "Item added to SpeakService!");
		
		SharedPreferences sp = SpeakerShared.getPrefs(this);
		if(sp.getBoolean("globalOption", true) == false){
			return;
		}
		
		AudioManager audio = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		if(sp.getBoolean("headphonesOnly", false) == true){
			if(!audio.isWiredHeadsetOn()){
				notifyIfCanceled();
				return;
			}
		}
		if(audio.getRingerMode() != AudioManager.RINGER_MODE_NORMAL && sp.getBoolean("skipSilent", false) == false){
			notifyIfCanceled();
			return;
		}
		if(Build.VERSION.SDK_INT >= 8){
			FroyoDucking.getDucking(audio);
		} else{
			audio.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_VIBRATE);
			audio.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_VIBRATE);
		}
		
		spokenText = text;
		mTts = new TextToSpeech(this, this);
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		int icon = R.drawable.ic_stat_talking;
		CharSequence tickerText = getResources().getText(R.string.talking);
		long when = System.currentTimeMillis();

		Notification notification = new Notification(icon, tickerText, when);
		Context context = getApplicationContext();
		CharSequence contentTitle = getResources().getText(R.string.app_name);
		CharSequence contentText = getResources().getText(R.string.talking);
		Intent notificationIntent = new Intent(this, SpeakerSettingsActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		notification.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_AUTO_CANCEL;
		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		mNotificationManager.notify(spokenText.hashCode(), notification);
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		bNotifiyIfCanceled = intent.hasExtra("notifyIfCanceled");
		start(intent.getStringExtra("speak"));
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		bNotifiyIfCanceled = intent.hasExtra("notifyIfCanceled");
		start(intent.getStringExtra("speak"));
		return Service.START_NOT_STICKY;
	}

}
