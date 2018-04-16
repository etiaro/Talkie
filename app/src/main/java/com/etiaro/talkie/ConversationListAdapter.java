package com.etiaro.talkie;


import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.etiaro.facebook.Conversation;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConversationListAdapter extends ArrayAdapter<Conversation> {
    Context context;

    public ConversationListAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        this.context = context;
    }

    public ConversationListAdapter(Context context, int resource, List<Conversation> items) {
        super(context, resource, items);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.conversation_row, null);
        }

        final Conversation conv = getItem(position);

        if (conv != null) {
            final ImageView img = v.findViewById(R.id.conversationImage);
            TextView name = v.findViewById(R.id.conversationName);
            TextView msg = v.findViewById(R.id.conversationMessage);

            if (img != null) {
                img.setBackground(MemoryManger.loadImage(conv.image, conv.thread_key, context, new MemoryManger.Callback() {
                    @Override
                    public void call() {
                        img.setBackground(MemoryManger.loadImage(conv.image, conv.thread_key, context, this));
                    }
                }));
            }

            if (name != null)
                name.setText(conv.name);

            if (msg != null)
                msg.setText(conv.snippet);
        }
        final Conversation c = conv;
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("click", c.name);
                Intent intent = new Intent(context, ConversationActivity.class);
                intent.putExtra("thread_key", c.thread_key);
                context.startActivity(intent);
            }
        });

        return v;
    }

}