package com.etiaro.talkie;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.etiaro.facebook.Message;

/**
 * Created by jakub on 04.04.18.
 */

public class Notifications {
    static void initGroups(Context c){
        NotificationManager notificationManager =
                (NotificationManager) c.getSystemService(c.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= 27) {
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel notificationChannel = new NotificationChannel("Talkie", "Talkie", importance);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    static void newMessage(Context c, Message msg){
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(c, "Talkie")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Message")
                .setContentText(msg.text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(c);
        notificationManager.notify(100, mBuilder.build());
    }
    static  void typing(Context c, String threadid, String userid){
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(c,  "Talkie")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(threadid)
                .setContentText(userid)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(c);
        notificationManager.notify(101, mBuilder.build());
    }
}
