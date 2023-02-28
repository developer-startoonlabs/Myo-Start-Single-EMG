package com.start.apps.pheezee.repository;

import static androidx.core.content.ContextCompat.startActivity;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import androidx.lifecycle.LiveData;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.start.apps.pheezee.activities.WarrantyDetails;
import com.start.apps.pheezee.pojos.AddPatientData;
import com.start.apps.pheezee.pojos.CalbrationData;
import com.start.apps.pheezee.pojos.CommentSessionUpdateData;
import com.start.apps.pheezee.pojos.ConfirmEmailAndPackageId;
import com.start.apps.pheezee.pojos.DeletePatientData;
import com.start.apps.pheezee.pojos.DeletePhiziouserData;
import com.start.apps.pheezee.pojos.DeleteSessionData;
import com.start.apps.pheezee.pojos.DeviceDeactivationStatus;
import com.start.apps.pheezee.pojos.DeviceDeactivationStatusResponse;
import com.start.apps.pheezee.pojos.DeviceDetailsData;
import com.start.apps.pheezee.pojos.DeviceLocationStatus;
import com.start.apps.pheezee.pojos.FirmwareData;
import com.start.apps.pheezee.pojos.FirmwareUpdateCheck;
import com.start.apps.pheezee.pojos.FirmwareUpdateCheckResponse;
import com.start.apps.pheezee.pojos.ForgotPassword;
import com.start.apps.pheezee.pojos.GetReportData;
import com.start.apps.pheezee.pojos.GetReportDataResponse;
import com.start.apps.pheezee.pojos.HealthData;
import com.start.apps.pheezee.pojos.LoginData;
import com.start.apps.pheezee.pojos.LoginResult;
import com.start.apps.pheezee.pojos.MmtData;
import com.start.apps.pheezee.pojos.MobileToken;
import com.start.apps.pheezee.pojos.PatientDetailsData;
import com.start.apps.pheezee.pojos.PatientImageData;
import com.start.apps.pheezee.pojos.PatientImageUploadResponse;
import com.start.apps.pheezee.pojos.PatientStatusData;
import com.start.apps.pheezee.pojos.PhizioDetailsData;
import com.start.apps.pheezee.pojos.PhizioEmailData;
import com.start.apps.pheezee.pojos.ResponseData;
import com.start.apps.pheezee.pojos.SceduledSessionNotSaved;
import com.start.apps.pheezee.pojos.SerialData;
import com.start.apps.pheezee.pojos.SessionData;
import com.start.apps.pheezee.pojos.SessionResult;
import com.start.apps.pheezee.pojos.SignUpData;
import com.start.apps.pheezee.pojos.WarrantyData;
import com.start.apps.pheezee.retrofit.GetDataService;
import com.start.apps.pheezee.retrofit.RetrofitClientInstance;
import com.start.apps.pheezee.room.Dao.DeviceStatusDao;
import com.start.apps.pheezee.room.Dao.MqttSyncDao;
import com.start.apps.pheezee.room.Dao.PhizioPatientsDao;
import com.start.apps.pheezee.room.Dao.SceduledSessionDao;
import com.start.apps.pheezee.room.Entity.DeviceStatus;
import com.start.apps.pheezee.room.Entity.MqttSync;
import com.start.apps.pheezee.room.Entity.PhizioPatients;
import com.start.apps.pheezee.room.Entity.SceduledSession;
import com.start.apps.pheezee.room.PheezeeDatabase;
import com.start.apps.pheezee.utils.BitmapOperations;
import com.start.apps.pheezee.utils.ByteToArrayOperations;
import com.start.apps.pheezee.utils.NetworkOperations;
import com.start.apps.pheezee.utils.OtpGeneration;
import com.start.apps.pheezee.utils.WriteResponseBodyToDisk;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.facebook.FacebookSdk.getApplicationContext;
import static com.start.apps.pheezee.activities.PatientsView.json_phizioemail;
import static com.start.apps.pheezee.services.PheezeeBleService.deactivate_device;
import static com.start.apps.pheezee.services.PheezeeBleService.device_details_email;
import static com.start.apps.pheezee.services.PheezeeBleService.device_details_status;
import static com.start.apps.pheezee.services.PheezeeBleService.firmware_log;
import static com.start.apps.pheezee.services.PheezeeBleService.firmware_update_available;
import static com.start.apps.pheezee.services.PheezeeBleService.health_status;
import static com.start.apps.pheezee.services.PheezeeBleService.location_status;
import static com.start.apps.pheezee.services.PheezeeBleService.scedule_device_status_service;

/**
 * That interacts with database
 */
public class MqttSyncRepository {
    private SceduledSessionDao sceduledSessionDao;
    private MqttSyncDao mqttSyncDao;
    private DeviceStatusDao deviceStatusDao;
    private PhizioPatientsDao phizioPatientsDao;
    private GetDataService getDataService;
    private onServerResponse listner;
    private OnLoginResponse loginlistner;
    private OnSignUpResponse signUpResponse;
    private OnDeletePhiziouser deletePhiziouser;
    private OnPhizioDetailsResponseListner phizioDetailsResponseListner;
    private OnReportDataResponseListner reportDataResponseListner;
    private GetSessionNumberResponse response;
    private OnSessionDataResponse onSessionDataResponse;
    private onDeviceStatusResponse onDeviceStatusResponse;
    private onSceduledSesssionResponse onSceduledSessionResponse;
    /**
     * Live object returned to get the item count in the database to update the sync button view
     */
    private LiveData<Long> count;
    private LiveData<List<PhizioPatients>> patients;
    private LiveData<List<SceduledSession>> sessions;
    private PheezeeDatabase database;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    private String timestamp;

    public MqttSyncRepository(Application application) {
        database = PheezeeDatabase.getInstance(application);
        mqttSyncDao = database.mqttSyncDao();
        phizioPatientsDao = database.phizioPatientsDao();
        deviceStatusDao = database.deviceStatusDao();
        sceduledSessionDao = database.sceduledSessionDao();
        this.count = mqttSyncDao.getEntityCount();
        this.patients = phizioPatientsDao.getAllActivePatients();
        getDataService = RetrofitClientInstance.getRetrofitInstance().create(GetDataService.class);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(application);
    }
//    Toast
    public void showToast(String message){
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();
    }
    //Local database functions
    public LiveData<List<PhizioPatients>> getAllPatietns() {
        return patients;
    }

    public LiveData<List<SceduledSession>> getAllSceduledSessions(String patientid){
        this.sessions = sceduledSessionDao.getAllSceduledSessionOfPatient(patientid);
        return sessions;
    }

    public void removeSceduledSessionFromDatabase(String patientid, int sessionno){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                sceduledSessionDao.removeSceduledSessionBasedOnSessionNo(patientid,sessionno);
            }
        });
    }

    public void removeAllSessionsForPataient(String patientid){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                sceduledSessionDao.delteAllSessionOfAPatient(patientid);
                phizioPatientsDao.updateSceduledSessionStatus(patientid, false);
            }
        });
    }

    public void getAllSceduledSessionsList(String patientid){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                List<SceduledSession> sessions = sceduledSessionDao.getAllSceduledSession(patientid);
                if(onSceduledSessionResponse!=null){
                    onSceduledSessionResponse.onResponse(sessions);
                }
            }
        });
    }

    public void clearDatabase() {
        new DeleteDatabase(database).execute();
    }

    public LiveData<Long> getCount() {
        return count;
    }

    private void updatePatientLocally(PhizioPatients patient) {
        new UpdatePatient(phizioPatientsDao).execute(patient);
    }
//    private void updatePatientDetails()

    public void insertPatient(PhizioPatients patient, PatientDetailsData data) {
        new InsertPhizioPatient(phizioPatientsDao).execute(patient);
        new SendDataAsyncTask(phizioPatientsDao).execute(data);
    }

    public void getPatientSessionNo(String patientid) {
        new GetSessionNumber(phizioPatientsDao).execute(patientid);
    }

    public void setPatientSessionNumber(String sessionno, String patientid) {
        new SetPatientSessionNumber(phizioPatientsDao).execute(sessionno, patientid);
    }

    /**
     * Called when pressed logout
     */
    public void deleteAllSync() {
        new DeleteAllMqttSync(mqttSyncDao).execute();
    }

    private void deleteParticular(int id) {
        new DeleteMqttSyncAsyncTask(mqttSyncDao).execute(id);
    }

//    public void deletePhiziouserFromServer(String phizioemail, String feedback, String todelete, Integer needdata) {
//        DeletePhiziouserData data = new DeletePhiziouserData(phizioemail, feedback, todelete, needdata);
//
//        Call<String> deleteusercall = getDataService.DeletePhiziouser(data);
//
//        deleteusercall.enqueue(new Callback<String>() {
//            @Override
//            public void onResponse(Call<String> call, Response<String> response) {
//                if(response.code() == 200 && response.body() == "account deleted and data sent") {
//                    Log.i("Tag", response.body());
//                } else {
//                    Log.i("Tag", response.body());
//                }
//            }
//
//            @Override
//            public void onFailure(Call<String> call, Throwable t) {
//                Log.i("Failure", "Failed");
//            }
//        });
//    }

    public void warrantyData(String str_uid) {
        WarrantyData tv_device_mamc = new WarrantyData(str_uid);
        Call<String> warrantycall = getDataService.warrantyDetails(tv_device_mamc);
        warrantycall.enqueue(new Callback<String>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.code() == 200)

                    {
                        String value = response.body();
                        editor = sharedPref.edit();
                        editor.putString("time_stamp",value);
                        editor.apply();
                }
                else if(response.code() ==200);
                {
                    String value = response.body();
                    value = null;

                }

            }


            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.i("Failure", "Failed");
            }
        });
    }

    private void startActivity(Intent intent) {

    }

    public void cal(String email_id,String date_stamp,String time_stamp) {

        CalbrationData data = new CalbrationData(email_id,date_stamp,time_stamp);
        Call<String> cal = getDataService.cal(data);
        cal.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.code() == 200) {
                    Log.i("Toast", response.body());
                }

            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e("Failure", "It Failed...");

            }
        });
    }




    public void serialnumber(String str_devicemac) {

        SerialData tv_device_mamc = new SerialData(str_devicemac);
        Call<String> serialcall = getDataService.serialnumber(tv_device_mamc);
        serialcall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.code() == 200){
                    String value2 = response.body();
                    editor = sharedPref.edit();
                    editor.putString("serial_number",value2);
                    editor.apply();
                }


            }

            private void startActivity(Intent intn) {
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.i("Failure", "Failed");
            }
        });
    }



    private static class DeleteMqttSyncAsyncTask extends AsyncTask<Integer, Void, Void> {

        private MqttSyncDao mqttSyncDao;

        DeleteMqttSyncAsyncTask(MqttSyncDao mqttSyncDao) {
            this.mqttSyncDao = mqttSyncDao;
        }

        @Override
        protected Void doInBackground(Integer... integers) {

            mqttSyncDao.deleteParticular(integers[0]);
            return null;
        }
    }


    private static class DeleteAllMqttSync extends AsyncTask<Void, Void, Void> {
        private MqttSyncDao mqttSyncDao;

        DeleteAllMqttSync(MqttSyncDao mqttSyncDao) {
            this.mqttSyncDao = mqttSyncDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mqttSyncDao.deleteAllMqttSync();
            return null;
        }
    }

    private static class DeleteDatabase extends AsyncTask<Void, Void, Void> {
        private PheezeeDatabase database;

        DeleteDatabase(PheezeeDatabase database) {
            this.database = database;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            database.clearAllTables();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    private static class UpdatePatient extends AsyncTask<PhizioPatients, Void, Void> {
        PhizioPatientsDao phizioPatientsDao;

        UpdatePatient(PhizioPatientsDao dao) {
            this.phizioPatientsDao = dao;
        }

        @Override
        protected Void doInBackground(PhizioPatients... patients) {
            phizioPatientsDao.update(patients[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    private static class InsertPhizioPatient extends AsyncTask<PhizioPatients, Void, Void> {
        PhizioPatientsDao phizioPatientsDao;

        InsertPhizioPatient(PhizioPatientsDao dao) {
            this.phizioPatientsDao = dao;
        }

        @Override
        protected Void doInBackground(PhizioPatients... patients) {
            phizioPatientsDao.insert(patients[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    private static class DeletePatient extends AsyncTask<PhizioPatients, Void, Void> {
        PhizioPatientsDao phizioPatientsDao;

        DeletePatient(PhizioPatientsDao dao) {
            this.phizioPatientsDao = dao;
        }

        @Override
        protected Void doInBackground(PhizioPatients... patient) {
            phizioPatientsDao.delete(patient[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class InsertAllPatients extends AsyncTask<List<PhizioPatients>, Void, Void> {
        PhizioPatientsDao patientsDao;

        InsertAllPatients(PhizioPatientsDao patientsDao) {
            this.patientsDao = patientsDao;
        }

        @SafeVarargs
        @Override
        protected final Void doInBackground(List<PhizioPatients>... lists) {
            patientsDao.insertAllPatients(lists[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            String email = "";
            try {
                JSONObject object = new JSONObject(sharedPref.getString("phiziodetails", ""));
                email = object.getString("phizioname");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (loginlistner != null) {
                loginlistner.onLoginResponse(true, email);
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class GetSessionNumber extends AsyncTask<String, Void, String> {
        PhizioPatientsDao patientsDao;

        GetSessionNumber(PhizioPatientsDao patientsDao) {
            this.patientsDao = patientsDao;
        }

        @Override
        protected String doInBackground(String... strings) {
            String sessionno = patientsDao.getPatientSessionNumber(strings[0]);
            int sessionnum = Integer.parseInt(sessionno);
            sessionnum += 1;
            sessionno = String.valueOf(sessionnum);
            return sessionno;
        }

        @Override
        protected void onPostExecute(String s) {
            if (response != null) {
                response.onSessionNumberResponse(s);
            }
            super.onPostExecute(s);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class UpdatePatientProfilePicUrl extends AsyncTask<PatientImageUploadResponse, Void, Void> {
        PhizioPatientsDao patientsDao;

        UpdatePatientProfilePicUrl(PhizioPatientsDao patientsDao) {
            this.patientsDao = patientsDao;
        }

        @Override
        protected Void doInBackground(PatientImageUploadResponse... responses) {
            patientsDao.updatePatientProfilePicUrl(responses[0].getUrl(), responses[0].getPatientid());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class SetPatientSessionNumber extends AsyncTask<String, Void, Void> {

        PhizioPatientsDao patientsDao;

        SetPatientSessionNumber(PhizioPatientsDao patientsDao) {
            this.patientsDao = patientsDao;
        }

        @Override
        protected Void doInBackground(String... strings) {
            patientsDao.setNumberOfSessions(strings[0], strings[1]);
            return null;
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class DeleteMultipleSyncItem extends AsyncTask<List<Integer>, Void, Void> {

        MqttSyncDao mqttSyncDao;

        DeleteMultipleSyncItem(MqttSyncDao mqttSyncDao) {
            this.mqttSyncDao = mqttSyncDao;
        }

        @SafeVarargs
        @Override
        protected final Void doInBackground(List<Integer>... lists) {
            mqttSyncDao.deleteMultipleItems(lists[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (listner != null) {
                listner.onSyncComplete(true, "Sync Completed");
                /*if(count.getValue()>0){
                    listner.onSyncComplete(false, "Sync unsuccessful, please try again later!");
                }else {
                    listner.onSyncComplete(true, "Sync Completed");
                }*/
            }
            super.onPostExecute(aVoid);
        }
    }

    /**
     * Stores the topic and message in database and sends to the server if internet is available.
     */
    @SuppressLint("StaticFieldLeak")
    public class SendDataAsyncTask extends AsyncTask<PatientDetailsData, Void, JSONObject> {

        PhizioPatientsDao patientsDao;

        SendDataAsyncTask(PhizioPatientsDao patientsDao) {
            this.patientsDao = patientsDao;
        }

        @Override
        protected JSONObject doInBackground(PatientDetailsData... patientDetailsData) {
            JSONObject object = null;
            try {
                Gson gson = new GsonBuilder().create();
                String json = gson.toJson(patientDetailsData[0]);
                object = new JSONObject(json);
                String mqtt_publish_phizio_addpatient = "phizio/addpatient";
                MqttSync mqttSync = new MqttSync(mqtt_publish_phizio_addpatient, object.toString());
                object.put("id", database.mqttSyncDao().insert(mqttSync));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return object;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            Gson gson = new Gson();
            AddPatientData data = gson.fromJson(jsonObject.toString(), AddPatientData.class);
            Call<ResponseData> add_patient_call = getDataService.addPatient(data);
            add_patient_call.enqueue(new Callback<ResponseData>() {
                @Override
                public void onResponse(@NonNull Call<ResponseData> call, @NonNull Response<ResponseData> response) {
                    if (response.code() == 200) {
                        ResponseData responseData = response.body();
                        if (responseData != null) {
                            if (responseData.getResponse().equalsIgnoreCase("inserted"))
                                deleteParticular(responseData.getId());
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseData> call, @NonNull Throwable t) {
                }
            });
        }
    }

    public void insetTheSceduledSessionDetails(JSONObject object){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    ArrayList<SceduledSession> sceduledSessions = new ArrayList<>();
                    String patientid = object.getString("patientid");
                    boolean check_if_already_sceduled = phizioPatientsDao.isSessionSceduled(patientid);
                    Log.i("SCEDULED1", String.valueOf(check_if_already_sceduled));
                    Long count = sceduledSessionDao.getSessionPresent(patientid);
                    Log.i("NUM OF Sessions", String.valueOf(count));
                    if(count!=0){
                        sceduledSessionDao.delteAllSessionOfAPatient(patientid);
                    }
                    phizioPatientsDao.updateSceduledSessionStatus(patientid,true);
                    boolean check_if_sceduled = phizioPatientsDao.isSessionSceduled(patientid);
                    Log.d("SCEDULED", String.valueOf(check_if_sceduled));
                    if(check_if_sceduled) {
                        JSONArray array = new JSONArray(object.getString("body"));
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject object = array.getJSONObject(i);
                            SceduledSession session = new SceduledSession(patientid, (i + 1), object.getString("bodypart"), object.getString("side"),
                                    object.getString("position"), object.getString("exercise"), object.getString("muscle"), object.getString("reps"),
                                    object.getString("emg"), object.getString("angleMin"), object.getString("angleMax"));
                            sceduledSessions.add(session);
                        }
                        sceduledSessionDao.insertAllSceduledSessions(sceduledSessions);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //Server related
    public void deletePatientFromServer(DeletePatientData deletePatientData, PhizioPatients patient) {
        Call<String> delete_call = getDataService.deletePatient(deletePatientData);
        delete_call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.code() == 200) {
                    String response_delete = response.body();
                    if (response_delete != null) {
                        if (response_delete.equalsIgnoreCase("deleted")) {
                            new DeletePatient(phizioPatientsDao).execute(patient);
                            if (listner != null) {
                                listner.onDeletePateintResponse(true);
                            }
                        }
                    } else {
                        if (listner != null)
                            listner.onUpdatePatientDetailsResponse(false);
                    }
                } else {
                    if (listner != null)
                        listner.onUpdatePatientDetailsResponse(false);
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                if (listner != null)
                    listner.onDeletePateintResponse(false);
            }
        });
    }

    //Update the patient status on server
    public void updatePatientStatusServer(PhizioPatients patient, PatientStatusData data) {
        Call<String> call = getDataService.updatePatientStatus(data);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.code() == 200) {
                    String status_response = response.body();
                    if (status_response != null) {
                        if (status_response.equalsIgnoreCase("inserted")) {
                            updatePatientLocally(patient);
                            if (listner != null) {
                                listner.onUpdatePatientStatusResponse(true);
                            }
                        } else {
                            listner.onUpdatePatientStatusResponse(false);
                        }
                    } else {
                        if (listner != null) {
                            listner.onUpdatePatientStatusResponse(false);
                        }
                    }
                } else {
                    listner.onUpdatePatientDetailsResponse(false);
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                if (listner != null)
                    listner.onUpdatePatientStatusResponse(false);
            }
        });
    }

    //update patient details on the server
    public void updatePatientDetailsServer(PhizioPatients patient, PatientDetailsData data) {
        Call<String> call = getDataService.updatePatientDetails(data);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.code() == 200) {
                    String update_response = response.body();
                    if (update_response != null) {
                        if (update_response.equalsIgnoreCase("updated")) {
                            updatePatientLocally(patient);
                            if (listner != null)
                                listner.onUpdatePatientDetailsResponse(true);
                        } else {
                            listner.onUpdatePatientDetailsResponse(false);
                        }
                    } else {
                        if (listner != null)
                            listner.onUpdatePatientDetailsResponse(false);
                    }
                } else {
                    if (listner != null)
                        listner.onUpdatePatientDetailsResponse(false);
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                if (listner != null)
                    listner.onUpdatePatientDetailsResponse(false);
            }
        });
    }

    public void forgotPassword(String email) {
        final String otp = OtpGeneration.OTP(4);
        ForgotPassword password = new ForgotPassword(email, otp);
        Call<String> forgot_password = getDataService.forgotPassword(password);
        forgot_password.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                String res = response.body();
                if (res != null) {
                    if (res.equalsIgnoreCase("invalid")) {
                        if (loginlistner != null) {
                            loginlistner.onForgotPasswordResponse(false, "Invalid email!");
                        }
                    } else if (res.equalsIgnoreCase("nsent")) {
                        if (loginlistner != null) {
                            loginlistner.onForgotPasswordResponse(false, "Email not sent!");
                        }
                    } else {
                        if (loginlistner != null) {
                            loginlistner.onForgotPasswordResponse(true, otp);
                        }
                    }
                } else {
                    if (loginlistner != null)
                        loginlistner.onForgotPasswordResponse(false, "Invalid, try again later!");
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                if (loginlistner != null) {
                    loginlistner.onForgotPasswordResponse(false, "Email not sent!");
                }
            }
        });
    }

    public void updatePassword(String email, String password) {
        LoginData data = new LoginData(email, password);
        Call<String> update_password_call = getDataService.updatePassword(data);
        update_password_call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.code() == 200) {
                    String res = response.body();
                    if (res != null) {
                        if (res.equalsIgnoreCase("updated")) {
                            if (loginlistner != null)
                                loginlistner.onPasswordUpdated("Password updated!");
                        } else {
                            if (loginlistner != null)
                                loginlistner.onPasswordUpdated("Error please try later");
                        }
                    } else {
                        if (loginlistner != null)
                            loginlistner.onPasswordUpdated("Error please try later");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                if (loginlistner != null)
                    loginlistner.onPasswordUpdated("Error please try later");
            }
        });
    }
    public void updateApp_version(String email, String app_version) {
        LoginData data = new LoginData(email, app_version);
        Call<String> update_appversion_call = getDataService.updateApp_version(data);
        update_appversion_call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.code() == 200) {
                    String res = response.body();
                    if (res != null) {
//                        if (res.equalsIgnoreCase("updated")) {
//                            if (loginlistner != null)
//                                loginlistner.onPasswordUpdated("Password updated!");
//                        } else {
//                            if (loginlistner != null)
//                                loginlistner.onPasswordUpdated("Error please try later");
//                        }
                    } else {
//                        if (loginlistner != null)
//                            loginlistner.onPasswordUpdated("Error please try later");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                if (loginlistner != null)
                    loginlistner.onPasswordUpdated("Error please try later");
            }
        });
    }

    public void confirmEmail(String email) {
        final String otp = OtpGeneration.OTP(4);
        ConfirmEmailAndPackageId password = new ConfirmEmailAndPackageId(email, otp);
        Call<String> confirm_email = getDataService.confirmEmail(password);
        confirm_email.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.code() == 200) {
                    String res = response.body();
                    if (res != null) {
                        if (res.equalsIgnoreCase("sent")) {
                            if (signUpResponse != null) {
                                signUpResponse.onConfirmEmail(true, otp);
                            }
                        } else if (res.equalsIgnoreCase("already")) {
                            if (signUpResponse != null) {
                                signUpResponse.onConfirmEmail(false, "Email already present!");
                            }
                        } else if(res.equalsIgnoreCase("packagealready")){
                            signUpResponse.onConfirmEmail(false, "Package id already being used");
                        }else if(res.equalsIgnoreCase("invalidpackageid")){
                            signUpResponse.onConfirmEmail(false, "Invalid package id");
                        } else {
                            if (signUpResponse != null) {
                                signUpResponse.onConfirmEmail(false, "Connection error, Please try again later!");
                            }
                        }
                    } else {
                        if (signUpResponse != null) {
                            signUpResponse.onConfirmEmail(false, "Error please try again later");
                        }
                    }
                } else {
                    if (signUpResponse != null) {
                        signUpResponse.onConfirmEmail(false, "Error please try again later");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                if (signUpResponse != null) {
                    signUpResponse.onConfirmEmail(false, "Error please try again later");
                }
            }
        });
    }

    public void signUp(SignUpData data) {
        Call<String> sign_up = getDataService.signUp(data);
        sign_up.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.code() == 200) {
                    String res = response.body();
                    if (res != null) {
                        if (res.equalsIgnoreCase("inserted")) {
                            editor = sharedPref.edit();
                            editor.putBoolean("isLoggedIn", true);
                            JSONObject jsonObject = new JSONObject();
                            try {
                                jsonObject.put("phizioname", data.getPhizioname());
                                jsonObject.put("phizioemail", data.getPhizioemail());
                                jsonObject.put("phiziophone", data.getPhiziophone());
                                jsonObject.put("phizioprofilepicurl", data.getPhizioprofilepicurl());
                                jsonObject.put("packagetype", 4);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            editor.putString("phiziodetails", jsonObject.toString());
                            editor.apply();
                            if (sharedPref.getInt("maxid", -1) == -1) {
                                editor = sharedPref.edit();
                                editor.putInt("maxid", 0);
                                editor.apply();
                            }
                            if (signUpResponse != null) {
                                signUpResponse.onSignUp(true);
                            }
                        } else if(res.equalsIgnoreCase("already")){
                            if (signUpResponse != null) {
                                signUpResponse.onSignUp(false);
                            }
                        }
                    }
                } else {
                    if (signUpResponse != null) {
                        signUpResponse.onSignUp(false);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                if (signUpResponse != null) {
                    signUpResponse.onSignUp(false);
                }
            }
        });
    }


    public void loginUser(String email, String password) {
        editor = sharedPref.edit();
        final int[] maxid = {0};
        Call<List<LoginResult>> login = getDataService.login(new LoginData(email, password));
        login.enqueue(new Callback<List<LoginResult>>() {
            @SuppressLint("ApplySharedPref")
            @Override
            public void onResponse(@NonNull Call<List<LoginResult>> call, @NonNull Response<List<LoginResult>> response) {
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        List<LoginResult> results = response.body();
                        if (results != null) {
                            if (results.get(0).getIsvalid()) {
                                editor = sharedPref.edit();
                                editor.putBoolean("isLoggedIn", true);
                                JSONObject object = new JSONObject();
                                try {
                                    object.put("phizioname", results.get(0).getPhizioname());
                                    object.put("phizioemail", results.get(0).getPhizioemail());
                                    object.put("phiziophone", results.get(0).getPhiziophone());
                                    object.put("phizioprofilepicurl", results.get(0).getPhizioprofilepicurl());
                                    object.put("address", results.get(0).getAddress());
                                    object.put("clinicname", results.get(0).getClinicname());
                                    object.put("cliniclogo",results.get(0).getCliniclogo());
                                    object.put("degree", results.get(0).getDegree());
                                    object.put("experience", results.get(0).getExperience());
                                    object.put("gender", results.get(0).getGender());
                                    object.put("phiziodob", results.get(0).getPhiziodob());
                                    object.put("specialization", results.get(0).getSpecialization());
                                    object.put("patientlimit", results.get(0).getPatientlimit());

//                                    object.put("type", results.get(0).getType());
//                                    object.put("packagetype", results.get(0).getPackagetype());
                                    if(results.get(0).getType()>0){
                                        object.put("type", results.get(0).getType());
                                    }else{
                                        object.put("type", 1);
                                    }
                                    if(results.get(0).getPackagetype()>0){
                                        object.put("packagetype", results.get(0).getPackagetype());
                                    }else{
                                        object.put("packagetype", 2);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                editor.putString("phiziodetails", object.toString());
                                editor.commit();
                                List<PhizioPatients> phiziopatients = results.get(0).getPhiziopatients();

                                if (phiziopatients.size() > 0 && sharedPref.getInt("maxid", -1) == -1) {
                                    for (int i = 0; i < phiziopatients.size(); i++) {
                                        String patientId = phiziopatients.get(i).getPatientid();
                                        try {
                                            int id = Integer.parseInt(patientId);
                                            if (id > maxid[0]) {
                                                maxid[0] = id;
                                            }
                                        } catch (NumberFormatException e) {
                                            if(phiziopatients.get(i).getPatientid().contains(" ")){
                                                String[] temp  = patientId.split(" ",2);
                                                try {
                                                    int id = Integer.parseInt(temp[0]);
                                                    if(id>maxid[0]){
                                                        maxid[0] = id;
                                                    }
                                                }catch (NumberFormatException e1){
                                                    e1.printStackTrace();
                                                }
                                            }
                                        }
                                    }
                                    editor = sharedPref.edit();
                                    editor.putInt("maxid", maxid[0]);
                                    editor.apply();
                                } else {
                                    editor = sharedPref.edit();
                                    editor.putInt("maxid", maxid[0]);
                                    editor.apply();
                                }
                                new InsertAllPatients(phizioPatientsDao).execute(phiziopatients);
                            } else {
                                if (loginlistner != null)
                                    loginlistner.onLoginResponse(false, "Invalid credentials");
                            }
                        } else {
                            if (loginlistner != null)
                                loginlistner.onLoginResponse(false, "Invalid credentials");
                        }
                    }
                });
            }

            @Override
            public void onFailure(@NonNull Call<List<LoginResult>> call, @NonNull Throwable t) {
                if (loginlistner != null)
                    loginlistner.onLoginResponse(false, "Invalid credentials");
            }
        });
    }


    public void uploadPatientImage(String patientid, String phizioemail, Bitmap bitmap) {
        PatientImageData data = new PatientImageData(patientid, phizioemail, BitmapOperations.bitmapToString(bitmap));
        Call<PatientImageUploadResponse> upload_patient_image = getDataService.uploadPatientProfilePicture(data);
        upload_patient_image.enqueue(new Callback<PatientImageUploadResponse>() {
            @Override
            public void onResponse(@NonNull Call<PatientImageUploadResponse> call, @NonNull Response<PatientImageUploadResponse> response) {
                if (response.code() == 200) {
                    PatientImageUploadResponse res = response.body();
                    if (res != null) {
                        if (res.getIsvalid()) {
                            new UpdatePatientProfilePicUrl(phizioPatientsDao).execute(res);
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<PatientImageUploadResponse> call, @NonNull Throwable t) {

            }
        });
    }

    public void updatePhizioDetails(PhizioDetailsData data) {
        Call<String> update_phizio_details = getDataService.updatePhizioDetails(data);
        update_phizio_details.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.code() == 200) {
                    String res = response.body();
                    if (res != null) {
                        if (res.equalsIgnoreCase("updated")) {
                            JSONObject json_phizio = null;
                            try {
                                json_phizio = new JSONObject(sharedPref.getString("phiziodetails", ""));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            editor = sharedPref.edit();
                            try {
                                assert json_phizio != null;
                                json_phizio.put("phizioname", data.getPhizioname());
                                json_phizio.put("phiziophone", data.getPhiziophone());
                                json_phizio.put("clinicname", data.getClinicname());
                                json_phizio.put("phiziodob", data.getPhiziodob());
                                json_phizio.put("experience", data.getExperience());
                                json_phizio.put("specialization", data.getSpecialization());
                                json_phizio.put("degree", data.getDegree());
                                json_phizio.put("gender", data.getGender());
                                json_phizio.put("address", data.getAddress());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            editor.putString("phiziodetails", json_phizio.toString());
                            editor.commit();
                            if (phizioDetailsResponseListner != null)
                                phizioDetailsResponseListner.onDetailsUpdated(true);
                        } else {
                            if (phizioDetailsResponseListner != null)
                                phizioDetailsResponseListner.onDetailsUpdated(false);
                        }
                    }
                } else {
                    if (phizioDetailsResponseListner != null)
                        phizioDetailsResponseListner.onDetailsUpdated(false);
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                if (phizioDetailsResponseListner != null) {
                    phizioDetailsResponseListner.onDetailsUpdated(false);
                }
            }
        });
    }

    public void updatePhizioProfilePic(String phizioemail, Bitmap photo) {
        PatientImageData data = new PatientImageData(null, phizioemail, BitmapOperations.bitmapToString(photo));
        Call<PatientImageUploadResponse> update_phizio_pic = getDataService.updatePhizioProfilePic(data);
        update_phizio_pic.enqueue(new Callback<PatientImageUploadResponse>() {
            @Override
            public void onResponse(@NonNull Call<PatientImageUploadResponse> call, @NonNull Response<PatientImageUploadResponse> response) {
                if (response.code() == 200) {
                    PatientImageUploadResponse response1 = response.body();
                    if (response1 != null) {
                        if (response1.getIsvalid()) {
                            try {
                                JSONObject object = new JSONObject(sharedPref.getString("phiziodetails", ""));
                                object.put("phizioprofilepicurl", response1.getUrl());
                                editor = sharedPref.edit();
                                editor.putString("phiziodetails", object.toString());
                                editor.apply();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if (phizioDetailsResponseListner != null) {
                                phizioDetailsResponseListner.onProfilePictureUpdated(true);
                            }
                        } else {
                            if (phizioDetailsResponseListner != null) {
                                phizioDetailsResponseListner.onProfilePictureUpdated(false);
                            }
                        }
                    } else {
                        if (phizioDetailsResponseListner != null) {
                            phizioDetailsResponseListner.onProfilePictureUpdated(false);
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<PatientImageUploadResponse> call, @NonNull Throwable t) {
                if (phizioDetailsResponseListner != null) {
                    phizioDetailsResponseListner.onProfilePictureUpdated(false);
                }
            }
        });
    }

    public void updatePhizioClinicLogoPic(String phizioemail, Bitmap photo) {
        PatientImageData data = new PatientImageData(null, phizioemail, BitmapOperations.bitmapToString(photo));
        Call<PatientImageUploadResponse> update_phizio_pic = getDataService.updatePhizioClinicLogoPic(data);
        update_phizio_pic.enqueue(new Callback<PatientImageUploadResponse>() {
            @Override
            public void onResponse(@NonNull Call<PatientImageUploadResponse> call, @NonNull Response<PatientImageUploadResponse> response) {
                if (response.code() == 200) {
                    PatientImageUploadResponse response1 = response.body();
                    if (response1 != null) {
                        if (response1.getIsvalid()) {
                            try {
                                JSONObject object = new JSONObject(sharedPref.getString("phiziodetails", ""));
                                object.put("cliniclogo", response1.getUrl());
                                editor = sharedPref.edit();
                                editor.putString("phiziodetails", object.toString());
                                editor.apply();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if (phizioDetailsResponseListner != null) {
                                phizioDetailsResponseListner.onClinicLogoUpdated(true);
                            }
                        } else {
                            if (phizioDetailsResponseListner != null) {
                                phizioDetailsResponseListner.onClinicLogoUpdated(false);
                            }
                        }
                    } else {
                        if (phizioDetailsResponseListner != null) {
                            phizioDetailsResponseListner.onClinicLogoUpdated(false);
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<PatientImageUploadResponse> call, @NonNull Throwable t) {
                if (phizioDetailsResponseListner != null) {
                    phizioDetailsResponseListner.onClinicLogoUpdated(false);
                }
            }
        });
    }

    public void getReportData(String email, String patientid) {
        Call<GetReportDataResponse> get_report = getDataService.getReportData(new GetReportData(email, patientid));
        get_report.enqueue(new Callback<GetReportDataResponse>() {
            @Override
            public void onResponse(@NonNull Call<GetReportDataResponse> call, @NonNull Response<GetReportDataResponse> response) {
                if (response.code() == 200) {
                    GetReportDataResponse res = response.body();
                    List<SessionResult> lel = res.getSessionResult();


                        if (reportDataResponseListner != null) {
                            reportDataResponseListner.onReportDataReceived(res, true);
                        }


                }
            }

            @Override
            public void onFailure(@NonNull Call<GetReportDataResponse> call, @NonNull Throwable t) {
                if (reportDataResponseListner != null) {
                        reportDataResponseListner.onReportDataReceived(null, false);

                }
            }
        });
    }


    public void getDayReport(String url, String fineName) {
        Call<ResponseBody> fileCall = getDataService.getReport(url);
        fileCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                File file = WriteResponseBodyToDisk.writeResponseBodyToDisk(response.body(), fineName);
                if (file != null) {
                    if (reportDataResponseListner != null) {
                        reportDataResponseListner.onDayReportReceived(file, null, true);
                        editor = sharedPref.edit();
                        // For removing the shared preference of Session reports
                        editor.remove(fineName).apply();
                        editor.apply();
                    }
                } else {
                    if (reportDataResponseListner != null) {
                        reportDataResponseListner.onDayReportReceived(null, "Not received!", false);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                if (reportDataResponseListner != null) {
                    reportDataResponseListner.onDayReportReceived(null, "Server not responding, try again later", false);
                }
            }
        });
    }

    public void getDayReportshare(String url, String fineName, Context context, ProgressDialog report_dialog) {
        Call<ResponseBody> fileCall = getDataService.getReport(url);
        fileCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                File file = WriteResponseBodyToDisk.writeResponseBodyToDisk(response.body(), fineName);
                if (file != null) {
                    if (reportDataResponseListner != null) {

                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        Uri uri = FileProvider.getUriForFile(
                                context,
                                context.getApplicationContext().getPackageName() + ".my.package.name.provider", file);
                        shareIntent.setType("application/pdf");
                        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                        context.startActivity(shareIntent);
                        report_dialog.dismiss();

                    }
                } else {
                    if (reportDataResponseListner != null) {
                        reportDataResponseListner.onDayReportReceived(null, "Not received!", false);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                if (reportDataResponseListner != null) {
                    reportDataResponseListner.onDayReportReceived(null, "Server not responding, try again later", false);
                }
            }
        });
    }

    public void insertSessionData(SessionData data) {
        Call<ResponseData> dataCall = getDataService.insertSessionData(data);
        dataCall.enqueue(new Callback<ResponseData>() {
            @Override
            public void onResponse(@NonNull Call<ResponseData> call, @NonNull Response<ResponseData> response) {
                if (response.code() == 200) {
                    ResponseData res = response.body();
                    if (res != null) {
                        if (res.getResponse().equalsIgnoreCase("inserted")) {
                            deleteParticular(res.getId());
                            if (onSessionDataResponse != null) {
                                onSessionDataResponse.onInsertSessionData(true, "Exercise saved");
                            }
                        } else {
                            if (onSessionDataResponse != null) {
                                onSessionDataResponse.onInsertSessionData(false, "Exercise not saved, Try again later");
                            }
                        }
                    } else {
                        if (onSessionDataResponse != null) {
                            onSessionDataResponse.onInsertSessionData(false, "Exercise not saved, Try again later");
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseData> call, @NonNull Throwable t) {
                if (onSessionDataResponse != null) {
                    onSessionDataResponse.onInsertSessionData(false, "");
                }
            }
        });
    }

    public void deleteSessionData(DeleteSessionData data) {
        Call<ResponseData> dataCall = getDataService.deletePatientSession(data);
        dataCall.enqueue(new Callback<ResponseData>() {
            @Override
            public void onResponse(@NonNull Call<ResponseData> call, @NonNull Response<ResponseData> response) {
                if (response.code() == 200) {
                    ResponseData res = response.body();
                    if (res != null) {
                        if (res.getResponse().equalsIgnoreCase("deleted")) {
                            deleteParticular(res.getId());
                            if (onSessionDataResponse != null)
                                onSessionDataResponse.onSessionDeleted(true, "Exercise removed");
                        } else {
                            if (onSessionDataResponse != null)
                                onSessionDataResponse.onSessionDeleted(false, "");
                        }
                    } else {
                        if (onSessionDataResponse != null)
                            onSessionDataResponse.onSessionDeleted(false, "");
                    }
                } else {
                    if (onSessionDataResponse != null)
                        onSessionDataResponse.onSessionDeleted(false, "");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseData> call, @NonNull Throwable t) {
                if (onSessionDataResponse != null)
                    onSessionDataResponse.onSessionDeleted(false, "");
            }
        });
    }

    public void updateMmtData(MmtData data) {
        Call<ResponseData> dataCall = getDataService.updateMmtData(data);
        dataCall.enqueue(new Callback<ResponseData>() {
            @Override
            public void onResponse(@NonNull Call<ResponseData> call, @NonNull Response<ResponseData> response) {
                if (response.code() == 200) {
                    ResponseData res = response.body();
                    if (res != null) {
                        if (res.getResponse().equalsIgnoreCase("updated")) {
                            deleteParticular(res.getId());
                            if (onSessionDataResponse != null) {
                                onSessionDataResponse.onMmtValuesUpdated(true, res.getResponse().substring(0,1).toUpperCase()+ res.getResponse().substring(1));
                            }
                        } else {
                            if (onSessionDataResponse != null) {
                                onSessionDataResponse.onMmtValuesUpdated(false, "");
                            }
                        }
                    } else {
                        if (onSessionDataResponse != null)
                            onSessionDataResponse.onMmtValuesUpdated(false, "");
                    }
                } else {
                    if (onSessionDataResponse != null)
                        onSessionDataResponse.onSessionDeleted(false, "");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseData> call, @NonNull Throwable t) {
                if (onSessionDataResponse != null)
                    onSessionDataResponse.onSessionDeleted(false, "");
            }
        });
    }

    public void sendFirmwareLogToTheServer(byte[] packet, String deviceMacc, String firmwareVersion
            , String serialId, boolean isNetworkAvailable, Context context) {
        AsyncTask.execute(() -> {
            Byte b = packet[1];
            int error_code = b.intValue();
            byte b2 = packet[2];
            String file_name = "";
            int line_number = 0;
            if (ByteToArrayOperations.byteToStringHexadecimal(b2).equals("E1")) {
                for (int i = 3; i < 32; i++) {
                    if (ByteToArrayOperations.byteToStringHexadecimal(packet[i]).equals("E2")) {
                        line_number = ByteToArrayOperations.getAngleFromData(packet[i + 1], packet[i + 2]);
                        break;
                    } else {
                        Byte temp_byte = new Byte(packet[i]);
                        file_name = file_name.concat(String.valueOf((char) Integer.parseInt(temp_byte.toString())));
                    }
                }
            }
            String final_string = sharedPref.getString("firmware_log", "") + "\n\n\n\n" +
                    "Phizio Email: " + json_phizioemail + "\n" + "Device Macc: " + deviceMacc + "\n" + "Firmware Version: " + firmwareVersion + "\n" +
                    "Serial Id: " + serialId + "\n" +
                    "Firmware Log: " + "\n" +
                    "\tError Code: " + error_code + "\n" +
                    "\tFile Name: " + file_name + "\n" +
                    "\tLine Number: " + line_number;

            editor = sharedPref.edit();
            editor.putString("firmware_log", final_string);
            editor.apply();
            if (isNetworkAvailable) {
                FirmwareData data = new FirmwareData(sharedPref.getString("firmware_log", ""));
                Call<Boolean> comment_data = getDataService.sendFirmwareLog(data);
                comment_data.enqueue(new Callback<Boolean>() {
                    @Override
                    public void onResponse(@NonNull Call<Boolean> call, @NonNull Response<Boolean> response) {
                        if (response.code() == 200) {
                            Boolean res = response.body();
                            if (res != null && res) {
                                editor = sharedPref.edit();
                                editor.putString("firmware_log", "");
                                editor.apply();
                                sendFirmwareLogBroadcast(true, context);
                            } else {
                                sendFirmwareLogBroadcast(false, context);
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Boolean> call, @NonNull Throwable t) {
                        sendFirmwareLogBroadcast(false, context);
                    }
                });
            } else {
                sendFirmwareLogBroadcast(false, context);
            }
        });
    }

    public void firmwareUpdateCheckAndGetLink(String firmware_version, Context context) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                FirmwareUpdateCheck check = new FirmwareUpdateCheck(firmware_version);
                Call<FirmwareUpdateCheckResponse> data = getDataService.checkFirmwareUpdateAndGetLink(check);
                data.enqueue(new Callback<FirmwareUpdateCheckResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<FirmwareUpdateCheckResponse> call, @NonNull Response<FirmwareUpdateCheckResponse> response) {
                        if (response.code() == 200) {
                            FirmwareUpdateCheckResponse check1 = response.body();
                            if (check1 != null) {
                                if (check1.isFirmware_available()) {
                                    editor = sharedPref.edit();
                                    editor.putString("firmware_update", check1.getLatest_firmware_link());
                                    editor.putString("firmware_version", check1.getFirmware_version());
                                    editor.apply();
                                    sendFirmwareUpdateAvailable(true, context);
                                } else {
                                    editor = sharedPref.edit();
                                    editor.putString("firmware_update", "");
                                    editor.putString("firmware_version", "");
                                    editor.apply();
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<FirmwareUpdateCheckResponse> call, @NonNull Throwable t) {

                    }
                });
            }
        });
    }


    private void sendFirmwareLogBroadcast(boolean response, Context context) {
        Intent i = new Intent(firmware_log);
        i.putExtra(firmware_log, response);
        context.sendBroadcast(i);
    }

    private void sendFirmwareUpdateAvailable(boolean response, Context context) {
        Intent i = new Intent(firmware_update_available);
        i.putExtra(firmware_update_available, response);
        context.sendBroadcast(i);
    }

    public void updateCommentData(CommentSessionUpdateData data) {
        Call<String> comment_data = getDataService.updateCommentData(data);
        comment_data.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.code() == 200) {
                    if (response.body() != null) {
                        if (response.body().equalsIgnoreCase("updated")) {
                            if (onSessionDataResponse != null) {
                                onSessionDataResponse.onCommentSessionUpdated(true);
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {

            }
        });
    }


    public void checkAndUpdateDeviceStatus(byte[] info_packet, Context context, int deviceCurrentState){
        if(deviceCurrentState==0) {
            if (NetworkOperations.isNetworkAvailable(context)) {
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        String uid = ByteToArrayOperations.getDeviceUid(info_packet);

                        DeviceDeactivationStatus status = new DeviceDeactivationStatus(uid);
                        Gson gson = new GsonBuilder().create();
                        String s = gson.toJson(status);
                        editor = sharedPref.edit();
                        editor.putString("uid_deactivation",s);
                        editor.commit();
                        Call<DeviceDeactivationStatusResponse> call = getDataService.getDeviceStatus(status);
                        call.enqueue(new Callback<DeviceDeactivationStatusResponse>() {
                            @Override
                            public void onResponse(@NonNull Call<DeviceDeactivationStatusResponse> call, @NonNull Response<DeviceDeactivationStatusResponse> response) {
                                if (response.code() == 200) {
                                    DeviceDeactivationStatusResponse res_device_status = response.body();
                                    if (!res_device_status.isStatus()) {
                                        sendDeactivateDeviceBroadcast(context);
                                    }else {
                                        editor = sharedPref.edit();
                                        editor.putString("uid_deactivation","");
                                        editor.apply();
                                        if(res_device_status.getUid()!=null)
                                            deleteDeviceStatus(res_device_status.getUid());
                                    }
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<DeviceDeactivationStatusResponse> call, @NonNull Throwable t) {
                            }
                        });
                    }
                });
            } else {
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        String uid = ByteToArrayOperations.getDeviceUid(info_packet);
                        DeviceDeactivationStatus status = new DeviceDeactivationStatus(uid);
                        Gson gson = new GsonBuilder().create();
                        String s = gson.toJson(status);
                        editor = sharedPref.edit();
                        editor.putString("uid_deactivation",s);
                        editor.commit();
                        DeviceStatus device_present = deviceStatusDao.getDeviceStatus(uid);
                        if (device_present != null) {
                            sendDeactivateDeviceBroadcast(context);
                        } else {
                            sendDeviceDeactivatedServiceScedule(context);
                        }
                    }
                });
            }
        }
    }


    public void getDeviceStatus(byte[] info_packet){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                String uid = ByteToArrayOperations.getDeviceUid(info_packet);
                DeviceDeactivationStatus status = new DeviceDeactivationStatus(uid);
                Call<DeviceDeactivationStatusResponse> call = getDataService.getDeviceStatus(status);
                call.enqueue(new Callback<DeviceDeactivationStatusResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<DeviceDeactivationStatusResponse> call, @NonNull Response<DeviceDeactivationStatusResponse> response) {
                        if (response.code() == 200) {
                            DeviceDeactivationStatusResponse res_device_status = response.body();
                            if (!res_device_status.isStatus()) {
                                if(onDeviceStatusResponse!=null){
                                    onDeviceStatusResponse.onDeviceStatusResponse(true,false);
                                }
                            }else {
                                if(onDeviceStatusResponse!=null){
                                    onDeviceStatusResponse.onDeviceStatusResponse(true,true);
                                }
                            }
                        }else {
                            if(onDeviceStatusResponse!=null){
                                onDeviceStatusResponse.onDeviceStatusResponse(false,false);
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<DeviceDeactivationStatusResponse> call, @NonNull Throwable t) {
                        if(onDeviceStatusResponse!=null){
                            onDeviceStatusResponse.onDeviceStatusResponse(false,false);
                        }
                    }
                });
            }
        });

    }


    public void insertDeviceStatus(DeviceStatus status){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                DeviceStatus device_present = deviceStatusDao.getDeviceStatus(status.getUid());
                if(device_present==null)
                    deviceStatusDao.insert(status);
            }
        });
    }

    public void deleteDeviceStatus(byte[] info_packet){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                String uid = ByteToArrayOperations.getDeviceUid(info_packet);
                deviceStatusDao.deleteDevice(uid);
            }
        });
    }

    public void deleteDeviceStatus(String uid){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                deviceStatusDao.deleteDevice(uid);
            }
        });
    }

    private void sendDeviceDeactivatedServiceScedule(Context context){
        Intent i = new Intent(scedule_device_status_service);
        i.putExtra(scedule_device_status_service,"");
        context.sendBroadcast(i);
    }

    private void sendDeactivateDeviceBroadcast(Context context){
        Intent i = new Intent(deactivate_device);
        i.putExtra(deactivate_device,"");
        context.sendBroadcast(i);
    }
    public void syncDataToServer() {
        new SyncDataAsync(mqttSyncDao).execute();
    }

    @SuppressLint("StaticFieldLeak")
    private class SyncDataAsync extends AsyncTask<Void, Void, List<MqttSync>> {

        MqttSyncDao mqttSyncDao;

        SyncDataAsync(MqttSyncDao mqttSyncDao) {
            this.mqttSyncDao = mqttSyncDao;
        }

        @Override
        protected List<MqttSync> doInBackground(Void... voids) {
            return mqttSyncDao.getAllMqttSyncItems();
        }

        @Override
        protected void onPostExecute(List<MqttSync> mqttSyncs) {
            super.onPostExecute(mqttSyncs);
            if(mqttSyncs.size()>0) {
                startSync(mqttSyncs);
                if(listner!=null){
                    listner.onSyncComplete(true,"Already Upto date!");
                }
            }
        }
    }

    private void startSync(List<MqttSync> mqttSyncs) {
        Call<List<Integer>> sync_data = getDataService.syncDataToServer(mqttSyncs);
        sync_data.enqueue(new Callback<List<Integer>>() {
            @Override
            public void onResponse(@NonNull Call<List<Integer>> call, @NonNull Response<List<Integer>> response) {
                if (response.code() == 200) {
                    List<Integer> list = response.body();
                    new DeleteMultipleSyncItem(mqttSyncDao).execute(list);
                } else {
                    if (listner != null) {
                        listner.onSyncComplete(false, "Server busy, try again later!");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Integer>> call, @NonNull Throwable t) {
                if (listner != null)
                    listner.onSyncComplete(false, "Server busy, try again later!");
            }
        });

    }


    //Pheezee Status related api's
    public void sendDeviceHealthStatusToTheServer(byte[] info_packet, Context context) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                HealthData data = ByteToArrayOperations.getHealthData(info_packet);
                editor = sharedPref.edit();
                Gson gson = new GsonBuilder().create();
                String s = gson.toJson(data);
                editor.putString("health_data", s);
                editor.apply();
                if (NetworkOperations.isNetworkAvailable(context)) {
                    Call<Boolean> call = getDataService.sendHealthStatusOfDevice(data);
                    call.enqueue(new Callback<Boolean>() {
                        @Override
                        public void onResponse(@NonNull Call<Boolean> call, @NonNull Response<Boolean> response) {
                            if (response.code() == 200) {
                                boolean b = response.body();
                                if (b) {
                                    editor = sharedPref.edit();
                                    editor.putString("health_data", "");
                                    editor.apply();
                                } else {
                                    sendHealthStatusBroadcast(false, context);
                                }
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<Boolean> call, @NonNull Throwable t) {
                            sendHealthStatusBroadcast(false, context);
                        }
                    });
                } else {
                    sendHealthStatusBroadcast(false, context);
                }
            }
        });
    }

    public void sendDeviceLocationStatusToTheServer(byte[] info_packet, Context context, double latitude, double longitude) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                if(latitude!=0 && longitude!=0){
                    String uid = ByteToArrayOperations.getDeviceUid(info_packet);
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String dateString = formatter.format(new Date());
                    DeviceLocationStatus status = new DeviceLocationStatus(uid,dateString,latitude,longitude);
                    editor = sharedPref.edit();
                    Gson gson = new GsonBuilder().create();
                    String s = gson.toJson(status);
                    editor.putString("device_location_data", s);
                    editor.apply();
                    if (NetworkOperations.isNetworkAvailable(context)) {
                        Call<Boolean> call = getDataService.sendDeviceLocationUpdate(status);
                        call.enqueue(new Callback<Boolean>() {
                            @Override
                            public void onResponse(@NonNull Call<Boolean> call, @NonNull Response<Boolean> response) {
                                if (response.code() == 200) {
                                    boolean b = response.body();
                                    if (b) {
                                        editor = sharedPref.edit();
                                        editor.putString("device_location_data", "");
                                        editor.apply();
                                    } else {
                                        sendDeviceLocationStatusBroadcast(false, context);
                                    }
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<Boolean> call, @NonNull Throwable t) {
                                sendDeviceLocationStatusBroadcast(false, context);
                            }
                        });
                    } else {
                        sendDeviceLocationStatusBroadcast(false, context);
                    }
                }
            }
        });
    }

    public void sendDeviceDetailsToTheServer(byte[] info_packet, Context context, String macc, String firmware_version, String hardware_version,
                                             String serial_version, String atiny_version){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                String uid = ByteToArrayOperations.getDeviceUid(info_packet);
                DeviceDetailsData data = new DeviceDetailsData(uid,macc,firmware_version,hardware_version,serial_version,atiny_version);
                editor = sharedPref.edit();
                Gson gson = new GsonBuilder().create();
                String s = gson.toJson(data);
                editor.putString("device_details_data", s);
                editor.apply();
                if(NetworkOperations.isNetworkAvailable(context)){
                    Call<Boolean> call = getDataService.sendDeviceDetailsToTheServer(data);
                    call.enqueue(new Callback<Boolean>() {
                        @Override
                        public void onResponse(@NonNull Call<Boolean> call, @NonNull Response<Boolean> response) {
                            if (response.code() == 200) {
                                boolean b = response.body();
                                if (b) {
                                    editor = sharedPref.edit();
                                    editor.putString("device_details_data", "");
                                    editor.apply();
                                } else {
                                    sendDeviceDetailsStatusBroadcast(false, context);
                                }
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<Boolean> call, @NonNull Throwable t) {
                            sendDeviceDetailsStatusBroadcast(false, context);
                        }
                    });
                }else {
                    sendDeviceDetailsStatusBroadcast(false, context);
                }
            }
        });
    }

    public void sendPhizioEmailToTheServer(byte[] info_packet, Context context){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                String uid = ByteToArrayOperations.getDeviceUid(info_packet);
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String dateString = formatter.format(new Date());
                PhizioEmailData data = new PhizioEmailData(uid, json_phizioemail, dateString);
                editor = sharedPref.edit();
                Gson gson = new GsonBuilder().create();
                String s = gson.toJson(data);
                editor.putString("device_email_data", s);
                editor.apply();
                if(NetworkOperations.isNetworkAvailable(context)){
                    Call<Boolean> call = getDataService.sendEmailUsedWithDevice(data);
                    call.enqueue(new Callback<Boolean>() {
                        @Override
                        public void onResponse(@NonNull Call<Boolean> call, @NonNull Response<Boolean> response) {
                            if (response.code() == 200) {
                                boolean b = response.body();
                                if (b) {
                                    editor = sharedPref.edit();
                                    editor.putString("device_email_data", "");
                                    editor.apply();
                                } else {
                                    sendDeviceEmailDetailsStatusBroadcast(false, context);
                                }
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<Boolean> call, @NonNull Throwable t) {
                            sendDeviceEmailDetailsStatusBroadcast(false, context);
                        }
                    });
                }else {
                    sendDeviceEmailDetailsStatusBroadcast(false, context);
                }
            }
        });
    }

    public void sendFirebaseTopkenToTheServer(String phizioemail, String mobile_token){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                MobileToken token = new MobileToken(phizioemail,mobile_token);
                Call<Boolean> call = getDataService.sendMobileTokenToTheServer(token);
                call.enqueue(new Callback<Boolean>() {
                    @Override
                    public void onResponse(@NonNull Call<Boolean> call, @NonNull Response<Boolean> response) {
                        if (response.code() == 200) {
                            boolean b = response.body();
                            if (b) {
                                Log.d("Token","Mobile token has been updated!");
                            } else {
                                Log.d("Token","Mobile token has not been updated!");
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Boolean> call, @NonNull Throwable t) {
                        Log.d("Token","Mobile token has not been updated!");
                    }
                });
            }
        });
    }

    public void sceduledSessionNotSaved( String patientid){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                JSONObject json_phizio = null;
                try {
                    json_phizio = new JSONObject(sharedPref.getString("phiziodetails", ""));
                    String json_phizioemail = json_phizio.getString("phizioemail");
                    SceduledSessionNotSaved sceduledSessionNotSaved = new SceduledSessionNotSaved(json_phizioemail,patientid);
                    Call<Boolean> call = getDataService.sendSceduledSessionNotSaved(sceduledSessionNotSaved);
                    call.enqueue(new Callback<Boolean>() {
                        @Override
                        public void onResponse(@NonNull Call<Boolean> call, @NonNull Response<Boolean> response) {
                            if (response.code() == 200) {
                                boolean b = response.body();
                                if (b) {
                                    Log.d("Session","Not Saved");
                                } else {
                                    Log.d("Session","Error");
                                }
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<Boolean> call, @NonNull Throwable t) {
                            Log.d("Session","Failure");
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private void sendHealthStatusBroadcast(boolean response, Context context){
        Intent i = new Intent(health_status);
        i.putExtra(health_status,response);
        context.sendBroadcast(i);
    }

    private void sendDeviceLocationStatusBroadcast(boolean response, Context context){
        Intent i = new Intent(location_status);
        i.putExtra(location_status,response);
        context.sendBroadcast(i);
    }

    private void sendDeviceDetailsStatusBroadcast(boolean response, Context context){
        Intent i = new Intent(device_details_status);
        i.putExtra(device_details_status,response);
        context.sendBroadcast(i);
    }

    private void sendDeviceEmailDetailsStatusBroadcast(boolean response, Context context){
        Intent i = new Intent(device_details_email);
        i.putExtra(device_details_email,response);
        context.sendBroadcast(i);
    }

    public void updateDeviceInformationOnTheServer(){

    }


    //Callbacks
    public interface OnSessionDataResponse{
        void onInsertSessionData(Boolean response, String message);
        void onSessionDeleted(Boolean response, String message);
        void onMmtValuesUpdated(Boolean response, String message);
        void onCommentSessionUpdated(Boolean response);
    }


    public interface GetSessionNumberResponse{
        void onSessionNumberResponse(String sessionnumber);
    }

    public interface OnReportDataResponseListner{
        void onReportDataReceived(GetReportDataResponse array, boolean response);
        void onDayReportReceived(File file, String message, Boolean response);
    }

    public interface OnPhizioDetailsResponseListner{
        void onDetailsUpdated(Boolean response);
        void onProfilePictureUpdated(Boolean response);
        void onClinicLogoUpdated(Boolean response);
    }

    public interface OnSignUpResponse{
        void onConfirmEmail(boolean response, String message);
        void onSignUp(boolean response);
    }

    public interface OnDeletePhiziouser{
        void onConfirmEmail(boolean response, String message);
        void onSignUp(boolean response);
    }
    public interface OnWarrrantyDetails{
        void onConfirmEmail(boolean response, String message);
        void onSignUp(boolean response);
    }

    public interface OnLoginResponse{
        void onLoginResponse(boolean response, String message);
        void onForgotPasswordResponse(boolean response, String message);
        void onPasswordUpdated( String message);
    }

    public interface onServerResponse{
        void onDeletePateintResponse(boolean response);
        void onUpdatePatientDetailsResponse(boolean response);
        void onUpdatePatientStatusResponse(boolean response);
        void onSyncComplete(boolean response, String message);
    }

    public interface onSceduledSesssionResponse{
        void onResponse(List<SceduledSession> sessions);
    }

    public interface onDeviceStatusResponse{
        void onDeviceStatusResponse(boolean response, boolean status);
    }

//    public interface FirmwareUpdatedListner{
//        void firmwareUpdated(boolean flag);
//    }

    public void setOnPhizioDetailsResponseListner(OnPhizioDetailsResponseListner phizioDetailsResponseListner){
        this.phizioDetailsResponseListner = phizioDetailsResponseListner;
    }

    public void unregisterPhizioDetailsResponseListner(){
        this.phizioDetailsResponseListner = null;
    }

    public void setOnLoginResponse(OnLoginResponse loginlistner){
        this.loginlistner = loginlistner;
    }

    public void setOnServerResponseListner(onServerResponse listner){
        this.listner = listner;
    }

    public void setOnSignUpResponse(OnSignUpResponse signUpResponse){
        this.signUpResponse = signUpResponse;
    }
    public void setOnDeletePhiziouser(OnDeletePhiziouser signUpResponse){
        this.deletePhiziouser = deletePhiziouser;
    }

    public void setOnReportDataResponseListener(OnReportDataResponseListner reportDataResponseListener){
        this.reportDataResponseListner = reportDataResponseListener;
    }

    public void setOnSessionNumberResponse(GetSessionNumberResponse response){
        this.response = response;
    }

    public void setOnSessionDataResponse(OnSessionDataResponse onSessionDataResponse){
        this.onSessionDataResponse = onSessionDataResponse;
    }


    public void disableReportDataListner(){
        this.reportDataResponseListner = null;
    }

    public void setOnDeviceStatusResponse(onDeviceStatusResponse response){
        this.onDeviceStatusResponse = response;
    }

    public void setOnSceduledSessionResponse(onSceduledSesssionResponse response){
        this.onSceduledSessionResponse = response;
    }

}
