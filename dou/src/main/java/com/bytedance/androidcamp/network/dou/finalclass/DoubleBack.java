package com.bytedance.androidcamp.network.dou.finalclass;

public final class DoubleBack {
    private static long firstClick=0;

    public long getFirstclickTime() { return firstClick;}
    public void setFirstClickTime(long time) { firstClick=time;}
}
