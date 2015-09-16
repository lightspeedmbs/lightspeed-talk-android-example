package co.herxun.tinichat.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBar.TabListener;
import android.support.v7.app.ActionBarActivity;
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

import com.arrownock.exception.ArrownockException;
import com.arrownock.im.callback.AnIMBinaryCallbackData;
import com.arrownock.im.callback.AnIMCallbackAdapter;
import com.arrownock.im.callback.AnIMMessageCallbackData;
import com.arrownock.im.callback.AnIMStatusUpdateCallbackData;
import com.arrownock.im.callback.AnIMTopicBinaryCallbackData;
import com.arrownock.social.AnSocialMethod;
import com.arrownock.social.IAnSocialCallback;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import co.herxun.tinichat.AppSectionsPagerAdapter;
import co.herxun.tinichat.R;
import co.herxun.tinichat.TinichatApplication;
import co.herxun.tinichat.Utils;

public class MainActivity extends ActionBarActivity{
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
				signUp(mEditID.getText().toString(),mEditPwd.getText().toString());
			}
		});
		mLoginDialog.setNegativeButton("Log In",  new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				login(mEditID.getText().toString(),mEditPwd.getText().toString());
			}
		});
		mLoginDialog.show();
	}
	
	/**
	 * @param username Username
	 * @param pwd Password
	 */
	private void login(final String username,final String pwd){
		final Map<String, Object> params = new HashMap<String, Object>();
	    params.put("username", username);
	    params.put("password", pwd); 
		try {
			mTA.anSocial.sendRequest("users/auth.json", AnSocialMethod.POST, params, new IAnSocialCallback(){
				@Override
				public void onFailure(JSONObject arg0) {
					 try {
		                	String errorMsg = arg0.getJSONObject("meta").getString("message");
		                	Toast.makeText(getBaseContext(), errorMsg, Toast.LENGTH_LONG).show();
		                    System.out.println("the error message: " + errorMsg);
		                    showLoginDialog();
		                } catch (Exception e1) {}
				}
				@Override
				public void onSuccess(final JSONObject arg0) {
					Log.e("login",arg0.toString());
	                try {
	                	String userId = arg0.getJSONObject("response").getJSONObject("user").getString("id");
	                	String userName = arg0.getJSONObject("response").getJSONObject("user").getString("username");
	                	String clientId = arg0.getJSONObject("response").getJSONObject("user").getString("clientId");
	                    System.out.println("user id is: " + userId);
                    	mTA.mUsername = userName;
                    	mTA.mCircleId = userId;
                    	mTA.mClientId = clientId;
                    	afterLogin();
	                } catch (Exception e1) {}
				}
			});
		} catch (ArrownockException e) {
			e.printStackTrace();
		}
	}
	
	private void signUp(final String username,final String pwd){
		final Map<String, Object> params = new HashMap<String, Object>();
	    params.put("username", username);
	    params.put("password", pwd); 
	    params.put("password_confirmation", pwd); 
	    params.put("enable_im", true); 
		try {
			mTA.anSocial.sendRequest("users/create.json", AnSocialMethod.POST, params, new IAnSocialCallback(){
				@Override
				public void onFailure(JSONObject arg0) {
					 try {
		                	String errorMsg = arg0.getJSONObject("meta").getString("message");
		                	Toast.makeText(getBaseContext(), errorMsg, Toast.LENGTH_LONG).show();
		                    System.out.println("the error message: " + errorMsg);
		                    showLoginDialog();
		                } catch (Exception e1) {}
				}
				@Override
				public void onSuccess(final JSONObject arg0) {
					Log.e("signUp",arg0.toString());
	                try {
	                	String userId = arg0.getJSONObject("response").getJSONObject("user").getString("id");
	                	String userName = arg0.getJSONObject("response").getJSONObject("user").getString("username");
	                	String clientId = arg0.getJSONObject("response").getJSONObject("user").getString("clientId");
	                    System.out.println("user id is: " + userId);
                    	mTA.mUsername = userName;
                    	mTA.mCircleId = userId;
                    	mTA.mClientId = clientId;
                    	afterLogin();
	                } catch (Exception e1) {}
				}
			});
		} catch (ArrownockException e) {
			e.printStackTrace();
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
		final ActionBar actionBar = getSupportActionBar();
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
						public void onTabUnselected(Tab arg0,FragmentTransaction arg1) {}
						public void onTabReselected(Tab arg0,FragmentTransaction arg1) {}
						@Override
						public void onTabSelected(Tab tab,FragmentTransaction arg1) {
							mViewPager.setCurrentItem(tab.getPosition());
						}
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
		public void receivedMessage(AnIMMessageCallbackData data) {
			final String from = data.getFrom();
			final String message = data.getMessage();
			final Map<String, String> customData = data.getCustomData();
			final String type = customData==null ?
					Utils.Constant.AttachmentType.TEXT:customData.get(Utils.Constant.MsgCustomData.TYPE);
			Log.d("Chat,MessageCallback", "received message: " + message);
			runOnUiThread(new Runnable() {
				public void run() {
					if (type.equals(Utils.Constant.AttachmentType.TEXT)){
						Toast.makeText(getBaseContext(),mTA.mUsersMap.get(from)+" : " + message, Toast.LENGTH_LONG).show();
					}else{
						Toast.makeText(getBaseContext(),mTA.mUsersMap.get(from)+" : [" + type + "]", Toast.LENGTH_LONG).show();
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