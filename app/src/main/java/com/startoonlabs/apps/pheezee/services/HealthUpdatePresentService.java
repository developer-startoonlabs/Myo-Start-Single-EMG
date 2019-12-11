package com.startoonlabs.apps.pheezee.services;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.startoonlabs.apps.pheezee.pojos.FirmwareUpdateCheck;
import com.startoonlabs.apps.pheezee.pojos.FirmwareUpdateCheckResponse;
import com.startoonlabs.apps.pheezee.pojos.HealthData;
import com.startoonlabs.apps.pheezee.retrofit.GetDataService;
import com.startoonlabs.apps.pheezee.retrofit.RetrofitClientInstance;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HealthUpdatePresentService extends JobService {
    private static final String TAG = "HealthStatusService";
    private boolean jobCancelled = false;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private GetDataService getDataService;

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.i(TAG,"sceduledHealthUpdate");
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        getDataService = RetrofitClientInstance.getRetrofitInstance().create(GetDataService.class);
        doInBackGround(params);
        return true;
    }

    private void doInBackGround(final JobParameters params) {

        if(jobCancelled){
            return;
        }
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE );
                NetworkInfo activeNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                NetworkInfo activeWifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                boolean isConnected = activeNetInfo != null && activeNetInfo.isConnectedOrConnecting();
                boolean isWifiConnected = activeWifiInfo != null && activeWifiInfo.isConnectedOrConnecting();
                if (isConnected || isWifiConnected ){
                    Gson gson = new GsonBuilder().create();
                    String health_Data = preferences.getString("health_data","");
                    Log.i("health_data1",health_Data);
                    if(!health_Data.equalsIgnoreCase("")){
                        Log.i("health_data2","Here");
                        HealthData data = gson.fromJson(health_Data,HealthData.class);
                        Log.i("health_data2",data.getUid());
                        Call<Boolean> call = getDataService.sendHealthStatusOfDevice(data);
                        call.enqueue(new Callback<Boolean>() {
                            @Override
                            public void onResponse(@NonNull Call<Boolean> call, @NonNull Response<Boolean> response) {
                                if(response.code()==200){
                                    boolean b = response.body();
                                    if(b){
                                        editor = preferences.edit();
                                        editor.putString("health_data","");
                                        editor.apply();
                                            jobFinished(params,false);
                                    }else {
                                        jobFinished(params,true);
                                    }
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<Boolean> call, @NonNull Throwable t) {
                                jobFinished(params,true);
                            }
                        });
                    }
                }

            }
        });
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        jobCancelled = true;
        return true;
    }
}