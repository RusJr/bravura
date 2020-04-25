package com.bravura.bravura.slider;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class SliderServiceGenerator {

    private static String BASE_URL = "https://slider.kz/";

    private static Retrofit.Builder builder = new Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create());

    private static Retrofit retrofit = builder.build();

    private static HttpLoggingInterceptor logging = new HttpLoggingInterceptor()
        .setLevel(HttpLoggingInterceptor.Level.BODY);

    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    public static SliderService createService() {
        httpClient.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Interceptor.Chain chain) throws IOException {
               Request original = chain.request();
               Request request = original.newBuilder()
                   .header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) snap Chromium/81.0.4044.92 Chrome/81.0.4044.92 Safari/537.36")
                   .header("Accept", "application/json, text/javascript, */*; q=0.01")
                   .header("X-Requested-With", "XMLHttpRequest")
                   .header("Referer", "https://slider.kz/")
//                   .header("Connection", "keep-alive")  TODO: mb
                   .method(original.method(), original.body())
                   .build();
               return chain.proceed(request);
            }
        });
        httpClient.addInterceptor(logging);
        builder.client(httpClient.build());
        retrofit = builder.build();


        return retrofit.create(SliderService.class);
    }
}
