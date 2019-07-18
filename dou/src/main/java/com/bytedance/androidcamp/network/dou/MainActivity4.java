package com.bytedance.androidcamp.network.dou;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.bytedance.androidcamp.network.dou.db.LikeContract;
import com.bytedance.androidcamp.network.dou.db.LikeDbHelper;
import com.bytedance.androidcamp.network.dou.finalclass.DoubleBack;
import com.bytedance.androidcamp.network.dou.model.Like;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class MainActivity4 extends AppCompatActivity {
    private RecyclerView recyclerView;
    private likesAdapter likesAdapter;
    private LikeDbHelper dbHelper;
    private SQLiteDatabase database;
    DoubleBack doubleBack=new DoubleBack();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);

        bindActivity(R.id.bottomButton1, MainActivity1.class);
        bindActivity(R.id.bottomButton2, MainActivity2.class);
        bindActivity(R.id.bottomButton3, MainActivity3.class);
        bindActivity(R.id.bottomButton5, MainActivity5.class);

        Window window=getWindow();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.pageBackground));
        }
        dbHelper = new LikeDbHelper(this);
        database = dbHelper.getWritableDatabase();
        recyclerView = findViewById(R.id.divider);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                RecyclerView.VERTICAL, false));
        recyclerView.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        likesAdapter = new likesAdapter();
        recyclerView.setAdapter(likesAdapter);
        likesAdapter.refresh(LoadLikeFromDatabase());
    }

    private void bindActivity(final int btnId, final Class<?> activityClass){
        findViewById(btnId).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity4.this, activityClass);
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
            Intent intent = new Intent(MainActivity4.this,ExitActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    private List<Like> LoadLikeFromDatabase(){
        List<Like> result=new LinkedList<>();
        if(database==null){
            return null;
        }else{
            Cursor cursor=null;
            try{
                cursor=database.query(LikeContract.LikeNode.TABLE_NAME, null,null,
                        null,null,null, LikeContract.LikeNode.COLUMN_DATE+" DESC");
                while(cursor!=null && cursor.moveToNext()) {
                    long id = cursor.getLong(cursor.getColumnIndex(LikeContract.LikeNode._ID));
                    int count = cursor.getInt(cursor.getColumnIndex(LikeContract.LikeNode.COLUMN_COUNT));
                    int state = cursor.getInt(cursor.getColumnIndex(LikeContract.LikeNode.COLUMN_STATE));
                    String name = cursor.getString(cursor.getColumnIndex(LikeContract.LikeNode.COLUMN_NAME));
                    long date = cursor.getLong(cursor.getColumnIndex(LikeContract.LikeNode.COLUMN_DATE));
                    String url = cursor.getString(cursor.getColumnIndex(LikeContract.LikeNode.COLUMN_URL));
                    Like like;
                    like = new Like(id);
                    like.setDate(new Date(date));
                    like.setName(name);
                    like.setCount(count);
                    like.setState(state);
                    like.setUrl(url);
                    result.add(like);
                }
            }finally {
                if(cursor!=null){
                    cursor.close();
                }
            }
        }
        return result;
    }
}
