package com.etiaro.talkie;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.etiaro.facebook.Account;
import com.etiaro.facebook.Conversation;
import com.etiaro.facebook.Message;
import com.etiaro.facebook.functions.GetConversationHistory;
import com.etiaro.facebook.functions.Listen;

import java.util.ArrayList;

public class MainService extends IntentService {
    static Listen.ListenCallbacks callbacks;
    boolean isStarted = false;
    public MainService() {
        super("MainService");
    }

    public static void start(Context context, Listen.ListenCallbacks callback) {
        if(callback!= null)
            callbacks = callback;
        Intent intent = new Intent(context, MainService.class);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if(!isStarted){
            for(final Account a : MemoryManger.accounts.values()) {
                Listen.start(this, a, new Listen.ListenCallbacks() {
                    @Override
                    public void newMessage(final Message msg) {
                        Notifications.newMessage(MainService.this, msg);
                        if(MemoryManger.conversations.containsKey(msg.conversation_id)){
                            Conversation tmp = MemoryManger.conversations.get(msg.conversation_id);
                            tmp.updateMessages(msg);
                            tmp.unread_count++;
                            MemoryManger.updateConversations(tmp);

                            MemoryManger.saveConversations(MainService.this);
                            callbacks.newMessage(msg);
                        }else{
                            GetConversationHistory run = new GetConversationHistory(a, msg.conversation_id, 20, 0);
                            run.execute(new GetConversationHistory.ConversationHistoryCallback() {
                                @Override
                                public void success(Conversation conversation) {
                                    MemoryManger.updateConversations(conversation);
                                    callbacks.newMessage(msg);
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

                    @Override
                    public void typing(String threadid, String userid, boolean isTyping) {
                        if(MemoryManger.conversations.containsKey(threadid))
                            if(isTyping) {
                                if (!MemoryManger.conversations.get(threadid).typing.contains(userid))
                                    MemoryManger.conversations.get(threadid).typing.add(userid);
                            }else
                                if (MemoryManger.conversations.get(threadid).typing.contains(userid))
                                    MemoryManger.conversations.get(threadid).typing.remove(userid);
                    }
                });
            }
            isStarted = true;
        }
    }
}
