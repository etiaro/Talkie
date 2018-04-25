package com.etiaro.talkie;

import android.app.NotificationManager;
import android.media.MediaMetadata;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.etiaro.facebook.Account;
import com.etiaro.facebook.Conversation;
import com.etiaro.facebook.Message;
import com.etiaro.facebook.functions.GetConversationHistory;
import com.etiaro.facebook.functions.SendMessage;

import java.util.ArrayList;
import java.util.List;

public class ConversationActivity extends AppCompatActivity {
    String conversationID;
    String accountID;  //TODO intent
    public static String activeConvID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        conversationID = getIntent().getStringExtra("thread_key");
        accountID = getIntent().getStringExtra("userID");
        if(conversationID == null){
            finish();
            return;
        }
        setTitle(MemoryManger.conversations.get(conversationID).name);
        FloatingActionButton btn = findViewById(R.id.floatingActionButton);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessage sm = new SendMessage((Account)MemoryManger.accounts.values().toArray()[0], MemoryManger.conversations.get(conversationID), "(y)", 0);
                sm.execute(new SendMessage.SendMessageCallback() {
                    @Override
                    public void success() {
                        Log.d("suc", "sent?");
                    }

                    @Override
                    public void fail() {
                        Log.d("fai", "not sent?");
                    }
                });
            }
        });
    }

    @Override
    protected void onStart(){
        super.onStart();
        if(MemoryManger.conversations.containsKey(conversationID))
            showMessages();
        new GetConversationHistory((Account)MemoryManger.accounts.get(accountID), conversationID, 20, 0f)
            .execute(new GetConversationHistory.ConversationHistoryCallback() {
                @Override
                public void success(Conversation conversation) {
                    MemoryManger.updateConversations(conversation);
                    MemoryManger.saveConversations(ConversationActivity.this);
                    showMessages();
                }

                @Override
                public void fail() {

                }

                @Override
                public void cancelled() {

                }
            });
    }

    @Override
    public void onResume() {
        super.onResume();
        if(Notifications.notifications.containsKey(conversationID))
            Notifications.notifications.get(conversationID).remove(this);
        activeConvID = conversationID;
    }

    @Override
    public void onPause(){
        super.onPause();
        activeConvID = "";
    }

    private void showMessages(){
        ArrayList<String> items = new ArrayList<>();
        int length = MemoryManger.conversations.get(conversationID).messages.size();
        for(int i = length-1; i >= 0; i--)
            items.add(((Message)MemoryManger.conversations.get(conversationID).messages.values().toArray()[i]).text);
        final ArrayAdapter<String> arr = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((ListView)findViewById(R.id.messages)).setAdapter(arr);
                ((ListView)findViewById(R.id.messages)).setSelection(arr.getCount()-1);
            }
        });
    }
}
