package com.test.sockettestclient.retrofit;

import com.google.gson.annotations.SerializedName;

// json Object 안 contents 정보 사용
public class ContentsResult {
    @SerializedName("idx")
    public int idx;
    @SerializedName("speechTime")
    public int speechTime;
    @SerializedName("text")
    public String text;
    @SerializedName("contentsPath")
    public String contentsPath;

    @Override
    public String toString() {
        return "ContentsResult{" +
                "idx=" + idx +
                ", speechTime=" + speechTime +
                ", text='" + text + '\'' +
                ", contentsPath='" + contentsPath + '\'' +
                '}';
    }

    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public int getSpeechTime() {
        return speechTime;
    }

    public void setSpeechTime(int speechTime) {
        this.speechTime = speechTime;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getContentsPath() {
        return contentsPath;
    }

    public void setContentsPath(String contentsPath) {
        this.contentsPath = contentsPath;
    }
}
