package com.test.contentsdb;

import android.content.Context;
import android.util.Log;

import com.test.sockettestclient.ObjectResult;

import java.util.List;

public class QueryContents {
    private Context mContext;

    public void loadContents(Context context){
        this.mContext = context;
        AppDatabase db = AppDatabase.getDBInstance(mContext.getApplicationContext());

        List<Contents> contentsList = db.contentsDao().getAllContents();
        for (Contents contents : contentsList) {
            Log.e("msg", "userList : " + contents.text);
        }
    }

    public void insertContents(Context context, List<ObjectResult> results){

    }
}
