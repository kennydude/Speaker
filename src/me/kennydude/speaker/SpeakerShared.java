package me.kennydude.speaker;

import java.util.Locale;

import org.json.JSONObject;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.PhoneNumberUtils;

public class SpeakerShared {
	
	public static String formatPhoneNumber(String number){
		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
		try{
			return phoneUtil.format(phoneUtil.parse(number, Locale.getDefault().getCountry()), PhoneNumberFormat.E164);
		} catch(Exception e){
			e.printStackTrace();
			return number;
		}
	}
	
	public static PerContactSettings.PhoneAndLabel getPhoneAndLabel(String phoneNumber, Context c){
		PerContactSettings.PhoneAndLabel pal = new PerContactSettings.PhoneAndLabel();
		try{
			SharedPreferences sp = c.getSharedPreferences("PerContact", Context.MODE_PRIVATE);
			pal.load(new JSONObject(sp.getString(phoneNumber, "{}")));
		} catch(Exception e){
			e.printStackTrace();
		}
		return pal;
	}

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
