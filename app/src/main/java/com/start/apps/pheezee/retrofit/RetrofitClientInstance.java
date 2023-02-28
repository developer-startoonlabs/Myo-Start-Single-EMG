package com.start.apps.pheezee.retrofit;


import okhttp3.OkHttpClient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;
/**
 * Retrofit instance
 */
public class RetrofitClientInstance {

    static final OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .build();
    private static Retrofit retrofit;
   private static final String BASE_URL = "http://13.127.78.38:3000";

//   private static final String BASE_URL = "http://13.127.78.38:7284";
//     private static final String BASE_URL = "http://3.111.80.246:5000";
   //  local server
//    private static final String BASE_URL = "https://testinglocalwithserver.pagekite.me";
    
    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            Gson gson = new GsonBuilder().setLenient().create();
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(okHttpClient)
                    .build();
        }
        return retrofit;
    }
}
