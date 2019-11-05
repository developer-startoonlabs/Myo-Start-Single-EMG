package com.startoonlabs.apps.pheezee.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
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
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.startoonlabs.apps.pheezee.R;
import com.startoonlabs.apps.pheezee.adapters.PatientsRecyclerViewAdapter;
import com.startoonlabs.apps.pheezee.classes.MyBottomSheetDialog;
import com.startoonlabs.apps.pheezee.pojos.DeletePatientData;
import com.startoonlabs.apps.pheezee.pojos.PatientDetailsData;
import com.startoonlabs.apps.pheezee.pojos.PatientStatusData;
import com.startoonlabs.apps.pheezee.popup.AddPatientPopUpWindow;
import com.startoonlabs.apps.pheezee.popup.EditPopUpWindow;
import com.startoonlabs.apps.pheezee.popup.UploadImageDialog;
import com.startoonlabs.apps.pheezee.repository.MqttSyncRepository;
import com.startoonlabs.apps.pheezee.room.Entity.PhizioPatients;
import com.startoonlabs.apps.pheezee.services.PheezeeBleService;
import com.startoonlabs.apps.pheezee.services.PicassoCircleTransformation;
import com.startoonlabs.apps.pheezee.services.Scanner;
import com.startoonlabs.apps.pheezee.utils.BatteryOperation;
import com.startoonlabs.apps.pheezee.utils.BitmapOperations;
import com.startoonlabs.apps.pheezee.utils.NetworkOperations;
import com.startoonlabs.apps.pheezee.utils.RegexOperations;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.startoonlabs.apps.pheezee.services.PheezeeBleService.battery_percent;
import static com.startoonlabs.apps.pheezee.services.PheezeeBleService.bluetooth_state;
import static com.startoonlabs.apps.pheezee.services.PheezeeBleService.device_state;
import static com.startoonlabs.apps.pheezee.services.PheezeeBleService.usb_state;

public class PatientsView extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        PatientsRecyclerViewAdapter.onItemClickListner, MqttSyncRepository.onServerResponse{
    private static final int REQUEST_FINE_LOCATION = 14;
    PheezeeBleService mService;
    private boolean mDeviceState = false;
    boolean isBound =  false;
    int REQUEST_ENABLE_BT = 1;
    public static int deviceBatteryPercent = -1;
    private int[] firmware_version = {-1,-1,-1};
    View patientLayoutView;
    MyBottomSheetDialog myBottomSheetDialog;
    ProgressDialog connecting_device_dialog;

    final CharSequence[] peezee_items = { "Scan Nearby Devices",
            "Qrcode Scan", "Cancel" };
    TextView email,fullName;
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
    ImageView iv_addPatient;
    private String deviceMacc = "";
    LinearLayout ll_add_bluetooth, ll_add_device;
    RelativeLayout rl_cap_view;
    RelativeLayout rl_battery_usb_state;
    RecyclerView mRecyclerView;
    ProgressDialog progress, deletepatient_progress;
    SearchView searchView;
    MqttSyncRepository repository ;
    String json_phizioemail = "";
    ConstraintLayout cl_phizioProfileNavigation;
    TextView tv_connect_to_pheezee;

    //bluetooth and device connection state
    ImageView iv_bluetooth_connected, iv_bluetooth_disconnected, iv_device_connected, iv_device_disconnected, iv_sync_data,  iv_sync_not_available;
    LinearLayout ll_device_and_bluetooth;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patients_view);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        repository = new MqttSyncRepository(getApplication());
        repository.setOnServerResponseListner(this);

        editor = sharedPref.edit();
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
        ll_add_bluetooth = findViewById(R.id.ll_add_bluetooth);
        ll_add_device = findViewById(R.id.ll_add_device);
        tv_battery_percentage = findViewById(R.id.tv_battery_percent);
        battery_bar = findViewById(R.id.progress_battery_bar);
        tv_patient_view_add_patient = findViewById(R.id.tv_patient_view_add_patient);
        ll_device_and_bluetooth = findViewById(R.id.ll_device_and_bluetooth);
        rl_battery_usb_state = findViewById(R.id.rl_battery_usb_state);
        iv_sync_data = findViewById(R.id.iv_sync_data);
        iv_sync_not_available = findViewById(R.id.iv_sync_data_disabled);
        tv_connect_to_pheezee = findViewById(R.id.tv_connect_to_pheezee);

        //connecting dialog
        connecting_device_dialog = new ProgressDialog(this);

        //device mac
        if(!sharedPref.getString("deviceMacaddress", "").equalsIgnoreCase("")){
            deviceMacc = sharedPref.getString("deviceMacaddress", "");
            tv_connect_to_pheezee.setText(R.string.turn_on_device);
        }

        iv_sync_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(NetworkOperations.isNetworkAvailable(PatientsView.this)){
                    progress = new ProgressDialog(PatientsView.this);
                    progress.setMessage("Syncing session data to the server");
                    progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progress.setIndeterminate(true);
                    progress.setCancelable(false);
                    progress.show();
                    repository.syncDataToServer();
//                    new SyncDataAsync().execute();
                }

                else
                    NetworkOperations.networkError(PatientsView.this);
            }
        });
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.nav_header_patients_view, navigationView);


        //external storage permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
            hasPermissions();
        }



        //Getting previous patient data
        try {
            json_phizio = new JSONObject(sharedPref.getString("phiziodetails", ""));
            json_phizioemail = json_phizio.getString("phizioemail");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        searchView = findViewById(R.id.search_view);
        ivBasicImage =  view.findViewById(R.id.imageViewdp);
        email = view.findViewById(R.id.emailId);
        Picasso.get().load(Environment.getExternalStoragePublicDirectory("profilePic"))
                .placeholder(R.drawable.user_icon)
                .error(R.drawable.user_icon)
                .transform(new PicassoCircleTransformation())
                .into(ivBasicImage);


        try {
            if(!json_phizio.getString("phizioprofilepicurl").equals("empty")){

                String temp = json_phizio.getString("phizioprofilepicurl");
                temp = temp.replaceFirst("@", "%40");
                temp = "https://s3.ap-south-1.amazonaws.com/pheezee/"+temp;
                Picasso.get().load(temp)
                        .placeholder(R.drawable.user_icon)
                        .error(R.drawable.user_icon)
                        .networkPolicy(NetworkPolicy.NO_CACHE,NetworkPolicy.NO_STORE)
                        .memoryPolicy(MemoryPolicy.NO_CACHE,MemoryPolicy.NO_STORE)
                        .transform(new PicassoCircleTransformation())
                        .into(ivBasicImage);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        fullName = view.findViewById(R.id.fullName);
        cl_phizioProfileNavigation = view.findViewById(R.id.phizioProfileNavigation);
        cl_phizioProfileNavigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PatientsView.this, PhizioProfile.class));
            }
        });

        try {
            email.setText(json_phizioemail);
            fullName.setText(json_phizio.getString("phizioname"));
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
                if(patients.size()>0) {
                    findViewById(R.id.noPatient).setVisibility(View.GONE);
                    findViewById(R.id.cl_recycler_view).setVisibility(View.VISIBLE);
                }
                else {
                    findViewById(R.id.cl_recycler_view).setVisibility(View.GONE);
                    findViewById(R.id.noPatient).setVisibility(View.VISIBLE);
                }

                Collections.reverse(patients);
                mAdapter.setNotes(patients);
            }
        });


        //Add device and bluetooth turn on click events
        ll_add_device.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPheezeeDevice(v);
            }
        });

        ll_add_bluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startBluetoothRequest();
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
                                iv_sync_not_available.setVisibility(View.GONE);
                                iv_sync_data.setVisibility(View.VISIBLE);
                            } else {
                                iv_sync_data.setVisibility(View.GONE);
                                iv_sync_not_available.setVisibility(View.VISIBLE);
                            }
                        }catch (NullPointerException e){
                        }
                    }
                });

        mAdapter.setOnItemClickListner(this);


        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(device_state);
        intentFilter.addAction(bluetooth_state);
        intentFilter.addAction(usb_state);
        intentFilter.addAction(battery_percent);
        intentFilter.addAction(PheezeeBleService.firmware_version);
        registerReceiver(patient_view_broadcast_receiver,intentFilter);

        ContextCompat.startForegroundService(this,new Intent(this,PheezeeBleService.class));


        Intent mIntent = new Intent(this,PheezeeBleService.class);
        bindService(mIntent, mConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            json_phizio = new JSONObject(sharedPref.getString("phiziodetails", ""));
            email.setText(json_phizioemail);
            fullName.setText(json_phizio.getString("phizioname"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(isBound){
            unbindService(mConnection);
        }
        unregisterReceiver(patient_view_broadcast_receiver);
        stopService(new Intent(this,PheezeeBleService.class));
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            backpressCount++;
            if (backpressCount == 1) {
                Toast.makeText(PatientsView.this, "press again to close pheezee app", Toast.LENGTH_SHORT).show();
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000);
                            backpressCount = 0;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
                thread.start();
            }
            if (backpressCount == 2) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
                finishAffinity();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.patients_view, menu);
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
        if (id==R.id.pheeze_device_info){
            Intent i = new Intent(PatientsView.this, DeviceInfoActivity.class);
            i.putExtra("deviceMacAddress", sharedPref.getString("deviceMacaddress", ""));
            startActivityForResult(i,13);
        }

        else if(id==R.id.nav_home){

        }

        else if(id==R.id.nav_add_device){
            addPheezeeDevice(item.getActionView());
        }
        else if(id==R.id.nav_add_patient){
            iv_addPatient.performClick();
        }
        else if(id==R.id.nav_app_version){
            startActivity(new Intent(PatientsView.this,AppInfo.class));
        }
        else if (id == R.id.nav_logout) {
            editor.clear();
            editor.commit();
            repository.clearDatabase();
            repository.deleteAllSync();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

//        else if(id == R.id.ota_device) {
//            Intent intent;
//            if (!bleStatusTextView.getText().toString().equals("C")) {
//                Toast.makeText(context, "Please Connect to Pheeze.", Toast.LENGTH_SHORT).show();
//            } else {
//                intent = new Intent(this, DfuActivity.class);
//                intent.putExtra("deviceMacAddress", sharedPref.getString("deviceMacaddress", ""));
//                startActivity(intent);
//            }
//
//        }

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
        iv_bluetooth_disconnected.setVisibility(View.VISIBLE);
        iv_bluetooth_connected.setVisibility(View.GONE);
        ll_add_bluetooth.setVisibility(View.VISIBLE);
        findViewById(R.id.ll_device_and_bluetooth).setVisibility(View.VISIBLE);
        findViewById(R.id.ll_device_and_bluetooth).setBackgroundResource(R.drawable.drawable_background_connect_to_pheezee);
    }

    /**
     * Bluetooth connected
     */
    private void bluetoothConnected() {
        iv_bluetooth_disconnected.setVisibility(View.GONE);
        iv_bluetooth_connected.setVisibility(View.VISIBLE);
        ll_add_bluetooth.setVisibility(View.GONE);
        findViewById(R.id.ll_device_and_bluetooth).setBackgroundResource(R.drawable.drawable_background_turn_on_device);
    }

    private void initiatePopupWindow() {
        AddPatientPopUpWindow patientPopUpWindow = new AddPatientPopUpWindow(this,json_phizioemail);
        patientPopUpWindow.openAddPatientPopUpWindow();
        patientPopUpWindow.setOnClickListner(new AddPatientPopUpWindow.onClickListner() {
            @Override
            public void onAddPatientClickListner(PhizioPatients patient, PatientDetailsData data, boolean isvalid) {
                if(isvalid){
                    repository.insertPatient(patient,data);
//                    new SendDataAsyncTask().execute(data);
                }
                else {
                    showToast("Invalid Input!!");
                }
            }
        });
    }


    public void addPheezeeDevice(View view){
        if(deviceMacc.equalsIgnoreCase("")) {
            if(hasPermissions()) {
                builder = new AlertDialog.Builder(PatientsView.this);
                builder.setTitle("Add Pheezee Device!");
                builder.setItems(peezee_items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (peezee_items[item].equals("Scan Nearby Devices")) {
                            to_scan_devices_activity = new Intent(PatientsView.this, ScanDevicesActivity.class);
                            startActivityForResult(to_scan_devices_activity, 12);
                        } else if (peezee_items[item].equals("Qrcode Scan")) {
                            startActivityForResult(new Intent(PatientsView.this, Scanner.class), 12);
                        } else {
                            dialog.dismiss();
                        }
                    }
                });
                builder.show();
            }
        }else {
            showToast("Please forget the current device to scan for new");
        }
    }


    /**
     *
     * @param patient
     */
    public void editPopUpWindow(PhizioPatients patient){
        EditPopUpWindow popUpWindow = new EditPopUpWindow(this,patient,json_phizioemail);
        popUpWindow.openEditPopUpWindow();
        popUpWindow.setOnClickListner(new EditPopUpWindow.onClickListner() {
            @Override
            public void onAddClickListner(PhizioPatients patients, PatientDetailsData data, boolean isvalid) {
                if(isvalid){
                    deletepatient_progress = new ProgressDialog(PatientsView.this);
                    deletepatient_progress.setTitle("Updating patient details, please wait");
                    deletepatient_progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    deletepatient_progress.setIndeterminate(true);
                    deletepatient_progress.show();
                    repository.updatePatientDetailsServer(patient,data);
                }
                else {
                    showToast("Invalid Input!!");
                }
            }
        });
    }

    /**
     * Called when device connects to update the view
     */
    private void pheezeeConnected() {
        iv_device_disconnected.setVisibility(View.GONE);
        iv_device_connected.setVisibility(View.VISIBLE);
        ll_add_device.setVisibility(View.GONE);
        ll_device_and_bluetooth.setVisibility(View.GONE);
        Drawable drawable = getResources().getDrawable(R.drawable.drawable_progress_battery);
        battery_bar.setProgressDrawable(drawable);
        @SuppressLint("ResourceAsColor") Drawable drawable_cap = new ColorDrawable(R.color.battery_gray);
        rl_cap_view.setBackground(drawable_cap);
    }

    /**
     * called when device disconnected to update the view
     */
    private void pheezeeDisconnected() {
        iv_device_connected.setVisibility(View.GONE);
        iv_device_disconnected.setVisibility(View.VISIBLE);
        ll_add_device.setVisibility(View.VISIBLE);
        if(iv_bluetooth_connected.getVisibility()==View.VISIBLE)
            ll_device_and_bluetooth.setBackgroundResource(R.drawable.drawable_background_turn_on_device);
        else
            ll_device_and_bluetooth.setBackgroundResource(R.drawable.drawable_background_connect_to_pheezee);
        ll_device_and_bluetooth.setVisibility(View.VISIBLE);
        Drawable drawable = getResources().getDrawable(R.drawable.drawable_progress_battery_disconnected);
        battery_bar.setProgressDrawable(drawable);
        @SuppressLint("ResourceAsColor") Drawable drawable_cap = new ColorDrawable(getResources().getColor(R.color.red));
        rl_cap_view.setBackground(drawable_cap);
        rl_battery_usb_state.setVisibility(View.GONE);
    }


    /**
     *
     * @param patient
     */
    public void startSession(PhizioPatients patient) {
        Log.i("timestamp1", String.valueOf(Calendar.getInstance().getTimeInMillis()));
        Intent intent = new Intent(PatientsView.this, BodyPartSelection.class);
        intent.putExtra("deviceMacAddress", sharedPref.getString("deviceMacaddress", ""));
        intent.putExtra("patientId", patient.getPatientid());
        intent.putExtra("patientName", patient.getPatientname());
        intent.putExtra("dateofjoin",patient.getDateofjoin());
        if (Objects.requireNonNull(sharedPref.getString("deviceMacaddress", "")).equals("")) {
            Toast.makeText(this, "First add pheezee to your application", Toast.LENGTH_LONG).show();
        } else if (!(iv_device_connected.getVisibility()==View.VISIBLE)  ) {
            Toast.makeText(this, "Make sure that the pheezee is on", Toast.LENGTH_LONG).show();
        }
        else {
            if(deviceBatteryPercent<15){
                String message = BatteryOperation.getDialogMessageForLowBattery(deviceBatteryPercent,this);
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Battery Low");
                builder.setMessage(message);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(intent);
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                    }
                });
                builder.show();
            }
            else {
                boolean flag = true;
                if(firmware_version[0]<1){
                       flag = false;
                }else if(firmware_version[1]<9){
                    if(firmware_version[2]<11){
                        flag = false;
                    }
                }else {
                    flag = true;
                }

                if(!flag){
                    NetworkOperations.firmwareVirsionNotCompatible(this);
                }else {
                    Log.i("timestamp2", String.valueOf(Calendar.getInstance().getTimeInMillis()));
                    startActivity(intent);
                }
            }
        }

    }

    /**
     * Updating the image of patient
     * @param view
     */
    public void chooseImageUpdateAction(final View view){
        patientLayoutView = view;
        UploadImageDialog dialog = new UploadImageDialog(this, 5, 6);
        dialog.showDialog();
    }

    /**
     * Opens the bottom bar sheet
     * @param view
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("ResourceType")
    public void openOpionsPopupWindow(View view, PhizioPatients patient){
        Bitmap patientpic_bitmap=null;
        Log.i("inside","here");
        patientTabLayout= (LinearLayout) (view).getParent();
        LinearLayout iv_layout = (LinearLayout)patientTabLayout.getChildAt(0);

        ImageView iv_patient_pic = iv_layout.findViewById(R.id.patientProfilePic);

        if(!(iv_patient_pic.getDrawable() ==null)) {
            try {
                patientpic_bitmap = ((BitmapDrawable) iv_patient_pic.getDrawable()).getBitmap();
            }
            catch (ClassCastException e){
                patientpic_bitmap = null;
            }
        }
        myBottomSheetDialog = new MyBottomSheetDialog(patientpic_bitmap, patient);
        myBottomSheetDialog.show(getSupportFragmentManager(),"MyBottomSheet");

    }


    public void editThePatientDetails(PhizioPatients patient){
        myBottomSheetDialog.dismiss();
        if(NetworkOperations.isNetworkAvailable(PatientsView.this))
            editPopUpWindow( patient);
        else {
            NetworkOperations.networkError(PatientsView.this);
        }
    }

    /**
     *
     * @param patientid
     * @param patientname
     */
    public void openReportActivity(String patientid, String patientname, String dateofjoin){
        if(NetworkOperations.isNetworkAvailable(PatientsView.this)){
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
        }
        else {
            NetworkOperations.networkError(PatientsView.this);
        }
    }


    /**
     *
     * @param patient
     */
    public void updatePatientStatus(PhizioPatients patient){
        myBottomSheetDialog.dismiss();
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Archive Patient");
        builder.setMessage("Are you sure you want to archive the patient?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(NetworkOperations.isNetworkAvailable(PatientsView.this)){
                    patient.setStatus("inactive");
                    deletepatient_progress = new ProgressDialog(PatientsView.this);
                    deletepatient_progress.setTitle("Updating patient status, please wait");
                    deletepatient_progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    deletepatient_progress.setIndeterminate(true);
                    deletepatient_progress.show();
                    PatientStatusData data = new PatientStatusData(json_phizioemail,patient.getPatientid(),patient.getStatus());
                    repository.updatePatientStatusServer(patient,data);
                }
                else {
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
     *
     * @param patient
     */
    public void deletePatient(PhizioPatients patient){
        myBottomSheetDialog.dismiss();
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Patient");
        builder.setMessage("Are you sure you want to delete the patient?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(NetworkOperations.isNetworkAvailable(PatientsView.this)){
                    deletepatient_progress = new ProgressDialog(PatientsView.this);
                    deletepatient_progress.setTitle("Deleting patient, please wait");
                    deletepatient_progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    deletepatient_progress.setIndeterminate(true);
                    deletepatient_progress.show();
                    DeletePatientData data = new DeletePatientData(json_phizioemail,patient.getPatientid());
                    repository.deletePatientFromServer(data,patient);
                }
                else {
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
            if(resultCode == RESULT_OK){
                ImageView imageView_patientpic = patientLayoutView.findViewById(R.id.patientProfilePic);
                patientTabLayout= (LinearLayout) (patientLayoutView).getParent().getParent();
                patientTabLayout = (LinearLayout) patientTabLayout.getChildAt(1);
                Bitmap photo = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
                photo = BitmapOperations.getResizedBitmap(photo,128);
                imageView_patientpic.setImageBitmap(photo);
                TextView tv_patientId = (TextView) patientTabLayout.getChildAt(1);
                if(NetworkOperations.isNetworkAvailable(this))
                    repository.uploadPatientImage(tv_patientId.getText().toString().substring(4).replaceAll("\\s+",""),json_phizioemail,photo);
                else
                    NetworkOperations.networkError(this);
            }
        }
        if(requestCode==6){
            ImageView imageView_patientpic = patientLayoutView.findViewById(R.id.patientProfilePic);
            patientTabLayout= (LinearLayout) (patientLayoutView).getParent().getParent();
            patientTabLayout = (LinearLayout) patientTabLayout.getChildAt(1);
            if(data!=null) {
                Uri selectedImage = data.getData();
                Glide.with(this).load(selectedImage).apply(new RequestOptions().centerCrop()).into(imageView_patientpic);
                TextView tv_patientId = (TextView) patientTabLayout.getChildAt(1);
                Bitmap photo = null;
                try {
                    photo = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                    photo = BitmapOperations.getResizedBitmap(photo,128);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(NetworkOperations.isNetworkAvailable(this))
                    repository.uploadPatientImage(tv_patientId.getText().toString().substring(4).replaceAll("\\s+",""),json_phizioemail,photo);
                else
                    NetworkOperations.networkError(this);
            }
        }
        else if(requestCode==12){
            if(resultCode==RESULT_OK){
                String macAddress = data.getStringExtra("macAddress");
                if(RegexOperations.validate(macAddress)){
                    if(mService!=null){
                        mService.updatePheezeeMac(macAddress);
                        mService.connectDevice(macAddress);
                        deviceMacc = macAddress;
                        editor = sharedPref.edit();
                        editor.putString("deviceMacaddress",macAddress);
                        editor.apply();
                        tv_connect_to_pheezee.setText(R.string.turn_on_device);
                        showToast("Connecting, please wait..");
                    }
                }
            }
            else if(resultCode == 2) {
                showToast("Not a mac address");
            }
        }

        else if(requestCode==13){
            if(resultCode==13){
                enableScanningTheDevices();
            }
        }
        else if(requestCode==REQUEST_FINE_LOCATION){
            Log.i("resultcode123", String.valueOf(resultCode));
        }

        if(requestCode==2){
            if(resultCode!=0){
                startActivity(new Intent(this,ScanDevicesActivity.class));
            }
        }
    }

    private void enableScanningTheDevices() {
        tv_connect_to_pheezee.setText(R.string.click_to_connect);
        deviceMacc="";
    }

    private void startBluetoothRequest() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    @Override
    public void onItemClick(PhizioPatients patient, View view) {
        openOpionsPopupWindow(view,patient);
    }

    @Override
    public void onStartSessionClickListner(PhizioPatients patient) {
        startSession(patient);
    }

    @Override
    public void onDeletePateintResponse(boolean response) {
        if(deletepatient_progress!=null)
            deletepatient_progress.dismiss();
        if(response){
            if(deletepatient_progress!=null){
                deletepatient_progress.dismiss();
                showToast("Patient Deleted");
            }
        }
        else {
            showToast("Please try again later");
        }
    }

    @Override
    public void onUpdatePatientDetailsResponse(boolean response) {
        if(deletepatient_progress!=null)
            deletepatient_progress.dismiss();
        if(response){
            if(deletepatient_progress!=null){
                deletepatient_progress.dismiss();
                showToast("Patient details updated");
            }
        }else {
            showToast("Please try again later");
        }
    }

    @Override
    public void onUpdatePatientStatusResponse(boolean response) {
        Log.i("here","patientdetails updated");
        if(deletepatient_progress!=null)
            deletepatient_progress.dismiss();
        if(response){
            if(deletepatient_progress!=null){
                deletepatient_progress.dismiss();
                showToast("Patient Staus Updated");
            }
        }
        else {
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
    public final Handler batteryStatus = new Handler(){
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(Message msg) {
            tv_battery_percentage.setText(msg.obj.toString().concat("%"));
            deviceBatteryPercent = Integer.parseInt(msg.obj.toString());
            int percent = BatteryOperation.convertBatteryToCell(deviceBatteryPercent);
            if(deviceBatteryPercent<15) {
                Drawable drawable = getResources().getDrawable(R.drawable.drawable_progress_battery_low);
                battery_bar.setProgressDrawable(drawable);
            }
            else {
                Drawable drawable = getResources().getDrawable(R.drawable.drawable_progress_battery);
                battery_bar.setProgressDrawable(drawable);
            }
            battery_bar.setProgress(percent);
        }
    };

    /**
     * This handler handels the batery state view change
     */
    @SuppressLint("HandlerLeak")
    public final Handler batteryUsbState = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.obj.toString().equalsIgnoreCase("c"))
                rl_battery_usb_state.setVisibility(View.VISIBLE);
            else
                rl_battery_usb_state.setVisibility(View.GONE);
        }
    };


    public void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }




    BroadcastReceiver patient_view_broadcast_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equalsIgnoreCase(device_state)){
                boolean device_status = intent.getBooleanExtra(device_state,false);
                if(device_status){
                    mDeviceState = true;
                    pheezeeConnected();
                }else {
                    mDeviceState = false;
                    pheezeeDisconnected();
                    battery_bar.setProgress(0);
                }
            }else if(action.equalsIgnoreCase(bluetooth_state)){
                boolean ble_state = intent.getBooleanExtra(bluetooth_state,false);
                if(ble_state){
                    bluetoothConnected();
                }else {
                    bluetoothDisconnected();
                }
            }else if(action.equalsIgnoreCase(usb_state)){
                boolean usb_status = intent.getBooleanExtra(usb_state,false);
                Message msg = new Message();
                if(usb_status){
                    msg.obj = "c";
                    batteryUsbState.sendMessage(msg);
                }else {
                    msg.obj = "nc";
                    batteryUsbState.sendMessage(msg);
                }
            }else if(action.equalsIgnoreCase(battery_percent)){
                String percent = intent.getStringExtra(battery_percent);
                Message msg = new Message();
                msg.obj = percent;
                if(mDeviceState)
                    batteryStatus.sendMessage(msg);
            }else if(action.equalsIgnoreCase(PheezeeBleService.firmware_version)){
                String firmwareVersion = intent.getStringExtra(PheezeeBleService.firmware_version);
                firmwareVersion = firmwareVersion.replace(".",",");
                try {
                    String[] firmware_split = firmwareVersion.split(",");
                    firmware_version[0] = Integer.parseInt(firmware_split[0]);
                    firmware_version[1] = Integer.parseInt(firmware_split[1]);
                    firmware_version[2] = Integer.parseInt(firmware_split[2]);
                }catch (NumberFormatException e){
                    firmware_version[0] = -1;firmware_version[1] = -1;firmware_version[2] = -1;
                }catch (ArrayIndexOutOfBoundsException e){
                    firmware_version[0] = -1;firmware_version[1] = -1;firmware_version[2] = -1;
                }
            }
        }
    };

    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            isBound = true;
            PheezeeBleService.LocalBinder mLocalBinder = (PheezeeBleService.LocalBinder)service;
            mService = mLocalBinder.getServiceInstance();
            if(!deviceMacc.equalsIgnoreCase(""))
                mService.updatePheezeeMac(deviceMacc);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
            mService = null;
        }
    };



    private boolean hasPermissions() {
        if (!hasLocationPermissions()) {
            requestLocationPermission();
            return false;
        }
        return true;
    }

    private boolean hasLocationPermissions() {
        return ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==REQUEST_FINE_LOCATION){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Log.i("here","acess");
                if(mService!=null){
                    if(mService.isScanning() || !deviceMacc.equalsIgnoreCase("")){
                        mService.stopScaninBackground();
                        mService.startScanInBackground();
                    }
                }
            }
        }
    }
}
