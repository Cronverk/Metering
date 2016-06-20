package com.example.grass.metering.calibration;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.example.grass.metering.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Grass on 10.03.2016.
 */
public class VysCalibrDialog extends DialogFragment implements View.OnClickListener {

    public static final String APP_PREFERENCES = "meteringData";
    private static SharedPreferences mSettings;
    private EditText editHeight;
    private EditText editLength;
    private View view;
    VysCalibrActivity activity;
    private AlertDialog dialog;

    public double eyeHeight;
    public double eyeLength;

    public void setMeteringActivity(VysCalibrActivity activity){
        this.activity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);


    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d("crea", "createDialog");

        mSettings = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
        getData();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = (LayoutInflater) getActivity().getLayoutInflater();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout

        view  = inflater.inflate(R.layout.dialog_meter, null);
        builder.setView(view);

        editHeight   = (EditText)   view.findViewById(R.id.editHeight);
        editLength   = (EditText)   view.findViewById(R.id.editLength);
        Button button = (Button) view.findViewById(R.id.button);
        button.setOnClickListener(this);
        setEdtit(eyeHeight,eyeLength);
        dialog     = builder.create();
        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private void saveData(float eyeHeight, float eyeLength){
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString("eyeHeight",""+ eyeHeight);
        editor.putString("eyeLength",""+ eyeLength);

        editor.commit();
    }

    @Override
    public int show(FragmentTransaction transaction, String tag) {

        return super.show(transaction, tag);
    }

    @Override
    public void onClick(View v) {
    //    try {
            double eyeHeight = Double.parseDouble(editHeight.getText().toString());
            double eyeLength = Double.parseDouble(editLength.getText().toString());
            if (eyeHeight != 0) {
                saveData(eyeHeight,eyeLength);
                    dialog.dismiss();
                }
    //        }catch(Exception e){}
    }

    public void saveData(double eyeHeight, double eyeLength){
        saveData(Float.valueOf("" + eyeHeight),Float.valueOf("" + eyeLength));
        this.eyeHeight = eyeHeight;
        this.eyeLength = eyeLength;
        activity.startTask(eyeHeight);
    }

    private void getData(){
        if (mSettings==null)
            return ;
        if (mSettings.getAll().size() ==0 )
            return ;
    try {
        eyeHeight = Double.parseDouble(mSettings.getString("eyeHeight", ""));
        eyeLength = Double.parseDouble(mSettings.getString("eyeLength", ""));
    }catch (Exception e){}
    }

    private void setEdtit(double eyeHeight, final double eyeLength){
        if(eyeHeight!=0&& eyeLength!=0) {
            editHeight.setText("" + eyeHeight);
            editLength.setText("" + eyeLength);

/*
            editHeight.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {

                    editHeight.setText("");
                }
            });
            /*editLength.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editHeight.setText("");
                }
            });*/
        }
    }

    public static void clear(){
        SharedPreferences.Editor edit =  mSettings.edit();

        edit.putString("eyeHeight", "0.0");
        edit.putString("eyeLength","0.0");
        edit.commit();
    }
}
