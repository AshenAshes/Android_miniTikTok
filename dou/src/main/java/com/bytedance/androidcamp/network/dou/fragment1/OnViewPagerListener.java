package com.bytedance.androidcamp.network.dou.fragment1;

import android.view.View;

public interface OnViewPagerListener {
    void onInitComplete();
    void onPageRelease(boolean isNext, int position);
    void onPageSelected(View view, int position , boolean isBottom);
}
