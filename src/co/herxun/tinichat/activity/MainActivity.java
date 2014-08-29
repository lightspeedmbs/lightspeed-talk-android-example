package co.herxun.tinichat.activity;

import java.util.Map;

import org.json.JSONObject;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import co.herxun.tinichat.AppSectionsPagerAdapter;
import co.herxun.tinichat.R;
import co.herxun.tinichat.TinichatApplication;
import co.herxun.tinichat.Utils;
import co.herxun.tinichat.fragment.FriendsListFragment;
import co.herxun.tinichat.fragment.TopicListFragment;

import com.arrownock.exception.ArrownockException;
import com.arrownock.im.callback.AnIMAddClientsCallbackData;
import com.arrownock.im.callback.AnIMBinaryCallbackData;
import com.arrownock.im.callback.AnIMCallbackAdapter;
import com.arrownock.im.callback.AnIMCreateTopicCallbackData;
import com.arrownock.im.callback.AnIMGetClientIdCallbackData;
import com.arrownock.im.callback.AnIMGetClientsStatusCallbackData;
import com.arrownock.im.callback.AnIMGetTopicListCallbackData;
import com.arrownock.im.callback.AnIMMessageCallbackData;
import com.arrownock.im.callback.AnIMStatusUpdateCallbackData;
import com.arrownock.im.callback.AnIMTopicBinaryCallbackData;
import com.arrownock.im.callback.AnIMTopicMessageCallbackData;
import com.arrownock.mrm.MRMJSONResponseHandler;

public class MainActivity extends FragmentActivity{
	private AppSectionsPagerAdapter mAppSectionsPagerAdapter;
	private ViewPager mViewPager;
	private TinichatApplication mTA;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem item = menu.add("Log Out");
		item.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				logout();
				return true;
			}
		});
		return true;
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_main);
		
		mTA = TinichatApplication.getInstance(this);
		showLoginDialog();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mTA.anIM.setCallback(null);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mTA.anIM.setCallback(mAnIMCallback);
	}
	
	/**
	 * Dialog requesting log in or register
	 */
	private void showLoginDialog(){
		AlertDialog.Builder mLoginDialog = new AlertDialog.Builder(this);
		final LinearLayout mLl = new LinearLayout(this);
		mLl.setLayoutParams(new LayoutParams(-1,-1));
		mLl.setOrientation(LinearLayout.VERTICAL);
		mLl.setPadding(50, 20, 50, 50);
		final EditText mEditID = new EditText(this);
		mEditID.setLayoutParams(new LayoutParams(-1,-2));
		mEditID.setGravity(Gravity.CENTER_HORIZONTAL);
		mEditID.setHint("Account Name");
		mLl.addView(mEditID);
		final EditText mEditPwd = new EditText(this);
		mEditPwd.setLayoutParams(new LayoutParams(-1,-2));
		mEditPwd.setGravity(Gravity.CENTER_HORIZONTAL);
		mEditPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
		mEditPwd.setHint("Password");
		mLl.addView(mEditPwd);
		mLoginDialog.setView(mLl);
		mLoginDialog.setCancelable(false);
		mLoginDialog.setPositiveButton("Sign Up", new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				loginSignup(mEditID.getText().toString(),mEditPwd.getText().toString(),"users/create");
			}
		});
		mLoginDialog.setNegativeButton("Log In",  new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				loginSignup(mEditID.getText().toString(),mEditPwd.getText().toString(),"users/login");
			}
		});
		mLoginDialog.show();
	}
	
	/**
	 * @param username Username
	 * @param pwd Password
	 * @param action Log in "users/login" ; Register "users/create"
	 */
	private void loginSignup(final String username,final String pwd,final String action){
		try {
		    JSONObject params = new JSONObject();
		    params.put("username", username);
		    params.put("password", pwd); 
		    
		    mTA.mrm.sendPostRequest(getBaseContext(), action, params,
		        new MRMJSONResponseHandler() {
		            @Override
		            public void onFailure(Throwable e, JSONObject response) {
		                try {
		                	String errorMsg = response.getJSONObject("meta").getString("message");
		                	Toast.makeText(getBaseContext(), errorMsg, Toast.LENGTH_LONG).show();
		                    System.out.println("the error message: " + errorMsg);
		                    showLoginDialog();
		                } catch (Exception e1) {}
		            }
		            @Override
		            public void onSuccess(int statusCode, JSONObject response) {
		                try {
		                	String circleId = response.getJSONObject("response").getJSONObject("user").getString("id");
		                    System.out.println("user id is: " + circleId);
		                    if(action.equals("users/create")){
		                    	loginSignup(username,pwd,"users/login");
		                    }else if(action.equals("users/login")){
		                    	mTA.mUsername = username;
		                    	mTA.mCircleId = circleId;
		                    	mTA.anIM.getClientId(circleId);
		                    }else{
		                    }
		                } catch (Exception e1) {}
		            }
		    });

		} catch (Exception e) {

		}  
	}
	
	/**
	 * Update clientId to Lightspeed Social database
	 */
	private void updateUser(final String circleId,final String clientID){
		try {
		    JSONObject params = new JSONObject();
		    params.put("id",circleId);
		    JSONObject customFields = new JSONObject();
		    customFields.put("clientId",clientID);
		    params.put("customFields",customFields);
		    
		    mTA.mrm.sendPostRequest(getBaseContext(), "users/update", params,
		        new MRMJSONResponseHandler() {
		            @Override
		            public void onFailure(Throwable e, JSONObject response) {
		                try {
		                	String errorMsg = response.getJSONObject("meta").getString("message");
		                	Toast.makeText(getBaseContext(), errorMsg, Toast.LENGTH_LONG).show();
		                    System.out.println("the error message: " + errorMsg);
		                } catch (Exception e1) {}
		            }
		            @Override
		            public void onSuccess(int statusCode, JSONObject response) {
		                try {
		                    System.out.println("updateUser: " + response.getJSONObject("response").getJSONObject("user").toString());
		                    runOnUiThread(new Runnable(){
								public void run() {
				                    afterLogin();
								}
							});
		                } catch (Exception e1) {}
		            }
		    });

		} catch (Exception e) {

		}  
	}
	
	/**
	 * After clientId has been successfully update to Lightspeed Social database。
	 * 
	 * 1.Initializee ViewPager
	 * 2.Connect to Lightspeed Talk server
	 */
	private void afterLogin(){
		mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager());
		final ActionBar actionBar = getActionBar();
		actionBar.show();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setOnPageChangeListener(new OnPageChangeListener(){
			public void onPageScrollStateChanged(int arg0) {}
			public void onPageScrolled(int arg0, float arg1, int arg2) {}
			@Override
			public void onPageSelected(int arg0) {
				actionBar.selectTab(actionBar.getTabAt(arg0));
				mAppSectionsPagerAdapter.getItem(arg0).onResume();	//Workaround to solve issue that Fragment's onResume() will not be called when using ViewPager
			}
		});
		mViewPager.setAdapter(mAppSectionsPagerAdapter);
		for (int i = 0; i < mAppSectionsPagerAdapter.getCount(); i++) {
			actionBar.addTab(actionBar.newTab()
					.setText(mAppSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(new TabListener(){
						@Override
						public void onTabSelected(Tab tab,FragmentTransaction ft) {
							mViewPager.setCurrentItem(tab.getPosition());
						}
						public void onTabUnselected(Tab tab,FragmentTransaction ft) {}
						public void onTabReselected(Tab tab,FragmentTransaction ft) {}
					}));
		}
		try {
			mTA.anIM.connect(mTA.mClientId);	
		} catch (ArrownockException e) {
			e.printStackTrace();
			Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}
	
	private AnIMCallbackAdapter mAnIMCallback = new AnIMCallbackAdapter() {
		@Override
		public void getClientId(final AnIMGetClientIdCallbackData data) {
			if(data.isError()){
				runOnUiThread(new Runnable(){
					public void run() {
						Toast.makeText(getBaseContext(), data.getException().getMessage(), Toast.LENGTH_LONG).show();
					}
				});
			}else{
				mTA.mClientId= data.getClientId();
				updateUser(mTA.mCircleId,mTA.mClientId);
			}
			
		}
		
		@Override
		public void getClientsStatus(final AnIMGetClientsStatusCallbackData data){
			((FriendsListFragment)mAppSectionsPagerAdapter.getItem(Utils.Constant.Fragment.ALL_USERS)).getClientStatus(data);
		}
		
		@Override
		public void getTopicList(final AnIMGetTopicListCallbackData data){
			((TopicListFragment)mAppSectionsPagerAdapter.getItem(Utils.Constant.Fragment.TOPICS)).getTopicList(data);
		}
		
		@Override
		public void createTopic(final AnIMCreateTopicCallbackData data){
			((TopicListFragment)mAppSectionsPagerAdapter.getItem(Utils.Constant.Fragment.TOPICS)).createTopic(data);
			
		}
		
		@Override
		public void addClientsToTopic(AnIMAddClientsCallbackData data){
			((TopicListFragment)mAppSectionsPagerAdapter.getItem(Utils.Constant.Fragment.TOPICS)).addClientsToTopic(data);
		}
		
		@Override
		public void receivedTopicMessage(AnIMTopicMessageCallbackData data) {
			final String from = data.getFrom();
			final String fromTopic = data.getTopic();
			final String message = data.getMessage();
			final Map<String, String> customData = data.getCustomData();
			Log.d("Chat,MessageCallback", "received message: " + message);
			Log.d("Chat,MessageCallback","received link: " + customData.get("link"));
			Log.d("Chat,MessageCallback","received type: " + customData.get("type"));
			
			runOnUiThread(new Runnable() {
				public void run() {
					if (customData.containsKey("type")&& !customData.get("type").equals("")){
						Toast.makeText(getBaseContext(),"["+mTA.mTopicsMap.get(fromTopic)+"] "+mTA.mUsersMap.get(from)+" : [" + customData.get("type") + "]", Toast.LENGTH_LONG).show();
					}else{
						Toast.makeText(getBaseContext(),"["+mTA.mTopicsMap.get(fromTopic)+"] "+mTA.mUsersMap.get(from)+" : "+ message, Toast.LENGTH_LONG).show();
					}
				}
			});

		}

		@Override
		public void receivedMessage(AnIMMessageCallbackData data) {
			final String from = data.getFrom();
			final String message = data.getMessage();
			final Map<String, String> customData = data.getCustomData();
			Log.d("Chat,MessageCallback", "received message: " + message);
			Log.d("Chat,MessageCallback", "received link: " + customData.get("link"));
			Log.d("Chat,MessageCallback", "received type: " + customData.get("type"));
			runOnUiThread(new Runnable() {
				public void run() {
					if (customData.containsKey("type")&& !customData.get("type").equals("")){
						Toast.makeText(getBaseContext(),mTA.mUsersMap.get(from)+" : [" + customData.get("type") + "]", Toast.LENGTH_LONG).show();
					}else{
						Toast.makeText(getBaseContext(),mTA.mUsersMap.get(from)+" : " + message, Toast.LENGTH_LONG).show();
					}
				} 
			});
		}
		
		@Override
		public void receivedBinary(final AnIMBinaryCallbackData data){
			runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(getBaseContext(),mTA.mUsersMap.get(data.getFrom())+" : ["+data.getFileType()+"]", Toast.LENGTH_LONG).show();
				}
			});
		}
		
		@Override
		public void receivedTopicBinary(final AnIMTopicBinaryCallbackData data){
			final String fromTopic = data.getTopic();
			runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(getBaseContext(),"["+mTA.mTopicsMap.get(fromTopic)+"] "+mTA.mUsersMap.get(data.getFrom())+" : ["+data.getFileType()+"]", Toast.LENGTH_LONG).show();
				}
			});
		}
		
		@Override
		public void statusUpdate(final AnIMStatusUpdateCallbackData data){
			final ArrownockException e = data.getException();
			if(e == null){
				runOnUiThread(new Runnable(){
					public void run() {
						Toast.makeText(getBaseContext(), data.getStatus().name(), Toast.LENGTH_LONG).show();
					}
				});
			}else{
				runOnUiThread(new Runnable(){
					public void run() {
						Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
					}
				});
				if (data.getException().getErrorCode() == ArrownockException.IM_FORCE_CLOSED
						|| data.getException().getErrorCode() == ArrownockException.IM_FAILED_DISCONNECT) {	
					logout();
				}
			}
		}
	};

	private void logout() {
		try {
			mTA.mrm.sendPostRequest(getBaseContext(), "users/logout", null,new MRMJSONResponseHandler());
			mTA.anIM.disconnect();
		} catch (ArrownockException e) {
			e.printStackTrace();
		}
		Intent it = new Intent();
		it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		it.setClass(getBaseContext(), MainActivity.class);
		startActivity(it);
		finish();
	}
}