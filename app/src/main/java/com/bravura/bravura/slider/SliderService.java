package com.bravura.bravura.slider;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;


public interface SliderService {

    @GET("vk_auth.php")
    Call<ResponseBody> search(@Query("q") String q);

    @GET("download/{id}/{duration}/{url}/{tit_art}.mp3")
    Call<ResponseBody> download(
        @Path("id") String id,
        @Path("duration") String duration,
        @Path("url") String url,
        @Path("tit_art") String tit_art,
        @Query("extra") String extra
    );

}
