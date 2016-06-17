package com.example.grass.metering.calibration;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.example.grass.metering.R;
import com.example.grass.metering.list.Item;
import com.example.grass.metering.list.ItemAdapter;

import java.util.ArrayList;

public class CalibrationActivity extends Activity implements View.OnClickListener {
    ItemAdapter adapter;
    ArrayList<Item> list;
    int colibrCount = 0;
    SharedPreferences spAccurate;
    int itemPoistion = 0;
    Context context;
    ListView view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);
        context = this;
        list= new ArrayList<Item>();
        adapter = new ItemAdapter(this,list);
        spAccurate = getSharedPreferences("ACCURATE", MODE_PRIVATE);

        if(spAccurate.contains("colibrCount")!=true) {
            spAccurate.edit().putString("colibrCount", "0");
            spAccurate.edit().apply();
        }
        else
            colibrCount = Integer.parseInt(spAccurate.getString("colibrCount",""));

        importAccuracy(colibrCount);

        view = (ListView)findViewById(R.id.listView);
        if(view!=null)
        view.setAdapter(adapter);
        (findViewById(R.id.button4)).setOnClickListener(this);
        (findViewById(R.id.button3)).setOnClickListener(this);
        (findViewById(R.id.button2)).setOnClickListener(this);



        view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                itemPoistion = position+1;
                Intent intent = new Intent(context, VysCalibrActivity.class);
                startActivityForResult(intent,1);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button4:
                Intent intent = new Intent(this, VysCalibrActivity.class);
                startActivityForResult(intent,1);
                break;
            case R.id.button2:
                SharedPreferences.Editor editor = spAccurate.edit();
                editor.clear();
                editor.commit();
                finish();
                break;
            case R.id.button3:
                finish();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(data!=null){
            double angle  = 0.0;
            double height = 0.0;
            double eyeLength = 0.0;
            double eyeHeight= 0.0;
            angle = data.getDoubleExtra("angle",angle);
            height = data.getDoubleExtra("height",height);
            eyeLength = data.getDoubleExtra("eyeLength",eyeLength);
            eyeHeight = data.getDoubleExtra("eyeHeight",eyeHeight);
            double accurate = roundNumber(1-(height/eyeHeight),2);

            adapter.notifyDataSetChanged();
            list.add(new Item(eyeLength,height,angle,eyeHeight,roundNumber(accurate,2)));
            addAccuracy(++colibrCount, angle, eyeHeight, eyeLength, roundNumber(accurate,2), height);
            Log.d("log","angle = "+angle+"height = "+height);
        }
    }

    private void addAccuracy(int id,double angle,double eyeHeight,double eyeLength,double accurate, double height){
        SharedPreferences.Editor editor = spAccurate.edit();
            editor.putString("angle"+id,""+angle);
            editor.putString("eyeLength"+id,""+eyeLength);
            editor.putString("eyeHeight"+id,""+eyeHeight);
            editor.putString("accurate"+id,""+accurate);
            editor.putString("height"+id,""+height);
            if(id>colibrCount)
                colibrCount++;
            editor.putString("colibrCount",""+colibrCount);
            editor.putString("accurate",""+roundNumber(countDisp(),2));
        editor.commit();
    }
    private void importAccuracy(int count){

        for(int i  = 1;i <= count; i++){
            double angle = Double.parseDouble(spAccurate.getString("angle"+i,""));
            double eyeLength = Double.parseDouble(spAccurate.getString("eyeLength"+i,""));
            double eyeHeight = Double.parseDouble(spAccurate.getString("eyeHeight"+i,""));
            double accurate = Double.parseDouble(spAccurate.getString("accurate"+i,""));
            double length = Double.parseDouble(spAccurate.getString("length"+i,""));
            list.add(new Item(eyeHeight,length,angle,eyeLength,accurate));
        }
    }

    private double countDisp(){
        double sum  = 0;
        double disp = 0;
        for(Item item : list)
            sum += item.getError()/list.size();
        for(Item item : list){
            disp+=Math.pow(item.getError() - sum,2)/list.size();
        }
        return disp;
    }
    public double roundNumber(double number, double accurancy) {
        accurancy = Math.pow(10, accurancy);
        number = Math.round(number * accurancy);

        return number / accurancy;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(list.size()>0)
            view.setSelection(list.size()-1);
    }
}
