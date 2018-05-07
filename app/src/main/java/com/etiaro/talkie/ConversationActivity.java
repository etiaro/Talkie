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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.etiaro.facebook.Account;
import com.etiaro.facebook.Conversation;
import com.etiaro.facebook.Message;
import com.etiaro.facebook.functions.GetConversationHistory;
import com.etiaro.facebook.functions.Listen;
import com.etiaro.facebook.functions.SendMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

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
        ImageButton btn = findViewById(R.id.sendButton);
        final EditText messageBox = findViewById(R.id.message);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = messageBox.getText().toString();
                messageBox.getText().clear();

                SendMessage sm = new SendMessage((Account)MemoryManger.accounts.values().toArray()[0], MemoryManger.conversations.get(conversationID), msg, 0);
                sm.execute(new SendMessage.SendMessageCallback() {
                    @Override
                    public void success() {
                    }

                    @Override
                    public void fail() {
                    }
                });
            }
        });
        //TODO scroll listener & load more
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

        MainService.start(this, new Listen.ListenCallbacks() {
            @Override
            public void newMessage(Message msg) {
                showMessages();
            }

            @Override
            public void typing(String threadid, String userid, boolean isTyping) {
            }
        });
    }

    @Override
    public void onPause(){
        super.onPause();
        activeConvID = "";
    }

    private void showMessages(){
        ArrayList<String> items = new ArrayList<>();
        int length = MemoryManger.conversations.get(conversationID).messages.size();
        for(int i = length-1; i >= 0; i--) {
            Message msg = (Message) MemoryManger.conversations.get(conversationID).messages.values().toArray()[i];
            if(msg.attachments.size() > 0)
                items.add(MemoryManger.conversations.get(conversationID).nicknames.get(msg.senderID) + ": " + msg.text + " --> " + msg.attachments.get(0).previewUrl);
            else
                items.add(MemoryManger.conversations.get(conversationID).nicknames.get(msg.senderID) + ": " + msg.text);
        }
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
