package com.start.apps.pheezee.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.Transition;

import start.apps.pheezee.R;

import com.start.apps.pheezee.activities.DeleteAccountTwo;
import com.start.apps.pheezee.classes.CircularRevealTransition;
import com.start.apps.pheezee.fragments.StandardGoldTeachFragment;
import com.start.apps.pheezee.popup.SessionSummaryStandardPopupWindow;
import com.start.apps.pheezee.utils.MuscleOperation;
import com.start.apps.pheezee.utils.ValueBasedColorOperations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.facebook.FacebookSdk.getApplicationContext;
import static com.start.apps.pheezee.activities.PatientsView.phizio_packagetype;
import static com.start.apps.pheezee.utils.PackageTypes.STANDARD_PACKAGE;

public class BodyPartSelectionRecyclerViewAdapter extends RecyclerView.Adapter<BodyPartSelectionRecyclerViewAdapter.ViewHolder> {
    private int selected_position = -1;
    Context context;
    String bodypart_name_str;
    //    int[] myPartList = new int[]{R.drawable.elbow_part, R.drawable.knee_part,R.drawable.ankle_part,R.drawable.hip_part,
//            R.drawable.wrist_part,R.drawable.shoulder_part,R.drawable.other_body_part};
    TypedArray myPartList;
    String[] string_array_bodypart;
    private String default_max_emg = "900";
    //    private List<BodyPartWithMmtSelectionModel> bodyPartsList;
    private int color_after_selected , color_nothing_selected;
    private String str_start, str_end, str_max_emg;
    onBodyPartOptionsSelectedListner listner;
    ArrayAdapter<String> array_exercise_names;
    Map <String,String> primary_muscle_lookuptable =  new HashMap<String,String>();


    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_body_part_name;
        ImageView iv_body_part_image;
        ConstraintLayout cl_body_tv_and_image;
        ConstraintLayout cl_selection;
        ConstraintLayout cl_dash;
        RadioGroup rg_orientation;
        RadioGroup rg_body_orientation;
        Spinner sp_exercise_name, sp_muscle_name, sp_goal;
        EditText et_max_angle, et_min_angle, et_max_emg, et_muscle_name, et_exercise_name;
        TextView tv_start, tv_end, tv_max_emg, tv_max_emg_text, tv_max_angle_text;


        ViewHolder(View view) {
            super(view);

            cl_body_tv_and_image = view.findViewById(R.id.model_cl_image_tv);
            cl_selection = view.findViewById(R.id.model_selection);
            cl_dash = view.findViewById(R.id.constraintLayout2);
            rg_body_orientation = view.findViewById(R.id.model_rg_body_orientation);
            rg_orientation = view.findViewById(R.id.model_rg_orientation);

            //spinnsers
            sp_exercise_name = view.findViewById(R.id.model_sp_exercise_name);
            sp_muscle_name = view.findViewById(R.id.model_sp_musclename_name);
            sp_goal = view.findViewById(R.id.model_sp_set_goal);

            //textviews
            tv_body_part_name = view.findViewById(R.id.model_tv_body_part_name);

            //Imageview
            iv_body_part_image = view.findViewById(R.id.model_iv_bodypart_image);

            //Number picker
            et_max_angle = view.findViewById(R.id.model_et_max_angle);
            et_max_emg = view.findViewById(R.id.model_et_max_emg);
            et_min_angle = view.findViewById(R.id.model_et_min_angle);
            et_muscle_name = view.findViewById(R.id.model_et_muscle_name);
            et_exercise_name = view.findViewById(R.id.model_et_exercise_name);

            //TextView
            tv_start = view.findViewById(R.id.model_tv_start);
            tv_end = view.findViewById(R.id.model_tv_stop);
            tv_max_emg = view.findViewById(R.id.tv_max_emg);

            tv_max_emg_text = view.findViewById(R.id.tv_max_emg_text);
            tv_max_angle_text = view.findViewById(R.id.tv_max_angle_text);

            if(phizio_packagetype==STANDARD_PACKAGE){
                tv_end.setVisibility(View.GONE);
                tv_start.setVisibility(View.GONE);
                tv_max_emg.setVisibility(View.GONE);
                et_max_angle.setVisibility(View.GONE);
                et_min_angle.setVisibility(View.GONE);
                et_max_emg.setVisibility(View.GONE);
                sp_goal.setVisibility(View.GONE);
                cl_dash.setVisibility(View.GONE);
                tv_max_emg_text.setVisibility(View.GONE);
                tv_max_angle_text.setVisibility(View.GONE);
            }
        }
    }

    public BodyPartSelectionRecyclerViewAdapter( Context context){
        this.context = context;
        this.myPartList = context.getResources().obtainTypedArray(R.array.body_part);
        string_array_bodypart = context.getResources().getStringArray(R.array.bodyPartName);
        this.str_start = context.getResources().getString(R.string.start);
        this.str_end = context.getResources().getString(R.string.end);
        this.str_max_emg = context.getResources().getString(R.string.max_emg);
        this.color_after_selected = context.getResources().getColor(R.color.pitch_black);
        this.color_nothing_selected = context.getResources().getColor(R.color.pitch_black);

        primary_muscle_lookuptable.put("shoulderflexion","Deltoid");
        primary_muscle_lookuptable.put("shoulderextension","Latissimus Dorsi");
        primary_muscle_lookuptable.put("shoulderadduction","Pectoralis Major");
        primary_muscle_lookuptable.put("shoulderabduction","Deltoid");
        primary_muscle_lookuptable.put("shouldermedial rotation","Pectoralis Major");
        primary_muscle_lookuptable.put("shoulderlateral rotation","Infraspinatus");
        primary_muscle_lookuptable.put("shoulderisometric","Select Muscle*");

        primary_muscle_lookuptable.put("elbowflexion","Biceps");
        primary_muscle_lookuptable.put("elbowextension","Tricep");
        primary_muscle_lookuptable.put("elbowisometric","Select Muscle*");

        primary_muscle_lookuptable.put("forearmsupination","Supinator (Deep)");
        primary_muscle_lookuptable.put("forearmpronation","Pronator Quadratus (Deep)");
        primary_muscle_lookuptable.put("forearmisometric","Select Muscle*");

        primary_muscle_lookuptable.put("wristflexion","Flexor Carpi Radialis");
        primary_muscle_lookuptable.put("wristextension","Extensor Digitorum");
        primary_muscle_lookuptable.put("wristradial deviation","Flexor Carpi Radialis");
        primary_muscle_lookuptable.put("wristulnar deviation","Extensor Carpi Ulnaris");
        primary_muscle_lookuptable.put("wristisometric","Select Muscle*");

        primary_muscle_lookuptable.put("ankleplantarflexion","Soleus - Posterior");
        primary_muscle_lookuptable.put("ankledorsiflexion","Tibialis Anterior");
        primary_muscle_lookuptable.put("ankleinversion","Tibialis Anterior");
        primary_muscle_lookuptable.put("ankleeversion","Peroneus Longus - Lateral");
        primary_muscle_lookuptable.put("ankleisometric","Select Muscle*");

        primary_muscle_lookuptable.put("kneeflexion","Gastrocnemius - Posterior");
        primary_muscle_lookuptable.put("kneeextension","Rectus Femoris - Anterior");
        primary_muscle_lookuptable.put("kneeisometric","Select Muscle*");

        primary_muscle_lookuptable.put("hipflexion","Tensor Fasciae Latae");
        primary_muscle_lookuptable.put("hipextension","Gluteus Maximus - Gluteal");
        primary_muscle_lookuptable.put("hipadduction","Adductor Magnus - Medial");
        primary_muscle_lookuptable.put("hipabduction","Gluteus Medius");
        primary_muscle_lookuptable.put("hipmedial rotation","Tensor Fasciae Latae - Gluteal");
        primary_muscle_lookuptable.put("hiplateral rotation","Adductor Magnus - Medial");
        primary_muscle_lookuptable.put("hipisometric","Select Muscle*");



        primary_muscle_lookuptable.put("spineflexion","");
        primary_muscle_lookuptable.put("spineextension","Iliocostalis Cervicis");
        primary_muscle_lookuptable.put("spinelateral flexion","Iliocostalis Thoracis");
        primary_muscle_lookuptable.put("spinerotation","Multifidus");
        primary_muscle_lookuptable.put("spineisometric","Select Muscle*");

        primary_muscle_lookuptable.put("cervicalflexion","");
        primary_muscle_lookuptable.put("cervicalextension","Iliocostalis Cervicis");
        primary_muscle_lookuptable.put("cervicallateral flexion","Iliocostalis Cervicis");
        primary_muscle_lookuptable.put("cervicalrotation","Iliocostalis Cervicis");
        primary_muscle_lookuptable.put("cervicalisometric","Select Muscle*");

        primary_muscle_lookuptable.put("thoracicflexion","");
        primary_muscle_lookuptable.put("thoracicextension","Iliocostalis Thoracis");
        primary_muscle_lookuptable.put("thoraciclateral flexion","");
        primary_muscle_lookuptable.put("thoracicrotation","");
        primary_muscle_lookuptable.put("thoracicisometric","Select Muscle*");

        primary_muscle_lookuptable.put("lumbarflexion","Rectus Abdominis");
        primary_muscle_lookuptable.put("lumbarextension","Quadratus Lumborum");
        primary_muscle_lookuptable.put("lumbarlateral flexion","Psoas major");
        primary_muscle_lookuptable.put("lumbarrotation","Multifidus");
        primary_muscle_lookuptable.put("lumbarisometric","Select Muscle*");

        primary_muscle_lookuptable.put("abdomenflexion","Rectus Abdominis");
        primary_muscle_lookuptable.put("abdomenextension","");
        primary_muscle_lookuptable.put("abdomenlateral flexion","Quadratus Lumborum");
        primary_muscle_lookuptable.put("abdomenrotation","External Oblique");
        primary_muscle_lookuptable.put("abdomenisometric","Select Muscle*");


    }


    @NonNull
    @Override
    public BodyPartSelectionRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new view
        if(viewType == 0)
        {
            final View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.body_part_selection_list_header, parent, false);
            return new BodyPartSelectionRecyclerViewAdapter.ViewHolder(itemView);
        }else if(viewType == 5)
        {
            final View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.body_part_selection_list_header, parent, false);
            return new BodyPartSelectionRecyclerViewAdapter.ViewHolder(itemView);
        }else if(viewType == 9)
        {
            final View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.body_part_selection_list_header, parent, false);
            return new BodyPartSelectionRecyclerViewAdapter.ViewHolder(itemView);
        }else if(viewType == 12)
        {
            final View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.body_part_selection_list_header, parent, false);
            return new BodyPartSelectionRecyclerViewAdapter.ViewHolder(itemView);
        }else{

        final View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.body_part_selection_list_model, parent, false);
            return new BodyPartSelectionRecyclerViewAdapter.ViewHolder(itemView);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull final BodyPartSelectionRecyclerViewAdapter.ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
//        BodyPartWithMmtSelectionModel bodyPartWithMmtSelectionModel = bodyPartsList.get(position);
        holder.iv_body_part_image.setImageResource(myPartList.getResourceId(position,-1));
        holder.tv_body_part_name.setText(string_array_bodypart[position]);





        if(selected_position!=position && selected_position!=-1){
            holder.cl_selection.setVisibility(View.GONE);
        }

        holder.cl_body_tv_and_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Transition transition = new CircularRevealTransition();
                transition.setDuration(300);
                transition.addTarget(holder.cl_selection);
                Animation aniFade = AnimationUtils.loadAnimation(context,R.anim.fade_in);
                holder.cl_body_tv_and_image.setAnimation(aniFade);
                holder.cl_body_tv_and_image.bringToFront();
                holder.rg_body_orientation.clearCheck();
                holder.rg_orientation.clearCheck();
                if(selected_position==position){
                    selected_position=-1;
//                    TransitionManager.beginDelayedTransition((ViewGroup)holder.cl_selection.getParent(), transition);
                    holder.cl_selection.setVisibility(View.GONE);
                    if(listner!=null){
                        listner.onBodyPartSelected(null);
                        listner.onOrientationSelected(null);
                        listner.onBodyOrientationSelected(null);
                        listner.onExerciseNameSelected(null);
                        listner.onMuscleNameSelected(null,0);
                        listner.onGoalSelected(0);
                        listner.onMaxEmgUpdated("");
                        listner.onMaxAngleUpdated("");
                        listner.onMinAngleUpdated("");
                        listner.onBodyPartSelectedPostion(-1);
                    }
                }else{
                    if(selected_position!=-1){
                        notifyItemChanged(selected_position);
                    }
//                    TransitionManager.beginDelayedTransition((ViewGroup)holder.cl_selection.getParent(), transition);
                    holder.cl_selection.setVisibility(View.VISIBLE);
                    selected_position = position;
                    if(listner!=null){
                        String bodypart = string_array_bodypart[position];
                        bodypart_name_str = string_array_bodypart[position];
                        listner.onBodyPartSelected(bodypart);
                        listner.onBodyPartSelectedPostion(selected_position);
                    }
                    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,R.array.setSessionGoalSpinner, R.layout.support_simple_spinner_dropdown_item);
                    adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                    holder.sp_goal.setAdapter(adapter);

                    ArrayAdapter<CharSequence> array_muscle_names = new ArrayAdapter<CharSequence>(context, R.layout.support_simple_spinner_dropdown_item, MuscleOperation.getExerciseNames(bodypart_name_str));
                    array_muscle_names.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                    holder.sp_exercise_name.setAdapter(array_muscle_names);



                    array_exercise_names = new ArrayAdapter<String>(context, R.layout.support_simple_spinner_dropdown_item, MuscleOperation.getMusleNames(bodypart_name_str)){
                        @Override
                        public View getDropDownView(int position, View convertView,
                                                    ViewGroup parent) {
                            View view = super.getDropDownView(position, convertView, parent);
                            TextView tv = (TextView) view;

                            String[] temp = MuscleOperation.getPrimarySecondaryMuscle(bodypart_name_str.toLowerCase(),holder.sp_exercise_name.getSelectedItem().toString().toLowerCase(),0);

//                            Log.d("string",String.valueOf(temp.length));
//                            Log.d("string",temp[0]);
//                            Log.d("string",bodypart_name_str);
//                            Log.d("string",holder.sp_exercise_name.getSelectedItem().toString().toLowerCase());

                            // Primary Muscles
                            for(int i =0 ; i<temp.length; i++ )
                            {
                                if(temp[i].equalsIgnoreCase(holder.sp_muscle_name.getItemAtPosition(position).toString().toLowerCase()))
                                {
                                        // Do nothing
                                        tv.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                                        tv.setTextColor(Color.parseColor("#FF000000"));
                                        return view;
                                }
                            }

                            // Secondary Muscles
                            temp = MuscleOperation.getPrimarySecondaryMuscle(bodypart_name_str,holder.sp_exercise_name.getSelectedItem().toString().toLowerCase(),1);
                            for(int i =0 ; i<temp.length; i++ )
                            {
                                if(temp[i].equalsIgnoreCase(holder.sp_muscle_name.getItemAtPosition(position).toString().toLowerCase()))
                                {
                                    // Make it italic
                                    tv.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
                                    tv.setTextColor(Color.parseColor("#FF000000"));
                                    return view;
                                }
                            }

                            // Other muscles
                            tv.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                            tv.setTextColor(Color.parseColor("#707070"));



                            return view;

                        }
                    };
                    array_exercise_names.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                    holder.sp_muscle_name.setAdapter(array_exercise_names);

//                    if(selected_position==8){
//                        holder.sp_exercise_name.setVisibility(View.INVISIBLE);
//                        holder.et_exercise_name.setVisibility(View.VISIBLE);
//                        holder.sp_muscle_name.setVisibility(View.INVISIBLE);
//                        holder.et_muscle_name.setVisibility(View.VISIBLE);
//                    }
                }
            }
        });

        holder.rg_orientation.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton btn = group.findViewById(checkedId);
                if(btn!=null) {
                    String orientation = btn.getText().toString();
                    if (listner != null) {
                        listner.onOrientationSelected(orientation);
                    }
                }
            }
        });
// images postion
        holder.rg_body_orientation.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton btn = group.findViewById(checkedId);
                if(btn!=null) {
                    String body_orientation = btn.getText().toString();
                    if (listner != null) {
                        listner.onBodyOrientationSelected(body_orientation);
                    }
                }
            }
        });
// getting information
        holder.sp_exercise_name.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position!=0){
                    String exercise_name = holder.sp_exercise_name.getSelectedItem().toString();
                    int normal_min = ValueBasedColorOperations.getBodyPartMinValue(bodypart_name_str,position);
                    int normal_max = ValueBasedColorOperations.getBodyPartMaxValue(bodypart_name_str,position);
                    holder.tv_end.setText(str_end.concat(String.valueOf(normal_max)));
                    holder.tv_start.setText(str_start.concat(String.valueOf(normal_min)));
                    holder.tv_max_emg.setText(str_max_emg.concat(": "+String.valueOf(900)));
                    holder.et_max_angle.setText(String.valueOf(normal_max));
                    holder.et_min_angle.setText(String.valueOf(normal_min));
                    holder.et_max_emg.setText(default_max_emg);
                    holder.tv_max_emg.setTextColor(color_after_selected);
                    holder.tv_start.setTextColor(color_after_selected);
                    holder.tv_end.setTextColor(color_after_selected);


                    // Start

                    array_exercise_names = new ArrayAdapter<String>(context, R.layout.support_simple_spinner_dropdown_item, MuscleOperation.getMusleNames(bodypart_name_str.toLowerCase(),holder.sp_exercise_name.getSelectedItem().toString().toLowerCase())){
                        @Override
                        public View getDropDownView(int position, View convertView,
                                                    ViewGroup parent) {
                            View view = super.getDropDownView(position, convertView, parent);
                            TextView tv = (TextView) view;

                            String[] temp = MuscleOperation.getPrimarySecondaryMuscle(bodypart_name_str.toLowerCase(),holder.sp_exercise_name.getSelectedItem().toString().toLowerCase(),0);

//                            Log.d("string",String.valueOf(temp.length));
//                            Log.d("string",temp[0]);
//                            Log.d("string",bodypart_name_str);
//                            Log.d("string",holder.sp_exercise_name.getSelectedItem().toString().toLowerCase());

                            // Primary Muscles
                            for(int i =0 ; i<temp.length; i++ )
                            {
                                if(temp[i].equalsIgnoreCase(holder.sp_muscle_name.getItemAtPosition(position).toString().toLowerCase()))
                                {
                                    // Do nothing
                                    tv.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                                    tv.setTextColor(Color.parseColor("#FF000000"));
                                    return view;
                                }
                            }

                            // Secondary Muscles
                            temp = MuscleOperation.getPrimarySecondaryMuscle(bodypart_name_str,holder.sp_exercise_name.getSelectedItem().toString().toLowerCase(),1);
                            for(int i =0 ; i<temp.length; i++ )
                            {
                                if(temp[i].equalsIgnoreCase(holder.sp_muscle_name.getItemAtPosition(position).toString().toLowerCase()))
                                {
                                    // Make it italic
                                    tv.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                                    tv.setTextColor(Color.parseColor("#FF000000"));
                                    return view;
                                }
                            }

                            // Other muscles
                            tv.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                            tv.setTextColor(Color.parseColor("#707070"));



                            return view;

                        }
                    };
                    array_exercise_names.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                    holder.sp_muscle_name.setAdapter(array_exercise_names);


                    // End
// data storing
                    ArrayList<String> arrayList = new ArrayList<>();
                    Collections.addAll(arrayList,MuscleOperation.getMusleNames(bodypart_name_str.toLowerCase(),holder.sp_exercise_name.getSelectedItem().toString().toLowerCase()));

                    String dictionary_value = bodypart_name_str+exercise_name;
                    dictionary_value = dictionary_value.toLowerCase();

//                    Log.d("lookup",dictionary_value);
//                    Log.d("lookup",primary_muscle_lookuptable.get(dictionary_value));


                    if(arrayList.contains(primary_muscle_lookuptable.get(dictionary_value))) {
                        holder.sp_muscle_name.setSelection(arrayList.indexOf(primary_muscle_lookuptable.get(dictionary_value)));
                        listner.onMuscleNameSelected(primary_muscle_lookuptable.get(dictionary_value),arrayList.indexOf(primary_muscle_lookuptable.get(dictionary_value)));
                    }else{
                        // Do nothing
                    }

                    if(listner!=null){
                        listner.onExerciseNameSelected(exercise_name);
                        listner.onMinAngleUpdated(String.valueOf(normal_min));
                        listner.onMaxAngleUpdated(String.valueOf(normal_max));
                        listner.onMaxEmgUpdated(String.valueOf(900));
                        listner.onExerciseSelectedPostion(position);
                    }
                }else {
                    holder.tv_end.setText(str_end);
                    holder.tv_start.setText(str_start);
                    holder.tv_max_emg.setText(str_max_emg);
                    holder.et_min_angle.setText("");
                    holder.et_max_angle.setText("");
                    holder.et_max_emg.setText("");
                    holder.tv_max_emg.setTextColor(color_nothing_selected);
                    holder.tv_start.setTextColor(color_nothing_selected);
                    holder.tv_end.setTextColor(color_nothing_selected);
                    if(listner!=null){
                        listner.onExerciseNameSelected(null);
                        listner.onMinAngleUpdated("");
                        listner.onMaxAngleUpdated("");
                        listner.onMaxEmgUpdated("");
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
// musule
        holder.sp_muscle_name.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    if(listner!=null){
                        String muscle_name = holder.sp_muscle_name.getSelectedItem().toString();
                        listner.onMuscleNameSelected(muscle_name, position);
                    }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
// set reps
        holder.sp_goal.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position!=0){
                    String str_goal = holder.sp_goal.getSelectedItem().toString().substring(0,2);
                    str_goal = str_goal.replaceAll("\\s+","");
                    int reps_selected = Integer.parseInt(str_goal);
                    if(listner!=null){
                        listner.onGoalSelected(reps_selected);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                if(listner!=null){
                    listner.onGoalSelected(0);
                }
            }
        });

//
        holder.sp_goal.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
        holder.sp_muscle_name.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
        holder.sp_exercise_name.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
// emg
        holder.et_max_emg.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(listner!=null){
                    String s1 = s.toString();
                    listner.onMaxEmgUpdated(s1);
                }
            }
        });

// edit text musl
        holder.et_muscle_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(listner!=null){
                    String s1 = s.toString();
                    if(s1.length()>0)
                        listner.onMuscleNameSelected(s1,1);
                    else
                        listner.onMuscleNameSelected(null,-1);
                }
            }
        });
// edit exs
        holder.et_exercise_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(listner!=null){
                    String s1 = s.toString();
                    if(s1.length()>0) {
                        listner.onExerciseNameSelected(s1);
                        listner.onExerciseSelectedPostion(1);
                    }else {
                        listner.onExerciseNameSelected(null);
                        listner.onExerciseSelectedPostion(-1);
                    }
                }
            }
        });
// max angle
        holder.et_max_angle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(listner!=null){
                    String s1 = s.toString();
                    listner.onMaxAngleUpdated(s1);
                }
            }
        });
//Min
        holder.et_min_angle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(listner!=null){
                    String s1 = s.toString();
                    listner.onMinAngleUpdated(s1);
                }
            }
        });

    }
// set postion - 01
    public void setPositionToInitial()
    {
        selected_position=-1;
    }

    @Override
    public int getItemCount() {
        return string_array_bodypart==null?0:string_array_bodypart.length;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }


    public interface onBodyPartOptionsSelectedListner{
        void onBodyPartSelected(String bodypart);
        void onOrientationSelected(String orientation);
        void onBodyOrientationSelected(String body_orientation);
        void onExerciseNameSelected(String exercise_name);
        void onMuscleNameSelected(String muscle_name, int position);
        void onGoalSelected(int reps_selected);
        void onMaxEmgUpdated(String max_emg_updated);
        void onMaxAngleUpdated(String max_angle_updated);
        void onMinAngleUpdated(String min_angle_updated);
        void onBodyPartSelectedPostion(int position);
        void onExerciseSelectedPostion(int position);



    }


    public void onSetOptionsSelectedListner(onBodyPartOptionsSelectedListner listner){
        this.listner = listner;
    }
}