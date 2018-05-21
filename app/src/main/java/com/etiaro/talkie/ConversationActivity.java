package com.etiaro.talkie;

import android.app.NotificationManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaMetadata;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.etiaro.facebook.Account;
import com.etiaro.facebook.Conversation;
import com.etiaro.facebook.Message;
import com.etiaro.facebook.functions.GetConversationHistory;
import com.etiaro.facebook.functions.Listen;
import com.etiaro.facebook.functions.SendMessage;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class ConversationActivity extends AppCompatActivity {
    String conversationID;
    String accountID;  //TODO intent
    public static String activeConvID = "";
    MessageListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        setSupportActionBar((Toolbar)findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        conversationID = getIntent().getStringExtra("thread_key");
        accountID = getIntent().getStringExtra("userID");
        if(conversationID == null){
            finish();
            return;
        }

        TextView name = findViewById(R.id.conversationName);
        name.setText(MemoryManger.conversations.get(conversationID).name);

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
        showStatus();
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
                if(msg.conversation_id.equals(conversationID))
                    showMessages();
            }

            @Override
            public void typing(String threadid, String userid, boolean isTyping) {

            }
            @Override
            public void presenceUpdate(Map<String, Long> users){
                if(users.containsKey(conversationID))
                    showStatus();
            }
            @Override
            public void readReceipt(JSONObject delta){
                showStatus();
                showMessages();
            }
            @Override
            public void deliveryReceipt(JSONObject delta){
                showStatus();
                showMessages();
            }
        });

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
    public void onPause(){
        super.onPause();
        activeConvID = "";
    }
    private void showStatus() {
        TextView status = findViewById(R.id.status);
        ImageView circle = findViewById(R.id.onlinecircle);
        if (!MemoryManger.onlineUsers.containsKey(conversationID)) {
            circle.setVisibility(View.GONE);
            status.setText("");
        } else if (MemoryManger.onlineUsers.get(conversationID) == 0l){
            circle.setVisibility(View.VISIBLE);
            status.setText("Online");
        }else {
            circle.setVisibility(View.GONE);
            status.setText("Offline for " + (Calendar.getInstance().getTimeInMillis()-MemoryManger.onlineUsers.get(conversationID))/60000 + "minutes");
        }
    }
    private void showMessages(){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
               getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#"+MemoryManger.conversations.get(conversationID).outgoing_bubble_color)));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Window window = getWindow();
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    window.setStatusBarColor(Color.parseColor("#"+MemoryManger.conversations.get(conversationID).outgoing_bubble_color));
                }

                ListView view = findViewById(R.id.messages);
                if(adapter == null) {
                    adapter = new MessageListAdapter(ConversationActivity.this, R.layout.conversation_row, conversationID);
                    view.setAdapter(adapter);
                }else {
                    adapter.clear();
                    adapter.addAll(new ArrayList<>(MemoryManger.conversations.get(conversationID).messages.values()));
                    adapter.notifyDataSetChanged();
                }
                ((ListView)findViewById(R.id.messages)).setSelection(adapter.getCount()-1);
            }
        });
    }
}
