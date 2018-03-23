package com.etiaro.talkie;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.etiaro.facebook.Message;
import com.etiaro.facebook.functions.GetConversationHistory;
import com.etiaro.facebook.functions.GetConversationList;
import com.etiaro.facebook.Conversation;

import java.util.ArrayList;

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

        MemoryManger.loadSharedPrefs(this, new MemoryManger.Callback() {
            @Override
            public void call() {
                if(MemoryManger.getInstance().accounts.size() <= 0) { //comeback to main Activity
                    runLoginActv();
                }else{
                    showList();
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
        for(final String id : MemoryManger.getInstance().accounts.keySet()) {
            setTitle(MemoryManger.getInstance().accounts.get(id).getName());
            GetConversationList ui = new GetConversationList(MemoryManger.getInstance().accounts.get(id), 20, 0, new String[]{"INBOX"});

            ui.execute(new GetConversationList.ConversationListCallback() {
                @Override
                public void success(ArrayList<Conversation> list) {
                    GetConversationHistory th = new GetConversationHistory(MemoryManger.accounts.get(id), list.get(0).thread_key, 20, 0);
                    th.execute(new GetConversationHistory.ConversationHistoryCallback() {
                        @Override
                        public void success(Conversation conversation) {
                            for(Message m : conversation.messages){
                                Log.d("msg", m.text);
                                //TODO conversationActivity
                            }
                        }

                        @Override
                        public void fail() {

                        }

                        @Override
                        public void cancelled() {

                        }
                    });

                    ArrayAdapter<String> arr = new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_list_item_1);
                    int i = 0;
                    for(Conversation t : list){
                        arr.add(t.messages.get(0).senderID + t.messages.get(0).text);
                        final String id = t.messages.get(0).senderID;
                    }
                    ((ListView)findViewById(R.id.msgList)).setAdapter(arr);
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
