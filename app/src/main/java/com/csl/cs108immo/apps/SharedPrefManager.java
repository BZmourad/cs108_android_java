package com.csl.cs108immo.apps;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {
    private static final String SHARED_PREF_NAME = "user_session";
    private static final String SHARED_PREF_CONFIG = "app_config";
    private static final String KEY_USERNAME = "key_username";
    private static final String KEY_MODE = "key_mode"; // online or offline
    private static final String KEY_TOKEN = "key_token"; // for online mode
    private static final String KEY_USER_CODE = "key_user_code";
    private static final String KEY_USER_NAME = "key_user_name";
    
    // Configuration keys
    private static final String KEY_SERVER_URL = "key_server_url";
    private static final String KEY_APP_MODE = "key_app_mode"; // "online" or "offline"
    
    private static SharedPrefManager mInstance;
    private static Context mCtx;

    private SharedPrefManager(Context context) {
        mCtx = context;
    }

    public static synchronized SharedPrefManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SharedPrefManager(context);
        }
        return mInstance;
    }

    public void userLogin(String username, boolean isOnline, String token, String code, String name) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USERNAME, username);
        editor.putBoolean(KEY_MODE, isOnline);
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_USER_CODE, code);
        editor.putString(KEY_USER_NAME, name);
        editor.apply();
    }

    public boolean isLoggedIn() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_USERNAME, null) != null;
    }

    public String getUsername() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_USERNAME, null);
    }

    public String getUserCode() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_USER_CODE, null);
    }

    public String getUserName() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_USER_NAME, null);
    }

    public boolean isOnlineMode() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(KEY_MODE, true); // default to online
    }

    public String getToken() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_TOKEN, null);
    }

    public void logout() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
    
    // Configuration methods
    public void saveServerConfig(String serverUrl, String appMode) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_CONFIG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_SERVER_URL, serverUrl);
        editor.putString(KEY_APP_MODE, appMode);
        editor.apply();
    }
    
    public String getServerUrl() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_CONFIG, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_SERVER_URL, "http://196.203.88.132/GESTION_INV_CNAM_WEB/FR/");
    }
    
    public String getAppMode() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_CONFIG, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_APP_MODE, "online");
    }
}