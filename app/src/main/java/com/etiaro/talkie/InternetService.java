package com.etiaro.talkie;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.net.URL;

public class InternetService{
    public static void loadImage(final String _url, final ConversationListAdapter cla, final String id, final Context context){
        new Thread(new Runnable() {
            @Override
            public void run() {
                URL url;
                BufferedOutputStream out;
                InputStream in;
                BufferedInputStream buf;
                try {
                    url = new URL(_url);
                    in = url.openStream();
                    // Read the inputstream
                    buf = new BufferedInputStream(in);

                    // Convert the BufferedInputStream to a Bitmap
                    final Bitmap bMap = BitmapFactory.decodeStream(buf);
                    if (in != null) {
                        in.close();
                    }
                    if (buf != null) {
                        buf.close();
                    }
                    MemoryManger.saveImage(context, bMap, id);
                    cla.imgs.put(id, new BitmapDrawable(context.getResources(), bMap));
                    cla.notifyDataSetChanged();
                    /*MemoryManger.saveImage(context, bMap, id, new MemoryManger.Callback() {
                        @Override
                        public void call() {
                            Handler handler = new Handler(context.getMainLooper());
                            handler.post(new Runnable() {
                                public void run() {
                                    imageView.setImageURI(Uri.parse(MemoryManger.getImagePath(id)));
                                }
                            });
                        }
                    });*/

                } catch (Exception e) {
                    Log.e("Error reading file", e.toString());
                }
            }
        }).start();
    }
}
