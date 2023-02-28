package com.start.apps.pheezee.retrofit;

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
import com.start.apps.pheezee.pojos.Overallresponse;
import com.start.apps.pheezee.pojos.PatientDetailsData;
import com.start.apps.pheezee.pojos.PatientImageData;
import com.start.apps.pheezee.pojos.PatientImageUploadResponse;
import com.start.apps.pheezee.pojos.PatientStatusData;
import com.start.apps.pheezee.pojos.PhizioDetailsData;
import com.start.apps.pheezee.pojos.PhizioEmailData;
import com.start.apps.pheezee.pojos.PhizioSessionReportData;
import com.start.apps.pheezee.pojos.ResponseData;
import com.start.apps.pheezee.pojos.SceduledSessionNotSaved;
import com.start.apps.pheezee.pojos.SerialData;
import com.start.apps.pheezee.pojos.SessionData;
import com.start.apps.pheezee.pojos.SessionDetailsResult;
import com.start.apps.pheezee.pojos.SignUpData;
import com.start.apps.pheezee.pojos.WarrantyData;
import com.start.apps.pheezee.room.Entity.MqttSync;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Url;

/**
 *
 */
public interface GetDataService {
    @GET
    Call<ResponseBody> getReport(@Url String url);


    @POST("/api/login/phizio")
    Call<List<LoginResult>> login(@Body LoginData object);

    @POST("/api/phizio/update/patientStatus")
    Call<String> updatePatientStatus(@Body PatientStatusData object);

    @POST("/api/phizio/updatepatientdetails")
    Call<String> updatePatientDetails(@Body PatientDetailsData patient);

    @POST("/api/phizio/addpatient")
    Call<ResponseData> addPatient(@Body AddPatientData patientData);

    @POST("/api/phizio/deletepatient")
    Call<String> deletePatient(@Body DeletePatientData data);


    @POST("/api/forgot/password")
    Call<String> forgotPassword(@Body ForgotPassword object);

    @POST("/api/phizioprofile/update/password")
    Call<String> updatePassword(@Body LoginData data);

    @POST("/api/phizioprofile/update/app_version")
    Call<String> updateApp_version(@Body LoginData data);

    @POST("/api/getheldon")
    Call<String> getHeldon(@Body PatientStatusData object);

    @POST("/api/getsession_report_count")
    Call<PhizioSessionReportData> getsession_report_count(@Body PatientStatusData object);

    @POST("/api/getsession_number_count")
    Call<PhizioSessionReportData> getsession_number_count(@Body PatientStatusData object);

    @POST("/api/getoveralletails")
    Call<Overallresponse> getOverall_list(@Body PatientStatusData object);

    @POST("/api/getsessiondetails")
    Call <List<SessionDetailsResult>> getSessiondetails(@Body PatientStatusData object);

    @POST("/api/confirm/email")
    Call<String> confirmEmail(@Body ConfirmEmailAndPackageId object);

    @POST("/api/signup/phizio")
    Call<String> signUp(@Body SignUpData data);

    @POST("/api/phizio/update/patientProfilePic")
    Call<PatientImageUploadResponse> uploadPatientProfilePicture(@Body PatientImageData data);

    @POST("/api/phizioprofile/update")
    Call<String> updatePhizioDetails(@Body PhizioDetailsData data);

    @POST("/api/phizio/profilepic/upload")
    Call<PatientImageUploadResponse> updatePhizioProfilePic(@Body PatientImageData data);

    @POST("/api/phizio/cliniclogo/upload")
    Call<PatientImageUploadResponse> updatePhizioClinicLogoPic(@Body PatientImageData data);

    @POST("/api/patient/generate/report_v2")
    Call<GetReportDataResponse> getReportData(@Body GetReportData data);


    @POST("/api/patient/entireEmgData")
    Call<ResponseData> insertSessionData(@Body SessionData data);

    @POST("/api/phizio/patient/deletepatient/sesssion")
    Call<ResponseData> deletePatientSession(@Body DeleteSessionData data);

    @POST("/api/phizio/patient/updateMmtGrade")
    Call<ResponseData> updateMmtData(@Body MmtData data);

    @POST("/api/phizio/patient/updateCommentSection")
    Call<String> updateCommentData(@Body CommentSessionUpdateData data);

    @POST("/api/sync/data")
    Call<List<Integer>> syncDataToServer(@Body List<MqttSync> sync);

    @POST("/api/firmware/log")
    Call<Boolean> sendFirmwareLog(@Body FirmwareData log);

    @POST("/api/firmware/update/check")
    Call<FirmwareUpdateCheckResponse> checkFirmwareUpdateAndGetLink(@Body FirmwareUpdateCheck check);

    @POST("/api/insert/pheezee/health/status")
    Call<Boolean> sendHealthStatusOfDevice(@Body HealthData data);

    @POST("/api/update/device/location")
    Call<Boolean> sendDeviceLocationUpdate(@Body DeviceLocationStatus data);

    @POST("/api/insert/pheezee/device")
    Call<Boolean> sendDeviceDetailsToTheServer(@Body DeviceDetailsData data);

    @POST("/api/update/device/email/used")
    Call<Boolean> sendEmailUsedWithDevice(@Body PhizioEmailData data);

    @POST("/api/get/device/status")
    Call<DeviceDeactivationStatusResponse> getDeviceStatus(@Body DeviceDeactivationStatus status);

    @POST("/api/phizio/device/mobileToken")
    Call<Boolean> sendMobileTokenToTheServer(@Body MobileToken token);

    @POST("/api/sceduled/session/not/saved")
    Call<Boolean> sendSceduledSessionNotSaved(@Body SceduledSessionNotSaved sceduledSessionNotSaved);

    @POST("/api/delete-phiziouser")
    Call<String> deletePhiziouser(@Body DeletePhiziouserData data);

    @POST("/api/phizio/calbirations")
    Call<String> cal(@Body CalbrationData data);

    @POST("/api/get-warranty-details")
    Call<String> warrantyDetails(@Body WarrantyData data);

    @POST("/api/get-serial-number")
    Call<String> serialnumber(@Body SerialData data);

}
