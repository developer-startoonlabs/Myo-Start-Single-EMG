package com.startoonlabs.apps.pheezee.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ParseException;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.startoonlabs.apps.pheezee.R;
import com.startoonlabs.apps.pheezee.adapters.SessionListArrayAdapter;
import com.startoonlabs.apps.pheezee.adapters.SessionReportListArrayAdapter;
import com.startoonlabs.apps.pheezee.adapters.OverallReportListArrayAdapter;
import com.startoonlabs.apps.pheezee.classes.SessionListClass;
import com.startoonlabs.apps.pheezee.fragments.FragmentReportDay;
import com.startoonlabs.apps.pheezee.fragments.ReportMonth;
import com.startoonlabs.apps.pheezee.fragments.ReportOverall;
import com.startoonlabs.apps.pheezee.fragments.ReportWeek;
import com.startoonlabs.apps.pheezee.pojos.Overallresponse;
import com.startoonlabs.apps.pheezee.pojos.PatientStatusData;
import com.startoonlabs.apps.pheezee.pojos.SessionDetailsResult;
import com.startoonlabs.apps.pheezee.repository.MqttSyncRepository;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.startoonlabs.apps.pheezee.retrofit.GetDataService;
import com.startoonlabs.apps.pheezee.retrofit.RetrofitClientInstance;

public class SessionReportActivity extends AppCompatActivity implements MqttSyncRepository.OnReportDataResponseListner {

    JSONArray session_arry;
    boolean inside_report_activity = true;
    Fragment fragment;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    SessionReportListArrayAdapter sesssionreport_adapter;
    OverallReportListArrayAdapter overallreport_adapter;
    ProgressDialog progress;
    ListView lv_sessionlist;
    ArrayList<String> dates_sessions;
    Iterator iterator;

    TextView tv_day, tv_week, tv_month, tv_overall_summary, tv_overall;
    MqttSyncRepository repository;
    ImageView iv_go_back;
    ArrayList<SessionListClass> mSessionListResults,mOverallListResults;
    GetDataService getDataService;


    public static String patientId="", phizioemail="", patientName="", dateofjoin="";;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_report);

        fragmentManager = getSupportFragmentManager();
        repository = new MqttSyncRepository(getApplication());
        repository.setOnReportDataResponseListener(this);

        declareView();

        getDataService = RetrofitClientInstance.getRetrofitInstance().create(GetDataService.class);

        patientId = getIntent().getStringExtra("patientid");
        phizioemail = getIntent().getStringExtra("phizioemail");
        patientName = getIntent().getStringExtra("patientname");
        dateofjoin = getIntent().getStringExtra("dateofjoin");

        progress = new ProgressDialog(this);
        progress.setMessage("Generating report");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.show();
        progress.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    finish();
                    dialog.dismiss();
                }
                return true;
            }
        });


        repository.getReportData(phizioemail,patientId);

    }

    private void declareView() {

        tv_day = findViewById(R.id.tv_session_report_day);
        tv_month = findViewById(R.id.tv_session_report_month);
        tv_week = findViewById(R.id.tv_session_report_week);
        tv_overall_summary = findViewById(R.id.tv_session_report_overall_report);
        tv_overall = findViewById(R.id.tv_session_report_overall);
        iv_go_back = findViewById(R.id.iv_back_session_report);
        lv_sessionlist =findViewById(R.id.report_listview);
        mSessionListResults = new ArrayList<SessionListClass>();
        mOverallListResults = new ArrayList<SessionListClass>();




        iv_go_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SessionReportActivity.this, PatientsView.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
            }
        });

        lv_sessionlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });


        tv_day.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeViewOfDayMonthWeek();
                tv_day.setTypeface(null, Typeface.BOLD);
                tv_day.setAlpha(1);
                String htmlString="<b><u>Session</u></b>";
                tv_day.setText(Html.fromHtml(htmlString));
                openDayFragment();

            }
        });


        tv_month.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeViewOfDayMonthWeek();
                tv_month.setTypeface(null, Typeface.BOLD);
                tv_month.setAlpha(1);

                openMonthFragment();
            }
        });

        tv_week.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeViewOfDayMonthWeek();
                tv_week.setTypeface(null, Typeface.BOLD);
                tv_week.setAlpha(1);
                openWeekFragment();
            }
        });

        tv_overall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeViewOfDayMonthWeek();
                tv_overall.setTypeface(null, Typeface.BOLD);
                tv_overall.setAlpha(1);
                String htmlString="<b><u>Overall</u></b>";
                tv_overall.setText(Html.fromHtml(htmlString));

                openOverallFragment();
            }
        });
    }



    public void changeViewOfDayMonthWeek(){
        tv_month.setTypeface(null, Typeface.NORMAL);
        tv_week.setTypeface(null, Typeface.NORMAL);
        tv_day.setTypeface(null, Typeface.NORMAL);
        tv_overall.setTypeface(null, Typeface.NORMAL);
        tv_day.setAlpha(0.5f);
        tv_week.setAlpha(0.5f);
        tv_month.setAlpha(0.5f);
        tv_overall.setAlpha(0.5f);

        String htmlString="Overall";
        tv_overall.setText(Html.fromHtml(htmlString));

        htmlString="Session";
        tv_day.setText(Html.fromHtml(htmlString));
    }

    public void openDayFragment() {
        Log.d("session","checl");
//
            if (session_arry != null) {
//                fragmentTransaction = fragmentManager.beginTransaction();
//                fragment = new FragmentReportDay();
//                fragmentTransaction.replace(R.id.fragment_report_container, fragment);
//                fragmentTransaction.commit();
//                FragmentManager fm = getSupportFragmentManager();
//                for (int i = 0; i < fm.getBackStackEntryCount(); ++i) {
//                    fm.popBackStack();
//                }
                HashSet<String> hashSet = new HashSet<>();
                if(session_arry.length()>0) {
                    for (int i = 0; i < session_arry.length(); i++) {
                        try {
                            JSONObject object = session_arry.getJSONObject(i);
                            hashSet.add(object.getString("heldon").substring(0,10));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }
                iterator = hashSet.iterator();
                dates_sessions = new ArrayList<>();
                while (iterator.hasNext()){
                    dates_sessions.add(iterator.next()+"");

                }

                Collections.sort(dates_sessions,new Comparator<String>() {
                    @Override
                    public int compare(String arg0, String arg1) {
                        SimpleDateFormat format = new SimpleDateFormat(
                                "yyyy-MM-dd");
                        int compareResult = 0;
                        try {
                            Date arg0Date = format.parse(arg0);
                            Date arg1Date = format.parse(arg1);
                            compareResult = arg0Date.compareTo(arg1Date);
                        } catch (ParseException e) {
                            e.printStackTrace();
                            compareResult = arg0.compareTo(arg1);
                        } catch (java.text.ParseException e) {
                            e.printStackTrace();
                        }
                        return compareResult;
                    }
                });

                Collections.sort(dates_sessions,Collections.reverseOrder());

                mSessionListResults.clear();
                for (int i = 0; i < dates_sessions.size(); i++) {

                    Log.d("session","knside loop");

                        int counter=0;
                        SessionListClass temp= new SessionListClass();
                        temp.setHeldon(dates_sessions.get(i));
                        temp.setPatientid(patientId);
                        temp.setPatientemail(phizioemail);

                         if(session_arry.length()>0) {
                        for (int j = 0; j < session_arry.length(); j++) {
                            try {
                                JSONObject object = session_arry.getJSONObject(j);
                                Log.d("session",object.getString("heldon").substring(0,10));
                                Log.d("session",dates_sessions.get(i));
                                if(object.getString("heldon").substring(0,10).equals(dates_sessions.get(i))){
                                    counter=counter+1;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }
//                         Log.d("session","hashe here");
//                         Log.d("session",hashSet)
//                        temp.setSession_time(String.valueOf(Collections.frequency(hashSet,dates_sessions.get(i))));
                        temp.setSession_time(String.valueOf(counter));
                        mSessionListResults.add(temp);




                }


                sesssionreport_adapter = new SessionReportListArrayAdapter(this, mSessionListResults,this.getApplication());
                lv_sessionlist.setAdapter(sesssionreport_adapter);
            } else {
                showToast("Fetching report data, please wait...");
            }
    }

    public void openWeekFragment() {
            if (session_arry != null) {
                fragmentTransaction = fragmentManager.beginTransaction();
                fragment = new ReportWeek();
                fragmentTransaction.replace(R.id.fragment_report_container, fragment);
                fragmentTransaction.commit();
                FragmentManager fm = getSupportFragmentManager();
                for (int i = 0; i < fm.getBackStackEntryCount(); ++i) {
                    fm.popBackStack();
                }
            } else {
                showToast("Fetching report data, please wait...");
            }
    }

    public void openMonthFragment(){
        fragmentTransaction = fragmentManager.beginTransaction();
        fragment = new ReportMonth();
        fragmentTransaction.replace(R.id.fragment_report_container,fragment);
        fragmentTransaction.commit();
        FragmentManager fm = getSupportFragmentManager();
        for(int i = 0; i < fm.getBackStackEntryCount(); ++i) {
            fm.popBackStack();
        }
    }

    public void openOverallFragment(){
        if(session_arry!=null) {


                    // START
            Calendar calendar = Calendar.getInstance();
            String date = calenderToYYYMMDD(calendar);
            PatientStatusData data = new PatientStatusData(phizioemail, patientId,date,date);
            Call<Overallresponse> getOverall_list_respone = getDataService.getOverall_list(data);

            getOverall_list_respone.enqueue(new  Callback<Overallresponse>() {
                @Override
                public void onResponse(Call<Overallresponse> call, Response<Overallresponse> response) {

                    if(response.isSuccessful()){
                        mOverallListResults.clear();

                        if (response.code() == 200) {
//                            String response_s = response.body();

                            Overallresponse obj = response.body();

                            if(obj.getElbow()>0)
                            {
                                SessionListClass temp= new SessionListClass();
                                temp.setBodypart("Elbow");
                                temp.setSession_time(String.valueOf(obj.getElbow()));
                                temp.setPatientid(patientId);
                                temp.setPatientemail(phizioemail);
                                mOverallListResults.add(temp);

                            }
                            if(obj.getKnee()>0)
                            {
                                SessionListClass temp= new SessionListClass();
                                temp.setBodypart("Knee");
                                temp.setSession_time(String.valueOf(obj.getKnee()));
                                temp.setPatientid(patientId);
                                temp.setPatientemail(phizioemail);
                                mOverallListResults.add(temp);

                            }
                            if(obj.getAnkle()>0)
                            {
                                SessionListClass temp= new SessionListClass();
                                temp.setBodypart("Ankle");
                                temp.setSession_time(String.valueOf(obj.getAnkle()));
                                temp.setPatientid(patientId);
                                temp.setPatientemail(phizioemail);
                                mOverallListResults.add(temp);

                            }
                            if(obj.getHip()>0)
                            {
                                SessionListClass temp= new SessionListClass();
                                temp.setBodypart("Hip");
                                temp.setSession_time(String.valueOf(obj.getHip()));
                                temp.setPatientid(patientId);
                                temp.setPatientemail(phizioemail);
                                mOverallListResults.add(temp);

                            }
                            if(obj.getWrist()>0)
                            {
                                SessionListClass temp= new SessionListClass();
                                temp.setBodypart("Wrist");
                                temp.setSession_time(String.valueOf(obj.getWrist()));
                                temp.setPatientid(patientId);
                                temp.setPatientemail(phizioemail);
                                mOverallListResults.add(temp);

                            }
                            if(obj.getShoulder()>0)
                            {
                                SessionListClass temp= new SessionListClass();
                                temp.setBodypart("Shoulder");
                                temp.setSession_time(String.valueOf(obj.getShoulder()));
                                temp.setPatientid(patientId);
                                temp.setPatientemail(phizioemail);
                                mOverallListResults.add(temp);

                            }
                            if(obj.getForearm()>0)
                            {
                                SessionListClass temp= new SessionListClass();
                                temp.setBodypart("Forearm");
                                temp.setSession_time(String.valueOf(obj.getForearm()));
                                temp.setPatientid(patientId);
                                temp.setPatientemail(phizioemail);
                                mOverallListResults.add(temp);

                            }
                            if(obj.getSpine()>0)
                            {
                                SessionListClass temp= new SessionListClass();
                                temp.setBodypart("Spine");
                                temp.setSession_time(String.valueOf(obj.getSpine()));
                                temp.setPatientid(patientId);
                                temp.setPatientemail(phizioemail);
                                mOverallListResults.add(temp);

                            }
                            if(obj.getOthers()>0)
                            {
                                SessionListClass temp= new SessionListClass();
                                temp.setBodypart("Others");
                                temp.setSession_time(String.valueOf(obj.getOthers()));
                                temp.setPatientid(patientId);
                                temp.setPatientemail(phizioemail);
                                mOverallListResults.add(temp);

                            }
                            
                            SessionListClass temp= new SessionListClass();
                            temp.setBodypart("-");
                            overallreport_adapter.add(temp);
                            overallreport_adapter.remove(temp);

                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Overallresponse> call, @NonNull Throwable t) {

                }
            });

                    // END


            overallreport_adapter = new OverallReportListArrayAdapter(this, mOverallListResults,this.getApplication());
            lv_sessionlist.setAdapter(overallreport_adapter);
        }
        else {
            showToast("Fetching report data, please wait...");
        }
    }

    private String calenderToYYYMMDD(Calendar date){
        Date date_cal = date.getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String strDate = dateFormat.format(date_cal);
        return strDate;
    }


    @Override
    protected void onPause() {
        super.onPause();
        inside_report_activity = false;
    }

    @Override
    protected void onDestroy() {
        repository.disableReportDataListner();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(this, PatientsView.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
    }

    public JSONArray getSessions(){
        return session_arry;
    }

    public void showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onReportDataReceived(JSONArray array, boolean response) throws JSONException {
        progress.dismiss();
        if (response){
            session_arry = array;
            changeViewOfDayMonthWeek();
            tv_day.setTypeface(null, Typeface.BOLD);
            tv_day.setAlpha(1);
            String htmlString="<b><u>Session</u></b>";
            tv_day.setText(Html.fromHtml(htmlString));

            openDayFragment();
        }
        else {
            showToast("Server busy, please try later!");
            onBackPressed();
        }
    }

    @Override
    public void onDayReportReceived(File file, String message, Boolean response) {
    }





    @Override
    protected void onResume() {

        super.onResume();
        inside_report_activity = true;
    }

}
