package com.example.finalproject;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.os.Vibrator;
import android.os.VibrationEffect;

import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;

import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.FileNotFoundException;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private Vibrator v;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;
    private Sensor mLinearAcc;
    private Sensor mGravity;

    private Boolean isFirst = true;

    private File fAcc;
    private File fGyro;
    private File fLinearAcc;
    private File fGrav;
    private BufferedOutputStream outAcc;
    private BufferedOutputStream outGyro;
    private BufferedOutputStream outLinear;
    private BufferedOutputStream outGrav;

    private TextView accVal;
    private TextView gyroVal;

    private EditText uName;
    private EditText uAge;
    private EditText uHeight;
    private EditText uHeight2;
    private EditText uWeight;
    private EditText uWeight2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mLinearAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

        accVal = (TextView) findViewById(R.id.accVal);
        gyroVal = (TextView) findViewById(R.id.gyroVal);

        uName = (EditText)findViewById(R.id.userNameVal);
        uAge = (EditText)findViewById(R.id.userAgeVal);
        uHeight = (EditText)findViewById(R.id.userHeightVal);
        uWeight = (EditText)findViewById(R.id.userWeightVal);
        uHeight2 = (EditText)findViewById(R.id.userHeightVal2);
        uWeight2 = (EditText)findViewById(R.id.userWeightVal2);

        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    private boolean isUserInfoValid(View view) {
        String data = "";
        Log.d("Debug", "in info valid");
        if (uName.getText().toString().isEmpty()) {
            uName.setError("Please enter a name");
            return false;
        }
        if (uAge.getText().toString().isEmpty()) {
            uAge.setError("Please enter your age");
            return false;
        }
        if (uHeight.getText().toString().isEmpty() && uHeight2.getText().toString().isEmpty()) {
            Toast.makeText(view.getContext(), "Please enter your height",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        if (uWeight.getText().toString().isEmpty() && uWeight2.getText().toString().isEmpty()) {
            Toast.makeText(view.getContext(), "Please enter your weight",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    public void startDetection(View view) {
        if (isFirst) {
            Log.d("isFirst", "in!");
            if (!isUserInfoValid(view)) {
                return;
            }

            String filename = uName.getText().toString();
            File f = new File(view.getContext().getExternalFilesDir(null), filename + ".txt");
            try {
                FileOutputStream fOut = new FileOutputStream(f);
                String data = filename + "," + uAge.getText().toString() + ","
                        + uHeight.getText().toString() + "," + uHeight2.getText().toString() + ","
                        + uWeight.getText().toString() + "," + uWeight2.getText().toString();
                fOut.write(data.getBytes());
                fOut.flush();
                fOut.close();
            } catch (FileNotFoundException e) {
                Log.e("File", "Failed to create user file");
                Toast.makeText(view.getContext(), "Unable to create user file",
                        Toast.LENGTH_SHORT).show();
                return;
            } catch (IOException e) {
                Log.e("File", "Failed to write data into user file");
                Toast.makeText(view.getContext(), "Unable to write data into user file",
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Button btn = (Button) findViewById(R.id.buttonStart);

        Long tsLong = System.currentTimeMillis();
        String userName = uName.getText().toString();
        String filenameAcc = userName + "-" + tsLong.toString() + "-acc.txt";
        String filenameGyro = userName + "-" + tsLong.toString() + "-gyro.txt";
        String filenameLinearAcc = userName + "-" + tsLong.toString() + "-lacc.txt";
        String filenameGrav = userName + "-" + tsLong.toString() + "-grav.txt";

        if (btn.getText().toString().contentEquals("Start")) {
            // throws error if storage is not writable
            if (!this.isExternalStorageWritable()) {
                Log.e("File", "External storage is not writable");
                Toast.makeText(view.getContext(), "External storage is not writable",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            fAcc = new File(view.getContext().getExternalFilesDir(null), filenameAcc);
            fGyro = new File(view.getContext().getExternalFilesDir(null), filenameGyro);
            fLinearAcc = new File(view.getContext().getExternalFilesDir(null), filenameLinearAcc);
            fGrav = new File(view.getContext().getExternalFilesDir(null), filenameGrav);

            try {
                outAcc = new BufferedOutputStream(new FileOutputStream(fAcc), 2048);
                outGyro = new BufferedOutputStream(new FileOutputStream(fGyro), 2048);
                outLinear = new BufferedOutputStream(new FileOutputStream(fLinearAcc), 2048);
                outGrav = new BufferedOutputStream(new FileOutputStream(fGrav), 2048);

            } catch (FileNotFoundException e) {
                Log.e("File", "Failed to create output streams");
                Toast.makeText(view.getContext(), "Failed to create output streams",
                        Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                return;
            }

            Log.d("store location", view.getContext().getExternalFilesDir(null).toString());

            if (isFirst) {
                isFirst = false;
            }

            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
            mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_UI);
            mSensorManager.registerListener(this, mLinearAcc, SensorManager.SENSOR_DELAY_UI);
            mSensorManager.registerListener(this, mGravity, SensorManager.SENSOR_DELAY_UI);

//            v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            v.vibrate(300);
            btn.setText("Stop");

        } else {
            try {
                outAcc.flush();
                outGyro.flush();
                outLinear.flush();
                outGrav.flush();
                outAcc.close();
                outGyro.close();
                outLinear.close();
                outGrav.close();
            } catch (Exception e) {
                Log.e("File", "Failed to close output stream");
                Toast.makeText(view.getContext(), "Failed to close output stream",
                        Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                return;
            }

            mSensorManager.unregisterListener(this);
            btn.setText("Start");
        }
    }
/*
    @Override
    protected void onResume() {
        Log.d("Resume", "Resume is called");
        super.onResume();
        if (isFirst) {
            isFirst = false;
        } else {
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        Log.d("Pause", "Pause is clicked");
        super.onPause();
        mSensorManager.unregisterListener(this);
    }
*/
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Long tsLong = System.currentTimeMillis();
        String data = tsLong.toString() + " " + Float.toString(event.values[0])
                + " " + Float.toString(event.values[1])
                + " " + Float.toString(event.values[2]) + "\n";

        try {
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    outAcc.write(data.getBytes());
                    accVal.setText(data);
                    break;
                case Sensor.TYPE_GYROSCOPE:
                    outGyro.write(data.getBytes());
                    gyroVal.setText(data);
                    break;
                case Sensor.TYPE_GRAVITY:
                    outGrav.write(data.getBytes());
                    break;
                case Sensor.TYPE_LINEAR_ACCELERATION:
                    outLinear.write(data.getBytes());
                    break;
            }
//            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
//                outAcc.write(data.getBytes());
//                accVal.setText(data);
//            } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
//                outGyro.write(data.getBytes());
//                gyroVal.setText(data);
//            } else {
//                Log.d("sensor value", data);
//            }
        } catch (Exception e) {
            Log.w("Output", "Cannot write sensor data to files");
            e.printStackTrace();
        }
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
}
