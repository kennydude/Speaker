package me.kennydude.speaker;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;
import android.speech.tts.TextToSpeech;
import android.text.ClipboardManager;
import android.util.Log;

public class SpeakerSettingsActivity extends PreferenceActivity {
	public static final Integer CHECK_TTS_AVAILABILITY = 1822;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        Preference customPref = (Preference) findPreference("testDesktopNotifier");
		customPref.setOnPreferenceClickListener(new OnPreferenceClickListener(){
			public boolean onPreferenceClick(Preference arg0) {
				Intent i = new Intent("org.damazio.notifier.service.UserReceiver.USER_MESSAGE");
				i.putExtra("title", "Test");
				i.putExtra("notifyIfCanceled", true);
				i.putExtra("description", "We're just testing a few things here!");
				sendBroadcast(i);
				return true;
			}
		});
		
		customPref = (Preference) findPreference("clipboard");
		customPref.setOnPreferenceClickListener(new OnPreferenceClickListener(){

			public boolean onPreferenceClick(Preference arg0) {
				ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
				
				Intent i = new Intent("me.kennydude.speaker.SPEAK_MESSAGE");
				String s = clipboardManager.getText().toString();
				if(s == null){
					s = getString(R.string.no_clipboard);
				} else if(s.equals("")){
					s = getString(R.string.no_clipboard);
				}
				
				i.putExtra("description", s);
				i.putExtra("notifyIfCanceled", true);
				sendBroadcast(i);
				
				return true;
			}
			
		});
		
		Intent checkIntent = new Intent();
	    checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
	    startActivityForResult(checkIntent, CHECK_TTS_AVAILABILITY);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if(requestCode == CHECK_TTS_AVAILABILITY){
    		Log.d("r", "TTS Response: "+requestCode);
    		Preference customPref = (Preference) findPreference("status");
    		if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
    			customPref.setSummary(R.string.tts_ok);
    		} else{
    			customPref.setSummary(R.string.tts_no_config);
    			customPref.setOnPreferenceClickListener(new OnPreferenceClickListener(){

					public boolean onPreferenceClick(Preference arg0) {
						startActivity(new Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA));
						return true;
					}
    				
    			});
    		}
    	}
    }
}