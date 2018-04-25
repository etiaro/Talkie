package com.etiaro.talkie;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.etiaro.facebook.Conversation;
import com.etiaro.facebook.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by jakub on 04.04.18.
 */

public class Notifications {
    static class Notification {
        int id;
        NotificationCompat.Builder builder;
        NotificationCompat.InboxStyle style;
        Notification(Context c, String[] lines, int id, String convId){
            style = new NotificationCompat.InboxStyle();
            for(String l : lines)
                style.addLine(l);
            builder = new NotificationCompat.Builder(c, "Talkie")
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setStyle(style);
            this.id = id;
            Intent intent = new Intent(c, ConversationActivity.class);
            intent.putExtra("thread_key", convId);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(c, 0, intent, 0);
            builder.setContentIntent(pendingIntent);
        }
        Notification(final Context c, final Message msg){
            style = new NotificationCompat.InboxStyle()
                    .addLine(msg.text);
            builder = new NotificationCompat.Builder(c, "Talkie")
                    .setLargeIcon(MemoryManger.loadImage(MemoryManger.conversations.get(msg.conversation_id).image,
                            msg.conversation_id, c, new MemoryManger.Callback() {
                        @Override
                        public void call() {
                            builder.setLargeIcon(MemoryManger.loadImage(MemoryManger.conversations.get(msg.conversation_id).image,
                                    msg.conversation_id, c,this).getBitmap());
                        }
                    }).getBitmap())
                    .setStyle(style);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) //TODO notifications not showing on. 4.4
                builder.setSmallIcon(R.drawable.ic_launcher_foreground);
            this.id = notifications.size();
            Intent intent = new Intent(c, ConversationActivity.class);
            intent.putExtra("thread_key", msg.conversation_id);
            PendingIntent pendingIntent = PendingIntent.getActivity(c, 0, intent, 0);
            builder.setContentIntent(pendingIntent);
        }
        void addLine(String line){
            style.addLine(line);
            builder.setStyle(style);
        }
        void remove(Context c){
            NotificationManager notificationManager =
                    (NotificationManager) c.getSystemService(c.NOTIFICATION_SERVICE);
            notificationManager.cancel(id);
            if(notifications.containsKey(id))
               notifications.remove(id);
        }

    }
    static LinkedHashMap<String, Notification> notifications = new LinkedHashMap<>();

    static void initGroups(Context c){
        if(Build.VERSION.SDK_INT >= 27) {
            NotificationManager notificationManager =
                (NotificationManager) c.getSystemService(c.NOTIFICATION_SERVICE);
            if(notificationManager.getNotificationChannel("Talkie") != null)
                return;
            NotificationChannel notificationChannel = new NotificationChannel("Talkie", "Talkie", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{800, 100, 500, 200, 200});
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    static void newMessage(Context c, Message msg){
        if(notifications.containsKey(msg.conversation_id)){
            notifications.get(msg.conversation_id).addLine(msg.text);
        }else {
            notifications.put(msg.conversation_id, new Notification(c, msg));
            if (MemoryManger.conversations.containsKey(msg.conversation_id))
                notifications.get(msg.conversation_id).builder.setContentTitle(MemoryManger.conversations.get(msg.conversation_id).name);
            else
                notifications.get(msg.conversation_id).builder.setContentTitle(msg.conversation_id);
        }
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(c);
        notificationManager.notify(notifications.get(msg.conversation_id).id, notifications.get(msg.conversation_id).builder.build());
    }
}
