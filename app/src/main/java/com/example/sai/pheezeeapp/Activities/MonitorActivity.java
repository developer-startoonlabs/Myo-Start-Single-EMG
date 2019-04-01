package com.example.sai.pheezeeapp.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sai.pheezeeapp.Classes.BluetoothSingelton;
import com.example.sai.pheezeeapp.R;
import com.example.sai.pheezeeapp.services.MqttHelper;
import com.example.sai.pheezeeapp.utils.AngleOperations;
import com.example.sai.pheezeeapp.views.ArcView;
import com.example.sai.pheezeeapp.views.ArcViewInside;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.goodiebag.protractorview.ProtractorView;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;
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
import java.util.UUID;

public class MonitorActivity extends AppCompatActivity {

    public final int sub_byte_size = 26;
    int maxAnglePart, minAnglePart;
    String bodypart;
    boolean servicesDiscovered = false;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    JSONObject json_phizio = new JSONObject();

    boolean isSessionRunning=false, enteredInsideTwenty = true;
    JSONArray jsonData;
    JSONArray emgJsonArray;
    public final int emg_data_size_raw = 20;
    public final int emg_num_packets_raw = 40;


    public final int emg_data_size_session = 10;
    public final int emg_num_packets_session = 20;
    ImageView iv_back_monitor;

    File  file_emgdata,file_dir_emgdata;
    FileOutputStream  outputStream_emgdata;

    //MQTT
    MqttHelper mqttHelper;
    String mqtt_publish_add_patient_session = "phizio/addpatientsession";
    String mqtt_publish_add_patient_session_emg_data = "patient/entireEmgData";
    String mqtt_publish_add_patient_session_response = "phizio/addpatientsession/response";
    String mqtt_publish_add_patient_session_emg_data_response = "patient/entireEmgData/response";

    PopupWindow report;
    int visiblity=View.VISIBLE;
    long k;
    String timeText ="";
    List<Entry> dataPoints;
    LineChart lineChart;
    LineDataSet lineDataSet;
    int SessionTimeForGraph;
    View rootView;
    private static final String TAG = null;
    BluetoothGattCharacteristic mCharacteristic,mCharacteristic2;
    BluetoothGatt mBluetoothGatt;
    TextView Angle,tv_snap;
    TextView Repetitions;
    TextView holdTime;
    TextView EMG;
    ProtractorView rangeOfMotion;
    ArcViewInside arcViewInside;
    TextView time;
    TextView patientId;
    TextView patientName;
    LineData lineData;
    JSONArray sessionResult  = new JSONArray();
    JSONObject sessionObj = new JSONObject();
    boolean recieverState=false;
    boolean pheezeeState = false;
    Button timer;
    Button stopBtn;
    long MillisecondTime, StartTime, TimeBuff, UpdateTime = 0L;
    Handler handler;
    int Seconds, Minutes, MilliSeconds ;
    ProgressBar r;
    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice remoteDevice;
    int REQUEST_ENABLE_BT = 1;
    ConnectivityManager connectivityManager;
    LinearLayout emgSignal;
    String holdTimeValue;
    int maxAngle,minAngle,maxEmgValue;
    Date rawdata_timestamp;
    Long tsLong=0L;
    String exerciseType;
    BluetoothGattDescriptor mBluetoothGattDescriptor,mBluetoothGattDescriptor_raw;

    ArrayList<BluetoothGattCharacteristic> arrayList;

    AngleOperations angleOperations;


    //All the constant uuids are written here
    public static final UUID service1_uuid = UUID.fromString("909a1400-9693-4920-96e6-893c0157fedd");
    public static final UUID characteristic1_service1_uuid = UUID.fromString("909a1401-9693-4920-96e6-893c0157fedd");
    public static final UUID descriptor_characteristic1_service1_uuid = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");


    public static final UUID service2_uuid = UUID.fromString("909a0309-9693-4920-96e6-893c0157fedd");
    public static final UUID characteristic1_service2_uuid = UUID.fromString("909a7777-9693-4920-96e6-893c0157fedd");




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_monitor);

        //declarations
//        sessionDataTrackedFile      = generateFile("sessionDataTracked.txt");
//        sessionDataUnTrackedFile    = generateFile( "sessionDataUnTracked.txt");
        angleOperations = new AngleOperations();
        lineChart                   = findViewById(R.id.chart);
//        Angle                       = findViewById(R.id.Angle);
        EMG                         = findViewById(R.id.emgValue);
//        rangeOfMotion               = findViewById(R.id.rangeOfMotion);
        arcViewInside               = findViewById(R.id.arcViewInside);
        Repetitions                 = findViewById(R.id.Repetitions);
        holdTime                    = findViewById(R.id.holdtime);
        timer                       = findViewById(R.id.timer);
        stopBtn                     = findViewById(R.id.stopBtn);
        patientId                   = findViewById(R.id.patientId);
        patientName                 = findViewById(R.id.patientName);
        time                        = findViewById(R.id.displayTime);
        emgSignal                   = findViewById(R.id.emg);
        handler                     = new Handler();
        emgJsonArray                = new JSONArray();

        iv_back_monitor = findViewById(R.id.iv_back_monitor);
        tv_snap = findViewById(R.id.snap_monitor);


        tv_snap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeScreenshot(null);
            }
        });





        //mqtt
        mqttHelper = new MqttHelper(this);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        try {
            json_phizio = new JSONObject(sharedPreferences.getString("phiziodetails", ""));
            Log.i("Patient View", json_phizio.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        connectivityManager         = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        patientId.setText(getIntent().getStringExtra("patientId"));
        patientName.setText(getIntent().getStringExtra("patientName"));
        bodypart = getIntent().getStringExtra("exerciseType");
        //Getting the max and min angle of the particular body part
        maxAnglePart = angleOperations.getMaxAngle(bodypart);
        minAnglePart = angleOperations.getMinAngle(bodypart);
        arcViewInside.setMinAngle(minAnglePart);
        arcViewInside.setMaxAngle(minAnglePart);

        Log.i("max and min",maxAnglePart+" "+minAnglePart);
        creatGraphView();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        exerciseType = getIntent().getStringExtra("exerciseType");
        Log.i("Exercise Type", exerciseType);

        timer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enteredInsideTwenty = true;
                isSessionRunning = true;
                emgJsonArray = new JSONArray();
                maxAngle = 0;
                minAngle = 360;
                maxEmgValue = 0;
//                dataPoints.clear();
//                lineChart.notifyDataSetChanged();
                creatGraphView();
//                if(dataPoints!=null)
//                    dataPoints.clear();
//                lineChart.setScaleEnabled(true);
                k = 0;
                timer.setVisibility(View.GONE);
                SessionTimeForGraph = 0;
                stopBtn.setVisibility(View.VISIBLE);

                if (!servicesDiscovered) {
                    stopBtn.setVisibility(View.GONE);
                    timer.setVisibility(View.VISIBLE);
                    Toast.makeText(MonitorActivity.this, "Make Sure Pheeze is On", Toast.LENGTH_SHORT).show();
                } else {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            sendParticularDataToPheeze(exerciseType);
                        }
                    }, 100);


                    rawdata_timestamp = Calendar.getInstance().getTime();
                    String s = rawdata_timestamp.toString().substring(0, 19);

                    file_dir_emgdata = new File(Environment.getExternalStorageDirectory()+"/Pheezee/files","EmgData");
                    if (!file_dir_emgdata.exists()) {
                        file_dir_emgdata.mkdirs();
                    }
                    file_emgdata = new File(file_dir_emgdata, ""+s+".txt");
                    try {
                        file_emgdata.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        outputStream_emgdata = new FileOutputStream(file_emgdata, true);
                        outputStream_emgdata.write("EMG".getBytes());
                        outputStream_emgdata.write("\n".getBytes());
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    StartTime = SystemClock.uptimeMillis();
                    visiblity = View.GONE;
                    recieverState = true;
                    pheezeeState = true;
                    handler.postDelayed(runnable, 0);
                }
            }
        });
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSessionRunning=false;
                timer.setBackgroundResource(R.drawable.rounded_start_button);
                visiblity = View.VISIBLE;
                stopBtn.setVisibility(View.GONE);
                timer.setVisibility(View.VISIBLE);
                //Discable notifications
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        mBluetoothGatt.setCharacteristicNotification(mCharacteristic,false);
                        mBluetoothGattDescriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                        mBluetoothGatt.writeDescriptor(mBluetoothGattDescriptor);
                        mBluetoothGatt.writeCharacteristic(mCharacteristic);
                    }
                });


                TimeBuff += MillisecondTime;
                handler.removeCallbacks(runnable);
                MillisecondTime = 0L ;
                StartTime = 0L ;
                TimeBuff = 0L ;
                UpdateTime = 0L ;
                Seconds = 0 ;
                Minutes = 0 ;
                MilliSeconds = 0 ;
                pheezeeState =false;
                timer.setText(R.string.timer_start);
                recieverState = false;
                Log.i("minAngle",""+minAngle);
//                if(maxAngle!=0&&!(maxAngle>180)&&minAngle!=180&&!(minAngle<0)) {
                    tsLong = System.currentTimeMillis();
                    String ts = tsLong.toString();
                    try {

                        JSONObject summary = new JSONObject();
                        summary.put("repetition", Integer.parseInt(Repetitions.getText().toString()));
                        summary.put("maxAngle", maxAngle);
                        summary.put("minAngle", minAngle);
                        summary.put("maxEmg", maxEmgValue);
                        summary.put("holdTime", holdTimeValue);
                        summary.put("sessionTime",time.getText().toString().substring(16));
                        summary.put("timeStamp", tsLong);
                        sessionObj.put("Summary", summary);
                        sessionObj.put("timeStamp", ts);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    initiatePopupWindowModified(v);
                    MediaScannerConnection.scanFile(
                            getApplicationContext(),
                            new String[]{file_emgdata.getAbsolutePath()},
                            null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                @Override
                                public void onScanCompleted(String path, Uri uri) {
                                    Log.v("grokkingandroid",
                                            "file " + path + " was scanned seccessfully: " + uri);
                                }
                            });
//                }else {
//                    Toast.makeText(MonitorActivity.this,"your alignment is wrong!! try again,",Toast.LENGTH_LONG).show();
//                }
            }
        });


        //recieved message
        mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

            }

            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message){
                Log.i(topic,message.toString());
                if(topic.equals(mqtt_publish_add_patient_session_response)){
                    if(message.toString().equals("inserted")){
                        Log.i("message",message.toString());
                        editor = sharedPreferences.edit();
                        editor.putString("sync_session","");
                        editor.apply();
                    }
                }
                if(topic.equals(mqtt_publish_add_patient_session_emg_data_response)){
                    if(message.toString().equals("inserted")){
                        Log.i("message emg",message.toString());
                        editor = sharedPreferences.edit();
                        editor.putString("sync_emg_session","");
                        editor.apply();
                    }
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
        MillisecondTime = 0L ;
        StartTime = 0L ;
        TimeBuff = 0L ;
        UpdateTime = 0L ;
        Seconds = 0 ;
        Minutes = 0 ;
        MilliSeconds = 0 ;
        time.setText("Session time:   00 : 00 : 00");

        bluetoothAdapter = BluetoothSingelton.getmInstance().getAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "Bluetooth Disabled", Toast.LENGTH_SHORT).show();
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        if(mBluetoothGatt!=null){
            mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
            Toast.makeText(this, "GATT CLOSED", Toast.LENGTH_SHORT).show();
        }
        Log.i("MAC ADDRESS",""+getIntent().getStringExtra("deviceMacAddress"));
        if(!getIntent().getStringExtra("deviceMacAddress").equals(""))

            remoteDevice = bluetoothAdapter.getRemoteDevice(getIntent().getStringExtra("deviceMacAddress"));
        if(remoteDevice==null){
            Toast.makeText(this, "Make sure pheeze is On.", Toast.LENGTH_SHORT).show();
        }

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(remoteDevice!=null)
                    mBluetoothGatt = remoteDevice.connectGatt(MonitorActivity.this,true,callback);
            }
        });

        if(mBluetoothGatt!=null){
            Log.i("BLGATT","not connected");
        }

        try {
            sessionObj.put("PatientId",this.getIntent().getStringExtra("patientId"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        iv_back_monitor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
//        ConstraintSet mConstraintSet1 = new ConstraintSet();
//        mConstraintSet1.clone(this,R.layout.fragment_monitor);
//        ConstraintLayout mConstraintLayout = findViewById(R.id.monitorLayout);
//        LinearLayout linearLayout = mConstraintLayout.findViewById(R.id.pIdAndPName);
//        ProtractorView rangeOfMotionTemp = mConstraintLayout.findViewById(R.id.rangeOfMotion);
//        TextView angleTemp = mConstraintLayout.findViewById(R.id.Angle);
//        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            linearLayout.setOrientation(LinearLayout.VERTICAL);
//            rangeOfMotionTemp.setVisibility(View.GONE);
//            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
//            params.setMargins(0,0,0,0);
//            angleTemp.setTextColor(Color.parseColor("#000000"));
//            angleTemp.setLayoutParams(params);
//        }
//
//        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
//            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
//            rangeOfMotionTemp.setVisibility(View.VISIBLE);
//            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//            params.setMargins(0,-120,0,0);
//            angleTemp.setTextColor(Color.parseColor("#4B7080"));
//            angleTemp.setLayoutParams(params);
//        }
//
//        mConstraintSet1.setVisibility(R.id.timer,visiblity);
//        mConstraintSet1.setVisibility(R.id.stopBtn,ConstraintSet.VISIBLE);
//        mConstraintSet1.applyTo(mConstraintLayout);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            rangeOfMotion.setVisibility(View.INVISIBLE);
        }

        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
//            rangeOfMotion.setVisibility(View.VISIBLE);
        }
    }

    private void creatGraphView() {
        dataPoints = new ArrayList<>();
        dataPoints.add(new Entry(0,0));
        lineDataSet=new LineDataSet(dataPoints, "Emg Graph");
        lineDataSet.setDrawCircles(false);
        lineDataSet.setValueTextSize(0);
        lineDataSet.setDrawValues(false);
        lineDataSet.setColor(Color.parseColor("#4b7080"));
        lineData = new LineData(lineDataSet);
        lineChart.getXAxis();
        lineChart.setVisibleXRangeMaximum(1000);
        lineChart.getXAxis().setAxisMinimum(0f);
        lineChart.getAxisLeft().setSpaceTop(60f);
        lineChart.getAxisRight().setSpaceTop(60f);
        lineChart.getAxisLeft().setStartAtZero(false);
        lineChart.getAxisLeft().setDrawGridLines(false);
        lineChart.getXAxis().setDrawGridLines(false);
        lineChart.getXAxis().setDrawAxisLine(false);
        lineChart.setHorizontalScrollBarEnabled(true);
        lineChart.getDescription().setEnabled(false);
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.setScaleXEnabled(true);
        lineChart.setData(lineData);
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 1
        if(resultCode!=0){
            remoteDevice = bluetoothAdapter.getRemoteDevice(getIntent().getStringExtra("deviceMacAddress"));

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    mBluetoothGatt = remoteDevice.connectGatt(MonitorActivity.this,true,callback);
                }
            });

        }
    }

    public BluetoothGattCallback callback = new BluetoothGattCallback() {
        @Override
        public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyUpdate(gatt, txPhy, rxPhy, status);
        }

        @Override
        public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyRead(gatt, txPhy, rxPhy, status);
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            if(newState == BluetoothProfile.STATE_CONNECTED){
                Log.i("GATT CONNECTED", "Attempting to start the service discovery"+ gatt.discoverServices());
            }
            else if (newState == BluetoothProfile.STATE_CONNECTING){
                Log.i("GATT DISCONNECTED", "GATT server is being connected");
            }

            else if(newState == BluetoothProfile.STATE_DISCONNECTING){
                Log.i("GATT DISCONNECTING","Gatt server disconnecting");
            }
            else if (newState == BluetoothProfile.STATE_DISCONNECTED){
                Log.i("GATT DISCONNECTED", "Gatt server disconnected");
            }

            super.onConnectionStateChange(gatt, status, newState);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);

            if(characteristic.getUuid().equals(characteristic1_service1_uuid)) {
                Log.i("characteristic", characteristic.getUuid().toString() + " Written");
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        mBluetoothGatt.setCharacteristicNotification(mCharacteristic, true);
                        mBluetoothGattDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        mBluetoothGatt.writeDescriptor(mBluetoothGattDescriptor);
                        Log.i("HEllo", "HELLO");
                    }
                });
            }

            else if(characteristic.getUuid().equals(characteristic1_service2_uuid)){
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {

                        mBluetoothGatt.setCharacteristicNotification(mCharacteristic2, true);
                        mBluetoothGattDescriptor_raw.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        mBluetoothGatt.writeDescriptor(mBluetoothGattDescriptor_raw);
                        Log.i("HEllo", "HELLO_raw");
                    }
                });
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if(status==BluetoothGatt.GATT_SUCCESS){
                servicesDiscovered = true;
                BluetoothGattCharacteristic characteristic = gatt.getService(service1_uuid).getCharacteristic(characteristic1_service1_uuid);
                Log.i("TEST", "INSIDE IF");


                if(characteristic1_service1_uuid.equals(characteristic.getUuid()))
                    mCharacteristic = characteristic;

                mCharacteristic2 = gatt.getService(service2_uuid).getCharacteristic(characteristic1_service2_uuid);
                mBluetoothGatt = gatt;
                gatt.setCharacteristicNotification(characteristic,true);
                gatt.setCharacteristicNotification(mCharacteristic2,true);
                mBluetoothGattDescriptor = characteristic.getDescriptor(descriptor_characteristic1_service1_uuid);
                mBluetoothGattDescriptor_raw = mCharacteristic2.getDescriptor(descriptor_characteristic1_service1_uuid);

                arrayList.add(mCharacteristic);
                arrayList.add(mCharacteristic2);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onCharacteristicChanged(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            if(characteristic1_service1_uuid.equals(characteristic.getUuid())) {
                byte temp_byte[];

                temp_byte = characteristic.getValue();

                byte header_main = temp_byte[0];
                byte header_sub = temp_byte[1];


                byte sub_byte[] = new byte[sub_byte_size];
                int j = 2;
                for (int i = 0; i < sub_byte_size; i++, j++) {
                    sub_byte[i] = temp_byte[j];
                }


                if (byteToStringHexadecimal(header_main).equals("AA")) {
                    if (byteToStringHexadecimal(header_sub).equals("01")) {
                        Message message = Message.obtain();
                        message.obj = sub_byte;

                        myHandler.sendMessage(message);

                        try {
                            sessionResult.put(message);
                            sessionObj.put("data", sessionResult);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            else if(characteristic.getUuid().equals(characteristic1_service2_uuid)){
                byte[] b = characteristic.getValue();

                if(b.length<47){
                    int[] emg_data;

                    byte sub_byte[] = new byte[b.length ];
                    for (int i = 0; i < sub_byte.length; i++) {
                        sub_byte[i] = b[i];
                    }
                    try {
                        outputStream_emgdata = new FileOutputStream(file_emgdata, true);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    byte temp_array[] = new byte[emg_data_size_raw];
                    for (int i = 0; i < emg_data_size_raw; i++) {
                        temp_array[i] = sub_byte[i];
                    }
                    emg_data = constructEmgDataRawWithoutCombine(temp_array);

                    String str[] = new String[emg_data_size_raw];
                    for (int i = 0; i < emg_data.length; i++) {
                        str[i] = "" + emg_data[i];
                    }

                    if(enteredInsideTwenty){
                        String temp = "From here 20 byte packets are coming.";
                        try {
                            outputStream_emgdata.write(temp.getBytes());
                            outputStream_emgdata.write("\n".getBytes());
                            outputStream_emgdata.write("\n".getBytes());
                            outputStream_emgdata.write("\n".getBytes());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        enteredInsideTwenty= false;
                    }

                    try {
                        for (int i = 0; i < str.length; i++) {
                            if(str[i]!=null) {
                                outputStream_emgdata.write(str[i].getBytes());
                                outputStream_emgdata.write("\n".getBytes());
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        outputStream_emgdata.flush();
                        outputStream_emgdata.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    int[] emg_data;
                    byte sub_byte[] = new byte[b.length - 2];
                    int j = 2;
                    for (int i = 0; i < sub_byte.length; i++, j++) {
                        sub_byte[i] = b[j];
                    }
                    byte header_main = b[0];
                    byte header_sub = b[1];

                    if(byteToStringHexadecimal(header_main).equals("BB")) {
                        if (byteToStringHexadecimal(header_sub).equals("01")) {
                            try {
                                outputStream_emgdata = new FileOutputStream(file_emgdata, true);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                            byte temp_array[] = new byte[emg_num_packets_raw];
                            for (int i = 0; i < emg_num_packets_raw; i++) {
                                temp_array[i] = sub_byte[i];
                            }
                            emg_data = constructEmgDataRaw(temp_array);

                            String str[] = new String[emg_data_size_raw];
                            for (int i = 0; i < emg_data.length; i++) {
                                str[i] = "" + emg_data[i];
                            }

                            try {
                                for (int i = 0; i < str.length; i++) {
                                    if(str[i]!=null) {
                                        outputStream_emgdata.write(str[i].getBytes());
                                        outputStream_emgdata.write("\n".getBytes());
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            try {
                                outputStream_emgdata.flush();
                                outputStream_emgdata.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            if(descriptor_characteristic1_service1_uuid.equals(descriptor.getUuid())){
                Log.i("READ DESCRIPTOR", ""+status);
            }
        }



        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.i("DES", "ENTERED DESCRIPTOR");
            if(descriptor.getCharacteristic().getUuid().equals(mCharacteristic.getUuid())){
                if(isSessionRunning)
                    sendRaw();
                else {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            mBluetoothGatt.setCharacteristicNotification(mCharacteristic2,false);
                            mBluetoothGattDescriptor_raw.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                            mBluetoothGatt.writeDescriptor(mBluetoothGattDescriptor_raw);
                            mBluetoothGatt.writeCharacteristic(mCharacteristic2);
                        }
                    });
                }
            }
        }
    };


    public boolean send(byte[] data) {

        if (mBluetoothGatt == null ) {
            Log.w(TAG, "BluetoothGatt not initialized");
            return false;
        }
        if (mCharacteristic == null) {
            Log.w(TAG, "Send characteristic not found");
            return false;
        }

        BluetoothGattService service = mBluetoothGatt.getService(service1_uuid);

        if(service==null){
            if (mCharacteristic == null) {
                Log.w(TAG, "Send service not found");
                return false;
            }
        }
        if(characteristic1_service1_uuid.equals(mCharacteristic.getUuid())){
            Log.i("TRUE", "TRUE");
        }


        mCharacteristic.setValue(data);

        return mBluetoothGatt.writeCharacteristic(mCharacteristic);
    }


    public boolean sendRaw(){
        Log.i("send raw","send raw");
        byte data[] = hexStringToByteArray("BB01");
        if (mBluetoothGatt == null ) {
            Log.w(TAG, "BluetoothGatt not initialized");
            return false;
        }
        if (mCharacteristic2 == null) {
            Log.w(TAG, "Send characteristic not found");
            return false;
        }

        BluetoothGattService service = mBluetoothGatt.getService(service2_uuid);

        if(service==null){
            if (mCharacteristic2 == null) {
                Log.w(TAG, "Send service not found");
                return false;
            }
        }
        if(characteristic1_service2_uuid.equals(mCharacteristic2.getUuid())){
            Log.i("TRUE", "TRUE");
        }


        mCharacteristic2.setValue(data);

        return mBluetoothGatt.writeCharacteristic(mCharacteristic2);
    }

    public Runnable runnable = new Runnable() {

        public void run() {

            MillisecondTime = SystemClock.uptimeMillis() - StartTime;

            UpdateTime = TimeBuff + MillisecondTime;

            Seconds = (int) (UpdateTime / 1000);

            Minutes = Seconds / 60;

            Seconds = Seconds % 60;

            MilliSeconds = (int) (UpdateTime % 1000);

            timeText ="Session time:   " + String.format("%02d", Minutes) + " : " + String.format("%02d", Seconds) + " : " + String.format("%02d", MilliSeconds);

            time.setText(timeText);

            handler.postDelayed(this, 0);
        }

    };



    @SuppressLint("HandlerLeak")
    public final Handler myHandler = new Handler() {
        public void handleMessage(Message message ) {
            int angleDetected,num_of_reps, hold_time_minutes, hold_time_seconds;
            int[] emg_data;
            byte[] sub_byte;
            sub_byte = (byte[]) message.obj;
            emg_data = constructEmgData(sub_byte);
            angleDetected = getAngleFromData(sub_byte[20],sub_byte[21]);
            num_of_reps = getNumberOfReps(sub_byte[22], sub_byte[23]);
            hold_time_minutes = sub_byte[24];
            hold_time_seconds = sub_byte[25];
            String angleValue = ""+angleDetected;
            String repetitionValue = ""+num_of_reps;

            String minutesValue=""+hold_time_minutes,secondsValue=""+hold_time_seconds;
            if(hold_time_minutes<10)
                minutesValue = "0"+hold_time_minutes;
            if(hold_time_seconds<10)
                secondsValue = "0"+hold_time_seconds;
            holdTimeValue = minutesValue+" : "+secondsValue;

            if(angleDetected>=minAnglePart && angleDetected<=maxAnglePart) {
//                rangeOfMotion.setAngle(angleDetected);
                arcViewInside.setMaxAngle(angleDetected);
            }
//            Angle.setText(angleValue);
            for (int i=0;i<emg_data.length;i++) {
                lineData.addEntry(new Entry((float) UpdateTime / 1000, emg_data[i]), 0);
                lineChart.notifyDataSetChanged();
                lineChart.invalidate();
                lineChart.getXAxis();
                lineChart.getAxisLeft();
                lineChart.getAxisLeft().setValueFormatter(new IAxisValueFormatter() {
                    @Override
                    public String getFormattedValue(float value, AxisBase axis) {
                        return (int) value + "μV";
                    }
                });
                if (UpdateTime / 1000 > 3)
                    lineChart.setVisibleXRangeMaximum(5f);
                lineChart.moveViewToX((float) UpdateTime / 1000);
            }
//            lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

            for (int i=0;i<emg_data.length;i++) {
                emgJsonArray.put(emg_data[i]);
                EMG.setText(Integer.toString(emg_data[i]));
            }
            Repetitions.setText(repetitionValue);
            LinearLayout.LayoutParams params;
            params = (LinearLayout.LayoutParams) emgSignal.getLayoutParams();
            for (int i=0;i<emg_data.length;i++) {
                maxEmgValue = maxEmgValue < emg_data[i] ? emg_data[i] : maxEmgValue;
                if (maxEmgValue == 0)
                    maxEmgValue = 1;
                params.height = ((View) emgSignal.getParent()).getMeasuredHeight() * emg_data[i] / maxEmgValue;
            }
            if(angleDetected>=minAnglePart && angleDetected<=maxAnglePart) {
                maxAngle = maxAngle < angleDetected ? angleDetected : maxAngle;
                minAngle = minAngle > angleDetected ? angleDetected : minAngle;
            }
            emgSignal.setLayoutParams(params);
            holdTime.setText(holdTimeValue);
        }
    };

    private  void initiatePopupWindowModified(View v){
        View layout = getLayoutInflater().inflate(R.layout.session_summary, null);

        report = new PopupWindow(layout, ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT,true);
        report.setWindowLayoutMode(ConstraintLayout.LayoutParams.MATCH_PARENT,ConstraintLayout.LayoutParams.MATCH_PARENT);
        report.setOutsideTouchable(true);
        report.showAtLocation(v, Gravity.CENTER, 0, 0);

        //Gettig all the view items from the layout

        LinearLayout ll_min_max_arc = layout.findViewById(R.id.ll_min_max_arc);
        final TextView tv_patient_name =layout.findViewById(R.id.tv_summary_patient_name);
        final TextView tv_patient_id = layout.findViewById(R.id.tv_summary_patient_id);
        TextView tv_held_on = layout.findViewById(R.id.session_held_on);
        TextView tv_overall_summary = layout.findViewById(R.id.tv_overall_summary);
        TextView tv_min_angle = layout.findViewById(R.id.tv_min_angle);
        TextView tv_max_angle = layout.findViewById(R.id.tv_max_angle);
        TextView tv_total_time = layout.findViewById(R.id.tv_total_time);
        TextView tv_action_time = layout.findViewById(R.id.tv_action_time);
        TextView tv_hold_time = layout.findViewById(R.id.tv_hold_time);
        TextView tv_num_of_reps = layout.findViewById(R.id.tv_num_of_reps);
        TextView tv_max_emg = layout.findViewById(R.id.tv_max_emg);
        LinearLayout ll_click_to_view_report = layout.findViewById(R.id.ll_click_to_view_report);

        ll_click_to_view_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isNetworkAvailable()){
                    Intent mmt_intent = new Intent(MonitorActivity.this, SessionReportActivity.class);
                    mmt_intent.putExtra("patientid", tv_patient_id.getText().toString());
                    mmt_intent.putExtra("patientname", tv_patient_name.getText().toString());
                    try {
                        mmt_intent.putExtra("phizioemail", json_phizio.getString("phizioemail"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    startActivity(mmt_intent);
                }
                else {
                    networkError();
                }
            }
        });

        //Share and cancel image view
        LinearLayout summary_go_back = layout.findViewById(R.id.summary_go_back);
        LinearLayout summary_share =  layout.findViewById(R.id.summary_share);

        summary_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file = takeScreenshot(report);
                Uri pdfURI = FileProvider.getUriForFile(MonitorActivity.this, getApplicationContext().getPackageName() + ".my.package.name.provider", file);

             Intent i = new Intent();
             i.setAction(Intent.ACTION_SEND);
             i.putExtra(Intent.EXTRA_STREAM,pdfURI);
             i.setType("application/jpg");
             startActivity(Intent.createChooser(i, "share pdf"));
            }
        });


        summary_go_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                report.dismiss();
            }
        });


        //Emg Progress Bar
        ProgressBar pb_max_emg = layout.findViewById(R.id.progress_max_emg);

        //Setting the text views
        tv_patient_id.setText(patientId.getText().toString());
        tv_patient_name.setText(patientName.getText().toString());


        //for held on date
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat formatter_date = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = formatter.format(new Date(tsLong));
        String dateString_date = formatter_date.format(new Date(tsLong));
        tv_held_on.setText(dateString_date);


        tv_min_angle.setText(Integer.toString(minAngle)+"°");
        tv_max_angle.setText(Integer.toString(maxAngle)+"°");


        //total session time
        String tempSessionTime = time.getText().toString().substring(16);
        tempSessionTime = tempSessionTime.substring(0,2)+"m"+tempSessionTime.substring(3,7)+"s";
        tv_total_time.setText(tempSessionTime);


        tv_action_time.setText(tempSessionTime);
        tv_hold_time.setText(holdTimeValue.substring(0,2)+"m"+holdTimeValue.substring(2)+"s");


        tv_num_of_reps.setText(Repetitions.getText().toString());
        tv_max_emg.setText(Integer.toString(maxEmgValue)+"μV");

        //Creating the arc
        ArcViewInside arcView =layout.findViewById(R.id.session_summary_arcview);
        arcView.setMaxAngle(maxAngle);
        arcView.setMinAngle(minAngle);
        TextView tv_180 = layout.findViewById(R.id.tv_180);
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            tv_180.setPadding(5,1,170,1);
        }

        arcView.setRangeColor(getResources().getColor(R.color.good_green));

        //Max Emg Progress
        pb_max_emg.setMax(400);
        pb_max_emg.setProgress(maxEmgValue);
        pb_max_emg.setEnabled(false);


        storeLocalSessionDetails(dateString,tempSessionTime);


    }

    private void initiatePopupWindow(View v) {

        View layout = getLayoutInflater().inflate(R.layout.session_analysis, null);

        report = new PopupWindow(layout, ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT);
        report.setOutsideTouchable(true);
        report.setContentView(layout);
        report.setFocusable(true);
        report.showAtLocation(rootView, Gravity.CENTER, 0, 0);

        //imp stuff 921-968
        LinearLayout cancelbtn = layout.findViewById(R.id.cancel_action);
        LinearLayout shareIcon = layout.findViewById(R.id.shareIcon);
        final ConstraintLayout summaryView = layout.findViewById(R.id.summaryView);
        TextView popUpPatientId = layout.findViewById(R.id.patientId);
        TextView popUpPatientName = layout.findViewById(R.id.patientName);
        TextView maxAngleView = layout.findViewById(R.id.maxAngle);
        TextView repetitions = layout.findViewById(R.id.totalReps);
        TextView minAngleView  = layout.findViewById(R.id.minAngle);
        TextView maxEmgView  = layout.findViewById(R.id.maxEmg);
        TextView holdTimeView  = layout.findViewById(R.id.holdtime);
        TextView heldTime = layout.findViewById(R.id.heldtime);
        TextView sessionTime = layout.findViewById(R.id.sessionTime);
        repetitions.setText(Repetitions.getText().toString());
        popUpPatientId.setText(patientId.getText().toString());
        popUpPatientName.setText(patientName.getText().toString());
        maxAngleView.setText(Integer.toString(maxAngle)+"°");
        minAngleView.setText(Integer.toString(minAngle)+"°");
        maxEmgView.setText(Integer.toString(maxEmgValue)+"μV");
        holdTimeView.setText(holdTimeValue.substring(0,2)+"m"+holdTimeValue.substring(2)+"s");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(new Date(tsLong));
        heldTime.setText(dateString);
        String tempSessionTime = time.getText().toString().substring(16);
        tempSessionTime = tempSessionTime.substring(0,2)+"m"+tempSessionTime.substring(3,7)+"s";
        sessionTime.setText(tempSessionTime);





        cancelbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                report.dismiss();
            }
        });
        shareIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MonitorActivity.this,"shot",Toast.LENGTH_SHORT).show();
                OnClickShare(summaryView);
            }
        });


        storeLocalSessionDetails(dateString,tempSessionTime);
    }

    private void storeLocalSessionDetails(String dateString,String tempsession) {
        JSONArray array ;
        if (!sharedPreferences.getString("phiziodetails", "").equals("")) {
            Log.i("Patient View","Inside");
            try {
                jsonData = new JSONArray(json_phizio.getString("phiziopatients"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if(jsonData.length()>0) {
                for (int i = 0; i < jsonData.length(); i++) {
                    try {
                        int numofsessions;
                        if (jsonData.getJSONObject(i).get("patientid").equals(patientId.getText().toString())) {
                            if(jsonData.getJSONObject(i).has("numofsession")) {
                                numofsessions = Integer.parseInt(jsonData.getJSONObject(i).get("numofsession").toString());
                                numofsessions += 1;
                            }
                            else {
                                numofsessions = 1;
                            }
                            jsonData.getJSONObject(i).put("numofsession",""+numofsessions);
                            JSONObject object = new JSONObject();
                            object.put("heldon",dateString);
                            object.put("maxangle",maxAngle);
                            object.put("minangle",minAngle);
                            object.put("maxemg",maxEmgValue);
                            object.put("holdtime","00 : 50");
                            object.put("bodypart",bodypart);
                            object.put("sessiontime",tempsession);
                            object.put("numofreps",Repetitions.getText().toString());
                            json_phizio.put("phiziopatients",jsonData);
                            editor = sharedPreferences.edit();
                            editor.putString("phiziodetails", json_phizio.toString());
                            editor.apply();
                            object.put("numofsessions",""+numofsessions);
                            object.put("phizioemail",json_phizio.get("phizioemail"));
                            object.put("patientid",patientId.getText().toString());

                            //mqtt publishing

                            MqttMessage mqttMessage = new MqttMessage();
                            mqttMessage.setPayload(object.toString().getBytes());

                            object.put("emgdata",emgJsonArray);
                            editor = sharedPreferences.edit();
                            if(isNetworkAvailable()) {
                                mqttHelper.publishMqttTopic(mqtt_publish_add_patient_session, mqttMessage);
                            }
                                JSONObject temp = new JSONObject();
                                temp.put("topic",mqtt_publish_add_patient_session);
                                temp.put("message",mqttMessage.toString());

                                editor = sharedPreferences.edit();
                                if(!sharedPreferences.getString("sync_session","").equals("")){
                                    array = new JSONArray(sharedPreferences.getString("sync_session",""));
                                    array.put(temp);
                                    editor.putString("sync_session",array.toString());
                                    editor.commit();
                                }
                                else {
                                    array = new JSONArray();
                                    array.put(temp);
                                    editor.putString("sync_session",array.toString());
                                    editor.commit();
                                }
                                //clearing the previuos mqtt message
                                mqttMessage.clearPayload();
                                mqttMessage.setPayload(object.toString().getBytes());


                                temp = new JSONObject();
                                temp.put("topic",mqtt_publish_add_patient_session_emg_data);
                                temp.put("message",mqttMessage.toString());
                                if(isNetworkAvailable())
                                    mqttHelper.publishMqttTopic(mqtt_publish_add_patient_session_emg_data, mqttMessage);
                                if(!sharedPreferences.getString("sync_session","").equals("")){
                                    array = new JSONArray(sharedPreferences.getString("sync_session",""));
                                    array.put(temp);
                                    editor.putString("sync_session",array.toString());
                                    editor.commit();
                                }
                                else {
                                    array = new JSONArray();
                                    array.put(temp);
                                    editor.putString("sync_session",array.toString());
                                    editor.commit();
                                }
                            }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mqttHelper.mqttAndroidClient.unregisterResources();
        mqttHelper.mqttAndroidClient.close();

        if(mBluetoothGatt!=null && mCharacteristic!=null){
            mBluetoothGatt.setCharacteristicNotification(mCharacteristic,false);
            mBluetoothGattDescriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(mBluetoothGattDescriptor);
            mBluetoothGatt.writeCharacteristic(mCharacteristic);
        }
    }

    public void OnClickShare(View view){

        Bitmap bitmap =getBitmapFromView(view);
        try {
            File file = new File(getExternalCacheDir(),"sessionSummary.png");
            FileOutputStream fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
            file.setReadable(true, false);
            final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            intent.setType("image/png");
            startActivity(Intent.createChooser(intent, "Share image via"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Bitmap getBitmapFromView(View view) {
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable =view.getBackground();
        if (bgDrawable!=null) {
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        }   else{
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);
        }
        view.draw(canvas);
        return returnedBitmap;
    }


    public void sendParticularDataToPheeze(String string){

        switch (string.toLowerCase()){

            case "elbow":{
                byte b[] = hexStringToByteArray("AA03");
                if(send(b)){
                    Log.i("SENDING","MESSAGE SENT AA03");
                }
                else {
                    Log.i("SENDING","UNSUCCESSFULL");
                }
                break;
            }

            case "knee":{
                byte b[] = hexStringToByteArray("AA04");
                if(send(b)){
                    Log.i("SENDING","MESSAGE SENT AA04");
                }
                else {
                    Log.i("Knee", "true");
                    Log.i("SENDING","UNSUCCESSFULL");
                }
                break;
            }

            case "ankle":{
                byte b[] = hexStringToByteArray("AA05");
                if(send(b)){
                    Log.i("SENDING","MESSAGE SENT AA05");
                }
                else {
                    Log.i("Knee", "true");
                    Log.i("SENDING","UNSUCCESSFULL");
                }
                break;
            }
            case "hip":{
                byte b[] = hexStringToByteArray("AA06");
                if(send(b)){
                    Log.i("SENDING","MESSAGE SENT AA06");
                }
                else {
                    Log.i("Knee", "true");
                    Log.i("SENDING","UNSUCCESSFULL");
                }
                break;
            }

            case "wrist":{
                byte b[] = hexStringToByteArray("AA07");
                if(send(b)){
                    Log.i("SENDING","MESSAGE SENT AA07");
                }
                else {
                    Log.i("Knee", "true");
                    Log.i("SENDING","UNSUCCESSFULL");
                }
                break;
            }

            case "sholder":{
                byte b[] = hexStringToByteArray("AA08");
                if(send(b)){
                    Log.i("SENDING","MESSAGE SENT AA08");
                }
                else {
                    Log.i("Knee", "true");
                    Log.i("SENDING","UNSUCCESSFULL");
                }
                break;
            }
        }
    }


    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }






    public int[] constructEmgData(byte[] sub_byte){
        int k=0;
        int[] emg_data = new int[emg_data_size_session];
        for (int i=0;i<emg_num_packets_session;i++){
            int a = sub_byte[i]&0xFF;
            int b = sub_byte[i+1]&0xFF;

            emg_data[k] = b<<8 | a;
            i++;
            k++;
        }
        return emg_data;
    }


    public int[] constructEmgDataRaw(byte[] sub_byte){
        int k=0;
        int[] emg_data = new int[emg_data_size_raw];
        for (int i=0;i<sub_byte.length;i++){
            int a = sub_byte[i]&0xFF;
            int b = sub_byte[i+1]&0xFF;

            emg_data[k] = b<<8 | a;
            i++;
            k++;
        }
        return emg_data;
    }

    public int[] constructEmgDataRawWithoutCombine(byte[] sub_byte){
        int k=0;
        int[] emg_data = new int[emg_data_size_raw];
        for (int i=0;i<sub_byte.length;i++){
            emg_data[k] = sub_byte[i]&0xFF;
            i++;
            k++;
        }
        return emg_data;
    }


    public int getAngleFromData(byte a, byte b){
        return b<<8 | a&0xFF;
    }

    public int getNumberOfReps(byte a, byte b){
        return (a & 0xff) |
                ((b & 0xff) << 8);
    }


    public String byteToStringHexadecimal(byte b){
        return String.format("%02X",b);
    }


    //Internet is there or not
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void networkError(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Network Error");
        builder.setMessage("Please connect to internet and try again");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.show();
    }


    private File takeScreenshot(PopupWindow popupWindow) {

        Date now = new Date();
        File snap = null;
        View v1;
        DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

        try {
            // image naming and path  to include sd card  appending name you choose for file
            String mPath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg";
            String child = patientName.getText().toString()+patientId.getText().toString();
            File f1 =  new File(Environment.getExternalStorageDirectory()+"/Pheezee/files/Monitor",child);

            if (!f1.exists()) {
                f1.mkdirs();
            }

            snap = new File(f1,now+".jpg");

            try {
                snap.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // create bitmap screen capture
            if(popupWindow!=null){
                v1 = popupWindow.getContentView().getRootView();
                v1.setDrawingCacheEnabled(true);
            }
            else {
                v1 = getWindow().getDecorView().getRootView();
                v1.setDrawingCacheEnabled(true);
            }
            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);

            FileOutputStream outputStream = new FileOutputStream(snap);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();


            MediaScannerConnection.scanFile(
                    getApplicationContext(),
                    new String[]{snap.getAbsolutePath()},
                    null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                            Log.v("grokkingandroid",
                                    "file " + path + " was scanned seccessfully: " + uri);
                        }
                    });
            //openScreenshot(imageFile);
        } catch (Throwable e) {
            // Several error may come out with file handling or DOM
            e.printStackTrace();
        }

        return snap;
    }
}
