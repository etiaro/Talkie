package com.etiaro.talkie;

import android.media.MediaMetadata;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.etiaro.facebook.Account;
import com.etiaro.facebook.Conversation;
import com.etiaro.facebook.Message;
import com.etiaro.facebook.functions.GetConversationHistory;

import java.util.ArrayList;
import java.util.List;

public class ConversationActivity extends AppCompatActivity {
    String conversationID;
    String userID;  //TODO intent

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        conversationID = getIntent().getStringExtra("thread_key");
        if(conversationID == null){
            finish();
        }
        setTitle(MemoryManger.conversations.get(conversationID).name);
    }

    @Override
    protected void onStart(){
        super.onStart();
        if(MemoryManger.conversations.containsKey(conversationID))
            showMessages();
        new GetConversationHistory((Account)MemoryManger.accounts.values().toArray()[0], conversationID, 20, 0f)
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

    private void showMessages(){
        ArrayList<String> items = new ArrayList<>();
        for(Message m : MemoryManger.conversations.get(conversationID).messages.values())
            items.add(m.text);
        final ArrayAdapter<String> arr = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((ListView)findViewById(R.id.messages)).setAdapter(arr);
            }
        });
    }
}
