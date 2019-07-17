package com.bytedance.androidcamp.network.dou.fragment1;

public interface OnViewPagerListener {
    void onInitComplete();
    void onPageRelease(boolean isNext, int position);
    void onPageSelected(int position ,boolean isBottom);
}
