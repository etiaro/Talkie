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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

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
                        if(!ConversationActivity.activeConvID.equals(msg.conversation_id))
                            Notifications.newMessage(MainService.this, msg);
                        if(MemoryManger.conversations.containsKey(msg.conversation_id)){
                            Conversation tmp = MemoryManger.conversations.get(msg.conversation_id);
                            msg.sent = true;
                            tmp.updateMessages(msg);
                            if(!ConversationActivity.activeConvID.equals(msg.conversation_id))
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
                        callbacks.typing(threadid, userid, isTyping);
                    }

                    @Override
                    public void presenceUpdate(Map<String, Long> users){
                        MemoryManger.updateOnlineStatus(MainService.this, users);
                        callbacks.presenceUpdate(users);
                    }

                    @Override
                    public void readReceipt(JSONObject delta){
                        //TODO update online-status
                        try {
                            if(delta.has("threadFbId"))   //GROUP
                                    MemoryManger.conversations.get(delta.getString("thread_fbid")).addReadReceipt(delta.getString("actorFbId"), delta.getLong("actionTimestampMs"));
                            else   // one-to-one
                                MemoryManger.conversations.get(delta.getString("actorFbId")).addReadReceipt(delta.getString("actorFbId"), delta.getLong("actionTimestampMs"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        MemoryManger.saveConversations(MainService.this);
                        callbacks.readReceipt(delta);
                    }

                    @Override
                    public void deliveryReceipt(JSONObject delta) {
                        //TODO update online-status by "deliveredWatermarkTimestampMs":"1526910836131"
                        try {
                            if (delta.getJSONObject("threadKey").has("thread_fbid"))   //GROUP
                                for (int i = 0; i < delta.getJSONArray("messageIds").length(); i++)
                                    MemoryManger.conversations.get(delta.getJSONObject("threadKey").getString("thread_fbid")).messages.get(delta.getJSONArray("messageIds").getString(i)).delivered = true;
                            else  // one-to-one
                                for (int i = 0; i < delta.getJSONArray("messageIds").length(); i++)
                                    MemoryManger.conversations.get(delta.getJSONObject("threadKey").getString("otherUserFbId")).messages.get(delta.getJSONArray("messageIds").getString(i)).delivered = true;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        MemoryManger.saveConversations(MainService.this);
                        callbacks.deliveryReceipt(delta);
                    }
                });
            }
            isStarted = true;
        }
    }
}
