package com.bytedance.androidcamp.network.dou.fragment1;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.bytedance.androidcamp.network.dou.R;
import com.bytedance.androidcamp.network.dou.api.IMiniDouyinService;
import com.bytedance.androidcamp.network.dou.model.Response_GET;
import com.bytedance.androidcamp.network.dou.model.Video;
import com.bytedance.androidcamp.network.dou.player.VideoPlayerIJK;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class FragmentRecommand extends Fragment {
    RecyclerView mRv;
    MyLayoutManager myLayoutManager;
    private Adapter adapter;
    private List<Video> mVideos = new ArrayList<>();
    private List<Integer> mIndex = new ArrayList<>();
    View view;

    ImageView pause;
    ImageView doubleClickImg1;
    ImageView doubleClickImg2;
    ProgressBar pbLoading;

    int LIST_MAX_COUNT = 100;
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

        fetchFeed(view);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter();
        mRv.setAdapter(adapter);
        adapter.notifyDataSetChanged();

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
                //releaseVideo(index);
            }

            @Override
            public void onPageSelected(View v, int position, boolean isbottom) {
                Log.e(TAG, "选择位置:" + position + " 下一页:" + isbottom);
                playVideo(v,position);
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
            Log.d("testtest",String.valueOf(mVideos.size()));
            int index = (int)( l % mVideos.size() );
            mIndex.add(index);

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

    private void releaseVideo(View v,int index){
        final VideoPlayerIJK ijkPlayer = v.findViewById(R.id.ijkPlayer);
        ijkPlayer.stop();
        //animate?
    }

    private void pauseVideo(View v,int index){
        final VideoPlayerIJK ijkPlayer = v.findViewById(R.id.ijkPlayer);
        ijkPlayer.pause();
    }

    public static RecyclerView.ViewHolder getHolder(RecyclerView rv,int index){
        if(null == rv || null == rv.getAdapter() || rv.getAdapter().getItemCount() == 0)
            return null;
        int count = rv.getAdapter().getItemCount();
        if(index < 0 || index > count -1)
            return null;
        RecyclerView.ViewHolder holder = rv.findViewHolderForAdapterPosition(index);
        if(holder == null){
            RecyclerView.RecycledViewPool pool = rv.getRecycledViewPool();
            int type=0;
            int recyclerViewCount = pool.getRecycledViewCount(type);
            holder = pool.getRecycledView(type);
            try{
                pool.putRecycledView(holder);
            }catch (Exception e){

            }
        }
        return holder;
    }

    private void playVideo(View v,int position) {
        final VideoPlayerIJK ijkPlayer = v.findViewById(R.id.ijkPlayer);
        ijkPlayer.start();
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

    @Override
    public void onResume() {
        super.onResume();
        mRv.getAdapter().notifyDataSetChanged();
    }

    //    private void videoScreenInit() {
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