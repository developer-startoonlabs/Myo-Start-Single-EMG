package com.startoonlabs.apps.pheezee.fragments;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.ParseException;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.startoonlabs.apps.pheezee.R;
import com.startoonlabs.apps.pheezee.activities.SessionReportActivity;
import com.startoonlabs.apps.pheezee.repository.MqttSyncRepository;
import com.startoonlabs.apps.pheezee.retrofit.GetDataService;
import com.startoonlabs.apps.pheezee.retrofit.RetrofitClientInstance;
import com.startoonlabs.apps.pheezee.utils.DateOperations;
import com.startoonlabs.apps.pheezee.utils.NetworkOperations;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.startoonlabs.apps.pheezee.activities.SessionReportActivity.patientId;
import static com.startoonlabs.apps.pheezee.activities.SessionReportActivity.patientName;
import static com.startoonlabs.apps.pheezee.activities.SessionReportActivity.phizioemail;

public class FragmentReportDay extends Fragment implements MqttSyncRepository.OnReportDataResponseListner {

    ImageView iv_left, iv_right;
    private int current_date_position = 0;
    TextView tv_day_report, tv_report_date ;
    String dateSelected = null;
    final Calendar myCalendar = Calendar.getInstance();
    JSONArray session_array;
    ArrayList<String> dates_sessions;
    Iterator iterator;
    ProgressDialog report_dialog;
    MqttSyncRepository repository;
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_fragment_report_day, container, false);
        tv_day_report = view.findViewById(R.id.fragment_day_generate_report);
        iv_left = view.findViewById(R.id.fragment_day_iv_left);
        iv_right = view.findViewById(R.id.fragment_day_iv_right);
        tv_report_date = view.findViewById(R.id.fragment_day_tv_report_date);
        session_array = ((SessionReportActivity)getActivity()).getSessions();
        report_dialog = new ProgressDialog(getActivity());
        report_dialog.setMessage("Generating day report please wait....");
        report_dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        report_dialog.setIndeterminate(true);
        repository = new MqttSyncRepository(getActivity().getApplication());
        repository.setOnReportDataResponseListener(this);

        HashSet<String> hashSet = fetchAllDates();
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

        if(dates_sessions.size()>0) {
            current_date_position = dates_sessions.size() - 1;
            tv_report_date.setText(DateOperations.getDateInMonthAndDateNew(dates_sessions.get(current_date_position)));
        }

        iv_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dates_sessions.size()>0) {
                    if (current_date_position > 0) {
                        current_date_position--;
                    } else {
                        sendShortToast("Reached End");
                    }

                    tv_report_date.setText(DateOperations.getDateInMonthAndDateNew(dates_sessions.get(current_date_position)));
                }
                else {
                    sendToast("No sessions done");
                }
            }
        });

        iv_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dates_sessions.size()>0) {
                    if (current_date_position < dates_sessions.size() - 1) {
                        current_date_position++;
                    } else {
                        sendShortToast("Reached End");
                    }
                    tv_report_date.setText(DateOperations.getDateInMonthAndDateNew(dates_sessions.get(current_date_position)));
                }
                else {
                    sendToast("No sesssions done");
                }
            }
        });

        tv_day_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                openDatePicker();
                if(dates_sessions.size()>0){
                    if(NetworkOperations.isNetworkAvailable(getActivity()))
                        getDayReport(dates_sessions.get(current_date_position));
                    else
                        NetworkOperations.networkError(getActivity());
                }
                else {
                    sendToast("No sessions done");
                }
            }
        });
        return view;
    }

    public void openDatePicker() {
        new DatePickerDialog(getActivity(), dateChangedListener, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    DatePickerDialog.OnDateSetListener dateChangedListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, month);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();

            if(dateSelected!=null) {
                String url ="/getreport/"+patientId+"/"+phizioemail+"/" + dateSelected;
                report_dialog.setMessage("Generating day report for sessions held on "+dateSelected+", please wait....");
                report_dialog.show();
                repository.getDayReport(url,patientId);
            }
        }
    };

    /**
     * Retrofit call to get the report pdf from the server
     * @param date
     */
    private void getDayReport(String date){
        String url = "/getreport/"+patientId+"/"+phizioemail+"/" + date;
        report_dialog.setMessage("Generating day report for sessions held on "+date+", please wait....");
        report_dialog.show();
        repository.getDayReport(url,patientId);
    }



    private void updateLabel() {
        String myFormat = "yyyy-MM-dd"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());
        dateSelected = sdf.format(myCalendar.getTime());
        Log.i("date selected",dateSelected);
    }

    private HashSet<String> fetchAllDates() {
        HashSet<String> hashSet = new HashSet<>();
        if(session_array.length()>0) {
            for (int i = 0; i < session_array.length(); i++) {
                try {
                    JSONObject object = session_array.getJSONObject(i);
                    hashSet.add(object.getString("heldon").substring(0,10));
                    Log.i("datespresent",object.getString("heldon").substring(0,10));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }
        return hashSet;
    }

    public void sendToast(String message){
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    public void sendShortToast(String message){
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onReportDataReceived(JSONArray array, boolean response) {

    }

    @Override
    public void onDayReportReceived(File file, String message, Boolean response) {
        report_dialog.dismiss();
        if(response){
            Intent target = new Intent(Intent.ACTION_VIEW);
            target.setDataAndType(FileProvider.getUriForFile(getActivity(), getActivity().getPackageName() + ".my.package.name.provider", file), "application/pdf");
            target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            target.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            try {
                startActivity(target);
            } catch (ActivityNotFoundException e) {
                // Instruct the user to install a PDF reader here, or something
            }
        }
        else {
            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
        }
    }
}
