package com.startoonlabs.apps.pheezee.popup;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.startoonlabs.apps.pheezee.R;
import com.startoonlabs.apps.pheezee.pojos.PatientDetailsData;
import com.startoonlabs.apps.pheezee.room.Entity.PhizioPatients;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.startoonlabs.apps.pheezee.activities.PatientsView.REQ_CAMERA;
import static com.startoonlabs.apps.pheezee.activities.PatientsView.REQ_GALLERY;
import static com.startoonlabs.apps.pheezee.activities.PatientsView.phizio_packagetype;

public class EditPopUpWindow {
    Context context;
    static PhizioPatients patient;
    onClickListner listner;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    Bitmap profile;
    String json_phizioemail;
    AlertDialog.Builder builder = null;
    boolean use_new_photo=false;
    final CharSequence[] items = { "Take Photo", "Choose from Library",
            "Cancel" };

    public EditPopUpWindow(final Activity context, PhizioPatients patient, String json_phizioemail){
        this.context = context;
        this.patient = patient;
        this.json_phizioemail = json_phizioemail;
    }

    public EditPopUpWindow(Context context,String json_phizioemail, Bitmap photo){
        this.context = context;
        this.json_phizioemail = json_phizioemail;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        editor = sharedPref.edit();
        this.profile = photo;
        use_new_photo = true;
    }

    public void openEditPopUpWindow(){
        PopupWindow pw;
        final String[] case_description = {""};
        Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
        Point size = new Point();display.getSize(size);int width = size.x;int height = size.y;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        @SuppressLint("InflateParams") final View layout = inflater.inflate(R.layout.popup, null);

        pw = new PopupWindow(layout, width - 100, ViewGroup.LayoutParams.WRAP_CONTENT,true);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            pw.setElevation(10);
        }
        pw.setTouchable(true);
        pw.setOutsideTouchable(true);
        pw.setContentView(layout);
        pw.setFocusable(true);
        pw.setAnimationStyle(R.style.Animation);
        pw.showAtLocation(layout, Gravity.CENTER, 0, 0);

        final TextView tv_patientId = layout.findViewById(R.id.tv_patient_id);
        final TextView tv_create_account = layout.findViewById(R.id.tv_create_account);
        final TextView patientName = layout.findViewById(R.id.patientName);
        final TextView patientAge = layout.findViewById(R.id.patientAge);
        final TextView caseDescription = layout.findViewById(R.id.contentDescription);
        final RadioGroup radioGroup = layout.findViewById(R.id.patientGender);
        RadioButton btn_male = layout.findViewById(R.id.radioBtn_male);
        RadioButton btn_female = layout.findViewById(R.id.radioBtn_female);
        final Spinner sp_case_des = layout.findViewById(R.id.sp_case_des);
        ImageView patient_profilepic = layout.findViewById(R.id.imageView4);

        if(use_new_photo==false)
        {
        Glide.with(context)
                .load("https://s3.ap-south-1.amazonaws.com/pheezee/physiotherapist/" + json_phizioemail.replaceFirst("@", "%40") + "/patients/" + patient.getPatientid() + "/images/profilepic.png")
                .apply(new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true))
                .into(patient_profilepic);
        }else {
            if(this.profile!=null){
                patient_profilepic.setImageBitmap(this.profile);
            }

        }

        tv_patientId.setText("Patient ID: "+patient.getPatientid());
        tv_patientId.setVisibility(View.VISIBLE);
        tv_create_account.setText("Edit Patient");
        //Adapter for spinner
        ArrayAdapter<String> array_exercise_names = new ArrayAdapter<String>(context, R.layout.support_simple_spinner_dropdown_item, context.getResources().getStringArray(R.array.case_description));
        array_exercise_names.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        sp_case_des.setAdapter(array_exercise_names);

        String[] cases_list = context.getResources().getStringArray(R.array.case_description);
        ArrayList<String> arrayList = new ArrayList<>();
        Collections.addAll(arrayList,cases_list);

        if(arrayList.contains(patient.getPatientcasedes())) {
            sp_case_des.setSelection(arrayList.indexOf(patient.getPatientcasedes()));
        }else{
            sp_case_des.setSelection(arrayList.size()-1);
        }


        sp_case_des.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm=(InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
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
                String patientname = patientName.getText().toString();
                String patientage = patientAge.getText().toString();
                if ((!patientname.equals(""))  && (!patientage.equals(""))&& (!case_description[0].equals("")) && btn!=null) {
                    PhizioPatients patients = new PhizioPatients(patient.getPatientid(),patientname,patient.getNumofsessions(),patient.getDateofjoin()
                                ,patientage,btn.getText().toString(),case_description[0],patient.getStatus(),patient.getPatientphone(),patient.getPatientprofilepicurl(),patient.isSceduled());
                    patient.setPatientname(patientname);
                    patient.setPatientage(patientage);
                    patient.setPatientcasedes(case_description[0]);
                    patient.setPatientgender(btn.getText().toString());
                    PatientDetailsData data = new PatientDetailsData(json_phizioemail, patient.getPatientid(),
                            patient.getPatientname(),patient.getNumofsessions(), patient.getDateofjoin(), patient.getPatientage(),
                            patient.getPatientgender(), patient.getPatientcasedes(), patient.getStatus(), patient.getPatientphone(), patient.getPatientprofilepicurl());
                    listner.onAddClickListner(patient,data,true,profile);
                    pw.dismiss();
                }
                else {
                    listner.onAddClickListner(null,null,false,profile);
                }
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pw.dismiss();
            }
        });

        patient_profilepic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                builder = new AlertDialog.Builder(context);
                builder.setTitle("Add Photo!");
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (items[item].equals("Take Photo")) {
                            pw.dismiss();
                            if(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                                    == PackageManager.PERMISSION_DENIED) {
                                ActivityCompat.requestPermissions(((Activity)context), new String[]{Manifest.permission.CAMERA}, 5);
                                cameraIntent();
                            }
                            else {
                                cameraIntent();
                            }
                        } else if (items[item].equals("Choose from Library")) {
                            pw.dismiss();
                            galleryIntent();
                        } else if (items[item].equals("Cancel")) {
                            dialog.dismiss();
                        }
                    }
                });
                builder.show();
            }
        });
    }

    private void cameraIntent() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            ((Activity) context).startActivityForResult(takePicture, 41);
        }else {
            ActivityCompat.requestPermissions(((Activity) context), new String[] {Manifest.permission.CAMERA}, REQ_CAMERA);
        }
    }
    private void galleryIntent() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        pickPhoto.putExtra("patientid",1);
            ((Activity) context).startActivityForResult(pickPhoto, 42);
        }else {
            ActivityCompat.requestPermissions(((Activity) context), new String[] {Manifest.permission.CAMERA}, REQ_GALLERY);
        }
    }


    public interface onClickListner{
        void onAddClickListner(PhizioPatients patients, PatientDetailsData data, boolean isvalid,Bitmap photo);
    }

    public void setOnClickListner(onClickListner listner){
        this.listner = listner;
    }
}
