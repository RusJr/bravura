package com.bravura.bravura.slider;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;


public interface SliderService {

    @GET("vk_auth.php")
    Call<ResponseBody> search(@Query("q") String q);

}


