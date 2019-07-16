package com.bytedance.androidcamp.network.dou;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

public class VideoActivity extends AppCompatActivity {
    int mPlayingPos = 0;
    VideoView mVideoView;
    ProgressBar progressBar;
    ImageView pause;
    RelativeLayout relativeLayout;

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

        String url = getIntent().getStringExtra("url");
        mVideoView = findViewById(R.id.video_container);
        progressBar = findViewById(R.id.progress_bar);
        relativeLayout = findViewById(R.id.layout);

        pause = findViewById(R.id.pause);
        pause.setVisibility(View.GONE);

        mVideoView.setVideoURI(Uri.parse(url));
        mVideoView.requestFocus();
        mVideoView.start();
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                progressBar.setVisibility(View.GONE);
            }
        });

        relativeLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(mVideoView.isPlaying()){
                    mVideoView.pause();
                    pause.setVisibility(View.VISIBLE);
                }
                else{
                    mVideoView.start();
                    pause.setVisibility(View.GONE);
                }
                return false;
            }
        });

        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.start();
                mp.setLooping(true);
            }
        });

        progressBar.setVisibility(View.VISIBLE);
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
}
