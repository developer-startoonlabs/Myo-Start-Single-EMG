package com.start.apps.pheezee.activities;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import start.apps.pheezee.R;
import com.start.apps.pheezee.pojos.PhizioDetailsData;
import com.start.apps.pheezee.popup.UploadImageDialog;
import com.start.apps.pheezee.repository.MqttSyncRepository;
import com.start.apps.pheezee.services.PicassoCircleTransformation;
import com.start.apps.pheezee.utils.BitmapOperations;
import com.start.apps.pheezee.utils.NetworkOperations;
import com.start.apps.pheezee.utils.RegexOperations;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static com.start.apps.pheezee.activities.PatientsView.ivBasicImage;

public class EditProfileActivity extends AppCompatActivity implements MqttSyncRepository.OnPhizioDetailsResponseListner {

    EditText et_phizio_name,et_phizio_lastname, et_phizio_phone,et_address, et_clinic_name, et_specialization, et_degree,et_experience;
    RadioGroup rg_gender;
    String gender;
    MqttSyncRepository repository;
    TextView et_dob,uploadLogoPhizio,uploadLogoClinic;
    ImageView iv_phizio_profilepic, iv_phizio_clinic_logo, iv_back_button,iv_update_clinic_logo,iv_update_profilepic;
    LinearLayout tv_edit_profile_pic;

    String str_name;
    boolean screen_one=true;


    final Calendar myCalendar = Calendar.getInstance();

    Button btn_update, btn_cancel_update;

    JSONObject json_phizio;
    SharedPreferences sharedPref;
    ProgressDialog dialog;
    RadioButton gender_male,gender_female;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        repository = new MqttSyncRepository(getApplication());
        repository.setOnPhizioDetailsResponseListner(this);
        //Shared Preference
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        dialog = new ProgressDialog(this);
        dialog.setMessage("Updating details, please wait");
        try {
            json_phizio = new JSONObject(sharedPref.getString("phiziodetails", ""));
        } catch (JSONException e) {
            e.printStackTrace();
        }



        setContentView(R.layout.profile_edit_layout);

        // End

        Button buttonNext = findViewById(R.id.buttonNext);
        uploadLogoPhizio = findViewById(R.id.uploadLogoPhizio);
        uploadLogoClinic = findViewById(R.id.uploadLogoClinic);

        setPhizioDetails();

        buttonNext.setOnClickListener( new View.OnClickListener() {
            public void onClick(View v) {
                if(isValidPhone(et_phizio_phone.getText().toString())){
                    screen_two();
                }else {

                    Toast.makeText(getApplicationContext(),"Phone number is not valid",Toast.LENGTH_SHORT).show();

                }




            }
        });


    }

    private void screen_two()
    {
        ConstraintLayout constraintLayoutEditDoctorPageOne = findViewById(R.id.constraintLayoutEditDoctorPageOne);
        ConstraintLayout constraintLayoutEditDoctorPageTwo = findViewById(R.id.constraintLayoutEditDoctorPageTwo);


            constraintLayoutEditDoctorPageOne.setVisibility(View.GONE);
            constraintLayoutEditDoctorPageTwo.setVisibility(View.VISIBLE);

            // Changing the hospital and phizio images
            iv_update_clinic_logo.setVisibility(View.VISIBLE);
            iv_phizio_clinic_logo.setVisibility(View.VISIBLE);

            iv_update_profilepic.setVisibility(View.GONE);
            iv_phizio_profilepic.setVisibility(View.GONE);
        try {
            if(json_phizio.has("cliniclogo") && !json_phizio.getString("cliniclogo").equalsIgnoreCase("/icons/clinic.png"))
            {
                uploadLogoClinic.setVisibility(View.GONE);
                uploadLogoPhizio.setVisibility(View.GONE);
            }else
            {
                uploadLogoClinic.setVisibility(View.VISIBLE);
                uploadLogoPhizio.setVisibility(View.GONE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        screen_one=false;


    }

    private void screen_one()
    {
        ConstraintLayout constraintLayoutEditDoctorPageOne = findViewById(R.id.constraintLayoutEditDoctorPageOne);
        ConstraintLayout constraintLayoutEditDoctorPageTwo = findViewById(R.id.constraintLayoutEditDoctorPageTwo);


        constraintLayoutEditDoctorPageOne.setVisibility(View.VISIBLE);
        constraintLayoutEditDoctorPageTwo.setVisibility(View.GONE);

        // Changing the hospital and phizio images
        iv_update_clinic_logo.setVisibility(View.GONE);
        iv_phizio_clinic_logo.setVisibility(View.GONE);

        iv_update_profilepic.setVisibility(View.VISIBLE);
        iv_phizio_profilepic.setVisibility(View.VISIBLE);
        screen_one=true;

        try {
            if(json_phizio.getString("phizioprofilepicurl").equals("empty")){
                uploadLogoPhizio.setVisibility(View.VISIBLE);
                uploadLogoClinic.setVisibility(View.GONE);
            } else {
                uploadLogoPhizio.setVisibility(View.GONE);
                uploadLogoClinic.setVisibility(View.GONE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }




    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }



    /**
     * Initializes views and sets the current values to all the view
     */
    private void setPhizioDetails() {
        et_phizio_name =  findViewById(R.id.editName);
        et_phizio_lastname =  findViewById(R.id.editLastName);
        et_phizio_phone =  findViewById(R.id.editMobile);
        et_degree = findViewById(R.id.editDegree);
        et_experience = findViewById(R.id.et_experience);




        et_address = findViewById(R.id.editCity);
        et_clinic_name = findViewById(R.id.phizio_clinic_name);
        et_specialization = findViewById(R.id.editDesignation);


        et_dob = findViewById(R.id.textViewDOB);


        rg_gender = findViewById(R.id.radioGenderGroup);
        gender_male = findViewById(R.id.male);
        gender_female = findViewById(R.id.female);
        gender_female = findViewById(R.id.female);



        iv_update_clinic_logo = findViewById(R.id.cameraButtonHospital);
        iv_update_profilepic = findViewById(R.id.cameraButtonDoctor);


        iv_phizio_clinic_logo = findViewById(R.id.hospitalImage);
        iv_phizio_profilepic = (ImageView)findViewById(R.id.doctorImage);
        iv_back_button = findViewById(R.id.backButton);
        Button buttonSave = findViewById(R.id.buttonSave);


        try {
            if(json_phizio.getString("phizioprofilepicurl").equals("empty")){
                uploadLogoPhizio.setVisibility(View.VISIBLE);
                uploadLogoClinic.setVisibility(View.GONE);
            } else uploadLogoPhizio.setVisibility(View.GONE);
        } catch (JSONException e) {
            e.printStackTrace();
        }




        iv_back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(screen_one!=true) {
                    screen_one();
                }else finish();

            }
        });

        rg_gender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton btn = group.findViewById(checkedId);
                if(btn!=null) {
                    gender = btn.getText().toString();

                }
            }
        });

        iv_update_clinic_logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(NetworkOperations.isNetworkAvailable(EditProfileActivity.this)) {
                    UploadImageDialog dialog1 = new UploadImageDialog(EditProfileActivity.this, 7, 8);
                    dialog1.showDialog();
                }
                else {
                    NetworkOperations.networkError(EditProfileActivity.this);
                }
            }
        });

        try {
            if(!json_phizio.getString("phizioprofilepicurl").equals("empty")) {
                String temp = null;

                try {
                    temp = json_phizio.getString("phizioprofilepicurl");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                temp = temp.replaceFirst("@", "%40");
                temp = "https://s3.ap-south-1.amazonaws.com/pheezee/" + temp;
                Picasso.get().load(temp)
                        .placeholder(R.drawable.user_icon)
                        .error(R.drawable.user_icon)
                        .networkPolicy(NetworkPolicy.NO_CACHE,NetworkPolicy.NO_STORE)
                        .memoryPolicy(MemoryPolicy.NO_CACHE,MemoryPolicy.NO_STORE)
                        .transform(new PicassoCircleTransformation())
                        .into(iv_phizio_profilepic);
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }






        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String profile_update_state="";
                if(NetworkOperations.isNetworkAvailable(EditProfileActivity.this)) {

                    if(et_phizio_lastname.getText().toString().length()>1)
                    {
                        str_name = et_phizio_name.getText().toString()+" "+et_phizio_lastname.getText().toString();
                    }else{
                        str_name = et_phizio_name.getText().toString();
                    }

                    String str_phone = et_phizio_phone.getText().toString();

                    String str_clinicname = et_clinic_name.getText().toString();
                    String str_dob = et_dob.getText().toString();
                    String str_experience = et_experience.getText().toString();
                    String specialization = et_specialization.getText().toString();
                    String degree = et_degree.getText().toString();
                    String address = et_address.getText().toString();

                    try {
                        if(!str_clinicname.equalsIgnoreCase("") && !str_dob.equalsIgnoreCase("") && !str_experience.equalsIgnoreCase("") && !specialization.equalsIgnoreCase("") && !degree.equalsIgnoreCase("") && !address.equalsIgnoreCase("") && !json_phizio.getString("cliniclogo").equalsIgnoreCase("/icons/clinic.png"))
                        {

                            profile_update_state="completed";
                            Log.d("clinichecking","fake");
                            Log.d("clinichecking",address);
                        }else profile_update_state="";



                    } catch (JSONException e) {
                        e.printStackTrace();
                        profile_update_state="";
                    }


                    if(RegexOperations.isValidUpdatePhizioDetails(str_name,str_phone)){
                        PhizioDetailsData data = new PhizioDetailsData(str_name,str_phone,getIntent().getStringExtra("et_phizio_email"),str_clinicname,str_dob,str_experience,specialization,degree,gender,address);
                        repository.updatePhizioDetails(data);
                        dialog.setMessage("Updating details, please wait");
                        dialog.show();
                    }
                    else {
                        showToast(RegexOperations.getNonValidStringForPhizioDetails(str_name,str_phone));
                    }
                }else {
                    NetworkOperations.networkError(EditProfileActivity.this);
                }
                Intent returnIntent = new Intent();
                returnIntent.putExtra("et_phizio_name",str_name);
                returnIntent.putExtra("et_specialization",et_specialization.getText().toString());
                returnIntent.putExtra("et_degree",et_degree.getText().toString());

                returnIntent.putExtra("et_experience",et_experience.getText().toString());
                returnIntent.putExtra("et_phizio_phone",et_phizio_phone.getText().toString());
                returnIntent.putExtra("et_clinic_name",et_clinic_name.getText().toString());
                returnIntent.putExtra("et_address",et_address.getText().toString());
                returnIntent.putExtra("profile_update_completed",profile_update_state);

                setResult(Activity.RESULT_OK,returnIntent);
                finish();
            }
        });







        iv_update_profilepic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if(NetworkOperations.isNetworkAvailable(EditProfileActivity.this)) {
                    UploadImageDialog dialog1 = new UploadImageDialog(EditProfileActivity.this, 5, 6);
                    dialog1.showDialog();
                }
                else {
                    NetworkOperations.networkError(EditProfileActivity.this);
                }
            }
        });



        et_dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(EditProfileActivity.this, dateChangedListener, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        setInitialValues();

    }

    private void setInitialValues() {
        try {

            String phzio_name_var = json_phizio.getString("phizioname");
            String[] phzio_name_array = phzio_name_var.split(" ");
            if(phzio_name_array.length > 0 && phzio_name_array[0]!=" " && phzio_name_array[0]!="")
            {
                if(phzio_name_array[0].length()>1) {
                    et_phizio_name.setText(phzio_name_array[0].substring(0, 1).toUpperCase() + phzio_name_array[0].substring(1).toLowerCase());
                    String phizio_lastname = "";
                    for (int i = 1; i < phzio_name_array.length; i++) {
                        if (i > 1) {
                            phizio_lastname = phizio_lastname + " ";
                        }
                        phizio_lastname = phizio_lastname + phzio_name_array[i];

                    }
                    if(phizio_lastname.length()>1) {
                        et_phizio_lastname.setText(phizio_lastname.substring(0, 1).toUpperCase() + phizio_lastname.substring(1).toLowerCase());
                    }else et_phizio_lastname.setText("");
                }
            }else et_phizio_name.setText(phzio_name_array[0].substring(0,1).toUpperCase()+ phzio_name_array[0].substring(1).toLowerCase());

            et_phizio_phone.setText(json_phizio.getString("phiziophone"));

            if(json_phizio.has("clinicname")) {
                if(json_phizio.getString("clinicname").length()>0) {
                    et_clinic_name.setText(json_phizio.getString("clinicname").substring(0, 1).toUpperCase() + json_phizio.getString("clinicname").substring(1));
                }
            }
            else {
                et_clinic_name.setText("");
            }

            if(json_phizio.has("phiziodob")) {
                if(json_phizio.getString("phiziodob").length()>0) {
                    et_dob.setText(json_phizio.getString("phiziodob"));
                }
            }
            else {
                et_dob.setText("");
            }

            if(json_phizio.has("experience")) {

                if(json_phizio.getString("experience").length()>0) {
                    et_experience.setText(json_phizio.getString("experience").substring(0, 1).toUpperCase() + json_phizio.getString("experience").substring(1).toLowerCase());
                }
            }else{

            }
            if(json_phizio.has("specialization")) {
                if(json_phizio.getString("specialization").length()>0) {
                    et_specialization.setText(json_phizio.getString("specialization").substring(0, 1).toUpperCase() + json_phizio.getString("specialization").substring(1));
                }
            }
            else {
                et_specialization.setText("");
            }


            if(json_phizio.has("degree")) {

                if(json_phizio.getString("degree").length()>0) {
                    et_degree.setText(json_phizio.getString("degree").substring(0, 1).toUpperCase() + json_phizio.getString("degree").substring(1));
                }
            }
            else {
                et_degree.setText("");
            }

            if(json_phizio.has("gender")) {
                gender = json_phizio.getString("gender");
                if(gender.equalsIgnoreCase("male"))
                {
                    rg_gender.check(gender_male.getId());
                }else if(gender.equalsIgnoreCase("female"))
                {
                    rg_gender.check(gender_female.getId());
                }

            }
            else {
                gender = "";
                rg_gender.check(gender_male.getId());
            }

            if(json_phizio.has("address")) {

                if(json_phizio.getString("address").length()>0) {
                    et_address.setText(json_phizio.getString("address"));
                }
            }
            else {
                et_address.setText("");
            }

            if(json_phizio.has("cliniclogo") && !json_phizio.getString("cliniclogo").equalsIgnoreCase("/icons/clinic.png")) {
//                iv_update_clinic_logo.setText("Update Clinic Logo");
                String temp = null;
                try {
                    temp = json_phizio.getString("cliniclogo");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Picasso.get().load(temp)
                        .placeholder(R.drawable.user_icon)
                        .error(R.drawable.user_icon)
                        .networkPolicy(NetworkPolicy.NO_CACHE,NetworkPolicy.NO_STORE)
                        .memoryPolicy(MemoryPolicy.NO_CACHE,MemoryPolicy.NO_STORE)
                        .transform(new PicassoCircleTransformation())
                        .into(iv_phizio_clinic_logo);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    DatePickerDialog.OnDateSetListener dateChangedListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, month);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        }
    };



    private void updateLabel() {
        String myFormat = "MM/dd/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());
        et_dob.setText(sdf.format(myCalendar.getTime()));
    }


    private void showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public boolean isValidPhone(CharSequence phone) {
        if (phone.toString().length()<10) return false;
        if (TextUtils.isEmpty(phone)) {
            return false;
        } else {
            return android.util.Patterns.PHONE.matcher(phone).matches();
        }
    }


    @Override
    public void onDetailsUpdated(Boolean response) {
        dialog.dismiss();
        if(response){
            showToast("Details updated");
        }
        else {
            showToast("Error please try again later");
        }
    }

    @Override
    public void onProfilePictureUpdated(Boolean response) {
        dialog.dismiss();
    }


    @Override
    public void onClinicLogoUpdated(Boolean response) {
        dialog.dismiss();
        if(response){
//            showToast("Updated clinic logo");
            if(iv_update_clinic_logo!=null){
//                iv_update_clinic_logo.setText("Update Clinic Logo");
            }
        }
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch(requestCode) {
            case 5:
                if(resultCode == RESULT_OK){
                    Bitmap photo = (Bitmap) imageReturnedIntent.getExtras().get("data");
                    photo = BitmapOperations.getResizedBitmap(photo,128);
                    iv_phizio_profilepic.setImageBitmap(photo);
                    ivBasicImage.setImageBitmap(photo);
                    repository.updatePhizioProfilePic(getIntent().getStringExtra("et_phizio_email"),photo);
                    dialog.setMessage("Uploading image, please wait");
                    dialog.show();
                }
                break;
            case 6:
                if(resultCode == RESULT_OK){
                    Uri selectedImage = imageReturnedIntent.getData();
                    iv_phizio_profilepic.setImageURI(selectedImage);
                    ivBasicImage.setImageURI(selectedImage);
                    iv_phizio_profilepic.invalidate();
                    BitmapDrawable drawable = (BitmapDrawable) iv_phizio_profilepic.getDrawable();
                    Bitmap photo = drawable.getBitmap();
                    photo = BitmapOperations.getResizedBitmap(photo,128);
                    repository.updatePhizioProfilePic(getIntent().getStringExtra("et_phizio_email"),photo);
                    dialog.setMessage("Uploading image, please wait");
                    dialog.show();
                }
                break;

            case 7:
                if(resultCode == RESULT_OK){
                    Bitmap photo = (Bitmap) imageReturnedIntent.getExtras().get("data");
                    photo = BitmapOperations.getResizedBitmap(photo,128);
                    iv_phizio_clinic_logo.setImageBitmap(photo);
                    repository.updatePhizioClinicLogoPic(getIntent().getStringExtra("et_phizio_email"),photo);
                    dialog.setMessage("Uploading image, please wait");
                    dialog.show();
                }
                break;
            case 8:
                if(resultCode == RESULT_OK){
                    Uri selectedImage = imageReturnedIntent.getData();
                    iv_phizio_clinic_logo.setImageURI(selectedImage);
                    iv_phizio_clinic_logo.invalidate();
                    try {
                        Bitmap photo = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                        photo = BitmapOperations.getResizedBitmap(photo,128);
                        repository.updatePhizioClinicLogoPic(getIntent().getStringExtra("et_phizio_email"),photo);
                        dialog.setMessage("Uploading image, please wait");
                        dialog.show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                break;
        }
    }




}