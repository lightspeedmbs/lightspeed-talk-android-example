package co.herxun.tinichat.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import co.herxun.tinichat.R;
import co.herxun.tinichat.TinichatApplication;
import co.herxun.tinichat.Utils;
import co.herxun.tinichat.activity.ChatActivity;

import com.arrownock.exception.ArrownockException;
import com.arrownock.im.callback.AnIMAddClientsCallbackData;
import com.arrownock.im.callback.AnIMCreateTopicCallbackData;
import com.arrownock.im.callback.AnIMGetTopicListCallbackData;

public class TopicListFragment extends Fragment {
	private TinichatApplication mTA;
	private ArrayList<HashMap<String, String>> topicsList;
	private SimpleAdapter topicsListAdapter;
	private Intent intent ;
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_topiclist,container, false);
			return rootView;
		}
		
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			
			mTA = (TinichatApplication) getActivity().getApplicationContext();

			topicsList = new ArrayList<HashMap<String, String>>();
			topicsListAdapter = new SimpleAdapter(getActivity(), topicsList,
					R.layout.listview_item, new String[] { "name", "parties_count" },
					new int[] { R.id.idText, R.id.statusText });
			ListView friendsListView = (ListView) getActivity().findViewById(R.id.topicsListView);
			friendsListView.setAdapter(topicsListAdapter);
			friendsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				public void onItemClick(AdapterView parent, View view, int position,long id) {
					intent = new Intent();
					String itemId = topicsList.get(position).get("id");
					intent.putExtra("targetId", itemId);
					intent.putExtra("type", Utils.Constant.RoomType.TOPIC);
					intent.setClass(getActivity(), ChatActivity.class);
					Set<String> userSet = new HashSet<String>();
					userSet.add(mTA.mClientId);
					try {
						mTA.anIM.addClientsToTopic(itemId,userSet);
					} catch (ArrownockException e) {
						e.printStackTrace();
					}
				}
			});
			
			Button createTopicText = (Button)getActivity().findViewById(R.id.btn_create);
			createTopicText.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					showTopicCreateDialog();
				}
			});
			
			try {
				mTA.anIM.getTopicList();
			} catch (ArrownockException e) {
				e.printStackTrace();
				Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
			}
		}
		
		@Override
		public void onResume(){
			super.onResume();
			getActivity().setProgressBarIndeterminateVisibility(true);
			try {
				mTA.anIM.getTopicList();
			} catch (ArrownockException e) {
				e.printStackTrace();
				Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
			}
		}
		private void showTopicCreateDialog(){
			AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
			final EditText editText = new EditText(getActivity());
			editText.setPadding(30, 30, 30, 30);
			editText.setGravity(Gravity.CENTER);
			editText.setHint("Topic Name");
			adb.setView(editText);
			adb.setNegativeButton("Cancel", null);
			adb.setPositiveButton("Create", new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					try {
						mTA.anIM.createTopic(editText.getText().toString());
					} catch (ArrownockException e) {
						e.printStackTrace();
						Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
					}
					
				}
			});
			adb.show();
			
		}
		
		public void addClientsToTopic(AnIMAddClientsCallbackData data){
			if(data.getException()!=null){
				Log.e("addClientsToTopic",data.getException().getMessage());
			}else{
				startActivity(intent);
			}
		}
		
		public void getTopicList(final AnIMGetTopicListCallbackData data){
			if(data.isError()){
				getActivity().runOnUiThread(new Runnable(){
					public void run() {
						Toast.makeText(getActivity(), data.getException().getMessage(), Toast.LENGTH_LONG).show();
					}
				});
			}
			topicsList.clear();
			mTA.mTopicsMap.clear();
			List<JSONObject> mTopiceList = data.getTopicList();
			if(mTopiceList!=null){
				for(JSONObject topic : mTopiceList){
					HashMap<String,String> item = new HashMap<String,String>();
					try {
						item.put("id", topic.getString("id"));
						item.put("name", topic.getString("name"));
						item.put("parties_count", topic.getString("parties_count")+" people");
						topicsList.add(item);
						mTA.mTopicsMap.put(topic.getString("id"), topic.getString("name"));
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
			getActivity().runOnUiThread(new Runnable(){
				public void run() {
					topicsListAdapter.notifyDataSetChanged();
					getActivity().setProgressBarIndeterminateVisibility(false);
				}
			});
		}
		
		public void createTopic(final AnIMCreateTopicCallbackData data){
			if(data.isError()){
				getActivity().runOnUiThread(new Runnable(){
					public void run() {
						Toast.makeText(getActivity(), data.getException().getMessage(), Toast.LENGTH_LONG).show();
					}
				});
			}else{
				Log.e("data.getTopic()",data.getTopic());
				
				Intent i = new Intent();
				i.putExtra("targetId", data.getTopic());
				i.putExtra("type", Utils.Constant.RoomType.TOPIC);
				i.setClass(getActivity(), ChatActivity.class);
				startActivity(i);
				
			}
		}
	}