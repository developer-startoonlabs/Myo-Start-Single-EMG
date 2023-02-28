package com.start.apps.pheezee.activities;

import static android.Manifest.permission.ACCESS_BACKGROUND_LOCATION;
import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS;
import static android.Manifest.permission.BLUETOOTH;
import static android.Manifest.permission.BLUETOOTH_ADMIN;
import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static android.Manifest.permission.BLUETOOTH_SCAN;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.Manifest.permission_group.CAMERA;
import static android.os.Build.VERSION.SDK_INT;

import static androidx.viewbinding.BuildConfig.VERSION_NAME;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.bluetooth.BluetoothAdapter;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import start.apps.pheezee.R;

import com.start.apps.pheezee.adapters.PatientsRecyclerViewAdapter;
import com.start.apps.pheezee.classes.MyBottomSheetDialog;
import com.start.apps.pheezee.pojos.DeletePatientData;
import com.start.apps.pheezee.pojos.PatientDetailsData;
import com.start.apps.pheezee.pojos.PatientStatusData;
import com.start.apps.pheezee.popup.AddDevicePopupWindow;
import com.start.apps.pheezee.popup.AddPatientPopUpWindow;
import com.start.apps.pheezee.popup.EditPopUpWindow;
import com.start.apps.pheezee.popup.UploadImageDialog;
import com.start.apps.pheezee.repository.MqttSyncRepository;
import com.start.apps.pheezee.room.Entity.PhizioPatients;
import com.start.apps.pheezee.services.DeviceDeactivationStatusService;
import com.start.apps.pheezee.services.DeviceDetailsService;
import com.start.apps.pheezee.services.DeviceEmailUpdateService;
import com.start.apps.pheezee.services.DeviceLocationStatusService;
import com.start.apps.pheezee.services.FirmwareLogService;
import com.start.apps.pheezee.services.HealthUpdatePresentService;
import com.start.apps.pheezee.services.PheezeeBleService;
import com.start.apps.pheezee.services.PicassoCircleTransformation;
import com.start.apps.pheezee.services.SyncDataToTheServerService;
import com.start.apps.pheezee.utils.BatteryOperation;
import com.start.apps.pheezee.utils.BitmapOperations;
import com.start.apps.pheezee.utils.DeviceErrorCodesAndDialogs;
import com.start.apps.pheezee.utils.NetworkOperations;
import com.start.apps.pheezee.utils.PackageOperations;
import com.start.apps.pheezee.utils.PackageTypes;
import com.start.apps.pheezee.utils.RegexOperations;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;


import static com.facebook.FacebookSdk.getApplicationContext;
import static com.start.apps.pheezee.services.PheezeeBleService.health_error_present_in_device;
import static com.start.apps.pheezee.services.PheezeeBleService.jobid_sync_data_to_server;
import static com.start.apps.pheezee.services.PheezeeBleService.show_device_health_error;
import static com.start.apps.pheezee.services.PheezeeBleService.usb_interrupt_check;
import static com.start.apps.pheezee.utils.PackageTypes.STANDARD_PACKAGE;
import static com.start.apps.pheezee.services.PheezeeBleService.battery_percent;
import static com.start.apps.pheezee.services.PheezeeBleService.bluetooth_state;
import static com.start.apps.pheezee.services.PheezeeBleService.deactivate_device;
import static com.start.apps.pheezee.services.PheezeeBleService.device_disconnected_firmware;
import static com.start.apps.pheezee.services.PheezeeBleService.device_state;
import static com.start.apps.pheezee.services.PheezeeBleService.firmware_log;
import static com.start.apps.pheezee.services.PheezeeBleService.firmware_update_available;
import static com.start.apps.pheezee.services.PheezeeBleService.jobid_device_details_update;
import static com.start.apps.pheezee.services.PheezeeBleService.jobid_device_status;
import static com.start.apps.pheezee.services.PheezeeBleService.jobid_firmware_log;
import static com.start.apps.pheezee.services.PheezeeBleService.jobid_health_data;
import static com.start.apps.pheezee.services.PheezeeBleService.jobid_location_status;
import static com.start.apps.pheezee.services.PheezeeBleService.jobid_user_connected_update;
import static com.start.apps.pheezee.services.PheezeeBleService.scedule_device_status_service;
import static com.start.apps.pheezee.services.PheezeeBleService.usb_state;

import android.app.Dialog;
import android.widget.Button;
import android.view.WindowManager;

public class PatientsView extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        PatientsRecyclerViewAdapter.onItemClickListner, MqttSyncRepository.onServerResponse {
    private static final int PERMISSION_REQUEST_CODE = 333;
    private static final int REQUEST_CALL = 26;
    private double latitude = 0, longitude = 0;
    private static final int REQUEST_FINE_LOCATION = 14;
    private static final int REQUEST_COARSE_LOCATION = 15;
    private static final int REQUEST_EXTERNAL_STORAGE = 16;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 17;
    public static final int REQ_CAMERA = 17;
    public static final int REQ_GALLERY = 18;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 200;
    //    private ReviewManager manager;
//    private ReviewInfo reviewInfo;
    PheezeeBleService mService;
    private boolean mDeviceState = false, mDeviceDeactivated = false, mDeviceHealthError = false, mInsideHome = true;
    boolean isBound = false;
    int REQUEST_ENABLE_BT = 1;
    public static int deviceBatteryPercent = -1;
    private int[] firmware_version = {-1, -1, -1};
    View patientLayoutView;
    MyBottomSheetDialog myBottomSheetDialog;
    ProgressDialog connecting_device_dialog;

    final CharSequence[] peezee_items = {"Scan for nearby Pheezee devices",
            "Qrcode Scan", "Cancel"};
    TextView email, fullName, tv_start_clinic_session;
    public static ImageView ivBasicImage;
    //new
    JSONObject json_phizio = new JSONObject();
    Intent to_scan_devices_activity;
    private PatientsRecyclerViewAdapter mAdapter;
    TextView tv_battery_percentage, tv_patient_view_add_patient;
    ProgressBar battery_bar;
    int backpressCount = 0;
    DrawerLayout drawer;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    AlertDialog.Builder builder;
    LinearLayout patientTabLayout;
    ImageView iv_addPatient, DialogCloseButton;
    Button edit_profile_btn;
    private String deviceMacc = "";
    LinearLayout add_device_bar;
    RelativeLayout rl_cap_view;
    RelativeLayout rl_battery_usb_state;
    RecyclerView mRecyclerView;
    ProgressDialog progress, deletepatient_progress;
    SearchView searchView;
    MqttSyncRepository repository;
    public static String json_phizioemail = "kranthikiran7702@gmail.com";
    public static int phizio_packagetype = 0;
    public static int patient_limit = 200;
    ConstraintLayout cl_phizioProfileNavigation;
    TextView tv_connect_to_pheezee;
    boolean connected_state;
    boolean ble_status_global;
    AddDevicePopupWindow feedback = null;
    public static int patientsize;
    boolean usb_state_var = false;

    //bluetooth and device connection state
    ImageView iv_bluetooth_connected, iv_bluetooth_disconnected, iv_device_connected, iv_device_disconnected, iv_device, iv_sync_data, iv_sync_not_available;

    AlertDialog mDialog, mDeactivatedDialog;
    LinearLayout ll_profile_update;
    Dialog dialog;
    private Timer timer;
    private Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patients_view);


        initializeView();
        getFirmwareIntentIfPresent();
        getPhizioDetails();
        setNavigation();
        setInitialMaccIfPresent();
//        checkPermissionsRequired();
        checkAndRequestPermissions();
        // requestPermission();
        getLastLocationOfDevice();
        checkLocationEnabled();
        setAllListners();
        setBluetoothInfoBroadcastReceiver();
        startBluetoothService();
        boundToBluetoothService();
        chekFirmwareLogPresentAndSrartService();
        chekHealthStatusLogPresentAndSrartService();
        registerFirmwareUpdateReceiver();
        subscribeFirebaseFirmwareUpdateTopic();
        checkAndSyncDataToTheServer();


        deviceMacc = sharedPref.getString("deviceMacaddress", "");

        iv_device_connected.setVisibility(View.GONE);

        if (deviceMacc == "") {
            iv_device_disconnected.setVisibility(View.GONE);
            iv_device.setVisibility(View.VISIBLE);

        } else {

            iv_device_disconnected.setVisibility(View.VISIBLE);
            iv_device.setVisibility(View.GONE);
        }
        profile_update_popup();
        Pheezee_app_version_send();


    }


    private void Pheezee_app_version_send() {
        if (NetworkOperations.isNetworkAvailable(PatientsView.this)) {
            repository.updateApp_version(json_phizioemail, "Local Server Laptop");
        }

    }

    private void profile_update_popup() {

        boolean profile_update = false;


        try {

            if (json_phizio.getString("clinicname").equalsIgnoreCase("")) {
                profile_update = true;
            }
            if (json_phizio.getString("phiziodob").equalsIgnoreCase("")) {
                profile_update = true;
            }
            if (json_phizio.getString("experience").equalsIgnoreCase("")) {
                profile_update = true;
            }
            if (json_phizio.getString("specialization").equalsIgnoreCase("")) {
                profile_update = true;
            }
            if (json_phizio.getString("degree").equalsIgnoreCase("")) {
                profile_update = true;
            }
            if (json_phizio.getString("address").equalsIgnoreCase("")) {
                profile_update = true;
                Log.d("clinichecking", "lapogba");
            }
            if (json_phizio.getString("cliniclogo").equalsIgnoreCase("")) {
                profile_update = true;
            }


        } catch (JSONException e) {
            e.printStackTrace();
            profile_update = true;

        }
        if (profile_update) {
            ll_profile_update.setVisibility(View.VISIBLE);
        } else ll_profile_update.setVisibility(View.GONE);
    }

    private void checkAndSyncDataToTheServer() {
        ComponentName componentName = new ComponentName(this, SyncDataToTheServerService.class);
        JobInfo.Builder info = new JobInfo.Builder(jobid_sync_data_to_server, componentName);
        info.setMinimumLatency(1000);
        info.setOverrideDeadline(3000);
        info.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        info.setRequiresCharging(false);
        JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(info.build());
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
            final Dialog dialog = new Dialog(PatientsView.this);
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

            // End


        }
        return gps_enabled;
    }

    private void getFirmwareIntentIfPresent() {
        if (getIntent().getExtras() != null) {
            if (getIntent().getStringExtra("downloadlink") != null
                    && !getIntent().getStringExtra("downloadlink").equalsIgnoreCase("")) {
                editor = sharedPref.edit();
                editor.putString("firmware_update", getIntent().getStringExtra("downloadlink"));
                editor.apply();
            }
        }
    }

    private void subscribeFirebaseFirmwareUpdateTopic() {
        FirebaseMessaging.getInstance().subscribeToTopic("ota")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "subscribed";
                        if (!task.isSuccessful()) {
                            msg = "subscription failed";
                        }
                    }
                });
    }

//    private void rewardFirebaseUpdateTopic() {
//        FirebaseMessaging.getInstance().subscribeToTopic("phiziomail")
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        Toast.makeText(getApplicationContext(),"your",Toast.LENGTH_LONG).show();
//                    }
//                });
//    }


    private void chekFirmwareLogPresentAndSrartService() {
        if (!Objects.requireNonNull(sharedPref.getString("firmware_log", "")).equalsIgnoreCase("")) {
            ComponentName componentName = new ComponentName(this, FirmwareLogService.class);
            JobInfo.Builder info = new JobInfo.Builder(jobid_firmware_log, componentName);
            info.setMinimumLatency(1000);
            info.setOverrideDeadline(3000);
            info.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
            info.setRequiresCharging(false);
            JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
            jobScheduler.schedule(info.build());
        }
    }

    private void chekHealthStatusLogPresentAndSrartService() {
        if (!Objects.requireNonNull(sharedPref.getString("health_data", "")).equalsIgnoreCase("")) {
            ComponentName componentName = new ComponentName(this, HealthUpdatePresentService.class);
            JobInfo.Builder info = new JobInfo.Builder(jobid_health_data, componentName);
            info.setMinimumLatency(1000);
            info.setOverrideDeadline(3000);
            info.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
            info.setRequiresCharging(false);
            JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
            jobScheduler.schedule(info.build());
        }
    }

    private void chekDeviceLocationStatusLogPresentAndSrartService() {
        if (!Objects.requireNonNull(sharedPref.getString("device_location_data", "")).equalsIgnoreCase("")) {
            ComponentName componentName = new ComponentName(this, DeviceLocationStatusService.class);
            JobInfo.Builder info = new JobInfo.Builder(jobid_location_status, componentName);
            info.setMinimumLatency(1000);
            info.setOverrideDeadline(3000);
            info.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
            info.setRequiresCharging(false);
            JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
            jobScheduler.schedule(info.build());
        }
    }

    private void chekDeviceDetailsStatusLogPresentAndSrartService() {
        if (!Objects.requireNonNull(sharedPref.getString("device_details_data", "")).equalsIgnoreCase("")) {
            ComponentName componentName = new ComponentName(this, DeviceDetailsService.class);
            JobInfo.Builder info = new JobInfo.Builder(jobid_device_details_update, componentName);
            info.setMinimumLatency(1000);
            info.setOverrideDeadline(3000);
            info.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
            info.setRequiresCharging(false);
            JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
            jobScheduler.schedule(info.build());
        }
    }

    private void chekDeviceEmailDetailsStatusLogPresentAndSrartService() {
        if (!Objects.requireNonNull(sharedPref.getString("device_email_data", "")).equalsIgnoreCase("")) {
            ComponentName componentName = new ComponentName(this, DeviceEmailUpdateService.class);
            JobInfo.Builder info = new JobInfo.Builder(jobid_user_connected_update, componentName);
            info.setMinimumLatency(1000);
            info.setOverrideDeadline(3000);
            info.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
            info.setRequiresCharging(false);
            JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
            jobScheduler.schedule(info.build());
        }
    }

    private void chekDeviceStatusLogPresentAndSrartService() {
        if (!Objects.requireNonNull(sharedPref.getString("uid_deactivation", "")).equalsIgnoreCase("")) {
            ComponentName componentName = new ComponentName(this, DeviceDeactivationStatusService.class);
            JobInfo.Builder info = new JobInfo.Builder(jobid_device_status, componentName);
            info.setMinimumLatency(1000);
            info.setOverrideDeadline(3000);
            info.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
            info.setRequiresCharging(false);
            JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
            jobScheduler.schedule(info.build());
        }
    }

    private void boundToBluetoothService() {
        Intent mIntent = new Intent(this, PheezeeBleService.class);
//        bindService(mIntent, mConnection, BIND_AUTO_CREATE);
    }

    private void startBluetoothService() {
        ContextCompat.startForegroundService(this, new Intent(this, PheezeeBleService.class));
    }

    private void setBluetoothInfoBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(device_state);
        intentFilter.addAction(bluetooth_state);
        intentFilter.addAction(usb_state);
        intentFilter.addAction(battery_percent);
        intentFilter.addAction(PheezeeBleService.firmware_version);
        intentFilter.addAction(PheezeeBleService.firmware_log);
        intentFilter.addAction(PheezeeBleService.health_status);
        intentFilter.addAction(PheezeeBleService.location_status);
        intentFilter.addAction(PheezeeBleService.device_details_status);
        intentFilter.addAction(PheezeeBleService.device_details_email);
        intentFilter.addAction(device_disconnected_firmware);
        intentFilter.addAction(scedule_device_status_service);
        intentFilter.addAction(deactivate_device);
        intentFilter.addAction(show_device_health_error);
        intentFilter.addAction(health_error_present_in_device);
        registerReceiver(patient_view_broadcast_receiver, intentFilter);
    }

    private void registerFirmwareUpdateReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(firmware_update_available);
        registerReceiver(firmware_update_receiver, intentFilter);
    }

    private void setAllListners() {
        iv_sync_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetworkOperations.isNetworkAvailable(PatientsView.this)) {
                    progress = new ProgressDialog(PatientsView.this);
                    progress.setMessage("Syncing session data to the server");
                    progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progress.setIndeterminate(true);
                    progress.setCancelable(false);
                    progress.show();
                    repository.syncDataToServer();
                } else
                    NetworkOperations.networkError(PatientsView.this);
            }
        });
        cl_phizioProfileNavigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PatientsView.this, PhizioProfile.class));
            }
        });

        DialogCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ll_profile_update.setVisibility(View.GONE);
            }
        });

        edit_profile_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetworkOperations.isNetworkAvailable(PatientsView.this)) {
                    Intent i = new Intent(PatientsView.this, EditProfileActivity.class);
                    i.putExtra("et_phizio_email", json_phizioemail);
                    startActivityForResult(i, 31);
                } else {
                    NetworkOperations.networkError(PatientsView.this);
                }

            }
        });

        try {
            email.setText(json_phizioemail);
            fullName.setText("Dr. " + json_phizio.getString("phizioname"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ivBasicImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PatientsView.this, PhizioProfile.class));
            }
        });

        repository.getAllPatietns().observe(this, new Observer<List<PhizioPatients>>() {
            @Override
            public void onChanged(List<PhizioPatients> patients) {
                if (patients.size() > 0) {
                    if (phizio_packagetype != STANDARD_PACKAGE) {
                        findViewById(R.id.noPatient).setVisibility(View.GONE);
                        findViewById(R.id.cl_recycler_view).setVisibility(View.VISIBLE);
                        tv_start_clinic_session.setVisibility(View.GONE);
                        iv_addPatient.setImageDrawable(getResources().getDrawable(R.mipmap.ic_add_patient));
                        tv_patient_view_add_patient.setTextColor(getResources().getColor(R.color.good_green));
                    } else {
                        findViewById(R.id.cl_recycler_view).setVisibility(View.GONE);
                        findViewById(R.id.noPatient).setVisibility(View.VISIBLE);
                        tv_start_clinic_session.setVisibility(View.VISIBLE);
                        iv_addPatient.setImageDrawable(getResources().getDrawable(R.mipmap.ic_add_patient_grey));
                        tv_patient_view_add_patient.setTextColor(getResources().getColor(R.color.white));
                    }
                } else {
                    if (phizio_packagetype != STANDARD_PACKAGE) {
                        findViewById(R.id.cl_recycler_view).setVisibility(View.GONE);
                        findViewById(R.id.noPatient).setVisibility(View.VISIBLE);
                        tv_start_clinic_session.setVisibility(View.GONE);
                        iv_addPatient.setImageDrawable(getResources().getDrawable(R.mipmap.ic_add_patient));
                        tv_patient_view_add_patient.setTextColor(getResources().getColor(R.color.good_green));
                    } else {
                        findViewById(R.id.cl_recycler_view).setVisibility(View.GONE);
                        findViewById(R.id.noPatient).setVisibility(View.VISIBLE);
                        tv_start_clinic_session.setVisibility(View.VISIBLE);
                        iv_addPatient.setImageDrawable(getResources().getDrawable(R.mipmap.ic_add_patient_grey));
                        tv_patient_view_add_patient.setTextColor(getResources().getColor(R.color.white));
                    }
                }

                Collections.reverse(patients);
                patientsize = patients.size();
                mAdapter.setNotes(patients);
            }
        });


        //Add device and bluetooth turn on click events
        add_device_bar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPheezeeDevice(v);

            }
        });


        iv_addPatient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initiatePopupWindow();
            }
        });


        tv_patient_view_add_patient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initiatePopupWindow();
            }
        });


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                mAdapter.getFilter().filter(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                mAdapter.getFilter().filter(s);
                return false;
            }
        });

        repository.getCount().observe(this,
                new Observer<Long>() {
                    @Override
                    public void onChanged(@Nullable Long mqttSyncs) {
                        try {
                            if (mqttSyncs != null && mqttSyncs > 0) {
                                iv_sync_not_available.setVisibility(View.INVISIBLE);//GONE
                                iv_sync_data.setVisibility(View.INVISIBLE);
                                checkAndSyncDataToTheServer();
                            } else {
                                iv_sync_data.setVisibility(View.INVISIBLE);
                                iv_sync_not_available.setVisibility(View.INVISIBLE);
                            }
                        } catch (NullPointerException e) {
                        }
                    }
                });


        tv_start_clinic_session.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSession("-", "-", "-");
            }
        });

        mAdapter.setOnItemClickListner(this);
    }


    private void getPhizioDetails() {

//        // Timer pass setting Enable
//        if (timer != null) {
//            timer.cancel();
//            Log.i("Main", "cancel timer");
//            timer = null;
//        }

        //Getting previous patient data
        try {
            json_phizio = new JSONObject(sharedPref.getString("phiziodetails", ""));
            json_phizioemail = json_phizio.getString("phizioemail");
            phizio_packagetype = json_phizio.getInt("packagetype");
            patient_limit = json_phizio.getInt("patientlimit");

            if (NetworkOperations.isNetworkAvailable(this)) {
                repository.sendFirebaseTopkenToTheServer(json_phizioemail, FirebaseInstanceId.getInstance().getToken());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i("Testing_Service:", "Working Fine");

    }


    @Override
    protected void onResume() {
        super.onResume();


        mInsideHome = true;
        try {
            json_phizio = new JSONObject(sharedPref.getString("phiziodetails", ""));
            email.setText(json_phizioemail);
            fullName.setText("Dr. " + json_phizio.getString("phizioname"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (mService != null) {
            mService.gerDeviceBasicInfo();
        }
        registerFirmwareUpdateReceiver();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timer = new Timer();
        Log.i("Time_Testing", "Timer Started for Logout");
        LogOutTimerTask logoutTimeTask = new LogOutTimerTask();
        timer.schedule(logoutTimeTask, 1); //auto logout in 1 minutes

        mInsideHome = false;
        unregisterReceiver(firmware_update_receiver);

        if (isBound) {
            unbindService(mConnection);
        }
        unregisterReceiver(patient_view_broadcast_receiver);
        Log.i("STOPING SERVICE", "SERVICE");
        stopService(new Intent(this, PheezeeBleService.class));
    }

    @Override
    public void onBackPressed() {

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
        notification_title.setText("Exit Notification");
        notification_message.setText("Are you sure you want to Exit the App?");

        // On click on Continue
        Notification_Button_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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


//    @Override
//    public void onBackPressed() {
//// TODO Auto-generated method stub
//        AlertDialog.Builder builder=new AlertDialog.Builder(PatientsView.this);
//        // builder.setCancelable(false);
//        builder.setTitle("Rate Us if u like this");
//        builder.setMessage("Do you want to Exit?");
//        builder.setPositiveButton("yes",new DialogInterface.OnClickListener() {
//
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                // TODO Auto-generated method stub
//                Toast.makeText(PatientsView.this, "Yes i wanna exit", Toast.LENGTH_LONG).show();
//
//                finish();
//            }
//        });
//        builder.setNegativeButton("No",new DialogInterface.OnClickListener() {
//
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                // TODO Auto-generated method stub
//                Toast.makeText(PatientsView.this, "i wanna stay on this page", Toast.LENGTH_LONG).show();
//                dialog.cancel();
//
//            }
//        });
////        builder.setNeutralButton("Rate",new DialogInterface.OnClickListener() {
////
//////            @Override
//////            public void onClick(DialogInterface dialog, int which) {
//////                // TODO Auto-generated method stub
//////
//////                final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
//////                try {
//////                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
//////                } catch (android.content.ActivityNotFoundException ) {
//////                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
//////                }
//////
//////            }
////        });
//        AlertDialog alert=builder.create();
//        alert.show();
//        //super.onBackPressed();
//    }

//    @Override
//    public void onBackPressed() {
//        DrawerLayout drawer = findViewById(R.id.drawer_layout);
//        if (drawer.isDrawerOpen(GravityCompat.START)) {
//            drawer.closeDrawer(GravityCompat.START);
//        } else {
//            backpressCount++;
//            if (backpressCount == 1) {
////                 Toast.makeText(PatientsView.this, "Press again to close Pheezee app", Toast.LENGTH_SHORT).show();
//
//
//                Thread thread = new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            Thread.sleep(1000);
//                            backpressCount = 0;
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                });
//                thread.start();
//            }
//            if (backpressCount == 2) {
//                Intent intent = new Intent(Intent.ACTION_MAIN);
//                intent.addCategory(Intent.CATEGORY_HOME);
//                startActivity(intent);
//                finishAffinity();
//            }
//        }
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.patients_view, menu);
        Log.i("STOPING SERVICE", "START SERVICES");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.pheeze_device_info) {
            Intent i = new Intent(PatientsView.this, DeviceInfoActivity.class);
            i.putExtra("deviceMacAddress", sharedPref.getString("deviceMacaddress", ""));
            i.putExtra("start_update", false);
            i.putExtra("reactivate_device", false);
            startActivityForResult(i, 13);
        } else if (id == R.id.nav_rate) {
            rateApp();
//                rateApp();
        } else if (id == R.id.nav_customer) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=918790896481&text=Hello Team")));


//            }
//        else if (id == R.id.nav_add_patient) {
//            initiatePopupWindow();
//        } else if (id == R.id.nav_app_version) {
//            startActivity(new Intent(PatientsView.this, AppInfo.class));
        } else if (id == R.id.my_account) {
            startActivity(new Intent(PatientsView.this, MyAccountPannel.class));
        } else if (id == R.id.nav_logout) {
            //Logout Pop Code
//                AlertDialog.Builder builder=new AlertDialog.Builder(PatientsView.this); //Home is name of the activity
//                builder.setMessage("Do you want to exit?");
//                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int id) {
//
//                        finish();
//                        Intent i=new Intent();
//                        i.putExtra("finish", true);
//                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // To clean up all activities
//                        //startActivity(i);
//                        finish();
//
//                    }
//                });
//
//                builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int id) {
//                        dialog.cancel();
//                    }
//                });
//
//                AlertDialog alert=builder.create();
//                alert.show();


            editor = sharedPref.edit();
            editor.clear();
            editor.commit();
            repository.clearDatabase();
            repository.deleteAllSync();
            FirebaseMessaging.getInstance().unsubscribeFromTopic("ota");
            startActivity(new Intent(this, LoginActivity.class));
            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancelAll();
            File reportPdf = null;


            // For deleting all the locally saved PDFs on logout.
//            reportPdf = new File(Environment.getExternalStorageDirectory() + "/Pheezee/files/reports");
            reportPdf = new File(getApplicationContext().getExternalFilesDir(null) + "/Pheezee");
            if (reportPdf.exists() && reportPdf.isDirectory()) {
                //write same defination for it.

                if (reportPdf.isDirectory()) {
                    String[] children = reportPdf.list();
                    for (int i = 0; i < children.length; i++) {
                        new File(reportPdf, children[i]).delete();
                    }
                }
            }

            finish();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public void drawSideBar(View view) {
        drawer.openDrawer(GravityCompat.START);
    }

    /**
     * Bluetooth disconnected
     */
    private void bluetoothDisconnected() {
        iv_bluetooth_disconnected.setVisibility(View.INVISIBLE);
        iv_bluetooth_connected.setVisibility(View.GONE);
    }

    /**
     * Bluetooth connected
     */
    private void bluetoothConnected() {
        iv_bluetooth_disconnected.setVisibility(View.GONE);
        iv_bluetooth_connected.setVisibility(View.INVISIBLE);
    }


    public void rateApp() {
        try {
            Intent rateIntent = rateIntentForUrl("market://details");
            startActivity(rateIntent);
        } catch (ActivityNotFoundException e) {
            Intent rateIntent = rateIntentForUrl("https://play.google.com/store/apps/details");
            startActivity(rateIntent);
        }
    }

    private Intent rateIntentForUrl(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("%s?id=%s", url, getPackageName())));
        int flags = Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK;
        flags |= Intent.FLAG_ACTIVITY_NEW_DOCUMENT;
        intent.addFlags(flags);
        return intent;
    }


    private void showpop() {


        ReviewManager manager = ReviewManagerFactory.create(PatientsView.this);
        Task<ReviewInfo> request = manager.requestReviewFlow();
        request.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.e("Status: ", "Working");
                ReviewInfo reviewInfo = task.getResult();
                Task<Void> flow = manager.launchReviewFlow(PatientsView.this, reviewInfo);
                flow.addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void result) {

                    }
                });


            } else {
                Log.e("Status: ", "Not Working");
            }
        });
    }

    private void initiatePopupWindow() {
        if (phizio_packagetype != STANDARD_PACKAGE) {
            if (mAdapter.getItemCount() < patient_limit) {
                AddPatientPopUpWindow patientPopUpWindow = new AddPatientPopUpWindow(this, json_phizioemail);
                patientPopUpWindow.openAddPatientPopUpWindow();
                patientPopUpWindow.setOnClickListner(new AddPatientPopUpWindow.onClickListner() {
                    @Override
                    public void onAddPatientClickListner(PhizioPatients patient, PatientDetailsData data, boolean isvalid, Bitmap photo) {
                        if (isvalid) {
                            repository.insertPatient(patient, data);
                        } else {
                            showToast("Please fill all details");
                        }
                    }
                });
            } else {
                try {
                    PackageTypes.showPatientAddingReachedDialog(this, json_phizioemail, phizio_packagetype, json_phizio.getString("phizioname"), json_phizio.getString("phiziophone"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } else {
            try {
                PackageOperations.featureNotAvailable(this, json_phizioemail, phizio_packagetype, json_phizio.getString("phizioname"), json_phizio.getString("phiziophone"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }


    public void addPheezeeDevice(View view) {

        if (!ble_status_global) {
            startBluetoothRequest();
        }

//        if(!hasLocationPermissions()) {
//            requestLocationPermission();
//        }
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            Log.i("Location_status:", "working");
            final Dialog dialog = new Dialog(PatientsView.this);
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
                    if (ContextCompat.checkSelfPermission(PatientsView.this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
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
            Log.d("Check", "permission_Granted");
            if (ble_status_global && checkLocationEnabled()) {
                deviceMacc = sharedPref.getString("deviceMacaddress", "");
                feedback = new AddDevicePopupWindow(PatientsView.this, deviceMacc, connected_state, "Pheezee", sharedPref, mService, usb_state_var);
                feedback.showWindow();
            }
        }


    }


    public void showForgetDeviceDialog() {

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
                editor = sharedPref.edit();
                editor.putString("deviceMacaddress", "");
                editor.apply();
                if (mService != null) {
                    mService.forgetPheezee();
                    mService.disconnectDevice();
                }
                iv_device_connected.setVisibility(View.GONE);
                iv_device_disconnected.setVisibility(View.GONE);
                iv_device.setVisibility(View.VISIBLE);
                enableScanningTheDevices();
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

        // End


    }


    /**
     * @param patient
     */
    public void editPopUpWindow(PhizioPatients patient) {
        EditPopUpWindow popUpWindow = new EditPopUpWindow(this, patient, json_phizioemail);
        popUpWindow.openEditPopUpWindow();
        popUpWindow.setOnClickListner(new EditPopUpWindow.onClickListner() {
            @Override
            public void onAddClickListner(PhizioPatients patients, PatientDetailsData data, boolean isvalid, Bitmap photo) {
                if (isvalid) {
                    deletepatient_progress = new ProgressDialog(PatientsView.this);
                    deletepatient_progress.setTitle("Updating patient details, please wait");
                    deletepatient_progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    deletepatient_progress.setIndeterminate(true);
                    deletepatient_progress.show();
                    repository.updatePatientDetailsServer(patient, data);
                } else {
                    showToast("Please fill all details");
                }
            }
        });
    }

    /**
     * Called when device connects to update the view
     */
    private void pheezeeConnected() {
        connected_state = true;
        iv_device_disconnected.setVisibility(View.GONE);
        iv_device.setVisibility(View.GONE);
        iv_device_connected.setVisibility(View.VISIBLE);
        Drawable drawable = getResources().getDrawable(R.drawable.drawable_progress_battery);
        battery_bar.setProgressDrawable(drawable);
        @SuppressLint("ResourceAsColor") Drawable drawable_cap = new ColorDrawable(R.color.battery_gray);
        rl_cap_view.setBackground(drawable_cap);
        if (feedback != null && feedback.ispopupshowing()) {
            feedback.UpdateWindow(PatientsView.this, deviceMacc, connected_state, "Pheezee", sharedPref, mService);
            feedback.refreshwindow();

        }
    }

    /**
     * called when device disconnected to update the view
     */
    private void pheezeeDisconnected() {
        connected_state = false;
        deviceMacc = sharedPref.getString("deviceMacaddress", "");

        iv_device_connected.setVisibility(View.GONE);

        if (deviceMacc == "") {
            iv_device_disconnected.setVisibility(View.GONE);
            iv_device.setVisibility(View.VISIBLE);

        } else {

            iv_device_disconnected.setVisibility(View.VISIBLE);
            iv_device.setVisibility(View.GONE);
        }


        Drawable drawable = getResources().getDrawable(R.drawable.drawable_progress_battery_disconnected);
        battery_bar.setProgressDrawable(drawable);
        @SuppressLint("ResourceAsColor") Drawable drawable_cap = new ColorDrawable(getResources().getColor(R.color.red));
        rl_cap_view.setBackground(drawable_cap);
        rl_battery_usb_state.setVisibility(View.GONE);
        if (feedback != null && feedback.ispopupshowing()) {
            feedback.UpdateWindow(PatientsView.this, deviceMacc, connected_state, "Pheezee", sharedPref, mService);
            feedback.refreshwindow();

        }
    }


    /**
     * @param patientid
     * @param patientname
     * @param dateofjoin
     */
    public void startSession(String patientid, String patientname, String dateofjoin) {
        Intent intent = new Intent(PatientsView.this, BodyPartSelection.class);
        intent.putExtra("deviceMacAddress", sharedPref.getString("deviceMacaddress", ""));
        intent.putExtra("patientId", patientid);
        intent.putExtra("patientName", patientname);
        intent.putExtra("dateofjoin", dateofjoin);
        if (Objects.requireNonNull(sharedPref.getString("deviceMacaddress", "")).equals("") && !mDeviceState) {
            Toast.makeText(this, "First add Pheezee to your application", Toast.LENGTH_LONG).show();
        } else if (!(iv_device_connected.getVisibility() == View.VISIBLE)) {
            Toast.makeText(this, "First add Pheezee to your application", Toast.LENGTH_LONG).show();
        } else {
            if (deviceBatteryPercent < 15) {

                // Custom notification added by Haaris
                // custom dialog

                String message = BatteryOperation.getDialogMessageForLowBattery(deviceBatteryPercent, this);

                final Dialog dialog = new Dialog(PatientsView.this);
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
                        dialog.dismiss();

                        boolean flag = true;
                        if (firmware_version[0] < 1) {
                            flag = false;
                        } else if (firmware_version[1] < 11 && firmware_version[0] <= 1) {
                            flag = false;
                        } else if (firmware_version[2] < 4 && firmware_version[1] <= 11) {
                            flag = false;
                        } else {
                            flag = true;
                        }

                        if (!flag) {
                            if (firmware_version[0] == -1 && firmware_version[1] == -1 && firmware_version[2] == -1) {
                                NetworkOperations.servicesNotDiscovered(PatientsView.this);
                            } else {
                                NetworkOperations.firmwareVirsionNotCompatible(PatientsView.this);
                            }
                        } else {
                            if (!mDeviceDeactivated && !mDeviceHealthError)
                                startActivity(intent);
                            else {
                                if (mDeviceDeactivated)
                                    showDeviceDeactivatedDialog();
                                else {
                                    if (mService != null) {
                                        DeviceErrorCodesAndDialogs.showDeviceErrorDialog(mService.getHealthErrorString(), PatientsView.this);
                                    }
                                }
                            }
                        }


                    }
                });


                dialog.show();

            } else {
                boolean flag = true;
                if (firmware_version[0] < 1) {
                    flag = false;
                } else if (firmware_version[1] < 11 && firmware_version[0] <= 1) {
                    flag = false;
                } else if (firmware_version[2] < 4 && firmware_version[1] <= 11) {
                    flag = false;
                } else {
                    flag = true;
                }

                if (!flag) {
                    if (firmware_version[0] == -1 && firmware_version[1] == -1 && firmware_version[2] == -1) {
                        NetworkOperations.servicesNotDiscovered(PatientsView.this);
                    } else {
                        NetworkOperations.firmwareVirsionNotCompatible(PatientsView.this);
                    }
                } else {
                    if (!mDeviceDeactivated && !mDeviceHealthError)
                        startActivity(intent);
                    else {
                        if (mDeviceDeactivated)
                            showDeviceDeactivatedDialog();
                        else {
                            if (mService != null) {
                                DeviceErrorCodesAndDialogs.showDeviceErrorDialog(mService.getHealthErrorString(), this);
                            }
                        }
                    }
                }
            }
        }

    }

    /**
     * Updating the image of patient
     *
     * @param view
     */
    public void chooseImageUpdateAction(final View view) {
        patientLayoutView = view;
        UploadImageDialog dialog = new UploadImageDialog(this, 5, 6);
        dialog.showDialog();
    }

    /**
     * Opens the bottom bar sheet
     *
     * @param view
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("ResourceType")
    public void openOpionsPopupWindow(View view, PhizioPatients patient) {
        Bitmap patientpic_bitmap = null;
        patientTabLayout = (LinearLayout) (view).getParent();
        LinearLayout iv_layout = (LinearLayout) patientTabLayout.getChildAt(0);

        ImageView iv_patient_pic = iv_layout.findViewById(R.id.patientProfilePic);

        if (!(iv_patient_pic.getDrawable() == null)) {
            try {
                patientpic_bitmap = ((BitmapDrawable) iv_patient_pic.getDrawable()).getBitmap();
            } catch (ClassCastException e) {
                patientpic_bitmap = null;
            }
        }
        myBottomSheetDialog = new MyBottomSheetDialog(patientpic_bitmap, patient);
        myBottomSheetDialog.show(getSupportFragmentManager(), "MyBottomSheet");

    }


    public void editThePatientDetails(PhizioPatients patient) {
        myBottomSheetDialog.dismiss();
        if (NetworkOperations.isNetworkAvailable(PatientsView.this))
            editPopUpWindow(patient);
        else {
            NetworkOperations.networkError(PatientsView.this);
        }
    }

    /**
     * @param patientid
     * @param patientname
     */
    public void openReportActivity(String patientid, String patientname, String dateofjoin) {
        if (NetworkOperations.isNetworkAvailable(PatientsView.this)) {
            Intent mmt_intent = new Intent(PatientsView.this, SessionReportActivity.class);
            mmt_intent.putExtra("patientid", patientid);
            mmt_intent.putExtra("patientname", patientname);
            mmt_intent.putExtra("dateofjoin", dateofjoin);
            try {
                mmt_intent.putExtra("phizioemail", json_phizio.getString("phizioemail"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            startActivity(mmt_intent);
            myBottomSheetDialog.dismiss();
        } else {
            NetworkOperations.networkError(PatientsView.this);
        }
    }


    /**
     * @param patient
     */
    public void updatePatientStatus(PhizioPatients patient) {
        myBottomSheetDialog.dismiss();
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Archive Patient");
        builder.setMessage("Are you sure you want to archive the patient?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (NetworkOperations.isNetworkAvailable(PatientsView.this)) {
                    patient.setStatus("inactive");
                    deletepatient_progress = new ProgressDialog(PatientsView.this);
                    deletepatient_progress.setTitle("Updating patient status, please wait");
                    deletepatient_progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    deletepatient_progress.setIndeterminate(true);
                    deletepatient_progress.show();
                    PatientStatusData data = new PatientStatusData(json_phizioemail, patient.getPatientid(), patient.getStatus());
                    repository.updatePatientStatusServer(patient, data);
                } else {
                    NetworkOperations.networkError(PatientsView.this);
                }
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }


    /**
     * @param patient
     */
    public void deletePatient(PhizioPatients patient) {
        myBottomSheetDialog.dismiss();

        // Custom notification added by Haaris
        // custom dialog
        final Dialog dialog = new Dialog(PatientsView.this);
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
        notification_title.setText("Delete Patient");
        notification_message.setText("Are you sure you want to delete the patient?");

        // On click on Continue
        Notification_Button_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetworkOperations.isNetworkAvailable(PatientsView.this)) {
                    deletepatient_progress = new ProgressDialog(PatientsView.this);
                    deletepatient_progress.setTitle("Deleting patient, please wait");
                    deletepatient_progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    deletepatient_progress.setIndeterminate(true);
                    deletepatient_progress.show();
                    DeletePatientData data = new DeletePatientData(json_phizioemail, patient.getPatientid());
                    repository.deletePatientFromServer(data, patient);
                } else {
                    NetworkOperations.networkError(PatientsView.this);
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

        dialog.show();

        // End

    }


    /**
     * For photo editing of patient
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 2296) {
            if (SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    // perform action when allow permission success
                } else {
                    Toast.makeText(this, "Allow permission for storage access!", Toast.LENGTH_SHORT).show();
                }
            }
        }

        // check if the request code is same as what is passed  here it is 1
        if (requestCode == 21) {
            if (resultCode == RESULT_OK) {
                Bitmap photo = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
                photo = BitmapOperations.getResizedBitmap(photo, 128);
//                imageView_patientpic.setImageBitmap(photo);
                LayoutInflater inflater = getLayoutInflater();
                View dialogLayout = inflater.inflate(R.layout.popup, null);
                final TextView tv_create_account = dialogLayout.findViewById(R.id.tv_create_account);
                AddPatientPopUpWindow patientPopUpWindow = new AddPatientPopUpWindow(this, json_phizioemail, photo);
                patientPopUpWindow.openAddPatientPopUpWindow();
                patientPopUpWindow.setOnClickListner(new AddPatientPopUpWindow.onClickListner() {
                    @Override
                    public void onAddPatientClickListner(PhizioPatients patient, PatientDetailsData data, boolean isvalid, Bitmap photo) {
                        if (isvalid) {
                            repository.insertPatient(patient, data);
                            Log.d("upload", patient.getPatientid());
                            repository.uploadPatientImage(patient.getPatientid(), json_phizioemail, photo);
                        } else {
                            showToast("Invalid Input!");
                        }
                    }
                });

            }
        }
        if (requestCode == 22) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    Uri selectedImage = data.getData();
                    Bitmap photo = null;
                    try {
                        photo = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                        photo = BitmapOperations.getResizedBitmap(photo, 128);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    AddPatientPopUpWindow patientPopUpWindow = new AddPatientPopUpWindow(this, json_phizioemail, photo);
                    patientPopUpWindow.openAddPatientPopUpWindow();
                    patientPopUpWindow.setOnClickListner(new AddPatientPopUpWindow.onClickListner() {
                        @Override
                        public void onAddPatientClickListner(PhizioPatients patient, PatientDetailsData data, boolean isvalid, Bitmap photo) {
                            if (isvalid) {
                                repository.insertPatient(patient, data);
                                Log.d("upload", patient.getPatientid());
                                repository.uploadPatientImage(patient.getPatientid(), json_phizioemail, photo);
                            } else {
                                showToast("Invalid Input!");
                            }
                        }
                    });
                }

            }
        }
        if (requestCode == 41) {
            if (resultCode == RESULT_OK) {
                Bitmap photo = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
                photo = BitmapOperations.getResizedBitmap(photo, 128);
//                imageView_patientpic.setImageBitmap(photo);
                LayoutInflater inflater = getLayoutInflater();
                View dialogLayout = inflater.inflate(R.layout.popup, null);
                final TextView tv_create_account = dialogLayout.findViewById(R.id.tv_create_account);
                EditPopUpWindow patientPopUpWindow = new EditPopUpWindow(this, json_phizioemail, photo);
                patientPopUpWindow.openEditPopUpWindow();
                patientPopUpWindow.setOnClickListner(new EditPopUpWindow.onClickListner() {
                    @Override
                    public void onAddClickListner(PhizioPatients patient, PatientDetailsData data, boolean isvalid, Bitmap photo) {
                        if (isvalid) {

                            Log.d("upload", patient.getPatientid());
                            repository.uploadPatientImage(patient.getPatientid(), json_phizioemail, photo);
                            deletepatient_progress = new ProgressDialog(PatientsView.this);
                            deletepatient_progress.setTitle("Updating patient details, please wait");
                            deletepatient_progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                            deletepatient_progress.setIndeterminate(true);
                            deletepatient_progress.show();
                            repository.updatePatientDetailsServer(patient, data);

                        } else {
                            showToast("Invalid Input!");
                        }
                    }
                });

            }
//            if(resultCode == RESULT_OK){
//                if(data.getStringExtra("profile_update_completed").equalsIgnoreCase("completed"))
//                {
//                    ll_profile_update.setVisibility(View.GONE);
//                }
//            }
        }
        if (requestCode == 42) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    Uri selectedImage = data.getData();
                    Bitmap photo = null;
                    try {
                        photo = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                        photo = BitmapOperations.getResizedBitmap(photo, 128);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    EditPopUpWindow patientPopUpWindow = new EditPopUpWindow(this, json_phizioemail, photo);
                    patientPopUpWindow.openEditPopUpWindow();
                    patientPopUpWindow.setOnClickListner(new EditPopUpWindow.onClickListner() {
                        @Override
                        public void onAddClickListner(PhizioPatients patient, PatientDetailsData data, boolean isvalid, Bitmap photo) {
                            if (isvalid) {

                                Log.d("upload", patient.getPatientid());
                                repository.uploadPatientImage(patient.getPatientid(), json_phizioemail, photo);
                                deletepatient_progress = new ProgressDialog(PatientsView.this);
                                deletepatient_progress.setTitle("Updating patient details, please wait");
                                deletepatient_progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                deletepatient_progress.setIndeterminate(true);
                                deletepatient_progress.show();
                                repository.updatePatientDetailsServer(patient, data);

                            } else {
                                showToast("Invalid Input!");
                            }
                        }
                    });
                }

            }
        }
        if (requestCode == 31) {
            if (resultCode == RESULT_OK) {
                if (data.getStringExtra("profile_update_completed").equalsIgnoreCase("completed")) {
                    ll_profile_update.setVisibility(View.GONE);
                }
            }
        }
        if (requestCode == 32) {
            if (resultCode == RESULT_OK) {

            }
        }
        if (requestCode == 5) {
            if (resultCode == RESULT_OK) {
                ImageView imageView_patientpic = patientLayoutView.findViewById(R.id.patientProfilePic);
                patientTabLayout = (LinearLayout) (patientLayoutView).getParent().getParent();
                patientTabLayout = (LinearLayout) patientTabLayout.getChildAt(1);
                Bitmap photo = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
                photo = BitmapOperations.getResizedBitmap(photo, 128);
                imageView_patientpic.setImageBitmap(photo);
                TextView tv_patientId = (TextView) patientTabLayout.getChildAt(2);
                if (NetworkOperations.isNetworkAvailable(this))
                    repository.uploadPatientImage(tv_patientId.getText().toString().substring(4), json_phizioemail, photo);
                else
                    NetworkOperations.networkError(this);
            }
        }
        if (requestCode == 6) {
            ImageView imageView_patientpic = patientLayoutView.findViewById(R.id.patientProfilePic);
            patientTabLayout = (LinearLayout) (patientLayoutView).getParent().getParent();
            patientTabLayout = (LinearLayout) patientTabLayout.getChildAt(1);
            if (data != null) {
                Uri selectedImage = data.getData();
                Glide.with(this).load(selectedImage).apply(new RequestOptions().centerCrop()).into(imageView_patientpic);
                TextView tv_patientId = (TextView) patientTabLayout.getChildAt(1);
                Bitmap photo = null;
                try {
                    photo = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                    photo = BitmapOperations.getResizedBitmap(photo, 128);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (NetworkOperations.isNetworkAvailable(this))
                    repository.uploadPatientImage(tv_patientId.getText().toString().substring(4), json_phizioemail, photo);
                else
                    NetworkOperations.networkError(this);
            }
        } else if (requestCode == 12) {
            if (resultCode == RESULT_OK) {
                String macAddress = data.getStringExtra("macAddress");
                if (RegexOperations.validate(macAddress)) {
                    if (mService != null) {
                        mService.updatePheezeeMac(macAddress);
                        mService.connectDevice(macAddress);
                        deviceMacc = macAddress;
                        editor = sharedPref.edit();
                        editor.putString("deviceMacaddress", macAddress);
                        editor.commit();
                        tv_connect_to_pheezee.setText(R.string.turn_on_device);
                        showToast("Connecting, please wait..");
                    }
                }
            } else if (resultCode == 2) {
                showToast("Not a mac address");
            }
        } else if (requestCode == 13) {
            if (resultCode == 13) {
                enableScanningTheDevices();
            }
        }

        if (requestCode == 2) {
            if (resultCode != 0) {
                startActivity(new Intent(this, ScanDevicesActivity.class));
            }
        }
//        if(requestCode==LocationRequest.PRIORITY_HIGH_ACCURACY) {
//            switch (resultCode) {
//                case Activity.RESULT_OK:
//                    // All required changes were successfully made
//                    break;
//                case Activity.RESULT_CANCELED:
//                    // The user was asked to change settings, but chose not to
//                    break;
//                default:
//                    break;
//            }
//        }
////        case LocationRequest.PRIORITY_HIGH_ACCURACY:
    }

    private void enableScanningTheDevices() {
        tv_connect_to_pheezee.setText(R.string.click_to_connect);
        deviceMacc = "";
    }

    private void startBluetoothRequest() {
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



    @Override
    public void onItemClick(PhizioPatients patient, View view) {
        openOpionsPopupWindow(view, patient);
    }

    @Override
    public void onStartSessionClickListner(PhizioPatients patient) {
        startSession(patient.getPatientid(), patient.getPatientname(), patient.getDateofjoin());
    }

    @Override
    public void onDeletePateintResponse(boolean response) {
        if (deletepatient_progress != null)
            deletepatient_progress.dismiss();
        if (response) {
            if (deletepatient_progress != null) {
                deletepatient_progress.dismiss();
                showToast("Patient deleted");
            }
        } else {
            showToast("Please try again later");
        }
    }

    @Override
    public void onUpdatePatientDetailsResponse(boolean response) {
        if (deletepatient_progress != null)
            deletepatient_progress.dismiss();
        if (response) {
            if (deletepatient_progress != null) {
                deletepatient_progress.dismiss();
                showToast("Patient details updated");
            }
        } else {
            showToast("Please try again later");
        }
    }

    @Override
    public void onUpdatePatientStatusResponse(boolean response) {
        if (deletepatient_progress != null)
            deletepatient_progress.dismiss();
        if (response) {
            if (deletepatient_progress != null) {
                deletepatient_progress.dismiss();
                showToast("Patient status updated");
            }
        } else {
            showToast("Please try again later");
        }
    }

    @Override
    public void onSyncComplete(boolean response, String message) {
        progress.dismiss();
        showToast(message);
    }

    /**
     * This handler handles the battery status and updates the bars of the battery symbol.
     */
    @SuppressLint("HandlerLeak")
    public final Handler batteryStatus = new Handler() {
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(Message msg) {
            tv_battery_percentage.setText(msg.obj.toString().concat("%"));
            deviceBatteryPercent = Integer.parseInt(msg.obj.toString());
            int percent = BatteryOperation.convertBatteryToCell(deviceBatteryPercent);
            if (deviceBatteryPercent < 15) {
                Drawable drawable = getResources().getDrawable(R.drawable.drawable_progress_battery_low);
                battery_bar.setProgressDrawable(drawable);
            } else {
                Drawable drawable = getResources().getDrawable(R.drawable.drawable_progress_battery);
                battery_bar.setProgressDrawable(drawable);
            }
            battery_bar.setProgress(percent);
            if (feedback != null && feedback.ispopupshowing()) {
                feedback.UpdateWindow(PatientsView.this, deviceMacc, connected_state, "Pheezee", sharedPref, mService);
                feedback.refreshwindow();

            }

            if (usb_interrupt_check == true) {
                usb_interrupt_check = false;

                final Dialog dialog = new Dialog(PatientsView.this);
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
                notification_title.setText("USB Alert");
                notification_message.setText("USB port is facing an issue. Please contact us at care@startoonlabs.com to resolve.");

                // On click on Continue
                Notification_Button_ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });


                dialog.show();
                // End
            }
        }
    };

    /**
     * This handler handels the batery state view change
     */
    @SuppressLint("HandlerLeak")
    public final Handler batteryUsbState = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.obj.toString().equalsIgnoreCase("c")) {
                rl_battery_usb_state.setVisibility(View.VISIBLE);
                usb_state_var = true;
            } else {
                rl_battery_usb_state.setVisibility(View.GONE);
                usb_state_var = false;
            }

            if (feedback != null && feedback.ispopupshowing()) {
                feedback.UpdateWindow(PatientsView.this, deviceMacc, connected_state, "Pheezee", sharedPref, mService, usb_state_var);
                feedback.refreshwindow();

            }
        }
    };


    public void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }


    BroadcastReceiver firmware_update_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            assert action != null;
            if (action.equalsIgnoreCase(firmware_update_available)) {
                boolean firmware_update_status = intent.getBooleanExtra(firmware_update_available, false);
                if (firmware_update_status && !Objects.requireNonNull(sharedPref.getString("firmware_update", "")).equalsIgnoreCase("")) {
                    showFirmwareUpdateAvailableDialog();
                }
            }
        }
    };

    private void showFirmwareUpdateAvailableDialog() {
        if (dialog == null && mDeactivatedDialog == null) {

            // Custom notification added by Haaris
            // custom dialog
            dialog = new Dialog(PatientsView.this);
            dialog.setContentView(R.layout.notification_dialog_box_single_button);

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
            notification_title.setText("Upgrade Pheezee");
            notification_message.setText("Pheezee firmware update available.\nPlease update for better experience.");


            // On click on Continue
            Notification_Button_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    Intent i = new Intent(PatientsView.this, DeviceInfoActivity.class);
                    i.putExtra("start_update", true);
                    i.putExtra("reactivate_device", false);
                    i.putExtra("deviceMacAddress", sharedPref.getString("deviceMacaddress", ""));
                    startActivityForResult(i, 13);

                }
            });

            dialog.show();

            // End


        }
    }

    private void showDeviceDeactivatedDialog() {
        if (mDeactivatedDialog == null || !mDeactivatedDialog.isShowing()) {

            // Custom notification added by Haaris
            // custom dialog
            final Dialog dialog = new Dialog(PatientsView.this);
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

            Notification_Button_ok.setText("Send Request");

            // Setting up the notification dialog
            notification_title.setText("Pheezee Deactivated");
            notification_message.setText("Pheezee has been deactivated. Please contact us at care@startoonlabs.com for reactivation");


            // On click on Continue
            Notification_Button_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(Intent.ACTION_SEND);
                    String[] recipients={"care@startoonlabs.com"};
                    String mail = json_phizioemail;
                    String macid = deviceMacc;
                    intent.putExtra(Intent.EXTRA_EMAIL, recipients);
                    intent.putExtra(Intent.EXTRA_SUBJECT,"Activate My Device");
                    intent.putExtra(Intent.EXTRA_TEXT,"My device stopped working. \n Requesting for reactivation "+ System.getProperty("line.separator") +"Login :" + mail + System.getProperty("line.separator") +"Device Mac Id:" + macid);

                    intent.setType("text/html");
                    intent.setPackage("com.google.android.gm");
                    startActivity(Intent.createChooser(intent, "Send mail"));

//                    dialog.dismiss();
//                    Intent i = new Intent(PatientsView.this, DeviceInfoActivity.class);
//                    i.putExtra("start_update", false);
//                    i.putExtra("reactivate_device", true);
//                    i.putExtra("deviceMacAddress", sharedPref.getString("deviceMacaddress", ""));
//                    startActivityForResult(i, 13);


                }
            });

            // On click on Cancel
            Notification_Button_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();


                }
            });

            dialog.show();

            // End

        }
    }


    BroadcastReceiver patient_view_broadcast_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            assert action != null;
            if (action.equalsIgnoreCase(device_state)) {
                boolean device_status = intent.getBooleanExtra(device_state, false);
                if (device_status) {
                    mDeviceState = true;
                    pheezeeConnected();
                } else {
                    mDeviceHealthError = false;
                    mDeviceDeactivated = false;
                    mDeviceState = false;
                    pheezeeDisconnected();
                    battery_bar.setProgress(0);
                }
            } else if (action.equalsIgnoreCase(bluetooth_state)) {
                boolean ble_state = intent.getBooleanExtra(bluetooth_state, false);

                if (ble_state) {
                    bluetoothConnected();
                    ble_status_global = true;
                } else {
                    mDeviceHealthError = false;
                    mDeviceDeactivated = false;
                    mDeviceState = false;
                    ble_status_global = false;
                    bluetoothDisconnected();
                }
            } else if (action.equalsIgnoreCase(usb_state)) {
                boolean usb_status = intent.getBooleanExtra(usb_state, false);
                Message msg = new Message();
                if (usb_status) {
                    msg.obj = "c";
                    batteryUsbState.sendMessage(msg);
                } else {
                    msg.obj = "nc";
                    batteryUsbState.sendMessage(msg);
                }
            } else if (action.equalsIgnoreCase(battery_percent)) {
                String percent = intent.getStringExtra(battery_percent);
                Message msg = new Message();
                msg.obj = percent;
                if (mDeviceState)
                    batteryStatus.sendMessage(msg);
            } else if (action.equalsIgnoreCase(PheezeeBleService.firmware_version)) {
                String firmwareVersion = intent.getStringExtra(PheezeeBleService.firmware_version);
                if (mDeviceState) {
                    if (!Objects.requireNonNull(sharedPref.getString("firmware_update", "")).equalsIgnoreCase("")
                            && !sharedPref.getString("firmware_version", "").equalsIgnoreCase(firmwareVersion)) {
                        if (!mDeviceDeactivated)
                            showFirmwareUpdateAvailableDialog();
                    } else {
                        editor = sharedPref.edit();
                        editor.putString("firmware_update", "");
                        editor.putString("firmware_version", "");
                        editor.apply();
                    }
                }
                firmwareVersion = firmwareVersion.replace(".", ",");
                try {
                    String[] firmware_split = firmwareVersion.split(",");
                    firmware_version[0] = Integer.parseInt(firmware_split[0]);
                    firmware_version[1] = Integer.parseInt(firmware_split[1]);
                    firmware_version[2] = Integer.parseInt(firmware_split[2]);
                } catch (NumberFormatException e) {
                    firmware_version[0] = -1;
                    firmware_version[1] = -1;
                    firmware_version[2] = -1;
                } catch (ArrayIndexOutOfBoundsException e) {
                    firmware_version[0] = -1;
                    firmware_version[1] = -1;
                    firmware_version[2] = -1;
                }
            } else if (action.equalsIgnoreCase(PheezeeBleService.firmware_log)) {
                boolean firmware_log_status = intent.getBooleanExtra(firmware_log, false);
                if (!firmware_log_status) {
                    chekFirmwareLogPresentAndSrartService();
                }
            } else if (action.equalsIgnoreCase(PheezeeBleService.health_status)) {
                chekHealthStatusLogPresentAndSrartService();
            } else if (action.equalsIgnoreCase(PheezeeBleService.location_status)) {
                chekDeviceLocationStatusLogPresentAndSrartService();
            } else if (action.equalsIgnoreCase(PheezeeBleService.device_details_status)) {
                chekDeviceDetailsStatusLogPresentAndSrartService();
            } else if (action.equalsIgnoreCase(PheezeeBleService.device_details_email)) {
                chekDeviceEmailDetailsStatusLogPresentAndSrartService();
            } else if (action.equalsIgnoreCase(PheezeeBleService.device_disconnected_firmware)) {
                boolean device_disconnected_status = intent.getBooleanExtra(device_disconnected_firmware, false);
                if (device_disconnected_status) {
                    mDeviceDeactivated = true;
                    if (mInsideHome)
                        showDeviceDeactivatedDialog();
                    cancelDeviceDeactivatedJob();
                } else {
                    mDeviceDeactivated = false;
                    if (mDeactivatedDialog != null && mDeactivatedDialog.isShowing()) {
                        mDeactivatedDialog.dismiss();
                    }
                }
            } else if (action.equalsIgnoreCase(scedule_device_status_service)) {
                chekDeviceStatusLogPresentAndSrartService();
            } else if (action.equalsIgnoreCase(deactivate_device)) {
                deactivatePheezeeDevice();
            } else if (action.equalsIgnoreCase(health_error_present_in_device)) {
                boolean ble_state = intent.getBooleanExtra(health_error_present_in_device, false);
                if (ble_state) {
                    mDeviceHealthError = true;
                } else {
                    mDeviceHealthError = false;
                }
            } else if (action.equalsIgnoreCase(show_device_health_error)) {
                if (mService != null)
                    DeviceErrorCodesAndDialogs.showDeviceErrorDialog(mService.getHealthErrorString(), PatientsView.this);
            }

        }
    };


    private void deactivatePheezeeDevice() {
        if (mService != null) {
            if (mDeviceState) {
                mService.deactivateDevice();
            }
        }
    }

    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            isBound = true;
            PheezeeBleService.LocalBinder mLocalBinder = (PheezeeBleService.LocalBinder) service;
            mService = mLocalBinder.getServiceInstance();
            if (!deviceMacc.equalsIgnoreCase(""))
                mService.updatePheezeeMac(deviceMacc);
            mService.setLatitudeAndLongitude(latitude, longitude);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
            mService = null;
        }
    };


    @Override
    protected void onPause() {
        super.onPause();
    }

    private boolean hasPermissions() {
        if (!hasLocationPermissions()) {
            requestLocationPermission();
            return false;
        }
        return true;
    }

    private boolean hasLocationPermissions() {
        return ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }



    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
    }

    private boolean locationpermission(){
        if(ContextCompat.checkSelfPermission(this,ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION},REQUEST_FINE_LOCATION);
            Log.d("kranthi_Location","PERMISSION_GRANTED");
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
            Log.d("kranthi_Location","PERMISSION_DENIED");
        }
        return false;
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//android  permission
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean READ_EXTERNAL_STORAGE = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean WRITE_EXTERNAL_STORAGE = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean  ACCESS_FINE_LOCATION = grantResults[2] == PackageManager.PERMISSION_GRANTED;

                    if (READ_EXTERNAL_STORAGE && WRITE_EXTERNAL_STORAGE && ACCESS_FINE_LOCATION) {

                        // perform action when allow permission success
                    } else {
                        Toast.makeText(this, "Allow permission for storage access!", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocationOfDevice();
                if (mService != null) {
                    if (mService.isScanning() || !deviceMacc.equalsIgnoreCase("")) {
                        mService.stopScaninBackground();
                        mService.startScanInBackground();
                    }
                }
            }
        }
    }



    private void initializeView() {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        repository = new MqttSyncRepository(getApplication());
        repository.setOnServerResponseListner(this);


        iv_addPatient = findViewById(R.id.home_iv_addPatients);
        rl_cap_view = findViewById(R.id.rl_cap_view);
        mRecyclerView = findViewById(R.id.patients_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new PatientsRecyclerViewAdapter(this);
        mRecyclerView.setAdapter(mAdapter);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        drawer = findViewById(R.id.drawer_layout);
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.setDrawerIndicatorEnabled(false);
        toggle.syncState();


        //bluetooth and device status related
        iv_bluetooth_connected = findViewById(R.id.iv_bluetooth_connected);
        iv_bluetooth_disconnected = findViewById(R.id.iv_bluetooth_disconnected);
        iv_device_connected = findViewById(R.id.iv_device_connected);
        iv_device_disconnected = findViewById(R.id.iv_device_disconnected);
        iv_device = findViewById(R.id.iv_device);
        add_device_bar = findViewById(R.id.add_device_bar);
        tv_battery_percentage = findViewById(R.id.tv_battery_percent);
        battery_bar = findViewById(R.id.progress_battery_bar);
        tv_patient_view_add_patient = findViewById(R.id.tv_patient_view_add_patient);
        rl_battery_usb_state = findViewById(R.id.rl_battery_usb_state);
        iv_sync_data = findViewById(R.id.iv_sync_data);
        iv_sync_not_available = findViewById(R.id.iv_sync_data_disabled);
        tv_connect_to_pheezee = findViewById(R.id.tv_connect_to_pheezee);
        tv_start_clinic_session = findViewById(R.id.tv_start_clinic_session);
        ll_profile_update = findViewById(R.id.ll_profile_update);
        DialogCloseButton = findViewById(R.id.DialogCloseButton);
        edit_profile_btn = findViewById(R.id.edit_profile_btn);


        //connecting dialog
        connecting_device_dialog = new ProgressDialog(this);
        Log.i("Testing_Internal","Working Fine");
    }

    private void setInitialMaccIfPresent() {
        //device mac
        if (!sharedPref.getString("deviceMacaddress", "").equalsIgnoreCase("")) {
            deviceMacc = sharedPref.getString("deviceMacaddress", "");
            tv_connect_to_pheezee.setText(R.string.turn_on_device);
        }
    }

    private void setNavigation() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.nav_header_patients_view, navigationView);
        searchView = findViewById(R.id.search_view);
        ivBasicImage = view.findViewById(R.id.imageViewdp);
        email = view.findViewById(R.id.emailId);
        Picasso.get().load(Environment.getExternalStoragePublicDirectory("profilePic"))
                .placeholder(R.drawable.user_icon)
                .error(R.drawable.user_icon)
                .transform(new PicassoCircleTransformation())
                .into(ivBasicImage);


        try {
            if (!json_phizio.getString("phizioprofilepicurl").equals("empty")) {

                String temp = json_phizio.getString("phizioprofilepicurl");
                temp = temp.replaceFirst("@", "%40");
                temp = "https://s3.ap-south-1.amazonaws.com/pheezee/" + temp;
                Picasso.get().load(temp)
                        .placeholder(R.drawable.user_icon)
                        .error(R.drawable.user_icon)
                        .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                        .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                        .transform(new PicassoCircleTransformation())
                        .into(ivBasicImage);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        fullName = view.findViewById(R.id.fullName);
        cl_phizioProfileNavigation = view.findViewById(R.id.phizioProfileNavigation);
    }


    private  boolean checkAndRequestPermissions() {


        int locationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int permissionStorage = ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE);
        int permissionCamera = ContextCompat.checkSelfPermission(this, CAMERA);
        int phonePermission = ContextCompat.checkSelfPermission(this,READ_PHONE_STATE);
        int bluetooth_permission = ContextCompat.checkSelfPermission(this,BLUETOOTH_CONNECT);
        int bluetooth_permission_2 = ContextCompat.checkSelfPermission(this,BLUETOOTH_SCAN);

        List<String> listPermissionsNeeded = new ArrayList<>();
        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (permissionStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (permissionCamera != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }
        if(phonePermission != PackageManager.PERMISSION_GRANTED){
            listPermissionsNeeded.add(READ_PHONE_STATE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if(bluetooth_permission_2 != PackageManager.PERMISSION_GRANTED){
                listPermissionsNeeded.add(BLUETOOTH_SCAN);
            }
            if(bluetooth_permission != PackageManager.PERMISSION_GRANTED){
                listPermissionsNeeded.add(BLUETOOTH_CONNECT);
            }

        }


//        if(bluetooth_permission_2 != PackageManager.PERMISSION_GRANTED){
//            listPermissionsNeeded.add(BLUETOOTH_SCAN);
//        }
//        if(bluetooth_permission != PackageManager.PERMISSION_GRANTED){
//            listPermissionsNeeded.add(BLUETOOTH_SCAN);
//        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

//    private void checkPermissionsRequired() {
//
//              if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE}, 1);
//        }
//        hasPermissions();
//    }









    // Change this Block
    public void getLastLocationOfDevice() {
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                .addOnSuccessListener(PatientsView.this, new OnSuccessListener<Location>() {
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

    private void cancelDeviceDeactivatedJob(){
        JobScheduler jobScheduler = (JobScheduler)getSystemService(JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel(6);
    }


    /**
     *
     * @param patient
     */
    public void startSceduledSession(PhizioPatients patient) {
        myBottomSheetDialog.dismiss();
        Intent intent = new Intent(PatientsView.this, ExercisePrescriptionActivity.class);
        intent.putExtra("deviceMacAddress", sharedPref.getString("deviceMacaddress", ""));
        intent.putExtra("patientId", patient.getPatientid());
        intent.putExtra("patientName", patient.getPatientname());
        intent.putExtra("dateofjoin",patient.getDateofjoin());
        if (Objects.requireNonNull(sharedPref.getString("deviceMacaddress", "")).equals("") && !mDeviceState) {
            Toast.makeText(this, "First add Pheezee to your application", Toast.LENGTH_LONG).show();
        } else if (!(iv_device_connected.getVisibility()==View.VISIBLE)  ) {
            Toast.makeText(this, "First add Pheezee to your application", Toast.LENGTH_LONG).show();
        }
        else {
            if(deviceBatteryPercent<15){
                //
                // Custom notification added by Haaris
                // custom dialog

                String message = BatteryOperation.getDialogMessageForLowBattery(deviceBatteryPercent,this);

                final Dialog dialog = new Dialog(PatientsView.this);
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
                        dialog.dismiss();

                        boolean flag = true;
                        if(firmware_version[0]<1){
                            flag = false;
                        }else if(firmware_version[1]<11 && firmware_version[0]<=1){
                            flag = false;
                        }else if(firmware_version[2]<4 && firmware_version[1]<=11) {
                            flag = false;
                        }else{
                            flag = true;
                        }

                        if(!flag){
                            if(firmware_version[0] == -1 && firmware_version[1] == -1 && firmware_version[2] == -1)
                            {
                                NetworkOperations.servicesNotDiscovered(PatientsView.this);
                            }else {
                                NetworkOperations.firmwareVirsionNotCompatible(PatientsView.this);
                            }
                        }else {
                            if(!mDeviceDeactivated && !mDeviceHealthError)
                                startActivity(intent);
                            else {
                                if(mDeviceDeactivated)
                                    showDeviceDeactivatedDialog();
                                else {
                                    if(mService!=null) {
                                        DeviceErrorCodesAndDialogs.showDeviceErrorDialog(mService.getHealthErrorString(), PatientsView.this);
                                    }
                                }
                            }
                        }


                    }
                });


                dialog.show();
                // End

            }
            else {
                boolean flag = true;
                if(firmware_version[0]<1){
                    flag = false;
                }else if(firmware_version[1]<11 && firmware_version[0]<=1){
                    flag = false;
                }else if(firmware_version[2]<4 && firmware_version[1]<=11) {
                    flag = false;
                }else{
                    flag = true;
                }

                if(!flag){
                    if(firmware_version[0] == -1 && firmware_version[1] == -1 && firmware_version[2] == -1)
                    {
                        NetworkOperations.servicesNotDiscovered(PatientsView.this);
                    }else {
                        NetworkOperations.firmwareVirsionNotCompatible(PatientsView.this);
                    }
                }else {
                    if(!mDeviceDeactivated && !mDeviceHealthError)
                        startActivity(intent);
                    else {
                        if(mDeviceDeactivated)
                            showDeviceDeactivatedDialog();
                        else {
                            if(mService!=null) {
                                DeviceErrorCodesAndDialogs.showDeviceErrorDialog(mService.getHealthErrorString(), this);
                            }
                        }
                    }
                }
            }
        }

    }





    private class LogOutTimerTask extends TimerTask {

        @Override
        public void run() {
            editor = sharedPref.edit();
            editor.clear();
            editor.commit();
            repository.clearDatabase();
            repository.deleteAllSync();
            FirebaseMessaging.getInstance().unsubscribeFromTopic("ota");
//            startActivity(new Intent(PatientsView.this, LoginActivity.class));
            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancelAll();
            File reportPdf = null;
            reportPdf = new File(getApplicationContext().getExternalFilesDir(null)+"/Pheezee");






            if (reportPdf.exists() && reportPdf.isDirectory()) {
                //write same defination for it.

                if (reportPdf.isDirectory()) {
                    String[] children = reportPdf.list();
                    for (int i = 0; i < children.length; i++) {

                        new File(reportPdf, children[i]).delete();
                    }

                }

            }



            finish();
        }

    }


}
