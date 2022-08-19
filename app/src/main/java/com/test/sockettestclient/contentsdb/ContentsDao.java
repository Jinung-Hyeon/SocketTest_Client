package com.test.sockettestclient.contentsdb;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ContentsDao {

    @Query("SELECT * FROM Contents")
    List<Contents> getAllContents();

    @Insert
    void insertContents(Contents contents);

    @Query("DELETE FROM Contents")
    void deleteAllContents();
}
