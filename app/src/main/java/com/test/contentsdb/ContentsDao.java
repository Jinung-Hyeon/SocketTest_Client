package com.test.contentsdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ContentsDao {

    @Query("SELECT * FROM contents")
    List<Contents> getAllContents();

    @Insert
    void insertContents(Contents contents);

    @Delete
    void deleteContents(Contents contents);
}
