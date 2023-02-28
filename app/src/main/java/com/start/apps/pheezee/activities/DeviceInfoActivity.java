package com.start.apps.pheezee.activities;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.type.DateTime;
import com.shreyaspatil.MaterialDialog.MaterialDialog;

import start.apps.pheezee.R;

import com.start.apps.pheezee.dfu.DfuService;
import com.start.apps.pheezee.dfu.fragment.UploadCancelFragment;
import com.start.apps.pheezee.popup.AddDevicePopupWindow;
import com.start.apps.pheezee.popup.DFUPopupWindow;
import com.start.apps.pheezee.repository.MqttSyncRepository;
import com.start.apps.pheezee.services.PheezeeBleService;
import com.start.apps.pheezee.utils.NetworkOperations;
import com.start.apps.pheezee.utils.RegexOperations;
import com.start.apps.pheezee.utils.ZipOperations;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import no.nordicsemi.android.dfu.DfuBaseService;
import no.nordicsemi.android.dfu.DfuController;
import no.nordicsemi.android.dfu.DfuProgressListener;
import no.nordicsemi.android.dfu.DfuProgressListenerAdapter;
import no.nordicsemi.android.dfu.DfuServiceInitiator;
import no.nordicsemi.android.dfu.DfuServiceListenerHelper;

import static com.start.apps.pheezee.activities.PatientsView.json_phizioemail;
import static com.start.apps.pheezee.services.PheezeeBleService.battery_percent;
import static com.start.apps.pheezee.services.PheezeeBleService.bluetooth_state;
import static com.start.apps.pheezee.services.PheezeeBleService.calibration_state;
import static com.start.apps.pheezee.services.PheezeeBleService.device_disconnected_firmware;
import static com.start.apps.pheezee.services.PheezeeBleService.device_state;
import static com.start.apps.pheezee.services.PheezeeBleService.df_characteristic_written;
import static com.start.apps.pheezee.services.PheezeeBleService.hardware_version;
import static com.start.apps.pheezee.services.PheezeeBleService.magnetometer_present;
import static com.start.apps.pheezee.services.PheezeeBleService.manufacturer_name;
import static com.start.apps.pheezee.services.PheezeeBleService.serial_id;
import static com.start.apps.pheezee.services.PheezeeBleService.usb_state;

import android.app.Dialog;
import android.view.WindowManager;


public class DeviceInfoActivity extends AppCompatActivity implements UploadCancelFragment.CancelFragmentListener {

    //Bluetooth related declarations
    public String TAG = "DeviceInfoActivity";
    LottieAnimationView calib_anim;
    private boolean inside_bootloader = false, mDeviceDeactivated = false, mActivateCommandGiven = false;
    private static final int REQUEST_ENABLE_BT = 1;
    private int device_baterry_level = 0;
    BluetoothAdapter bluetoothAdapter;
    BluetoothManager mBluetoothManager;
    PheezeeBleService mService;
    private boolean isBound = false, start_update = false, mDeviceState = false;
    private String deviceMacc = "";
    private static final int REQUEST_FINE_LOCATION = 14;
    AlertDialog mDialog_scan;
    private double latitude = 0, longitude = 0;
    AlertDialog.Builder builder;
    final CharSequence[] peezee_items = {"Scan for nearby Pheezee devices",
            "Qrcode Scan", "Cancel"};
    Intent to_scan_devices_activity;

    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    //Declaring all the view items
    TextView tv_device_name, tv_device_mamc, tv_firmware_version, tv_serial_id, tv_hardware_version,
            tv_battery_level, tv_connection_status, tv_disconnect_forget, mTextUploading, mTextPercentage, tv_update_firmware, tv_deviceinfo_device_connected, tv_deviceinfo_device_disconnected,
            tv_reactivate_device, tv_calibrate_device, tv_status_calibrate;
    ImageView iv_back_device_info, my_device_image;
    private ProgressBar mProgressBar;
    LinearLayout ll_dfu;
    DfuController controller;
    MaterialDialog mDialog, mDfuDialog;
    AlertDialog mDeactivatedDialog, dialog_calibrate;
    MqttSyncRepository repository;
    ProgressDialog mCheckReactivationDialog;
    Handler calibration_handler = new Handler();
    Button btn_start_calibration, btn_cancel_calibration;

    DFUPopupWindow dfu_popupwindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_info);
        repository = new MqttSyncRepository(getApplication());
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        tv_disconnect_forget = findViewById(R.id.tv_disconnect_forget);
        tv_device_name = findViewById(R.id.tv_deviceinfo_device_name);
        tv_device_mamc = findViewById(R.id.tv_deviceinfo_device_mac);
        tv_battery_level = findViewById(R.id.tv_deviceinfo_device_battery);
        tv_connection_status = findViewById(R.id.tv_deviceinfo_device_connection_status);
        tv_deviceinfo_device_connected = findViewById(R.id.tv_deviceinfo_device_connected);
        tv_deviceinfo_device_disconnected = findViewById(R.id.tv_deviceinfo_device_disconnected);
        tv_serial_id = findViewById(R.id.tv_deviceinfo_device_serial);
        tv_hardware_version = findViewById(R.id.tv_hardware_version);
        tv_firmware_version = findViewById(R.id.tv_deviceinfo_device_firmware);
        iv_back_device_info = findViewById(R.id.iv_back_device_info);
        my_device_image = findViewById(R.id.my_device_image);
        mProgressBar = findViewById(R.id.progressbar_file);
        mTextUploading = findViewById(R.id.textviewUploading);
        mTextPercentage = findViewById(R.id.textviewProgress);
        tv_update_firmware = findViewById(R.id.update_firmware);
        tv_reactivate_device = findViewById(R.id.tv_reactivate_device);
        tv_calibrate_device = findViewById(R.id.tv_calibrate_device);
        ll_dfu = findViewById(R.id.ll_dfu);
        mCheckReactivationDialog = new ProgressDialog(this);

        iv_back_device_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation aniFade = AnimationUtils.loadAnimation(DeviceInfoActivity.this, R.anim.fade_in);
                iv_back_device_info.setAnimation(aniFade);
                finish();
            }
        });

        tv_calibrate_device.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calibrateDeviceDialog();

            }
        });

        tv_reactivate_device.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reactivateDevice();
            }
        });


        tv_connection_status.setText("N/C");
        if (deviceMacc.equals("")) {
            my_device_image.setImageResource(R.drawable.my_device_pheezee);
        } else my_device_image.setImageResource(R.drawable.my_device_image_disconnected);

        //checking bluetooth switched on and connecting gatt functions
        mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        bluetoothAdapter = mBluetoothManager.getAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(DeviceInfoActivity.this, "Bluetooth Disabled", Toast.LENGTH_SHORT).show();
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

        if (!getIntent().getStringExtra("deviceMacAddress").equals("")) {
            tv_device_mamc.setText(getIntent().getStringExtra("deviceMacAddress"));
            tv_disconnect_forget.setText("+ Add Pheezee");
            deviceMacc = tv_device_mamc.getText().toString();

        }

        if (!preferences.getString("deviceMacaddress", "").equalsIgnoreCase("")) {
            tv_device_mamc.setText(preferences.getString("deviceMacaddress", ""));
            tv_disconnect_forget.setText("+ Add Pheezee");
            deviceMacc = tv_device_mamc.getText().toString();
        }

        tv_deviceinfo_device_connected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPheezeeDevice(v);
            }
        });

        tv_deviceinfo_device_disconnected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                addPheezeeDevice(v);
                editor = preferences.edit();
                editor.putString("deviceMacaddress", "");
                editor.apply();
                if (mService != null) {
                    mService.forgetPheezee();
                    mService.disconnectDevice();
                }

                enableScanningTheDevices();
                deviceMacc = "";
                refreshView();
                AddDevicePopupWindow feedback = new AddDevicePopupWindow(DeviceInfoActivity.this, deviceMacc, false, "Pheezee", preferences, mService);
                feedback.showWindow();
            }
        });
        tv_disconnect_forget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                editor = preferences.edit();
//                editor.putString("deviceMacaddress","");
//                editor.apply();
//                if(mService!=null){
//                    mService.forgetPheezee();
//                    mService.disconnectDevice();
//                }
//                refreshView();
//                tv_disconnect_forget.setText("");
//                Intent to_scan_devices_activity;
//                to_scan_devices_activity = new Intent(DeviceInfoActivity.this, ScanDevicesActivity.class);
//                startActivityForResult(to_scan_devices_activity, 12);
                addPheezeeDevice(v);
//                finish();
            }
        });

        tv_update_firmware.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int x = mService.getDeviceDeactivationStatus();
                mDeviceDeactivated = x == 1;
                if (!mDeviceDeactivated) {
                    if (NetworkOperations.isNetworkAvailable(DeviceInfoActivity.this)) {
                        startFirmwareUpdate();
                    } else {
                        NetworkOperations.networkError(DeviceInfoActivity.this);
                    }
                } else {
                    Toast.makeText(DeviceInfoActivity.this, "Pheeze Deactivated", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Intent mIntent = new Intent(this, PheezeeBleService.class);
        bindService(mIntent, mConnection, BIND_AUTO_CREATE);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(device_state);
        intentFilter.addAction(bluetooth_state);
        intentFilter.addAction(usb_state);
        intentFilter.addAction(battery_percent);
        intentFilter.addAction(PheezeeBleService.firmware_version);
        intentFilter.addAction(serial_id);
        intentFilter.addAction(manufacturer_name);
        intentFilter.addAction(df_characteristic_written);
        intentFilter.addAction(hardware_version);
        intentFilter.addAction(device_disconnected_firmware);
        intentFilter.addAction(calibration_state);
        intentFilter.addAction(magnetometer_present);
        registerReceiver(device_info_receiver, intentFilter);


        if (getIntent().getBooleanExtra("start_update", false)) {
            tv_update_firmware.setVisibility(View.VISIBLE);
            start_update = true;
        }

        if (getIntent().getBooleanExtra("reactivate_device", false)) {
            mActivateCommandGiven = true;
        }
    }

    private boolean checkLocationEnabled() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (!gps_enabled) {

            // Custom notification added by Haaris
            // custom dialog
            final Dialog dialog = new Dialog(DeviceInfoActivity.this);
            dialog.setContentView(R.layout.notification_dialog_box);

            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

            TextView notification_title = dialog.findViewById(R.id.notification_box_title);
            TextView notification_message = dialog.findViewById(R.id.notification_box_message);

            Button Notification_Button_ok = (Button) dialog.findViewById(R.id.notification_ButtonOK);
            Button Notification_Button_cancel = (Button) dialog.findViewById(R.id.notification_ButtonCancel);

            Notification_Button_ok.setText("Settings");
            Notification_Button_cancel.setText("Cancel");

            // Setting up the notification dialog
            notification_title.setText("Location is turned OFF");
            notification_message.setText("Please turn on location to scan and connect Pheezee");

            // On click on Continue
            Notification_Button_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
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

            dialog.show();
            dialog.getWindow().setAttributes(lp);

            // End

        }
        return gps_enabled;
    }


    public void getLastLocationOfDevice() {
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(DeviceInfoActivity.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();

                        }
                    }
                });
    }

    private boolean hasLocationPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==REQUEST_FINE_LOCATION){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                getLastLocationOfDevice();
                if(mService!=null){
                    if(mService.isScanning() || !deviceMacc.equalsIgnoreCase("")){
                        mService.stopScaninBackground();
                        mService.startScanInBackground();
                    }
                }
            }
        }
    }


    private boolean hasPermissions() {
        if (!hasLocationPermissions()) {
            requestLocationPermission();
            return false;
        }
        return true;
    }

    public void addPheezeeDevice(View view){
        if(deviceMacc.equalsIgnoreCase("")) {
            if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
                Log.i("Location_status:", "working");
                final Dialog dialog = new Dialog(DeviceInfoActivity.this);
                dialog.setContentView(R.layout.notification_dialog_box);

                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(dialog.getWindow().getAttributes());
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

                dialog.getWindow().setAttributes(lp);

                TextView notification_title = dialog.findViewById(R.id.notification_box_title);
                TextView notification_message = dialog.findViewById(R.id.notification_box_message);

                Button Notification_Button_ok = (Button) dialog.findViewById(R.id.notification_ButtonOK);
                Button Notification_Button_cancel = (Button) dialog.findViewById(R.id.notification_ButtonCancel);

                Notification_Button_ok.setText("Yes");
                Notification_Button_cancel.setText("No");

                // Setting up the notification dialog
                notification_title.setText("Location permission request");
                notification_message.setText("Pheezee app needs location permission \n to connect Pheezee device");

                // On click on Continue
                Notification_Button_ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (ContextCompat.checkSelfPermission(DeviceInfoActivity.this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                            dialog.dismiss();

                        }
                    }
                });
                // On click Cancel
                Notification_Button_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();

                    }
                });

                dialog.show();
            } else if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (checkLocationEnabled()) {
                    AddDevicePopupWindow feedback = new AddDevicePopupWindow(DeviceInfoActivity.this, deviceMacc, false, "Pheezee", preferences, mService);
                    feedback.showWindow();
                }

            }

        } else {
            showForgetDeviceDialog_common();
        }

    }

    /**
     * For photo editing of patient
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 1
        if(requestCode==5){

        }
        else if(requestCode==12) {
            if (resultCode == RESULT_OK) {
                String macAddress = data.getStringExtra("macAddress");
                if (RegexOperations.validate(macAddress)) {
                    if (mService != null) {
                        mService.updatePheezeeMac(macAddress);
                        mService.connectDevice(macAddress);
                        deviceMacc = macAddress;
                        editor = preferences.edit();
                        editor.putString("deviceMacaddress", macAddress);
                        editor.commit();
                        // tv_connect_to_pheezee.setText(R.string.turn_on_device);
                        showToast("Connecting, please wait..");
                    }
                }
            }
            finish();
        }


    }


    public void showForgetDeviceDialog(){

        // Custom notification added by Haaris
        // custom dialog
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.notification_dialog_box);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        TextView notification_title = dialog.findViewById(R.id.notification_box_title);
        TextView notification_message = dialog.findViewById(R.id.notification_box_message);

        Button Notification_Button_ok = (Button) dialog.findViewById(R.id.notification_ButtonOK);
        Button Notification_Button_cancel = (Button) dialog.findViewById(R.id.notification_ButtonCancel);

        Notification_Button_ok.setText("Continue");
        Notification_Button_cancel.setText("Cancel");

        // Setting up the notification dialog
        notification_title.setText("Add New Pheezee");
        notification_message.setText("Adding new Pheezee will disconnect the existing device. Do you want to Continue?");

        // On click on Continue
        Notification_Button_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor = preferences.edit();
                editor.putString("deviceMacaddress","");
                editor.apply();
                if(mService!=null){
                    mService.forgetPheezee();
                    mService.disconnectDevice();
//                                    editor = preferences.edit();
//                editor.putString("deviceMacaddress","");
//                editor.apply();
//                if(mService!=null){
//                    mService.forgetPheezee();
//                    mService.disconnectDevice();
//                }
                    refreshView();
//                tv_disconnect_forget.setText("");
                }
                enableScanningTheDevices();
                dialog.dismiss();

                to_scan_devices_activity = new Intent(DeviceInfoActivity.this, ScanDevicesActivity.class);
                startActivityForResult(to_scan_devices_activity, 12);
            }
        });
        // On click Cancel
        Notification_Button_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

            }
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);

        // End


    }


    public void showForgetDeviceDialog_common(){

        // Custom notification added by Haaris
        // custom dialog
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.notification_dialog_box);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        dialog.getWindow().setAttributes(lp);

        TextView notification_title = dialog.findViewById(R.id.notification_box_title);
        TextView notification_message = dialog.findViewById(R.id.notification_box_message);

        Button Notification_Button_ok = (Button) dialog.findViewById(R.id.notification_ButtonOK);
        Button Notification_Button_cancel = (Button) dialog.findViewById(R.id.notification_ButtonCancel);

        Notification_Button_ok.setText("Yes");
        Notification_Button_cancel.setText("No");

        // Setting up the notification dialog
        notification_title.setText("Forget Device");
        notification_message.setText("Are you sure you want to forget the current device?");

        // On click on Continue
        Notification_Button_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    editor = preferences.edit();
                    editor.putString("deviceMacaddress", "");
                    editor.apply();
                    if (mService != null) {
                        mService.forgetPheezee();
                        mService.disconnectDevice();
                    }

                    enableScanningTheDevices();
                    dialog.dismiss();
                    deviceMacc = "";
                    refreshView();
                    AddDevicePopupWindow feedback = new AddDevicePopupWindow(DeviceInfoActivity.this, deviceMacc, false, "Pheezee", preferences, mService);
                    feedback.showWindow();
                }

        });
        // On click Cancel
        Notification_Button_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

            }
        });

        dialog.show();

        // End


    }



    private void calibrateDeviceDialog(){
        if(mDeviceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = getLayoutInflater();
            View dialogLayout = inflater.inflate(R.layout.dialog_calibrate_device, null);
            btn_start_calibration = dialogLayout.findViewById(R.id.btn_start_calibration);
            btn_cancel_calibration = dialogLayout.findViewById(R.id.btn_cancel_calibrate);
            tv_status_calibrate = dialogLayout.findViewById(R.id.tv_status_calibrating);
            calib_anim = dialogLayout.findViewById(R.id.calibration_anim);
            builder.setView(dialogLayout);
            builder.setCancelable(false);
            dialog_calibrate = builder.create();
            dialog_calibrate.setCancelable(true);
            dialog_calibrate.show();
            btn_cancel_calibration.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cancelCalibrate();
                    if (dialog_calibrate != null)
                        dialog_calibrate.dismiss();
                }
            });
            btn_start_calibration.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (btn_start_calibration.getText().toString().equalsIgnoreCase("start")) {
//                        SharedPreferences.Editor device_id = editor.putString("deviceMacaddress", "");
//                        deviceMacc = "";
                        String email_id = json_phizioemail;
                        String date_stamp = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                        String time_stamp = new SimpleDateFormat("HH:mm:ss",Locale.getDefault()).format(new Date());
                        repository.cal(email_id,date_stamp,time_stamp);

                        tv_status_calibrate.setText("Calibrating");
                        tv_status_calibrate.setTextColor(getResources().getColor(R.color.pitch_black));

                        btn_start_calibration.setText("Stop");
                        dialog_calibrate.setCancelable(false);
                        calib_anim.playAnimation();
                        calibrateDevice();
                    } else {
                        dialog_calibrate.setCancelable(true);
                        tv_status_calibrate.setText("Calibration Failed. Try Again.");
                        tv_status_calibrate.setTextColor(getResources().getColor(R.color.red));
                        btn_start_calibration.setText("Start");
                        if(calib_anim.isAnimating()){
                            calib_anim.cancelAnimation();
                        }
                        cancelCalibrate();
                    }
                }
            });
        }else {
            showToast("Pheezee not connected, please connect and try again");
        }
    }

    private void reactivateDevice() {
        if(NetworkOperations.isNetworkAvailable(this)) {
            if (mDeviceState) {
                if (mService != null) {
                    byte[] info_packet = mService.getInfoPacket();
                    if (info_packet != null) {
                        if (repository != null) {
                            mActivateCommandGiven = true;
                            mCheckReactivationDialog.setMessage("Checking Device State from server");
                            mCheckReactivationDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                            mCheckReactivationDialog.setIndeterminate(true);
                            mCheckReactivationDialog.show();
                            repository.getDeviceStatus(info_packet);
                            repository.setOnDeviceStatusResponse(new MqttSyncRepository.onDeviceStatusResponse() {
                                @Override
                                public void onDeviceStatusResponse(boolean response, boolean status) {
                                    if (response) {
                                        if (status) {
                                            if (mCheckReactivationDialog.isShowing() && mService != null) {
                                                if (mDeviceState) {
                                                    mCheckReactivationDialog.setMessage("Reactivating device, please wait..");
                                                    mService.reactivateDevice();
                                                } else {
                                                    showToast("Pheezee not connected, please connect and try again");
                                                }
                                            }

                                        } else {
                                            mCheckReactivationDialog.dismiss();
                                            showDeviceDeactivatedDialog("Pheezee Deactivated", "The device is still deactivated, please contact us at care@startoonlabs.com");
                                        }
                                    } else {
                                        mCheckReactivationDialog.dismiss();
                                        showToast("Please try again later");
                                    }
                                }
                            });
                        }
                    }
                }
            } else {
                showToast("Pheezee not connected, please connect and try again");
            }
        }else {
            NetworkOperations.networkError(this);
        }
    }

    Runnable calibration_time_runnable = new Runnable() {
        @Override
        public void run() {
            if(mDeviceState){
                if(mService!=null){
                    mService.disableNotificationOfSession();
                }
            }
            if(dialog_calibrate!=null && dialog_calibrate.isShowing()){
                dialog_calibrate.setCancelable(true);
                if(btn_start_calibration!=null){
                    btn_start_calibration.setText("Start");
                    if(calib_anim!=null){
                        calib_anim.cancelAnimation();
                    }
                }if(tv_status_calibrate!=null){
                    if(tv_status_calibrate.getText().toString().contains("Calibrating")){
                        tv_status_calibrate.setText("Calibration failed, try again later!");
                        tv_status_calibrate.setTextColor(getResources().getColor(R.color.red));
                    }
                }
            }
        }
    };


    private void calibrateDevice(){
        if(mDeviceState){
            if(mService!=null){
                mService.writeCalibrationToCustomCharacteristic();
                calibration_handler.postDelayed(calibration_time_runnable,32000);
            }
        } else {
            showToast("Pheezee not connected, please connect and try again");
        }
    }

    private void cancelCalibrate(){
        calibration_handler.removeCallbacks(calibration_time_runnable);
        if(mDeviceState){
            if(mService!=null){
                mService.disableNotificationOfSession();
            }
        }
    }

    private void startFirmwareUpdate(){
        if(mDeviceState) {
            String message = getResources().getString(R.string.instructions_dfu);
            String instruction = getResources().getString(R.string.instructions2_dfu);
            if(mDfuDialog!=null){
                mDfuDialog.dismiss();
            }
            // custom dialog
            final Dialog dialog = new Dialog(DeviceInfoActivity.this);
            dialog.setContentView(R.layout.notification_dialog_box_instruction);

            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

            TextView notification_title = dialog.findViewById(R.id.notification_box_title);
            TextView notification_message = dialog.findViewById(R.id.notification_box_message);
            TextView notification_instructions = dialog.findViewById(R.id.notification_box_instruction);


            Button Notification_Button_ok = (Button) dialog.findViewById(R.id.notification_ButtonOK);
            Button Notification_Button_cancel = (Button) dialog.findViewById(R.id.notification_ButtonCancel);

            Notification_Button_ok.setText("Continue");
            Notification_Button_cancel.setText("Cancel");

            // Setting up the notification dialog
            notification_title.setText("Instructions");
            notification_message.setText(message);
            notification_instructions.setText(instruction);


            // On click on Continue
            Notification_Button_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                        dialog.dismiss();
                        BatteryManager bm = (BatteryManager) getSystemService(BATTERY_SERVICE);
                        int batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
                        if (batLevel < 30 && device_baterry_level < 30) {
                            dfuStatusDialog("Pheezee Update Failed","Low battery identified.\n Please charge both your phone and Pheezee and try again.");
                        } else if (batLevel < 30) {
                            dfuStatusDialog("Pheezee Update Failed","Low battery identified.\n Please charge your phone and try again.");
                        } else if (device_baterry_level < 30) {
                            dfuStatusDialog("Pheezee Update Failed","Low battery identified.\n Please charge Pheezee and try again.");
                        } else {
                            String str = tv_update_firmware.getText().toString();
                            if (str.equalsIgnoreCase("Update available")) {
                                if (mService != null) {
                                    mService.writeToDfuCharacteristic();
                                }
                            } else {
                                if (controller != null) {
                                    if (isDfuServiceRunning()) {
                                        showUploadCancelDialog();
                                    }
                                }
                            }
                        }

                    }


            });
            // On click Cancel
            Notification_Button_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();

                }
            });

            dialog.show();
            dialog.getWindow().setAttributes(lp);

            // End

        }else{
            showToast("Please connect Pheezee");
        }

    }


    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            isBound = true;
            PheezeeBleService.LocalBinder mLocalBinder = (PheezeeBleService.LocalBinder)service;
            mService = mLocalBinder.getServiceInstance();
            mService.gerDeviceInfo();
            device_baterry_level = mService.getDeviceBatteryLevel();
            mDeviceState = mService.getDeviceState();
            if(start_update){
                tv_update_firmware.performClick();
            }
            if(mActivateCommandGiven){
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        reactivateDevice();
                    }
                },100);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
            mService = null;
        }
    };




    @Override
    protected void onDestroy() {
        super.onDestroy();
        calibration_handler.removeCallbacks(calibration_time_runnable);
        if(mService!=null){
            mService.disableNotificationOfSession();
        }
        if(isBound){
            unbindService(mConnection);
        }
        unregisterReceiver(device_info_receiver);
        if(controller!=null){
            if(isDfuServiceRunning()){
                controller.abort();
            }
        }
        if(dialog_calibrate!=null){
            if(dialog_calibrate.isShowing())
                dialog_calibrate.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        if(!isDfuServiceRunning())
            super.onBackPressed();
        else {
//            showUploadCancelDialog();
        }
    }

    /**
     * Refresh the view with null string once disconnected
     */
    public void refreshView(){
        tv_device_name.setText(R.string.device_null);
        tv_device_mamc.setText(R.string.device_null);
        tv_firmware_version.setText(R.string.device_null);
        tv_serial_id.setText(R.string.device_null);
        tv_battery_level.setText(R.string.device_null);
        tv_connection_status.setText(R.string.device_not_connected);
        if(deviceMacc.equals("")) {
            my_device_image.setImageResource(R.drawable.my_device_pheezee);
        }else my_device_image.setImageResource(R.drawable.my_device_image_disconnected);
//        tv_disconnect_forget.setText("");
        tv_hardware_version.setText(R.string.device_null);
    }



    BroadcastReceiver device_info_receiver = new BroadcastReceiver() {
        @SuppressLint("ResourceAsColor")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            assert action != null;
            if(action.equalsIgnoreCase(device_state)){
                boolean device_status = intent.getBooleanExtra(device_state,false);
                if(device_status){
                    tv_connection_status.setText("Connected");
                    my_device_image.setImageResource(R.drawable.my_device_image_connected);
                    tv_deviceinfo_device_connected.setVisibility(View.GONE);
                    tv_deviceinfo_device_disconnected.setVisibility(View.INVISIBLE);
                    mDeviceState = true;
                }else {
                    tv_calibrate_device.setVisibility(View.GONE);
                    if(dialog_calibrate!=null && dialog_calibrate.isShowing()){
                        dialog_calibrate.dismiss();
                        dfuStatusDialog("Calibration Failed","Please connect Pheezee and try again.");
                    }
                    tv_update_firmware.setVisibility(View.GONE);
                    tv_connection_status.setText("Not Connected");
                    tv_deviceinfo_device_connected.setVisibility(View.VISIBLE);
                    tv_deviceinfo_device_disconnected.setVisibility(View.INVISIBLE);
                    if(deviceMacc.equals("")) {
                        my_device_image.setImageResource(R.drawable.my_device_pheezee);
                    }else my_device_image.setImageResource(R.drawable.my_device_image_disconnected);
                    mDeviceState = false;
                }
            }else if(action.equalsIgnoreCase(bluetooth_state)){
                boolean ble_state = intent.getBooleanExtra(bluetooth_state,false);
                if(ble_state){
                }else {
                    if(inside_bootloader){
                        if(isDfuServiceRunning()){
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    dfuCanceledView();
                                    controller.abort();
                                    // if this activity is still open and upload process was completed, cancel the notification
                                    final NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                    manager.cancel(DfuService.NOTIFICATION_ID);
                                }
                            }, 200);

                            dfuStatusDialogfail("Update Failed","Please turn on mobile bluetooth and try again.");
                        }
                    }
                    tv_connection_status.setText("Not Connected");
                    if(deviceMacc.equals("")) {
                        my_device_image.setImageResource(R.drawable.my_device_pheezee);
                    }else my_device_image.setImageResource(R.drawable.my_device_image_disconnected);
                }
            }else if(action.equalsIgnoreCase(usb_state)){
                boolean usb_status = intent.getBooleanExtra(usb_state,false);
                if(usb_status){
                }else {
                }
            }else if(action.equalsIgnoreCase(battery_percent)){
                device_baterry_level = Integer.parseInt(intent.getStringExtra(battery_percent));
                String percent = intent.getStringExtra(battery_percent);
                if(device_baterry_level==0){
                    if(mDeviceState)
                        tv_battery_level.setText(percent.concat("%"));
                    else
                        tv_battery_level.setText("Null");
                }else {
                    tv_battery_level.setText(percent.concat("%"));
                }

            }else if(action.equalsIgnoreCase(PheezeeBleService.firmware_version)){
                String firmwareVersion = intent.getStringExtra(PheezeeBleService.firmware_version);
                if(!Objects.requireNonNull(preferences.getString("firmware_update", "")).equalsIgnoreCase("")
                        && !preferences.getString("firmware_version","").equalsIgnoreCase(firmwareVersion)){
                    tv_update_firmware.setVisibility(View.VISIBLE);
                }
                String atiny_version = intent.getStringExtra(PheezeeBleService.atiny_version);
                tv_firmware_version.setText(firmwareVersion.concat(";").concat(atiny_version));
            }else if(action.equalsIgnoreCase(serial_id)){
                String serial = intent.getStringExtra(serial_id);
                tv_serial_id.setText("Serial ID : "+serial);
            }else if(action.equalsIgnoreCase(manufacturer_name)){
                String manufacturer = intent.getStringExtra(manufacturer_name);
                tv_device_name.setText(manufacturer);
            }else if(action.equalsIgnoreCase(df_characteristic_written)){
                startDfuService();
            }else if(action.equalsIgnoreCase(hardware_version)){
                String hardwareVersion = intent.getStringExtra(hardware_version);
                tv_hardware_version.setText(hardwareVersion);
            }else if(action.equalsIgnoreCase(device_disconnected_firmware)){
                boolean device_disconnected_status = intent.getBooleanExtra(device_disconnected_firmware,false);
                if(device_disconnected_status){
                    mDeviceDeactivated = true;
                    tv_reactivate_device.setVisibility(View.VISIBLE);
                }else {
                    mDeviceDeactivated = false;
                    tv_reactivate_device.setVisibility(View.GONE);
                    if(mCheckReactivationDialog!=null && mCheckReactivationDialog.isShowing()){
                        mCheckReactivationDialog.dismiss();
                    }
                    if(mActivateCommandGiven) {
                        showDeviceDeactivatedDialog("Device Activated", "Congratulations, the device has been reactivated.");
                        mActivateCommandGiven = false;
                    }
                }
            }else if(action.equalsIgnoreCase(calibration_state)){
                boolean calibration_status = intent.getBooleanExtra(calibration_state,false);
                if(mService!=null){
                    mService.disableNotificationOfSession();
                    calibration_handler.removeCallbacks(calibration_time_runnable);
                }
                if(calibration_status){
                    if(tv_status_calibrate!=null){
                        tv_status_calibrate.setText("Calibration successfull!");
                        tv_status_calibrate.setTextColor(getResources().getColor(R.color.background_green));
                    }if (btn_start_calibration!=null){
                        btn_start_calibration.setText("Start");
                        if(calib_anim!=null){
                            calib_anim.cancelAnimation();
                        }
                    }
                }else {
                    if(tv_status_calibrate!=null){
                        tv_status_calibrate.setText("Calibration failed, try again later!");
                        tv_status_calibrate.setTextColor(getResources().getColor(R.color.red));
                    }if (btn_start_calibration!=null){
                        btn_start_calibration.setText("Start");
                        if(calib_anim!=null){
                            calib_anim.cancelAnimation();
                        }
                    }
                }
                if(dialog_calibrate!=null && dialog_calibrate.isShowing()){
                    dialog_calibrate.setCancelable(true);
                }
            }else if(action.equalsIgnoreCase(magnetometer_present)){
                boolean magnetometer_status = intent.getBooleanExtra(magnetometer_present,false);
                if(magnetometer_status){
                    tv_calibrate_device.setVisibility(View.VISIBLE);
                }else {
                    tv_calibrate_device.setVisibility(View.GONE);
                }
            }
//            else if(action.equalsIgnoreCase(device_deactivated)){
//                mDeviceDeactivated = true;
//                tv_reactivate_device.setVisibility(View.VISIBLE);
//            }
        }
    };

    private void dfuCanceledView() {
        ll_dfu.setVisibility(View.GONE);
        mTextPercentage.setText("");
        mTextUploading.setText("");
        mProgressBar.setProgress(0);

        if(dfu_popupwindow!=null)
        {
            dfu_popupwindow.dismiss();
            dfu_popupwindow=null;
        }
    }

    @Override
    public void onCancelUpload() {
        mProgressBar.setIndeterminate(true);
        mTextUploading.setText(R.string.dfu_status_aborting);
        mTextPercentage.setText(null);
    }


    private void showUploadCancelDialog() {
        final LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        final Intent pauseAction = new Intent(DfuService.BROADCAST_ACTION);
        pauseAction.putExtra(DfuService.EXTRA_ACTION, DfuService.ACTION_PAUSE);
        manager.sendBroadcast(pauseAction);

        final UploadCancelFragment fragment = UploadCancelFragment.getInstance();
        fragment.show(getSupportFragmentManager(), TAG);
    }

    private void showProgressBar() {
        mProgressBar.setVisibility(View.VISIBLE);
        mTextPercentage.setVisibility(View.VISIBLE);
        mTextPercentage.setText(null);
        mTextUploading.setText(R.string.dfu_status_uploading);
        mTextUploading.setVisibility(View.VISIBLE);
    }

    private void showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    private void enableScanningTheDevices() {

        deviceMacc="";
    }


    //DFU SERVICE LISTNER
    private final DfuProgressListener mDfuProgressListener = new DfuProgressListenerAdapter() {
        @Override
        public void onDeviceConnecting(final String deviceAddress) {
            mProgressBar.setIndeterminate(true);
            mTextPercentage.setText(R.string.dfu_status_connecting);
            inside_bootloader = false;
            dfu_popupwindow.connecting();

        }

        @Override
        public void onDeviceConnected(String deviceAddress) {
            mProgressBar.setIndeterminate(true);
//            mTextPercentage.setText(R.string.dfu_device_connected);
            inside_bootloader = false;
        }

        @Override
        public void onDfuProcessStarted(String deviceAddress) {
            mProgressBar.setIndeterminate(true);
//            mTextPercentage.setText(R.string.dfu_started);
            inside_bootloader = false;

        }


        @Override
        public void onDeviceDisconnected(String deviceAddress) {
            mProgressBar.setIndeterminate(true);
            mTextPercentage.setText(R.string.dfu_device_disconnected);
            inside_bootloader = false;
//            Log.i("here","Disconnected");
            if(mService!=null){
                mService.showNotification("Device Disconnected");
            }
        }

        @Override
        public void onDfuProcessStarting(final String deviceAddress) {

            // This callback function is called 2-times during the DFU update.
            // To make sure the popup window instance is only created once we are using this.
            if(dfu_popupwindow==null)
            {
                // Showing DFU popupwindow
                dfu_popupwindow = new DFUPopupWindow(DeviceInfoActivity.this);
                dfu_popupwindow.showWindow();
            }

            tv_update_firmware.setVisibility(View.INVISIBLE);
            mProgressBar.setIndeterminate(true);
            mTextPercentage.setText(R.string.dfu_switching_to_dfu);
            inside_bootloader = false;
            if(mService!=null){
                mService.showNotification("Updating device");
            }



        }

        @Override
        public void onEnablingDfuMode(final String deviceAddress) {
            mProgressBar.setIndeterminate(true);
            mTextPercentage.setText(R.string.starting_bootloader);
            inside_bootloader = true;
        }

        @Override
        public void onFirmwareValidating(final String deviceAddress) {
            mProgressBar.setIndeterminate(true);
            mTextPercentage.setText(R.string.dfu_status_validating);
        }

        @Override
        public void onDeviceDisconnecting(final String deviceAddress) {
            mProgressBar.setIndeterminate(true);
            mTextPercentage.setText(R.string.dfu_status_disconnecting);
            inside_bootloader = false;
            if(mService!=null){
                mService.showNotification("Device Disconnecting");
            }
        }

        @Override
        public void onDfuCompleted(final String deviceAddress) {
            inside_bootloader = false;
            dfu_popupwindow.successfull();
            tv_update_firmware.setVisibility(View.GONE);
            mTextPercentage.setText(R.string.dfu_status_completed);
            dfuStatusDialogsucessfull("Pheezee Updated",getResources().getString(R.string.dfu_successfull)+" "+preferences.getString("firmware_version",""));
            editor = preferences.edit();
            editor.putString("firmware_update","");
            editor.putString("firmware_version", "");
            editor.apply();
            // let's wait a bit until we cancel the notification. When canceled immediately it will be recreated by service again.
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    dfuCanceledView();
                    showToast(getResources().getString(R.string.firmware_updated));

                    // if this activity is still open and upload process was completed, cancel the notification
                    final NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.cancel(DfuService.NOTIFICATION_ID);
                }
            }, 200);
            if(mService!=null){
                mService.showNotification("Device updated");
            }
        }

        @Override
        public void onDfuAborted(final String deviceAddress) {
            inside_bootloader = false;
            tv_update_firmware.setText("Update available");
            tv_update_firmware.setVisibility(View.VISIBLE);
            mTextPercentage.setText(R.string.dfu_status_aborted);
            if(mDeviceState){
                dfuStatusDialog("Device Update Aborted", "The device update has been aborted, please try again later");
            }else {
                dfuStatusDialog("Device Update Aborted", "The device update has been aborted, please reset Pheezee and try again later");
            }
            // let's wait a bit until we cancel the notification. When canceled immediately it will be recreated by service again.
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    dfuCanceledView();
//                    showToast(getResources().getString(R.string.dfu_aborted));

                    // if this activity is still open and upload process was completed, cancel the notification
                    final NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.cancel(DfuService.NOTIFICATION_ID);
                }
            }, 200);
            if(mService!=null){
                if(!mService.getDeviceState())
                    mService.showNotification("Device update was aborted");
            }
        }

        @Override
        public void onProgressChanged(final String deviceAddress, final int percent, final float speed, final float avgSpeed, final int currentPart, final int partsTotal) {
            inside_bootloader = false;
            dfu_popupwindow.updating(percent);
            mProgressBar.setIndeterminate(false);
            mProgressBar.setProgress(percent);
            mTextPercentage.setText(getString(R.string.dfu_uploading_percentage, percent));
            if (partsTotal > 1)
                mTextUploading.setText(getString(R.string.dfu_status_uploading_part, currentPart, partsTotal));
            else
                mTextUploading.setText(R.string.dfu_status_uploading);
        }

        @Override
        public void onError(final String deviceAddress, final int error, final int errorType, final String message) {
            inside_bootloader = false;
            dfuCanceledView();
            if(error== DfuBaseService.ERROR_BLUETOOTH_DISABLED){
                dfuStatusDialogfail("Update Failed","Please turn on mobile bluetooth and try again");
            }
            else if( error==DfuBaseService.ERROR_DEVICE_DISCONNECTED){
                dfuStatusDialogfail("Update Failed","Reset Pheezee and try again");
            }else if(errorType==2){
                dfuStatusDialogfail("Update Failed","Reset Pheezee and try again");
            }
            tv_update_firmware.setText("Update available");
            tv_update_firmware.setVisibility(View.VISIBLE);
//            showToast("Error :"+message);
            // We have to wait a bit before canceling notification. This is called before DfuService creates the last notification.
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // if this activity is still open and upload process was completed, cancel the notification
                    final NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.cancel(DfuService.NOTIFICATION_ID);
                }
            }, 200);
            if(mService!=null){
                mService.showNotification("Device Not Connected");
            }
        }
    };

    private  void dfuStatusDialogsucessfull(String title, String message){
        if(mDialog!=null){
            mDialog.dismiss();
        }

        // Custom notification added by Haaris
        // custom dialog
        final Dialog dialog = new Dialog(DeviceInfoActivity.this);
        dialog.setContentView(R.layout.dfu_successful_notification_box);


        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        TextView notification_title = dialog.findViewById(R.id.notification_box_title);
        TextView notification_message = dialog.findViewById(R.id.notification_box_message);

        Button Notification_Button_ok = (Button) dialog.findViewById(R.id.notification_ButtonOK);

        Notification_Button_ok.setText("Okay");

        // Setting up the notification dialog
        notification_title.setText(title);
        notification_message.setText(message);

        // On click on Continue
        Notification_Button_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if(dfu_popupwindow!=null)
                {
                    dfu_popupwindow.dismiss();
                    dfu_popupwindow=null;
                }

            }
        });


        dialog.show();
        dialog.getWindow().setAttributes(lp);

        // End


    }

    private  void dfuStatusDialogfail(String title, String message){
        if(mDialog!=null){
            mDialog.dismiss();
        }

        if(dfu_popupwindow!=null)
        {
            dfu_popupwindow.dismiss();
            dfu_popupwindow=null;
        }

        // Custom notification added by Haaris
        // custom dialog
        final Dialog dialog = new Dialog(DeviceInfoActivity.this);
        dialog.setContentView(R.layout.dfu_fail_notification_box);


        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        TextView notification_title = dialog.findViewById(R.id.notification_box_title);
        TextView notification_message = dialog.findViewById(R.id.notification_box_message);

        Button Notification_Button_ok = (Button) dialog.findViewById(R.id.notification_ButtonOK);
        Button Notification_Button_cancel = (Button) dialog.findViewById(R.id.notification_ButtonCancel);

        Notification_Button_ok.setText("Restart");

        // Setting up the notification dialog
        notification_title.setText(title);
        notification_message.setText(message);

        // On click on Continue
        Notification_Button_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

            }
        });
        // On click on Continue
        Notification_Button_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

            }
        });


        dialog.show();
        dialog.getWindow().setAttributes(lp);

        // End


    }

    private  void dfuStatusDialog(String title, String message){
        if(mDialog!=null){
            mDialog.dismiss();
        }

        // Custom notification added by Haaris
        // custom dialog
        final Dialog dialog = new Dialog(DeviceInfoActivity.this);
        dialog.setContentView(R.layout.notification_dialog_box_single_button);


        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        TextView notification_title = dialog.findViewById(R.id.notification_box_title);
        TextView notification_message = dialog.findViewById(R.id.notification_box_message);

        Button Notification_Button_ok = (Button) dialog.findViewById(R.id.notification_ButtonOK);

        Notification_Button_ok.setText("Okay");

        // Setting up the notification dialog
        notification_title.setText(title);
        notification_message.setText(message);

        // On click on Continue
        Notification_Button_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

            }
        });


        dialog.show();
        dialog.getWindow().setAttributes(lp);

        // End


    }
    @Override
    protected void onResume() {
        super.onResume();
        DfuServiceListenerHelper.registerProgressListener(this,mDfuProgressListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        DfuServiceListenerHelper.unregisterProgressListener(this, mDfuProgressListener);
    }

    private void startDfuService(){
        if(Build.VERSION.SDK_INT>=30)
            DfuServiceInitiator.createDfuNotificationChannel(this);
        if (isDfuServiceRunning()) {
            showUploadCancelDialog();
            return;
        }
//        ll_dfu.setVisibility(View.VISIBLE);
        showProgressBar();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String mFilePath = "";
                Uri mFileStreamUri;

                // Moves the current Thread into the background
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

                HttpURLConnection httpURLConnection = null;
                byte[] buffer = new byte[2048];
                try {
                    //Your http connection
                    httpURLConnection = (HttpURLConnection) new URL(preferences.getString("firmware_update","")).openConnection();

                    //Change below path to Environment.getExternalStorageDirectory() or something of your
                    // own by creating storage utils
                    File zip = new File(Environment.getExternalStorageDirectory()+"/Pheezee/firmware/");
                    if(!zip.exists())
                        zip.mkdir();
//                            File file = new File(zip, "latest.zip");
//                            file.createNewFile();

                    ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(httpURLConnection.getInputStream()));
                    ZipEntry zipEntry = zipInputStream.getNextEntry();

                    int readLength;

                    while(zipEntry != null){
                        File newFile = new File(zip, zipEntry.getName());
                        String canonicalPath = newFile.getCanonicalPath();
                        if(!canonicalPath.startsWith(zip.getAbsolutePath())){
                            showToast("Please try again later");
                            dfuCanceledView();
                            return;
                        }else {
                            if (!zipEntry.isDirectory()) {
                                FileOutputStream fos = new FileOutputStream(newFile);
                                while ((readLength = zipInputStream.read(buffer)) > 0) {
                                    fos.write(buffer, 0, readLength);
                                }
                                fos.close();
                            } else {
                                newFile.mkdirs();
                            }

                            zipInputStream.closeEntry();
                            zipEntry = zipInputStream.getNextEntry();
                        }
                    }
                    // Close Stream and disconnect HTTP connection. Move to finally
                    zipInputStream.closeEntry();
                    zipInputStream.close();
                } catch (IOException | NullPointerException e) {
                    e.printStackTrace();
                    dfuCanceledView();
                    return;
                } finally {
                    if (httpURLConnection != null) {
                        httpURLConnection.disconnect();
                        ZipOperations.zipFolder(new File(Environment.getExternalStorageDirectory()+"/Pheezee/firmware/"));
                        File file = new File(Environment.getExternalStorageDirectory()+"/Pheezee/firmware.zip");
                        if(file!=null) {
                            mFilePath = Environment.getExternalStorageDirectory() + "/Pheezee/firmware.zip";
                            mFileStreamUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/Pheezee/firmware.zip"));


                            if(mService!=null) {
                                final DfuServiceInitiator starter = new DfuServiceInitiator(mService.getMacAddress())
                                        .setDeviceName(mService.getDeviceName())
                                        .setKeepBond(false);
                                starter.setZip(mFileStreamUri, mFilePath);
                                controller = starter.start(getApplicationContext(), DfuService.class);
                            }
                        }
                    }
                }
            }
        }).start();
    }


    private boolean isDfuServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (DfuService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void showDeviceDeactivatedDialog(String title, String  message) {
        if(mDeactivatedDialog==null || !mDeactivatedDialog.isShowing()) {
            mDeactivatedDialog = new AlertDialog.Builder(this)
                    .setTitle(title)
                    .setMessage(message)
                    .show();
        }
    }
}
