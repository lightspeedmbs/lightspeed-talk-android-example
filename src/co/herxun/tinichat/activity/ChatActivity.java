package co.herxun.tinichat.activity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import co.herxun.tinichat.HistoryAdapter;
import co.herxun.tinichat.R;
import co.herxun.tinichat.TinichatApplication;
import co.herxun.tinichat.Utils;

import com.arrownock.exception.ArrownockException;
import com.arrownock.im.AnIMMessage;
import com.arrownock.im.AnIMMessageType;
import com.arrownock.im.callback.AnIMBinaryCallbackData;
import com.arrownock.im.callback.AnIMCallbackAdapter;
import com.arrownock.im.callback.AnIMGetTopicInfoCallbackData;
import com.arrownock.im.callback.AnIMMessageCallbackData;
import com.arrownock.im.callback.AnIMMessageSentCallbackData;
import com.arrownock.im.callback.AnIMRemoveClientsCallbackData;
import com.arrownock.im.callback.AnIMStatusUpdateCallbackData;
import com.arrownock.im.callback.AnIMTopicBinaryCallbackData;
import com.arrownock.im.callback.AnIMTopicMessageCallbackData;
import com.arrownock.im.callback.IAnIMHistoryCallback;

public class ChatActivity extends Activity {
	private Button btnSend, btnMore,btnDetail;
	private EditText textEdit;
	private TextView textTargetId;
	private ListView chatHistory;
	
	private TinichatApplication mTA;
	private HistoryAdapter historyAdapter;
	private ArrayList<HashMap<String, Object>> history;

	private int roomType = Utils.Constant.RoomType.DEFAULT;
	private String targetId = "";
	private String mMsg = "";
	private String mAttachment = "";
	private String mAttachmentType = "";
	private byte[] mAttachmentData ;
	private AnIMGetTopicInfoCallbackData mTopicInfo;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		
		mTA = (TinichatApplication) getApplicationContext();
		setViewContent();
		
		Bundle bundle = this.getIntent().getExtras();
		targetId = bundle.containsKey("targetId") ? bundle.getString("targetId") : "";
		roomType = bundle.containsKey("type") ? bundle.getInt("type") : Utils.Constant.RoomType.DEFAULT;
		
		if(roomType == Utils.Constant.RoomType.CLIENT){
			textTargetId.setText(mTA.mUsersMap.get(targetId));
			findViewById(R.id.btn_detail).setVisibility(View.GONE);
		}else{
			try {
				mTA.anIM.getTopicInfo(targetId);
			} catch (ArrownockException e) {
				e.printStackTrace();
			}
		}
		
		Log.i("targetId: "+targetId,"roomType: "+roomType);
	
		getHistory();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mTA.anIM.setCallback(null);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mTA.anIM.setCallback(messagecallback);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			if(roomType == Utils.Constant.RoomType.TOPIC){
				Set<String> userSet = new HashSet<String>();
				userSet.add(mTA.mClientId);
				try {
					mTA.anIM.removeClientsFromTopic(targetId, userSet);
				} catch (ArrownockException e) {
					e.printStackTrace();
				}
			}else{
				finish();
			}
		}
		return false;
	}
	
	public void getHistory() {
		IAnIMHistoryCallback historyCallback = new IAnIMHistoryCallback() {
			@Override
			public void onError(final ArrownockException arg0) {
				Log.e("getHistory", arg0.getMessage());
				runOnUiThread(new Runnable(){
					public void run() {
						Toast.makeText(getBaseContext(), arg0.getMessage(), Toast.LENGTH_LONG).show();
					}
				});
				
			}

			@Override
			public void onSuccess(final List mHistory, int arg1) {
				final List<AnIMMessage> historyList = mHistory;
				runOnUiThread(new Runnable() {
					public void run() {
						history.clear();
						for (int i = historyList.size()-1; i >=0; i--) {
							HashMap<String, Object> item = new HashMap<String, Object>();
							if(historyList.get(i).getType() == AnIMMessageType.AnIMBinaryMessage){
								item.put(Utils.Constant.HistoryItem.FROM, historyList.get(i).getFrom());
								item.put(Utils.Constant.HistoryItem.CONTENT, historyList.get(i).getContent());
								item.put(Utils.Constant.HistoryItem.TYPE, historyList.get(i).getFileType());
							}else{
								Map customData = historyList.get(i).getCustomData();
								if(customData!=null){
									String customData_data = (String) customData.get(Utils.Constant.MsgCustomData.DATA);
									String customData_type = (String) customData.get(Utils.Constant.MsgCustomData.TYPE);
									item.put(Utils.Constant.HistoryItem.FROM, historyList.get(i).getFrom());
									item.put(Utils.Constant.HistoryItem.MSG, historyList.get(i).getMessage());
									if (customData != null) {
										if (customData_data!=null)
											item.put(Utils.Constant.HistoryItem.DATA,customData_data);
										if (customData_type!=null)
											item.put(Utils.Constant.HistoryItem.TYPE,customData_type);
									}
								}else{
									item.put(Utils.Constant.HistoryItem.FROM, historyList.get(i).getFrom());
									item.put(Utils.Constant.HistoryItem.MSG, historyList.get(i).getMessage());
								}
							}
							history.add(item);
						}
						updateHistoryList(true);
					}
				});
			}
		};
		if (roomType == Utils.Constant.RoomType.CLIENT) {
			Set<String> targetIds;
			targetIds = new HashSet();
			targetIds.add(targetId);
			mTA.anIM.getHistory(targetIds, mTA.mClientId, 30, 0, historyCallback);
		} else if (roomType == Utils.Constant.RoomType.TOPIC) {
			mTA.anIM.getTopicHistory(targetId, mTA.mClientId, 30, 0, historyCallback);
		}
	}

	public void updateHistoryList(Boolean scroll) {
		historyAdapter.notifyDataSetChanged();
		if(scroll)
			chatHistory.smoothScrollToPosition(chatHistory.getCount()-1);
	}
	
	
	public void sendMsg(String msg ,String attachment , String attachmentType , byte[] attachmentData) {
		if (!msg.equals("") && msg != null) {
			mMsg = msg;
			mAttachment = attachment;
			mAttachmentType = attachmentType;

			Set<String> targetIds;
			targetIds = new HashSet();
			targetIds.add(targetId);
			
			try {
				if(attachmentType == Utils.Constant.AttachmentType.IMAGE){
					Log.i("send Binary to " + targetIds, "data: " + attachmentData.length);
					if (roomType == Utils.Constant.RoomType.CLIENT) {
						mTA.anIM.sendBinary(targetIds, attachmentData,Utils.Constant.AttachmentType.IMAGE);
					} else if (roomType == Utils.Constant.RoomType.TOPIC) {
						mTA.anIM.sendBinaryToTopic(targetId, attachmentData,Utils.Constant.AttachmentType.IMAGE);
					}
				}else if(attachmentType.equals("")){
					Log.i("send Msg to " + targetIds, "message: " + msg);
					if (roomType == Utils.Constant.RoomType.CLIENT) {
						mTA.anIM.sendMessage(targetIds, msg);
					} else if (roomType == Utils.Constant.RoomType.TOPIC) {
						mTA.anIM.sendMessageToTopic(targetId, msg);
					}
				}else{
					HashMap map = new HashMap();
					map.put(Utils.Constant.MsgCustomData.DATA, attachment);
					map.put(Utils.Constant.MsgCustomData.TYPE, attachmentType);
					Log.i("send Msg to " + targetIds, "message: " + msg);
					Log.i("send Msg to " + targetIds, "data: " + map);
					if (roomType == Utils.Constant.RoomType.CLIENT) {
						mTA.anIM.sendMessage(targetIds, msg,map);
					} else if (roomType == Utils.Constant.RoomType.TOPIC) {
						mTA.anIM.sendMessageToTopic(targetId, msg, map);
					}
				}
			} catch (ArrownockException e) {
				e.printStackTrace();
				Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
			}
		}
	}
	
	AnIMCallbackAdapter messagecallback = new AnIMCallbackAdapter() {
		@Override
		public void receivedTopicMessage(AnIMTopicMessageCallbackData data) {
			final String from = data.getFrom();
			final String fromTopic = data.getTopic();
			final String message = data.getMessage();
			final Map<String, String> customData = data.getCustomData();
			final String customData_data = customData==null?null:customData.get(Utils.Constant.MsgCustomData.DATA);
			final String customData_type = customData==null?null:customData.get(Utils.Constant.MsgCustomData.TYPE);
			
			Log.d("Chat,MessageCallback", "received message: " + message);
			Log.d("Chat,MessageCallback",
					"received link: " + customData_data);
			Log.d("Chat,MessageCallback",
					"received type: " + customData_type);
			Log.d("Chat,MessageCallback",
					fromTopic+","+targetId+","+targetId.equals(fromTopic)+","+(roomType == Utils.Constant.RoomType.TOPIC));
			
			if (roomType == Utils.Constant.RoomType.TOPIC && targetId.equals(fromTopic)) {
				runOnUiThread(new Runnable() {
					public void run() {
						HashMap<String, Object> item = new HashMap<String, Object>();
						item.put(Utils.Constant.HistoryItem.MSG, message);
						item.put(Utils.Constant.HistoryItem.FROM, from);
						if (customData_data!=null)
							item.put(Utils.Constant.HistoryItem.DATA, customData_data);
						if (customData_type!=null)
							item.put(Utils.Constant.HistoryItem.TYPE, customData_type);
						history.add(item);
						updateHistoryList(true);
						
					}
				});
			} else {
				runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(getBaseContext(),"["+mTA.mTopicsMap.get(fromTopic)+"] "+mTA.mUsersMap.get(from)+" : "+ message, Toast.LENGTH_LONG).show();
					}
				});
			}

		}

		@Override
		public void receivedMessage(AnIMMessageCallbackData data) {
			final String from = data.getFrom();
			final String message = data.getMessage();
			final Map<String, String> customData = data.getCustomData();
			final String customData_data = customData==null?null:customData.get(Utils.Constant.MsgCustomData.DATA);
			final String customData_type = customData==null?null:customData.get(Utils.Constant.MsgCustomData.TYPE);
			Log.d("Chat,MessageCallback", "received message: " + message);
			Log.d("Chat,MessageCallback",
					"received link: " + customData_data);
			Log.d("Chat,MessageCallback",
					"received type: " + customData_type);
			if (roomType == Utils.Constant.RoomType.CLIENT && from.equals(targetId)) {
				runOnUiThread(new Runnable() {
					public void run() {
						// Toast.makeText(ChatActivity.this, from + " : " +
						// message,Toast.LENGTH_LONG).show();
						HashMap<String, Object> item = new HashMap<String, Object>();
						item.put("msg", message);
						item.put("from", from);
						if (customData_data!=null)
							item.put(Utils.Constant.HistoryItem.DATA, customData_data);
						if (customData_type!=null)
							item.put(Utils.Constant.HistoryItem.TYPE, customData_type);

						history.add(item);
						updateHistoryList(true);
					}
				});
			} else {
				runOnUiThread(new Runnable() {
					public void run() {
						if (customData_type!=null){
							Toast.makeText(getBaseContext(),mTA.mUsersMap.get(from)+" : [" + customData.get("type") + "]", Toast.LENGTH_LONG).show();
						}else{
							Toast.makeText(getBaseContext(),mTA.mUsersMap.get(from)+" : " + message, Toast.LENGTH_LONG).show();
						}
					}
				});
			}
		}
		
		@Override
		public void receivedBinary(final AnIMBinaryCallbackData data){
			if (data.getFrom().equals(targetId)) {
				runOnUiThread(new Runnable() {
					public void run() {
						HashMap<String, Object> item = new HashMap<String, Object>();
						item.put(Utils.Constant.HistoryItem.FROM, data.getFrom());
						item.put(Utils.Constant.HistoryItem.TYPE, data.getFileType());
						item.put(Utils.Constant.HistoryItem.CONTENT, data.getContent());
						history.add(item);
						
						updateHistoryList(true);
					}
				});
			}else{
				runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(getBaseContext(),mTA.mUsersMap.get(data.getFrom())+" : ["+data.getFileType()+"]", Toast.LENGTH_LONG).show();
					}
				});
			}
		}
		
		@Override
		public void receivedTopicBinary(final AnIMTopicBinaryCallbackData data){
			if (data.getTopic().equals(targetId)) {
				runOnUiThread(new Runnable() {
					public void run() {
						HashMap<String, Object> item = new HashMap<String, Object>();
						item.put(Utils.Constant.HistoryItem.FROM, data.getFrom());
						item.put(Utils.Constant.HistoryItem.TYPE, data.getFileType());
						item.put(Utils.Constant.HistoryItem.CONTENT, data.getContent());
						history.add(item);
						
						updateHistoryList(true);
					}
				});
			}else{
				runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(getBaseContext(),"["+mTA.mTopicsMap.get(data.getTopic())+"] "+mTA.mUsersMap.get(data.getFrom())+" : ["+data.getFileType()+"]", Toast.LENGTH_LONG).show();
					}
				});
			}
		}
		
		@Override
		public void messageSent(final AnIMMessageSentCallbackData data){
			if(data.isError()){
				runOnUiThread(new Runnable(){
					public void run() {
						Toast.makeText(getBaseContext(), data.getException().getMessage(), Toast.LENGTH_LONG).show();
					}
				});
			}else{
				runOnUiThread(new Runnable() {
					public void run() {
						HashMap<String, Object> item = new HashMap<String, Object>();
						item.put(Utils.Constant.HistoryItem.MSG, mMsg);
						item.put(Utils.Constant.HistoryItem.FROM, mTA.mClientId);
						if (!mAttachmentType.equals(""))
							item.put(Utils.Constant.HistoryItem.TYPE, mAttachmentType);
						if (!mAttachment.equals(""))
							item.put(Utils.Constant.HistoryItem.DATA, mAttachment);
						if(mAttachmentData != null){
							item.put(Utils.Constant.HistoryItem.CONTENT, mAttachmentData);
						}
						history.add(item);
			
						InputMethodManager imm = (InputMethodManager) ChatActivity.this
								.getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(textEdit.getWindowToken(), 0);
						textEdit.setText("");
						mMsg = "";
						mAttachment = "";
						mAttachmentType = "";
						mAttachmentData = null;
						
						updateHistoryList(true);
					}
				});
			}
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
				if (data.getException().getErrorCode() == ArrownockException.IM_FORCE_CLOSED
						|| data.getException().getErrorCode() == ArrownockException.IM_FAILED_DISCONNECT) {
					runOnUiThread(new Runnable(){
						public void run() {
							Toast.makeText(getBaseContext(), "Log in by another device", Toast.LENGTH_LONG).show();
						}
					});
					try {
						mTA.anIM.disconnect();
					} catch (ArrownockException e1) {
						e1.printStackTrace();
					}
					Intent it = new Intent();
					it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					it.setClass(getBaseContext(), MainActivity.class);
					startActivity(it);
					finish();
				}
			}
		}
		
		@Override
		public void removeClientsFromTopic(final AnIMRemoveClientsCallbackData data){
			if(data.getException()!=null){
				Log.e("removeClientsFromTopic",data.getException().getMessage());
				runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(getBaseContext(),data.getException().getMessage(), Toast.LENGTH_LONG).show();
					}
				});
			}else{
				finish();
			}
		}
		@Override
		public void getTopicInfo(final AnIMGetTopicInfoCallbackData data){
			if(data.getException() != null){
				runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(getBaseContext(),data.getException().getMessage(), Toast.LENGTH_LONG).show();
					}
				});
			}else{
				mTopicInfo = data;
				runOnUiThread(new Runnable() {
					public void run() {
						textTargetId.setText(mTopicInfo.getTopicName());
					}
				});
			}
		}
	};
	
	private Bitmap getBmpFromAsset(String path){
		AssetManager assetManager = getBaseContext().getAssets();
		InputStream istr;
		Bitmap bitmap = null;
		try {
			istr = assetManager.open(path);
			bitmap = BitmapFactory.decodeStream(istr);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bitmap;
	}
	
	private byte[] convertBmpToByte(Bitmap mBmp) {
		int quality =50;
		byte[] btArray = null;
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		mBmp.compress(Bitmap.CompressFormat.JPEG, quality, stream);
		btArray = stream.toByteArray();
		return btArray;
	}
	
	
	private void loadBitmap(final byte[] data, final ImageView imgView) {
		InputStream is = new ByteArrayInputStream(data);
		Bitmap bmp = BitmapFactory.decodeStream(is);
		imgView.setImageBitmap(bmp);
	}
	
	private void setViewContent(){
		textEdit = (EditText) findViewById(R.id.editMsg);
		btnSend = (Button) findViewById(R.id.btnSend);
		textTargetId = (TextView) findViewById(R.id.textTargetId);
		btnMore = (Button) findViewById(R.id.btnMore);
		chatHistory = (ListView) findViewById(R.id.chatHistory);
		btnDetail = (Button) findViewById(R.id.btn_detail);
		
		btnDetail.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mTopicInfo!=null){
					showTopicDetailDialog(mTopicInfo);
				}
			}
		});
		
		btnSend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sendMsg(textEdit.getText().toString().trim(),"","",null);
			}
		});
		btnMore.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showAttachmentDialog();
			}
		});		
		
		Log.i("userId2Name.size",mTA.mUsersMap.size()+"");
		chatHistory = (ListView) findViewById(R.id.chatHistory);
		history = new ArrayList<HashMap<String, Object>>();
		historyAdapter = new HistoryAdapter(ChatActivity.this, history,mTA.mClientId,mTA.mUsersMap);
		chatHistory.setAdapter(historyAdapter);
		chatHistory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					public void onItemClick(AdapterView parent, View view,int position, long id) {
						String data = "";
						String type = "";
						byte[] content = null ;
						if (history.get(position).containsKey(Utils.Constant.HistoryItem.DATA)
								&& history.get(position).get(Utils.Constant.HistoryItem.DATA) != null) {
							data = (String) history.get(position).get(Utils.Constant.HistoryItem.DATA);
						}
						if (history.get(position).containsKey(Utils.Constant.HistoryItem.CONTENT)
								&& history.get(position).get(Utils.Constant.HistoryItem.CONTENT) != null) {
							content = (byte[]) history.get(position).get(Utils.Constant.HistoryItem.CONTENT);
						}
						if (history.get(position).containsKey(Utils.Constant.HistoryItem.TYPE)
								&& history.get(position).get(Utils.Constant.HistoryItem.TYPE) != null) {
							type = (String) history.get(position).get(Utils.Constant.HistoryItem.TYPE);

							if (type.equals(Utils.Constant.AttachmentType.LOCATION)) {
								Intent intent = new Intent(Intent.ACTION_VIEW);
							    intent.setData(Uri.parse("geo:0,0?q="+data+"()"));
							    if (intent.resolveActivity(getPackageManager()) != null) {
							        startActivity(intent);
							    }
							}else if(type.equals(Utils.Constant.AttachmentType.LINK)){
								Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(data));
								startActivity(browserIntent);
							}else {
								showContentDialog(type,data,content);
							} 
						}
					}
				});
	}
	
	private void showAttachmentDialog(){
		int id =1;
		AlertDialog.Builder mLoginDialog = new AlertDialog.Builder(this);
		final LinearLayout mll = new LinearLayout(this);
		mll.setLayoutParams(new LayoutParams(-1,-1));
		mll.setOrientation(LinearLayout.VERTICAL);
		final LinearLayout mll2 = new LinearLayout(this);
		mll2.setLayoutParams(new LayoutParams(-1,-1));
		mll2.setOrientation(LinearLayout.HORIZONTAL);
		mll2.setPadding(30, 30, 30, 30);
		final ImageView btnImg= new ImageView(this);
		final ImageView btnLink= new ImageView(this);
		final ImageView btnVideo= new ImageView(this);
		final ImageView btnLocation= new ImageView(this);
		btnImg.setId(id++);
		btnLink.setId(id++);
		btnVideo.setId(id++);
		btnLocation.setId(id++);
		LinearLayout.LayoutParams lp= new LinearLayout.LayoutParams(-1,-2);
		lp.weight = 1;
		btnImg.setLayoutParams(lp);
		btnLink.setLayoutParams(lp);
		btnVideo.setLayoutParams(lp);
		btnLocation.setLayoutParams(lp);
		btnImg.setImageResource(R.drawable.tabicon_image_up);
		btnLink.setImageResource(R.drawable.tabicon_link_up);
		btnVideo.setImageResource(R.drawable.tabicon_video_up);
		btnLocation.setImageResource(R.drawable.tabicon_location_up);
		mll2.addView(btnImg);
		mll2.addView(btnLink);
		mll2.addView(btnVideo);
		mll2.addView(btnLocation);
		mll.addView(mll2);
		
		final EditText et = new EditText(this);
		LinearLayout.LayoutParams mLp = new LinearLayout.LayoutParams(-1,-1);
		mLp.leftMargin = 30 ;
		mLp.rightMargin = 30 ;
		mLp.bottomMargin = 30 ;
		et.setLayoutParams(mLp);
		mll.addView(et);

		final ImageView iv= new ImageView(this);
		mll.addView(iv);
		iv.setVisibility(View.GONE);
		
		OnClickListener icon = new OnClickListener() {
			@Override
			public void onClick(View v) {
				btnImg.setImageResource(R.drawable.tabicon_image_up);
				btnLink.setImageResource(R.drawable.tabicon_link_up);
				btnVideo.setImageResource(R.drawable.tabicon_video_up);
				btnLocation.setImageResource(R.drawable.tabicon_location_up);
				iv.setVisibility(View.GONE);
				if(v.getId() == btnImg.getId()){
					btnImg.setImageResource(R.drawable.tabicon_image_select);
					iv.setVisibility(View.VISIBLE);
					et.setText(R.string.default_img);
					mAttachmentType = Utils.Constant.AttachmentType.IMAGE;
					Bitmap bmp = getBmpFromAsset(getString(R.string.default_img));
					iv.setImageBitmap(bmp);
					mAttachmentData = convertBmpToByte(bmp);
				}else if(v.getId() == btnLink.getId()){
					btnLink.setImageResource(R.drawable.tabicon_link_select);
					et.setText(R.string.default_link);
					mAttachmentType = Utils.Constant.AttachmentType.LINK;
				}else if(v.getId() == btnVideo.getId()){
					btnVideo.setImageResource(R.drawable.tabicon_video_select);
					et.setText(R.string.default_video);
					mAttachmentType = Utils.Constant.AttachmentType.VIDEO;
				}else if(v.getId() == btnLocation.getId()){
					btnLocation.setImageResource(R.drawable.tabicon_location);
					et.setText(R.string.default_latlng);
					mAttachmentType = Utils.Constant.AttachmentType.LOCATION;
				}
			}
		};
		
		btnImg.setOnClickListener(icon);
		btnLink.setOnClickListener(icon);
		btnVideo.setOnClickListener(icon);
		btnLocation.setOnClickListener(icon);

		mLoginDialog.setView(mll);
		mLoginDialog.setNegativeButton("Cancel",  new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mMsg = "";
				mAttachment = "";
				mAttachmentType = "";
				mAttachmentData = null;
			}
		});
		mLoginDialog.setPositiveButton("Send",  new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mAttachment = et.getText().toString();
				sendMsg(" ",mAttachment,mAttachmentType,mAttachmentData);
			}
		});
		mLoginDialog.show();
	}
	
	
	private void showContentDialog(String type , String link, byte[] data){
		AlertDialog.Builder mLoginDialog = new AlertDialog.Builder(this);
		final RelativeLayout mRl = new RelativeLayout(this);
		mRl.setLayoutParams(new LayoutParams(-1,-1));
		final VideoView mVv= new VideoView(this);
		final ImageView mIv= new ImageView(this);
		mVv.setZOrderOnTop(true);
		if(type.equals(Utils.Constant.AttachmentType.IMAGE)){
			mIv.setLayoutParams(new RelativeLayout.LayoutParams(-1,-1));
			mRl.addView(mIv);
			loadBitmap(data, mIv);
		}else{
			mVv.setLayoutParams(new RelativeLayout.LayoutParams(-1,-1));
			mVv.setVideoURI(Uri.parse(link));
			mVv.requestFocus();
			mVv.start();
			mRl.addView(mVv);
		}
		mLoginDialog.setView(mRl);
		mLoginDialog.setNegativeButton("Close",  new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mVv.stopPlayback();
			}
		});
		mLoginDialog.show();
	}
	
	private void showTopicDetailDialog(AnIMGetTopicInfoCallbackData topicInfo){
		Set<String> parties = topicInfo.getParties();
		AlertDialog.Builder mLoginDialog = new AlertDialog.Builder(this);
		mLoginDialog.setTitle("User list");
		final String[] partiesArray = new String[parties.size()];
		int i =0;
		for(String party : parties){
			if(mTA.mUsersMap.get(party)==null){
				if(party .equals(mTA.mClientId)){
					partiesArray[i++] = mTA.mUsername;
				}
			}else{
				partiesArray[i++] = mTA.mUsersMap.get(party);
			}
		}
		mLoginDialog.setItems(partiesArray, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getBaseContext(), partiesArray[which],Toast.LENGTH_LONG).show();
            }
        });

		mLoginDialog.setNegativeButton("Close",  new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		mLoginDialog.show();
	}
}
