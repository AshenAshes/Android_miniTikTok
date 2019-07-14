package com.bytedance.androidcamp.network.dou;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.bytedance.androidcamp.network.dou.fragment1.FragmentLocation;
import com.bytedance.androidcamp.network.dou.fragment1.FragmentRecommand;
import com.google.android.material.tabs.TabLayout;

public class MainActivity1 extends AppCompatActivity {
    private static final int PAGE_COUNT=2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);

        TabLayout tableLayout = findViewById(R.id.tab_layout);
        LinearLayout mLinearLayout=(LinearLayout) tableLayout.getChildAt(0);
        mLinearLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
        mLinearLayout.setDividerDrawable(ContextCompat.getDrawable(this, R.drawable.tabdivider_vertical));
        mLinearLayout.setDividerPadding(dip2px(15));
//      mLinearLayout.setBackgroundColor(getResources().getColor(R.color.tabDividerColor));
        mLinearLayout.setBackgroundColor(0);

        ViewPager pager = findViewById(R.id.view_pager);

        bindActivity(R.id.bottomButton2, MainActivity2.class);
        bindActivity(R.id.bottomButton3, MainActivity3.class);
        bindActivity(R.id.bottomButton4, MainActivity4.class);
        bindActivity(R.id.bottomButton5, MainActivity5.class);

        pager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                if(position==0)
                    return new FragmentRecommand();
                else
                    return new FragmentLocation();
            }

            @Override
            public int getCount() {
                return PAGE_COUNT;
            }

            @Override
            public CharSequence getPageTitle(int position){
                if(position==0)
                    return "推荐";
                else
                    return "附近";
            }
        });
        tableLayout.setupWithViewPager(pager);
    }

    public int dip2px(int dip) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dip * density + 0.5);
    }

    private void bindActivity(final int btnId, final Class<?> activityClass){
        findViewById(btnId).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity1.this, activityClass);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
            }
        });
    }
}
