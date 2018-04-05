package com.etiaro.talkie;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.telecom.Call;
import android.util.Log;

import com.etiaro.facebook.Account;
import com.etiaro.facebook.Conversation;
import com.etiaro.facebook.functions.GetUserInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jakub on 21.03.18.
 */

public class MemoryManger{
    //Thats the singlethon stuff
    private static MemoryManger instance = null;
    protected MemoryManger() {}
    public static MemoryManger getInstance() {
        if(instance == null) {
            instance = new MemoryManger();
        }
        return instance;
    }
    static public HashMap<String, Account> accounts = new HashMap<>();
    static public LinkedHashMap<String, Conversation> conversations = new LinkedHashMap<>();
    static public HashMap<String, GetUserInfo.UserInfo> users = new HashMap<>();

    public static void saveAccIDs(final Context context, final Callback callback){
        new Thread(new Runnable(){
            @Override
            public void run() {
                JSONArray IDsJSON = new JSONArray();
                for(String id : accounts.keySet()){
                    IDsJSON.put(id);
                }
                SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.shared_pref_codes), Context.MODE_PRIVATE);;
                sharedPref.edit().putString(context.getString(R.string.shared_pref_codes), IDsJSON.toString()).apply();

                if(callback != null)
                    callback.call();
            }
        }).start();
    }
    public static void saveAccIDs(final Context context){
        saveAccIDs(context, null);
    }

    public static void loadSharedPrefs(final Context context, final Callback callback){
        new Thread(new Runnable(){
            @Override
            public void run() {
                SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.shared_pref_codes), Context.MODE_PRIVATE);;
                String IDs = sharedPref.getString(context.getString(R.string.shared_pref_codes), null);
                try{
                    JSONArray obj = new JSONArray(IDs);
                    //accounts
                    for(int i = 0; i < obj.length(); i++){
                        SharedPreferences sp = context.getSharedPreferences(context.getString(R.string.shared_pref_codes)+"."+obj.getString(i), Context.MODE_PRIVATE);
                        String JSON = sp.getString(context.getString(R.string.sp_account), null);
                        if(JSON == null)
                            continue;
                        Account ac = new Account(JSON);
                        accounts.put(ac.getUserID(), ac);
                    }
                    //users
                    SharedPreferences sp = context.getSharedPreferences(context.getString(R.string.shared_pref_users), Context.MODE_PRIVATE);
                    String JSON = sp.getString(context.getString(R.string.sp_users), null);
                    JSONObject users = new JSONObject(JSON);
                    Iterator<String> it = users.keys();
                    while(it.hasNext()){
                        String key = it.next();
                        MemoryManger.users.put(key, new GetUserInfo.UserInfo(new JSONObject(users.getString(key))));
                    }
                    //conversations
                    sp = context.getSharedPreferences(context.getString(R.string.shared_pref_conversations), Context.MODE_PRIVATE);
                    JSON = sp.getString(context.getString(R.string.sp_conversations), null);

                    JSONObject convs = new JSONObject(JSON);
                    it = convs.keys();
                    while(it.hasNext()){
                        String key = it.next();
                        conversations.put(key, new Conversation(convs.getJSONObject(key), convs.getJSONObject(key).getString("accountID")));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("Talkie", "Critical error while parsing accounts data "+e.toString());
                }
                saveAccIDs(context); //to delete failed accounts

                Log.d("talkie", "loaded accounts data");

                if(callback != null)
                    callback.call();
            }
        }).start();
    }
    public static void loadSharedPrefs(final Context context){
        loadSharedPrefs(context, null);
    }

    public static void updateUsers(final Context context, GetUserInfo.UserInfo... list){
        for(GetUserInfo.UserInfo u : list)
            users.put(u.id, u);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                JSONObject obj = new JSONObject();
                for(GetUserInfo.UserInfo u : users.values())
                    obj.put(u.id, u.toString());
                SharedPreferences sp = context.getSharedPreferences(context.getString(R.string.shared_pref_users), Context.MODE_PRIVATE);
                sp.edit().putString(context.getString(R.string.sp_users), obj.toString()).apply();
                Log.d("talkie", "Saved users data");
                } catch (JSONException e) {
                    Log.e("Talkie","Error on saving users");
                }
            }
        }).start();
    }

    public static void saveAccount(final Context context, final Account ac, final Callback callback) {
        new Thread(new Runnable(){
            @Override
            public void run() {
                SharedPreferences sp = context.getSharedPreferences(context.getString(R.string.shared_pref_codes)+"."+ac.getUserID(), Context.MODE_PRIVATE);
                sp.edit().putString(context.getString(R.string.sp_account), ac.toString()).apply();
                Log.d("talkie", "Saved account data to "+context.getString(R.string.shared_pref_codes)+"."+ac.getUserID());
                if(callback != null)
                    callback.call();
            }
        }).start();
    }
    public static void saveAccount(final Context context, final Account ac){
        saveAccount(context, ac, null);
    }

    public static void saveConversations(final Context context, final Callback callback){
        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject json = new JSONObject();
                int num = 50; //saves ONLY @num newest conversations
                for(Conversation c : conversations.values()) {
                    if(num-- < 0)
                        break;
                    try {
                        json.put(c.thread_key, c.toJSON());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                SharedPreferences sp = context.getSharedPreferences(context.getString(R.string.shared_pref_conversations), context.MODE_PRIVATE);
                sp.edit().putString(context.getString(R.string.sp_conversations), json.toString()).apply();
                Log.d("info", "saved conversations");
                if(callback != null)
                    callback.call();
            }
        }).start();
    }
    public static void saveConversations(final Context context){
        saveConversations(context, null);
    }

    public static void updateConversations(Conversation... convs){
        for(Conversation c : convs){
            if(conversations.containsKey(c.thread_key))
                try {
                    conversations.get(c.thread_key).update(c.toJSON());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            else
                conversations.put(c.thread_key, c);
        }
        sortConversations();
    }
    static public void sortConversations(){
        orderByValue(MemoryManger.conversations, new Comparator<Conversation>() {
            @Override
            public int compare(Conversation c1, Conversation c2) {
                return c1.compareTo(c2);
            }
        });
    }
    static <K, V> void orderByValue(LinkedHashMap<K, V> m, final Comparator<? super V> c) {
        List<Map.Entry<K, V>> entries = new ArrayList<>(m.entrySet());
        Collections.sort(entries, new Comparator<Map.Entry<K, V>>() {
            @Override
            public int compare(Map.Entry<K, V> lhs, Map.Entry<K, V> rhs) {
                return c.compare(lhs.getValue(), rhs.getValue());
            }
        });
        m.clear();
        for(Map.Entry<K, V> e : entries) {
            m.put(e.getKey(), e.getValue());
        }
    }

    public static void saveImage(final Context context, final Bitmap bm, final String id, final Callback callback){
        new Thread(new Runnable() {
            @Override
            public void run() {
                ContextWrapper cw = new ContextWrapper(context);
                File directory = cw.getDir("thumbImg", Context.MODE_PRIVATE);
                // Create imageDir
                File mypath = new File(directory,id +".jpg");

                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(mypath);
                    // Use the compress method on the BitMap object to write image to the OutputStream
                    bm.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if(callback != null)
                    callback.call();
            }
        }).start();
    }
    public static void saveImage(Context context, Bitmap bitmap, String id){
        saveImage(context, bitmap, id, null);
    }
    public static String getImagePath(String id){
        return "/data/data/com.etiaro.talkie/app_thumbImg/"+id+".jpg";
    }

    public interface Callback{
        void call();
    }
}

//TODO conversations Saving, loading and sorting
