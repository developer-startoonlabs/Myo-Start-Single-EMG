package com.start.apps.pheezee.popup;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static com.google.common.reflect.Reflection.getPackageName;
import static com.start.apps.pheezee.activities.PatientsView.REQ_CAMERA;
import static com.start.apps.pheezee.activities.PatientsView.REQ_GALLERY;

import com.start.apps.pheezee.activities.PatientsView;

import start.apps.pheezee.R;

public class UploadImageDialog {

    Context context;
    AlertDialog.Builder builder = null;
    final CharSequence[] items = { "Take Photo", "Choose from Library",
            "Cancel" };

    private int result_code_gallery, result_code_camera;


    public UploadImageDialog(Context context, int result_code_camera, int result_code_gallery){
        this.context = context;
        this.result_code_camera = result_code_camera;
        this.result_code_gallery = result_code_gallery;
    }

    public void showDialog(){
        builder = new AlertDialog.Builder(context);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    cameraIntent();
                } else if (items[item].equals("Choose from Library")) {
                    galleryIntent();
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }

        });
        builder.show();
    }

    private void cameraIntent() {
        if(ContextCompat.checkSelfPermission(context, CAMERA) == PackageManager.PERMISSION_DENIED) {

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
        }else if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            ((Activity) context).startActivityForResult(takePicture, result_code_camera);
        }
    }


    private void galleryIntent() {
        if(ContextCompat.checkSelfPermission(context, READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            Log.i("Location_status:","working");
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
        }else if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        pickPhoto.putExtra("patientid",1);
            ((Activity) context).startActivityForResult(pickPhoto, result_code_gallery);
        }else {
            ActivityCompat.requestPermissions(((Activity) context), new String[] {Manifest.permission.CAMERA}, REQ_GALLERY);
        }
    }


}
