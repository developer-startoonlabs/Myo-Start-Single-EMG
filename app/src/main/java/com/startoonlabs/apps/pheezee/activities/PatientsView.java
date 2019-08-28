package com.startoonlabs.apps.pheezee.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
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
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.startoonlabs.apps.pheezee.R;
import com.startoonlabs.apps.pheezee.classes.BluetoothGattSingleton;
import com.startoonlabs.apps.pheezee.classes.BluetoothSingelton;
import com.startoonlabs.apps.pheezee.classes.MyBottomSheetDialog;
import com.startoonlabs.apps.pheezee.patientsRecyclerView.PatientsListData;
import com.startoonlabs.apps.pheezee.patientsRecyclerView.PatientsRecyclerViewAdapter;
import com.startoonlabs.apps.pheezee.pojos.PatientDetailsData;
import com.startoonlabs.apps.pheezee.pojos.PatientStatusData;
import com.startoonlabs.apps.pheezee.repository.MqttSyncRepository;
import com.startoonlabs.apps.pheezee.retrofit.GetDataService;
import com.startoonlabs.apps.pheezee.retrofit.RetrofitClientInstance;
import com.startoonlabs.apps.pheezee.room.Entity.MqttSync;
import com.startoonlabs.apps.pheezee.room.Entity.PhizioPatients;
import com.startoonlabs.apps.pheezee.room.PheezeeDatabase;
import com.startoonlabs.apps.pheezee.services.MqttHelper;
import com.startoonlabs.apps.pheezee.services.PicassoCircleTransformation;
import com.startoonlabs.apps.pheezee.services.Scanner;
import com.startoonlabs.apps.pheezee.utils.BatteryOperation;
import com.startoonlabs.apps.pheezee.utils.BitmapOperations;
import com.startoonlabs.apps.pheezee.utils.ByteToArrayOperations;
import com.startoonlabs.apps.pheezee.utils.DateOperations;
import com.startoonlabs.apps.pheezee.utils.NetworkOperations;
import com.startoonlabs.apps.pheezee.utils.PatientOperations;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 *
 */
public class PatientsView extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,View.OnClickListener, PatientsRecyclerViewAdapter.onItemClickListner {
    String json_phizioemail = "";
    public static boolean deviceState = true, connectPressed = false, deviceBatteryUsbState = false,sessionStarted = false;
    int maxid = 0;
    public static int deviceBatteryPercent=0;
    public static boolean insideMonitor = false;
    private boolean insidePatientViewActivity = true;
    RelativeLayout rl_cap_view;
    Toast connected_disconnected_toast;
    ConstraintLayout cl_phizioProfileNavigation;
    //Caracteristic uuids
    //All the constant uuids are written here
    public static final UUID service1_uuid = UUID.fromString("909a1400-9693-4920-96e6-893c0157fedd");
    public static final UUID characteristic1_service1_uuid = UUID.fromString("909a1401-9693-4920-96e6-893c0157fedd");
    public static final UUID descriptor_characteristic1_service1_uuid = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");


    public static final UUID battery_service1_uuid = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");
    public static final UUID battery_level_battery_service_characteristic_uuid = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");

    //Boolean for weather the bodypart window is present or not on the activity
    boolean f_bodypart_popup = false;

    static BluetoothGattCharacteristic mCharacteristic;
    BluetoothGattCharacteristic mCustomCharacteristic;
    static BluetoothGattDescriptor mBluetoothGattDescriptor;
    LinearLayout ll_device_and_bluetooth;

    //bluetooth and device connection state
    ImageView iv_bluetooth_connected, iv_bluetooth_disconnected, iv_device_connected, iv_device_disconnected, iv_sync_data,  iv_sync_not_available;



    PopupWindow bodyPartLayoutWndow;
    PopupWindow bodyPartLayout;
    View patientLayoutView;
    MyBottomSheetDialog myBottomSheetDialog;

    //For Alert Dialog
    final CharSequence[] items = { "Take Photo", "Choose from Library",
            "Cancel" };

    final CharSequence[] peezee_items = { "Scan Nearby Devices",
            "Qrcode Scan", "Cancel" };


    TextView email,fullName;
    public static ImageView ivBasicImage;
    //new
    JSONObject json_phizio = new JSONObject();
    //MQTT HELPER

    MqttHelper mqttHelper;
    String mqtt_publish_phizio_addpatient = "phizio/addpatient";
    String mqtt_publish_phizio_deletepatient = "phizio/deletepatient";
    String mqtt_publish_phizio_update_patientdetails = "phizio/updatepatientdetails";

    String mqtt_publish_phizio_patient_profilepic = "phizio/update/patientProfilePic";
    String mqtt_sub_phizio_patient_profilepic = "phizio/update/patientProfilePic/response";
    String mqtt_publish_add_patient_session_emg_data_response = "patient/entireEmgData/response";

    String mqtt_get_profile_pic_response = "phizio/getprofilepic/response";
    private String mqtt_mmt_updated_response = "phizio/patient/updateMmtGrade/response";
    private String mqtt_delete_session_response = "phizio/patient/deletepatient/sesssion/response";

    String mqtt_update_patient_status = "phizio/update/patientStatus";



    String mqtt_subs_phizio_addpatient_response = "phizio/addpatient/response";
//    String mqtt_phizio_profilepic_change_response = "phizio/profilepic/upload/response";  //for the profile picture change of patient
    //Request action intents

    int REQUEST_ENABLE_BT = 1;
    final int REQUEST_ENABLE_BT_SCAN = 2;

    boolean isBleConnected;
    public boolean gattconnection_established = false;
    //All the intents

    Intent to_scan_devices_activity;

    private List<PatientsListData> mdataset = new ArrayList<>();
    private PatientsRecyclerViewAdapter mAdapter;
    EditText patientNameField;
    TextView tv_battery_percentage, tv_patient_view_add_patient;
    ProgressBar battery_bar;
    PopupWindow pw;
    int backpressCount = 0;
    DrawerLayout drawer;
    static SharedPreferences sharedPref;
    static SharedPreferences.Editor editor;
    JSONArray jsonData;
    AlertDialog.Builder builder;
    static BluetoothGatt bluetoothGatt;
    BluetoothDevice remoteDevice;
    BluetoothAdapter bluetoothAdapter;
    BluetoothManager mBluetoothManager;
    String MacAddress;
    GoogleSignInClient mGoogleSignInClient;
    GoogleSignInOptions gso;
    Context context;
    LinearLayout patientTabLayout;
    LinearLayout patientLayout;
    ImageView iv_addPatient;
    LinearLayout ll_add_bluetooth, ll_add_device;
    RelativeLayout rl_battery_usb_state;


    RecyclerView mRecyclerView;
    ProgressDialog progress;

    SearchView searchView;

    MqttSyncRepository repository ;
    List<MqttSync> list_sync = null;
    Handler server_busy_handler;
    GetDataService getDataService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patients_view);
        context = this;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        repository = new MqttSyncRepository(getApplication());
//        Log.i("Ble",Build.HARDWARE);
        editor = sharedPref.edit();
        MacAddress = sharedPref.getString("deviceMacaddress", "");
        iv_addPatient = findViewById(R.id.home_iv_addPatient);
        rl_cap_view = findViewById(R.id.rl_cap_view);
        mRecyclerView = findViewById(R.id.patients_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        server_busy_handler = new Handler();
        getDataService = RetrofitClientInstance.getRetrofitInstance().create(GetDataService.class);

        //connected disconnected toast
        connected_disconnected_toast = Toast.makeText(getApplicationContext(),null,Toast.LENGTH_SHORT);
        connected_disconnected_toast.setGravity(Gravity.BOTTOM,30,40);
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

        iv_sync_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(NetworkOperations.isNetworkAvailable(PatientsView.this))
                    new SyncDataAsync().execute();
                else
                    NetworkOperations.networkError(PatientsView.this);
            }
        });



        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.nav_header_patients_view, navigationView);

        Log.i("shared pref",sharedPref.getString("sync_emg_session",""));

        //external storage permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }

        //Getting previous patient data

        try {
            json_phizio = new JSONObject(sharedPref.getString("phiziodetails", ""));
            json_phizioemail = json_phizio.getString("phizioemail");
            Log.i("Patient View", json_phizio.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        searchView = findViewById(R.id.search_view);
        ivBasicImage =  view.findViewById(R.id.imageViewdp);
        Picasso.get().load(Environment.getExternalStoragePublicDirectory("profilePic"))
                .placeholder(R.drawable.user_icon)
                .error(R.drawable.user_icon)
                .transform(new PicassoCircleTransformation())
                .into(ivBasicImage);

        try {
            if(!json_phizio.getString("phizioprofilepicurl").equals("empty")){

                String temp = json_phizio.getString("phizioprofilepicurl");
                Log.i("phiziopic",temp);
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
            email = view.findViewById(R.id.emailId);
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

        String macAddress;


        //MQTT HELPER
        mqttHelper  = new MqttHelper(this,"patientsview");


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

            /*if (!Objects.requireNonNull(sharedPref.getString("phiziodetails", "")).equals("")) {

                JSONObject object = new JSONObject(sharedPref.getString("phiziodetails", ""));
                JSONArray array = new JSONArray(object.getString("phiziopatients"));
                if(array.length()>0 && sharedPref.getInt("maxid",-1)==-1){
                    for (int i=0;i<array.length();i++){
                        JSONObject pateints = array.getJSONObject(i);
                        try {
                            int id = Integer.parseInt(pateints.getString("patientid"));
                            if(id>maxid){
                                maxid = id;
                            }
                        }catch (NumberFormatException e){
                            Log.i("Exception",e.getMessage());
                        }
                    }
                    editor = sharedPref.edit();
                    editor.putInt("maxid",maxid);
                    editor.apply();
                }
                else if(array.length()<=0 && sharedPref.getInt("maxid",-1)==-1){
                    editor = sharedPref.edit();
                    editor.putInt("maxid",maxid);
                    editor.apply();
                }
                if(array.length()>0) {
                    findViewById(R.id.noPatient).setVisibility(View.GONE);
                    pushJsonData(array);
                }
            }
            else {
                Log.i("emptypatient","empty");
            }*/
        if (!(getIntent().getStringExtra("macAddress") == null || getIntent().getStringExtra("macAddress").equals(""))) {
                macAddress = getIntent().getStringExtra("macAddress");
                editor.putString("deviceMacaddress", macAddress);
                editor.apply();
        }

        if(ScanDevicesActivity.selectedDeviceMacAddress != null){
            macAddress = ScanDevicesActivity.selectedDeviceMacAddress;
            editor.putString("deviceMacaddress",macAddress);
            editor.apply();
            ScanDevicesActivity.selectedDeviceMacAddress = null;
        }

        mBluetoothManager = (BluetoothManager)getSystemService(BLUETOOTH_SERVICE);
        bluetoothAdapter = mBluetoothManager.getAdapter();
        BluetoothSingelton.getmInstance().setAdapter(bluetoothAdapter);
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
        if (bluetoothAdapter==null || !bluetoothAdapter.isEnabled()) {
            bluetoothDisconnected();
            startBluetoothRequest();
        } else {
            bluetoothConnected();
            if (bluetoothGatt != null) {
//                bluetoothGatt.disconnect();
                Log.i("pressed",""+connectPressed);

                BluetoothGattSingleton.getmInstance().setAdapter(bluetoothGatt);
                gattconnection_established = false;
                Message message = Message.obtain();
                message.obj = "N/C";
                bleStatusHandler.sendMessage(message);
            }
            if(!Objects.requireNonNull(sharedPref.getString("deviceMacaddress", "")).equals("")) {
                Log.i("Enabled","true");
                remoteDevice = bluetoothAdapter.getRemoteDevice(sharedPref.getString("deviceMacaddress", "EC:24:B8:31:BD:67"));

                if(!sharedPref.getString("pressed","").equalsIgnoreCase("c") || bluetoothGatt==null) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            bluetoothGatt = remoteDevice.connectGatt(PatientsView.this, true, callback);
                            if (bluetoothGatt != null) {
                                gattconnection_established = true;
                                BluetoothGattSingleton.getmInstance().setAdapter(bluetoothGatt);
                            }
                        }
                    });
                }
            }
        }

        iv_addPatient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initiatePopupWindow(v);
            }
        });
        tv_patient_view_add_patient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initiatePopupWindow(v);
            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothProfile.EXTRA_STATE);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(bluetoothReceiver, filter);


        //mqttcallback
        mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) { }
            @Override
            public void connectionLost(Throwable cause) { }

            @Override
            public void messageArrived(String topic, MqttMessage message){
                Log.i(topic,message.toString());
                try {
                    if(topic.equals(mqtt_get_profile_pic_response+json_phizio.getString("phizioemail"))){
                       Bitmap bitmap = BitmapFactory.decodeByteArray(message.getPayload(), 0, message.getPayload().length);
                       ivBasicImage.setImageBitmap(bitmap);
                   }
                   else if(topic.equals(mqtt_subs_phizio_addpatient_response+json_phizio.getString("phizioemail"))){
                       JSONObject object = new JSONObject(message.toString());
                       if(object.has("response") && object.getString("response").equalsIgnoreCase("inserted")){
                           repository.deleteParticular(object.getInt("id"));
                           if(list_sync!=null){
                               server_busy_handler.removeCallbacks(runnable);
                               list_sync.remove(0);
                               if(list_sync.size()>0){
                                   MqttMessage new_message = new MqttMessage();
                                   JSONObject object1 = new JSONObject(list_sync.get(0).getMessage());
                                   object1.put("id", list_sync.get(0).getId());
                                   new_message.setPayload(object1.toString().getBytes());
                                   if (NetworkOperations.isNetworkAvailable(PatientsView.this)) {
                                       mqttHelper.publishMqttTopic(list_sync.get(0).getTopic(), new_message);
                                   }
                               }else {
                                   progress.dismiss();
                                   showToast("Sync Completed!");
                               }
                           }
                       }
                        if(message.toString().equals("inserted")){
                             Log.i("message emg",message.toString());
                             editor = sharedPref.edit();
                             editor.putString("sync_emg_session","");
                             editor.apply();
                        }
                   }
                   else if(topic.equals(mqtt_sub_phizio_patient_profilepic+json_phizio.getString("phizioemail"))){
                        Log.i("patient profilepic",message.toString());
                   }

                   else if(topic.equals(mqtt_publish_add_patient_session_emg_data_response+json_phizio.getString("phizioemail"))){
                        JSONObject object = new JSONObject(message.toString());
                        if(object.has("response") && object.getString("response").equalsIgnoreCase("inserted")) {
                            repository.deleteParticular(object.getInt("id"));
                            if(list_sync!=null ) {
                                server_busy_handler.removeCallbacks(runnable);
                                list_sync.remove(0);
                                if (list_sync.size() > 0) {
                                    MqttMessage new_message = new MqttMessage();
                                    JSONObject object1 = new JSONObject(list_sync.get(0).getMessage());
                                    object1.put("id", list_sync.get(0).getId());
                                    new_message.setPayload(object1.toString().getBytes());
                                    if (NetworkOperations.isNetworkAvailable(PatientsView.this)) {
                                        mqttHelper.publishMqttTopic(list_sync.get(0).getTopic(), new_message);
                                    }
                                } else {
                                    progress.dismiss();
                                    showToast("Sync Completed!");
                                }
                            }
                        }
                    }
                   else if(topic.equals(mqtt_mmt_updated_response+json_phizio.getString("phizioemail"))){
                        JSONObject object = new JSONObject(message.toString());
                        if(object.has("response") && object.getString("response").equalsIgnoreCase("updated")) {
                            repository.deleteParticular(object.getInt("id"));
                            if(list_sync!=null) {
                                server_busy_handler.removeCallbacks(runnable);
                                list_sync.remove(0);
                                if (list_sync.size() > 0) {
                                    MqttMessage new_message = new MqttMessage();
                                    JSONObject object1 = new JSONObject(list_sync.get(0).getMessage());
                                    object1.put("id", list_sync.get(0).getId());
                                    new_message.setPayload(object1.toString().getBytes());
                                    if (NetworkOperations.isNetworkAvailable(PatientsView.this)) {
                                        mqttHelper.publishMqttTopic(list_sync.get(0).getTopic(), new_message);
                                    }
                                } else {
                                    progress.dismiss();
                                    showToast("Sync Completed!");
                                }
                            }
                        }
                    }
                   else if(topic.equals(mqtt_delete_session_response+json_phizio.getString("phizioemail"))){
                        JSONObject object = new JSONObject(message.toString());
                        if(object.has("response") && object.getString("response").equalsIgnoreCase("deleted")) {
                            repository.deleteParticular(object.getInt("id"));
                            if(list_sync!=null) {
                                list_sync.remove(0);
                                if (list_sync.size() > 0) {
                                    MqttMessage new_message = new MqttMessage();
                                    JSONObject object1 = new JSONObject(list_sync.get(0).getMessage());
                                    object1.put("id", list_sync.get(0).getId());
                                    new_message.setPayload(object1.toString().getBytes());
                                    if (NetworkOperations.isNetworkAvailable(PatientsView.this)) {
                                        mqttHelper.publishMqttTopic(list_sync.get(0).getTopic(), new_message);
                                    }
                                } else {
                                    progress.dismiss();
                                    showToast("Sync Completed!");
                                }
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });


        //search option android

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
                            Log.i("Exception",e.getMessage());
                        }
                    }
                });


        mAdapter.setOnItemClickListner(this);
    }

    /**
     * Promts user to turn on bluetooth
     */
    private void startBluetoothRequest() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
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

    @Override
    protected void onDestroy() {
        unregisterReceiver(bluetoothReceiver);
            mqttHelper.mqttAndroidClient.unregisterResources();
            mqttHelper.mqttAndroidClient.close();
        if(bluetoothGatt!=null){
            disconnectDevice();
        }
        super.onDestroy();
    }

    /**
     * Updating the recycler view adapter
     * @param data
     */
    public void pushJsonData(JSONArray data) {
        mdataset.clear();
        if (data.length() > 0) {
            PatientsListData patientsList;
            for (int i = data.length() - 1; i >= 0; i--) {
                try {
                    if(!data.getJSONObject(i).has("status")  || data.getJSONObject(i).getString("status").equals("active")) {
                        patientsList = new PatientsListData(data.getJSONObject(i).getString("patientname"), data.getJSONObject(i).getString("patientid"), data.getJSONObject(i).getString("patientprofilepicurl"));
                        mdataset.add(patientsList);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        if(mdataset.size()>0) {
            findViewById(R.id.noPatient).setVisibility(View.GONE);
            findViewById(R.id.cl_recycler_view).setVisibility(View.VISIBLE);
        }
        else {
            findViewById(R.id.cl_recycler_view).setVisibility(View.GONE);
            findViewById(R.id.noPatient).setVisibility(View.VISIBLE);
        }
        mAdapter.notifyDataSetChanged();
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if(f_bodypart_popup){ bodyPartLayout.dismiss(); f_bodypart_popup = false; }
           else {
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
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
        // Handle navigation view item clicks here.
        int id = item.getItemId();
            if (id==R.id.pheeze_device_info){
                 Intent i = new Intent(PatientsView.this, DeviceInfoActivity.class);
                 i.putExtra("deviceMacAddress", sharedPref.getString("deviceMacaddress", ""));
                 startActivity(i);
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
    //            if (GoogleSignIn.getLastSignedInAccount(this) != null)
    //                signOut();
    //            else
    //                AccessToken.setCurrentAccessToken(null);
                 editor.clear();
                 editor.commit();
                 repository.clearDatabase();
                 repository.deleteAllSync();
                 disconnectDevice();
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
     * Add patient
     * @param v
     */
    private void initiatePopupWindow(View v) {
        try {
            final JSONObject jsonObject = new JSONObject();
            final String[] case_description = {""};
            jsonData = new JSONArray();
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            int height = size.y;
            LayoutInflater inflater = (LayoutInflater) PatientsView.this
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            @SuppressLint("InflateParams") final View layout = inflater.inflate(R.layout.popup, null);

            pw = new PopupWindow(layout);
            pw.setHeight(height - 400);
            pw.setWidth(width - 100);


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                pw.setElevation(10);
            }
            pw.setTouchable(true);
            pw.setOutsideTouchable(true);
            pw.setContentView(layout);
            pw.setFocusable(true);
            pw.showAtLocation(mRecyclerView, Gravity.CENTER, 0, 0);


            final EditText patientName = layout.findViewById(R.id.patientName);

            final EditText patientId = layout.findViewById(R.id.patientId);
            if(sharedPref.getInt("maxid",-1)!=-1){
                int id = sharedPref.getInt("maxid",0);
                id+=1;
                patientId.setEnabled(false);
                patientId.setText(String.valueOf(id));
            }
            final EditText patientAge = layout.findViewById(R.id.patientAge);
            final EditText caseDescription = layout.findViewById(R.id.contentDescription);
            final RadioGroup radioGroup = layout.findViewById(R.id.patientGender);
            final Spinner sp_case_des = layout.findViewById(R.id.sp_case_des);
            //Adapter for spinner
            ArrayAdapter<String> array_exercise_names = new ArrayAdapter<String>(PatientsView.this, R.layout.support_simple_spinner_dropdown_item, getResources().getStringArray(R.array.case_description));
            array_exercise_names.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
            sp_case_des.setAdapter(array_exercise_names);
            final String todaysDate = DateOperations.dateInMmDdYyyy();
            sp_case_des.setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    InputMethodManager imm=(InputMethodManager)getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    return false;
                }
            }) ;
            Button addBtn = layout.findViewById(R.id.addBtn);
            Button cancelBtn = layout.findViewById(R.id.cancelBtn);

            sp_case_des.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if(position<sp_case_des.getAdapter().getCount()-1){
                        caseDescription.setVisibility(View.GONE);
                        if(position!=0) {
                            case_description[0] = sp_case_des.getSelectedItem().toString();
                        }
                    }
                    if(position==sp_case_des.getAdapter().getCount()-1){
                        case_description[0] = "";
                        caseDescription.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            jsonData = new JSONArray(json_phizio.getString("phiziopatients"));
            addBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    RadioButton btn = layout.findViewById(radioGroup.getCheckedRadioButtonId());
                    if(caseDescription.getVisibility()==View.VISIBLE){
                        case_description[0] = caseDescription.getText().toString();
                    }
                    String patientid =  patientId.getText().toString();
                    String patientname = patientName.getText().toString();
                    String patientage = patientAge.getText().toString();

                    if ((!patientname.equals("")) && (!patientid.equals("")) && (!patientage.equals(""))&& (!case_description[0].equals("")) && btn!=null) {
                        PatientDetailsData data = new PatientDetailsData(json_phizioemail,patientid, patientname, "0",
                                todaysDate,patientage, btn.getText().toString(),case_description[0],"active", "", "empty");

                        /*if (!Objects.requireNonNull(sharedPref.getString("phiziodetails", "")).equals("")) {
                            if(jsonData.length()>0) {
                                try {
                                    for (int i = 0; i < jsonData.length(); i++) {
                                        if (jsonData.getJSONObject(i).get("patientid").equals(patientId.getText().toString())) {
                                            Toast.makeText(PatientsView.this,
                                                    "Patient with same patient id is already exsists for patient name " + jsonData.getJSONObject(i).get("patientname"),
                                                    Toast.LENGTH_LONG).show();
                                            Log.i("ALREDY PRESENT","ALREADY");

                                            pw.dismiss();
                                            return;
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        try {
                            jsonObject.put("patientname", patientName.getText().toString());
                            jsonObject.put("patientid", patientId.getText().toString());
                            jsonObject.put("numofsessions", "0");
                            jsonObject.put("patientage",patientAge.getText().toString());
                            jsonObject.put("patientgender",btn.getText().toString());
                            jsonObject.put("patientcasedes",case_description[0]);
                            jsonObject.put("status","active");
                            jsonObject.put("patientphone", patientId.getText().toString());
                            jsonObject.put("patientprofilepicurl", "empty");
                            jsonObject.put("dateofjoin",todaysDate);
                            jsonObject.put("sessions", new JSONArray());
                            jsonData.put(jsonObject);
                            jsonObject.put("phizioemail",json_phizio.get("phizioemail"));
                            new SendDataAsyncTask().execute(jsonObject);

                            json_phizio.put("phiziopatients",jsonData);
                            editor.putString("phiziodetails", json_phizio.toString());
                            if(sharedPref.getInt("maxid",-1)!=-1) {
                                editor.putInt("maxid", Integer.parseInt(patientId.getText().toString()));
                            }
                            editor.commit();
                            pushJsonData(jsonData);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }*/
                        pw.dismiss();
                    }
                    else {
                        Toast.makeText(PatientsView.this, "Invalid Input!!", Toast.LENGTH_SHORT).show();
                    }

                }
            });
            cancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    pw.dismiss();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Bluetooth callback
     */
    public BluetoothGattCallback callback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTING) {
                    refreshDeviceCache(gatt);
                    isBleConnected = true;
                    Message msg = Message.obtain();
                    msg.obj = "C..";
                    Log.i("connected","true");
                    deviceState = true;
                    bleStatusHandler.sendMessage(msg);
                } else if (newState == BluetoothProfile.STATE_CONNECTED) {
                    refreshDeviceCache(gatt);
                    Message msg = Message.obtain();
                    isBleConnected = true;
                    msg.obj = "C";
                    deviceState=true;
                    Log.i("connected", "connected");
                    bleStatusHandler.sendMessage(msg);
                    gatt.discoverServices();
                } else if (newState == BluetoothProfile.STATE_DISCONNECTING) {
                    Message msg = Message.obtain();
                    isBleConnected = false;
                    msg.obj = "N/C";
                    bleStatusHandler.sendMessage(msg);
                }
                else {
                    Message msg = Message.obtain();
                    isBleConnected = false;
                    msg.obj = "N/C";
                    bleStatusHandler.sendMessage(msg);
                }
            }
            if(status == BluetoothGatt.GATT_FAILURE){
                Message msg = Message.obtain();
                msg.obj = "N/C";
                bleStatusHandler.sendMessage(msg);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) throws NullPointerException {
            BluetoothGattCharacteristic characteristic = gatt.getService(service1_uuid).getCharacteristic(characteristic1_service1_uuid);
            Log.i("TEST", "INSIDE IF");
            bluetoothGatt = gatt;
            if(characteristic!=null)
                mCustomCharacteristic = characteristic;
            gatt.setCharacteristicNotification(mCustomCharacteristic,true);
            mBluetoothGattDescriptor = mCustomCharacteristic.getDescriptor(descriptor_characteristic1_service1_uuid);
            mCharacteristic = gatt.getService(battery_service1_uuid).getCharacteristic(battery_level_battery_service_characteristic_uuid);
            new MyAsync().execute();
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.i("inside","descritor");
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Message msg = new Message();
            if(characteristic.getUuid().equals(battery_level_battery_service_characteristic_uuid)){
                byte b[] = characteristic.getValue();
                int battery  = b[0];
                int usb_state = b[1];
                if(usb_state==1) {
                    if(findViewById(R.id.rl_battery_usb_state).getVisibility()==View.GONE) {
                        msg.obj = "c";
                        batteryUsbState.sendMessage(msg);
                    }
                    Log.i("battery notif read","connected");
                }
                else if(usb_state==0) {
                    msg.obj = "nc";
                    batteryUsbState.sendMessage(msg);
                    Log.i("battery notif read","disconnected");
                }

                Log.i("battery changed to",battery+"");
                Message message = new Message();
                message.obj = battery+"";
                batteryStatus.sendMessage(message);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Message msg = new Message();
            Log.i("chatacteristic","read");
                if(characteristic.getUuid()==mCustomCharacteristic.getUuid()) {
                    byte info_packet[] = characteristic.getValue();
                    Log.i("inside", "inside");
                    int battery = info_packet[11] & 0xFF;
                    int device_status = info_packet[12] & 0xFF;
                    int device_usb_state = info_packet[13] & 0xFF;
                    if(device_usb_state==1) {
                        msg.obj = "c";
                        batteryUsbState.sendMessage(msg);
                    }
                    else if(device_status==0) {
                        msg.obj = "nc";
                        batteryUsbState.sendMessage(msg);
                    }
                    Log.i("device", battery + " " + device_status + " " + device_usb_state);
                    bluetoothGatt.readCharacteristic(mCharacteristic);
                }

                //remove comments later now.
                else if(characteristic.getUuid()==mCharacteristic.getUuid()) {
                    byte b[] = characteristic.getValue();
                    int battery  = b[0];
                    int usb_state = b[1];
                    if(usb_state==1) {
                        msg.obj = "c";
                        batteryUsbState.sendMessage(msg);
                        Log.i("battery service read","connected");
                    }
                    else if(usb_state==0) {
                        msg.obj = "nc";
                        batteryUsbState.sendMessage(msg);
                        Log.i("battery service read","disconnected");
                    }
                    Log.i("battery",battery+" read");
                    Message message = new Message();
                    message.obj = battery+"";
                    batteryStatus.sendMessage(message);

                    gatt.setCharacteristicNotification(mCharacteristic, true);
                    mBluetoothGattDescriptor = mCharacteristic.getDescriptor(descriptor_characteristic1_service1_uuid);
                    mBluetoothGattDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    bluetoothGatt.writeDescriptor(mBluetoothGattDescriptor);
                }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.i("chatacteristic","written");
            bluetoothGatt.readCharacteristic(mCustomCharacteristic);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothProfile.EXTRA_STATE);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(bluetoothReceiver, filter);
        insidePatientViewActivity = true;
        if(bluetoothAdapter==null || !bluetoothAdapter.isEnabled()){
            Message message = Message.obtain();
            message.obj = "N/C";
            bleStatusHandler.sendMessage(message);
        }
        try {
            json_phizio = new JSONObject(sharedPref.getString("phiziodetails", ""));
            email.setText(json_phizioemail);
            fullName.setText(json_phizio.getString("phizioname"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        insideMonitor=false;
        insidePatientViewActivity = false;
        if(bluetoothAdapter==null || !bluetoothAdapter.isEnabled()){
            Message message = Message.obtain();
            message.obj = "N/C";
            bleStatusHandler.sendMessage(message);
        }
        if(remoteDevice == null){
            Message message = Message.obtain();
            message.obj = "N/C";
            bleStatusHandler.sendMessage(message);
        }
    }

    @Override
    protected void onRestart() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothProfile.EXTRA_STATE);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(bluetoothReceiver, filter);
        if(bluetoothAdapter==null || !bluetoothAdapter.isEnabled()){
            Message message = Message.obtain();
            message.obj = "N/C";
            bleStatusHandler.sendMessage(message);
        }
        if(!deviceState){
            disconnectDevice();
            pheezeeDisconnected();
        }
        super.onRestart();
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }


    /**
     *
     * @param patient
     */
    public void editPopUpWindow(PhizioPatients patient){
        final String[] case_description = {""};
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();display.getSize(size);int width = size.x;int height = size.y;
        LayoutInflater inflater = (LayoutInflater) PatientsView.this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        @SuppressLint("InflateParams") final View layout = inflater.inflate(R.layout.popup, null);

        pw = new PopupWindow(layout);
        pw.setHeight(height - 400);
        pw.setWidth(width - 100);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            pw.setElevation(10);
        }
        pw.setTouchable(true);
        pw.setOutsideTouchable(true);
        pw.setContentView(layout);
        pw.setFocusable(true);
        pw.showAtLocation(mRecyclerView, Gravity.CENTER, 0, 0);

        final TextView patientName = layout.findViewById(R.id.patientName);
        final TextView patientId = layout.findViewById(R.id.patientId);
        final TextView patientAge = layout.findViewById(R.id.patientAge);
        final TextView caseDescription = layout.findViewById(R.id.contentDescription);
        final RadioGroup radioGroup = layout.findViewById(R.id.patientGender);
        RadioButton btn_male = layout.findViewById(R.id.radioBtn_male);
        RadioButton btn_female = layout.findViewById(R.id.radioBtn_female);
        final Spinner sp_case_des = layout.findViewById(R.id.sp_case_des);
        //Adapter for spinner
        ArrayAdapter<String> array_exercise_names = new ArrayAdapter<String>(PatientsView.this, R.layout.support_simple_spinner_dropdown_item, getResources().getStringArray(R.array.case_description));
        array_exercise_names.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        sp_case_des.setAdapter(array_exercise_names);

        sp_case_des.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm=(InputMethodManager)getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return false;
            }
        }) ;

        sp_case_des.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position<sp_case_des.getAdapter().getCount()-1){
                    caseDescription.setVisibility(View.GONE);
                    if(position!=0) {
                        case_description[0] = sp_case_des.getSelectedItem().toString();
                    }
                }
                if(position==sp_case_des.getAdapter().getCount()-1){
                    caseDescription.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        Button addBtn = layout.findViewById(R.id.addBtn);
        addBtn.setText("Update");
        patientId.setVisibility(View.GONE);
        final Button cancelBtn = layout.findViewById(R.id.cancelBtn);

        patientName.setText(patient.getPatientname());
        patientAge.setText(patient.getPatientage());
        if(patient.getPatientgender().equalsIgnoreCase("M"))
            radioGroup.check(btn_male.getId());
        else
            radioGroup.check(btn_female.getId());
        caseDescription.setText(patient.getPatientcasedes());
        case_description[0] = patient.getPatientcasedes();
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(caseDescription.getVisibility()==View.VISIBLE){
                    case_description[0] = caseDescription.getText().toString();
                }
                RadioButton btn = layout.findViewById(radioGroup.getCheckedRadioButtonId());
                if ((!patientName.getText().toString().equals(""))  && (!patientAge.getText().toString().equals(""))&& (!case_description[0].equals("")) && btn!=null) {
                    patient.setPatientname(patientName.getText().toString());
                    patient.setPatientage(patientAge.getText().toString());
                    patient.setPatientcasedes(case_description[0]);
                    patient.setPatientgender(btn.getText().toString());
                    repository.updatePatient(patient);
                    Call<String> call = null;
                    try {
                        PatientDetailsData data = new PatientDetailsData(json_phizio.getString("phizioemail"), patient.getPatientid(),
                                patient.getPatientname(),patient.getNumofsessions(), patient.getDateofjoin(), patient.getPatientage(),
                                patient.getPatientgender(), patient.getPatientcasedes(), patient.getStatus(), patient.getPatientphone(), patient.getPatientprofilepicurl());
                        call = getDataService.updatePatientDetails(data);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    call.enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            String string = response.body();
                            Log.i("response",string);
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {

                        }
                    });
                }
                else {
                    Toast.makeText(PatientsView.this, "Invalid Input!!", Toast.LENGTH_SHORT).show();
                }
                pw.dismiss();
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pw.dismiss();
            }
        });
    }

    /**
     * Bluetooth device status handler
     */
    @SuppressLint("HandlerLeak")
    public final Handler bleStatusHandler = new Handler() {
        public void handleMessage(Message msg) {
            String status = (String) msg.obj;
            if(status.equalsIgnoreCase("N/C"))
                pheezeeDisconnected();
            else if(status.equalsIgnoreCase("C")) {
                pheezeeConnected();
                showToast("Device Connected");
            }
        }
    };

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
        Log.i("Inside disconnect", "Device Disconnected");
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
        Log.i("red color","red");
    }

    /**
     * Receiver for bluetooth connectivity and device disconnection
     */
    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)){

//                Toast.makeText(PatientsView.this, "The device has got disconnected...", Toast.LENGTH_LONG).show();
                connected_disconnected_toast.setText("The device got disconnected..");
                connected_disconnected_toast.show();
                if(sessionStarted && insideMonitor){
                    sessionStarted=false;
                    deviceState=false;
                }
                if(sharedPref.getBoolean("isLoggedIn",false)==false)
                    finish();

                if(sharedPref.getString("pressed", "").equals("c")){
                    if(bluetoothGatt==null){
                    if (bluetoothAdapter==null || !bluetoothAdapter.isEnabled()) {
                        bluetoothDisconnected();
                        startBluetoothRequest();
                    }
                    else {
                        if(!Objects.requireNonNull(sharedPref.getString("deviceMacaddress", "")).equals("")) {
                            Log.i("Enabled","true");
                            remoteDevice = bluetoothAdapter.getRemoteDevice(sharedPref.getString("deviceMacaddress", "EC:24:B8:31:BD:67"));


                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    bluetoothGatt = remoteDevice.connectGatt(PatientsView.this, true, callback);
                                    if(bluetoothGatt!=null) {
                                        gattconnection_established = true;
                                        BluetoothGattSingleton.getmInstance().setAdapter(bluetoothGatt);
                                    }
                                }
                            });
                        }
                    }
                }
                    editor = sharedPref.edit();
                    editor.putString("pressed","");
                    editor.commit();
                }
                else {
                    isBleConnected = false;
                    Message message = Message.obtain();
                    message.obj = "N/C";
                    bleStatusHandler.sendMessage(message);
                    if (deviceState && !insidePatientViewActivity) {       //The device state is related to the device info screen if the user forcefully disconnects and forget the device
                        Intent i = getIntent();
                        finish();
                        startActivity(i);
                    }
                }
            }
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_ON) {
                    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    bluetoothConnected();
                    if(!MacAddress.equals("")) {
                        remoteDevice = bluetoothAdapter.getRemoteDevice(MacAddress);
                        if (remoteDevice != null) {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    bluetoothGatt = remoteDevice.connectGatt(PatientsView.this, true, callback);
                                }
                            });
                        }
                    }
                }
                if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_OFF) {
                    bluetoothDisconnected();
                    Toast.makeText(PatientsView.this, "Bluetooth turned off", Toast.LENGTH_LONG).show();
                    if(bluetoothGatt!=null){
                        bluetoothGatt.disconnect();
                        bluetoothGatt.close();
                    }
                    Message message = Message.obtain();
                    message.obj = "N/C";
                    bleStatusHandler.sendMessage(message);
                }
            }

        }
    };

    /**
     *
     * @param patient
     */
    public void startSession(PhizioPatients patient) {

        final Intent intent = new Intent(PatientsView.this, BodyPartSelection.class);
        intent.putExtra("deviceMacAddress", sharedPref.getString("deviceMacaddress", ""));
        intent.putExtra("patientId", patient.getPatientid());
        intent.putExtra("patientName", patient.getPatientname());



        if (Objects.requireNonNull(sharedPref.getString("deviceMacaddress", "")).equals("")) {
            Toast.makeText(this, "First add pheezee to your application", Toast.LENGTH_LONG).show();
        } else if (!(iv_device_connected.getVisibility()==View.VISIBLE)  ) {
            Toast.makeText(this, "Make sure that the pheezee is on", Toast.LENGTH_LONG).show();
        }
        else {

            String message = BatteryOperation.getDialogMessageForLowBattery(deviceBatteryPercent,this);
            if(!message.equalsIgnoreCase("c")){
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
            else
                startActivity(intent);
        }

    }


    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothProfile.EXTRA_STATE);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(bluetoothReceiver, filter);
    }

    /**
     * Disconnects the device
     */
    public static void disconnectDevice() {
        if(bluetoothGatt==null){
            Log.i("inside","null");
            editor = sharedPref.edit();
            editor.putString("pressed","");
            editor.commit();
            return;
        }

        if(mCharacteristic!=null && mBluetoothGattDescriptor!=null) {
            Log.i("inside","Characteristics");
            bluetoothGatt.setCharacteristicNotification(mCharacteristic, false);
            mBluetoothGattDescriptor = mCharacteristic.getDescriptor(descriptor_characteristic1_service1_uuid);
            mBluetoothGattDescriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
            bluetoothGatt.writeDescriptor(mBluetoothGattDescriptor);
        }
        bluetoothGatt.close();
        Log.i("bluetooth gatt closed","closed");
        bluetoothGatt = null;
        deviceState=false;
    }

    /**
     * Updates the chache of the device connected from the blutooth as it will not discover new services.
     * @param gatt
     * @return
     */
    private boolean refreshDeviceCache(BluetoothGatt gatt){
        try {
            BluetoothGatt localBluetoothGatt = gatt;
            Method localMethod = localBluetoothGatt.getClass().getMethod("refresh", new Class[0]);
            if (localMethod != null) {
                return (Boolean) localMethod.invoke(localBluetoothGatt, new Object[0]);
            }
        }
        catch (Exception localException) {
            Log.e("EXCEPTION", "An exception occured while refreshing device");
        }
        return false;
    }

    /**
     * Updating the image of patient
     * @param view
     */
    public void chooseImageUpdateAction(final View view){
        patientLayoutView = view;
        builder = new AlertDialog.Builder(PatientsView.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    if(ContextCompat.checkSelfPermission(PatientsView.this, Manifest.permission.CAMERA)
                            == PackageManager.PERMISSION_DENIED) {
                        ActivityCompat.requestPermissions(PatientsView.this, new String[]{Manifest.permission.CAMERA}, 5);
                        cameraIntent();
                    }
                    else {
                        cameraIntent();
                    }
                } else if (items[item].equals("Choose from Library")) {
                    galleryIntent();
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    /**
     * Opens the builer for different device connecting techniques
     * @param view
     */
    public void addPheezeeDevice(View view){
        builder = new AlertDialog.Builder(PatientsView.this);
        builder.setTitle("Add Pheezee Device!");
        builder.setItems(peezee_items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (peezee_items[item].equals("Scan Nearby Devices")) {
                    to_scan_devices_activity = new Intent(PatientsView.this, ScanDevicesActivity.class);
                    if (bluetoothAdapter==null || !bluetoothAdapter.isEnabled()) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT_SCAN);
                    }
                    else
                        startActivity(to_scan_devices_activity);
                }  else if (peezee_items[item].equals("Qrcode Scan")) {
                    startActivity(new Intent(PatientsView.this, Scanner.class));
                }
                else{
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void galleryIntent() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto , 6);
    }


    private void cameraIntent() {
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        startActivityForResult(takePicture, 5);
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
                //TextView tv_patientId = patientLayoutView.getRootView().findViewById(R.id.patientId);
                Bitmap photo = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
                photo = BitmapOperations.getResizedBitmap(photo,128);
                imageView_patientpic.setImageBitmap(photo);
                TextView tv_patientId = (TextView) patientTabLayout.getChildAt(1);
                JSONObject object = new JSONObject();

                MqttMessage message = new MqttMessage();

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                if (photo != null) {
                    photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
                }
                byte[] byteArray = stream.toByteArray();
                String encodedString = Base64.encodeToString(byteArray,Base64.DEFAULT);
                try {
                    object.put("image",encodedString);
                    object.put("phizioemail",json_phizio.getString("phizioemail"));
                    object.put("patientid",tv_patientId.getText().toString().substring(4).replaceAll("\\s+",""));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                PatientOperations.putPatientProfilePicUrl(this,tv_patientId.getText().toString().substring(4).replaceAll("\\s+",""),"");
                message.setPayload(object.toString().getBytes());

                if(NetworkOperations.isNetworkAvailable(PatientsView.this))
                    mqttHelper.publishMqttTopic(mqtt_publish_phizio_patient_profilepic,message);
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
                JSONObject object = new JSONObject();

                MqttMessage message = new MqttMessage();

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                if (photo != null) {
                    photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
                }
                byte[] byteArray = stream.toByteArray();
                String encodedString = Base64.encodeToString(byteArray, Base64.DEFAULT);
                try {
                    object.put("image", encodedString);
                    object.put("phizioemail", json_phizio.getString("phizioemail"));
                    object.put("patientid", tv_patientId.getText().toString().substring(4).replaceAll("\\s+", ""));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                message.setPayload(object.toString().getBytes());
                if(NetworkOperations.isNetworkAvailable(PatientsView.this)) {
                    mqttHelper.publishMqttTopic(mqtt_publish_phizio_patient_profilepic, message);
                    PatientOperations.putPatientProfilePicUrl(this,tv_patientId.getText().toString().substring(4).replaceAll("\\s+",""),"");
                }
            }
        }
        if (resultCode != 0) {
            if(!Objects.requireNonNull(sharedPref.getString("deviceMacaddress", "")).equals("")) {
                remoteDevice = bluetoothAdapter.getRemoteDevice(sharedPref.getString("deviceMacaddress", ""));


                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        bluetoothGatt = remoteDevice.connectGatt(PatientsView.this, true, callback);
                    }
                });

            }
        }

        if(requestCode==2){
            if(resultCode!=0){
                startActivity(new Intent(this,ScanDevicesActivity.class));
            }
        }
    }


    /**
     * Opens the bottom bar sheet
     * @param view
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("ResourceType")
    public void openOpionsPopupWindow(View view, PhizioPatients patient){
        Boolean bool = true;
        Bitmap patientpic_bitmap=null;


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
    public void openReportActivity(String patientid, String patientname){
        if(NetworkOperations.isNetworkAvailable(PatientsView.this)){
            Intent mmt_intent = new Intent(PatientsView.this, SessionReportActivity.class);
            mmt_intent.putExtra("patientid", patientid);
            mmt_intent.putExtra("patientname", patientname);
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
//        final MqttMessage mqttMessage = new MqttMessage();
//        final JSONObject object = new JSONObject();

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Archive Patient");
        builder.setMessage("Are you sure you want to archive the patient?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(NetworkOperations.isNetworkAvailable(PatientsView.this)){
                    patient.setStatus("inactive");
                    repository.updatePatient(patient);
                    try {
                        Call<String> call = getDataService.updatePatientStatus(new PatientStatusData(json_phizio.getString("phizioemail"),patient.getPatientid(),patient.getStatus()));
                        call.enqueue(new Callback<String>() {
                            @Override
                            public void onResponse(Call<String> call, Response<String> response) {
                                String string = response.body();
                                Log.i("response",string);
                            }

                            @Override
                            public void onFailure(Call<String> call, Throwable t) {

                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
//                    for(int k=0;k<jsonData.length();k++){
//                        try {
//                            if(jsonData.getJSONObject(k).getString("patientid").equals(patientIdTemp.getText().toString().substring(5))){
//
//
//                                object.put("phizioemail", json_phizio.get("phizioemail"));
//                                object.put("patientid",jsonData.getJSONObject(k).get("patientid"));
//                                object.put("status","inactive");
//                                jsonData.getJSONObject(k).put("status","inactive");
//                                json_phizio.put("phiziopatients",jsonData);
//
//                                editor.putString("phiziodetails",json_phizio.toString());
//                                editor.commit();
//                                pushJsonData(new JSONArray(json_phizio.getString("phiziopatients")));
//                                mqttMessage.setPayload(object.toString().getBytes());
//                                mqttHelper.publishMqttTopic(mqtt_update_patient_status,mqttMessage);
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
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
     * Triggered when user presses the delete patient
     * @param view
     */
    public void deletePatient(View view){
        myBottomSheetDialog.dismiss();
        final MqttMessage mqttMessage = new MqttMessage();
        final JSONObject object = new JSONObject();
        try {
            jsonData = new JSONArray(json_phizio.getString("phiziopatients"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Patient");
        builder.setMessage("Are you sure you want to delete the patient?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(NetworkOperations.isNetworkAvailable(PatientsView.this)){
                    final TextView patientIdTemp = patientTabLayout.findViewById(R.id.patientId);
                    for(int k=0;k<jsonData.length();k++){
                        try {
                            if(jsonData.getJSONObject(k).getString("patientid").equals(patientIdTemp.getText().toString().substring(5))){


                                object.put("phizioemail", json_phizio.get("phizioemail"));
                                object.put("patientid",jsonData.getJSONObject(k).get("patientid"));
                                jsonData.remove(k);
                                json_phizio.put("phiziopatients",jsonData);

                                editor.putString("phiziodetails",json_phizio.toString());
                                editor.commit();

                                Log.i("jsonData", json_phizio.getString("phiziopatients"));
                                pushJsonData(jsonData);
                                mqttMessage.setPayload(object.toString().getBytes());
                                mqttHelper.publishMqttTopic(mqtt_publish_phizio_deletepatient,mqttMessage);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
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

    @Override
    public void onItemClick(PhizioPatients patient, View view) {
        openOpionsPopupWindow(view,patient);
    }

    @Override
    public void onStartSessionClickListner(PhizioPatients patient) {
        startSession(patient);
    }

    private class MyAsync extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... voids) {

            byte b[] = ByteToArrayOperations.hexStringToByteArray("AA02");
            if(send(b)){
                Log.i("SENDING","MESSAGE SENT");
            }
            else {
                Log.i("SENDING","UNSUCCESSFULL");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                }
            });
        }
    }

    /**
     * sends the data to the device by writing into a characteristic
     * @param data
     * @return
     */
    public boolean send(byte[] data) {

        if (bluetoothGatt == null ) {
            return false;
        }
        if (mCustomCharacteristic == null) {
            return false;
        }

        BluetoothGattService service = bluetoothGatt.getService(service1_uuid);

        if(service==null){
            if (mCustomCharacteristic == null) {
                return false;
            }
        }
        if(characteristic1_service1_uuid.equals(mCustomCharacteristic.getUuid())){
            Log.i("TRUE", "TRUE");
        }


        mCustomCharacteristic.setValue(data);

        return bluetoothGatt.writeCharacteristic(mCustomCharacteristic);
    }

    /**
     * This handler handles the battery status and updates the bars of the battery symbol.
     */
    @SuppressLint("HandlerLeak")
    public final Handler batteryStatus = new Handler(){
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(Message msg) {
            Log.i("Battery",msg.obj.toString());
            tv_battery_percentage.setText(msg.obj.toString().concat("%"));
            Log.i("Battery Percentage",msg.obj.toString());
            deviceBatteryPercent = Integer.parseInt(msg.obj.toString());
            int percent = BatteryOperation.convertBatteryToCell(deviceBatteryPercent);
            Log.i("After percent Formulae",percent+"");
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

    /**
     * For syncing the data
     */
    public class SyncDataAsync extends AsyncTask<Void,Void,List<MqttSync>>{

        @Override
        protected List<MqttSync> doInBackground(Void... voids) {
            PheezeeDatabase database = PheezeeDatabase.getInstance(PatientsView.this);
            List<MqttSync> list = database.mqttSyncDao().getAllMqttSyncItems();
            return list;
        }

        @Override
        protected void onPostExecute(List<MqttSync> mqttSyncs) {
            super.onPostExecute(mqttSyncs);
            startSync(mqttSyncs);
        }
    }

    /**
     * called when user presses the sync button when sync is available
     * @param list_sync
     */
    public void startSync(List<MqttSync> list_sync) {
        if (list_sync.size() > 0) {
            this.list_sync = list_sync;
            server_busy_handler.postDelayed(runnable,12000);
            progress = new ProgressDialog(PatientsView.this);
            progress.setMessage("Syncing session data to the server");
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setIndeterminate(true);
            progress.setCancelable(false);
            progress.show();
            MqttMessage message = new MqttMessage();
            try {
                JSONObject object = new JSONObject(list_sync.get(0).getMessage());
                object.put("id", list_sync.get(0).getId());
                message.setPayload(object.toString().getBytes());
                if (NetworkOperations.isNetworkAvailable(PatientsView.this)) {
                    mqttHelper.publishMqttTopic(list_sync.get(0).getTopic(), message);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else {
            showToast("Nothing to sync");
        }
    }
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            progress.dismiss();
            NetworkOperations.openServerBusyDialog(PatientsView.this);
        }
    };

    /**
     * Stores the topic and message in database and sends to the server if internet is available.
     */
    public class SendDataAsyncTask extends AsyncTask<JSONObject,Void,JSONObject>{

        @Override
        protected JSONObject doInBackground(JSONObject... jsonObjects) {
            PheezeeDatabase database = PheezeeDatabase.getInstance(PatientsView.this);

//            message.setPayload(jsonObjects[0].toString().getBytes());
            MqttSync mqttSync = new MqttSync(mqtt_publish_phizio_addpatient,jsonObjects[0].toString());
//            long id = database.mqttSyncDao().insert(mqttSync);
//            Log.i("identity",String.valueOf(id));
            try {
                jsonObjects[0].put("id",database.mqttSyncDao().insert(mqttSync));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return jsonObjects[0];
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            MqttMessage message = new MqttMessage();
            message.setPayload(jsonObject.toString().getBytes());
            if(NetworkOperations.isNetworkAvailable(PatientsView.this)) {
//                                mqttHelper.syncData();
                mqttHelper.publishMqttTopic(mqtt_publish_phizio_addpatient, message);
            }
        }
    }



}
