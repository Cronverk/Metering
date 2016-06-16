package com.example.grass.metering;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.grass.metering.calibration.CalibrationActivity;
import com.example.grass.metering.validation.MyValidator;
import com.example.grass.metering.validation.ValidationCallback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import static com.example.grass.metering.Constants.TAG;
import static com.example.grass.metering.Constants.VAL;


public class MeteringActivity extends AppCompatActivity implements View.OnClickListener, SensorEventListener,
        ValidationCallback, SoundPool.OnLoadCompleteListener {
    MeteringDialog dialog;
    SensorManager sensorManager;
    SharedPreferences spAccurate;

    private float[] accelerometerValues;
    private ArrayList<Double> angles;
    private double[] task_data;
    TextView heightView;
    TextView angleView;

    MeteringTask task;


    Sensor accelerometer;
    SoundPool sp;
    int sound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metering2);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        spAccurate = getSharedPreferences("ACCURATE", MODE_PRIVATE);

        //checkDate();

        sp = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        sp.setOnLoadCompleteListener(this);

        try {
            sound = sp.load(getAssets().openFd("1897.ogg"), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        task_data = new double[]{0, 0};

        dialog = new MeteringDialog();
        dialog.setMeteringActivity(this);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE); // Получаем менеджер сенсоров
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);

        heightView = (TextView) findViewById(R.id.heightValue);
        angleView = (TextView) findViewById(R.id.angleValue);
        angles = new ArrayList<>();

        dialog.show(getFragmentManager(), "Налаштування");

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


    }

    boolean checkDate() {

        SharedPreferences preferences = getSharedPreferences("my_settings", MODE_PRIVATE);

        long timeX = preferences.getLong("dayD", 0) - Calendar.getInstance().getTimeInMillis();

        Log.d(TAG, "checkDateDiff: " + " timeX" + timeX / (24 * 60 * 60 * 1000));
        if (timeX > 0) {
            Log.d(TAG, "checkDate() returned: " + true);
            return true;

        } else {
            Log.d(TAG, "checkDate() returned: " + false);
            MyValidator val = new MyValidator(getApplicationContext(), this);
            val.execute();
            return preferences.getBoolean(VAL, false);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layer:
                Log.d("ran", "ran");
                stopTask();
                break;
            case R.id.buttonChange:
                dialog.show(getFragmentManager(), "Налаштування");
                break;
            case R.id.buttonUpdate:
                try {
                    stopTask();
                    double[] data = dialog.getParams();
                    startTask(data[0], data[1]);
                }catch (Exception e){

                }
                break;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            accelerometerValues = event.values;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public float[] getOrientation() {
        float[] values = new float[3];
            double ax = accelerometerValues[0];
            double ay = accelerometerValues[1];
            double az = accelerometerValues[2];
            double x = Math.atan(ax / Math.sqrt(ay * ay + az * az));
            double y = Math.atan(ay / Math.sqrt(ax * ax + az * az));
            double z = Math.atan(az / Math.sqrt(ay * ay + ax * ax));


            values[0] = (float) Math.toDegrees(x);
            values[1] = (float) Math.toDegrees(y);
            values[2] = (float) Math.toDegrees(z) - 90;
            Log.d("orientation", "orientation 2 " + values[0] + " " + values[1] + " " + values[2]);
        return values;
    }

    public boolean checkRotate(float angle) {
        if (Math.abs(angle) > 60 && Math.abs(angle) < 115) {
            return true;
        }
        return false;
    }

    public double[] calculateHeight(double angle, double height, double length) {
            angles.add((double) roundNumber(angle, 2));

        if (angles.size() == 3) {
            angle = roundNumber(averageAngle(), 2);
            double tan = Math.tan(Math.toRadians(Math.abs(angle)));
            double height1 = length * tan;
            Log.d("orientation", "tan = " + tan + " height1 = " + height1+" lenght = "+length);
            task_data[0] = Math.abs(angle);
            task_data[1] = roundNumber(height + height1,1);
            angles = new ArrayList<>();

        }
        return task_data;
    }

    public double averageAngle() {
        double sum = 0;
        for (double value : angles)
            sum += value;
        return sum / angles.size();
    }


    public class MeteringTask extends AsyncTask<Double, String, double[]> {

        private boolean runFlag = true;

        public void stopTask() {
            runFlag = false;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            heightView.setText("00.00");
            angleView.setText("00.00");
            enableButtons(false);
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
                    Log.d("ff",""+values[1]);
                    if (values[1] > 0)
                        task_data = calculateHeight(values[1], height, length);
                    else task_data = calculateHeight(0, height, length);
                    publishProgress("" + doubleToDegree(task_data[0]));
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

            double accurate  = 0;
            if((spAccurate.contains("accurate"))==true) {
                accurate = Double.parseDouble(spAccurate.getString("accurate", "0"));
                Log.d("accurate",""+accurate);
            }


            heightView.setText("" + roundNumber(doubles[1]+accurate, 1));
            angleView.setText("" + doubleToDegree(doubles[0]));

            sp.play(sound, 1, 1, 0, 0, 1);

            enableButtons(true);
        }
    }

    public static String doubleToDegree(double value){
        int degree = (int) value;
        double rawMinute = Math.abs((value % 1) * 60);
        int minute = (int) rawMinute;
        return String.format("%d° %d′ ", degree,minute);
    }

    public void stopTask() {
        task.stopTask();
    }

    public void startTask(Double height, Double length) {

        task = new MeteringTask();
        task.execute(height, length);

    }

    private void enableButtons(boolean status){
        Button b = (Button)findViewById(R.id.buttonChange);
        b.setEnabled(status);
        b =(Button)findViewById(R.id.buttonUpdate);
        b.setEnabled(status);
        if(status){
            RelativeLayout layer = (RelativeLayout) findViewById(R.id.layer);
            layer.setVisibility(View.GONE);
        }
        else{
            RelativeLayout layer = (RelativeLayout) findViewById(R.id.layer);
            layer.setVisibility(View.VISIBLE);
        }
    }

    public double roundNumber(double number, double accurancy) {
        accurancy = Math.pow(10, accurancy);
        number = Math.round(number * accurancy);

        return number / accurancy;
    }

    public void showDiatog() {
        TelephonyManager manager = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        String imei = manager.getDeviceId();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Незареєстрована копія")
                .setMessage("Для реєстрації зателефонуйте за одним з цих номерів: \n(0572) 94-96-42, \n(050) 400-83-41, \n(097) 529-67-78. \nТа повідомте цей номер " + imei + ".")
                .setCancelable(false);
        builder.setNegativeButton("Відмова", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
      /*  builder.setPositiveButton("На сайт", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.lesovod.com.ua"));
                startActivity(browserIntent);
            }
        });
     */   builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });
        AlertDialog dialog = builder.create();


        dialog.show();
    }

    @Override
    public void valid(Boolean valid) {

        if (valid) {
            SharedPreferences preferences = getSharedPreferences("my_settings", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(Constants.VAL, true);
            editor.apply();

        } else {
            showDiatog();
        }
    }



    @Override
    public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_calibration) {
            SharedPreferences.Editor editor = spAccurate.edit();
            editor.clear();
            editor.commit();
            stopTask();

            Intent intent = new Intent(this, CalibrationActivity.class);
            startActivity(intent);
            return true;
        }
        if(id == R.id.action_help){
            Intent intent = new Intent(this, HelpActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
