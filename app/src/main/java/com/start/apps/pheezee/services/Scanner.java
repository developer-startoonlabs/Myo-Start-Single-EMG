package com.start.apps.pheezee.services;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.vision.barcode.Barcode;
import start.apps.pheezee.R;

import com.start.apps.pheezee.activities.PatientsView;
import com.start.apps.pheezee.utils.RegexOperations;

import java.util.List;

import info.androidhive.barcode.BarcodeReader;

public class Scanner extends AppCompatActivity implements BarcodeReader.BarcodeReaderListener {

    ImageView back_button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        back_button = findViewById(R.id.summary_go_back);

        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onScanned(Barcode barcode) {
        Intent intent = new Intent();
        boolean isMac = RegexOperations.validate(barcode.displayValue);
        if(isMac) {
            intent.putExtra("macAddress", barcode.displayValue);
            setResult(-1, intent);
        }
        else {
            intent.putExtra("macAddress", barcode.displayValue);
            setResult(2, intent);
        }
        finish();
    }

    @Override
    public void onScannedMultiple(List<Barcode> barcodes) {

    }

    @Override
    public void onBitmapScanned(SparseArray<Barcode> sparseArray) {

    }

    @Override
    public void onScanError(String errorMessage) {

    }

    @Override
    public void onCameraPermissionDenied() {
        if(ContextCompat.checkSelfPermission(this, CAMERA) == PackageManager.PERMISSION_DENIED) {
            Log.i("Location_status:","working");
            final Dialog dialog = new Dialog(this);
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
            notification_title.setText("Camera Permission Request");
            notification_message.setText("Pheezee App Need Camera Permission \n to access camera");

            // On click on Continue
            Notification_Button_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                        dialog.dismiss();
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
        }

    }
}
