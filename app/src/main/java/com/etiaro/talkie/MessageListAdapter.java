package com.etiaro.talkie;


import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
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

            if(msg.senderID.equals(MemoryManger.conversations.get(conversationID).accountID))
                if(MemoryManger.conversations.get(conversationID).outgoing_bubble_color == null || MemoryManger.conversations.get(conversationID).outgoing_bubble_color.equals("null"))
                    v.findViewById(R.id.message_text).getBackground().setColorFilter(Color.BLUE, PorterDuff.Mode.MULTIPLY);
                else
                    v.findViewById(R.id.message_text).getBackground().setColorFilter(Color.parseColor("#"+MemoryManger.conversations.get(conversationID).outgoing_bubble_color), PorterDuff.Mode.MULTIPLY);
            else
                v.findViewById(R.id.message_text).getBackground().setColorFilter(Color.rgb(210, 210, 210), PorterDuff.Mode.MULTIPLY);


            if (imgView != null) {
                if(position + 1 >= getCount() || !getItem(position+1).senderID.equals(msg.senderID)) {
                    imgView.setBackground(MemoryManger.loadImage(MemoryManger.users.get(msg.senderID).thumbSrc, msg.senderID, context, new MemoryManger.Callback() {
                        @Override
                        public void call() {
                            imgView.setBackground(MemoryManger.loadImage(MemoryManger.users.get(msg.senderID).thumbSrc, msg.senderID, context, this));
                        }
                    }));
                }else {
                    imgView.setBackground(null);
                }
            }

            if (timeView != null)
                if(position == 0 || getItem(position-1).timestamp_precise+300000 < msg.timestamp_precise) {
                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(msg.timestamp_precise);
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                    timeView.setText(sdf.format(c.getTime()));
                }else {
                    timeView.setVisibility(GONE);
                    timeView.setText("");
                }
            if (msgView != null)
                msgView.setText(msg.text);
        }

        return v;
    }

}