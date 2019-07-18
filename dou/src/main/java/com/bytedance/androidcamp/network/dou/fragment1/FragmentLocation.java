package com.bytedance.androidcamp.network.dou.fragment1;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bytedance.androidcamp.network.dou.R;
import com.bytedance.androidcamp.network.dou.api.IMiniDouyinService;
import com.bytedance.androidcamp.network.dou.model.Response_GET;
import com.bytedance.androidcamp.network.dou.model.Video;
import com.bytedance.androidcamp.network.dou.VideoActivity;
import com.bytedance.androidcamp.network.lib.util.ImageHelper;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FragmentLocation extends Fragment {
    RecyclerView mRv;
    MyLayoutManager myLayoutManager;
    private List<Video> mVideos = new ArrayList<>();
    View view;

    private Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(IMiniDouyinService.HOST)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    private IMiniDouyinService miniDouyinService  = retrofit.create(IMiniDouyinService.class);
    public float baseWidth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment1_location,container,false);

        DisplayMetrics dm = getResources().getDisplayMetrics();
        baseWidth = (float)(dm.widthPixels/2.0);// - dip2px(4));

        mRv = view.findViewById(R.id.rv);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        mRv.setLayoutManager(layoutManager);

        fetchFeed(view);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter();
        mRv.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        return view;
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter {
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            return new MyViewHolder(LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.rv_item_location,viewGroup,false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            final Video video = mVideos.get(position);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) holder.itemView.findViewById(R.id.img).getLayoutParams();
            layoutParams.width=(int)baseWidth;
            float factor=(baseWidth/(float)video.getImage_w());
            layoutParams.height=(int)(video.getImage_h()*factor);
            holder.itemView.findViewById(R.id.img).setLayoutParams(layoutParams);

            ((MyViewHolder)holder).bind(FragmentLocation.this.getActivity(),video);
        }

        @Override
        public int getItemCount() {
            return mVideos.size();
        }
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        private ImageView img;
        private TextView text;

        public MyViewHolder(@NonNull View Itemview){
            super(Itemview);
            img = Itemview.findViewById(R.id.img);
            text= Itemview.findViewById(R.id.text);
        }

        public void bind(final Activity activity, final Video video) {
            ImageHelper.displayWebImage(video.getImageUrl(), img);
            text.setText(video.getUserName());
            Log.d("getName",video.getUserName());
            img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    VideoActivity.launch(activity, video.getVideoUrl(), video.getUserName());
                }
            });
        }
    }


    public void fetchFeed(View view) {
        Call<Response_GET> call=miniDouyinService.getVideo();
        call.enqueue(new Callback<Response_GET>(){
            @Override
            public void onResponse(Call<Response_GET> call, Response<Response_GET> response) {
                if(response.body() != null && response.isSuccessful()){
                    mVideos=response.body().getVideos();
                    mRv.getAdapter().notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<Response_GET> call, Throwable throwable) {
                Toast.makeText(getActivity(), "视频流加载失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static int px2dip(Context context, float pxValue){
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int)(pxValue / scale + 0.5f);
    }

    public int dip2px(int dip) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dip * density + 0.5);
    }
}
