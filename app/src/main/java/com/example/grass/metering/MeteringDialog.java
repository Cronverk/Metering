package com.example.grass.metering;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import java.util.HashMap;
import java.util.Map;

import ua.com.lesovod.taxation.R;

/**
 * Created by Grass on 10.03.2016.
 */
public class MeteringDialog extends DialogFragment implements View.OnClickListener {

    public static final String APP_PREFERENCES = "meteringData";
    private SharedPreferences mSettings;
    private RadioGroup radioGroup;
    private EditText editText;
    private View view;
    MeteringActivity activity;
    private AlertDialog dialog;

    private double height;
    private double length;

    HashMap<String,Double> map;

    public void setMeteringActivity(MeteringActivity activity){
        this.activity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



    }

    public void setViews(){
        map        = getData();
        if(map!=null) {
            editText.setText("" + map.get("height"));
            double id = map.get("id");
            radioGroup.check((int) id);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d("crea", "createDialog");

        mSettings = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout

        view  = inflater.inflate(R.layout.dialog_metering, null);
        builder.setView(view);

        editText   = (EditText)   view.findViewById(R.id.editHeight);
        radioGroup = (RadioGroup) view.findViewById(R.id.radioHeight);
        Button button = (Button) view.findViewById(R.id.buttonOk);
        button.setOnClickListener(this);

        dialog     = builder.create();
        setViews();

        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private void saveData(int id,int length,float height){
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putFloat("height", height);
        editor.putInt  ("length", length);
        editor.putInt  ("id"    , id);

        editor.commit();
    }
    private HashMap<String,Double> getData(){
        if (mSettings==null)
            return null;
        if (mSettings.getAll().size() ==0 )
            return null;

        double height   = 0;
        double   length = 0;
        double   id     = 0;

        Map map = mSettings.getAll();
        height  = new Double(""+map.get("height"));
        length  = new Double(""+map.get("length"));
        id      = new Double(""+ map.get("id"));

        HashMap<String,Double> data = null;
        if(height != 0 && length !=0 ) {
            data = new HashMap();
            data.put("height", height);
            data.put("length", length);
            data.put("id"    , id);
        }
        return data;
    }
    private int getLength(int id){
        int value = 0 ;
        switch (id){
            case R.id.radioButton1:
                value =  20;
                break;
            case  R.id.radioButton2:
                value =  30;
                break;
        }
        return value;
    }


    @Override
    public int show(FragmentTransaction transaction, String tag) {

        setViews();
        return super.show(transaction, tag);
    }

    @Override
    public void onClick(View v) {
        int lid    = radioGroup.getCheckedRadioButtonId();
        int length = getLength(radioGroup.getCheckedRadioButtonId());
        double height = 1.9;
        try {
            height = Double.parseDouble(editText.getText().toString());
        }catch (Exception e){

        }
            saveData(lid,length,new Float(""+height));
        activity.startTask(height, (double) length);
        this.length = length;
        this.height = height;

        dialog.dismiss();
    }

    public double[] getParams(){
        return new double[]{height,length};
    }
}
