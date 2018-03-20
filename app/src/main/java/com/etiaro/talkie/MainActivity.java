package com.etiaro.talkie;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.FaceDetector;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonWriter;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.etiaro.facebook.Account;
import com.etiaro.facebook.Facebook;
import com.etiaro.facebook.Interfaces;
import com.etiaro.facebook.functions.GetThreadList;
import com.etiaro.facebook.functions.GetUserInfo;
import com.etiaro.facebook.functions.Login;

import org.json.JSONArray;
import org.json.JSONException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.shared_pref_codes), Context.MODE_PRIVATE);;
        String sharedPrefIDs = sharedPref.getString(getString(R.string.shared_pref_codes), null);


        if(getIntent().hasExtra(getString(R.string.intent_loggedIn))){ //logged-in
            saveAccIDs();
            saveAccount(Facebook.getInstance().accounts.get(getIntent().getStringExtra(getString(R.string.intent_loggedIn))));
            Log.d("talkie", "Fully logged in and saved");
        }else if(Facebook.getInstance().accounts.size() > 0) { //comeback to main Activity

        }else if (sharedPrefIDs != null){  // restart of app
            loadSharedPrefs(sharedPrefIDs);
            if(Facebook.getInstance().accounts.size() <= 0)
                runLoginActv();
        } else //No accounts, give login form
            runLoginActv();

        setContentView(R.layout.activity_main);

        for(String id : Facebook.getInstance().accounts.keySet()) {
            setTitle(Facebook.getInstance().accounts.get(id).getName());
            GetThreadList ui = new GetThreadList(Facebook.getInstance().accounts.get(id), 20, System.currentTimeMillis(), new String[]{"INBOX"});

            ui.execute(new Interfaces.ThreadListCallback() {
                @Override
                public void success(JSONArray list) {
                    ArrayList<String> arr = new ArrayList<>();
                    for(int i = 0; i < list.length(); i++){
                        try {
                            arr.add(list.getJSONObject(i).getJSONObject("last_message").getJSONArray("nodes")
                                    .getJSONObject(0).getString("snippet"));
                            Log.d("msg", list.getJSONObject(i).getJSONObject("last_message").getJSONArray("nodes")
                                    .getJSONObject(0).getString("snippet"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_list_item_1, arr);
                    ((ListView)findViewById(R.id.msgList)).setAdapter(arrayAdapter);
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


    private void runLoginActv(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void saveAccIDs(){
        JSONArray IDsJSON = new JSONArray();
        for(String id : Facebook.getInstance().accounts.keySet()){
            IDsJSON.put(id);
        }
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.shared_pref_codes), Context.MODE_PRIVATE);;
        sharedPref.edit().putString(getString(R.string.shared_pref_codes), IDsJSON.toString()).apply();
    }

    private void loadSharedPrefs(String IDs){
        try{
            JSONArray obj = new JSONArray(IDs);
            for(int i = 0; i < obj.length(); i++){
                SharedPreferences sp = getSharedPreferences(getString(R.string.shared_pref_codes)+"."+obj.getString(i), Context.MODE_PRIVATE);
                String JSON = sp.getString(getString(R.string.sp_account), null);
                if(JSON == null){
                    continue;
                }
                Account ac = new Account(JSON);
                Facebook.getInstance().accounts.put(ac.getUserID(), ac);
            }
        } catch (JSONException e) {
            Log.e("Talkie", "Critical error while parsing accounts data");
        }
        saveAccIDs(); //to delete failed accounts

        Log.d("talkie", "loaded accounts data");
    }

    private void saveAccount(Account ac){
        SharedPreferences sp = getSharedPreferences(getString(R.string.shared_pref_codes)+"."+ac.getUserID(), Context.MODE_PRIVATE);
        sp.edit().putString(getString(R.string.sp_account), ac.toString()).apply();
        Log.d("talkie", "Saved account data to "+getString(R.string.shared_pref_codes)+"."+ac.getUserID());
    }
    //TODO async Memory Manager
}
