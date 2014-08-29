package co.herxun.tinichat;

import java.util.HashMap;
import java.util.Map;

import com.arrownock.exception.ArrownockException;
import com.arrownock.im.AnIM;
import com.arrownock.mrm.MRM;

import android.app.Application;
import android.content.Context;
import android.util.Log;

/**
 * Global variables that often used
 */
public class TinichatApplication extends Application {
	
	public AnIM anIM;	/** Lightspeed Talk component */
	public MRM mrm;		/** Lightspeed Social component */
	
	public String mClientId;
	public String mUsername;
	public String mCircleId;
	
	/** Map of clientId and userName，make searching userName by clientId faster*/
	public Map<String,String> mUsersMap;	
	/** Map of topicId and topicName，make searching topicName by topicId faster*/
	public Map<String,String> mTopicsMap;	
	
	public void init() {
		try {
			anIM = new AnIM(this);
			mrm = new MRM(this);
			mUsersMap = new HashMap<String,String>();
			mTopicsMap = new HashMap<String,String>();
		} catch (ArrownockException e) {
			Log.e("TinichatApplication", e.getMessage());
		}
		
	}
	
	public static TinichatApplication getInstance(Context ct){
		TinichatApplication mTA = (TinichatApplication) ct.getApplicationContext();
		mTA.init();
		return mTA;
	}
}
