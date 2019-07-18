package com.bytedance.androidcamp.network.dou;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.bytedance.androidcamp.network.dou.db.LikeContract;
import com.bytedance.androidcamp.network.dou.db.LikeDbHelper;
import com.bytedance.androidcamp.network.dou.gesture.BrightnessHelper;
import com.bytedance.androidcamp.network.dou.gesture.VideoGestureRelativeLayout;
import com.bytedance.androidcamp.network.dou.gesture.showChangeLayout;

public class VideoActivity extends AppCompatActivity  implements VideoGestureRelativeLayout.VideoGestureListener{
    String url;

    int mPlayingPos = 0;
    VideoView mVideoView;
    ProgressBar progressBar;

    ImageView btnFullScreen;
    ImageView btnPortraitScreen;
    ImageView btnPause;
    ImageView btnPlay;
    ImageView pause;
    SeekBar seekBar;
    RelativeLayout rl_bottom;
    TextView tvTime;
    TextView likecount;

    RelativeLayout relativeLayout;
    ImageView doubleClickImg1;
    ImageView doubleClickImg2;

    Animator animator1;
    Animator animator2;

    private boolean isPortrait = true;
    private boolean menu_visible = false;

    int mVideoWidth = 0;
    int mVideoHeight = 0;

    private SQLiteDatabase database;
    private LikeDbHelper likeDbHelper;
    private TextView textView;


    private final String TAG = "gesturetestm";
    private VideoGestureRelativeLayout ly_VG;
    private showChangeLayout scl;
    private AudioManager mAudioManager;
    private int maxVolume = 0;
    private int oldVolume = 0;
    private int newProgress = 0, oldProgress = 0;
    private BrightnessHelper mBrightnessHelper;
    private float brightness = 1;
    private Window mWindow;
    private WindowManager.LayoutParams mLayoutParams;

    private Handler handler;
    public static final int MSG_REFRESH = 1001;

    public static void launch(Activity activity, String url) {
        Intent intent = new Intent(activity, VideoActivity.class);
        intent.putExtra("url", url);
        activity.startActivity(intent);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        btnPlay = findViewById(R.id.btn_play);
        btnPause=findViewById(R.id.btn_pause);
        seekBar = findViewById(R.id.seekBar);
        btnFullScreen = findViewById(R.id.btn_fullScreen);
        btnPortraitScreen = findViewById(R.id.btn_portrait);
        rl_bottom = (RelativeLayout) findViewById(R.id.include_play_bottom);

        likeDbHelper=new LikeDbHelper(this);
        database=likeDbHelper.getWritableDatabase();

        url = getIntent().getStringExtra("url");
        mVideoView = findViewById(R.id.video_container);
        progressBar = findViewById(R.id.progress_bar);
        relativeLayout = findViewById(R.id.GestureView);
        doubleClickImg1 = findViewById(R.id.doubleClickImg1);
        doubleClickImg2 = findViewById(R.id.doubleClickImg2);
        animator1 = AnimatorInflater.loadAnimator(getApplicationContext(),R.animator.doubleclick);
        animator2 = AnimatorInflater.loadAnimator(getApplicationContext(),R.animator.doubleclick);
        likecount=findViewById(R.id.like_count);

        pause = findViewById(R.id.pause);
        pause.setVisibility(View.GONE);
        rl_bottom.setVisibility(View.INVISIBLE);

        tvTime = findViewById(R.id.tv_time);
//        tvLoadMsg = findViewById(R.id.tv_load_msg);
//        pbLoading = findViewById(R.id.pb_loading);
//        rlLoading = findViewById(R.id.rl_loading);
//        tvPlayEnd = findViewById(R.id.tv_play_end);
//        rlPlayer = findViewById(R.id.rl_player);

        ly_VG = (VideoGestureRelativeLayout) findViewById(R.id.GestureView);
        ly_VG.setVideoGestureListener(this);

        scl = (showChangeLayout) findViewById(R.id.ResponseView);

        //初始化获取音量属性
        mAudioManager = (AudioManager)getSystemService(Service.AUDIO_SERVICE);
        maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        //初始化亮度调节
        mBrightnessHelper = new BrightnessHelper(this);

        //下面这是设置当前APP亮度的方法配置
        mWindow = getWindow();
        mLayoutParams = mWindow.getAttributes();
        brightness = mLayoutParams.screenBrightness;

        mVideoView.setVideoURI(Uri.parse(url));
        mVideoView.requestFocus();
        mVideoView.start();
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                progressBar.setVisibility(View.GONE);
                refresh();
                handler.sendEmptyMessageDelayed(MSG_REFRESH, 50);
                mVideoWidth = mp.getVideoWidth();
                mVideoHeight = mp.getVideoHeight();
                //toggle();
                mp.start();
                pause.setVisibility(View.INVISIBLE);
            }
        });

        btnFullScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });
        btnPortraitScreen.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.start();
                mp.setLooping(true);
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //进度改变
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //开始拖动
                handler.removeCallbacksAndMessages(null);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //停止拖动
                mVideoView.seekTo(mVideoView.getDuration() * seekBar.getProgress() / 100);
                handler.sendEmptyMessageDelayed(MSG_REFRESH, 100);
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btnPlay.getVisibility()==View.VISIBLE) {
                    mVideoView.pause();
                    btnPlay.setVisibility(View.INVISIBLE);
                    btnPause.setVisibility(View.VISIBLE);
                    pause.setVisibility(View.VISIBLE);
                } else {
                    mVideoView.start();
                    btnPlay.setVisibility(View.VISIBLE);
                    btnPause.setVisibility(View.INVISIBLE);
                    pause.setVisibility(View.INVISIBLE);
                }
            }
        });

        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btnPlay.getVisibility()==View.VISIBLE) {
                    mVideoView.pause();
                    btnPlay.setVisibility(View.INVISIBLE);
                    btnPause.setVisibility(View.VISIBLE);
                    pause.setVisibility(View.VISIBLE);
                } else {
                    mVideoView.start();
                    btnPlay.setVisibility(View.VISIBLE);
                    btnPause.setVisibility(View.INVISIBLE);
                    pause.setVisibility(View.INVISIBLE);
                }
            }
        });

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_REFRESH:
                        if (mVideoView.isPlaying()) {
                            refresh();
                            handler.sendEmptyMessageDelayed(MSG_REFRESH, 50);
                        }
                        break;
                }
            }
        };

        progressBar.setVisibility(View.VISIBLE);
    }

    private void toggle() {
        if (!isPortrait) {
            portrait();
        } else {
            lanscape();
        }
    }

    private void portrait() {
        mVideoView.pause();
        isPortrait = true;
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        WindowManager wm = (WindowManager) this
                .getSystemService(Context.WINDOW_SERVICE);
        float width = wm.getDefaultDisplay().getWidth();
        float height = wm.getDefaultDisplay().getHeight();
        float ratio = width / height;
        if (width < height) {
            ratio = height/width;
        }

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mVideoView.getLayoutParams();
        Log.d("windowWidth:",String.valueOf(width));                //1080
        Log.d("windowHeight",String.valueOf(height));               //1920
        Log.d("layoutWidth",String.valueOf(layoutParams.width));    //-1
        Log.d("layoutHeight",String.valueOf(layoutParams.height));  //-1
        layoutParams.height = (int) (mVideoHeight * ratio);
        layoutParams.width = (int) width;
        mVideoView.setLayoutParams(layoutParams);
        btnFullScreen.setVisibility(View.INVISIBLE);
        btnPortraitScreen.setVisibility(View.VISIBLE);
        mVideoView.start();
    }

    private void lanscape() {
        mVideoView.pause();
        isPortrait = false;
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        WindowManager wm = (WindowManager) this
                .getSystemService(Context.WINDOW_SERVICE);
        float width = wm.getDefaultDisplay().getWidth();
        float height = wm.getDefaultDisplay().getHeight();
        float ratio = width / height;

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mVideoView.getLayoutParams();

        layoutParams.height = (int) RelativeLayout.LayoutParams.MATCH_PARENT;
        layoutParams.width = (int) RelativeLayout.LayoutParams.MATCH_PARENT;
        mVideoView.setLayoutParams(layoutParams);
        btnFullScreen.setVisibility(View.VISIBLE);
        btnPortraitScreen.setVisibility(View.INVISIBLE);
        mVideoView.start();
    }

    private void refresh() {
        long current = mVideoView.getCurrentPosition() / 1000;
        long duration = mVideoView.getDuration() / 1000;
        long current_second = current % 60;
        long current_minute = current / 60;
        long total_second = duration % 60;
        long total_minute = duration / 60;
        String time = current_minute + ":" + ((current_second>9) ? current_second : "0" + current_second) + "/"
                + total_minute + ":" + ((total_second>9) ? total_second : "0" + total_second);
        tvTime.setText(time);
        Log.d("refresh:",time);
        if (duration != 0) {
            seekBar.setProgress((int) (current * 100 / duration));
        }
    }

    @Override
    protected void onPause() {
        mPlayingPos = mVideoView.getCurrentPosition(); //先获取再stopPlay(),原因自己看源码
        mVideoView.stopPlayback();
        pause.setVisibility(View.VISIBLE);
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (mPlayingPos > 0) {
        //    mVideoView.start();
            progressBar.setVisibility(View.VISIBLE);
            mVideoView.seekTo(mPlayingPos);
            mPlayingPos = 0;
        }
        super.onResume();
    }

    @Override
    public void onBrightnessGesture(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        //这是直接设置系统亮度的方法
//        if (Math.abs(distanceY) > ly_VG.getHeight()/255){
//            if (distanceY > 0){
//                setBrightness(4);
//            }else {
//                setBrightness(-4);
//            }
//        }
        //下面这是设置当前APP亮度的方法
        Log.d(TAG, "onBrightnessGesture: old" + brightness);
        float newBrightness = (e1.getY() - e2.getY()) / ly_VG.getHeight() ;
        newBrightness += brightness;

        Log.d(TAG, "onBrightnessGesture: new" + newBrightness);
        if (newBrightness < 0){
            newBrightness = 0;
        }else if (newBrightness > 1){
            newBrightness = 1;
        }
        mLayoutParams.screenBrightness = newBrightness;
        mWindow.setAttributes(mLayoutParams);
        scl.setProgress((int) (newBrightness * 100));
        scl.setImageResource(R.drawable.brightness_w);
        scl.show();
    }

    //这是直接设置系统亮度的方法,上面调用的
    private void setBrightness(int brightness) {
        //要是有自动调节亮度，把它关掉
        mBrightnessHelper.offAutoBrightness();

        int oldBrightness = mBrightnessHelper.getBrightness();
        Log.d(TAG, "onBrightnessGesture: oldBrightness: " + oldBrightness);
        int newBrightness = oldBrightness + brightness;
        Log.d(TAG, "onBrightnessGesture: newBrightness: " + newBrightness);
        //设置亮度
        mBrightnessHelper.setSystemBrightness(newBrightness);
        //设置显示
        scl.setProgress((int) (Float.valueOf(newBrightness)/mBrightnessHelper.getMaxBrightness() * 100));
        scl.setImageResource(R.drawable.brightness_w);
        scl.show();
    }

    @Override
    public void onVolumeGesture(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        Log.d(TAG, "onVolumeGesture: oldVolume " + oldVolume);
        int value = ly_VG.getHeight()/maxVolume ;
        int newVolume = (int) ((e1.getY() - e2.getY())/value + oldVolume);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,newVolume,AudioManager.FLAG_PLAY_SOUND);

//      int newVolume = oldVolume;
        Log.d(TAG, "onVolumeGesture: value" + value);

        //另外一种调音量的方法，感觉体验不好，就没采用
//        if (distanceY > value){
//            newVolume = 1 + oldVolume;
//            mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
//        }else if (distanceY < -value){
//            newVolume = oldVolume - 1;
//            mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);
//        }
        Log.d(TAG, "onVolumeGesture: newVolume "+ newVolume);

        int volumeProgress = (int) (newVolume/Float.valueOf(maxVolume) *100);
        if (volumeProgress >= 50){
            scl.setImageResource(R.drawable.volume_higher_w);
        }else if (volumeProgress > 0){
            scl.setImageResource(R.drawable.volume_lower_w);
        }else {
            scl.setImageResource(R.drawable.volume_off_w);
        }
        scl.setProgress(volumeProgress);
        scl.show();
    }

    @Override
    public void onFF_REWGesture(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

    }

    @Override
    public void onSingleTapGesture(MotionEvent e) {
        // 暂停/播放
        if (mVideoView.isPlaying()) {
            Log.d("check:","isPlaying");
            btnPlay.setVisibility(View.INVISIBLE);
            btnPause.setVisibility(View.VISIBLE);
            pause.setVisibility(View.VISIBLE);
            mVideoView.pause();
        }
        else{
            Log.d("check:", "isNotPlaying");
            mVideoView.start();
            btnPlay.setVisibility(View.VISIBLE);
            btnPause.setVisibility(View.INVISIBLE);
            pause.setVisibility(View.INVISIBLE);
        }
        // 显示/隐藏进度条
        if (menu_visible == false) {
            rl_bottom.setVisibility(View.VISIBLE);
            Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.show_bottom);
            rl_bottom.startAnimation(animation);
            menu_visible = true;
        } else {
            rl_bottom.setVisibility(View.INVISIBLE);
            Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.move_bottom);
            rl_bottom.startAnimation(animation);
            menu_visible = false;
        }
    }

    @Override
    public void onDoubleTapGesture(MotionEvent e) {
        int count=LoadLikeFromDatabase();
        likecount.setText(count+"");
        animator1.setTarget(doubleClickImg1);
        animator1.start();
        animator2.setTarget(doubleClickImg2);
        animator2.start();
    }

    @Override
    public void onDown(MotionEvent e) {
        //每次按下的时候更新当前亮度和音量，还有进度
        oldProgress = newProgress;
        oldVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        brightness = mLayoutParams.screenBrightness;
        if (brightness == -1) {
            //一开始是默认亮度的时候，获取系统亮度，计算比例值
            brightness = mBrightnessHelper.getBrightness() / 255f;
        }
    }

    @Override
    public void onEndFF_REW(MotionEvent e) {

    }

    private int LoadLikeFromDatabase(){
        if(database==null){
            return -1;
        }else{
            int result;
            Cursor cursor=null;
            try{
                cursor=database.query(LikeContract.LikeNode.TABLE_NAME, null,"url like ?",
                        new String[]{url},null,null,null);
                if(cursor!=null && cursor.moveToNext()){
                    result=cursor.getInt(cursor.getColumnIndex(LikeContract.LikeNode.COLUMN_COUNT));
                }else{
                    ContentValues values=new ContentValues();
                    int count=(int)(100+Math.random()*200);
                    values.put(LikeContract.LikeNode.COLUMN_COUNT,count);
                    values.put(LikeContract.LikeNode.COLUMN_STATE,1);
                    values.put(LikeContract.LikeNode.COLUMN_URL,url);
                    long rowId=database.insert(LikeContract.LikeNode.TABLE_NAME,null,values);
                    if(rowId!=-1){
                        return count;
                    }else{
                        return -1;
                    }
                }
            }finally {
                if(cursor!=null){
                    cursor.close();
                }
            }
            return result;
        }
    }
}
