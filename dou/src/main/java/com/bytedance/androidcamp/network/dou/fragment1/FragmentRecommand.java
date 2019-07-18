package com.bytedance.androidcamp.network.dou.fragment1;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.Image;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bytedance.androidcamp.network.dou.R;
import com.bytedance.androidcamp.network.dou.VideoActivityGesture;
import com.bytedance.androidcamp.network.dou.api.IMiniDouyinService;
import com.bytedance.androidcamp.network.dou.model.Response_GET;
import com.bytedance.androidcamp.network.dou.model.Video;
import com.bytedance.androidcamp.network.dou.player.VideoPlayerIJK;
import com.bytedance.androidcamp.network.dou.player.VideoPlayerListener;
import com.bytedance.androidcamp.network.lib.util.ImageHelper;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

import static androidx.constraintlayout.widget.Constraints.TAG;
import static com.bytedance.androidcamp.network.dou.VideoActivityGesture.MSG_REFRESH;

public class FragmentRecommand extends Fragment {
    RecyclerView mRv;
    MyLayoutManager myLayoutManager;
    private Adapter adapter;
    private List<Video> mVideos = new ArrayList<>();
    View view;

    ImageView pause;
    ImageView doubleClickImg1;
    ImageView doubleClickImg2;
    ProgressBar pbLoading;

    private boolean isPortrait = true;
    private boolean menu_visible = true;
    boolean isPlayFinish = false;
    boolean isPrepare = false;
    int mVideoWidth = 0;
    int mVideoHeight = 0;
    RelativeLayout rlLoading;

    private Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(IMiniDouyinService.HOST)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    private IMiniDouyinService miniDouyinService  = retrofit.create(IMiniDouyinService.class);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment1_recommand,container,false);

        mRv = view.findViewById(R.id.rv);
        myLayoutManager = new MyLayoutManager(getActivity(), OrientationHelper.VERTICAL, false);
        mRv.setLayoutManager(myLayoutManager);
<<<<<<< HEAD

        fetchFeed(view);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter();
        mRv.setAdapter(adapter);
        adapter.notifyDataSetChanged();

=======

        fetchFeed(view);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter();
        mRv.setAdapter(adapter);
        adapter.notifyDataSetChanged();

>>>>>>> replace ijkplayer with videoview
        initListener();
        initIJKPlayer();
        return view;
    }

    private void initListener() {
        myLayoutManager.setOnViewPagerListener(new OnViewPagerListener() {
            @Override
            public void onInitComplete() {

            }

            @Override
            public void onPageRelease(boolean isNext, int position) {
                Log.e(TAG, "释放位置:" + position + " 下一页:" + isNext);
                int index = 0;
                if (isNext) {
                    index = 0;
                } else {
                    index = 1;
                }
<<<<<<< HEAD
                //releaseVideo(index);
=======
                releaseVideo(index);
>>>>>>> replace ijkplayer with videoview
            }

            @Override
            public void onPageSelected(int position, boolean bottom) {
                Log.e(TAG, "选择位置:" + position + " 下一页:" + bottom);
                playVideo(0);
            }
        });
    }

    private void initIJKPlayer(){
        //加载native库
        try {
            IjkMediaPlayer.loadLibrariesOnce(null);
            IjkMediaPlayer.native_profileBegin("libijkplayer.so");
        } catch (Exception e) {

        }
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter{
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MyViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.rv_item_recommend,parent,false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            final long l = System.currentTimeMillis();
            final int index = (int)( l % mVideos.size() );

            final Video video = mVideos.get(index);
            ((MyViewHolder)holder).bind(FragmentRecommand.this.getActivity(),video);
        }

        @Override
        public int getItemCount() {
            return mVideos.size();
        }
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        private VideoPlayerIJK ijkPlayer;
        private ImageView pause;

        private boolean isPlayFinish = false;
        private boolean isPrepare = false;

        public MyViewHolder(@NonNull View Itemview){
            super(Itemview);
            ijkPlayer=Itemview.findViewById(R.id.ijkPlayer);
            pause=Itemview.findViewById(R.id.pause);
        }

        public void bind(final Activity activity, final Video video) {
            String url = video.getVideoUrl();
            url=url.replaceFirst("https","http");
            ijkPlayer.setVideoPath(url);
        }
    }

    private void releaseVideo(int index){
        View itemView = mRv.getChildAt(index);
        final VideoPlayerIJK ijkPlayer = itemView.findViewById(R.id.ijkPlayer);
        final ImageView pause = itemView.findViewById(R.id.pause);
        ijkPlayer.stop();
        //animate?
    }

    private void playVideo(int position) {
        View itemView = mRv.getChildAt(position);
        final VideoPlayerIJK ijkPlayer = itemView.findViewById(R.id.ijkPlayer);
        ijkPlayer.start();
<<<<<<< HEAD
=======

//        ijkPlayer.setListener(new VideoPlayerListener(){
//            @Override
//            public void onBufferingUpdate(IMediaPlayer mp, int percent) {
//            }
//
//            @Override
//            public void onCompletion(IMediaPlayer mp) {
//            }
//
//            @Override
//            public boolean onError(IMediaPlayer mp, int what, int extra) {
//                return false;
//            }
//
//            @Override
//            public boolean onInfo(IMediaPlayer mp, int what, int extra) {
//                return false;
//            }
//
//            @Override
//            public void onPrepared(IMediaPlayer mp) {
//            }
//
//            @Override
//            public void onSeekComplete(IMediaPlayer mp) {
//            }
//
//            @Override
//            public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sar_num, int sar_den) {
//            }
//        });
>>>>>>> replace ijkplayer with videoview
    }


    public void fetchFeed(View view) {
        Call<Response_GET> call=miniDouyinService.getVideo();
        call.enqueue(new Callback<Response_GET>(){
            @Override
            public void onResponse(Call<Response_GET> call, Response<Response_GET> response) {
                if(response.body() != null && response.isSuccessful()){
                    List<Video> temp = response.body().getVideos();
                    for(Video i :temp){
                        if(i.getStudentId().equals("3170106666")){
                            mVideos.add(i);
                        }
                    }
                    mRv.getAdapter().notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<Response_GET> call, Throwable throwable) {
                Toast.makeText(getActivity(), "视频流加载失败", Toast.LENGTH_SHORT).show();
            }
        });
    }
<<<<<<< HEAD

    @Override
    public void onResume() {
        super.onResume();
        mRv.getAdapter().notifyDataSetChanged();
    }

    //    private void videoScreenInit() {
=======
//
//    private void videoScreenInit() {
>>>>>>> replace ijkplayer with videoview
//        if (isPortrait) {
//            portrait();
//        } else {
//            lanscape();
//        }
//    }
//
//    private void toggle() {
//        if (!isPortrait) {
//            portrait();
//        } else {
//            lanscape();
//        }
//    }
//
//    private void portrait() {
//        ijkPlayer.pause();
//        isPortrait = true;
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//
//        WindowManager wm = (WindowManager) this
//                .getSystemService(Context.WINDOW_SERVICE);
//        float width = wm.getDefaultDisplay().getWidth();
//        float height = wm.getDefaultDisplay().getHeight();
//        float ratio = width / height;
//        if (width < height) {
//            ratio = height/width;
//        }
//
//        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) rlPlayer.getLayoutParams();
//        layoutParams.height = (int) (mVideoHeight * ratio);
//        layoutParams.width = (int) width;
//        rlPlayer.setLayoutParams(layoutParams);
//        btnSetting.setText(getResources().getString(R.string.fullScreek));
//        ijkPlayer.start();
//    }
//
//    private void lanscape() {
//        ijkPlayer.pause();
//        isPortrait = false;
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//
//        WindowManager wm = (WindowManager) this
//                .getSystemService(Context.WINDOW_SERVICE);
//        float width = wm.getDefaultDisplay().getWidth();
//        float height = wm.getDefaultDisplay().getHeight();
//        float ratio = width / height;
//
//        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) rlPlayer.getLayoutParams();
//
//        layoutParams.height = (int) RelativeLayout.LayoutParams.MATCH_PARENT;
//        layoutParams.width = (int) RelativeLayout.LayoutParams.MATCH_PARENT;
//        rlPlayer.setLayoutParams(layoutParams);
//        btnSetting.setText(getResources().getString(R.string.smallScreen));
//        ijkPlayer.start();
//    }

}
