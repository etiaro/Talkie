package com.etiaro.talkie;

import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.etiaro.facebook.Account;
import com.etiaro.facebook.functions.GetConversationList;
import com.etiaro.facebook.Conversation;
import com.etiaro.facebook.functions.Listen;

import java.util.ArrayList;

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        MemoryManger.loadSharedPrefs(this, new MemoryManger.Callback() {
            @Override
            public void call() {
                if(MemoryManger.getInstance().accounts.size() <= 0) {
                    runLoginActv();
                }else{
                    showList();
                    updateConversationList();
                    //TODO callbacks of listen
                    Listen.start(MainActivity.this, (Account)MemoryManger.accounts.values().toArray()[0], new Listen.ListenCallbacks() {
                        @Override
                        public void newMessage(String msg) {
                            Log.d("NEWMSG", msg);
                            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(MainActivity.this)
                                    .setSmallIcon(R.drawable.ic_launcher_background)
                                    .setContentTitle("Message")
                                    .setContentText(msg)
                                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(MainActivity.this);
                            notificationManager.notify(100, mBuilder.build());

                        }
                    });
                }
            }
        });
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
                public void success(ArrayList<Conversation> list) {
                    MemoryManger.updateConversations(list.toArray(new Conversation[list.size()]));

                    MemoryManger.saveConversations(MainActivity.this);
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
