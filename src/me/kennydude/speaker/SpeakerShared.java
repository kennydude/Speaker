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
            if(cr.getCount() != 0){
            	return cr.getString(0);
            }
            
            try{
	        	String number = ""; // Trick tts
	        	char[] numbers = phoneNumber.toCharArray();
	        	for(char n : numbers){
	        		number += n + " ";
	        	}
	        	return number;
        	} catch(Exception ae){
        		return cntxt.getString(R.string.unknown_number);
        	}
            
            
        }catch(Exception e){
        	try{
	        	e.printStackTrace();
	        	String number = ""; // Trick tts
	        	char[] numbers = phoneNumber.toCharArray();
	        	for(char n : numbers){
	        		number += n + " ";
	        	}
	        	return number;
        	} catch(Exception ae){
        		return cntxt.getString(R.string.unknown_number);
        	}
        }
	}
}
