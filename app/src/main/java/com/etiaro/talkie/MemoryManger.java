package com.etiaro.talkie;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.etiaro.facebook.Account;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;

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
                    for(int i = 0; i < obj.length(); i++){
                        SharedPreferences sp = context.getSharedPreferences(context.getString(R.string.shared_pref_codes)+"."+obj.getString(i), Context.MODE_PRIVATE);
                        String JSON = sp.getString(context.getString(R.string.sp_account), null);
                        if(JSON == null){
                            continue;
                        }
                        Account ac = new Account(JSON);
                        accounts.put(ac.getUserID(), ac);
                    }
                } catch (JSONException e) {
                    Log.e("Talkie", "Critical error while parsing accounts data");
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

    public interface Callback{
        void call();
    }
}
