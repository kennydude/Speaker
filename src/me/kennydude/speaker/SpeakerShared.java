package me.kennydude.speaker;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.ContactsContract.PhoneLookup;

public class SpeakerShared {

	public static SharedPreferences getPrefs(Context r){
		return PreferenceManager.getDefaultSharedPreferences(r);
	}
	
	public static String getNameByPhone(String phoneNumber, Context cntxt){
		ContentResolver resolver = cntxt.getContentResolver();
		try{
            Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
            Cursor cr = resolver.query(uri, new String[]{PhoneLookup.DISPLAY_NAME}, null, null, null);
            cr.moveToFirst();
            return cr.getString(0);
        }catch(Exception e){
        	e.printStackTrace();
        	String number = ""; // Trick tts
        	char[] numbers = phoneNumber.toCharArray();
        	for(char n : numbers){
        		number += n + " ";
        	}
        	return number;
        }
	}
}
