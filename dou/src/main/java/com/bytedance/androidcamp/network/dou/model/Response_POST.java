package com.bytedance.androidcamp.network.dou.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Response_POST {
    /**
     * results :
     * url :
     * success :
     */
    @SerializedName("url") private String url;
    @SerializedName("success") private boolean success;

    public String getUrl() { return url; }
    public boolean getSuccess() {return success; }
}
