package co.herxun.tinichat.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.arrownock.exception.ArrownockException;
import com.arrownock.im.callback.AnIMCallbackAdapter;
import com.arrownock.im.callback.AnIMGetClientIdCallbackData;
import com.arrownock.im.callback.AnIMGetClientsStatusCallbackData;
import com.arrownock.im.callback.AnIMGetTopicListCallbackData;
import com.arrownock.im.callback.AnIMMessageCallbackData;
import com.arrownock.im.callback.AnIMStatusUpdateCallbackData;
import com.arrownock.im.callback.AnIMTopicMessageCallbackData;
import com.arrownock.mrm.MRMJSONResponseHandler;

import co.herxun.tinichat.R;
import co.herxun.tinichat.Utils;
import co.herxun.tinichat.R.layout;
import co.herxun.tinichat.activity.ChatActivity;
import co.herxun.tinichat.activity.MainActivity;
import co.herxun.tinichat.TinichatApplication;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class FriendsListFragment extends Fragment {
	private TinichatApplication mTA;
	private SimpleAdapter friendsListAdapter;
    private ArrayList<HashMap<String, String>> partiesList;
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_friendslist,container, false);
		return rootView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		mTA = (TinichatApplication) getActivity().getApplicationContext();
		
		partiesList = new ArrayList<HashMap<String, String>>();
		friendsListAdapter = new SimpleAdapter(getActivity(), partiesList,
				R.layout.listview_item, new String[] { "username", "status" },
				new int[] { R.id.idText, R.id.statusText });
		ListView friendsListView = (ListView) getActivity().findViewById(R.id.friendsListView);
		friendsListView.setAdapter(friendsListAdapter);
		friendsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView parent, View view, int position,long id) {
				Intent intent = new Intent();
				String itemId = partiesList.get(position).get("clientId");
				intent.putExtra("targetId", itemId);
				intent.putExtra("type", Utils.Constant.RoomType.CLIENT);
				intent.setClass(getActivity(), ChatActivity.class);
				startActivity(intent);
			}
		});
	}
	@Override
	public void onResume(){
		super.onResume();
		getActivity().setProgressBarIndeterminateVisibility(true);
		Thread t = new Thread(new Runnable(){
			@Override
			public void run() {
				getAllUsersList();
			}
		});
		t.start();
	}
	
	private void getAllUsersList(){
		try {
		JSONObject params = new JSONObject();
		params.put("pagesize", 100);
			mTA.mrm.sendPostRequest(getActivity(), "users/search", params, 
		        new MRMJSONResponseHandler() {
		    		@Override
		            public void onFailure(Throwable e, final JSONObject response) {
		    			getActivity().runOnUiThread(new Runnable(){
		    				public void run() {
		    					try {
				    				Toast.makeText(getActivity(),response.getJSONObject("meta").getString("message"), Toast.LENGTH_LONG).show();
				                    System.out.println("the error message: " + response.getJSONObject("meta").getString("message"));
				                } catch (Exception e1) {}
		    				}
		    			});
		                
		            }
		    		@Override
		    		public void onSuccess(int statusCode, JSONObject response) {
		    			try {
		                    JSONArray usersArray = response.getJSONObject("response").getJSONArray("users");		               
		                    mTA.mUsersMap.clear();
		                    partiesList.clear();
		                    for (int i = 0; i < usersArray.length(); i++) {
		                        JSONObject user = (JSONObject) usersArray.get(i);
		                        if(user.getString("id").equals(mTA.mCircleId)){
		                        	continue;
		                        }
		                        HashMap<String, String> item = new HashMap<String, String>();
		            			item.put("clientId", (String) user.getJSONObject("customFields").getString("clientId"));
		            			item.put("username", (String) user.get("username"));
		            			item.put("status", "Offline");
		            			partiesList.add(item);		
		            			
		            			mTA.mUsersMap.put((String) user.getJSONObject("customFields").getString("clientId"),  (String) user.get("username"));
		                    }
		                    
		                    mTA.anIM.getClientsStatus(mTA.mUsersMap.keySet());
		                    
		                } catch (Exception e) {
		                	e.printStackTrace();
		                	try {
			                	Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
			                } catch (Exception e1) {}
		                }
		    		}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 *  Update user's status
	 */
	public void getClientStatus(final AnIMGetClientsStatusCallbackData data){
		if(data.isError()){
			getActivity().runOnUiThread(new Runnable(){
				public void run() {
					Toast.makeText(getActivity(), data.getException().getMessage(), Toast.LENGTH_LONG).show();
				}
			});
		}
		Map<String,Boolean> clientStatusMap = data.getClientsStatus();
		for(int i=0;i<partiesList.size();i++){
			HashMap<String,String> user = partiesList.get(i);
			if(clientStatusMap.containsKey(user.get("clientId"))){
				user.remove("status");
				user.put("status", clientStatusMap.get(user.get("clientId")) ? "Online":"Offline");
				partiesList.remove(i);
				partiesList.add(i, user);
			}
		}
		getActivity().runOnUiThread(new Runnable(){
			public void run() {
				friendsListAdapter.notifyDataSetChanged();
				getActivity().setProgressBarIndeterminateVisibility(false);
			}
		});
	}
}
