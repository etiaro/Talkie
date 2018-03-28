package com.etiaro.talkie;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class ConversationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
        setTitle(MemoryManger.conversations.get(getIntent().getStringExtra("thread_key")).name);
    }
}
