package co.herxun.tinichat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class HistoryAdapter extends BaseAdapter {
	private ArrayList<HashMap<String,Object>> mData;
	private Map<String,String> mClientId2Name;
	private String mClientId;
	private Context ct;
	
	
	public HistoryAdapter(Context context, ArrayList<HashMap<String, Object>> history,String clientId,Map<String,String> clientId2Name) {
		ct = context;
		mData = history;
		mClientId = clientId;
		mClientId2Name = clientId2Name;
	}
	@Override
	public int getCount() {
		return mData.size();
	}
	@Override
	public Object getItem(int position) {
		return mData.get(position);
	}
	@Override
	public long getItemId(int position) {
		return position;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		HashMap<String, Object> msg = mData.get(position) ;
		
		
		int id = 1;
		RelativeLayout itemView = new RelativeLayout(ct);
		itemView.setLayoutParams(new AbsListView.LayoutParams(-1,-2));
		itemView.setPadding(20, 20, 20, 20);
		TextView textUserName = new TextView(ct);
		textUserName.setId(id++);
		textUserName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
		textUserName.setTextColor(0xff666666);
		TextView textContent = new TextView(ct);
		textContent.setId(id++);
		textContent.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
		textContent.setTextColor(0xff000000);
		ImageView imgAttechment = new ImageView(ct);
		imgAttechment.setId(id++);
		
		String type = null;
		int icon = 0;
		if(msg.containsKey(Utils.Constant.HistoryItem.TYPE)&&!msg.get(Utils.Constant.HistoryItem.TYPE).equals(null)){
			type=(String) msg.get(Utils.Constant.HistoryItem.TYPE);
			
			if (type.equals(Utils.Constant.AttachmentType.IMAGE)) {
				Log.e("??? type", type);
				icon = R.drawable.tabicon_image_select;
			} else if (type.equals(Utils.Constant.AttachmentType.VIDEO)) {
				icon = R.drawable.tabicon_video_select;
			} else if (type.equals(Utils.Constant.AttachmentType.LINK)) {
				icon = R.drawable.tabicon_link_select;
			} else if (type.equals(Utils.Constant.AttachmentType.LOCATION)) {
				icon = R.drawable.tabicon_location;
			}
		}

		RelativeLayout.LayoutParams paramsTextUsername = new RelativeLayout.LayoutParams(-2,-2);
		RelativeLayout.LayoutParams paramsTextContent = new RelativeLayout.LayoutParams(-2,-2);
		paramsTextContent.addRule(RelativeLayout.BELOW, textUserName.getId());
		RelativeLayout.LayoutParams paramsImageAttech = new RelativeLayout.LayoutParams(-2,-2);
		paramsImageAttech.addRule(RelativeLayout.BELOW, textUserName.getId());
		if(msg.get("from").equals(mClientId)){
			textUserName.setText("Me");
			paramsTextUsername.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			paramsImageAttech.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			paramsTextContent.addRule(RelativeLayout.LEFT_OF, imgAttechment.getId());
			paramsImageAttech.leftMargin = 20;
		}else{
			textUserName.setText(mClientId2Name.containsKey(msg.get("from")) ? mClientId2Name.get(msg.get("from")):"???");
			paramsTextUsername.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			paramsImageAttech.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			paramsTextContent.addRule(RelativeLayout.RIGHT_OF, imgAttechment.getId());
			paramsImageAttech.rightMargin = 20;
		}
		textUserName.setLayoutParams(paramsTextUsername);
		textContent.setLayoutParams(paramsTextContent);
		imgAttechment.setLayoutParams(paramsImageAttech);
		
		if(type!=null){
			imgAttechment.setImageResource(icon);
		}
		textContent.setText((CharSequence) msg.get("msg"));
		
		itemView.addView(textUserName);
		itemView.addView(textContent);
		itemView.addView(imgAttechment);
		
		return itemView;
	}
	public void updateReceiptsList(HashMap<String, Object> newMsg) {
		mData.add(0, newMsg);
	    this.notifyDataSetChanged();
	}
}
