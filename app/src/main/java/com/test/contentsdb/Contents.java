package com.test.contentsdb;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Contents {

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "idx")
    public int idx;

    @ColumnInfo(name = "text")
    public String text;

    @ColumnInfo(name = "imgUrl")
    public String imgUrl;

    @ColumnInfo(name = "speechTime")
    public int speechTime;
}
