package com.start.apps.pheezee.popup;
import com.start.apps.pheezee.services.PheezeeBleService;
import com.start.apps.pheezee.services.Scanner;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import start.apps.pheezee.R;
import com.start.apps.pheezee.activities.ScanDevicesActivity;
import com.start.apps.pheezee.utils.BatteryOperation;
import static com.start.apps.pheezee.activities.PatientsView.deviceBatteryPercent;

public class AddDevicePopupWindow {

    private Context context;
    private PopupWindow add_device_popup=null;
    Intent to_scan_devices_activity;
    TextView tv_cancel;
    LinearLayout my_device_image_layout;
    String macc_address,device_name;
    boolean connected_state;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    PheezeeBleService mService;
    ImageView iv_device_connected,iv_device_disconnected,iv_device;
    public static boolean popup_state=false;
    View layout;
    ProgressBar battery_bar;
    RelativeLayout rl_battery_usb_state;
    boolean usb_state=false;



    public AddDevicePopupWindow(Context context, String macc_address, boolean connected_state, String device_name,SharedPreferences sharedPref, PheezeeBleService mService){
        this.context = context;
        this.macc_address = macc_address;
        this.connected_state = connected_state;
        this.device_name = device_name;
        this.sharedPref = sharedPref;
        this.mService = mService;


    }

    public AddDevicePopupWindow(Context context, String macc_address, boolean connected_state, String device_name,SharedPreferences sharedPref, PheezeeBleService mService,boolean usb_state){
        this.context = context;
        this.macc_address = macc_address;
        this.connected_state = connected_state;
        this.device_name = device_name;
        this.sharedPref = sharedPref;
        this.mService = mService;
        this.usb_state=usb_state;



    }



    public void UpdateWindow(Context context, String macc_address, boolean connected_state, String device_name, SharedPreferences sharedPref, PheezeeBleService mService){
        this.context = context;
        this.macc_address = macc_address;
        this.connected_state = connected_state;
        this.device_name = device_name;
        this.sharedPref = sharedPref;
        this.mService = mService;


    }

    public void UpdateWindow(Context context, String macc_address, boolean connected_state, String device_name, SharedPreferences sharedPref, PheezeeBleService mService,boolean usb_state){
        this.context = context;
        this.macc_address = macc_address;
        this.connected_state = connected_state;
        this.device_name = device_name;
        this.sharedPref = sharedPref;
        this.mService = mService;
        this.usb_state = usb_state;


    }

    public void refreshwindow(){

        ImageView summary_go_back = layout.findViewById(R.id.summary_go_back);
        ImageView my_device_image_state = layout.findViewById(R.id.my_device_image_state);
        TextView my_device_name = layout.findViewById(R.id.my_device_name);
        TextView my_device_macaddress = layout.findViewById(R.id.my_device_macaddress);
        ImageView my_device_button_state = layout.findViewById(R.id.my_device_button_state);
        TextView tv_add_new_device = layout.findViewById(R.id.tv_add_new_device);
        battery_bar = layout.findViewById(R.id.progress_battery_bar);
        TextView tv_battery_percent = layout.findViewById(R.id.tv_battery_percent);
        LinearLayout ll_battery_bar = layout.findViewById(R.id.battery_bar);
        rl_battery_usb_state = layout.findViewById(R.id.rl_battery_usb_state);


        if(my_device_image_state!=null) {
            if (connected_state == true) {
                my_device_image_state.setImageResource(R.drawable.my_device_image_connected);
                my_device_name.setText(device_name);
                my_device_macaddress.setText(macc_address);
                my_device_button_state.setImageResource(R.drawable.connected_image);
//                my_device_button_state.setText("Connected");
                ll_battery_bar.setVisibility(View.VISIBLE);
                if (usb_state) {
                    rl_battery_usb_state.setVisibility(View.VISIBLE);
                } else rl_battery_usb_state.setVisibility(View.GONE);

                //Battery Status
                int percent = BatteryOperation.convertBatteryToCell(deviceBatteryPercent);
                if (deviceBatteryPercent < 15) {
                    Drawable drawable = context.getResources().getDrawable(R.drawable.drawable_progress_battery_low);
                    battery_bar.setProgressDrawable(drawable);
                } else {
                    Drawable drawable = context.getResources().getDrawable(R.drawable.drawable_progress_battery);
                    battery_bar.setProgressDrawable(drawable);
                }
                battery_bar.setProgress(percent);
                if(deviceBatteryPercent==-1) {
                    tv_battery_percent.setText(String.valueOf(1) + "%");
                }else tv_battery_percent.setText(String.valueOf(deviceBatteryPercent) + "%");

            } else {

                my_device_image_state.setImageResource(R.drawable.my_device_image_disconnected);
                my_device_name.setText(device_name);
                my_device_macaddress.setText(macc_address);
                my_device_button_state.setImageResource(R.drawable.disconnect_image);
//                my_device_button_state.setText("Disconnected");
                ll_battery_bar.setVisibility(View.GONE);

            }

            summary_go_back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    add_device_popup.dismiss();
                }
            });


            tv_add_new_device.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (macc_address.equalsIgnoreCase("")) {
//                        add_device_popup.dismiss();
                    } else {
                        showForgetDeviceDialog();
                    }
                }
            });
        }

    }




    public void showWindow(){
        Configuration config = ((Activity)context).getResources().getConfiguration();


        if(macc_address=="") {

            layout = ((Activity) context).getLayoutInflater().inflate(R.layout.add_device_popup, null);

            add_device_popup = new PopupWindow(layout, ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT, true);
            add_device_popup.setWindowLayoutMode(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT);
            add_device_popup.setOutsideTouchable(true);
            add_device_popup.showAtLocation(layout, Gravity.CENTER, 0, 0);

            ImageView summary_go_back = layout.findViewById(R.id.summary_go_back);
            ImageView add_device_ble_scan = layout.findViewById(R.id.add_device_ble_scan);
            ImageView add_device_qr_scan = layout.findViewById(R.id.add_device_qr_scan);
            tv_cancel = layout.findViewById(R.id.tv_cancel);
            my_device_image_layout = layout.findViewById(R.id.my_device_image_layout);


            summary_go_back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    add_device_popup.dismiss();
                }
            });

            tv_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    add_device_popup.dismiss();
                }
            });

                add_device_ble_scan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    to_scan_devices_activity = new Intent(context, ScanDevicesActivity.class);
                    ((Activity) context).startActivityForResult(to_scan_devices_activity, 12);
                    add_device_popup.dismiss();
                }
            });

            add_device_qr_scan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((Activity) context).startActivityForResult(new Intent(context, Scanner.class), 12);
                    add_device_popup.dismiss();
                }
            });

        }else if(macc_address!="")
        {
            layout = ((Activity) context).getLayoutInflater().inflate(R.layout.add_device_state, null);

            add_device_popup = new PopupWindow(layout, ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT, true);
            add_device_popup.setWindowLayoutMode(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT);
            add_device_popup.setOutsideTouchable(true);
            add_device_popup.showAtLocation(layout, Gravity.CENTER, 0, 0);


            ImageView summary_go_back = layout.findViewById(R.id.summary_go_back);
            ImageView my_device_image_state = layout.findViewById(R.id.my_device_image_state);
            TextView my_device_name = layout.findViewById(R.id.my_device_name);
            TextView my_device_macaddress = layout.findViewById(R.id.my_device_macaddress);
            ImageView my_device_button_state = layout.findViewById(R.id.my_device_button_state);
            TextView tv_add_new_device = layout.findViewById(R.id.tv_add_new_device);
            battery_bar = layout.findViewById(R.id.progress_battery_bar);
            TextView tv_battery_percent = layout.findViewById(R.id.tv_battery_percent);
            LinearLayout ll_battery_bar = layout.findViewById(R.id.battery_bar);
            rl_battery_usb_state = layout.findViewById(R.id.rl_battery_usb_state);



            if(connected_state==true){
                my_device_image_state.setImageResource(R.drawable.my_device_image_connected);
                my_device_name.setText(device_name);
                my_device_macaddress.setText(macc_address);
                my_device_button_state.setImageResource(R.drawable.connected_image);
                ll_battery_bar.setVisibility(View.VISIBLE);
                if(usb_state)
                {
                    rl_battery_usb_state.setVisibility(View.VISIBLE);
                }else rl_battery_usb_state.setVisibility(View.GONE);

                //Battery Status
                int percent = BatteryOperation.convertBatteryToCell(deviceBatteryPercent);
                if(deviceBatteryPercent<15) {
                    Drawable drawable = context.getResources().getDrawable(R.drawable.drawable_progress_battery_low);
                    battery_bar.setProgressDrawable(drawable);
                }
                else {
                    Drawable drawable = context.getResources().getDrawable(R.drawable.drawable_progress_battery);
                    battery_bar.setProgressDrawable(drawable);
                }
                battery_bar.setProgress(percent);
                if(deviceBatteryPercent==-1) {
                    tv_battery_percent.setText(String.valueOf(1) + "%");
                }else tv_battery_percent.setText(String.valueOf(deviceBatteryPercent) + "%");

            }else
            {
                my_device_image_state.setImageResource(R.drawable.my_device_image_disconnected);
                my_device_name.setText(device_name);
                my_device_macaddress.setText(macc_address);
                my_device_button_state.setImageResource(R.drawable.disconnect_image);
                ll_battery_bar.setVisibility(View.GONE);
            }

            summary_go_back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    add_device_popup.dismiss();
                }
            });


            tv_add_new_device.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(macc_address.equalsIgnoreCase("")) {
//                        add_device_popup.dismiss();
                    }else
                    {
                        showForgetDeviceDialog();
                    }
                }
            });


        }

     

       }

    private void showToast(String nothing_selected) {
        Toast.makeText(context, nothing_selected, Toast.LENGTH_SHORT).show();
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };

    public void showForgetDeviceDialog(){

        // Custom notification added by Haaris
        // custom dialog
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
        notification_title.setText("Forget Device");
        notification_message.setText("Are you sure you want to forget the current device?");

        // On click on Continue
        Notification_Button_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                editor = sharedPref.edit();
                editor.putString("deviceMacaddress","");
                editor.apply();
                if(mService!=null){
                    mService.forgetPheezee();
                    mService.disconnectDevice();
                }


                dialog.dismiss();
                macc_address="";
                connected_state=false;
                add_device_popup.dismiss();

                View layout = ((Activity) context).getLayoutInflater().inflate(R.layout.app_bar_patients_view, null);;
                iv_device_connected = layout.findViewById(R.id.iv_device_connected);
                iv_device_disconnected = layout.findViewById(R.id.iv_device_disconnected);
                iv_device = layout.findViewById(R.id.iv_device);

                iv_device_connected.setVisibility(View.GONE);
                iv_device_disconnected.setVisibility(View.GONE);
                iv_device.setVisibility(View.VISIBLE);

                showWindow();


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

        // End


    }

    public boolean ispopupshowing()
    {
        if(add_device_popup!=null) {
            return add_device_popup.isShowing();
        }else return false;
    }

    public void dissmiss_popup()
    {
        if(add_device_popup!=null) {
            add_device_popup.dismiss();
        }
    }



}


