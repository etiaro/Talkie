package com.etiaro.talkie;


import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
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
    HashMap<String, Drawable> imgs = new HashMap<>();

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
            ImageView img = v.findViewById(R.id.conversationImage);
            TextView name = v.findViewById(R.id.conversationName);
            TextView msg = v.findViewById(R.id.conversationMessage);

            if (img != null) {
                if(imgs.containsKey(conv.thread_key))   //file is loaded, loading from variable
                    img.setImageDrawable(imgs.get(conv.thread_key));
                else if(new File(MemoryManger.getImagePath(conv.thread_key)).exists()) { //file exists, loading from file
                    imgs.put(conv.thread_key, Drawable.createFromPath(MemoryManger.getImagePath(conv.thread_key)));
                    img.setImageDrawable(imgs.get(conv.thread_key));
                }else //downloading image
                    InternetService.loadImage(conv.image, img, conv.thread_key, context);
            }

            if (name != null)
                name.setText(conv.name);

            if (msg != null)
                msg.setText(conv.snippet);
        }

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("click", conv.name);
                Intent intent = new Intent(context, ConversationActivity.class);
                intent.putExtra("thread_key", conv.thread_key);
                context.startActivity(intent);
            }
        });

        return v;
    }

}