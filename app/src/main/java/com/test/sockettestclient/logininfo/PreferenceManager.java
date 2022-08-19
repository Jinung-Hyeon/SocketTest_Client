package com.test.sockettestclient.logininfo;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {

    private static SharedPreferences getPreferences(Context context){
        return context.getSharedPreferences("rebuild_preference", Context.MODE_PRIVATE);
    }

    public static void setIpString(Context context, String key, String value) {
        SharedPreferences prefs = getPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getIpString(Context context, String key) {
        SharedPreferences prefs = getPreferences(context);
        String value = prefs.getString(key, "");
        return value;
    }


}
