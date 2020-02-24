package com.startoonlabs.apps.pheezee.fragments;


import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaScannerConnection;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.startoonlabs.apps.pheezee.R;
import com.startoonlabs.apps.pheezee.activities.MonitorActivity;
import com.startoonlabs.apps.pheezee.activities.PatientsView;
import com.startoonlabs.apps.pheezee.popup.SessionSummaryPopupWindow;
import com.startoonlabs.apps.pheezee.popup.SessionSummaryStandardPopupWindow;
import com.startoonlabs.apps.pheezee.repository.MqttSyncRepository;
import com.startoonlabs.apps.pheezee.room.Entity.SceduledSession;
import com.startoonlabs.apps.pheezee.utils.AngleOperations;
import com.startoonlabs.apps.pheezee.utils.BatteryOperation;
import com.startoonlabs.apps.pheezee.utils.ByteToArrayOperations;
import com.startoonlabs.apps.pheezee.utils.MuscleOperation;
import com.startoonlabs.apps.smileyprogressbar.VerticalStarProgressView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.startoonlabs.apps.pheezee.activities.MonitorActivity.IS_SCEDULED_SESSION;
import static com.startoonlabs.apps.pheezee.activities.MonitorActivity.IS_SCEDULED_SESSIONS_COMPLETED;
import static com.startoonlabs.apps.pheezee.activities.MonitorActivity.total_sceduled_size;
import static com.startoonlabs.apps.pheezee.utils.PackageTypes.ACHEDAMIC_TEACH_PLUS;
import static com.startoonlabs.apps.pheezee.utils.PackageTypes.NUMBER_OF_STARTS;
import static com.startoonlabs.apps.pheezee.utils.PackageTypes.NUMBER_OF_VALUES_FOR_BASE_LINE;
import static com.startoonlabs.apps.pheezee.utils.PackageTypes.ONE_STAR_VALUE;
import static com.startoonlabs.apps.pheezee.utils.PackageTypes.SECOND_TIME_FOR_STAR;
import static com.startoonlabs.apps.pheezee.utils.PackageTypes.STANDARD_PACKAGE;
import static com.startoonlabs.apps.pheezee.utils.PackageTypes.TEACH_PACKAGE;
import static com.startoonlabs.apps.pheezee.services.PheezeeBleService.battery_percent;
import static com.startoonlabs.apps.pheezee.services.PheezeeBleService.bluetooth_state;
import static com.startoonlabs.apps.pheezee.services.PheezeeBleService.device_disconnected_firmware;
import static com.startoonlabs.apps.pheezee.services.PheezeeBleService.device_state;
import static com.startoonlabs.apps.pheezee.services.PheezeeBleService.session_data;
import static com.startoonlabs.apps.pheezee.services.PheezeeBleService.usb_state;

/**
 * A simple {@link Fragment} subclass.
 */
public class StarMonitorFragment extends Fragment implements MqttSyncRepository.GetSessionNumberResponse {

    private int live_sceduled_size = 0;
    //Star related
    private int one_star_value = ONE_STAR_VALUE;
    private int base_value_first_10 = 0, base_value=0;
    private int second_time = SECOND_TIME_FOR_STAR;
    private boolean one_star_calibrated = false;

    private int phizio_packagetype = 0;
    //session inserted on server
    private boolean sessionCompleted = false, can_beeep_max = true,can_beep_min = true;
    MqttSyncRepository repository;
    private String str_body_orientation="",json_phizioemail = "", patientid = "", bodyorientation = "", patientname = "";
    TextView tv_session_no, tv_max_emg, tv_body_part, patientId, patientName;
    private int ui_rate = 0, gain_initial = 20, body_orientation = 0, angleCorrection = 0,
            currentAngle = 0, Seconds, Minutes, maxAngle, minAngle, maxEmgValue, orientation_position=0;

    boolean angleCorrected = false, deviceState = true, usbState = false;
    String bodypart, orientation = "NO", timeText = "", holdTimeValue = "0:0";
    private String  str_exercise_name, str_muscle_name, str_max_emg_selected, str_min_angle_selected, str_max_angle_selected;
    private int exercise_position, bodypart_position, repsselected, muscle_position;
    SharedPreferences sharedPreferences;
    JSONObject json_phizio = new JSONObject();
    JSONArray emgJsonArray, romJsonArray;
    List<Entry> dataPoints;
    LineChart lineChart;
    LineDataSet lineDataSet;
    LineData lineData, lineDataNew;
    Button timer, stopBtn, cancelBtn;
    long MillisecondTime, StartTime, TimeBuff, UpdateTime = 0L;
    Handler handler;
    Date rawdata_timestamp;
    Long tsLong = 0L;
    AngleOperations angleOperations;
    private boolean mSessionStarted = false;

    VerticalStarProgressView verticalStarProgressView;
    AlertDialog deviceDisconnectedDialog, usbPluggedInDialog, error_device_dialog;
    ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);

    File file_session_emgdata, file_dir_session_emgdata, file_session_romdata, file_session_sessiondetails;
    FileOutputStream outputStream_session_emgdata, outputStream_session_romdata, outputStream_session_sessiondetails;

    private String str_active_time,  str_reps, str_time = "Session time:   00 : 00";
    public void deviceDisconnectedPopup(boolean operation) {
        String title = "Device Disconnected";
        String message;
        if(operation){
            message = "Please come in range to the device to continue the session";
        }else {
            message = "Please come in range to start session";
        }
        AlertDialog.Builder deviceDisconnected = new AlertDialog.Builder(getActivity());
        deviceDisconnected.setTitle(title);
        deviceDisconnected.setMessage(message);
        deviceDisconnected.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        if(operation) {
            deviceDisconnected.setNegativeButton("End Session", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    stopBtn.performClick();
                }
            });
        }
        deviceDisconnectedDialog = deviceDisconnected.create();
        deviceDisconnectedDialog.show();
    }


    public void usbConnectedDialog(boolean operation) {
        String title = "Usb Connected";
        String message;
        if(operation){
            message = "Please disconnect usb to continue the session";
        }else {
            message = "Please disconnect usb to start session";
        }
        AlertDialog.Builder deviceDisconnected = new AlertDialog.Builder(getActivity());
        deviceDisconnected.setTitle(title);
        deviceDisconnected.setMessage(message);
        deviceDisconnected.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        if(operation) {
            deviceDisconnected.setNegativeButton("End Session", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    stopBtn.performClick();
                }
            });
        }
        usbPluggedInDialog = deviceDisconnected.create();
        usbPluggedInDialog.show();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_star_monitor, container, false);

        angleOperations = new AngleOperations();
        lineChart = root.findViewById(R.id.chart);
        timer = root.findViewById(R.id.timer);
        stopBtn = root.findViewById(R.id.stopBtn);
        patientId = root.findViewById(R.id.patientId);
        patientName = root.findViewById(R.id.patientName);
        tv_session_no = root.findViewById(R.id.tv_session_no);
        tv_body_part = root.findViewById(R.id.bodyPart);
        cancelBtn = root.findViewById(R.id.cancel);
        tv_max_emg = root.findViewById(R.id.tv_max_emg_show);
        verticalStarProgressView = root.findViewById(R.id.vertical_star_view);


        handler = new Handler();
        emgJsonArray = new JSONArray();
        romJsonArray = new JSONArray();
        repository = new MqttSyncRepository(getActivity().getApplication());
        repository.setOnSessionNumberResponse(this);



        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        try {
            json_phizio = new JSONObject(sharedPreferences.getString("phiziodetails", ""));
            json_phizioemail = json_phizio.getString("phizioemail");
            phizio_packagetype = json_phizio.getInt("packagetype");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(!IS_SCEDULED_SESSION)
            updateInitialValues();
        else {
            boolean session_present = ((MonitorActivity)getActivity()).isSceduledSessionsCompleted();
            if(!session_present){
                updateInitialValues(((MonitorActivity)getActivity()).getSceduledSessionListFirstItem());
            }
        }
        setListnersOnViews();

        MillisecondTime = 0L;
        StartTime = 0L;
        TimeBuff = 0L;
        UpdateTime = 0L;
        Seconds = 0;
        Minutes = 0;
        str_time = "Session time:   00 : 00";

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(device_state);
        intentFilter.addAction(bluetooth_state);
        intentFilter.addAction(usb_state);
        intentFilter.addAction(battery_percent);
        intentFilter.addAction(session_data);
        intentFilter.addAction(device_disconnected_firmware);
        getActivity().registerReceiver(session_data_receiver,intentFilter);

        creatGraphView();

        ((MonitorActivity)getActivity()).getBasicDeviceInfo();
        return root;
    }

    private void setListnersOnViews() {
        timer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String message = BatteryOperation.getDialogMessageForLowBattery(PatientsView.deviceBatteryPercent, getActivity());
                if (!message.equalsIgnoreCase("c")) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Battery Low");
                    builder.setMessage(message);
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(deviceState && !usbState)
                                startSession();
                            else {
                                if(!deviceState)
                                    deviceDisconnectedPopup(false);
                                else
                                    usbConnectedDialog(false);
                            }
                        }
                    });
                    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    builder.show();
                } else{
                    if(deviceState && !usbState)
                        startSession();
                    else {
                        if(!deviceState)
                            deviceDisconnectedPopup(false);
                        else
                            usbConnectedDialog(false);
                    }
                }

            }
        });

        /**
         * Cancel session
         */
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (timer.getVisibility() == View.GONE) {
                    sessionCompleted = true;
                    mSessionStarted = false;
                    timer.setBackgroundResource(R.drawable.rounded_start_button);
                    stopBtn.setVisibility(View.GONE);
                    timer.setVisibility(View.VISIBLE);
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            ((MonitorActivity)getActivity()).disableNotificationOfSession();
                        }
                    });
                    TimeBuff += MillisecondTime;
                    handler.removeCallbacks(runnable);
                    MillisecondTime = 0L;
                    StartTime = 0L;
                    TimeBuff = 0L;
                    UpdateTime = 0L;
                    Seconds = 0;
                    Minutes = 0;
                    timer.setText(R.string.timer_start);
                    tsLong = System.currentTimeMillis();
                    if(phizio_packagetype==TEACH_PACKAGE||phizio_packagetype==ACHEDAMIC_TEACH_PLUS)
                        insertValuesAndNotifyMediaStore("Canceled");
                }
            }
        });

        /**
         * Stop session
         */
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sessionCompleted = true;
                mSessionStarted = false;
                cancelBtn.setVisibility(View.GONE);
                timer.setBackgroundResource(R.drawable.rounded_start_button);
                stopBtn.setVisibility(View.GONE);
                timer.setVisibility(View.VISIBLE);
                //Discable notifications
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        ((MonitorActivity)getActivity()).disableNotificationOfSession();
                    }
                });
                TimeBuff += MillisecondTime;
                handler.removeCallbacks(runnable);
                MillisecondTime = 0L;
                StartTime = 0L;
                TimeBuff = 0L;
                UpdateTime = 0L;
                Seconds = 0;
                Minutes = 0;
                timer.setText(R.string.timer_start);
                tsLong = System.currentTimeMillis();
                if(IS_SCEDULED_SESSION){
                    ((MonitorActivity)getActivity()).removeFirstFromSceduledList();
                    live_sceduled_size = ((MonitorActivity)getActivity()).getSceduledSize();
                    if(live_sceduled_size !=0){
                        updateInitialValues(((MonitorActivity)getActivity()).getSceduledSessionListFirstItem());
                    }else {
                        IS_SCEDULED_SESSIONS_COMPLETED = true;
                        repository.removeAllSessionsForPataient(patientid);
                    }
                }
                if(phizio_packagetype!=STANDARD_PACKAGE)
                    initiatePopupWindowModified();
                else
                    initiatePopupWindowStandard();

                if(phizio_packagetype==TEACH_PACKAGE||phizio_packagetype==ACHEDAMIC_TEACH_PLUS)
                    insertValuesAndNotifyMediaStore("Stopped");


            }
        });
    }

    private void updateInitialValues() {
        //get intent values
        patientid = getActivity().getIntent().getStringExtra("patientId");
        patientname = getActivity().getIntent().getStringExtra("patientName");
        bodyorientation = getActivity().getIntent().getStringExtra("bodyorientation");
        body_orientation = getActivity().getIntent().getIntExtra("body_orientation", 0);
        bodypart = getActivity().getIntent().getStringExtra("exerciseType");
        orientation = getActivity().getIntent().getStringExtra("orientation");
        str_body_orientation = getActivity().getIntent().getStringExtra("bodyorientation");
        str_exercise_name = getActivity().getIntent().getStringExtra("exercisename");

        str_muscle_name = getActivity().getIntent().getStringExtra("musclename");
        str_max_emg_selected = getActivity().getIntent().getStringExtra("maxemgselected");
        str_max_angle_selected = getActivity().getIntent().getStringExtra("maxangleselected");
        str_min_angle_selected = getActivity().getIntent().getStringExtra("minangleselected");
        exercise_position = getActivity().getIntent().getIntExtra("exerciseposition",0);
        bodypart_position = getActivity().getIntent().getIntExtra("bodypartposition",0);
        repsselected = getActivity().getIntent().getIntExtra("repsselected",0);
        muscle_position = getActivity().getIntent().getIntExtra("muscleposition",0);
        if(orientation.equalsIgnoreCase("left"))
            orientation_position=1;
        else
            orientation_position=2;

        //setting patient id and name
        if(patientid.length()>3){
            String temp = patientid.substring(0,3)+"xxx";
            patientId.setText(temp);
        }else {
            patientId.setText(patientid);
        }

        patientName.setText(patientname);


        //setting session number
        if(phizio_packagetype!=STANDARD_PACKAGE)
            repository.getPatientSessionNo(patientid);

        tv_body_part.setText(tv_body_part.getText().toString().concat(bodypart));
        tv_body_part.setText(orientation + "-" + bodypart + "-" + str_exercise_name);
    }

    private void updateInitialValues(SceduledSession session) {
        //get intent values
        live_sceduled_size = ((MonitorActivity)getActivity()).getSceduledSize();
        patientid = getActivity().getIntent().getStringExtra("patientId");
        patientname = getActivity().getIntent().getStringExtra("patientName");
        bodyorientation = session.getPosition();

        if (bodyorientation.equalsIgnoreCase("sit")) body_orientation = 2;
        else if (bodyorientation.equalsIgnoreCase("stand")) body_orientation = 1;
        else body_orientation = 3;


        bodypart = session.getBodypart();

        orientation = session.getSide();
        str_exercise_name = session.getExercise();

        str_muscle_name = session.getMuscle();
        str_max_emg_selected = session.getEmg();
        str_max_angle_selected = session.getAngleMax();
        str_min_angle_selected = session.getAngleMin();

        bodypart_position = MuscleOperation.getBodypartPosition(bodypart,getActivity());
        exercise_position = MuscleOperation.getExercisePosition(str_exercise_name,bodypart_position);
        try {
            repsselected = Integer.parseInt(session.getReps());
        }catch (NumberFormatException e){
            repsselected = 0;
            e.printStackTrace();
        }
        muscle_position = MuscleOperation.getMusclePosition(str_muscle_name,bodypart_position);
        if(orientation.equalsIgnoreCase("left"))
            orientation_position=1;
        else
            orientation_position=2;

        //setting patient id and name
        if(patientid.length()>3){
            String temp = patientid.substring(0,3)+"xxx";
            patientId.setText(temp);
        }else {
            patientId.setText(patientid);
        }

        patientName.setText(patientname);


        //setting session number
        if(phizio_packagetype!=STANDARD_PACKAGE)
            repository.getPatientSessionNo(patientid);

        tv_body_part.setText(session.getSessionno()+"/"+total_sceduled_size+":-"+orientation + "-" + bodypart + "-" + str_exercise_name);
    }

    /**
     * Inserts the summary values in files and also tells the media to scan the files for visibility when connected to the laptop.
     */
    private void insertValuesAndNotifyMediaStore(String session_action) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                try {
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String dateString = formatter.format(new Date(tsLong));
                    outputStream_session_sessiondetails.write("Held-On : ".concat(dateString).getBytes());

                    outputStream_session_sessiondetails.write("\n\n\n".getBytes());

                    outputStream_session_sessiondetails.write("Angle-Corrected : ".concat(String.valueOf(angleCorrection)).getBytes());

                    outputStream_session_sessiondetails.write("\n".getBytes());
                    outputStream_session_sessiondetails.write("Session Details".getBytes());
                    outputStream_session_sessiondetails.write("\n".getBytes());
                    outputStream_session_sessiondetails.write("Max Angle:".concat(String.valueOf(maxAngle)).getBytes());
                    outputStream_session_sessiondetails.write("\n".getBytes());
                    outputStream_session_sessiondetails.write("Min Angle:".concat(String.valueOf(minAngle)).getBytes());
                    outputStream_session_sessiondetails.write("\n".getBytes());
                    outputStream_session_sessiondetails.write("Max Emg:".concat(String.valueOf(maxEmgValue)).getBytes());
                    outputStream_session_sessiondetails.write("\n".getBytes());
                    outputStream_session_sessiondetails.write("Hold Time:".concat(holdTimeValue).getBytes());
                    outputStream_session_sessiondetails.write("\n".getBytes());
                    outputStream_session_sessiondetails.write("Num of Reps:".concat(str_reps).getBytes());
                    outputStream_session_sessiondetails.write("\n".getBytes());
                    outputStream_session_sessiondetails.write("Session Time:".concat(str_time).getBytes());
                    outputStream_session_sessiondetails.write("\n".getBytes());
                    outputStream_session_sessiondetails.write("Active Time:".concat(str_active_time).getBytes());
                    outputStream_session_sessiondetails.write("\n".getBytes());
                    outputStream_session_sessiondetails.write("Session: ".concat(session_action).getBytes());

                    outputStream_session_sessiondetails.write("\n\n\n".getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                MediaScannerConnection.scanFile(
                        getActivity().getApplicationContext(),
                        new String[]{file_session_emgdata.getAbsolutePath()},
                        null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            @Override
                            public void onScanCompleted(String path, Uri uri) {
                            }
                        });
                MediaScannerConnection.scanFile(
                        getActivity().getApplicationContext(),
                        new String[]{file_session_romdata.getAbsolutePath()},
                        null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            @Override
                            public void onScanCompleted(String path, Uri uri) {
                            }
                        });
                MediaScannerConnection.scanFile(
                        getActivity().getApplicationContext(),
                        new String[]{file_session_sessiondetails.getAbsolutePath()},
                        null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            @Override
                            public void onScanCompleted(String path, Uri uri) {
//                        Log.v("grokkingandroid",
//                                "file " + path + " was scanned seccessfully: " + uri);
                            }
                        });
            }
        });

    }


    public void startSession() {
        base_value=0;base_value_first_10=0;one_star_value=ONE_STAR_VALUE;
        one_star_calibrated =false;
        error_device_dialog=null;
        mSessionStarted = true;
        sessionCompleted = false;
        ui_rate = 0;
        angleCorrected = false;
        angleCorrection = 0;
        emgJsonArray = new JSONArray();
        romJsonArray = new JSONArray();
        maxAngle = 0;minAngle = 360;maxEmgValue = 0;
        can_beeep_max=true;can_beep_min=true;
        creatGraphView();
        timer.setVisibility(View.GONE);
        cancelBtn.setVisibility(View.VISIBLE);
        stopBtn.setVisibility(View.VISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ((MonitorActivity)getActivity()).sendBodypartDataToDevice(bodypart, body_orientation, patientname, exercise_position,
                        muscle_position, bodypart_position, orientation_position);
            }
        }, 100);
        rawdata_timestamp = Calendar.getInstance().getTime();
        if(phizio_packagetype==TEACH_PACKAGE||phizio_packagetype==ACHEDAMIC_TEACH_PLUS){
            initializeAndWriteInitialToFile();
        }
        StartTime = SystemClock.uptimeMillis();
        handler.postDelayed(runnable, 0);
    }

    private void initializeAndWriteInitialToFile() {
        android.text.format.DateFormat df = new android.text.format.DateFormat();
//            String s = rawdata_timestamp.toString().substring(0, 19);
        String s = String.valueOf(DateFormat.format("yyyy-MM-dd hh-mm-ssa", rawdata_timestamp));
        String child = patientname + patientid;
        file_dir_session_emgdata = new File(Environment.getExternalStorageDirectory() + "/Pheezee/files/EmgData/" + child + "/sessiondata/", s);
        if (!file_dir_session_emgdata.exists()) {
            file_dir_session_emgdata.mkdirs();
        }
        file_session_emgdata = new File(file_dir_session_emgdata, "emg.txt");
        file_session_romdata = new File(file_dir_session_emgdata, "rom.txt");
        file_session_sessiondetails = new File(file_dir_session_emgdata, "sessiondetails.txt");
        try {
            file_session_emgdata.createNewFile();
            file_session_romdata.createNewFile();
            file_session_sessiondetails.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            outputStream_session_emgdata = new FileOutputStream(file_session_emgdata, true);
            outputStream_session_romdata = new FileOutputStream(file_session_romdata, true);
            outputStream_session_sessiondetails = new FileOutputStream(file_session_sessiondetails, true);

            //emg file output stream
            outputStream_session_emgdata.write("EMG".getBytes());
            outputStream_session_emgdata.write("\n".getBytes());

            //rom file output stream
            outputStream_session_romdata.write("EMG".getBytes());
            outputStream_session_romdata.write("\n".getBytes());


            //sessiondetails file output stream
            outputStream_session_sessiondetails.write("Patient Name : ".getBytes());
            outputStream_session_sessiondetails.write(patientname.getBytes());
            outputStream_session_sessiondetails.write("\n".getBytes());
            outputStream_session_sessiondetails.write("Patient Id: ".concat(patientid).getBytes());
            outputStream_session_sessiondetails.write("\n".getBytes());
            outputStream_session_sessiondetails.write("Orientation-Bodypart-ExerciseName-MuscleName : ".concat(orientation + "-" + bodypart + "-" + str_exercise_name+"-"+str_muscle_name).getBytes());
            outputStream_session_sessiondetails.write("\n".getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * handler for session time incrimental
     */
    public Runnable runnable = new Runnable() {
        public void run() {
            MillisecondTime = SystemClock.uptimeMillis() - StartTime;
            UpdateTime = TimeBuff + MillisecondTime;
            Seconds = (int) (UpdateTime / 1000);
            Minutes = Seconds / 60;
            Seconds = Seconds % 60;
            timeText = "Session time:   " + String.format("%02d", Minutes) + " : " + String.format("%02d", Seconds);
            str_time = timeText;

            if(!one_star_calibrated) {
                if (Seconds == second_time) {
                    if (ui_rate > 10) {
                        Log.i("Here",Seconds+", "+second_time);
                        if (maxEmgValue - base_value <= 60) {
                            Log.i("MAX-BASE",Seconds+", "+second_time);
                            second_time += SECOND_TIME_FOR_STAR;
                            if (second_time == 60) {
                                second_time = 0;
                            }
                        } else {
                            if (maxEmgValue > 0) {
                                one_star_value = maxEmgValue / NUMBER_OF_STARTS;
                                one_star_calibrated = true;
                                Log.i("CALIBRATED", String.valueOf(one_star_value));
                            }
                        }
                    }
                }
            }
            handler.postDelayed(this, 0);
        }
    };


    /**
     * Handler to post the values received from device in the view
     */
    @SuppressLint("HandlerLeak")
    public final Handler myHandler = new Handler() {
        public void handleMessage(Message message) {
            try {
                if (mSessionStarted) {

//                    ToneGenerator toneGen2 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                    int angleDetected = 0, num_of_reps = 0, hold_time_minutes, hold_time_seconds, active_time_minutes, active_time_seconds;
                    int emg_data, error_device = 0, progress=0;
                    byte[] sub_byte;
                    sub_byte = (byte[]) message.obj;
                    if (sub_byte != null) {
                        error_device = sub_byte[10] & 0xFF;
                        if (error_device == 0) {
                            emg_data = ByteToArrayOperations.getAngleFromData(sub_byte[0], sub_byte[1]);
                            Log.i("Calibration values",base_value+", "+one_star_value);
                            if(ui_rate<=NUMBER_OF_VALUES_FOR_BASE_LINE){
                                base_value_first_10+=emg_data;
                            }else {
                                base_value = base_value_first_10/NUMBER_OF_VALUES_FOR_BASE_LINE;
                            }
                            if(emg_data>base_value){
                                int num_of_starts = Math.abs(emg_data / one_star_value);
                                if(num_of_starts==0)
                                    num_of_starts += 1;
                                verticalStarProgressView.setProgress(num_of_starts);
                            }else {
                                verticalStarProgressView.setProgress(0);
                            }


                            angleDetected = ByteToArrayOperations.getAngleFromData(sub_byte[2], sub_byte[3]);
                            if (ui_rate == 0) {
                                minAngle = angleDetected;
                                maxAngle = angleDetected;
                            }
                            num_of_reps = ByteToArrayOperations.getNumberOfReps(sub_byte[4], sub_byte[5]);
                            hold_time_minutes = sub_byte[6];
                            hold_time_seconds = sub_byte[7];
                            active_time_minutes = sub_byte[8];
                            active_time_seconds = sub_byte[9];
                            currentAngle = angleDetected;
                            str_reps = "" + num_of_reps;
                            String minutesValue = "" + hold_time_minutes, secondsValue = "" + hold_time_seconds;
                            if (hold_time_minutes < 10)
                                minutesValue = "0" + hold_time_minutes;
                            if (hold_time_seconds < 10)
                                secondsValue = "0" + hold_time_seconds;
                            holdTimeValue = minutesValue + "m: " + secondsValue + "s";
                            romJsonArray.put(angleDetected);

//            //Beep
                            if (!str_max_angle_selected.equals("")) {
                                int x = Integer.parseInt(str_max_angle_selected);
                                if (angleDetected < x && !can_beeep_max) {
                                    can_beeep_max = true;
                                }
                            }

                            if (!str_min_angle_selected.equals("")) {
                                int x = Integer.parseInt(str_min_angle_selected);
                                if (angleDetected > x && !can_beep_min) {
                                    can_beep_min = true;
                                }
                            }

                            if(phizio_packagetype==TEACH_PACKAGE||phizio_packagetype==ACHEDAMIC_TEACH_PLUS){
                                try {
                                    outputStream_session_romdata = new FileOutputStream(file_session_romdata, true);
                                    outputStream_session_romdata.write(String.valueOf(angleDetected).getBytes());
                                    outputStream_session_romdata.write(",".getBytes());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    outputStream_session_romdata.flush();
                                    outputStream_session_romdata.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            LinearLayout.LayoutParams params;
                            ++ui_rate;
                            lineData.addEntry(new Entry((float) ui_rate / 50, emg_data), 0);

                            emgJsonArray.put(emg_data);
                            if(phizio_packagetype==TEACH_PACKAGE||phizio_packagetype==ACHEDAMIC_TEACH_PLUS){
                                try {
                                    outputStream_session_emgdata = new FileOutputStream(file_session_emgdata, true);
                                    outputStream_session_emgdata.write(String.valueOf(emg_data).getBytes());
                                    outputStream_session_emgdata.write(",".getBytes());
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    outputStream_session_emgdata.flush();
                                    outputStream_session_emgdata.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                            maxEmgValue = maxEmgValue < emg_data ? emg_data : maxEmgValue;
                            if (maxEmgValue == 0)
                                maxEmgValue = 1;
                            tv_max_emg.setText(String.valueOf(maxEmgValue));
                            lineChart.notifyDataSetChanged();
                            lineChart.invalidate();
                            lineChart.getXAxis();
                            lineChart.getAxisLeft();
                            lineChart.getAxisLeft().setValueFormatter(new IAxisValueFormatter() {
                                @Override
                                public String getFormattedValue(float value, AxisBase axis) {
                                    return (int) value + getResources().getString(R.string.emg_unit);
                                }
                            });
                            if (UpdateTime / 1000 > 3)
                                lineChart.setVisibleXRangeMaximum(5f);
                            lineChart.moveViewToX((float) ui_rate / 50);

                            //Beep
                            if (!str_max_angle_selected.equals("")) {
                                int x = Integer.parseInt(str_max_angle_selected);
                                if (angleDetected > x && can_beeep_max) {
                                    new Handler().post(new Runnable() {
                                        @Override
                                        public void run() {
//                                            ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                                            //toneGen1.startTone(ToneGenerator.TONE_PROP_BEEP,150);
                                            if(toneGen1!=null){
                                                toneGen1.stopTone();
                                            }
                                            toneGen1.startTone(ToneGenerator.TONE_PROP_ACK, 150);
                                        }
                                    });
                                    can_beeep_max = false;
                                }
                            }

                            if (!str_min_angle_selected.equals("")) {
                                int x = Integer.parseInt(str_min_angle_selected);
                                if (angleDetected <= x && can_beep_min) {
                                    new Handler().post(new Runnable() {
                                        @Override
                                        public void run() {
//                                            ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                                            //toneGen1.startTone(ToneGenerator.TONE_PROP_BEEP,150);
                                            if(toneGen1!=null){
                                                toneGen1.stopTone();
                                            }
                                            toneGen1.startTone(ToneGenerator.TONE_PROP_ACK, 150);
                                        }
                                    });
                                    can_beep_min = false;
                                }
                            }
                            maxAngle = maxAngle < angleDetected ? angleDetected : maxAngle;
                            minAngle = minAngle > angleDetected ? angleDetected : minAngle;
                            minutesValue = "" + active_time_minutes;
                            secondsValue = "" + active_time_seconds;
                            if (active_time_minutes < 10)
                                minutesValue = "0" + active_time_minutes;
                            if (active_time_seconds < 10)
                                secondsValue = "0" + active_time_seconds;
                            str_active_time = minutesValue + "m: " + secondsValue + "s";

                            if (num_of_reps >= repsselected && repsselected != 0 && !sessionCompleted) {
                                sessionCompleted = true;
                                openSuccessfullDialogAndCloseSession();
                            }
                        } else {
                            if(error_device_dialog==null)
                                errorInDeviceDialog();
                        }
                    }
                }
            } catch (IndexOutOfBoundsException e){
                e.printStackTrace();
            }
        }
    };

    /**
     * Close session in 2000ms once the session goal is reached
     */
    private void openSuccessfullDialogAndCloseSession() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Session Completed");
        builder.setMessage("You have reached the goal.");
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
        stopBtn.performClick();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                alertDialog.cancel();
            }
        }, 2000);
    }

    private void errorInDeviceDialog() {
        String title = "Error";
        String message = "Please make sure the device is placed properly as per the device placement pictures and make sure the wire connecting the two modules is not stretched. Reset the device to start again.";
        AlertDialog.Builder error_device = new AlertDialog.Builder(getActivity());
        error_device.setTitle(title);
        error_device.setCancelable(false);
        error_device.setMessage(message);
        error_device.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        error_device_dialog = error_device.create();
        error_device_dialog.show();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mSessionStarted=false;
        try {
            Objects.requireNonNull(getActivity()).unregisterReceiver(session_data_receiver);
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }


    /**
     * Refreshes the line graph
     */
    private void creatGraphView() {
        lineChart.setHardwareAccelerationEnabled(true);
        dataPoints = new ArrayList<>();
        dataPoints.add(new Entry(0, 0));
        lineDataSet = new LineDataSet(dataPoints, "");
        lineDataSet.setDrawCircles(false);
        lineDataSet.setValueTextSize(0);
        lineDataSet.setDrawValues(false);
        lineDataSet.setColor(getResources().getColor(R.color.pitch_black));
        lineData = new LineData(lineDataSet);
        lineDataNew = new LineData(lineDataSet);    //for 30000
        lineChart.getXAxis();
        lineChart.getXAxis().setAxisMinimum(0f);
        lineChart.getAxisLeft().setSpaceTop(60f);
        lineChart.getAxisRight().setSpaceTop(60f);
        lineChart.getAxisRight().setDrawLabels(false);
        lineChart.getAxisLeft().setStartAtZero(false);
        lineChart.getAxisLeft().setDrawGridLines(false);
        lineChart.getXAxis().setDrawGridLines(false);
        lineChart.getXAxis().setDrawAxisLine(false);
        lineChart.setHorizontalScrollBarEnabled(true);
        lineChart.getDescription().setEnabled(false);
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.setScaleXEnabled(true);
        lineChart.fitScreen();
        lineChart.setData(lineData);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initiatePopupWindowModified() {
        String sessionNo = tv_session_no.getText().toString();
        String sessiontime = str_time.substring(16);
        String actiontime = str_active_time;

        SessionSummaryPopupWindow window = new SessionSummaryPopupWindow(getActivity(), maxEmgValue, sessionNo, maxAngle, minAngle, orientation, bodypart,
                json_phizioemail, sessiontime, actiontime, holdTimeValue, str_reps,
                angleCorrection, patientid, patientname, tsLong, bodyorientation, getActivity().getIntent().getStringExtra("dateofjoin"), exercise_position,bodypart_position,
                str_muscle_name,str_exercise_name,str_min_angle_selected,str_max_angle_selected,str_max_emg_selected,repsselected);
        window.showWindow();
        window.storeLocalSessionDetails(emgJsonArray,romJsonArray);
        if(phizio_packagetype!=STANDARD_PACKAGE)
            repository.getPatientSessionNo(patientid);
        window.setOnSessionDataResponse(new MqttSyncRepository.OnSessionDataResponse() {
            @Override
            public void onInsertSessionData(Boolean response, String message) {
                if (response)
                    showToast(message);
            }

            @Override
            public void onSessionDeleted(Boolean response, String message) {
                showToast(message);
            }

            @Override
            public void onMmtValuesUpdated(Boolean response, String message) {
                showToast(message);
            }

            @Override
            public void onCommentSessionUpdated(Boolean response) {
            }
        });
    }


    private void initiatePopupWindowStandard() {
        String sessionNo = tv_session_no.getText().toString();
        String sessiontime = str_time.substring(16);
        String actiontime = str_active_time;

        //testing with empty emg and rom array
//        emgJsonArray = new JSONArray();
//        romJsonArray = new JSONArray();

        SessionSummaryStandardPopupWindow window = new SessionSummaryStandardPopupWindow(getActivity(), maxEmgValue, sessionNo, maxAngle, minAngle, orientation, bodypart,
                sessiontime, actiontime, holdTimeValue, str_reps,
                patientid, patientname, tsLong, exercise_position,bodypart_position,
                str_muscle_name,str_exercise_name,str_min_angle_selected,str_max_angle_selected,repsselected);
        window.showWindow();
    }


    /**
     * show toask
     *
     * @param message
     */
    private void showToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSessionNumberResponse(String sessionnumber) {
        tv_session_no.setText(sessionnumber);
    }

    BroadcastReceiver session_data_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equalsIgnoreCase(device_state)){

                boolean device_status = intent.getBooleanExtra(device_state,false);
                if(device_status){
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            deviceState = true;
                            if(mSessionStarted) {
                                ((MonitorActivity)getActivity()).sendBodypartDataToDevice(bodypart, body_orientation, patientname, exercise_position,
                                        muscle_position, bodypart_position, orientation_position);
                            }
                            if(deviceDisconnectedDialog!=null) {
                                deviceDisconnectedDialog.dismiss();
                            }
                        }
                    },2000);
                }else {
                    deviceState = false;
                    if(deviceDisconnectedDialog!=null) {
                        if (!deviceDisconnectedDialog.isShowing())
                            deviceDisconnectedPopup(mSessionStarted);
                    }else {
                        deviceDisconnectedPopup(mSessionStarted);
                    }
                }
            }else if(action.equalsIgnoreCase(bluetooth_state)){
                boolean ble_state = intent.getBooleanExtra(bluetooth_state,false);
                if(ble_state){
//                    showToast("Bluetooth Enabled");
                }else {
                    ((MonitorActivity)getActivity()).startBleRequest();
                }
            }else if(action.equalsIgnoreCase(usb_state)){
                boolean usb_status = intent.getBooleanExtra(usb_state,false);
                if(usb_status){
                    usbState = true;
                    if(usbPluggedInDialog!=null) {
                        if(!usbPluggedInDialog.isShowing())
                            usbConnectedDialog(mSessionStarted);
                    }else {
                        usbConnectedDialog(mSessionStarted);
                    }
                }else {
                    usbState = false;
                    if(usbPluggedInDialog!=null) {
                        usbPluggedInDialog.dismiss();
                    }
                    if(mSessionStarted){
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                ((MonitorActivity)getActivity()).sendBodypartDataToDevice(bodypart, body_orientation, patientname, exercise_position,
                                        muscle_position, bodypart_position, orientation_position);
                            }
                        },500);
                    }
                }
            }else if(action.equalsIgnoreCase(battery_percent)){
                String percent = intent.getStringExtra(battery_percent);
            }else if(action.equalsIgnoreCase(session_data)){
                if(mSessionStarted) {
                    Message message = new Message();
                    if(((MonitorActivity)getActivity()).getSessionData()!=null) {
                        message.obj = ((MonitorActivity) getActivity()).getSessionData().obj;
                        myHandler.sendMessage(message);
                    }
                }
            }else if(action.equalsIgnoreCase(device_disconnected_firmware)){
                boolean device_disconnected_status = intent.getBooleanExtra(device_disconnected_firmware,false);
                if(device_disconnected_status){
                    showToast("The device has been deactivated");
                    Intent i = new Intent(getActivity(), PatientsView.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(i);
                }
            }
        }
    };

}
