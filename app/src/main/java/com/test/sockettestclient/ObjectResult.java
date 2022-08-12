package com.test.sockettestclient;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

// json Object 정보
public class ObjectResult {

    @SerializedName("contents")
    List<ContentsResult> contents = new ArrayList<>();

    public List<ContentsResult> getContents() {
        return contents;
    }

    public void setContents(List<ContentsResult> contents) {
        this.contents = contents;
    }

    @Override
    public String toString() {
        return "ObjectResult{" +
                "contents=" + contents +
                '}';
    }
}
