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
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by Grass on 10.03.2016.
 */
public class MeteringDialog extends DialogFragment implements View.OnClickListener {

    public static final String APP_PREFERENCES = "meteringData";
    private SharedPreferences mSettings;
    private EditText editText;
    private View view;
    MeteringActivity activity;
    private AlertDialog dialog;
    Button button20;
    Button button30;

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
          //  check((int) id);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d("crea", "createDialog");

        mSettings = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = (LayoutInflater) getActivity().getLayoutInflater();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout

        view  = inflater.inflate(R.layout.dialog_metering, null);
        builder.setView(view);

        editText   = (EditText)   view.findViewById(R.id.editHeight);

        button20   = (Button)     view.findViewById(R.id.button20);
        button30   = (Button)     view.findViewById(R.id.button30);

        button20.setOnClickListener(this);
        button30.setOnClickListener(this);

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
        height  = Double.valueOf(""+map.get("height"));
        length  = Double.valueOf(""+map.get("length"));
        id      = Double.valueOf(""+ map.get("id"));

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
            case R.id.button20:
                value =  20;
                break;
            case  R.id.button30:
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
        /*
        switch (v.getId()) {
            case R.id.button20:
                check(R.id.button20);
                break;
            case R.id.button30:
                check(R.id.button30);
                break;
        }
*/
        int lid = getCheck();
        int length = getLength(lid);
        double height = 0;
        try {
            height = Double.parseDouble(editText.getText().toString());
        } catch (Exception e) {

        }
        if (height != 0) {
            saveData(lid, length, Float.valueOf("" + height));
            activity.startTask(height, (double) length);
            this.length = length;
            this.height = height;
            dialog.dismiss();
         //   Toast.makeText(getActivity().getApplicationContext(), "Значення має бути більше нуля", Toast.LENGTH_LONG).show();
        }


    }

    public double[] getParams(){
        return new double[]{height,length};
    }

    public void check(int id){
        button20.setEnabled(true);
        button30.setEnabled(true);
        switch (id){
            case R.id.button20:
                button20.setEnabled(false);
                break;
            case R.id.button30:
                button30.setEnabled(false);
                break;
        }
    }
    public int getCheck(){
        if(!button20.isEnabled())
            return R.id.button20;
        return R.id.button30;
    }
}
