package com.etiaro.talkie;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.telecom.Call;
import android.util.Log;

import com.etiaro.facebook.Account;
import com.etiaro.facebook.Attachment;
import com.etiaro.facebook.Conversation;
import com.etiaro.facebook.functions.GetUserInfo;
import com.etiaro.facebook.functions.Login;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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
    private static HashMap<String, BitmapDrawable> imgs = new HashMap<>();
    private static HashMap<String, Long> onlineUsers = new HashMap<>();
    private static HashMap<String, Drawable> attachmects = new HashMap<>(); //TODO

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

    public static void loadSharedPrefs(Context context){
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
            if(JSON != null) {
                JSONObject users = new JSONObject(JSON);
                Iterator<String> it = users.keys();
                while (it.hasNext()) {
                    String key = it.next();
                    MemoryManger.users.put(key, new GetUserInfo.UserInfo(new JSONObject(users.getString(key))));
                }
            }
            //onlinestatus
            sp = context.getSharedPreferences(context.getString(R.string.shared_pref_status), Context.MODE_PRIVATE);
            for(Map.Entry<String, ?> user : sp.getAll().entrySet()){
                onlineUsers.put(user.getKey(), (Long)user.getValue());
            }
            //conversations
            sp = context.getSharedPreferences(context.getString(R.string.shared_pref_conversations), Context.MODE_PRIVATE);
            for(Map.Entry<String, ?> conv : sp.getAll().entrySet()){
                JSONObject tmp = new JSONObject((String)conv.getValue());
                conversations.put(conv.getKey(), new Conversation(tmp, tmp.getString("accountID")));
            }
            sortConversations();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Talkie", "Critical error while parsing accounts data "+e.toString());
        }
        saveAccIDs(context); //to delete failed accounts

        Log.d("talkie", "loaded accounts data");
    }
    public static void updateOnlineStatus(Context context, String user, Long status){
        onlineUsers.put(user, status);
        context.getSharedPreferences(context.getString(R.string.shared_pref_status), Context.MODE_PRIVATE).edit()
                .putLong(user, status).apply();
    }
    public static void updateOnlineStatus(Context c, Map<String, Long> users){
        for(Map.Entry<String, Long> user : users.entrySet()){
            updateOnlineStatus(c, user.getKey(), user.getValue());
        }
    }
    public static Long getUserStatus(String user){
        if(onlineUsers.containsKey(user))
            return onlineUsers.get(user);
        else
            return null;
    }
    public static void updateUsers(final Context context, GetUserInfo.UserInfo... list){
        for(GetUserInfo.UserInfo u : list) {
            if(users.containsKey(u.id))
                if(users.get(u.id).thumbSrc != u.thumbSrc){
                    loadImage(u.thumbSrc, u.id, context, null);//update image - changed
                }
            users.put(u.id, u);
        }
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
                Conversation convs[] = conversations.values().toArray(new Conversation[conversations.size()]);//avoid concurrentModificationException
                SharedPreferences.Editor sp = context.getSharedPreferences(context.getString(R.string.shared_pref_conversations), context.MODE_PRIVATE).edit();
                for(Conversation c : convs) {
                    sp.putString(c.thread_key, c.toString());
                }
                sp.apply();
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
                    Log.e("updateConv", e.toString());
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

    public static BitmapDrawable loadImage(final String _url, final String id, final Context context, final Callback cb){
        if(imgs.containsKey(id))
            return imgs.get(id);
        else if(new File(MemoryManger.getImagePath(id)).exists()){
            imgs.put(id, new BitmapDrawable(context.getResources(), MemoryManger.getImagePath(id)));
            return imgs.get(id);
        }else if(_url != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    URL url;
                    BufferedOutputStream out;
                    InputStream in;
                    BufferedInputStream buf;
                    try {
                        url = new URL(_url);
                        in = url.openStream();
                        // Read the inputstream
                        buf = new BufferedInputStream(in);

                        // Convert the BufferedInputStream to a Bitmap
                        final Bitmap bMap = BitmapFactory.decodeStream(buf);
                        if (in != null) {
                            in.close();
                        }
                        if (buf != null) {
                            buf.close();
                        }
                        MemoryManger.saveImage(context, bMap, id);

                        imgs.put(id, new BitmapDrawable(context.getResources(), bMap));
                        if(cb != null)
                            cb.call();
                    } catch (Exception e) {
                        Log.e("Error reading file", e.toString());
                    }
                }
            }).start();
        }
        return new BitmapDrawable(context.getResources(), BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher_background));
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
