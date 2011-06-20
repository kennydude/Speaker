package me.kennydude.speaker;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;

public class SpeakerSettingsActivity extends PreferenceActivity {
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        Preference customPref = (Preference) findPreference("testDesktopNotifier");
		customPref.setOnPreferenceClickListener(new OnPreferenceClickListener(){
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				Intent i = new Intent("org.damazio.notifier.service.UserReceiver.USER_MESSAGE");
				i.putExtra("title", "Test");
				i.putExtra("description", "We're just testing a few things here!");
				sendBroadcast(i);
				return true;
			}
		});
    }
}