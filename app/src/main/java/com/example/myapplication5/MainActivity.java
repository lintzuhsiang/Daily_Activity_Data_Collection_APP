package com.example.myapplication5;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
//import android.support.v7.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.Toast;


import com.vuzix.hud.actionmenu.ActionMenuActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

//public class MainActivity extends AppCompatActivity {
//public class AudioRecordTest extends AppCompatActivity{
public class MainActivity extends ActionMenuActivity {

    private SensorManager sensorManager;
    private SensorEventListener sensorEventListener;

    public static long timestamp = System.currentTimeMillis();
    public static long filesavetimestamp = System.currentTimeMillis();
    public JSONObject sensorDic;
    public ArrayList<JSONObject> mainSensorArray = new ArrayList<>();

    private Button btn;
    private File saveFile;

    private Button takePictureButton;
    private TextView textView;
    private static final String LOG_TAG = "AudioRecordTest";

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String fileName = null;
    private File outputFile = null;

//    private RecordButton recordButton = null;
    private MediaRecorder recorder = null;

    private boolean mstartRecording = true;

//    private PlayButton   playButton = null;
    private MediaPlayer player = null;
    private Handler timerHandler = new Handler();

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};

    private TextView text;

    private ImageView reddot;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                sensorDic = new JSONObject();
                try{
                    sensorDic.put("time", System.currentTimeMillis() - timestamp);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JSONObject sensorValue = new JSONObject();
                Log.d("emotion2", String.valueOf(sensorEvent.sensor.getStringType()));
                switch (sensorEvent.sensor.getStringType()) {

                    case Sensor.STRING_TYPE_ACCELEROMETER:
                        try {
                            sensorValue.put("accelerometer", sensorEvent.values[0]);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case Sensor.STRING_TYPE_MAGNETIC_FIELD:
                        try {
                            sensorValue.put("magnetic", sensorEvent.values[0]);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case Sensor.STRING_TYPE_GYROSCOPE:
                        try {
                            sensorValue.put("gyroscope", sensorEvent.values[0]);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case Sensor.STRING_TYPE_LIGHT:
                        try {
                            sensorValue.put("light", sensorEvent.values[0]);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case Sensor.STRING_TYPE_PRESSURE:
                        try {
                            sensorValue.put("pressure", sensorEvent.values[0]);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;

                }
                try {
                    sensorDic.put("value", sensorValue);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d("emotion", String.valueOf(sensorDic));
                mainSensorArray.add(sensorDic);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
                Log.d("emotion", sensor.toString());

            }
        };


        btn = findViewById(R.id.button);
        assert btn != null;

        reddot = findViewById(R.id.imageView2);
        int imageResource = getResources().getIdentifier("@drawable/reddot","drawable",getPackageName());
        reddot.setImageResource(imageResource);

        Toast.makeText(MainActivity.this, "Tap to start App.", Toast.LENGTH_LONG).show();

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRecord(mstartRecording);
                if (mstartRecording) {
                    Toast.makeText(MainActivity.this, "Now Start Recording.", Toast.LENGTH_LONG).show();
                    reddot.setVisibility(View.VISIBLE);

                } else {
                    Toast.makeText(MainActivity.this, "Now Stop Recording.", Toast.LENGTH_LONG).show();
                    reddot.setVisibility(View.INVISIBLE);
                }
                mstartRecording = !mstartRecording;
            }

        });



    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();

    }

    private void onRecord(boolean start) {
        if (start) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss",Locale.getDefault());

            fileName = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath()+"/"+simpleDateFormat.format(new Date());
            startRecording(fileName);
            startSensor(fileName);

        } else {
            stopRecording();
            stopSensor();
        }
    }




    private void startRecording(String fileName) {

        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);

        recorder.setOutputFile(fileName+".3gp");
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        } catch (IOException e) {
        }

        recorder.start();

    }


    private void startSensor(String fileName){
        // register this class as a listener for the gyroscope sensor
        sensorManager.registerListener(sensorEventListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                SensorManager.SENSOR_DELAY_NORMAL);

        // register this class as a listener for the gyroscope sensor
        sensorManager.registerListener(sensorEventListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);


        // register this class as a listener for the gyroscope sensor
        sensorManager.registerListener(sensorEventListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_NORMAL);

        // register this class as a listener for the gyroscope sensor
        sensorManager.registerListener(sensorEventListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT),
                SensorManager.SENSOR_DELAY_NORMAL);

        // register this class as a listener for the gyroscope sensor
        sensorManager.registerListener(sensorEventListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE),
                SensorManager.SENSOR_DELAY_NORMAL);

        if(System.currentTimeMillis()-filesavetimestamp > 3000){  //save every 3 seconds
            saveFile = new File(fileName + ".json");
            Log.d("startSensor",saveFile.getAbsolutePath());
            if(isExternalStorageWritable()) {
                writeFileToExternalStorage();
            }
            filesavetimestamp = System.currentTimeMillis();
        }
    }

    private void stopSensor(){
        sensorManager.unregisterListener(sensorEventListener);

    }

    private void stopRecording() {
        recorder.stop();
        recorder.release();
        recorder = null;
    }




    private boolean isExternalStorageWritable() {

        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());

    }

    private void writeFileToExternalStorage() {

        try {

            saveFile.createNewFile();

            FileOutputStream outputStream = new FileOutputStream(saveFile);
            outputStream.write(mainSensorArray.toString().getBytes());

            outputStream.flush();
            outputStream.close();
            Log.d("emotion","File saved");
        } catch (Exception ex) {

            Log.e("emotion", "Error writing to file: " + ex.toString());

        }

    }


    @Override
    public void onStop() {
        super.onStop();
        if (recorder != null) {
            recorder.release();
            recorder = null;

        }


        if (player != null) {
            player.release();
            player = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // unregister listener
        sensorManager.unregisterListener(sensorEventListener);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //KNOWN SENSOR Available


    }





}
