package com.start.apps.pheezee.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.airbnb.lottie.LottieAnimationView;

import start.apps.pheezee.R;

import com.start.apps.pheezee.adapters.DeviceListArrayAdapter;
import com.start.apps.pheezee.classes.BluetoothSingelton;
import com.start.apps.pheezee.classes.DeviceListClass;
import com.start.apps.pheezee.services.PheezeeBleService;
import com.start.apps.pheezee.utils.RegexOperations;

import java.util.ArrayList;

import static com.start.apps.pheezee.services.PheezeeBleService.bluetooth_state;
import static com.start.apps.pheezee.services.PheezeeBleService.device_state;
import static com.start.apps.pheezee.services.PheezeeBleService.scan_state;
import static com.start.apps.pheezee.services.PheezeeBleService.scan_too_frequent;
import static com.start.apps.pheezee.services.PheezeeBleService.scanned_list;

import android.view.WindowManager;

public class ScanDevicesActivity extends AppCompatActivity {
    private boolean tooFrequentScan = false;

    Dialog tooFrequentDialog;
    ListView lv_scandevices;
    SwipeRefreshLayout swipeRefreshLayout;

    Handler handler;
    private boolean mBluetoothState = true;

    private static final int REQUEST_FINE_LOCATION = 1;
    TextView tv_stoScan;
    ArrayList<DeviceListClass> mScanResults;
    DeviceListArrayAdapter deviceListArrayAdapter;
    private static final int REQUEST_ENABLE_BT = 1;
    BluetoothAdapter madapter_scandevices;
    ImageView iv_back_scan_devices;
    LottieAnimationView view, scan_toolbar_anim;
    //sercice and bind
    PheezeeBleService mCustomService;
    boolean isBound = false;
    long first_started = 0;
    int num_of_scan = 0;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_devices);
        Toolbar toolbar = findViewById(R.id.toolbar_scandevices);
        setSupportActionBar(toolbar);


        //Initialization
        tv_stoScan = findViewById(R.id.tv_stopscan);
        iv_back_scan_devices = findViewById(R.id.back_scan_devices);
        lv_scandevices = findViewById(R.id.lv_deviceList);
        swipeRefreshLayout = findViewById(R.id.scandevices_swiperefresh);
        view = findViewById(R.id.scan_devices_anim);
        scan_toolbar_anim = findViewById(R.id.scan_devices_toolbar);

        handler = new Handler();
        mScanResults = new ArrayList<>();
        hasPermissions();
        deviceListArrayAdapter = new DeviceListArrayAdapter(this, mScanResults);


        deviceListArrayAdapter.setOnDeviceConnectPressed(new DeviceListArrayAdapter.onDeviceConnectPressed() {
            @Override
            public void onDeviceConnectPressed(String macAddress) {
                if (mBluetoothState) {
                    Intent intent = new Intent();
                    if (RegexOperations.validate(macAddress)) {

                        intent.putExtra("macAddress", "Pheezee");
                        setResult(-1, intent);
                    } else {
                        intent.putExtra("macAddress", "Pheezee");
                        setResult(2, intent);
                    }
                    finish();
                } else {
                    startBleRequest();
                }
            }
        });

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        madapter_scandevices = BluetoothSingelton.getmInstance().getAdapter();
        madapter_scandevices = bluetoothManager.getAdapter();
        if (madapter_scandevices == null || !madapter_scandevices.isEnabled()) {
            mBluetoothState = false;
            startBleRequest();
        }


        iv_back_scan_devices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        tv_stoScan.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                String check_operation;
                check_operation = tv_stoScan.getText().toString();
                if (check_operation.equalsIgnoreCase("Scan")) {
                    tv_stoScan.setText(R.string.scandevices_stop);
                    mCustomService.startScanInBackground();
                } else {
                    tv_stoScan.setText(R.string.scandevices_scan);
                    mCustomService.stopScaninBackground();
                }
            }
        });

        swipeRefreshLayout.setColorSchemeResources(R.color.good_green, R.color.pale_good_green);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mCustomService.stopScaninBackground();
                mCustomService.startScanInBackground();
                if (tooFrequentScan) {
                    swipeRefreshLayout.setRefreshing(false);
                } else {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }, 2000);
                }
            }
        });

        lv_scandevices.setAdapter(deviceListArrayAdapter);

//        start_scan_handler.run();

        Intent mIntent = new Intent(this, PheezeeBleService.class);
        bindService(mIntent, mConnection, BIND_AUTO_CREATE);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(device_state);
        intentFilter.addAction(bluetooth_state);
        intentFilter.addAction(scanned_list);
        intentFilter.addAction(scan_state);
        intentFilter.addAction(scan_too_frequent);
        registerReceiver(receiver, intentFilter);
    }

    private void startBleRequest() {
        Intent enable_bluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        startActivityForResult(enable_bluetooth, REQUEST_ENABLE_BT);
    }


    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            isBound = true;
            PheezeeBleService.LocalBinder mLocalBinder = (PheezeeBleService.LocalBinder) service;
            mCustomService = mLocalBinder.getServiceInstance();
            if (mBluetoothState) {
                mCustomService.stopScaninBackground();
                mCustomService.startScanInBackground();
                updateList(mCustomService.getScannedList());
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            mCustomService = null;
            isBound = false;
        }
    };

    public Context getContext() {
        return this;
    }

    @Override
    protected void onResume() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            finish();
        }
        super.onResume();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound) {
            unbindService(mConnection);
        }
        unregisterReceiver(receiver);
    }

    private boolean hasPermissions() {
        if (madapter_scandevices == null || !madapter_scandevices.isEnabled()) {
            requestBluetoothEnable();
            return false;
        } else if (!hasLocationPermissions()) {
            requestLocationPermission();
            return false;
        }
        return true;
    }

    /**
     * request bluetooth enable
     */
    private void requestBluetoothEnable() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }


    private boolean hasLocationPermissions() {
        return ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
    }




    private void updateList(ArrayList<DeviceListClass> listClasses){
        if(listClasses!=null && listClasses.size()>1) {
            if(view.getVisibility()==View.VISIBLE) {
                view.setVisibility(View.GONE);
                scan_toolbar_anim.setVisibility(View.VISIBLE);
            }
            mScanResults = listClasses;
            deviceListArrayAdapter.updateList(mScanResults);
        }else {
            if(listClasses!=null){
                mScanResults = listClasses;
                deviceListArrayAdapter.updateList(mScanResults);
            }
            if(view.getVisibility()==View.GONE) {
                view.setVisibility(View.VISIBLE);
            }
            if(scan_toolbar_anim.getVisibility()==View.VISIBLE)
            {
                scan_toolbar_anim.setVisibility(View.GONE);
            }
        }
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equalsIgnoreCase(device_state)){
                boolean device_status = intent.getBooleanExtra(device_state,false);
                if(device_status){
                }else {
                }
            }else if(action.equalsIgnoreCase(bluetooth_state)){
                boolean ble_state = intent.getBooleanExtra(bluetooth_state,false);
                if(ble_state){
                    mBluetoothState = true;
                    mCustomService.stopScaninBackground();
                    mCustomService.startScanInBackground();
                }else {
                    startBleRequest();
                    mBluetoothState = false;
                    setAnimationHidden();
//                    mCustomService.stopScaninBackground();
                }
            }else if(action.equalsIgnoreCase(scanned_list)){
                if(mCustomService!=null){
                    mScanResults =  mCustomService.getScannedList();
                    updateList(mScanResults);
                }

            }else if(action.equalsIgnoreCase(scan_state)){
                boolean scanning_state = intent.getBooleanExtra(scan_state,false);
                if(scanning_state){
                    setAnimVisible();
                    tv_stoScan.setText(R.string.scandevices_stop);
                }else {
                    setAnimationHidden();
                    tv_stoScan.setText(R.string.scandevices_scan);
                }
            }else if(action.equalsIgnoreCase(scan_too_frequent)){
                boolean scanning_frequence = intent.getBooleanExtra(scan_too_frequent,false);
                if(scanning_frequence){
                    tooFrequentScan = true;
                    tv_stoScan.setText(R.string.scandevices_stop);
//                    tv_stoScan.setTextColor(getResources().getColor(R.color.red));
                    if(tooFrequentDialog!=null){
                        if(!tooFrequentDialog.isShowing()){
                            tooFrequentScanningDialog();
                        }
                    }else {
                        tooFrequentScanningDialog();
                    }
                }else {
                    tooFrequentScan = false;
//                    tv_stoScan.setTextColor(getResources().getColor(R.color.background_green));
                    if(tooFrequentDialog!=null){
                        tooFrequentDialog.dismiss();
                    }
                }
            }
        }
    };

    public void setAnimVisible(){
        if(scan_toolbar_anim.getVisibility()==View.GONE){
//            scan_toolbar_anim.setVisibility(View.VISIBLE);
        }

        if(mScanResults.size()==0) {
            if(view.getVisibility()==View.GONE){
                view.setVisibility(View.VISIBLE);
            }
        }
        view.playAnimation();
        scan_toolbar_anim.playAnimation();
    }




    public void setAnimationHidden(){
        if(view.getVisibility()==View.VISIBLE){
            view.setVisibility(View.GONE);
        }
        if(scan_toolbar_anim.getVisibility()==View.VISIBLE){
            scan_toolbar_anim.setVisibility(View.GONE);
        }
    }

    public void showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void tooFrequentScanningDialog() {

        // Custom notification added by Haaris
        // custom dialog
        tooFrequentDialog = new Dialog(this);
        tooFrequentDialog.setContentView(R.layout.notification_dialog_box_single_button);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(tooFrequentDialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        tooFrequentDialog.getWindow().setAttributes(lp);

        TextView notification_title = tooFrequentDialog.findViewById(R.id.notification_box_title);
        TextView notification_message = tooFrequentDialog.findViewById(R.id.notification_box_message);

        Button Notification_Button_ok = (Button) tooFrequentDialog.findViewById(R.id.notification_ButtonOK);

        Notification_Button_ok.setText("Okay");

        // Setting up the notification dialog
        notification_title.setText("Scanning in progress");
        notification_message.setText("You are starting and stopping the scan very frequently, your scanning is running please wait.");

        // On click on Continue
        Notification_Button_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tooFrequentDialog.dismiss();
            }
        });

        tooFrequentDialog.show();

        // End

    }
}
