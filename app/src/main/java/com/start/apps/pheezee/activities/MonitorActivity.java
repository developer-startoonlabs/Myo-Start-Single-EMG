package com.start.apps.pheezee.activities;
/**
 * Date of commented : 27-10-2022
 * Commented By : Burra Kranthi Kiran
 * Description : Included all import files related to live monitoring screen
 */


import android.Manifest; /** getting the activity start information for entry level start application **/
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;/** Bluetooth Adapter is the function to bride between the resources and components **/
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;/** The ComponentName constructor taking two Strings can be used to refer to a component in another application.But, the first argument is not the package name of the class; it is the package name of the application---the package attribute of the manifest element in that application's AndroidManifest.xml**/
import android.content.DialogInterface;/** To initialize the AlertDialog; buildexit is AlertDialog.Builder and exitalert is AlertDialog **/
import android.content.Intent;/** Intent is the message that is passed between components such as activities, content providers, broadcast receivers, services etc **/
import android.content.ServiceConnection; /** This are used to Start Service at background and run the service **/
import android.content.SharedPreferences; /** this is used share the value from one activity to another activity **/
import android.content.pm.PackageManager; /** Manage the share data to accessed **/
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import start.apps.pheezee.R;

import com.start.apps.pheezee.fragments.SmileyMonitoringFragment; /** This File is for the SmileyMonitorFragment Theme, Calculation and Function will be return in that File **/
import com.start.apps.pheezee.fragments.StandardGoldTeachFragment; /** This File is for the ArcView Dialer show in Theme, Calculation and Function will be return in that File **/
import com.start.apps.pheezee.fragments.StarMonitorFragment;/**This File is for the StarMonitor show in Theme, Calculation and Function will be return in that File**/
import com.start.apps.pheezee.repository.MqttSyncRepository;/** Async and sending the data with help of using this activity **/
import com.start.apps.pheezee.room.Entity.SceduledSession;/** It is used for local database storage **/
import com.start.apps.pheezee.services.PheezeeBleService; /** Ble Service Pheezee Connection send the data and receiving the data  **/
import com.start.apps.pheezee.utils.TakeScreenShot;/** Screen Short Functionality **/

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Locale;

import static com.start.apps.pheezee.utils.PackageTypes.ACHEDAMIC_TEACH_PLUS;
import static com.start.apps.pheezee.utils.PackageTypes.GOLD_PLUS_PACKAGE;
import static com.start.apps.pheezee.utils.PackageTypes.STANDARD_PACKAGE;

public class MonitorActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {
    public static int total_sceduled_size = 0;
    public static boolean IS_SCEDULED_SESSIONS_COMPLETED = false;
    public static boolean IS_SCEDULED_SESSION = false;
    public static String IS_SESSION_SCEDULED_ON = "";
    private int phizio_packagetype = 0;
    private String currentMessageForTextToSpeach = "";
    private boolean isAlreadySceduled = false;

    int selected_theme = 0;
    //session inserted on server
    ImageView iv_back_monitor, iv_theme_chooser, iv_smiley_icon;
    private String str_body_orientation = "", json_phizioemail = "";
    TextView tv_snap;
    int REQUEST_ENABLE_BT = 1;
    SharedPreferences sharedPreferences;
    JSONObject json_phizio = new JSONObject();
    public List<SceduledSession> sessions;
    private boolean mSessionStarted = false, isBound = false;
    PheezeeBleService mService;
    String patientid, patientname;
    BluetoothAdapter bluetoothAdapter;
    BluetoothManager mBluetoothManager;
    Fragment fragment;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    MqttSyncRepository repository;
    private TextToSpeech mTTS;
    static public boolean show_popup_once = true;


    /**
     * Date of commented : 27-10-2022
     * Commented By : Burra Kranthi Kiran
     * Description: The below code onCreate is function this will default function to run the android when the MonitorActivity
     * When this function started it will be bring the layout("UI design") and start function in background
     * The following function run in the background
     * Inputs: layout, layout id, permission check and json Data
     * Output: for each layout id gave function Script , getting the permission state and Data json array Plotted.
     * Functions used here: permission check android, Bluetooth Connection State, Server App Connection Status, Check Condition for Choose Monitor Screen Design, visibilityChangesBasedOnPackageType and Choosing the Fragment for the Based on the Selection user Theme
     * Other functions using this: Check package theme, Ble State and Async data to server
     */
    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor_updated);
        repository = new MqttSyncRepository(getApplication());
        fragmentManager = getSupportFragmentManager();
        iv_back_monitor = findViewById(R.id.iv_back_monitor);
        tv_snap = findViewById(R.id.snap_monitor);
        iv_theme_chooser = findViewById(R.id.theme_chooser);
        iv_smiley_icon = findViewById(R.id.theme_smiley);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        try {
            json_phizio = new JSONObject(sharedPreferences.getString("phiziodetails", ""));
            json_phizioemail = json_phizio.getString("phizioemail");
            phizio_packagetype = json_phizio.getInt("packagetype");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        patientid = getIntent().getStringExtra("patientId");
        patientname = getIntent().getStringExtra("patientName");
        IS_SCEDULED_SESSION = getIntent().getBooleanExtra("issceduled", false);
        IS_SCEDULED_SESSIONS_COMPLETED = false;
        IS_SESSION_SCEDULED_ON = patientid;
        show_popup_once = true;


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        iv_back_monitor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation aniFade = AnimationUtils.loadAnimation(MonitorActivity.this, R.anim.fade_in);
                iv_back_monitor.setAnimation(aniFade);
                finish();
            }
        });

        /**
         * Date of commented : 27-10-2022
         * Commented By : Burra Kranthi Kiran
         * Description: this below function for the Screen Short In the Appliaction ("tv_snap")
         */
        tv_snap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TakeScreenShot screenShot = new TakeScreenShot(MonitorActivity.this, patientname, patientid);
                screenShot.takeScreenshot(null);
                showToast("Took Screenshot");
            }
        });
        tv_snap.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                    tv_snap.setAlpha(0.4f);
                } else if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                    tv_snap.setAlpha(1f);
                }
                return false;
            }
        });

        /**
         * Date of commented : 27-10-2022
         * Commented By : Burra Kranthi Kiran
         * Description: the below for the checking the Choose Theme Style function, visibilityChangesBasedOnPackageType
         */

        visibilityChangesBasedOnPackageType();
        registerForContextMenu(iv_theme_chooser);

        /**
         * Date of commented : 27-10-2022
         * Commented By : Burra Kranthi Kiran
         * Description: The below code is used to check the bluetooth state
         */
        mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        bluetoothAdapter = mBluetoothManager.getAdapter();
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
            startBleRequest();
        }
        /**
         * Date of commented : 27-10-2022
         * Commented By : Burra Kranthi Kiran
         * Description: the below code is for text to speak function. Here we are selecting the language and voice language and we are checking the set data missing if any missed the we are using try and catch block error handling.        */
        mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = mTTS.setLanguage(Locale.US);

                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        mTTS = null;
                    } else {
                        try {
                            mTTS.setPitch(Settings.Secure.getInt(getContentResolver(), Settings.Secure.TTS_DEFAULT_PITCH));
                            mTTS.setSpeechRate(Settings.Secure.getInt(getContentResolver(), Settings.Secure.TTS_DEFAULT_RATE));
                        } catch (Settings.SettingNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        /**
         * Date of commented : 27-10-2022
         * Commented By : Burra Kranthi Kiran
         * Description : The below code for the get Information from Bluetooth Class Provider
         */
        Intent mIntent = new Intent(this, PheezeeBleService.class);
        bindService(mIntent, mConnection, BIND_AUTO_CREATE);

        if (!IS_SCEDULED_SESSION)
            standardGoldTeachFragment();
        else {
            repository.getAllSceduledSessionsList(patientid);
            repository.setOnSceduledSessionResponse(new MqttSyncRepository.onSceduledSesssionResponse() {
                @Override
                public void onResponse(List<SceduledSession> session) {
                    sessions = session;
                    total_sceduled_size = session.get(session.size() - 1).getSessionno();
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (session != null && session.size() > 0)
                                standardGoldTeachFragment();
                            else {
                                Intent i = new Intent(MonitorActivity.this, PatientsView.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                startActivity(i);
                                repository.removeAllSessionsForPataient(patientid);
                            }
                        }
                    });
                }
            });
        }

    }

    /**
     * Date of commented : 27-10-2022
     * Commented By : Burra Kranthi Kiran
     * Description : On the above we are create the theme package manager for choosing the function running on create after on create the function call from there to hear.
     */
    private void visibilityChangesBasedOnPackageType() {
        if (phizio_packagetype != STANDARD_PACKAGE) {
//            tv_snap.setVisibility(View.VISIBLE);
        }
        if (phizio_packagetype == GOLD_PLUS_PACKAGE || phizio_packagetype == ACHEDAMIC_TEACH_PLUS) {
            iv_theme_chooser.setVisibility(View.VISIBLE);
            iv_smiley_icon.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Date of commented : 27-10-2022
     * Commented By : Burra Kranthi Kiran
     * Description : The following Code for Each Fragment to start fragment by using following function there we are three Fragment()
     */

    public void standardGoldTeachFragment() {
        if (selected_theme != 1) {
            fragmentTransaction = fragmentManager.beginTransaction();
            fragment = new StandardGoldTeachFragment();
            fragmentTransaction.replace(R.id.fragment_monitor_container, fragment);
            fragmentTransaction.commit();
            FragmentManager fm = getSupportFragmentManager();
            for (int i = 0; i < fm.getBackStackEntryCount(); ++i) {
                fm.popBackStack();
            }
            selected_theme = 1;
        }
    }

    public void smileyFragment() {
        if (selected_theme != 2) {
            fragmentTransaction = fragmentManager.beginTransaction();
            fragment = new SmileyMonitoringFragment();
            fragmentTransaction.replace(R.id.fragment_monitor_container, fragment);
            fragmentTransaction.commit();
            FragmentManager fm = getSupportFragmentManager();
            for (int i = 0; i < fm.getBackStackEntryCount(); ++i) {
                fm.popBackStack();
            }
            selected_theme = 2;
        }
    }

    public void starFragment() {
        if (selected_theme != 3) {
            fragmentTransaction = fragmentManager.beginTransaction();
            fragment = new StarMonitorFragment();
            fragmentTransaction.replace(R.id.fragment_monitor_container, fragment);
            fragmentTransaction.commit();
            FragmentManager fm = getSupportFragmentManager();
            for (int i = 0; i < fm.getBackStackEntryCount(); ++i) {
                fm.popBackStack();
            }
            selected_theme = 3;
        }
    }

    /**
     * Date of commented : 27-10-2022
     * Commented By : Burra Kranthi Kiran
     * Description : The Below Code for the ServiceConnection as mention in the above this used get communication between server. Here there two state one is connected state and other one is disconnecting state.
     */
    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            isBound = true;
            PheezeeBleService.LocalBinder mLocalBinder = (PheezeeBleService.LocalBinder) service;
            mService = mLocalBinder.getServiceInstance();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
            mService = null;
        }
    };

    /**
     * Date of commented : 27-10-2022
     * Commented By : Burra Kranthi Kiran
     * Description : The below code if the user turn off the Bluetooth manually this will ask permission to enable ble
     */


    public void startBleRequest() {
        showToast("Bluetooth disabled");
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

    /**
     * Date of commented : 27-10-2022
     * Commented By : Burra Kranthi Kiran
     * Description : The below is function for gain Management. Increase and Decease.
     */
    public void increaseGain(){
        if(mService!=null){
            mService.increaseGain();
        }
    }

    public void decreaseGain(){
        if(mService!=null){
            mService.decreaseGain();
        }
    }

    /**
     * Date of commented : 27-10-2022
     * Commented By : Burra Kranthi Kiran
     * Description : the below code for session going on the background notification
     */
    public void disableNotificationOfSession(){
        if(mService!=null){
            mSessionStarted = false;
            mService.disableNotificationOfSession();
        }
    }

    /**
     * Date of commented : 27-10-2022
     * Commented By : Burra Kranthi Kiran
     * Description : Send Bodypart Information to the Device function
     * @param bodypart
     * @param body_orientation
     * @param patientname
     * @param exercise_position
     * @param muscle_position
     * @param bodypart_position
     * @param orientation_position
     */
    public void sendBodypartDataToDevice(String bodypart, int body_orientation, String patientname, int exercise_position,
                                         int muscle_position, int bodypart_position, int orientation_position){
        if(mService!=null){
            mSessionStarted = true;
            mService.sendBodypartDataToDevice(bodypart, body_orientation, patientname, exercise_position,
                    muscle_position, bodypart_position, orientation_position);
        }
    }

    /**
     * Date of commented : 27-10-2022
     * Commented By : Burra Kranthi Kiran
     * Description : This function is used to get session Data Information
     * @return
     */
    public Message getSessionData(){
        if(mService!=null) {
            Message msg = mService.getSessionData();
            return msg;
        }
        return null;
    }

    /**
     * Date of commented : 27-10-2022
     * Commented By : Burra Kranthi Kiran
     * Description : This is used for the pop function. Here the it show pop for the menu button for selection theme.
     */
    public void showPopup(View v){
        if(!mSessionStarted) {
            PopupMenu popup = new PopupMenu(this, v);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.theme_chooser, popup.getMenu());
            popup.setOnMenuItemClickListener(this);
            popup.show();
        }else {
            showToast("Stop the session to change theme!");
        }
    }

    /**
     * Date of commented : 27-10-2022
     * Commented By : Burra Kranthi Kiran
     * Description : This will as Activity result
     * @param requestCode
     * @param resultCode
     * @param data
     */

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }


    /**
     * Date of commented : 27-10-2022
     * Commented By : Burra Kranthi Kiran
     * Description : show Toast message function.
     * @param message
     */
    private void showToast(String message) {
        Toast.makeText(MonitorActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Date of commented : 27-10-2022
     * Commented By : Burra Kranthi Kiran
     * Description : We are using destroy concept for the after kill the activity the below function will run
     */
    @Override
    protected void onDestroy() {
        IS_SCEDULED_SESSION = false;
        IS_SCEDULED_SESSIONS_COMPLETED = false;
        IS_SESSION_SCEDULED_ON = "";
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                mService.disableNotificationOfSession();
            }
        });
        mSessionStarted=false;
        if(isBound){
            unbindService(mConnection);
        }
        repository.setOnSceduledSessionResponse(null);
        if(mTTS!=null){
            mTTS.stop();
            mTTS.shutdown();
        }
        super.onDestroy();
    }

    /**
     * Date of commented : 27-10-2022
     * Commented By : Burra Kranthi Kiran
     * Description : this is used clicked by user based on that theme show
     * @param item
     * @return
     */
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_normal_theme:{
                Log.i("Here","1");
                standardGoldTeachFragment();
                return true;
            }

            case R.id.menu_smiley_theme:{
                smileyFragment();
                Log.i("Here","2");
                return true;
            }

            case R.id.menu_star_theme:{
                starFragment();
                Log.i("Here","3");
                return true;
            }

            default:{
                return false;
            }
        }
    }

    /**
     * Date of commented : 27-10-2022
     * Commented By : Burra Kranthi Kiran
     * Description : the function is used to get Device Info. current we are not using in this activity but this is used on the Standard Fragment .
     */

    public void getBasicDeviceInfo() {
        if(mService!=null){
            mService.gerDeviceBasicInfo();
        }
    }
    /**
     * Date of commented : 27-10-2022
     * Commented By : Burra Kranthi Kiran
     * Description : the below function for session performance.
     */
    public boolean isSceduledSessionsCompleted(){
        if(sessions!=null){
            if(sessions.size()>0){
                return false;
            }else {
                return false;
            }
        }
        return true;
    }

    public SceduledSession getSceduledSessionListFirstItem(){
        if(sessions!=null){
            if(sessions.size()>0){
                return sessions.get(0);
            }else {
                return null;
            }
        }
        return null;
    }

    public int getSceduledSize() {
        if(sessions!=null){
            return sessions.size();
        }
        return 0;
    }

    public void removeFirstFromSceduledList() {
        if(sessions!=null && sessions.size()>0){
            repository.removeSceduledSessionFromDatabase(patientid,sessions.get(0).getSessionno());
            sessions.remove(0);
        }
    }
    /**
     * Date of commented : 27-10-2022
     * Commented By : Burra Kranthi Kiran
     * Description : this function for changing the current activity to previous activity.
     */
    @Override
    public void onBackPressed() {
        if(IS_SCEDULED_SESSION){
            if(IS_SCEDULED_SESSIONS_COMPLETED){
                Intent i = new Intent(MonitorActivity.this, PatientsView.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
            }
        }
        super.onBackPressed();
    }

    /**
     * Date of commented : 27-10-2022
     * Commented By : Burra Kranthi Kiran
     * Description : sceduled session function from above function.
     */

    public void sceduledSessionsHasBeenCompletedDialog() {
        AlertDialog.Builder sesssionsCompleted = new AlertDialog.Builder(this);
        sesssionsCompleted.setTitle("Sessions Completed");
        sesssionsCompleted.setMessage("All the sceduled sessions has been completed. Would you like to end or continue?");
        sesssionsCompleted.setPositiveButton("End", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent i = new Intent(MonitorActivity.this, PatientsView.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
            }
        });

        sesssionsCompleted.setNegativeButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        sesssionsCompleted.show();
    }
    /**
     * Date of commented : 27-10-2022
     * Commented By : Burra Kranthi Kiran
     * Description : the above function text to speach we are using below function.
     */
    public void textToSpeachVoice(String message){
        if(mTTS!=null){
            if(!currentMessageForTextToSpeach.equalsIgnoreCase(message)) {
                if(!mTTS.isSpeaking()){
                    mTTS.speak(message, TextToSpeech.QUEUE_FLUSH, null);
                    currentMessageForTextToSpeach = message;
                }
            }else {
                if(!mTTS.isSpeaking()){
                    mTTS.speak(message, TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        }
    }
}
