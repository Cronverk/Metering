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
import android.view.WindowManager;
import android.widget.TextView;

import java.util.ArrayList;


public class MeteringActivity extends Activity implements View.OnClickListener, SensorEventListener {
    MeteringDialog dialog;
    SensorManager sensorManager;

    private float[] accelerometerValues;
    private float[] magneticFieldValues;
    private ArrayList<Double> angles;
    private double[] task_data;
    TextView heightView;
    TextView angleView;

    MeteringTask task;


    Sensor accelerometer;
    Sensor magneticField;
    private double zy_angle;
    Display display;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_metering);

        task_data = new double[]{0,0};

        dialog = new MeteringDialog();
        dialog.setMeteringActivity(this);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE); // Получаем менеджер сенсоров
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        sensorManager.registerListener(this,accelerometer,SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this,magneticField,SensorManager.SENSOR_DELAY_UI);

        heightView = (TextView)findViewById(R.id.heightValue);
        angleView  = (TextView)findViewById(R.id.angleValue);
        angles = new ArrayList<>();

        dialog.show(getFragmentManager(),"Налаштування");

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.layer:
                Log.d("ran","ran");
                stopTask();
                break;
            case R.id.buttonChange:
                dialog.show(getFragmentManager(), "Налаштування");
                break;
            case R.id.buttonUpdate:
                double[] data = dialog.getParams();
                startTask(data[0],data[1]);
                break;
        }
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

        if(magneticFieldValues !=null) {
            float[] R = new float[9];
            SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticFieldValues);
            SensorManager.getOrientation(R, values);

            values[0] = (float) Math.toDegrees(values[0]);
            values[1] = (float) Math.toDegrees(values[1]);
            values[2] = (float) Math.toDegrees(values[2]);
            Log.d("orientation","orientation 1 "+ values[0]+" " +values[1]+ " " + values[2]);
        }else {
            double ax = accelerometerValues[0];
            double ay = accelerometerValues[1];
            double az = accelerometerValues[2];
            double x  = Math.atan(ax/Math.sqrt(ay*ay+az*az));
            double y  = Math.atan(ay/Math.sqrt(ax*ax+az*az));
            double z  = Math.atan(az/Math.sqrt(ay*ay+ax*ax));


            values[0] = (float) Math.toDegrees(x);
            values[1] = (float) Math.toDegrees(y);
            values[2] = (float) Math.toDegrees(z)-90;
            Log.d("orientation","orientation 2 "+ values[0]+" " +values[1]+ " " + values[2]);
        }
        return values;
    }
    public boolean checkRotate(float angle){
        if(Math.abs(angle)>60&& Math.abs(angle)<115){
            return true;
        }
        return false;
    }
    public double[] calculateHeight(double angle,double height,double length){
        if(angle>0)
            angles.add((double) 0);
        else
        angles.add((double) roundNumber(angle,2));

        if(angles.size() == 3) {
            angle = roundNumber(averageAngle(),2);
            double tan = Math.tan(Math.toRadians(Math.abs(angle)));
            double height1 = length * tan;
            Log.d("orientation", "tan = " + tan + " angle = " + angle);
            task_data[0] = Math.abs(angle);
            task_data[1] = height+height1;
            angles = new ArrayList<>();

        }
        return task_data;
    }
    public double averageAngle(){
        double sum = 0;
        for(double value : angles)
            sum +=value;
        return sum/angles.size();
    }

    public class MeteringTask extends AsyncTask<Double,String,double[]>{

        private boolean runFlag =true;

        public void stopTask(){
            runFlag = false;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            heightView.setText("00.00");
            angleView.setText ("00.00");
        }

        @Override
        protected double[] doInBackground(Double... params) {
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
                   // if (values[1] < 0)
                        task_data = calculateHeight(values[1], height, length);
                   // else res = 0;
                    publishProgress(""+roundNumber(task_data[0],2));
                }
            }
            return task_data;
        }
        protected void onProgressUpdate(String... data) {
            angleView.setText(data[0]);
        }

        @Override
        protected void onPostExecute(double[] doubles) {
            super.onPostExecute(doubles);

            heightView.setText("" + roundNumber(doubles[1],2));
            angleView.setText ("" + doubles[0]);
        }
    }

    public void stopTask(){
        task.stopTask();
    }
    public void startTask(Double height,Double length){
        task= new MeteringTask();
        task.execute(height, length);
    }


    public double roundNumber(double number , double accurancy){
        accurancy = Math.pow(10,accurancy);
        number    = Math.round(number*accurancy);

        return number/accurancy;
    }


}
