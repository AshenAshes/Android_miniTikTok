package com.bytedance.androidcamp.network.dou.fragment1;

import android.animation.Animator;
import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import com.bytedance.androidcamp.network.dou.R;
import com.bytedance.androidcamp.network.dou.api.IMiniDouyinService;
import com.bytedance.androidcamp.network.dou.db.LikeContract;
import com.bytedance.androidcamp.network.dou.db.LikeDbHelper;
import com.bytedance.androidcamp.network.dou.model.Like;
import com.bytedance.androidcamp.network.dou.model.Response_GET;
import com.bytedance.androidcamp.network.dou.model.Video;
import com.bytedance.androidcamp.network.dou.player.VideoPlayerIJK;
import com.bytedance.androidcamp.network.dou.player.VideoPlayerListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class FragmentRecommand extends Fragment {
    RecyclerView mRv;
    MyLayoutManager myLayoutManager;
    LinearLayoutManager layoutManager;
    PagerSnapHelper snapHelper;
    private Adapter adapter;
    private List<Video> mVideos = new ArrayList<>();
    private List<Integer> mIndex = new ArrayList<>();
    View view;

    ImageView pause;
    ImageView doubleClickImg1;
    ImageView doubleClickImg2;
    Animator animator1;
    Animator animator2;
    ProgressBar pbLoading;

    int LIST_MAX_COUNT = 100;
    private boolean isPortrait = true;
    private boolean menu_visible = true;
    boolean isPlayFinish = false;
    boolean isPrepare = false;
    int mVideoWidth = 0;
    int mVideoHeight = 0;
    RelativeLayout rlLoading;
    int screenHeight = 0;

    private SQLiteDatabase database;
    private LikeDbHelper likeDbHelper;

    private Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(IMiniDouyinService.HOST)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    private IMiniDouyinService miniDouyinService = retrofit.create(IMiniDouyinService.class);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment1_recommand, container, false);

        mRv = view.findViewById(R.id.rv);
        screenHeight = getActivity().getResources().getDisplayMetrics().heightPixels;
        layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(mRv);
        mRv.setLayoutManager(layoutManager);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter();
        mRv.setAdapter(adapter);

        likeDbHelper=new LikeDbHelper(getActivity());
        database=likeDbHelper.getWritableDatabase();

        initIJKPlayer();

        fetchFeed();
        return view;
    }

    private void initIJKPlayer() {
        //加载native库
        try {
            IjkMediaPlayer.loadLibrariesOnce(null);
            IjkMediaPlayer.native_profileBegin("libijkplayer.so");
        } catch (Exception e) {

        }
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<MyViewHolder> {
        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MyViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.rv_item_recommend, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            final long l = System.currentTimeMillis();
            Log.d("testtest", String.valueOf(mVideos.size()));
            int index = (int) (l % mVideos.size());
            mIndex.add(index);

            final Video video = mVideos.get(index);
            Log.i("Video", "bind " + position);
            holder.bind(FragmentRecommand.this.getActivity(), video);
        }

        @Override
        public int getItemCount() {
            return mVideos.size();
        }

        @Override
        public void onViewAttachedToWindow(@NonNull MyViewHolder holder) {
            super.onViewAttachedToWindow(holder);
            Log.i("Video", "attach " + holder.getAdapterPosition());
            holder.attach();
        }

        @Override
        public void onViewDetachedFromWindow(@NonNull MyViewHolder holder) {
            super.onViewDetachedFromWindow(holder);
            Log.i("Video", "detach " + holder.getAdapterPosition());
            holder.detach();
        }
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private VideoPlayerIJK ijkPlayer;
        private ImageView pause;
        private ImageView imgLike;
        private ImageView imgRedLike;
        private TextView likeText;
        private boolean likeState=false;

        private boolean isPlayFinish = false;
        private boolean isAttach = false;
        private boolean isPrepare = false;

        private Video video;

        public MyViewHolder(@NonNull View Itemview) {
            super(Itemview);
            ijkPlayer = Itemview.findViewById(R.id.ijkPlayer);
            pause = Itemview.findViewById(R.id.pause);
            imgLike=Itemview.findViewById(R.id.like_img);
            imgRedLike=Itemview.findViewById(R.id.like_red_img);
            likeText=Itemview.findViewById(R.id.like_count);
        }

        public void bind(final Activity activity, final Video video) {
            this.video = video;
            pause.setVisibility(View.GONE);
            imgRedLike.setVisibility(View.INVISIBLE);
            imgLike.setVisibility(View.VISIBLE);
            ijkPlayer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (pause.getVisibility() == View.VISIBLE) {
                        if (isPlayFinish) {
                            isPlayFinish = false;
                            ijkPlayer.seekTo(0);
                        }
                        tryToPlay();
                        pause.setVisibility(View.GONE);
                    } else {
                        if (ijkPlayer.isPlaying()) {
                            ijkPlayer.pause();
                        }
                        pause.setVisibility(View.VISIBLE);
                    }
                }
            });
            imgLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(likeState==true){
                        likeState=false;
                        likeText.setTextColor(Color.WHITE);
                        imgRedLike.setVisibility(View.INVISIBLE);
                        imgLike.setVisibility(View.VISIBLE);
                    }
                    else{
                        likeState=true;
                        likeText.setTextColor(Color.RED);
                        imgRedLike.setVisibility(View.VISIBLE);
                        imgLike.setVisibility(View.INVISIBLE);
                    }
                }
            });
            imgRedLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(likeState==true){
                        likeState=false;
                        likeText.setTextColor(Color.WHITE);
                        imgRedLike.setVisibility(View.INVISIBLE);
                        imgLike.setVisibility(View.VISIBLE);
                    }
                    else{
                        likeState=true;
                        likeText.setTextColor(Color.RED);
                        imgRedLike.setVisibility(View.VISIBLE);
                        imgLike.setVisibility(View.INVISIBLE);
                    }
                }
            });
        }

        private void tryToPlay() {
            if (isAttach && isPrepare) {
                ijkPlayer.start();
            }
        }

        public void attach() {
            isAttach = true;
            String url = video.getVideoUrl();
            url = url.replaceFirst("https", "http");
            ijkPlayer.setVideoPath(url);
            ijkPlayer.setListener(new VideoPlayerListener() {
                @Override
                public void onPrepared(IMediaPlayer iMediaPlayer) {
                    super.onPrepared(iMediaPlayer);
                    isPrepare = true;
                    ijkPlayer.seekTo(0);
                    ijkPlayer.pause();
                    tryToPlay();
                }

                @Override
                public void onCompletion(IMediaPlayer iMediaPlayer) {
                    super.onCompletion(iMediaPlayer);
                    isPlayFinish = true;
                    pause.setVisibility(View.VISIBLE);
                }

                @Override
                public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
                    return super.onError(iMediaPlayer, i, i1);
                }
            });

        }

        public void detach() {
            isAttach = false;
            isPrepare = false;
            isPlayFinish = false;
            ijkPlayer.pause();
            ijkPlayer.stop();
            ijkPlayer.release();
            pause.setVisibility(View.GONE);
        }
    }

    public void fetchFeed() {
        Call<Response_GET> call = miniDouyinService.getVideo();
        call.enqueue(new Callback<Response_GET>() {
            @Override
            public void onResponse(Call<Response_GET> call, Response<Response_GET> response) {
                if (response.body() != null && response.isSuccessful()) {
                    List<Video> temp = response.body().getVideos();
                    for (Video i : temp) {
                        if (i.getStudentId().equals("3170106666")) {
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

    private Like LoadLikeFromDatabase(String url_in){
        Like like;
        if(database==null){
            return null;
        }else{
            Cursor cursor=null;
            try{
                cursor=database.query(LikeContract.LikeNode.TABLE_NAME, null,"url like ?",
                        new String[]{url_in},null,null,null);

                if(cursor!=null && cursor.moveToNext()){
                    long id=cursor.getLong(cursor.getColumnIndex(LikeContract.LikeNode._ID));
                    int count=cursor.getInt(cursor.getColumnIndex(LikeContract.LikeNode.COLUMN_COUNT));
                    int state=cursor.getInt(cursor.getColumnIndex(LikeContract.LikeNode.COLUMN_STATE));
                    String name=cursor.getString(cursor.getColumnIndex(LikeContract.LikeNode.COLUMN_NAME));
                    long date=cursor.getLong(cursor.getColumnIndex(LikeContract.LikeNode.COLUMN_DATE));
                    String url=cursor.getString(cursor.getColumnIndex(LikeContract.LikeNode.COLUMN_URL));
                    like=new Like(id);
                    like.setDate(new Date(date));
                    like.setName(name);
                    like.setCount(count);
                    like.setState(state);
                    like.setUrl(url);
                }else{
                    return null;
                }
            }finally {
                if(cursor!=null){
                    cursor.close();
                }
            }
        }
        return like;
    }

    private int LoadCountFromDatabase(String url, String name){
        int result =-1;
        if(database==null){
            Log.d("database", "LoadCountFromDatabase: ");
            return -1;
        }else{
            Cursor cursor=null;
            try{
                cursor=database.query(LikeContract.LikeNode.TABLE_NAME, null,"url like ?",
                        new String[]{url},null,null,null);

                if(cursor!=null && cursor.moveToNext()){
                    int count=cursor.getInt(cursor.getColumnIndex(LikeContract.LikeNode.COLUMN_COUNT));
                }else{
                    ContentValues values=new ContentValues();
                    int count=(int)(100+Math.random()*200);
                    values.put(LikeContract.LikeNode.COLUMN_COUNT,count);
                    values.put(LikeContract.LikeNode.COLUMN_STATE,0);
                    values.put(LikeContract.LikeNode.COLUMN_URL,url);
                    values.put(LikeContract.LikeNode.COLUMN_DATE,System.currentTimeMillis());
                    values.put(LikeContract.LikeNode.COLUMN_NAME,name);
                    long rowId=database.insert(LikeContract.LikeNode.TABLE_NAME,null,values);
                    if(rowId!=-1){
                        result=count;
                    }else{
                        Log.d("database", "LoadCountFromDatabase2: ");
                        return -1;
                    }
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