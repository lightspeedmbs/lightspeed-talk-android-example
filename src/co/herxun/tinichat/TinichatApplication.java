package co.herxun.tinichat;

import java.util.HashMap;
import java.util.Map;

import com.arrownock.exception.ArrownockException;
import com.arrownock.im.AnIM;
import com.arrownock.social.AnSocial;

import android.app.Application;
import android.content.Context;
import android.util.Log;

/**
 * Global variables that often used
 */
public class TinichatApplication extends Application {
	
	public AnIM anIM;	/** Lightspeed Talk component */
	public AnSocial anSocial;		/** Lightspeed Social component */
	
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
			anSocial = new AnSocial(this, getString(R.string.app_key));
			mUsersMap = new HashMap<String,String>();
			mTopicsMap = new HashMap<String,String>();
		} catch (ArrownockException e) {
			throw new RuntimeException(e.getMessage());
		}
		
	}
	
	public static TinichatApplication getInstance(Context ct){
		TinichatApplication mTA = (TinichatApplication) ct.getApplicationContext();
		mTA.init();
		return mTA;
	}
}
