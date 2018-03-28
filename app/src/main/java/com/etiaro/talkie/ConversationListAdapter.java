package com.etiaro.talkie;


import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.etiaro.facebook.Conversation;

import java.io.File;
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
            TextView tt2 = v.findViewById(R.id.conversationName);
            TextView tt3 = v.findViewById(R.id.conversationMessage);

            if (img != null) {
                if(imgs.containsKey(conv.thread_key))   //file is loaded, loading from variable
                    img.setImageDrawable(imgs.get(conv.thread_key));
                else if(new File(MemoryManger.getImagePath(conv.thread_key)).exists()) { //file exists, loading from file
                    imgs.put(conv.thread_key, Drawable.createFromPath(MemoryManger.getImagePath(conv.thread_key)));
                    img.setImageDrawable(imgs.get(conv.thread_key));
                }else //downloading image
                    InternetService.loadImage(conv.image, img, conv.thread_key, context);
            }

            if (tt2 != null) {
                tt2.setText(conv.name);
            }

            if (tt3 != null) {
                tt3.setText(conv.messages.get(conv.messages.size()-1).text);
            }
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