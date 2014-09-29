package com.emot.adapters;

import java.util.List;  
import android.app.Activity;  
import android.content.Context;  
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;  
import android.view.View;  
import android.view.ViewGroup;  

import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;  
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;  

import com.emot.emotobjects.ChatMessage;
import com.emot.screens.R; 


public class ChatListArrayAdapter extends BaseAdapter {  
     private Activity mContext;  
     private List<ChatMessage> mList;
    private LinearLayout mMessageContainer;
     //private RelativeLayout mMessageContainer;
     private ChatMessage mChatMessage;
     private LayoutInflater mLayoutInflater = null;  
     public ChatListArrayAdapter(Activity context, List<ChatMessage> list) {  
          mContext = context;  
          mList = list;  
          mLayoutInflater = (LayoutInflater) mContext  
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
     }  
     @Override  
     public int getCount() {  
          return mList.size();  
     }  
     @Override  
     public Object getItem(int pos) {  
          return mList.get(pos);  
     }  
     @Override  
     public long getItemId(int position) {  
          return position;  
     }  
     @Override  
     public View getView(int position, View convertView, ViewGroup parent) {  
          View v = convertView;  
          CompleteListViewHolder viewHolder;  
          Log.i("ChatListArrayAdapter", "convertView " +convertView);
          //mMessageContainer.setGravity(Gravity.RIGHT);
          mChatMessage = mList.get(position);
          
        //  if (convertView != null) {  
              LayoutInflater li = (LayoutInflater) mContext  
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
              if(!mChatMessage.isRight() ){
              v = li.inflate(R.layout.chat_row_left, null); 
              viewHolder = new CompleteListViewHolder(v, true);  
              v.setTag(viewHolder); 
              viewHolder.mChatTextRight.setText(mChatMessage.getmMessage());
              viewHolder.mDateTimeRight.setText(mChatMessage.getmTime());
              }else{
            	  v = li.inflate(R.layout.chat_row, null); 
            	  viewHolder = new CompleteListViewHolder(v, false);  
                  v.setTag(viewHolder); 
                  viewHolder.mChatText.setText(mChatMessage.getmMessage());
                  viewHolder.mDateTime.setText(mChatMessage.getmTime());
              }
         
              
              
      //   } //else {  
            //  viewHolder = (CompleteListViewHolder) v.getTag();  
        // }
        //  if(!mChatMessage.isRight() ){
        	 Log.i("ChatListArrayAdapter", mChatMessage.getmTime() +"Left Aigned");
   
       //   viewHolder.mChatTextRight.setText(mChatMessage.getmMessage());
      //    viewHolder.mDateTimeRight.setText(mChatMessage.getmTime());
         // viewHolder.mDateTime.setGravity(Gravity.RIGHT);
        //  }else{
        	  Log.i("ChatListArrayAdapter", mChatMessage.getmMessage() + "Right Aigned");
        	  
        	  
        	 
        	 //mMessageContainer.setGravity(Gravity.RIGHT); 
        	// viewHolder.mChatTextRight.setVisibility(View.VISIBLE);
        	//  viewHolder.mChatText.setVisibility(View.GONE);
        //	  android.widget.RelativeLayout.LayoutParams params =(RelativeLayout.LayoutParams)viewHolder.mChatText.getLayoutParams();
         			// params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
         			// params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
         			//params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        	  
        	 // viewHolder.mDateTime.setGravity(Gravity.RIGHT);
        //  }
          
          
          
          
          
          return v;  
     }  
}  
class CompleteListViewHolder {  
     public TextView mChatText; 
     public TextView mChatTextRight; 
     public TextView mDateTime;
     public TextView mDateTimeRight;
     public CompleteListViewHolder(View base, boolean left) {  
    	 
    		 mDateTimeRight = (TextView)base.findViewById(R.id.chatDateRight);
    	 mChatTextRight = (TextView) base.findViewById(R.id.chatContentRight);
    	
    	 //mChatTextRight = (TextView) base.findViewById(R.id.chatContentRight);  
    	 mChatText = (TextView) base.findViewById(R.id.chatContent); 
    	 mDateTime = (TextView)base.findViewById(R.id.chatDate);
    	 }
    	 
      
}  