package com.etiaro.talkie;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.etiaro.facebook.Account;
import com.etiaro.facebook.functions.GetConversationList;
import com.etiaro.facebook.Conversation;
import com.etiaro.facebook.functions.GetUserInfo;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

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
        //TODO my own listAdapter to show conversations, clickable and start activity(ConversationActivity)
        final ConversationListAdapter arr = new ConversationListAdapter(MainActivity.this, R.layout.conversation_row,
                new ArrayList<Conversation>(MemoryManger.conversations.values()));
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((ListView)findViewById(R.id.msgList)).setAdapter(arr);
            }
        });
    }
    private void updateConversationList(){
        for(final String id : MemoryManger.getInstance().accounts.keySet()) {
            GetConversationList ui = new GetConversationList(MemoryManger.getInstance().accounts.get(id), 20, 0, new String[]{"INBOX"});

            ui.execute(new GetConversationList.ConversationListCallback() {
                @Override
                public void success(ArrayList<Conversation> list) {
                    MemoryManger.updateConversations(list);

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
