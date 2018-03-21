package com.etiaro.talkie;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.etiaro.facebook.Account;
import com.etiaro.facebook.functions.GetThreadList;
import com.etiaro.facebook.functions.Thread;

import org.json.JSONArray;
import org.json.JSONException;

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
        for(String id : MemoryManger.getInstance().accounts.keySet()) {
            setTitle(MemoryManger.getInstance().accounts.get(id).getName());
            GetThreadList ui = new GetThreadList(MemoryManger.getInstance().accounts.get(id), 20, System.currentTimeMillis(), new String[]{"INBOX"});

            ui.execute(new GetThreadList.ThreadListCallback() {
                @Override
                public void success(ArrayList<Thread> list) {
                    ArrayAdapter<String> arr = new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_list_item_1);
                    int i = 0;
                    for(Thread t : list){
                        arr.add(t.messages.get(0).senderID + t.messages.get(0).snippet);
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
