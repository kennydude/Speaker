package me.kennydude.speaker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.QuickContactBadge;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.provider.BaseColumns;
import android.provider.Contacts.People;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.ContactsContract;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;

public class PerContactSettings extends Activity {
	public List<PhoneAndLabel> content;
	PhoneAndLabelAdapter pcs_pald;
	
	static final Integer PICK_RESULT = 938;

	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data){
		if(requestCode == PICK_RESULT && resultCode == RESULT_OK){
			Uri uri = data.getData();
            if (uri != null) {
                Cursor c = null;
                try {
                    c = getContentResolver().query(uri, new String[] { BaseColumns._ID,
                    		ContactsContract.Data.DISPLAY_NAME },
                            null, null, null);
                    if (c != null && c.moveToFirst()) {
                        Integer id = c.getInt(0);
                        
                        Cursor num = getContentResolver().query(
                     		ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
                     		new String[]{
                     				ContactsContract.CommonDataKinds.Phone.TYPE,
                     				ContactsContract.CommonDataKinds.Phone.LABEL,
                     				ContactsContract.CommonDataKinds.Phone.NUMBER 
                     		},
                     		ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?", 
                     		new String[]{id.toString()}, null);
                        
                        final List<PhoneAndLabel> ps = new ArrayList<PhoneAndLabel>();
                        while(num.moveToNext()){
                        	PhoneAndLabel pal = new PhoneAndLabel();
                        	Integer type = num.getInt(0);
                        	pal.label = ContactsContract.CommonDataKinds.Phone.getTypeLabel(getResources(),
                        			type, num.getString(1)).toString();
                        	pal.phone = SpeakerShared.formatPhoneNumber( num.getString(2) );
                        	pal.contact_name = c.getString(1);
                        	
                        	ps.add(pal);
                        }
                        
                        PhoneAndLabelAdapter pala = new PhoneAndLabelAdapter(PerContactSettings.this,
                        		android.R.layout.test_list_item, 0, ps);
                        AlertDialog.Builder ab = new AlertDialog.Builder(PerContactSettings.this);
                        ab.setAdapter(pala, new OnClickListener(){

							public void onClick(DialogInterface arg0, int position) {
								content.add(ps.get(position));
								pcs_pald.notifyDataSetChanged();
							}
                        	
                        });
                        ab.setTitle(R.string.select_number);
                        ab.show();
                        
                    }
                } finally {
                    if (c != null) {
                        c.close();
                    }
                }
            }
            
		}
	}
	
	public class PhoneAndLabelAdapter extends ArrayAdapter<PhoneAndLabel>{
		public PhoneAndLabelAdapter(Context context, int resource,
				int textViewResourceId, List<PhoneAndLabel> objects) {
			super(context, resource, textViewResourceId, objects);
		}
		public boolean numberLabel = true;
		
		public View getView(int position, View convertView, ViewGroup parent){
			PhoneAndLabel pal = this.getItem(position);
			TextView tv = new TextView(PerContactSettings.this);
			tv.setPadding(15, 15, 15, 15);
			String x, y = "";
			if(numberLabel == true)
				x = pal.label;
			else{
				x = pal.contact_name + " " + pal.label;
				y = "<br/>";
				y += getResources().getString(R.string.speak_sms);
				y += ": ";
				switch(pal.announce_sms){
					case 2:
						y += getResources().getString(R.string.dont_speak); break;
					case 1:
						y += getResources().getString(R.string.default_opt); break;
					case 0:
						y += getResources().getString(R.string.speak); break;
				}
				y += "<br/>";
				y += getResources().getString(R.string.notify_calls);
				y += ": ";
				switch(pal.announce_call){
					case 2:
						y += getResources().getString(R.string.dont_speak); break;
					case 1:
						y += getResources().getString(R.string.default_opt); break;
					case 0:
						y += getResources().getString(R.string.speak); break;
				}
				if(pal.sms_privacy){
					y += getResources().getString(R.string.with_privacy);
				}
			}
			tv.setText(Html.fromHtml("<strong>" + x + "</strong> " + pal.phone + y));
			return tv;
		}
	}
	
	public static class PhoneAndLabel{
		String label = "";
		String phone = "";
		String contact_name = "";
		
		Integer announce_sms = 1;
		Integer announce_call = 1;
		
		Boolean sms_privacy = false;
		
		// Options
		
		public String save(){
			try{
				JSONObject out = new JSONObject();
				out.put("label", label);
				out.put("phone", phone.replace(" ", ""));
				out.put("contact_name", contact_name);
				out.put("announce_sms", announce_sms);
				out.put("announce_call", announce_call);
				out.put("sms_privacy", sms_privacy);
				return out.toString();
			} catch(Exception e){
				e.printStackTrace();
			}
			return "";
		}
		
		public void load(JSONObject in){
			try{
				label = in.getString("label");
				phone = in.getString("phone").replace(" ", "");
				contact_name = in.getString("contact_name");
				announce_sms = in.getInt("announce_sms");
				announce_call = in.getInt("announce_call");
				sms_privacy = in.getBoolean("sms_privacy");
			} catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Save everything :D
	 */
	public void Save(){
		Editor e = sp.edit();
		e.clear();
		for(PhoneAndLabel pal : content){
			e.putString(pal.phone, pal.save());
		}
		e.commit();
	}
	
	public void Load(){
		for(Object map : sp.getAll().values()){
			try{
				String contents = (String)map;
				PhoneAndLabel pal = new PhoneAndLabel();
				pal.load(new JSONObject(contents));
				content.add(pal);
			} catch(Exception e){
				e.printStackTrace(); // Should never happen
			}
		}
	}
	
	SharedPreferences sp;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(Pro.getInstance(PerContactSettings.this).Status == PurchaseStatus.NOT_PURCHASED){
			// HA LOL
			finish();
		}
		this.setContentView(R.layout.per_contact);

		Button btn = (Button)findViewById(R.id.add_contact);
		btn.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				startActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), PICK_RESULT);
			}
		});
		
		sp = this.getSharedPreferences("PerContact", Context.MODE_PRIVATE);
		
		content = new ArrayList<PhoneAndLabel>();
		pcs_pald = new PhoneAndLabelAdapter(PerContactSettings.this,
        		android.R.layout.test_list_item, 0, content);
		pcs_pald.numberLabel = false;
		
		Load();
		
		ListView lv = (ListView) findViewById(R.id.list);
		lv.setAdapter(pcs_pald);
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			
			void setupSeekBar(SmoothSeekBar sb){
				sb.values = new String[]{
						getResources().getString(R.string.speak),
						getResources().getString(R.string.default_opt),
						getResources().getString(R.string.dont_speak)
				};
			}

			public void onItemClick(AdapterView<?> arg0, View arg1, final int location,
					long arg3) {
				AlertDialog.Builder ab = new AlertDialog.Builder(PerContactSettings.this);
				final PhoneAndLabel pal = content.get(location);
				
				View v = ((LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
						R.layout.per_contact_dlg,
						null);
				
				ab.setTitle(getResources().getString(R.string.settings_for_contact).replace("{name}", pal.contact_name)
						.replace("{line}", pal.label));
				
				SmoothSeekBar sb = (SmoothSeekBar)v.findViewById(R.id.seek);
				setupSeekBar(sb);
				sb.currentValue = pal.announce_sms;
				sb.onValueChanged = new SmoothSeekBar.OnValueChanged() {
					@Override
					public void ValueChanged(SmoothSeekBar seekbar, Integer new_value) {
						pal.announce_sms = new_value;
						content.set(location, pal);
						Save();
					}
				};
				
				sb = (SmoothSeekBar)v.findViewById(R.id.seek_calls);
				setupSeekBar(sb);
				sb.currentValue = pal.announce_call;
				sb.onValueChanged = new SmoothSeekBar.OnValueChanged() {
					@Override
					public void ValueChanged(SmoothSeekBar seekbar, Integer new_value) {
						pal.announce_call = new_value;
						content.set(location, pal);
						Save();
					}
				};
				
				CheckBox cb = (CheckBox)v.findViewById(R.id.sms_privacy);
				cb.setChecked(pal.sms_privacy);
				cb.setOnCheckedChangeListener(new OnCheckedChangeListener(){

					public void onCheckedChanged(CompoundButton arg0,
							boolean new_value) {
						pal.sms_privacy = new_value;
						content.set(location, pal);
						Save();
					}
					
				});
				
				ab.setView( v );
				ab.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						pcs_pald.notifyDataSetChanged();
					}
				});
				ab.setNegativeButton(R.string.delete, new DialogInterface.OnClickListener() {
					
					public void onClick(final DialogInterface dialog, int which) {
						AlertDialog.Builder ab = new AlertDialog.Builder(PerContactSettings.this);
						ab.setMessage(R.string.are_you_sure);
						ab.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
							
							public void onClick(DialogInterface dialog2, int which) {
								dialog2.dismiss();
							}
						});
						ab.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
							
							public void onClick(DialogInterface dialog2, int which) {
								dialog2.dismiss();
								dialog.dismiss();
								content.remove(location);
								Save();
								pcs_pald.notifyDataSetChanged();
							}
						});
						ab.show();
					}
				});
				ab.show();
			}
		});
	}
}