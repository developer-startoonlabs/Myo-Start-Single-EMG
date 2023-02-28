package com.start.apps.pheezee.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import start.apps.pheezee.R;
import com.start.apps.pheezee.classes.DeviceListClass;
import com.start.apps.pheezee.repository.MqttSyncRepository;
import com.start.apps.pheezee.utils.ByteToArrayOperations;
import com.start.apps.pheezee.utils.DeviceErrorCodesAndDialogs;
import com.start.apps.pheezee.utils.NetworkOperations;
import com.start.apps.pheezee.utils.ValueBasedColorOperations;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.start.apps.pheezee.App.CHANNEL_ID;

public class PheezeeBleService extends Service {
    private byte[] temp_info_packet = null;
    private String mHealthErrorString = "";
    private double latitude=0, longitude=0;
    private long first_scan = 0;
    private int num_of_scan = 0, mDeviceStatus=0;
    private boolean tooFrequentScan  =false, firmware_error = false;
    SharedPreferences preferences;
    private final String device_connected_notif = "Device Connected";
    private final String device_disconnected_notif = "Device not connected";
    private final String device_charging = "Device Connected, charging";
    private boolean first_packet = true;
    Message mMessage = new Message();

    boolean isConnectCommandGiven = false;
    //Intent Actions
    public static String device_state = "device.state";
    public static String bluetooth_state = "ble.state";
    public static String battery_percent = "battery.percent";
    public static String usb_state = "usb.state";
    public static String firmware_version = "firmware.version";
    public static String atiny_version = "atiny.version";
    public static String manufacturer_name = "manufacturer.name";
    public static String hardware_version = "hardware.version";
    public static String serial_id = "serial.id";
    public static String scanned_list = "scanned.list";
    public static String session_data = "session.data";
    public static String scan_state = "scan.state";
    public static String scan_too_frequent = "scan.too.frequent";
    public static String firmware_log = "firmware.log";
    public static String health_status = "health.status";
    public static String location_status = "location.status";
    public static String device_details_status = "device.details.status";
    public static String device_details_email = "device.details.email";
    public static String dfu_start_initiated = "dfu.start.initiated";
    public static String df_characteristic_written = "dfu.characteristic.written";
    public static String firmware_update_available = "firmware.update.available";
    public static String device_disconnected_firmware = "device.disconnected.firmware";
    public static String scedule_device_status_service = "scedule.device.status.service";
    public static String deactivate_device = "deactivate.device";
    public static String show_device_health_error = "show.device.health.error";
    public static String health_error_present_in_device = "health.error.present.in.device";
    public static String calibration_state = "calibration.state";
    public static String magnetometer_present = "magnetometer.present";

    public static boolean usb_interrupt_check=false;


    public static int jobid_firmware_log = 0;
    public static  int jobid_fimware_update = 1;
    public static int jobid_health_data = 2;
    public static int jobid_location_status = 3;
    public static int jobid_device_details_update = 4;
    public static int jobid_user_connected_update = 5;
    public static int jobid_device_status = 6;
    public static int jobid_sync_data_to_server = 6;









    //Service UUIDS
    public static final UUID generic_service_uuid = UUID.fromString("00001800-0000-1000-8000-00805f9b34fb");
    public static final UUID custom_service_uuid = UUID.fromString("909a1400-9693-4920-96e6-893c0157fedd");
    public static final UUID battery_service_uuid = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");
    public static final UUID device_info_service_uuid = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");
    public static final UUID dfu_service_uuid = UUID.fromString("0000fe59-0000-1000-8000-00805f9b34fb");





    //Characteristic
    public static final UUID device_name_characteristic_uuid = UUID.fromString("00002a00-0000-1000-8000-00805f9b34fb");
    public static final UUID custom_characteristic_uuid = UUID.fromString("909a1401-9693-4920-96e6-893c0157fedd");
    public static final UUID battery_level_characteristic_uuid = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");
    public static final UUID firmware_version_characteristic_uuid = UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb");
    public static final UUID serial_number_characteristic_uuid = UUID.fromString("00002a25-0000-1000-8000-00805f9b34fb");
    public static final UUID manufacturer_name_characteristic_uuid = UUID.fromString("00002a29-0000-1000-8000-00805f9b34fb");
    public static final UUID hardware_version_characteristic_uuid = UUID.fromString("00002a27-0000-1000-8000-00805f9b34fb");
    public static final UUID dfu_characteristic_uuid = UUID.fromString("8ec90003-f315-4f60-9fb8-838830daea50");


    //descriptor
    public static final UUID universal_descriptor = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");


    //Binder
    IBinder myServiceBinder = new LocalBinder();

    //Characteristic read list
    ArrayList<BluetoothGattCharacteristic> mCharacteristicReadList;

    private Boolean mDeviceState = false, mBluetoothState = false, mUsbState = false, mMagnetometerPresent = false;
    private int mBatteryPercent = 0;
    private String mFirmwareVersion = "", mSerialId = "", mManufacturerName = "", mAtinyVersion = "", mHardwareVersion="";
    private boolean mScanning = false, mDeviceHealthError = false;
    public String deviceMacc = "";
    ArrayList<DeviceListClass> mScanResults;
    private BtleScanCallback mScanCallback;
    BluetoothLeScanner mBluetoothLeScanner;
    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice remoteDevice;
    BluetoothGatt bluetoothGatt;
    BluetoothGattCharacteristic mCustomCharacteristic, mBatteryCharacteristic, mFirmwareVersionCharacteristic,
            mSerialIdCharacteristic, mManufacturerNameCharacteristic, mHardwareVersionCharacteristic, mDeviceNameCharacteristic, mDfuCharacteristic;

    BluetoothGattDescriptor mBatteryDescriptor, mDfuDescriptor, mCustomCharacteristicDescriptor;


    private String mCharacteristicWrittenValue = "";

    private MqttSyncRepository repository;

    HashMap<String, Integer> Bodypart_number = new HashMap<String, Integer>();

    public PheezeeBleService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return myServiceBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if(!Objects.requireNonNull(preferences.getString("deviceMacaddress", "")).equalsIgnoreCase(""))
            deviceMacc = preferences.getString("deviceMacaddress","");
        repository = new MqttSyncRepository(this.getApplication());

        Bodypart_number.put("Elbow", 0);
        Bodypart_number.put("Knee", 1);
        Bodypart_number.put("Ankle", 2);
        Bodypart_number.put("Hip", 3);
        Bodypart_number.put("Wrist", 4);
        Bodypart_number.put("Shoulder", 5);
        Bodypart_number.put("Forearm", 6);
        Bodypart_number.put("Spine", 7);
        Bodypart_number.put("Cervical", 7);
        Bodypart_number.put("Thoracic", 7);
        Bodypart_number.put("Lumbar", 7);

        Bodypart_number.put("Abdomen", 7);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showNotification(device_disconnected_notif);
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        mCharacteristicReadList = new ArrayList<>();
        if(bluetoothAdapter!=null && bluetoothAdapter.isEnabled()){
            mBluetoothState = true;
            bluetoothStateBroadcast();
            if(!deviceMacc.equalsIgnoreCase(""))
                startScanInBackground();
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothProfile.EXTRA_STATE);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(bluetoothReceiver, filter);
        return START_NOT_STICKY;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(bluetoothGatt!=null){
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
        }
        stopScaninBackground();
        disconnectDevice();
        unregisterReceiver(bluetoothReceiver);
        stopSelf();
    }

    public void startScanInBackground(){
        if(!tooFrequentScan) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    startScan();
                }
            });
        }else {
            sendTooFrequentScanBroadCast();
        }
    }

    public void stopScaninBackground(){
        if(!tooFrequentScan) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    long current_scan_time = Calendar.getInstance().getTimeInMillis();
                    int scan_difference = 0;
                    if (first_scan == 0) {
                        first_scan = Calendar.getInstance().getTimeInMillis();
                    } else {
                        scan_difference = (int) ((current_scan_time - first_scan) / 1000);
                    }
                    if (num_of_scan > 3 && scan_difference != 0 && scan_difference <= 30) {
                        tooFrequentScan = true;
                        sendTooFrequentScanBroadCast();
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                first_scan = 0;
                                num_of_scan = 0;
                                tooFrequentScan = false;
                                sendTooFrequentScanBroadCast();
                            }
                        }, 30000);
                    } else {
                        if(scan_difference>30){
                            scan_difference = 0;
                            first_scan = 0;
                            num_of_scan = 0;
                            tooFrequentScan = false;
                        }
                        stopScan();
                        num_of_scan++;
                    }
                }
            });
        }else {
            sendTooFrequentScanBroadCast();
        }
    }

    public void showNotification(String deviceState){
        Notification builder = new NotificationCompat.Builder(this,CHANNEL_ID)
                .setContentTitle("Pheezee")
                .setContentText(deviceState)
                .setSmallIcon(R.mipmap.pheezee_logos_final_square_round)
                .setColor(getResources().getColor(R.color.default_blue_light))
                .setDefaults(Notification.DEFAULT_ALL)
                .build();
        startForeground(1,builder);
    }


    @SuppressLint("MissingPermission")
    private void startScan() {
        List<ScanFilter> filters = new ArrayList<>();
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();
        if(!deviceMacc.equalsIgnoreCase("")){
            ScanFilter mFilter = new ScanFilter.Builder().setDeviceAddress(deviceMacc).build();
            filters.add(mFilter);
        }

        if(!mScanning && mBluetoothState) {
            mScanResults = new ArrayList<>();
            mScanCallback = new BtleScanCallback(mScanResults);
            mBluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
            if(mBluetoothLeScanner!=null) {
                mBluetoothLeScanner.startScan(filters, settings, mScanCallback);
                mScanning = true;
                sendScanStateBroadcast();
            }else {
                Toast.makeText(this, "Error: Restart the phone.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void setLatitudeAndLongitude(double latitude, double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public boolean isScanning(){
        return mScanning;
    }

    //Body part selection
    public boolean getDeviceState() {
        return mDeviceState;
    }

    public int getDeviceBatteryLevel(){
        return mBatteryPercent;
    }

    public boolean getUsbState(){
        return mUsbState;
    }

    public void sendTooFrequentScanBroadCast(){
        Intent i = new Intent(scan_too_frequent);
        i.putExtra(scan_too_frequent,tooFrequentScan);
        sendBroadcast(i);
    }

    public void sendDfuStartInitiated(boolean flag){
        Intent i = new Intent(dfu_start_initiated);
        i.putExtra(dfu_start_initiated,flag);
        sendBroadcast(i);
    }

    public void deviceStateBroadcast(){
        Intent i = new Intent(device_state);
        i.putExtra(device_state,mDeviceState);
        sendBroadcast(i);
    }

    public void updatePheezeeMac(String macaddress){
        this.deviceMacc = macaddress;
        if(mBluetoothState)
            startScanInBackground();
    }

    public void forgetPheezee(){
        this.deviceMacc = "";
        isConnectCommandGiven = false;
    }

    public void bluetoothStateBroadcast(){
        Intent i = new Intent(bluetooth_state);
        i.putExtra(bluetooth_state,mBluetoothState);
        sendBroadcast(i);
    }

    public void sendScanStateBroadcast(){
        Intent i = new Intent(scan_state);
        i.putExtra(scan_state,mScanning);
        sendBroadcast(i);
    }

    public void sendBatteryLevelBroadCast(){
        Intent i = new Intent(battery_percent);
        i.putExtra(battery_percent,String.valueOf(mBatteryPercent));
        sendBroadcast(i);
    }

    public void sendUsbStateBroadcast(){
        Intent i = new Intent(usb_state);
        i.putExtra(usb_state,mUsbState);
        sendBroadcast(i);
    }

    public void sendMagnetometerPresentState(){
        Intent i = new Intent(magnetometer_present);
        i.putExtra(magnetometer_present,mMagnetometerPresent);
        sendBroadcast(i);
    }

    public void sendDeviceDisconnectedBroadcast(){
        if(mDeviceStatus==1) {
            Intent i = new Intent(device_disconnected_firmware);
            i.putExtra(device_disconnected_firmware, true);
            sendBroadcast(i);
        }else {
            Intent i = new Intent(device_disconnected_firmware);
            i.putExtra(device_disconnected_firmware, false);
            sendBroadcast(i);
        }
    }

    public void sendErrorDeviceShowDialogBroadcast(){
        Intent i = new Intent(show_device_health_error);
        i.putExtra(show_device_health_error, "");
        sendBroadcast(i);
    }

    public void sendErrorPresentInDeviceHealthToRestrictSession(){
        Intent i = new Intent(health_error_present_in_device);
        i.putExtra(health_error_present_in_device,mDeviceHealthError);
        sendBroadcast(i);
    }

    public void sendFirmwareVersion(){
        Intent i = new Intent(firmware_version);
        i.putExtra(firmware_version,mFirmwareVersion);
        i.putExtra(atiny_version,mAtinyVersion);
        sendBroadcast(i);
    }

    public void sendSerialNumberBroadcast(){
        Intent i = new Intent(serial_id);
        i.putExtra(serial_id,mSerialId);
        sendBroadcast(i);
    }

    public void sendManufacturerName(){
        Intent i = new Intent(manufacturer_name);
        i.putExtra(manufacturer_name,mManufacturerName);
        sendBroadcast(i);
    }

    public void sendHardwareVersion(){
        Intent i = new Intent(hardware_version);
        i.putExtra(hardware_version,mHardwareVersion);
        sendBroadcast(i);
    }

    public void sendScannedListBroadcast(){
        Intent i = new Intent(scanned_list);
        i.putExtra(scanned_list,"");
        sendBroadcast(i);
    }

    public void sendSessionDataBroadcast(){
        Intent i = new Intent(session_data);
        i.putExtra(session_data,"");
        sendBroadcast(i);
    }

    public Message getSessionData(){
        return mMessage;
    }

    public ArrayList<DeviceListClass> getScannedList(){
        return mScanResults;
    }



    @SuppressLint("MissingPermission")
    public void stopScan(){
        if (mScanning && bluetoothAdapter != null && bluetoothAdapter.isEnabled() && mBluetoothLeScanner != null) {
            mBluetoothLeScanner.stopScan(mScanCallback);
        }
        mScanCallback = null;
        mScanning = false;
        sendScanStateBroadcast();
    }


    public void gerDeviceInfo(){
        bluetoothStateBroadcast();
        deviceStateBroadcast();
        sendBatteryLevelBroadCast();
        sendFirmwareVersion();
        deviceStateBroadcast();
        sendSerialNumberBroadcast();
        sendManufacturerName();
        sendHardwareVersion();
        sendDeviceDisconnectedBroadcast();
        sendMagnetometerPresentState();
    }

    public void gerDeviceBasicInfo(){
        bluetoothStateBroadcast();
        deviceStateBroadcast();
        sendBatteryLevelBroadCast();
        sendFirmwareVersion();
        deviceStateBroadcast();
        sendSerialNumberBroadcast();
        sendManufacturerName();
        sendHardwareVersion();
        sendUsbStateBroadcast();
    }

    public void increaseGain(){
        byte[] b = ByteToArrayOperations.hexStringToByteArray("AD01");
        writeCharacteristic(mCustomCharacteristic,b, "AD01");
    }

    public void decreaseGain(){
        byte[] b = ByteToArrayOperations.hexStringToByteArray("AD02");
        writeCharacteristic(mCustomCharacteristic,b,"AD02");
    }

    public void sendBodypartDataToDevice(String bodypart, int body_orientation, String patientName, int exercise_position,
                                         int muscle_position, int bodypart_position, int orientation_position){
        String session_performing_notif = "Device Connected, Session is going on ";
        showNotification(session_performing_notif +patientName);

        // When Abdomen is selected, extension is removed and current implementation checks the exercise position.
        // TODO: Improve the passing of exercise information using strings are similar to that of bodypart selected.
        if(bodypart_position==13 && exercise_position>=2) exercise_position=exercise_position+1;

        writeCharacteristic(mCustomCharacteristic, ValueBasedColorOperations.getParticularDataToPheeze(body_orientation, muscle_position, exercise_position, Bodypart_number.get(bodypart), orientation_position),"AE");
    }

    @SuppressLint("MissingPermission")
    public void disableNotificationOfSession(){
        showNotification(device_connected_notif);
        if(bluetoothGatt!=null && mCustomCharacteristicDescriptor!=null && mCustomCharacteristic!=null){
            bluetoothGatt.setCharacteristicNotification(mCustomCharacteristic,false);
            mCustomCharacteristicDescriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
            bluetoothGatt.writeDescriptor(mCustomCharacteristicDescriptor);
            bluetoothGatt.writeCharacteristic(mCustomCharacteristic);
        }
    }

    public void connectDevice(String deviceMacc){
        if(!isConnectCommandGiven) {
            isConnectCommandGiven = true;
            final BluetoothManager mbluetoothManager=(BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = mbluetoothManager.getAdapter();
            BluetoothDevice remoteDevice = bluetoothAdapter.getRemoteDevice(deviceMacc);
            this.remoteDevice = remoteDevice;
            new Handler(getMainLooper()).post(new Runnable() {
                @SuppressLint("MissingPermission")
                @Override
                public void run() {
                    bluetoothGatt = remoteDevice.connectGatt(getApplicationContext(), false, callback);
                    refreshDeviceCache(bluetoothGatt);
                }
            });
        }
    }

    @SuppressLint("MissingPermission")
    public void disconnectDevice() {
        if(bluetoothGatt==null){
            return;
        }
        bluetoothGatt.disconnect();
        bluetoothGatt.close();

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    class BtleScanCallback extends ScanCallback {
        private ArrayList<DeviceListClass> mScanResults;

        BtleScanCallback(ArrayList<DeviceListClass> mScanResults) {
            this.mScanResults = mScanResults;
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if(!deviceMacc.equalsIgnoreCase("") && !mDeviceState){
                BluetoothDevice device = result.getDevice();
                String deviceAddress = device.getAddress();
                if(deviceAddress.equalsIgnoreCase(deviceMacc)){
                    connectDevice(deviceMacc);
                }
            }else {
                addScanResult(result);
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult result : results) {
//                Log.i("device0",results.get(0).getDevice().getAddress());
                if(!deviceMacc.equalsIgnoreCase("") && !mDeviceState){
                    BluetoothDevice device = result.getDevice();
                    String deviceAddress = device.getAddress();
                    @SuppressLint("MissingPermission") String deviceName = device.getName();
                    if(deviceAddress.equalsIgnoreCase(deviceMacc)){
                        connectDevice(deviceMacc);
                    }
                }else {
                    addScanResult(result);
                }
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            //Log.e(TAG, "BLE Scan Failed with code " + errorCode);
        }

        private void addScanResult(ScanResult result) {
            String setDeviceBondState;
            BluetoothDevice device = result.getDevice();
            String deviceAddress = device.getAddress();
            @SuppressLint("MissingPermission") String deviceName = device.getName();

            if(deviceName==null)
                deviceName = "UNKNOWN DEVICE";

            long timeStamp = System.currentTimeMillis() -
                    SystemClock.elapsedRealtime() +
                    (result.getTimestampNanos() / 1000000);
            int deviceRssi = result.getRssi();
            @SuppressLint("MissingPermission") int deviceBondState = device.getBondState();
            //Just to update the bondstate if needed to
            if(deviceBondState == 0)
                setDeviceBondState = "BONDED";
            else
                setDeviceBondState = "NOT BONDED";

            //

            boolean flag = false, toBeUpdated = false;
            ArrayList<Integer> list = new ArrayList();
            for (int i = 0; i < mScanResults.size(); i++) {
                if (mScanResults.get(i).getDeviceMacAddress().equals(deviceAddress)) {
                    if(!Objects.equals(mScanResults.get(i).getDeviceBondState(), setDeviceBondState)){
                        mScanResults.get(i).setDeviceBondState(setDeviceBondState);
                    }

                    if (Integer.parseInt(mScanResults.get(i).getDeviceRssi())!= deviceRssi){
                        mScanResults.get(i).setDeviceRssi(""+deviceRssi);
                    }

                    long temp = timeStamp - mScanResults.get(i).getTimeStampNano();
                    mScanResults.get(i).setTimeStampNano(timeStamp);
                    flag = true;
                }else {
                    long currentTimeStamp = System.currentTimeMillis();
                    long temp = currentTimeStamp - mScanResults.get(i).getTimeStampNano();
                    if(temp>4000){
                        list.add(i);
                        toBeUpdated = true;
                    }
                }
            }



            if (!flag) {
                if(mDeviceState){
                    stopScaninBackground();
                }
                String str = "pheezee";
                if(deviceName.toLowerCase().contains(str)) {
                    DeviceListClass deviceListClass = new DeviceListClass();
                    deviceListClass.setDeviceName(deviceName);
                    deviceListClass.setDeviceMacAddress(deviceAddress);
                    deviceListClass.setDeviceRssi("" + deviceRssi);
                    deviceListClass.setTimeStampNano(timeStamp);
                    if (deviceBondState == 0)
                        deviceListClass.setDeviceBondState("BONDED");
                    else
                        deviceListClass.setDeviceBondState("NOT BONDED");

                    mScanResults.add(deviceListClass);

                    sendScannedListBroadcast();
                }
            }

            for (int i=0;i<list.size();i++){
                int a = list.get(i);
                try {
                    mScanResults.remove(a);
                }catch (ArrayIndexOutOfBoundsException e){
                    e.printStackTrace();
                }catch (IndexOutOfBoundsException e){
                    e.printStackTrace();
                }
            }
            if(toBeUpdated){
                sendScannedListBroadcast();
            }
        }
    }




    public BluetoothGattCallback callback = new BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    mCharacteristicReadList = new ArrayList<>();
                    mDeviceState = true;
                    gatt.discoverServices();
                    deviceStateBroadcast();
                    checkDeviceMacSavedOrNot();
                    showNotification(device_connected_notif);
                    stopScaninBackground();
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Message msg = Message.obtain();
                    mDeviceState = false;
                }
            }
            if(status == BluetoothGatt.GATT_FAILURE){
                Message msg = Message.obtain();
                msg.obj = "N/C";
                showNotification(device_disconnected_notif);
            }
        }
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status){
            bluetoothGatt = gatt;
//            mDeviceNameCharacteristic = gatt.getService(generic_service_uuid).getCharacteristic(device_name_characteristic_uuid);
            mCustomCharacteristic = gatt.getService(custom_service_uuid).getCharacteristic(custom_characteristic_uuid);
            mBatteryCharacteristic = gatt.getService(battery_service_uuid).getCharacteristic(battery_level_characteristic_uuid);
            mFirmwareVersionCharacteristic = gatt.getService(device_info_service_uuid).getCharacteristic(firmware_version_characteristic_uuid);
            mManufacturerNameCharacteristic = gatt.getService(device_info_service_uuid).getCharacteristic(manufacturer_name_characteristic_uuid);
            mHardwareVersionCharacteristic = gatt.getService(device_info_service_uuid).getCharacteristic(hardware_version_characteristic_uuid);
            mSerialIdCharacteristic = gatt.getService(device_info_service_uuid).getCharacteristic(serial_number_characteristic_uuid);
            try {
                mDfuCharacteristic = gatt.getService(dfu_service_uuid).getCharacteristic(dfu_characteristic_uuid);
            }catch (NullPointerException e){
                e.printStackTrace();
            }

            //Descriptors
            mCustomCharacteristicDescriptor = mCustomCharacteristic.getDescriptor(universal_descriptor);
            mBatteryDescriptor = mBatteryCharacteristic.getDescriptor(universal_descriptor);
            if(mDfuCharacteristic!=null)
                mDfuDescriptor = mDfuCharacteristic.getDescriptor(universal_descriptor);
            byte[] b = ByteToArrayOperations.hexStringToByteArray("AA02");
            writeCharacteristic(mCustomCharacteristic,b,"AA02");
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
//            if(descriptor.getCharacteristic().getUuid().equals(battery_level_characteristic_uuid))
//                bluetoothGatt.readCharacteristic(mFirmwareVersionCharacteristic);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if(characteristic.getUuid().equals(battery_level_characteristic_uuid)){
                byte[] b = characteristic.getValue();
                int battery  = b[0];
                int usb_state = b[1];

                if(Math.abs(mBatteryPercent - battery) > 9 && usb_state!=1)
                {
                    usb_interrupt_check=true;
                }

                if(usb_state==1) {
                    if(!mUsbState) {
                        mUsbState = true;
                        sendUsbStateBroadcast();
                        showNotification(device_charging);
                    }
                }
                else if(usb_state==0) {
                    if(mUsbState) {
                        mUsbState = false;
                        sendUsbStateBroadcast();
                        showNotification(device_connected_notif);
                    }
                }
                mBatteryPercent = battery;
                sendBatteryLevelBroadCast();
            }else if(characteristic.getUuid().equals(custom_characteristic_uuid)){
                byte[] temp_byte;
                temp_byte = characteristic.getValue();
                byte header_main = temp_byte[0];
                byte header_sub = temp_byte[1];
                //session related
                int sub_byte_size = temp_byte.length-2;
                byte[] sub_byte = new byte[sub_byte_size];
                if (ByteToArrayOperations.byteToStringHexadecimal(header_main).equals("AA")) {
                    if (ByteToArrayOperations.byteToStringHexadecimal(header_sub).equals("01")) {
                        int j = 2;
                        for (int i = 0; i < sub_byte_size; i++, j++) {
                            sub_byte[i] = temp_byte[j];
                        }
                        mMessage = Message.obtain();
                        mMessage.obj = sub_byte;
                        boolean sessionCompleted = false;
                        if (!sessionCompleted && !first_packet) {
                            sendSessionDataBroadcast();
                        } else {
                            first_packet = false;
                        }
                    }
                }else if (ByteToArrayOperations.byteToStringHexadecimal(header_main).equalsIgnoreCase("CA")){
                    int done = temp_byte[1] & 0xFF;
                    int successful = temp_byte[2] & 0xFF;
                    if(done==2){
                        if(successful==1){
                            sendCalibrationUpdate(true);
                        }else if(successful==2){
                            sendCalibrationUpdate(false);
                        }
                    }
                }
                if (ByteToArrayOperations.byteToStringHexadecimal(header_main).equals("AF")) {
//                    software_gain = header_sub;
                    mMessage = Message.obtain();
                    mMessage.obj = sub_byte;
                    sendSessionDataBroadcast();
                }
            }
        }
        @SuppressLint("MissingPermission")
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if(characteristic.getUuid().equals(custom_characteristic_uuid)){
                byte[] info_packet = characteristic.getValue();
                byte header_main = info_packet[0];
                byte header_sub = info_packet[1];
                temp_info_packet = info_packet;
                if(ByteToArrayOperations.byteToStringHexadecimal(header_main).equals("AA")) {
                    if (ByteToArrayOperations.byteToStringHexadecimal(header_sub).equals("02")) {
                        int battery = info_packet[11] & 0xFF;
                        int device_status = info_packet[12] & 0xFF;
                        int device_usb_state = info_packet[13] & 0xFF;
                        int error = info_packet[9] & 0xFF;
                        int device_disconnected = 0;
                        try {
                            device_disconnected = info_packet[43] & 0xFF;
                        }catch (ArrayIndexOutOfBoundsException e){
                            device_disconnected = 0;
                        }
                        mMagnetometerPresent = ByteToArrayOperations.getMagnetometerPresent(info_packet);
                        sendMagnetometerPresentState();
                        mDeviceStatus = device_disconnected;
                        sendDeviceDisconnectedBroadcast();
                        mAtinyVersion = String.valueOf(info_packet[10] & 0xFF);
                        if (error == 1) firmware_error = true;
                        else firmware_error = false;
                        //Remove later
                        if (device_usb_state == 1) {
                            mUsbState = true;
                            sendUsbStateBroadcast();
                            showNotification(device_charging);
                        } else if (device_status == 0) {
                            mUsbState = false;
                            sendUsbStateBroadcast();
                            showNotification(device_connected_notif);
                        }
                        boolean check_to_show_error_dialog = DeviceErrorCodesAndDialogs.doalogToShow(info_packet);
                        if(check_to_show_error_dialog){
                            mHealthErrorString = DeviceErrorCodesAndDialogs.getErrorCodeString(info_packet);
                            sendErrorDeviceShowDialogBroadcast();
                            boolean check_error_present = DeviceErrorCodesAndDialogs.isSessionRedirectionEnabled(info_packet);
                            Log.i("ERROR", String.valueOf(check_error_present));
                            if(check_error_present){
                                mDeviceHealthError = true;
                                sendErrorPresentInDeviceHealthToRestrictSession();
                            }else {
                                mDeviceHealthError = false;
                                sendErrorPresentInDeviceHealthToRestrictSession();
                            }
                        }
                        mBatteryPercent = battery;
                        sendBatteryLevelBroadCast();
                        writeCharacteristic(mCustomCharacteristic, ByteToArrayOperations.hexStringToByteArray("AA03"),"AA03");
                    }
                }else if(ByteToArrayOperations.byteToStringHexadecimal(header_main).equals("EE")){
                    if(repository!=null){
                        if(NetworkOperations.isNetworkAvailable(getApplicationContext()))
                            repository.sendFirmwareLogToTheServer(info_packet, deviceMacc, mFirmwareVersion, mSerialId,true,getApplicationContext());
                        else
                            repository.sendFirmwareLogToTheServer(info_packet, deviceMacc, mFirmwareVersion, mSerialId,false,getApplicationContext());
                    }
                }

            } else if(characteristic.getUuid().equals(firmware_version_characteristic_uuid)){
                byte[] b = characteristic.getValue();
                mFirmwareVersion = new String(b, StandardCharsets.UTF_8);
                sendFirmwareVersion();
                if(repository!=null){
                    if(NetworkOperations.isNetworkAvailable(getApplicationContext()))
                        repository.firmwareUpdateCheckAndGetLink(mFirmwareVersion,getApplicationContext());
                    else scheduleFirmwareUpdateCheckJob();
                }
                mCharacteristicReadList.add(mSerialIdCharacteristic);
                mCharacteristicReadList.add(mManufacturerNameCharacteristic);
                mCharacteristicReadList.add(mHardwareVersionCharacteristic);
            }else if(characteristic.getUuid().equals(serial_number_characteristic_uuid)){
                byte[] b = characteristic.getValue();
                mSerialId = new String(b, StandardCharsets.UTF_8);
                sendSerialNumberBroadcast();
                mCharacteristicReadList.remove(0);
            }else if(characteristic.getUuid().equals(manufacturer_name_characteristic_uuid)){
                byte[] b = characteristic.getValue();
                mManufacturerName = new String(b, StandardCharsets.UTF_8);
                sendManufacturerName();
                mCharacteristicReadList.remove(0);

            }else if(characteristic.getUuid().equals(hardware_version_characteristic_uuid)){
                byte[] b = characteristic.getValue();
                mHardwareVersion = new String(b, StandardCharsets.UTF_8);
                sendHardwareVersion();
                mCharacteristicReadList.remove(0);
//

                if(repository!=null){
                    if(temp_info_packet!=null) {
                        repository.sendDeviceDetailsToTheServer(temp_info_packet, getApplicationContext(), deviceMacc, mFirmwareVersion, mHardwareVersion,
                                mSerialId, mAtinyVersion);
                        repository.sendDeviceHealthStatusToTheServer(temp_info_packet, getApplicationContext());
                        repository.sendDeviceLocationStatusToTheServer(temp_info_packet, getApplicationContext(), latitude, longitude);
                        repository.sendPhizioEmailToTheServer(temp_info_packet, getApplicationContext());
                        repository.checkAndUpdateDeviceStatus(temp_info_packet,getApplicationContext(),mDeviceStatus);
                    }
                }
                gatt.setCharacteristicNotification(mBatteryCharacteristic, true);
                mBatteryDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                bluetoothGatt.writeDescriptor(mBatteryDescriptor);


                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(firmware_error){
                            byte[] error_code = ByteToArrayOperations.hexStringToByteArray("EE");
                            writeCharacteristic(mCustomCharacteristic,error_code,"EE");
                        }
                    }
                },500);

            }

            if(mCharacteristicReadList.size()>0){
                bluetoothGatt.readCharacteristic(mCharacteristicReadList.get(0));
            }
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            if(mCharacteristicWrittenValue.equalsIgnoreCase("AA02")) {
                bluetoothGatt.readCharacteristic(mCustomCharacteristic);
            }
            else{
                if(mCharacteristicWrittenValue.equals("AE")) {
                    bluetoothGatt.setCharacteristicNotification(mCustomCharacteristic, true);
                    mCustomCharacteristicDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    bluetoothGatt.writeDescriptor(mCustomCharacteristicDescriptor);
                }else if(mCharacteristicWrittenValue.contains("EE")){
                    bluetoothGatt.readCharacteristic(mCustomCharacteristic);
                }else if(mCharacteristicWrittenValue.equalsIgnoreCase("1")){
                    sendDfuCharacteristicWritten();
                }else if(mCharacteristicWrittenValue.equalsIgnoreCase("D1")){
                    mDeviceStatus = 1;
                    sendDeviceDisconnectedBroadcast();
                    if(repository!=null){
                        repository.deleteDeviceStatus(temp_info_packet);
                    }
                }else if(mCharacteristicWrittenValue.equalsIgnoreCase("D2")){
                    mDeviceStatus = 0;
                    sendDeviceDisconnectedBroadcast();
                }else if(mCharacteristicWrittenValue.equalsIgnoreCase("AA03")){
                    bluetoothGatt.readCharacteristic(mFirmwareVersionCharacteristic);
                }else if(mCharacteristicWrittenValue.equalsIgnoreCase("CA")){
                    bluetoothGatt.setCharacteristicNotification(mCustomCharacteristic, true);
                    mCustomCharacteristicDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    bluetoothGatt.writeDescriptor(mCustomCharacteristicDescriptor);
                }
            }
        }
    };

    private void sendCalibrationUpdate(boolean b) {
        Intent i = new Intent(calibration_state);
        i.putExtra(calibration_state,b);
        sendBroadcast(i);
    }


    private void checkDeviceMacSavedOrNot() {
        if(preferences.getString("deviceMacaddress","").equalsIgnoreCase("")){
            if(remoteDevice!=null){
                if(!remoteDevice.getAddress().equalsIgnoreCase("")){
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("deviceMacaddress",remoteDevice.getAddress());
                    editor.commit();
                }
            }
        }
    }

    private void scheduleFirmwareUpdateCheckJob() {
        ComponentName componentName = new ComponentName(this, FirmwareUpdatePresentService.class);
        JobInfo.Builder info = new JobInfo.Builder(jobid_fimware_update,componentName);
        info.setMinimumLatency(1000);
        info.setOverrideDeadline(3000);
        info.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        info.setRequiresCharging(false);
        JobScheduler jobScheduler = (JobScheduler)getSystemService(JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(info.build());
    }

    public String getHealthErrorString(){
        return mHealthErrorString;
    }


    public void writeToDfuCharacteristic(){
        byte[] b = "1".getBytes();
        writeCharacteristic(mDfuCharacteristic,b,"1");
    }

    @SuppressLint("MissingPermission")
    private void writeCharacteristic(BluetoothGattCharacteristic characteristic, byte[] b, String value) {
        if(bluetoothGatt!=null && characteristic!=null) {
            characteristic.setValue(b);
            mCharacteristicWrittenValue = value;
            bluetoothGatt.writeCharacteristic(characteristic);
        }
    }


    private boolean refreshDeviceCache(BluetoothGatt gatt){
        try {
            BluetoothGatt localBluetoothGatt = gatt;
            Method localMethod = localBluetoothGatt.getClass().getMethod("refresh", new Class[0]);
            if (localMethod != null) {
                return (Boolean) localMethod.invoke(localBluetoothGatt, new Object[0]);
            }
        }
        catch (Exception localException) {
            localException.printStackTrace();
        }
        return false;
    }



    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)){
                isConnectCommandGiven=false;mDeviceHealthError = false;mMagnetometerPresent = false;
                mHealthErrorString = "";
                mUsbState = false;
                mDeviceState = false;mFirmwareVersion="Null"; mSerialId="Null";mBatteryPercent = 0;mManufacturerName="Null";mHardwareVersion="Null";
                mAtinyVersion = "Null";
                mDeviceStatus=0;
                if(bluetoothGatt!=null) {
                    bluetoothGatt.disconnect();
                    bluetoothGatt.close();
                }
                showNotification(device_disconnected_notif);
                gerDeviceInfo();
                if(!deviceMacc.equalsIgnoreCase(""))
                    startScanInBackground();
            }
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_ON) {
                    isConnectCommandGiven = false;
                    mBluetoothState = true;
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            if(!deviceMacc.equalsIgnoreCase(""))
                                startScanInBackground();
                            bluetoothStateBroadcast();
                        }
                    });
                }
                if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_OFF) {
                    mUsbState = false;mHealthErrorString = "";mMagnetometerPresent = false;
                    isConnectCommandGiven = false;
                    mDeviceStatus=0;
                    mBluetoothState = false;mDeviceState = false;mFirmwareVersion="Null"; mSerialId="NULL";mBatteryPercent = 0;mManufacturerName="Null";
                    mHardwareVersion="Null";
                    mAtinyVersion = "Null";
                    showNotification(device_disconnected_notif);
                    gerDeviceInfo();
                }
            }

        }
    };

    public class LocalBinder extends Binder {
        public PheezeeBleService getServiceInstance(){
            return PheezeeBleService.this;
        }
    }

    public void writeCalibrationToCustomCharacteristic(){
        writeCharacteristic(mCustomCharacteristic, ByteToArrayOperations.hexStringToByteArray("CA"),"CA");
    }

    private void sendDfuCharacteristicWritten(){
        Intent i = new Intent(df_characteristic_written);
        i.putExtra(df_characteristic_written,"");
        sendBroadcast(i);
    }

    public String getMacAddress(){
        return remoteDevice.getAddress();
    }

    @SuppressLint("MissingPermission")
    public String getDeviceName(){
        return remoteDevice.getName();
    }

    public int getDeviceDeactivationStatus(){
        return mDeviceStatus;
    }
    public void deactivateDevice(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                byte[] b = ByteToArrayOperations.hexStringToByteArray("D1");
                writeCharacteristic(mCustomCharacteristic,b,"D1");
            }
        },2000);

    }

    public void reactivateDevice(){
        byte[] b = ByteToArrayOperations.hexStringToByteArray("D2");
        writeCharacteristic(mCustomCharacteristic,b,"D2");
    }

    public byte[] getInfoPacket(){
        return temp_info_packet;
    }
}
