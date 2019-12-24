package com.startoonlabs.apps.pheezee.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.startoonlabs.apps.pheezee.R;
import com.startoonlabs.apps.pheezee.fragments.FragmentReportDay;
import com.startoonlabs.apps.pheezee.fragments.ReportMonth;
import com.startoonlabs.apps.pheezee.fragments.ReportOverall;
import com.startoonlabs.apps.pheezee.fragments.ReportWeek;
import com.startoonlabs.apps.pheezee.repository.MqttSyncRepository;

import org.json.JSONArray;

import java.io.File;

public class SessionReportActivity extends AppCompatActivity implements MqttSyncRepository.OnReportDataResponseListner {

    JSONArray session_arry;
    boolean inside_report_activity = true;
    Fragment fragment;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    ProgressDialog progress;

    TextView tv_day, tv_week, tv_month, tv_overall_summary, tv_overall;
    MqttSyncRepository repository;
    ImageView iv_go_back;


    public static String patientId="", phizioemail="", patientName="", dateofjoin="";;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_report);
        fragmentManager = getSupportFragmentManager();
        repository = new MqttSyncRepository(getApplication());
        repository.setOnReportDataResponseListener(this);
        declareView();

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



        iv_go_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SessionReportActivity.this, PatientsView.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
            }
        });


        tv_day.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeViewOfDayMonthWeek();
                tv_day.setTypeface(null, Typeface.BOLD);
                tv_day.setAlpha(1);
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
    }

    public void openDayFragment(){
            if (session_arry != null) {
                fragmentTransaction = fragmentManager.beginTransaction();
                fragment = new FragmentReportDay();
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
            fragmentTransaction = fragmentManager.beginTransaction();
            fragment = new ReportOverall();
            fragmentTransaction.replace(R.id.fragment_report_container, fragment);
            fragmentTransaction.commit();
            FragmentManager fm = getSupportFragmentManager();
            for (int i = 0; i < fm.getBackStackEntryCount(); ++i) {
                fm.popBackStack();
            }
        }
        else {
            showToast("Fetching report data, please wait...");
        }
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
    public void onReportDataReceived(JSONArray array, boolean response) {
        progress.dismiss();
        if (response){
            session_arry = array;
            changeViewOfDayMonthWeek();
            tv_day.setTypeface(null, Typeface.BOLD);
            tv_day.setAlpha(1);
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
