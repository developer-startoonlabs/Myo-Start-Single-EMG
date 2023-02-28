package com.start.apps.pheezee.fragments;
/**
 * Date of commented : 27-10-2022
 * Commented By : Burra Kranthi Kiran
 * Description : Included all import files related to StandradGoldTeachFragment screen
 */
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import android.text.InputType;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import start.apps.pheezee.R;
import com.start.apps.pheezee.activities.MonitorActivity;
import com.start.apps.pheezee.activities.PatientsView;
import com.start.apps.pheezee.classes.EmgPeak;
import com.start.apps.pheezee.popup.SessionSummaryPopupWindow;
import com.start.apps.pheezee.popup.SessionSummaryStandardPopupWindow;
import com.start.apps.pheezee.repository.MqttSyncRepository;
import com.start.apps.pheezee.room.Entity.SceduledSession;
import com.start.apps.pheezee.utils.AngleOperations;
import com.start.apps.pheezee.utils.BatteryOperation;
import com.start.apps.pheezee.utils.ByteToArrayOperations;
import com.start.apps.pheezee.utils.MuscleOperation;
import com.start.apps.pheezee.utils.ValueBasedColorOperations;
import com.start.apps.pheezee.views.ArcViewInside;

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
import android.util.Log;

import static com.facebook.FacebookSdk.getApplicationContext;
import static com.start.apps.pheezee.activities.MonitorActivity.IS_SCEDULED_SESSION;/** this will checked like boolean type the session is scheduled **/
import static com.start.apps.pheezee.activities.MonitorActivity.IS_SCEDULED_SESSIONS_COMPLETED;/** this will checked like boolean type the session_completion is scheduled **/
import static com.start.apps.pheezee.activities.MonitorActivity.show_popup_once;/** this will get from monitor screen pop function**/
import static com.start.apps.pheezee.activities.MonitorActivity.total_sceduled_size; /** Here we are getting the Values from ble hardware**/
import static com.start.apps.pheezee.utils.PackageTypes.ACHEDAMIC_TEACH_PLUS;/** This is used for geting the current phizio user package type ACHEDAMIC_TEACH_PLUS **/
import static com.start.apps.pheezee.utils.PackageTypes.GOLD_PACKAGE;/** This is used for geting the current phizio user package type GOLD_PACKAGE **/
import static com.start.apps.pheezee.utils.PackageTypes.GOLD_PLUS_PACKAGE;/** This is used for geting the current phizio user package type GOLD_PLUS_PACKAGE **/
import static com.start.apps.pheezee.utils.PackageTypes.MIN_PEAK_DECIDED;
import static com.start.apps.pheezee.utils.PackageTypes.PERCENTAGE_TEXT_TO_SPEACH_EMG_PEAK;
import static com.start.apps.pheezee.utils.PackageTypes.STANDARD_PACKAGE;/** This is used for geting the current phizio user package type STANDARD_PACKAGE **/
import static com.start.apps.pheezee.utils.PackageTypes.TEACH_PACKAGE;/** This is used for geting the current phizio user package type TEACH_PACKAGE **/
import static com.start.apps.pheezee.services.PheezeeBleService.battery_percent;/** This is used for getting battery percent from the ble services **/
import static com.start.apps.pheezee.services.PheezeeBleService.bluetooth_state;/** This is used for getting bluetooth state from the ble services **/
import static com.start.apps.pheezee.services.PheezeeBleService.device_disconnected_firmware;/** This is used for getting device_disconnected_firmware from the ble services **/
import static com.start.apps.pheezee.services.PheezeeBleService.device_state;/** This is used for getting device_state from the ble services **/
import static com.start.apps.pheezee.services.PheezeeBleService.session_data;/** This is used for getting session_data from the ble services **/
import static com.start.apps.pheezee.services.PheezeeBleService.usb_state;/** This is used for getting usb_state from the ble services **/
import android.view.WindowManager;
import com.start.apps.pheezee.classes.PatientActivitySingleton;
public class StandardGoldTeachFragment extends Fragment implements MqttSyncRepository.GetSessionNumberResponse {

    private boolean can_beep = false, can_voice = false;
    private int peakSpeachComdition = 0;
    ArrayList<EmgPeak> emgPeakList = new ArrayList<>();
    private int current_emg_peak_index = 0, max_emg_peak_index = 0;
    private int live_sceduled_size = 0;
    private int phizio_packagetype = 0;
    //session inserted on server
    private boolean sessionCompleted = false, can_beeep_max = true,can_beep_min = true;
    MqttSyncRepository repository;
    private String str_body_orientation="",json_phizioemail = "", patientid = "", bodyorientation = "", patientname = "";
    TextView tv_max_angle, tv_min_angle, tv_max_emg, Repetitions, holdTime, btn_emg_decrease_gain, btn_emg_increase_gain,
            tv_session_no, tv_body_part, monitor_muscle_name,tv_repsselected, tv_repsselected_slash,EMG, time, patientId, patientName, tv_action_time,tv_recording;
    private int ui_rate = 0, gain_initial = 20, body_orientation = 0, angleCorrection = 0,
            currentAngle = 0, Seconds, Minutes, maxAngle, minAngle, maxEmgValue, orientation_position=0;
    private int hold_angle_session=0,hold_time_minutes_session=0, hold_time_seconds_session=0;
    String holdTime_final="00m:00s";
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
    ArcViewInside arcViewInside;
    ImageView iv_angle_correction,iv_recording_icon;
    LineData lineData, lineDataNew;
    Button timer, stopBtn, cancelBtn;
    long MillisecondTime, StartTime, TimeBuff, UpdateTime = 0L;
    Handler handler;
    LinearLayout emgSignal;
    Date rawdata_timestamp;
    Long tsLong = 0L;
    AngleOperations angleOperations;
    private boolean mSessionStarted = false;



    private int prev_rep=0,current_rep=0,last_min_angle=360,last_max_angle=0;
    private int prev_angle=0;
    private boolean first_read=true;


    AlertDialog  error_device_dialog;
    Dialog deviceDisconnectedDialog,usbPluggedInDialog;
    ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);

    File file_session_emgdata, file_dir_session_emgdata, file_session_romdata, file_session_sessiondetails;
    FileOutputStream outputStream_session_emgdata, outputStream_session_romdata, outputStream_session_sessiondetails;
    private boolean can_talk = false;


    /**
     * Date of commented : 27-10-2022
     * Commented By : Burra Kranthi Kiran
     * Description : This is device disconnection pop this work on the two conditions
     * 1. Device is out of range from the mobile
     * 2. Device not connected with fly values
     */

    public void deviceDisconnectedPopup(boolean operation) {

        String title = "Device Disconnected";
        String message;
        //
        // Custom notification added by Haaris
        // custom dialog

        if(operation){
            message = "Please come in range to the device to continue the session";

            deviceDisconnectedDialog = new Dialog(getActivity());
            deviceDisconnectedDialog.setContentView(R.layout.notification_dialog_box);

            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(deviceDisconnectedDialog.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

            deviceDisconnectedDialog.getWindow().setAttributes(lp);

            TextView notification_title = deviceDisconnectedDialog.findViewById(R.id.notification_box_title);
            TextView notification_message = deviceDisconnectedDialog.findViewById(R.id.notification_box_message);

            Button Notification_Button_ok = (Button) deviceDisconnectedDialog.findViewById(R.id.notification_ButtonOK);
            Button Notification_Button_cancel = (Button) deviceDisconnectedDialog.findViewById(R.id.notification_ButtonCancel);

            Notification_Button_ok.setText("Okay");
            Notification_Button_cancel.setText("End Session");

            // Setting up the notification dialog
            notification_title.setText(title);
            notification_message.setText(message);

            // On click on Continue
            Notification_Button_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    deviceDisconnectedDialog.dismiss();

                }
            });



            // On click on Cancel
            Notification_Button_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stopBtn.performClick();
                    deviceDisconnectedDialog.dismiss();
                }
            });
            deviceDisconnectedDialog.show();
//            onDestroy();
        }else {
            message = "Please come in range to start session";
            deviceDisconnectedDialog = new Dialog(getActivity());
            deviceDisconnectedDialog.setContentView(R.layout.notification_dialog_box_single_button);
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(deviceDisconnectedDialog.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

            deviceDisconnectedDialog.getWindow().setAttributes(lp);

            TextView notification_title = deviceDisconnectedDialog.findViewById(R.id.notification_box_title);
            TextView notification_message = deviceDisconnectedDialog.findViewById(R.id.notification_box_message);

            Button Notification_Button_ok = (Button) deviceDisconnectedDialog.findViewById(R.id.notification_ButtonOK);

            Notification_Button_ok.setText("Okay");

            // Setting up the notification dialog
            notification_title.setText(title);
            notification_message.setText(message);

            // On click on Continue
            Notification_Button_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    deviceDisconnectedDialog.dismiss();


                }
            });


            deviceDisconnectedDialog.show();

            // End

        }


    }

    /**
     * Date of commented : 27-10-2022
     * Commented By : Burra Kranthi Kiran
     * Description : This is usb connected to hardware device pop when usb connect to device
     */

    public void usbConnectedDialog(boolean operation) {


        String title = "Usb Connected";
        String message;
        //
        // Custom notification added by Haaris
        // custom dialog

        if(operation){
            message = "Please disconnect USB to continue the session";

            usbPluggedInDialog = new Dialog(getActivity());
            usbPluggedInDialog.setContentView(R.layout.notification_dialog_box);

            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(usbPluggedInDialog.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

            usbPluggedInDialog.getWindow().setAttributes(lp);

            TextView notification_title = usbPluggedInDialog.findViewById(R.id.notification_box_title);
            TextView notification_message = usbPluggedInDialog.findViewById(R.id.notification_box_message);

            Button Notification_Button_ok = (Button) usbPluggedInDialog.findViewById(R.id.notification_ButtonOK);
            Button Notification_Button_cancel = (Button) usbPluggedInDialog.findViewById(R.id.notification_ButtonCancel);

            Notification_Button_ok.setText("Okay");
            Notification_Button_cancel.setText("End Session");

            // Setting up the notification dialog
            notification_title.setText(title);
            notification_message.setText(message);

            // On click on Continue
            Notification_Button_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    usbPluggedInDialog.dismiss();


                }
            });

            // On click on Cancel
            Notification_Button_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stopBtn.performClick();
                    usbPluggedInDialog.dismiss();


                }
            });
            usbPluggedInDialog.show();

        }else {
            message = "Please disconnect USB to start session";
            usbPluggedInDialog = new Dialog(getActivity());
            usbPluggedInDialog.setContentView(R.layout.notification_dialog_box_single_button);

            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(usbPluggedInDialog.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

            usbPluggedInDialog.getWindow().setAttributes(lp);

            TextView notification_title = usbPluggedInDialog.findViewById(R.id.notification_box_title);
            TextView notification_message = usbPluggedInDialog.findViewById(R.id.notification_box_message);

            Button Notification_Button_ok = (Button) usbPluggedInDialog.findViewById(R.id.notification_ButtonOK);

            Notification_Button_ok.setText("Okay");

            // Setting up the notification dialog
            notification_title.setText(title);
            notification_message.setText(message);

            // On click on Continue
            Notification_Button_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    usbPluggedInDialog.dismiss();


                }
            });


            usbPluggedInDialog.show();

            // End

        }

    }

    /**
     * Date of commented : 27-10-2022
     * Commented By : Burra Kranthi Kiran
     * Description: The below code onCreate is function this will default function to run the android when the MonitorActivity
     * When this function started it will be bring the layout("UI design") and start function in background
     * The following function run in the background
     * Inputs: layout, layout id, Screen width size, sharedprefrance Data, Graph View and parameter(Device pop Images)
     * Output: pop Images, EMG plotting point, ROM plotting point, ArcView Dailer Values and Graph Values
     * Functions used here: Check the EMG Values, ROM Values, Angle correction function, start, stop, cancel, inactive button, Device Information check function and device parameter function
     * Other functions using this: BLE Function, Server Communication Function and permission Function
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = null;
        Configuration config = getResources().getConfiguration();
        if(config.smallestScreenWidthDp >= 600){
            root = inflater.inflate(R.layout.fragment_standard_gold_teach, container, false);
        }else {
            root = inflater.inflate(R.layout.fragment_standard_gold_teach, container, false);
        }

        angleOperations = new AngleOperations();
        lineChart = root.findViewById(R.id.chart);
        EMG = root.findViewById(R.id.emgValue);
        arcViewInside = root.findViewById(R.id.arcViewInside);

        Repetitions = root.findViewById(R.id.Repetitions);
        holdTime = root.findViewById(R.id.holdtime);
        timer = root.findViewById(R.id.timer);
        stopBtn = root.findViewById(R.id.stopBtn);
        patientId = root.findViewById(R.id.patientId);
        patientName = root.findViewById(R.id.patientName);
        time = root.findViewById(R.id.displayTime);
        emgSignal = root.findViewById(R.id.emg);
        tv_session_no = root.findViewById(R.id.tv_session_no);
        tv_body_part = root.findViewById(R.id.bodyPart);
        monitor_muscle_name = root.findViewById(R.id.monitor_muscle_name);
        cancelBtn = root.findViewById(R.id.cancel);
        iv_angle_correction = root.findViewById(R.id.tv_angleCorrection);
        iv_recording_icon = root.findViewById(R.id.recording_icon);
        tv_action_time = root.findViewById(R.id.tv_action_time);
        tv_max_angle = root.findViewById(R.id.tv_max_angle);
        tv_min_angle = root.findViewById(R.id.tv_min_angle);
        tv_max_emg = root.findViewById(R.id.tv_max_emg_show);
        tv_repsselected = root.findViewById(R.id.repsSelected);
        tv_recording = root.findViewById(R.id.recording_text);
        tv_repsselected_slash = root.findViewById(R.id.tv_repsselected_slash);
        btn_emg_decrease_gain = root.findViewById(R.id.btn_emg_decrease_gain);
        btn_emg_increase_gain = root.findViewById(R.id.btn_emg_increase_gain);
        handler = new Handler(Looper.getMainLooper());
        emgJsonArray = new JSONArray();
        romJsonArray = new JSONArray();
        repository = new MqttSyncRepository(getActivity().getApplication());
        repository.setOnSessionNumberResponse(this);
        /**
         * Date of commented : 27-10-2022
         * Commented By : Burra Kranthi Kiran
         * Description: this below function for Data Sharepreferance from previous activity
         */


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



        /**
         * Date of commented : 27-10-2022
         * Commented By : Burra Kranthi Kiran
         * Description: this below function for the filter Pheezee Hardware Values
         */


        arcViewInside.setMinAngle(0);
        arcViewInside.setMaxAngle(0);
        setListnersOnViews();

        MillisecondTime = 0L;
        StartTime = 0L;
        TimeBuff = 0L;
        UpdateTime = 0L;
        Seconds = 0;
        Minutes = 0;
        time.setText("00m:00s");

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(device_state);
        intentFilter.addAction(bluetooth_state);
        intentFilter.addAction(usb_state);
        intentFilter.addAction(battery_percent);
        intentFilter.addAction(session_data);
        intentFilter.addAction(device_disconnected_firmware);
        getActivity().registerReceiver(session_data_receiver,intentFilter);
        Log.e("device_info", String.valueOf(intentFilter));
        Log.e("device_info",device_state);
        Log.e("device_info",bluetooth_state);
        Log.e("device_info",battery_percent);
        Log.e("device_info",session_data);

        creatGraphView();

        ((MonitorActivity)getActivity()).getBasicDeviceInfo();

        /**
         * Date of commented : 27-10-2022
         * Commented By : Burra Kranthi Kiran
         * Description: this below function for the based on movement and bodypart we are getting device placement image.
         */
        // custom dialog
        final Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.custom_dialog_box);

        ImageView device_placement = dialog.findViewById(R.id.image_device_placement);
        String str_side_orientation = getActivity().getIntent().getStringExtra("orientation");
        String str_body_part = getActivity().getIntent().getStringExtra("exerciseType");
        str_exercise_name = getActivity().getIntent().getStringExtra("exercisename");
        str_muscle_name = getActivity().getIntent().getStringExtra("musclename");
        str_max_angle_selected = getActivity().getIntent().getStringExtra("maxangleselected");
        str_min_angle_selected = getActivity().getIntent().getStringExtra("minangleselected");

        int color = ValueBasedColorOperations.getBodyPartMaxValue(str_body_part,str_exercise_name);
        arcViewInside.setMaxAngle(0);
        arcViewInside.setMinAngle(0);





        if(!str_min_angle_selected.equals("") && !str_max_angle_selected.equals("")){
            int reference_min_angle = Integer.parseInt(str_min_angle_selected);
            int reference_max_angle = Integer.parseInt(str_max_angle_selected);
            arcViewInside.setEnableAndMinMax(reference_min_angle,reference_max_angle,true);
        }


        String test = str_side_orientation+str_body_part+str_exercise_name+str_muscle_name;
        test = "ic_dk_"+test;
        test = test.replace(" - ","_");
        test = test.replace(" ","_");
        test = test.replace(")","");
        test = test.replace("(","");
        test = test.toLowerCase();




        Log.d("newplacementtest",test);


        if(str_exercise_name.toLowerCase().contains("rotation") && str_body_part.toLowerCase().contains("shoulder"))
        {
            test = test+"_rotation";
//            res = getResources().getIdentifier(test, "drawable",getActivity().getPackageName());
//            device_placement.setImageResource(res);
        }

        int res = getResources().getIdentifier(test, "drawable",getActivity().getPackageName());

        if(res !=0) {


            device_placement.setImageResource(res);

            TextView muscle_name = dialog.findViewById(R.id.muscle_title);
            TextView exercise_name = dialog.findViewById(R.id.device_placement_title);

            muscle_name.setText(getActivity().getIntent().getStringExtra("musclename"));
            exercise_name.setText(getActivity().getIntent().getStringExtra("orientation") +" "+getActivity().getIntent().getStringExtra("exerciseType") +" - "+  getActivity().getIntent().getStringExtra("exercisename"));
            // if button is clicked, close the custom dialog

            Button Notification_Button_ok = (Button) dialog.findViewById(R.id.notification_ButtonOK);

            Notification_Button_ok.setText("Next");

            // On click on Continue
            Notification_Button_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(deviceState && !usbState)
                    {
                        // Do nothing
                    }
                    else {
                        if(!deviceState)
                            deviceDisconnectedPopup(false);
                        else
                            usbConnectedDialog(false);
                    }
                    dialog.dismiss();


                }
            });

            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            if(show_popup_once==true)
            {
                show_popup_once = false;
                dialog.show();
            }

        }else
        {
            // Do not show anything.
        }




        return root;
    }


    /**
     * Date of commented : 27-10-2022
     * Commented By : Burra Kranthi Kiran
     * Description: this below function for EMG Zoom in  and Zoom Out
     */
    private void setListnersOnViews() {

        btn_emg_increase_gain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSessionStarted) {
                    btn_emg_decrease_gain.setBackgroundResource(R.drawable.incr_decr_layout);
                    if (gain_initial < 120) {
                        gain_initial += 10;
                        lineChart.zoom(1.4f, 1.4f, ui_rate, ui_rate);
                        if (gain_initial == 120) {
                            btn_emg_increase_gain.setBackgroundResource(R.drawable.ic_emg_button_increase);

                        }
                    }
                    ((MonitorActivity)getActivity()).increaseGain();
                } else {
                    showToast("Please start the session!");
                }
            }
        });
        btn_emg_decrease_gain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSessionStarted) {
                    btn_emg_increase_gain.setBackgroundResource(R.drawable.incr_decr_layout);
                    if (gain_initial > 10) {
                        gain_initial -= 10;
                        if (gain_initial == 10) {
                            btn_emg_decrease_gain.setBackgroundResource(R.drawable.ic_emg_button_decrease);
                        }
                        lineChart.zoomOut();
                    }
                    ((MonitorActivity)getActivity()).decreaseGain();
                } else {
                    showToast("Please start the session!");
                }
            }
        });

        /**
         * Date of commented : 27-10-2022
         * Commented By : Burra Kranthi Kiran
         * Description: this below function for the Timer action
         */

        timer.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                cancelBtn.setBackgroundResource(R.drawable.round_cancel_buttons_red);
                String message = BatteryOperation.getDialogMessageForLowBattery(PatientsView.deviceBatteryPercent, getActivity());
                if (!message.equalsIgnoreCase("c")) {
                    //
                    // Custom notification added by Haaris
                    // custom dialog


                    final Dialog dialog = new Dialog(getActivity());
                    dialog.setContentView(R.layout.notification_dialog_box_single_button);
                    dialog.setCancelable(false);

                    WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                    lp.copyFrom(dialog.getWindow().getAttributes());
                    lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                    lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

                    dialog.getWindow().setAttributes(lp);

                    TextView notification_title = dialog.findViewById(R.id.notification_box_title);
                    TextView notification_message = dialog.findViewById(R.id.notification_box_message);

                    Button Notification_Button_ok = (Button) dialog.findViewById(R.id.notification_ButtonOK);

                    Notification_Button_ok.setText("Okay");

                    // Setting up the notification dialog
                    notification_title.setText("Battery Low Alert");
                    notification_message.setText(message);

                    // On click on Continue
                    Notification_Button_ok.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if(deviceState && !usbState)
                                startSession();
                            else {
                                if(!deviceState)
                                    deviceDisconnectedPopup(false);
                                else
                                    usbConnectedDialog(false);
                            }
                            dialog.dismiss();


                        }
                    });


                    dialog.show();

                    // End
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
         * Date of commented : 27-10-2022
         * Commented By : Burra Kranthi Kiran
         * Description: this below function for cancel the session
         */
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (timer.getVisibility() == View.GONE) {
                    sessionCompleted = true;
                    mSessionStarted = false;
                    tv_max_angle.setText(String.valueOf(0)+"°");
                    tv_min_angle.setText(String.valueOf(0)+"°");
                    time.setText("00m:00s");
                    holdTime.setText("00m:00s");
                    tv_max_emg.setText(String.valueOf(0)+"μV");
                    EMG.setText(String.valueOf(0)+"μV");
                    arcViewInside.setMinAngle(0);
                    arcViewInside.setMaxAngle(0);
                    creatGraphView();
                    LinearLayout.LayoutParams params;
                    params = (LinearLayout.LayoutParams) emgSignal.getLayoutParams();
                    params.height = (int) (20);
                    emgSignal.setLayoutParams(params);
                    tv_recording.setText("");
                    tv_recording.clearAnimation();
                    iv_recording_icon.setImageDrawable(getResources().getDrawable(R.drawable.bg_circle_red));
                    timer.setBackgroundResource(R.drawable.rounded_start_button);
                    cancelBtn.setBackgroundResource(R.drawable.round_cancel_buttons_green);
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
                PatientActivitySingleton.getInstance().PopElement();
            }
        });

        /**
         * Date of commented : 27-10-2022
         * Commented By : Burra Kranthi Kiran
         * Description: this below function for the Stop session
         */
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sessionCompleted = true;
                mSessionStarted = false;
                tv_recording.setText("");
                tv_recording.clearAnimation();
                iv_recording_icon.setImageDrawable(getResources().getDrawable(R.drawable.bg_circle_red));
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
                if(minAngle==360)
                {
                    minAngle=0;
                }
                timer.setText(R.string.timer_start);
                tsLong = System.currentTimeMillis();
                if(IS_SCEDULED_SESSION){
                    ((MonitorActivity)getActivity()).removeFirstFromSceduledList();
                    live_sceduled_size = ((MonitorActivity)getActivity()).getSceduledSize();
                    if(live_sceduled_size ==0){
                        IS_SCEDULED_SESSIONS_COMPLETED = true;
                    }
                }
                if(phizio_packagetype!=STANDARD_PACKAGE)
                    initiatePopupWindowModified();
                else
                    initiatePopupWindowStandard();

                if(phizio_packagetype==TEACH_PACKAGE||phizio_packagetype==ACHEDAMIC_TEACH_PLUS)
                    insertValuesAndNotifyMediaStore("Stopped");

                if(IS_SCEDULED_SESSION){
                    live_sceduled_size = ((MonitorActivity)getActivity()).getSceduledSize();
                    if(live_sceduled_size !=0){
                        updateInitialValues(((MonitorActivity)getActivity()).getSceduledSessionListFirstItem());
                    }else {
                        IS_SCEDULED_SESSIONS_COMPLETED = true;
                        repository.removeAllSessionsForPataient(patientid);
                    }
                }

                if (current_rep < repsselected && repsselected != 0 ) {
                    openUnsuccessfullDialog();
                }


            }
        });

        /**
         * Date of commented : 27-10-2022
         * Commented By : Burra Kranthi Kiran
         * Description: this below function for the Angle Correction.
         */
        iv_angle_correction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                final Dialog dialog = new Dialog(getActivity());
                dialog.setContentView(R.layout.angle_correction_dialog_box);



                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(dialog.getWindow().getAttributes());
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

                TextView notification_title = dialog.findViewById(R.id.notification_box_title);
                TextView notification_message = dialog.findViewById(R.id.notification_box_message);
                final EditText editText = dialog.findViewById(R.id.et_corrected_angle);
                editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);

                Button Notification_Button_ok = (Button) dialog.findViewById(R.id.notification_ButtonOK);
                Button Notification_Button_cancel = (Button) dialog.findViewById(R.id.notification_ButtonCancel);

                Notification_Button_ok.setText("Set");
                Notification_Button_cancel.setText("Cancel");

                editText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        editText.setAlpha(1);
                        editText.setText("");
                    }
                });


                // On click on Continue
                Notification_Button_ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!editText.getText().toString().equals("")) {
                            try {
                                if (mSessionStarted) {
                                    angleCorrection = Integer.parseInt(editText.getText().toString());
                                    angleCorrected = true;
                                    maxAngle = angleCorrection;
                                    minAngle = angleCorrection;
                                    angleCorrection -= currentAngle;
                                    currentAngle += angleCorrection;

                                    startSession_angleCorrected();


                                }
                            } catch (NumberFormatException e) {

                            }
                        }
                        dialog.dismiss();

                    }
                });
                // On click Cancel
                Notification_Button_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();

                    }
                });



                if (mSessionStarted) {
                    dialog.show();
                    dialog.getWindow().setAttributes(lp);
                } else {
                    showToast("Please start the session!");
                }

//                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//                builder.setTitle("Correct Angle");
//                builder.setMessage("please enter the expected angle");
//                final EditText editText = new EditText(getActivity());
//                editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
//                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
//                        LinearLayout.LayoutParams.MATCH_PARENT,
//                        LinearLayout.LayoutParams.MATCH_PARENT);
//                editText.setLayoutParams(lp);
//                builder.setView(editText);
//                builder.setPositiveButton("Set", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        if (!editText.getText().toString().equals("")) {
//                            try {
//                                if (mSessionStarted) {
//                                    angleCorrection = Integer.parseInt(editText.getText().toString());
//                                    angleCorrected = true;
//                                    maxAngle = angleCorrection;
//                                    minAngle = angleCorrection;
//                                    angleCorrection -= currentAngle;
//                                    currentAngle += angleCorrection;
//
//
//                                }
//                            } catch (NumberFormatException e) {
//
//                            }
//                        }
//                    }
//                });
//
//                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                    }
//                });
//                if (mSessionStarted) {
//                    builder.show();
//                } else {
//                    showToast("Please start session!");
//                }

                // END

            }
        });
    }

    /**
     * Date of commented : 27-10-2022
     * Commented By : Burra Kranthi Kiran
     * Description: this below function for the Update Values in above function call
     */

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
        tv_body_part.setText(orientation + " " + bodypart + " " + str_exercise_name);
        monitor_muscle_name.setText(str_muscle_name);
        if (repsselected!= 0) {
            tv_repsselected.setText(String.valueOf(repsselected));
        } else {
            tv_repsselected.setVisibility(View.GONE);
            tv_repsselected_slash.setVisibility(View.GONE);
            Repetitions.setGravity(Gravity.CENTER);
        }

        if(phizio_packagetype==STANDARD_PACKAGE || phizio_packagetype==GOLD_PACKAGE || phizio_packagetype==TEACH_PACKAGE){
            can_beep = true;
        }else {
            if((Integer.parseInt(str_min_angle_selected)!=ValueBasedColorOperations.getBodyPartMinValue(bodypart,exercise_position))
                    ||(Integer.parseInt(str_max_angle_selected)!=ValueBasedColorOperations.getBodyPartMaxValue(bodypart,exercise_position))){
                can_beep = true;
                can_voice = false;
            }else {
                can_beep = false;
                can_voice = true;
            }
        }
    }
    /**
     * Date of commented : 27-10-2022
     * Commented By : Burra Kranthi Kiran
     * Description: this below function for the Update Values parameter in above function call
     */

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
        exercise_position = MuscleOperation.getExercisePosition(str_exercise_name,bodypart);
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
        /**
         * Date of commented : 27-10-2022
         * Commented By : Burra Kranthi Kiran
         * Description: this below function for setting patient id and name
         */
        if(patientid.length()>3){
            String temp = patientid.substring(0,3)+"xxx";
            patientId.setText(temp);
        }else {
            patientId.setText(patientid);
        }

        patientName.setText(patientname);
        /**
         * Date of commented : 27-10-2022
         * Commented By : Burra Kranthi Kiran
         * Description: this below function for setting session number
         */
        if(phizio_packagetype!=STANDARD_PACKAGE)
            repository.getPatientSessionNo(patientid);

        tv_body_part.setText(session.getSessionno()+"/"+total_sceduled_size+":-"+orientation + "-" + bodypart + "-" + str_exercise_name);
        if (repsselected!= 0) {
            tv_repsselected.setText("/".concat(String.valueOf(repsselected)));
        } else {
            tv_repsselected.setVisibility(View.GONE);
        }
        if(phizio_packagetype==STANDARD_PACKAGE || phizio_packagetype==GOLD_PACKAGE || phizio_packagetype==TEACH_PACKAGE){
            can_beep = true;
        }else {
            if((Integer.parseInt(str_min_angle_selected)!=ValueBasedColorOperations.getBodyPartMinValue(bodypart,exercise_position))
                    ||(Integer.parseInt(str_max_angle_selected)!=ValueBasedColorOperations.getBodyPartMaxValue(bodypart,exercise_position))){
                can_beep = true;
                can_voice = false;
            }else {
                can_beep = false;
                can_voice = true;
            }
        }
    }
    /**
     * Date of commented : 27-10-2022
     * Commented By : Burra Kranthi Kiran
     * Description: this below function for Inserts the summary values in files and also tells the media to scan the files for visibility when connected to the laptop.
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
                    outputStream_session_sessiondetails.write("Num of Reps:".concat(Repetitions.getText().toString()).getBytes());
                    outputStream_session_sessiondetails.write("\n".getBytes());
                    outputStream_session_sessiondetails.write("Session Time:".concat(time.getText().toString()).getBytes());
                    outputStream_session_sessiondetails.write("\n".getBytes());
                    outputStream_session_sessiondetails.write("Active Time:".concat(tv_action_time.getText().toString()).getBytes());
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
    /**
     * Date of commented : 27-10-2022
     * Commented By : Burra Kranthi Kiran
     * Description: this below function for Updates the view of gain to default.
     * Currently we are not using the this.
     */
    private void updateGainView() {
//        btn_emg_decrease_gain.setBackgroundResource(R.drawable.round_corner_layout);
//        btn_emg_increase_gain.setBackgroundResource(R.drawable.round_corner_layout);
    }

    /**
     * Date of commented : 27-10-2022
     * Commented By : Burra Kranthi Kiran
     * Description: this below function for Start Session.
     * Currently we are not using the this.
     */
    public void startSession() {
        current_emg_peak_index=0;max_emg_peak_index=0;
        emgPeakList = new ArrayList<>();
        updateGainView();
        error_device_dialog=null;
        mSessionStarted = true;
        sessionCompleted = false;
        ui_rate = 0;
        angleCorrected = false;
        angleCorrection = 0;
        emgJsonArray = new JSONArray();
        romJsonArray = new JSONArray();
        maxAngle = 0;minAngle = 360;maxEmgValue = 0;
        hold_angle_session = 0;
        hold_time_seconds_session = 0;
        hold_time_minutes_session = 0;
        holdTime_final="00m:00s";
        tv_recording.setText("Recording");
        Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(500); //You can manage the blinking time with this parameter
        anim.setStartOffset(20);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        tv_recording.startAnimation(anim);

        can_beeep_max=true;can_beep_min=true;
        creatGraphView();
        timer.setVisibility(View.GONE);
        cancelBtn.setVisibility(View.VISIBLE);
        stopBtn.setVisibility(View.VISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
//                mService.sendBodypartDataToDevice(bodypart, body_orientation, patientname, exercise_position,
//                        muscle_position, bodypart_position, orientation_position);

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


        // Checking for activity
        PatientActivitySingleton activity = PatientActivitySingleton.getInstance();
        JSONObject jsonObject = new JSONObject();
        JSONArray array = new JSONArray();
        //for held on date
        Calendar calendar = Calendar.getInstance();
        Date date_cal = calendar.getTime();
        java.text.DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        String heldon = dateFormat.format(date_cal);
        try {
            jsonObject.put("engagement", 1);
            jsonObject.put("timestamp", heldon);

        }catch (Throwable e)
        {
        }
        array.put(jsonObject);
        activity.setPatientDetails(patientid,patientname,json_phizioemail,array);



    }

    /**
     * Date of commented : 27-10-2022
     * Commented By : Burra Kranthi Kiran
     * Description: this below function for Start Session after Angle Correction.
     * Currently we are not using the this.
     */

    public void startSession_angleCorrected() {
        current_emg_peak_index=0;max_emg_peak_index=0;
        emgPeakList = new ArrayList<>();
        updateGainView();
        error_device_dialog=null;
        mSessionStarted = true;
        sessionCompleted = false;
        ui_rate = 0;

        emgJsonArray = new JSONArray();
        romJsonArray = new JSONArray();
        maxEmgValue = 0;
        hold_angle_session = 0;
        hold_time_seconds_session = 0;
        hold_time_minutes_session = 0;
        holdTime_final="00m:00s";
        tv_recording.setText("Recording");
        Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(500); //You can manage the blinking time with this parameter
        anim.setStartOffset(20);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        tv_recording.startAnimation(anim);

        can_beeep_max=true;can_beep_min=true;
        creatGraphView();
        timer.setVisibility(View.GONE);
        cancelBtn.setVisibility(View.VISIBLE);
        stopBtn.setVisibility(View.VISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
//                mService.sendBodypartDataToDevice(bodypart, body_orientation, patientname, exercise_position,
//                        muscle_position, bodypart_position, orientation_position);

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
    /**
     * Date of commented : 27-10-2022
     * Commented By : Burra Kranthi Kiran
     * Description: this below function for store raw file in local in files.
     * Currently we are not using the this.
     */

    private void initializeAndWriteInitialToFile() {
        android.text.format.DateFormat df = new android.text.format.DateFormat();
//            String s = rawdata_timestamp.toString().substring(0, 19);
        String s = String.valueOf(DateFormat.format("yyyy-MM-dd hh-mm-ssa", rawdata_timestamp));
        String child = patientname + patientid;
        file_dir_session_emgdata = new File(getApplicationContext().getExternalFilesDir(null) + "/Pheezee/files/EmgData/" + child + "/sessiondata/", s);
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
     * Date of commented : 27-10-2022
     * Commented By : Burra Kranthi Kiran
     * Description: this below function for handler for session time incrimental.
     * Currently we are not using the this.
     */
    public Runnable runnable = new Runnable() {
        public void run() {
            MillisecondTime = SystemClock.uptimeMillis() - StartTime;
            UpdateTime = TimeBuff + MillisecondTime;
            Seconds = (int) (UpdateTime / 1000);
            Minutes = Seconds / 60;
            Seconds = Seconds % 60;
            timeText = "Session time:   " + String.format("%02d", Minutes) + " : " + String.format("%02d", Seconds);
            time.setText(String.format("%02d", Minutes) +"m"+ ":" + String.format("%02d", Seconds)+"s");

            if(Seconds>30 && Minutes < 1)
            {
                iv_recording_icon.setImageDrawable(getResources().getDrawable(R.drawable.bg_square_red));
                tv_recording.setText("30s");
                tv_recording.clearAnimation();

            }

            if(phizio_packagetype==GOLD_PLUS_PACKAGE || phizio_packagetype==ACHEDAMIC_TEACH_PLUS) {
                if (Seconds == 59 && can_voice) {
                    if(((MonitorActivity)getActivity())!=null)
                        ((MonitorActivity) getActivity()).textToSpeachVoice("Good! Keep Going!");

                }
            }

            handler.postDelayed(this, 0);
        }
    };
    /**
     * Date of commented : 27-10-2022
     * Commented By : Burra Kranthi Kiran
     * Description: this below function for Handler to post the values received from device in the view
     * Currently we are not using the this.
     */

    @SuppressLint("HandlerLeak")
    public final Handler myHandler = new Handler() {
        public void handleMessage(Message message) {
            try {
                if (mSessionStarted) {

//                    ToneGenerator toneGen2 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                    int angleDetected = 0, num_of_reps = 0, hold_time_minutes, hold_time_seconds, active_time_minutes, active_time_seconds,hold_angle =0;

                    int emg_data, error_device = 0;
                    byte[] sub_byte;
                    sub_byte = (byte[]) message.obj;
                    if (sub_byte != null) {
                        error_device = sub_byte[10] & 0xFF;
                        if (error_device == 0) {
                            emg_data = ByteToArrayOperations.getAngleFromData(sub_byte[0], sub_byte[1]);
                            if(phizio_packagetype==GOLD_PLUS_PACKAGE||phizio_packagetype==ACHEDAMIC_TEACH_PLUS)
                                emgPeakDetectionAndVoiceAleart(emg_data);
                            angleDetected = ByteToArrayOperations.getAngleFromData(sub_byte[2], sub_byte[3]);
                            currentAngle = angleDetected;
                            if(str_exercise_name.equals("Isometric")){
                                angleDetected = 0 * angleDetected;
                                currentAngle = angleDetected;
                                arcViewInside.setRadius(0);
                            }
                            if (angleCorrected) {
                                angleDetected += angleCorrection;
                                arcViewInside.setMaxAngle(angleDetected);
                            } else {
                                arcViewInside.setMaxAngle(angleDetected);
                            }
                            if (ui_rate == 0) {
                                minAngle = angleDetected;
                                maxAngle = angleDetected;
                            }
                            num_of_reps = ByteToArrayOperations.getNumberOfReps(sub_byte[4], sub_byte[5]);
                            if(sub_byte.length > 11) {
                                hold_angle = ByteToArrayOperations.getAngleFromData(sub_byte[11], sub_byte[12]);
                            }



                            hold_time_minutes = sub_byte[6];
                            hold_time_seconds = sub_byte[7];
                            active_time_minutes = sub_byte[8];
                            active_time_seconds = sub_byte[9];

                            // Updating the hold angle and hold time to latest values

                            if(hold_time_minutes == 0 && hold_time_seconds ==0)
                            {
                                // Do nothing
                            }else
                            {
                                if(hold_time_seconds>=hold_time_seconds_session && hold_time_minutes>=hold_time_minutes_session) {
                                    hold_time_seconds_session = hold_time_seconds;
                                    hold_time_minutes_session = hold_time_minutes;

                                    String minutesValue = "" + hold_time_minutes, secondsValue = "" + hold_time_seconds;
                                    if (hold_time_minutes < 10)
                                        minutesValue = "0" + hold_time_minutes;
                                    if (hold_time_seconds < 10)
                                        secondsValue = "0" + hold_time_seconds;
                                    holdTime_final = minutesValue + "m: " + secondsValue + "s";


                                    if(hold_angle!=0 && hold_angle!=360){

                                        hold_angle_session = hold_angle;
                                    }
                                }
                            }



                            String repetitionValue = "" + num_of_reps;


                            Repetitions.setText(repetitionValue);

                            if(num_of_reps >= repsselected && repsselected != 0) Repetitions.setText(Integer.toString(repsselected));

                            String minutesValue = "" + hold_time_minutes, secondsValue = "" + hold_time_seconds;
                            if (hold_time_minutes < 10)
                                minutesValue = "0" + hold_time_minutes;
                            if (hold_time_seconds < 10)
                                secondsValue = "0" + hold_time_seconds;
                            holdTimeValue = minutesValue + "m: " + secondsValue + "s";

                            romJsonArray.put(angleDetected);
                            if(phizio_packagetype==GOLD_PLUS_PACKAGE || phizio_packagetype==ACHEDAMIC_TEACH_PLUS) {
                                if (hold_time_seconds == 0 && hold_time_minutes == 0) {
                                    can_talk = true;
                                } else if (hold_time_minutes > 0 || hold_time_seconds > 5) {
                                    if (can_talk && can_voice) {
                                        ((MonitorActivity) getActivity()).textToSpeachVoice("Good! You are able to hold!");
                                        can_talk = false;
                                    }
                                }
                            }
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
                            params = (LinearLayout.LayoutParams) emgSignal.getLayoutParams();
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
                            tv_max_emg.setText(String.valueOf(maxEmgValue)+"μV");
                            params.height = (int) (((View) emgSignal.getParent()).getMeasuredHeight() * emg_data / maxEmgValue);
                            EMG.setText(Integer.toString(emg_data).concat(getResources().getString(R.string.emg_unit)));
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
                                if (angleDetected > x && can_beeep_max && can_beep) {
                                    new Handler().post(new Runnable() {
                                        @Override
                                        public void run() {
//                                            ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
//                                            toneGen1.startTone(ToneGenerator.TONE_PROP_BEEP,150);
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
                                if (angleDetected <= x && can_beep_min && can_beep) {
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
                            tv_max_angle.setText(String.valueOf(maxAngle)+"°");
                            minAngle = minAngle > angleDetected ? angleDetected : minAngle;
                            tv_min_angle.setText(String.valueOf(minAngle)+"°");
//            }
                            emgSignal.setLayoutParams(params);
                            holdTime.setText(holdTimeValue);
                            minutesValue = "" + active_time_minutes;
                            secondsValue = "" + active_time_seconds;
                            if (active_time_minutes < 10)
                                minutesValue = "0" + active_time_minutes;
                            if (active_time_seconds < 10)
                                secondsValue = "0" + active_time_seconds;
                            tv_action_time.setText(minutesValue + "m: " + secondsValue + "s");
                            current_rep = num_of_reps;

                            if(first_read)
                            {
                                first_read=false;
                                prev_angle = angleDetected;

                            }

                            if(angleDetected - prev_angle > 90)
                            {
                                Log.d("anglelarge",Integer.toString(angleDetected - prev_angle));
                                cancelBtn.performClick();
                                // custom dialog
                                final Dialog dialog = new Dialog(getActivity());
                                dialog.setContentView(R.layout.pheezee_placement_error_popup);

                                TextView Notification_Button_ok =  dialog.findViewById(R.id.repeat_exercise_btn);
                                // On click on Continue
                                Notification_Button_ok.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialog.dismiss();
                                    }
                                });

                                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                dialog.show();
                            }
                            // Target reps
                            prev_angle = angleDetected;

                            if(prev_rep != current_rep)
                            {
                                prev_rep = current_rep;
                                if(prev_rep<repsselected){
                                    last_min_angle=360;
                                    last_max_angle=0;
                                }
                            }
                            if(prev_rep == current_rep &&prev_rep!=repsselected)
                            {
                                last_min_angle = last_min_angle > angleDetected ? angleDetected : last_min_angle;

                            }
                            if(prev_rep == current_rep &&prev_rep!=repsselected)
                            {
                                last_max_angle= last_max_angle < angleDetected ? angleDetected : last_max_angle;
                            }

                            if (num_of_reps >= repsselected && repsselected != 0 && !sessionCompleted && ( last_min_angle+ (last_max_angle-last_min_angle)*0.3 +5)>angleDetected) {
                                sessionCompleted = true;
                                openSuccessfullDialogAndCloseSession();
                            }else if (num_of_reps > repsselected && repsselected != 0 && !sessionCompleted) {
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
     * Date of commented : 27-10-2022
     * Commented By : Burra Kranthi Kiran
     * Description: this below function for Close session in 2000ms once the session goal is reached
     */

    private void openSuccessfullDialogAndCloseSession() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                //
                // Custom notification added by Haaris
                // custom dialog


                final Dialog dialog = new Dialog(getActivity());
                dialog.setContentView(R.layout.notification_dialog_box_session_complete);
                dialog.setCancelable(false);

                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(dialog.getWindow().getAttributes());
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

                dialog.getWindow().setAttributes(lp);

                TextView notification_title = dialog.findViewById(R.id.notification_box_title);
                TextView notification_message = dialog.findViewById(R.id.notification_box_message);

                Button Notification_Button_ok = (Button) dialog.findViewById(R.id.notification_ButtonOK);

                Notification_Button_ok.setText("View Summary");

                // Setting up the notification dialog
                notification_title.setText("Congratulations");
                notification_message.setText("You have reached the goal");

                // On click on Continue
                Notification_Button_ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });


                dialog.show();

                // End

                stopBtn.performClick();

            }
        }, 200);
    }


    private void openUnsuccessfullDialog() {

        //
        // Custom notification added by Haaris
        // custom dialog


        final Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.notification_dialog_box_session_complete);
        dialog.setCancelable(false);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        dialog.getWindow().setAttributes(lp);

        TextView notification_title = dialog.findViewById(R.id.notification_box_title);
        TextView notification_message = dialog.findViewById(R.id.notification_box_message);
        ImageView smileyimage = dialog.findViewById(R.id.image_notification);

        int res = getResources().getIdentifier("ic_award_dissapointed", "drawable",getActivity().getPackageName());

        smileyimage.setImageResource(res);

        Button Notification_Button_ok = (Button) dialog.findViewById(R.id.notification_ButtonOK);

        Notification_Button_ok.setText("View Summary");

        // Setting up the notification dialog
        notification_title.setText("Try Again");
        notification_message.setText("You have not reached your goal today");

        // On click on Continue
        Notification_Button_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


        dialog.show();

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
        handler.removeCallbacks(runnable);
        try {
            requireActivity().unregisterReceiver(session_data_receiver);
        }catch (NullPointerException e){
            e.printStackTrace();
        }


    }
    /**
     * Date of commented : 27-10-2022
     * Commented By : Burra Kranthi Kiran
     * Description: this below function forRefreshes the line graph
     */


    private void creatGraphView() {
        Legend l = lineChart.getLegend();
        l.setEnabled(false);

        lineChart.setHardwareAccelerationEnabled(true);
        dataPoints = new ArrayList<>();
        dataPoints.add(new Entry(0, 0));
        lineDataSet = new LineDataSet(dataPoints, "Emg Graph");
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
        String sessiontime = timeText.substring(16);
        String actiontime = tv_action_time.getText().toString();

        //testing with empty emg and rom array
//        emgJsonArray = new JSONArray();
//        romJsonArray = new JSONArray();
        // Updating the activity list
        // Checking for activity
        PatientActivitySingleton activity = PatientActivitySingleton.getInstance();
        JSONObject jsonObject = new JSONObject();
        JSONArray array = new JSONArray();
        //for held on date
        Calendar calendar = Calendar.getInstance();
        Date date_cal = calendar.getTime();
        java.text.DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        String heldon = dateFormat.format(date_cal);
        try {
            jsonObject.put("engagement", 0);
            jsonObject.put("timestamp", heldon);

        }catch (Throwable e)
        {
        }
        array.put(jsonObject);
        activity.setPatientDetails(patientid,patientname,json_phizioemail,array);


        SessionSummaryPopupWindow window = new SessionSummaryPopupWindow(getActivity(), maxEmgValue, sessionNo, maxAngle, minAngle, orientation, bodypart,
                json_phizioemail, sessiontime, actiontime, holdTime_final, Repetitions.getText().toString(),
                angleCorrection, patientid, patientname, tsLong, bodyorientation, getActivity().getIntent().getStringExtra("dateofjoin"), exercise_position,bodypart_position,
                str_muscle_name,str_exercise_name,str_min_angle_selected,str_max_angle_selected,str_max_emg_selected,repsselected,emgJsonArray,romJsonArray,phizio_packagetype,hold_angle_session);
        window.showWindow();
//        window.storeLocalSessionDetails(emgJsonArray,romJsonArray);
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
        String sessiontime = time.getText().toString().substring(16);
        String actiontime = tv_action_time.getText().toString();

        //testing with empty emg and rom array
//        emgJsonArray = new JSONArray();
//        romJsonArray = new JSONArray();

        SessionSummaryStandardPopupWindow window = new SessionSummaryStandardPopupWindow(getActivity(), maxEmgValue, sessionNo, maxAngle, minAngle, orientation, bodypart,
                sessiontime, actiontime, holdTime.getText().toString(), Repetitions.getText().toString(),
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
                Log.e("Kranthi",device_state);

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
                    Log.e("Kranthi", String.valueOf(deviceState));
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
    /**
     * Date of commented : 27-10-2022
     * Commented By : Burra Kranthi Kiran
     * Description: this below function for EMG Peak Detection Calculation
     */
    public void emgPeakDetectionAndVoiceAleart(int emg){
        if(ui_rate==0){
            emgPeakList.add(new EmgPeak(emg,-1,-1,false));
        }else {
            if(current_emg_peak_index<3){
                if(!emgPeakList.get(current_emg_peak_index).isPeak_done()){
                    if((emg-emgPeakList.get(current_emg_peak_index).getInitValue())>=MIN_PEAK_DECIDED){
                        if(emgPeakList.get(current_emg_peak_index).getMax_emg_value()<emg)
                            emgPeakList.get(current_emg_peak_index).setMax_emg_value(emg);
                    }else if(emgPeakList.get(current_emg_peak_index).getMax_emg_value()!=-1){
                        if((emg-emgPeakList.get(current_emg_peak_index).getInitValue())<=10 || (emg-emgPeakList.get(current_emg_peak_index).getInitValue())>=10){
                            emgPeakList.get(current_emg_peak_index).setFinal_value(emg);
                            emgPeakList.get(current_emg_peak_index).setPeak_done(true);
                            current_emg_peak_index++;
                            emgPeakList.add(current_emg_peak_index,new EmgPeak(emgPeakList.get(0).getInitValue(),-1,-1,false));
                        }
                    }
                }
            }else if(current_emg_peak_index==3){
                if(emgPeakList.size()>3) {
                    int maxValue = 0;
                    for (int i = 0; i < 3; i++) {
                        if(emgPeakList.get(i).getMax_emg_value()>maxValue){
                            maxValue = emgPeakList.get(i).getMax_emg_value();
                            max_emg_peak_index = i;
                        }
                    }
                    peakSpeachComdition = (emgPeakList.get(max_emg_peak_index).getMax_emg_value()*PERCENTAGE_TEXT_TO_SPEACH_EMG_PEAK)/100;
                    if(!emgPeakList.get(current_emg_peak_index).isPeak_done()){
                        if((emg-emgPeakList.get(current_emg_peak_index).getInitValue())>=MIN_PEAK_DECIDED){
                            if(emgPeakList.get(current_emg_peak_index).getMax_emg_value()<emg)
                                emgPeakList.get(current_emg_peak_index).setMax_emg_value(emg);
                        }else if(emgPeakList.get(current_emg_peak_index).getMax_emg_value()!=-1){
                            if((emg-emgPeakList.get(current_emg_peak_index).getInitValue())<=10 || (emg-emgPeakList.get(current_emg_peak_index).getInitValue())>=10){
                                emgPeakList.get(current_emg_peak_index).setFinal_value(emg);
                                emgPeakList.get(current_emg_peak_index).setPeak_done(true);
                                if(emgPeakList.get(current_emg_peak_index).getMax_emg_value()<peakSpeachComdition && can_voice){
                                    ((MonitorActivity)getActivity()).textToSpeachVoice("You are trying hard! Keep trying!");
                                }
                                current_emg_peak_index++;
                                emgPeakList.add(current_emg_peak_index,new EmgPeak(emgPeakList.get(0).getInitValue(),-1,-1,false));
                            }
                        }
                    }
                }
            }else {
                if(!emgPeakList.get(current_emg_peak_index).isPeak_done()){
                    if((emg-emgPeakList.get(current_emg_peak_index).getInitValue())>=MIN_PEAK_DECIDED){
                        if(emgPeakList.get(current_emg_peak_index).getMax_emg_value()<emg)
                            emgPeakList.get(current_emg_peak_index).setMax_emg_value(emg);
                    }else if(emgPeakList.get(current_emg_peak_index).getMax_emg_value()!=-1){
                        if((emg-emgPeakList.get(current_emg_peak_index).getInitValue())<=10 || (emg-emgPeakList.get(current_emg_peak_index).getInitValue())>=10){
                            emgPeakList.get(current_emg_peak_index).setFinal_value(emg);
                            emgPeakList.get(current_emg_peak_index).setPeak_done(true);
                            if(emgPeakList.get(current_emg_peak_index).getMax_emg_value()<peakSpeachComdition && can_voice){
                                ((MonitorActivity)getActivity()).textToSpeachVoice("You are trying hard! Keep trying!");
                            }
                            current_emg_peak_index++;
                            emgPeakList.add(current_emg_peak_index,new EmgPeak(emgPeakList.get(0).getInitValue(),-1,-1,false));
                        }
                    }
                }
            }
        }
    }


}
