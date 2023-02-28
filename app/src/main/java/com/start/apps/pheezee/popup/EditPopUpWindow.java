package com.start.apps.pheezee.popup;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import start.apps.pheezee.R;

import com.start.apps.pheezee.activities.PatientsView;
import com.start.apps.pheezee.pojos.PatientDetailsData;
import com.start.apps.pheezee.room.Entity.PhizioPatients;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Collections;

import static com.start.apps.pheezee.activities.PatientsView.REQ_CAMERA;
import static com.start.apps.pheezee.activities.PatientsView.REQ_GALLERY;

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

    boolean gallery_selected=false;
    boolean camera_selected=false;

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
        ImageView patient_profilepic_image = layout.findViewById(R.id.profile_picture);

        if(use_new_photo==false)
        {
        Glide.with(context)
                .load("https://s3.ap-south-1.amazonaws.com/pheezee/physiotherapist/" + json_phizioemail.replaceFirst("@", "%40") + "/patients/" + patient.getPatientid() + "/images/profilepic.png")
                .apply(new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .error(R.drawable.test_patient_add_1))
                        .listener(new RequestListener<Drawable>() {
                              @Override
                              public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                  return false;
                              }

                              @Override
                              public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                  patient_profilepic_image.setVisibility(View.VISIBLE);
                                  patient_profilepic.setVisibility(View.GONE);
                                  return false;
                              }
                          }
                )
                .into(patient_profilepic_image);

        }else {
            if(this.profile!=null){
                patient_profilepic_image.setImageBitmap(this.profile);
                patient_profilepic_image.setVisibility(View.VISIBLE);
                patient_profilepic.setVisibility(View.GONE);
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
        if(patient.getPatientgender().equalsIgnoreCase("Male")) {
            radioGroup.check(btn_male.getId());
        }
        else{
            radioGroup.check(btn_female.getId());
        }
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

                // Custom notification added by Haaris
                // custom dialog
                final Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.set_profile_photo_layout);


                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(dialog.getWindow().getAttributes());
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

                dialog.getWindow().setAttributes(lp);

                ImageView take_photo_asset =  dialog.findViewById(R.id.take_photo_asset);
                ImageView gallery_asset =  dialog.findViewById(R.id.gallery_asset);

                Button Notification_Button_ok = (Button) dialog.findViewById(R.id.notification_ButtonOK);



                // On click on Continue
                Notification_Button_ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        if(camera_selected==true)
                        {
                            if(ContextCompat.checkSelfPermission(context,CAMERA) == PackageManager.PERMISSION_DENIED) {

                                final Dialog dialog = new Dialog(context);
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
                                notification_title.setText("Camera permission request");
                                notification_message.setText("Pheezee app needs camera permission \n to access camera");

                                // On click on Continue
                                Notification_Button_ok.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if(ContextCompat.checkSelfPermission(context, CAMERA) == PackageManager.PERMISSION_DENIED) {
                                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                            Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                                            intent.setData(uri);
                                            context.startActivity(intent);
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
                            } else if(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                pw.dismiss();
//                                ActivityCompat.requestPermissions(((Activity)context), new String[]{Manifest.permission.CAMERA}, 5);
                                cameraIntent();
                            }
                            else {
                                pw.dismiss();
                                cameraIntent();
                            }

                            dialog.dismiss();

                        }else if(gallery_selected==true)
                        {
                            if(ContextCompat.checkSelfPermission(context,READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
//                                Log.i("Location_status:","working");
                                final Dialog dialog = new Dialog(context);
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
                                notification_title.setText("Storage permission request");
                                notification_message.setText("Pheezee app needs storage permission \n to access your gallery");

                                // On click on Continue
                                Notification_Button_ok.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if(ContextCompat.checkSelfPermission(context, READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                            Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                                            intent.setData(uri);
                                            context.startActivity(intent);
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
                            } else if(ContextCompat.checkSelfPermission(context, READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                                galleryIntent();
                                pw.dismiss();
                                dialog.dismiss();
                            }
                            else {
                                pw.dismiss();
                                galleryIntent();
                            }

                        }

                    }
                });

                // On click on Continue
                take_photo_asset.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        gallery_selected=false;
                        camera_selected=true;
                        gallery_asset.setImageResource(context.getResources().getIdentifier("ic_gallery_unselected", "drawable",context.getPackageName()));
                        take_photo_asset.setImageResource(context.getResources().getIdentifier("ic_camera_selected", "drawable",context.getPackageName()));
                        Notification_Button_ok.setBackground(context.getResources().getDrawable(R.drawable.round_same_buttons));
                        Notification_Button_ok.setTextColor(ContextCompat.getColor(context,R.color.white));




                    }
                });

                // On click on Continue
                gallery_asset.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        gallery_selected=true;
                        camera_selected=false;
                        gallery_asset.setImageResource(context.getResources().getIdentifier("ic_gallery_selected", "drawable",context.getPackageName()));
                        take_photo_asset.setImageResource(context.getResources().getIdentifier("ic_camera_unselected", "drawable",context.getPackageName()));
                        Notification_Button_ok.setBackground(context.getResources().getDrawable(R.drawable.round_same_buttons));
                        Notification_Button_ok.setTextColor(ContextCompat.getColor(context,R.color.white));


                    }
                });

                dialog.show();

                // End

            }
        });

        patient_profilepic_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Custom notification added by Haaris
                // custom dialog
                final Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.set_profile_photo_layout);


                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(dialog.getWindow().getAttributes());
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

                dialog.getWindow().setAttributes(lp);

                ImageView take_photo_asset =  dialog.findViewById(R.id.take_photo_asset);
                ImageView gallery_asset =  dialog.findViewById(R.id.gallery_asset);

                Button Notification_Button_ok = (Button) dialog.findViewById(R.id.notification_ButtonOK);



                // On click on Continue
                Notification_Button_ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        if(camera_selected==true)
                        {
                            if(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                                    == PackageManager.PERMISSION_DENIED) {
                                pw.dismiss();
                                ActivityCompat.requestPermissions(((Activity)context), new String[]{Manifest.permission.CAMERA}, 5);
                                cameraIntent();
                            }
                            else {
                                pw.dismiss();
                                cameraIntent();
                            }

                            dialog.dismiss();

                        }else if(gallery_selected==true)
                        {
                            galleryIntent();
                            pw.dismiss();
                            dialog.dismiss();
                        }else
                        {
                            Toast.makeText(context, "Please select any one option.", Toast.LENGTH_SHORT).show();
                        }


                    }
                });

                // On click on Continue
                take_photo_asset.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        gallery_selected=false;
                        camera_selected=true;
                        gallery_asset.setImageResource(context.getResources().getIdentifier("ic_gallery_unselected", "drawable",context.getPackageName()));
                        take_photo_asset.setImageResource(context.getResources().getIdentifier("ic_camera_selected", "drawable",context.getPackageName()));
                        Notification_Button_ok.setBackground(context.getResources().getDrawable(R.drawable.round_same_buttons));
                        Notification_Button_ok.setTextColor(ContextCompat.getColor(context,R.color.white));




                    }
                });

                // On click on Continue
                gallery_asset.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        gallery_selected=true;
                        camera_selected=false;
                        gallery_asset.setImageResource(context.getResources().getIdentifier("ic_gallery_selected", "drawable",context.getPackageName()));
                        take_photo_asset.setImageResource(context.getResources().getIdentifier("ic_camera_unselected", "drawable",context.getPackageName()));
                        Notification_Button_ok.setBackground(context.getResources().getDrawable(R.drawable.round_same_buttons));
                        Notification_Button_ok.setTextColor(ContextCompat.getColor(context,R.color.white));


                    }
                });

                dialog.show();

                // End

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
