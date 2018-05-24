package com.etiaro.talkie;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.etiaro.facebook.Account;
import com.etiaro.facebook.Message;
import com.etiaro.facebook.functions.GetConversationList;
import com.etiaro.facebook.Conversation;
import com.etiaro.facebook.functions.GetUserInfo;
import com.etiaro.facebook.functions.Listen;
import com.etiaro.facebook.functions.SendMessage;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    ConversationListAdapter arr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(getIntent().hasExtra(getString(R.string.intent_loggedIn))){ //logged-in
            MemoryManger.saveAccIDs(this);
            MemoryManger.saveAccount(this, MemoryManger.getInstance().accounts.get(getIntent().getStringExtra(getString(R.string.intent_loggedIn))));
            Log.d("talkie", "Fully logged in and saved");
        }
        Notifications.initGroups(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        MemoryManger.loadSharedPrefs(this);
        if(MemoryManger.getInstance().accounts.size() <= 0) {
            runLoginActv();
        }else{
            showList();
            updateConversationList();
            MainService.start(MainActivity.this, new Listen.ListenCallbacks() {
                @Override
                public void newMessage(Message msg) {
                    showList();
                }

                @Override
                public void typing(String threadid, String userid, boolean isTyping) { }

                @Override
                public void presenceUpdate(Map<String, Long> users){ showList(); }

                @Override
                public void readReceipt(JSONObject delta){}

                @Override
                public void deliveryReceipt(JSONObject delta){}

            });
        }
    }
    private void runLoginActv(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void showList(){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ListView view = findViewById(R.id.msgList);
                if(arr == null) {
                    arr = new ConversationListAdapter(MainActivity.this, R.layout.conversation_row,
                            new ArrayList<>(MemoryManger.conversations.values()));
                    view.setAdapter(arr);
                }else {
                    arr.clear();
                    arr.addAll(new ArrayList<>(MemoryManger.conversations.values()));
                    arr.notifyDataSetChanged();
                }
            }
        });

    }
    private void updateConversationList(){
        for(final String id : MemoryManger.getInstance().accounts.keySet()) {
            GetConversationList ui = new GetConversationList(MemoryManger.getInstance().accounts.get(id), 20, 0, new String[]{"INBOX"});

            ui.execute(new GetConversationList.ConversationListCallback() {
                @Override
                public void success(ArrayList<Conversation> list, ArrayList<GetUserInfo.UserInfo> users) {
                    MemoryManger.updateConversations(list.toArray(new Conversation[list.size()]));
                    MemoryManger.saveConversations(MainActivity.this);
                    MemoryManger.updateUsers(MainActivity.this, users.toArray(new GetUserInfo.UserInfo[users.size()]));
                    showList();
                }

                @Override
                public void fail() {

                }

                @Override
                public void cancelled() {

                }
            });
        }
    }
}
