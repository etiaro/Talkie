package com.etiaro.talkie;


import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.etiaro.facebook.Message;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static android.view.View.GONE;

public class MessageListAdapter extends ArrayAdapter<Message> {
    Context context;
    String conversationID;//init in contructor

    public MessageListAdapter(Context context, int textViewResourceId, String conv) {
        super(context, textViewResourceId, new ArrayList<Message>(MemoryManger.conversations.get(conv).messages.values()));
        this.context = context;
        conversationID = conv;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Message msg = getItem(position);
        View v;
        LayoutInflater vi;
        vi = LayoutInflater.from(getContext());
        if(msg.senderID.equals(MemoryManger.conversations.get(conversationID).accountID))
            v = vi.inflate(R.layout.message_me_row, null);
        else
            v = vi.inflate(R.layout.message_other_row, null);

        if (msg != null) {


            final ImageView imgView = v.findViewById(R.id.message_image);
            TextView timeView = v.findViewById(R.id.message_time);
            TextView msgView = v.findViewById(R.id.message_text);

            if(!msg.senderID.equals(MemoryManger.conversations.get(conversationID).accountID)) {
                ImageView onlinecricle = v.findViewById(R.id.onlinecircle);
                if (MemoryManger.getUserStatus(msg.senderID) != null && MemoryManger.getUserStatus(msg.senderID) == 0L)
                    onlinecricle.setVisibility(View.VISIBLE);
                else
                    onlinecricle.setVisibility(View.GONE);
            }else{
                TextView receipt = v.findViewById(R.id.receiptText);
                if(msg.sent){
                    receipt.setVisibility(View.VISIBLE);
                    receipt.setText("Sent");
                    if(getNextSentByMe(position) != null && getItem(getNextSentByMe(position)).sent){
                        receipt.setVisibility(GONE);
                    }
                }
                if(msg.delivered){
                    receipt.setVisibility(View.VISIBLE);
                    receipt.setText("Delivered");
                    if(getNextSentByMe(position) != null && getItem(getNextSentByMe(position)).delivered){
                        receipt.setVisibility(GONE);
                    }
                }
                if(!msg.unread){
                    receipt.setVisibility(View.VISIBLE);
                    receipt.setText("Read");
                    if(getNextSentByMe(position) != null && !getItem(getNextSentByMe(position)).unread){
                        receipt.setVisibility(GONE);
                    }
                }
            }

            if (msgView != null)
                msgView.setText(msg.text);

            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)msgView.getLayoutParams();
            if(position <= 0 || !getItem(position-1).senderID.equals(msg.senderID))
                params.topMargin = 30;
            else
                params.topMargin = 0;
            msgView.setLayoutParams(params);

            if(msg.senderID.equals(MemoryManger.conversations.get(conversationID).accountID))
                msgView.getBackground().setColorFilter(Color.parseColor("#"+MemoryManger.conversations.get(conversationID).outgoing_bubble_color), PorterDuff.Mode.MULTIPLY);
            else
                msgView.getBackground().setColorFilter(Color.rgb(210, 210, 210), PorterDuff.Mode.MULTIPLY);


            if (imgView != null) {
                if(position + 1 >= getCount() || !getItem(position+1).senderID.equals(msg.senderID)) {
                    imgView.setImageDrawable(MemoryManger.loadImage(MemoryManger.users.get(msg.senderID).thumbSrc, msg.senderID, context, new MemoryManger.Callback() {
                        @Override
                        public void call() {
                            imgView.setImageDrawable(MemoryManger.loadImage(MemoryManger.users.get(msg.senderID).thumbSrc, msg.senderID, context, this));
                        }
                    }));
                }else {
                    imgView.setImageDrawable(null);
                    if(!msg.senderID.equals(MemoryManger.conversations.get(conversationID).accountID))
                        v.findViewById(R.id.onlinecircle).setVisibility(GONE);
                }
            }

            if (timeView != null)
                if(position == 0 || getItem(position-1).timestamp_precise+600000 < msg.timestamp_precise) {
                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(msg.timestamp_precise);
                    SimpleDateFormat sdf;
                    if(c.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR) && c.get(Calendar.DAY_OF_YEAR) == Calendar.getInstance().get(Calendar.DAY_OF_YEAR))
                        sdf = new SimpleDateFormat("HH:mm");
                    else
                        sdf = new SimpleDateFormat("dd MMM ''YY' at 'HH:mm");
                    timeView.setText(sdf.format(c.getTime()));
                }else {
                    timeView.setVisibility(GONE);
                    timeView.setText("");
                }
        }

        return v;
    }

    Integer getNextSentByMe(int pos){
        for(int i = pos+1; i < getCount(); i++){
            if(getItem(i).senderID.equals(MemoryManger.conversations.get(conversationID).accountID))
                return i;
        }
        return null;
    }
}