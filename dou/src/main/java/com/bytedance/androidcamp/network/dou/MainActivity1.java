package com.bytedance.androidcamp.network.dou;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bytedance.androidcamp.network.dou.finalclass.DoubleBack;
import com.bytedance.androidcamp.network.dou.fragment1.FragmentLocation;
import com.bytedance.androidcamp.network.dou.fragment1.FragmentRecommand;
import com.google.android.material.tabs.TabLayout;

public class MainActivity1 extends AppCompatActivity {
    private static final int PAGE_COUNT=2;
    DoubleBack doubleBack=new DoubleBack();
    LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);
        linearLayout=findViewById(R.id.linearLayout);

        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        TabLayout tableLayout = findViewById(R.id.tab_layout);
        LinearLayout mLinearLayout=(LinearLayout) tableLayout.getChildAt(0);
        mLinearLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
        mLinearLayout.setDividerDrawable(ContextCompat.getDrawable(this, R.drawable.tabdivider_vertical));
        mLinearLayout.setDividerPadding(dip2px(40));
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
                if (position == 0) {
                    //linearLayout.setBackgroundColor(0x00000000);//@null
                    return new FragmentRecommand();
                } else {
                   //linearLayout.setBackgroundColor(0xff000000);//black
                    return new FragmentLocation();
                }
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

        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if(position == 0){
                    linearLayout.setBackgroundColor(0x00000000);
                }
                else{
                    linearLayout.setBackgroundColor(0xff000000);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

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

    @Override
    public void onBackPressed() {
        //实现Home键效果
        //super.onBackPressed();这句话一定要注掉,不然又去调用默认的back处理方式了
        long nowTime=System.currentTimeMillis();
        long minusTime=nowTime-doubleBack.getFirstclickTime();
        if(minusTime > 2000){
            Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_LONG).show();
            doubleBack.setFirstClickTime(nowTime);
        }
        else{
            Intent intent = new Intent(MainActivity1.this,ExitActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

}