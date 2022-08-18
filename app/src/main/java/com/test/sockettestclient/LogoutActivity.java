package com.test.sockettestclient;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class LogoutActivity extends AppCompatActivity {

    private static final String TAG = "AlarmTest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logout);
        ActionBar ac = getSupportActionBar();
        ac.hide();
        Log.e(TAG, "logoutActivity");
    }
}