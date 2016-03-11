package com.example.grass.metering;


import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

import ua.com.lesovod.taxation.R;

public class MeteringActivity extends Activity implements View.OnClickListener, SensorEventListener {
    MeteringDialog dialog;
    SensorManager sensorManager;

    private float[] accelerometerValues;
    private float[] magneticFieldValues;
    private ArrayList<Double> angles;
    private double res = 0.0 ;
    TextView heightView;

    MeteringTask task;


    Sensor accelerometer;
    Sensor magneticField;
    private double zy_angle;
    Display display;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_metering);

        dialog = new MeteringDialog();
        dialog.setMeteringActivity(this);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE); // Получаем менеджер сенсоров
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        sensorManager.registerListener(this,accelerometer,SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this,magneticField,SensorManager.SENSOR_DELAY_UI);

        heightView = (TextView)findViewById(R.id.heightValue);
        angles = new ArrayList<>();

        dialog.show(getFragmentManager(),"Налаштування");
    }

    @Override
    protected void onResume() {
        super.onResume();

    }
    @Override
    public void onClick(View v) {
        stopTask();
        dialog.show(getFragmentManager(), "Налаштування");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            accelerometerValues = event.values;
        if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            magneticFieldValues = event.values;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public float[] getOrientation(){
        float[] values = new float[3];
        float[] R = new float[9];
        SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticFieldValues);
        SensorManager.getOrientation(R, values);

        values[0] = (float) Math.toDegrees(values[0]);
        values[1] = (float) Math.toDegrees(values[1]);
        values[2] = (float) Math.toDegrees(values[2]);
        return values;
    }
    public boolean checkRotate(float angle){
        if(Math.abs(angle)>60&& Math.abs(angle)<115){
            return true;
        }
        return false;
    }
    public double calculateHeight(float angle,double height,double length){
        angles.add((double) Math.round(angle));

        if(angles.size() == 3) {
            angle = Math.round(averageAngle());
            double tan = Math.tan(Math.toRadians(Math.abs(angle)));
            double height1 = length * tan;
            Log.d("orientation", "tan = " + tan + " angle = " + angle);
            angles = new ArrayList<>();
            return height + height1;
        }
        else return res;
    }
    public double averageAngle(){
        double sum = 0;
        for(double value : angles)
            sum +=value;
        return sum/angles.size();
    }

    public class MeteringTask extends AsyncTask<Double,String,Void>{

        private boolean runFlag =true;

        public void stopTask(){
            runFlag = false;
        }
        @Override
        protected Void doInBackground(Double... params) {
            double height = params[0];
            double length = params[1];
            while (runFlag) {
                try {
                    new Thread().sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                float[] values = getOrientation();
                if (checkRotate(values[2])) {
                    if (values[1] < 0)
                        res = calculateHeight(values[1], height, length);
                    else res = 0;
                    publishProgress(""+Math.round(res));
                }
            }
            return null;
        }
        protected void onProgressUpdate(String... height) {
            heightView.setText(height[0]);
        }
    }

    public void stopTask(){
        task.stopTask();
    }
    public void startTask(Double height,Double length){
        task= new MeteringTask();
        task.execute(height,length);
    }

}
