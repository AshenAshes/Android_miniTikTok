package com.bytedance.androidcamp.network.dou.api;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

import com.bytedance.androidcamp.network.dou.model.Response_GET;
import com.bytedance.androidcamp.network.dou.model.Response_POST;

public interface IMiniDouyinService {
    // TODO 7: Define IMiniDouyinService
    String HOST="http://test.androidcamp.bytedance.com/mini_douyin/invoke/";
    String PATH_POST="video";
    String PATH_GET="video";

    @Multipart
    @POST(PATH_POST)
    Call<Response_POST> postVideo(@Query("student_id") String student_id, @Query("user_name") String user_name,
                                  @Part MultipartBody.Part cover_image, @Part MultipartBody.Part video);

    @GET(PATH_GET)
    Call<Response_GET> getVideo();
}

