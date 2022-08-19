package com.test.sockettestclient.contentsdb;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Contents.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    public abstract ContentsDao contentsDao();

    private static AppDatabase INSTANCE;

    public static AppDatabase getDBInstance(Context context){

        // INSTANCE가 null이면 초기화
        if (INSTANCE == null){
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "DID_Contents").allowMainThreadQueries().build();
        }
        return INSTANCE;
    }
}
