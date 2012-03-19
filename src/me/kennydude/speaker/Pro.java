package me.kennydude.speaker;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.util.Log;


/**
 * Pro tools
 * @author kennydude
 *
 */
public class Pro {
	public static final String pubkey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhjIyrXrMyPxADNLdZQysBSk96VfOo5biJMqd3hT0sOCAxPD1U4pPm3UixShB6tX9uvJ7mmaBi4NtyVfK6rIQDgEnFZ/IeBYGoAeuDtLlTUIm9O/0dviRlgJ+hGSSeSkqWR7gBwhn/rJB1mP+grXtXwMXODIcgezeNUJBhJ2Lt7iSgd/70FUGb097FN6ApJUyWtiLYoUPDATMfEpf29i/2MFUntTV0Nm9lEbV2RJ0comq3vPP2t+DaVdG4P/K6Q5pi2rr+0X4Kni3qxiNLF8TellzMX1xBetcDcvMOZi/Z7o4i/A1mkZ4ryeCm3RTEJ//W7lGmNQKSvlk33mdcdeaRQIDAQAB";
	
	static Pro pro;
	
	public EventHandler UpdateStatus;
	public PurchaseStatus Status = PurchaseStatus.NOT_PURCHASED;
	
	UpdateRecv recv;
	Context cn;
	
	public static Pro getInstance(Context c){
		if(pro == null){
			pro = new Pro(c);
		}
		return pro;
	}
	
	public Pro(Context cntxt){
		cn = cntxt;
		recv = new UpdateRecv();
		
		IntentFilter updater = new IntentFilter();
		updater.addAction("me.kennydude.speaker.PROCHECK");
		cntxt.registerReceiver(recv, updater);
	}
	
	public void check(){
		try{
			cn.getPackageManager().getPackageInfo("me.kennydude.speaker.pro", 0);
		} catch(Exception e){
			Status = PurchaseStatus.NOT_PURCHASED;
			UpdateStatus.onEvent();
			return;
		}
		
		Status = PurchaseStatus.PROCESSING;
		UpdateStatus.onEvent();
		
		Intent intent = new Intent();
		intent.setComponent(new ComponentName("me.kennydude.speaker.pro", "me.kennydude.speaker.pro.ProCheckService"));
		intent.putExtra("pubkey", pubkey);
		cn.startService(intent);
	}
	
	/**
	 * Deregister Broadcaster
	 */
	public void close(Context cntxt){
		cntxt.unregisterReceiver(recv);
	}
	
	public static abstract class EventHandler{
		public abstract void onEvent();
	}

	public void purchase() {
		Intent purchase = new Intent(Intent.ACTION_VIEW);
		purchase.setData(Uri.parse("http://market.android.com/details?id=me.kennydude.speaker.pro"));
		cn.startActivity(purchase);
	}
	
	class UpdateRecv extends BroadcastReceiver{

		@Override
		public void onReceive(Context arg0, Intent intent) {
			String r = intent.getStringExtra("response");
			if(r.equals("ALLOW")){
				Status = PurchaseStatus.PURCHASED;
			} else if(r.equals("ERROR")){
				Status = PurchaseStatus.ERROR;
			} else if(r.equals("RETRY")){
				Status = PurchaseStatus.ERROR;
			}
			UpdateStatus.onEvent();
		}
	}
	
}
