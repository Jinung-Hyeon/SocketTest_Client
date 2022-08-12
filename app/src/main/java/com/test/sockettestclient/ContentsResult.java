package com.test.sockettestclient;

import com.google.gson.annotations.SerializedName;

// json Object 안 contents 정보 사용
public class ContentsResult {
    @SerializedName("speechTime")
    int speechTime;
    @SerializedName("text")
    String text;
    @SerializedName("contentsPath")
    String contentsPath;

    @Override
    public String toString() {
        return "DataBean{" +
                "speechTime=" + speechTime +
                ", text='" + text + '\'' +
                ", contentsPath='" + contentsPath + '\'' +
                '}';
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
