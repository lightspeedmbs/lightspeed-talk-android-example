package co.herxun.tinichat.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import co.herxun.tinichat.R;
import co.herxun.tinichat.TinichatApplication;
import co.herxun.tinichat.Utils;
import co.herxun.tinichat.activity.ChatActivity;

import com.arrownock.exception.ArrownockException;
import com.arrownock.im.callback.AnIMGetClientsStatusCallbackData;
import com.arrownock.im.callback.IAnIMGetClientsStatusCallback;
import com.arrownock.social.AnSocialMethod;
import com.arrownock.social.IAnSocialCallback;

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
			public void onItemClick(AdapterView parent, View view, int position, long id) {
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
	
	private void getAllUsersList() {
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("limit", 100);
		params.put("sort", "-created_at");
		try {
			mTA.anSocial.sendRequest("users/query.json", AnSocialMethod.GET,
					params, new IAnSocialCallback() {
						@Override
						public void onFailure(final JSONObject arg0) {
							getActivity().runOnUiThread(new Runnable() {
								public void run() {
									try {
										Toast.makeText(	getActivity(),arg0.getJSONObject("meta").getString("message"),Toast.LENGTH_LONG).show();
										System.out.println("the error message: "+ arg0.getJSONObject("meta").getString("message"));
									} catch (Exception e1) {
									}
								}
							});
						}

						@Override
						public void onSuccess(final JSONObject arg0) {
							Log.e("getAllUsersList", arg0.toString());
							try {
								final JSONArray usersArray = arg0.getJSONObject("response").getJSONArray("users");
								getActivity().runOnUiThread(new Runnable() {
									public void run() {
										mTA.mUsersMap.clear();
										partiesList.clear();
										friendsListAdapter.notifyDataSetChanged();
										for (int i = 0; i < usersArray.length(); i++) {
											try {
												JSONObject user = (JSONObject) usersArray.get(i);
												if (user.getString("id").equals(mTA.mCircleId)) {
													continue;
												}
												HashMap<String, String> item = new HashMap<String, String>();
												item.put("clientId",user.getString("clientId"));
												item.put("username",user.getString("username"));
												item.put("status", "Offline");
												partiesList.add(item);
												
												friendsListAdapter.notifyDataSetChanged();
												
												mTA.mUsersMap.put(user.getString("clientId"),user.getString("username"));
											} catch (Exception e) {
												e.printStackTrace();
											}
										}
										mTA.anIM.getClientsStatus(mTA.mUsersMap.keySet(), new IAnIMGetClientsStatusCallback() {
											@Override
											public void onSuccess(AnIMGetClientsStatusCallbackData anIMGetClientsStatusCallbackData) {
												getClientStatus(anIMGetClientsStatusCallbackData);
											}

											@Override
											public void onError(ArrownockException e) {
												e.printStackTrace();
											}
										});
									}
								});
								

							} catch (Exception e) {
								e.printStackTrace();
								try {
									Toast.makeText(getActivity(),e.getMessage(), Toast.LENGTH_LONG).show();
								} catch (Exception e1) {
								}
							}
						}
					});
		} catch (ArrownockException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 *  Update user's status
	 */
	public void getClientStatus(final AnIMGetClientsStatusCallbackData data){
		if(data.isError()){
			if (getActivity() != null) {
				getActivity().runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(getActivity(), data.getException().getMessage(), Toast.LENGTH_LONG).show();
					}
				});
			}
		}else{
			if (getActivity() != null) {
				getActivity().runOnUiThread(new Runnable() {
					public void run() {
						Map<String, Boolean> clientStatusMap = data.getClientsStatus();
						for (int i = 0; i < partiesList.size(); i++) {
							HashMap<String, String> user = partiesList.get(i);
							if (clientStatusMap.containsKey(user.get("clientId"))) {
								user.remove("status");
								user.put("status", clientStatusMap.get(user.get("clientId")) ? "Online" : "Offline");
								partiesList.remove(i);
								partiesList.add(i, user);
								friendsListAdapter.notifyDataSetChanged();
							}
						}
						getActivity().setProgressBarIndeterminateVisibility(false);
					}
				});
			}
		}
	}
}
