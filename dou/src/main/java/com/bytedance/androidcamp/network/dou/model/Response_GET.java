package com.bytedance.androidcamp.network.dou.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Response_GET {
    /**
     * feeds :
     * success :
     */
    @SerializedName("feeds") private List<Video> Videos;
    @SerializedName("success") private boolean success;

    public List<Video> getVideos() { return Videos;}
    public boolean getSuccess() { return success; }

}
