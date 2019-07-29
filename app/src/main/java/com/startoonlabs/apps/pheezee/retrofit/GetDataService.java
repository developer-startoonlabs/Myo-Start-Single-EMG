package com.startoonlabs.apps.pheezee.retrofit;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 *
 */
public interface GetDataService {
    @GET
    Call<ResponseBody> getReport(@Url String url);
}
