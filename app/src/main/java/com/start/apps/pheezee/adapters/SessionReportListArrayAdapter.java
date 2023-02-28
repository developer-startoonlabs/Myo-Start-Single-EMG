package com.start.apps.pheezee.adapters;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.se.omapi.Session;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;

import retrofit2.http.Url;
import start.apps.pheezee.R;

import com.nayaastra.skewpdfview.SkewPdfView;
import com.start.apps.pheezee.classes.SessionListClass;
import com.start.apps.pheezee.pojos.GetReportDataResponse;
import com.start.apps.pheezee.repository.MqttSyncRepository;
import com.start.apps.pheezee.utils.DateOperations;
import com.start.apps.pheezee.utils.NetworkOperations;
import com.start.apps.pheezee.utils.WriteResponseBodyToDisk;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import static com.start.apps.pheezee.activities.SessionReportActivity.patientId;
import static com.start.apps.pheezee.activities.SessionReportActivity.patientName;
import static com.start.apps.pheezee.activities.SessionReportActivity.phizioemail;


public class SessionReportListArrayAdapter extends ArrayAdapter<SessionListClass> implements MqttSyncRepository.OnReportDataResponseListner {

    private TextView tv_s_no,tv_date, tv_exercise_no,tv_download_date;
    private Button view_button;
    private ImageView share_icon;

    SkewPdfView skewPdfView;


    private Context context;
    public ArrayList<SessionListClass> mSessionArrayList;
    MqttSyncRepository repository;
    ProgressDialog report_dialog;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    public SessionReportListArrayAdapter(Context context, ArrayList<SessionListClass> mSessionArrayList, Application application){
        super(context, R.layout.sessionsreport_listview_model, mSessionArrayList);
        this.mSessionArrayList=mSessionArrayList;
        repository = new MqttSyncRepository(application);
        repository.setOnReportDataResponseListener(this);
        this.context = context;
    }


    public void updateList(ArrayList<SessionListClass> mSessionArrayList){
        this.mSessionArrayList.clear();
        this.mSessionArrayList.addAll(mSessionArrayList);
        this.notifyDataSetChanged();
    }





    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

//        ViewHolder holder = null;

        if (convertView == null) {

            LayoutInflater vi = (LayoutInflater) context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.sessionsreport_listview_model, null);
//
//            holder = new ViewHolder();
//            holder.name = (CheckBox) convertView.findViewById(R.id.checkBox1);
//            holder.name.setChecked(true);
//            convertView.setTag(holder);
//
//            holder.name.setOnClickListener( new View.OnClickListener() {
//                public void onClick(View v) {
//                    CheckBox cb = (CheckBox) v ;
//                    SessionListClass session_list_element = (SessionListClass) cb.getTag();
//                    session_list_element.setSelected(cb.isChecked());
//                }
//            });
        }
//        else {
//            holder = (ViewHolder) convertView.getTag();
//        }


        SessionListClass selected_item = mSessionArrayList.get(position);
//        holder.name.setChecked(selected_item.isSelected());
//        holder.name.setTag(selected_item);

        tv_s_no = convertView.findViewById(R.id.tv_s_no);
        tv_date = convertView.findViewById(R.id.tv_date);
        tv_exercise_no = convertView.findViewById(R.id.tv_exercise_no);
        tv_download_date = convertView.findViewById(R.id.tv_download_date);
        view_button = convertView.findViewById(R.id.view_button);
        share_icon = convertView.findViewById(R.id.share_icon);

        tv_s_no.setText(String.valueOf(mSessionArrayList.size()-position)+".");

        //Date
        String test = mSessionArrayList.get(position).getHeldon();
        test=test.replace("-","/");
        String[] date_split = test.split("/");
        test = date_split[2]+"/"+date_split[1]+"/"+date_split[0];
        test = DateOperations.getDateInMonthAndDate(test);
        date_split = test.split(",");
        test = date_split[0];
        tv_date.setText(test);

        // Exercise
        tv_exercise_no.setText(mSessionArrayList.get(position).getSession_time()+" Movements");

        // Downloaded date - Using muscle name data as a substitute
        if(mSessionArrayList.get(position).getMuscle_name() != null) {
            tv_download_date.setText("Downloaded on "+mSessionArrayList.get(position).getMuscle_name());
            tv_download_date.setTextColor(context.getResources().getColor(R.color.background_green));
        }else
        {
            tv_download_date.setText("View report by downloading");
            tv_download_date.setTextColor(context.getResources().getColor(R.color.red));

        }

        view_button.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override
            public void onClick(View v) {
                if(NetworkOperations.isNetworkAvailable(context))
                    getDayReport(mSessionArrayList.get(position).getHeldon());
                else
                    NetworkOperations.networkError(context);
            }
        });




        share_icon.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override
            public void onClick(View v) {
                if(NetworkOperations.isNetworkAvailable(context))
                    getDayReportshare(mSessionArrayList.get(position).getHeldon(),context);
                else
                    NetworkOperations.networkError(context);



            }
        });



        return convertView;

    }

    private String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
//        cal.setTimeInMillis(time * 1000);
        String date = DateFormat.format("dd-MM-yyyy", cal).toString();
        return date;
    }

    /**
     * Retrofit call to get the report pdf from the server
     * @param date
     */



    private void getDayReport(String date){
        String url = "/getreport/"+patientId+"/"+phizioemail+"/" + date;
        String url1 = "http://"+"13.127.78.38:3000"+"/getreport/"+patientId+"/"+phizioemail+"/" + date;
        Log.i("url1", url1);


        Log.i("Url Testing",url);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        if(WriteResponseBodyToDisk.checkFileInDisk(patientId+date) && !sharedPreferences.getBoolean(patientId+date,true))
        {

            Intent target = new Intent(Intent.ACTION_VIEW);
            target.setDataAndType(FileProvider.getUriForFile(context, context.getPackageName() + ".my.package.name.provider", WriteResponseBodyToDisk.GetFileFromDisk(patientId+date)), "application/pdf");
            target.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            target.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            try {
                context.startActivity(target);

            } catch (ActivityNotFoundException e) {

                // Instruct the user to install a PDF reader here, or something
            }
        return;
        }

        report_dialog = new ProgressDialog(context);
        report_dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        report_dialog.setMessage("Please Wait");    //("Generating session report held on "+date+", please wait..");
        report_dialog.show();
        repository.getDayReport(url,patientId+date);
    }
    /**
     * Retrofit call to get the report pdf from the server
     * @param date
     */

    private void getDayReportshare(String date, Context context){
        String url = "/getreport/"+patientId+"/"+phizioemail+"/" + date;

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        if(WriteResponseBodyToDisk.checkFileInDisk(patientId+date) && !sharedPreferences.getBoolean(patientId+date,true))
        {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            Uri uri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".my.package.name.provider", WriteResponseBodyToDisk.GetFileFromDisk(patientId+date));
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            context.startActivity(shareIntent);
            return;
        }

        report_dialog = new ProgressDialog(context);
        report_dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        report_dialog.setMessage("Please Wait");       // ("Sharing session report held on "+date+", please wait..");
        report_dialog.show();
        repository.getDayReportshare(url,patientName+"-day",context,report_dialog);

    }

    public void sendToast(String message){
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public void sendShortToast(String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onReportDataReceived(GetReportDataResponse array, boolean response) {

    }

    @Override
    public void onDayReportReceived(File file, String message, Boolean response) {
        report_dialog.dismiss();
        if(response){
            Intent target = new Intent(Intent.ACTION_VIEW);
            target.setDataAndType(FileProvider.getUriForFile(context, context.getPackageName() + ".my.package.name.provider", file), "application/pdf");
            target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            target.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            try {
                context.startActivity(target);
            } catch (ActivityNotFoundException e) {
                // Instruct the user to install a PDF reader here, or something
            }
        }
        else {
     //    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
        }
    }


}

