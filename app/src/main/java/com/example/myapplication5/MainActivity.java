package com.example.myapplication5;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

//public class MainActivity extends AppCompatActivity {
//public class AudioRecordTest extends AppCompatActivity{
public class MainActivity extends ActionMenuActivity {
    public SimpleDateFormat DateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());

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
    private static final int REQUEST_RECORD_VIDEO_PERMISSION = 300;
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String fileName = null;
    private File outputFile = null;

    //    private RecordButton recordButton = null;
    private static final String TAG = "Thesis";
    private static final String ACTION = "TEST_";
    private boolean mstartRecording = false;

    //    private PlayButton   playButton = null;
    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};

    private ImageView reddot;
    private String cameraId;
    private Size imageDimension;
    private TextureView textureView;
    private CaptureRequest.Builder previewRequestBuilder;
    private Surface recorderSurface;
    private Handler cameraHandler;
    private CameraCaptureSession cameraPrewSession;
    private MediaRecorder recorder = new MediaRecorder();

    private HandlerThread backgroundThread;
    private CameraDevice cameraDevice;
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());

    private Handler timerHandler = new Handler();
    private Handler handler = new Handler();
    private Runnable runnableCode = new Runnable() {
        @Override
        public void run() {

        }
    };

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                sensorDic = new JSONObject();
                try {
                    sensorDic.put("time", System.currentTimeMillis() - timestamp);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JSONObject sensorValue = new JSONObject();
                Log.d(TAG, "sensor get stringType" + sensorEvent.sensor.getStringType());
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
                Log.d(TAG, "SensorDic " + String.valueOf(sensorDic));
                mainSensorArray.add(sensorDic);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
                Log.d(TAG, "sensor " + sensor.toString());

            }
        };

        btn = findViewById(R.id.button);
        assert btn != null;
        textureView = findViewById(R.id.textureview);
        reddot = findViewById(R.id.imageView2);
        int imageResource = getResources().getIdentifier("@drawable/reddot", "drawable", getPackageName());
        reddot.setImageResource(imageResource);


        Toast.makeText(MainActivity.this, "Tap to start App.", Toast.LENGTH_LONG).show();

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "mstartRecording " + mstartRecording);
//                onRecord(mstartRecording);

                if (mstartRecording) {
                    reddot.setVisibility(View.INVISIBLE);
                    stopRecording();
                    stopSensor();
                    handler.removeCallbacks(runnableCode);

                } else {
                    reddot.setVisibility(View.VISIBLE);
                    setUpRecord();
                    startRecording();
                    initSensor();
                    startSensor();
                }

            }

        });


    }

    private void onRecord(boolean isRecording) {
        if (isRecording) {
            stopRecording();
            stopSensor();
//            Toast.makeText(MainActivity.this, "Now Stop Recording.", Toast.LENGTH_SHORT).show();
        } else {
//            fileName = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/"+ACTION + simpleDateFormat.format(new Date());
            setUpRecord();
            startRecording();
            startSensor();
//            Toast.makeText(MainActivity.this, "Now Start Recording.", Toast.LENGTH_SHORT).show();
        }
    }


    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            Log.e("camera", "onOpened");
            cameraDevice = camera;
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            Log.e("camera", "onClose");
            cameraDevice.close();
            cameraDevice = null;

        }

        @Override
        public void onError(CameraDevice camera, int error) {
            Log.e("camera", "onERROR" + error);

            cameraDevice.close();
            cameraDevice = null;
        }
    };

    private Size chooseVideoSize(Size[] choices) {
        for (Size size : choices) {
            if (size.getWidth() == size.getHeight() * 4 / 3 && size.getWidth() <= 1080) {
                return size;
            }
        }
        return choices[choices.length - 1];
    }

    private Size chooseOptimalSize(Size[] choices, int width, int height, Size aspectRatio) {
        List<Size> bigEnough = new ArrayList<Size>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getHeight() == option.getWidth() * h / w &&
                    option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }

        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new Comparator<Size>() {
                @Override
                public int compare(Size lhs, Size rhs) {
                    return Long.signum(lhs.getWidth() * lhs.getHeight() - rhs.getWidth() * rhs.getHeight());
                }
            });
        } else {
            return choices[0];
        }
    }

    private void setUpRecord() {
        if (recorder == null) {
            recorder = new MediaRecorder();
        }
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);

        CamcorderProfile cpHigh = CamcorderProfile.get(CamcorderProfile.QUALITY_LOW);
        recorder.setProfile(cpHigh);
        String fileName = getFilePath();
        recorder.setOutputFile(fileName + ".3gp");

        try {
            recorder.prepare();
            Log.d(TAG, "recorder prepared");
        } catch (IOException e) {
        }
    }

    private void startRecording() {
        SurfaceTexture texture = textureView.getSurfaceTexture();
        assert texture != null;
        texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());

        try {
            previewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        List<Surface> surfaces = new ArrayList<>(1);
        Surface previewSurface = new Surface(texture);
        surfaces.add(previewSurface);
        recorderSurface = recorder.getSurface();

        surfaces.add(recorderSurface);
        previewRequestBuilder.addTarget(recorderSurface);
        previewRequestBuilder.addTarget(previewSurface);
        try {
            cameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {
                //            cameraDevice.createCaptureSession((List<Surface>) recorderSurface, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                    cameraPrewSession = cameraCaptureSession;
                    try {
                        cameraPrewSession.setRepeatingRequest(previewRequestBuilder.build(), null, null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
//
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mstartRecording = true;
                            recorder.start();


                        }
                    });
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            }, null);//cameraHandler
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    private void stopRecording() {
        mstartRecording = false;
        if (cameraDevice != null && cameraPrewSession != null) {
            try {
                cameraPrewSession.stopRepeating();
//                cameraPrewSession.abortCaptures();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (null != recorder) {
                    recorder.stop();
                    recorder.reset();    // set state to idle
                    recorder.release();
                    recorder = null;
                }
            }
        };
        timer.schedule(timerTask, 30);

    }

    private void openCamera() {
        Log.d(TAG, "openCamera");
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            assert manager != null;
            cameraId = manager.getCameraIdList()[0];

            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            Size videoSize = chooseVideoSize(map.getOutputSizes(MediaRecorder.class));

            imageDimension = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), textureView.getWidth(), textureView.getHeight(), videoSize);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                return;
            }
            manager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void closeCamera() {
        closePreviewSession();
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }


    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            // Transform you image captured size according to the surface width and height
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };

    private String getFilePath() {
        fileName = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/" + ACTION + simpleDateFormat.format(new Date());
        Log.i(TAG, "path " + fileName);
        return fileName;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        closeCamera();
        sensorManager.unregisterListener(sensorEventListener);
    }


    @Override
    protected void onPause() {
        super.onPause();
        closeCamera();
        Log.d(TAG, "onPause");
//        stopThread();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
//        startThread();
        if (textureView.isAvailable()) {
            Log.d(TAG, "onResuem textureView is Available");
            openCamera();
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }

    }

    private void closePreviewSession() {
        if (cameraPrewSession != null) {
            cameraPrewSession.close();
            cameraPrewSession = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("ee", String.valueOf(requestCode));
        Log.d("ee", String.valueOf(PackageManager.PERMISSION_GRANTED));

        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
            case REQUEST_RECORD_VIDEO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted) finish();

    }


    static final int REQUEST_VIDEO_CAPTURE = 10;

    private void recordVideoIntent() {
        Intent intentRecord = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intentRecord.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
        if (intentRecord.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intentRecord, REQUEST_VIDEO_CAPTURE);
        }

    }
//    private String strVideoPath = "";
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode,Intent data){
//        super.onActivityResult(requestCode,resultCode,data);
//        if (resultCode==RESULT_OK){
//            Uri uriVideo = data.getData();
//            Cursor cursor = this.getContentResolver().query(uriVideo,null,null,null,null);
//            if(cursor.moveToNext()){
//                strVideoPath = cursor.getString(cursor.getColumnIndex("_data"));
//                Toast.makeText(this, strVideoPath, Toast.LENGTH_SHORT).show();
//            }
//        }
//    }

    public void startThread() {
        backgroundThread = new HandlerThread("Camera2");
        backgroundThread.start();
        cameraHandler = new Handler(backgroundThread.getLooper());
    }

    private void stopThread() {
        backgroundThread.quitSafely();
        try {
            backgroundThread.join();
            backgroundThread = null;
            cameraHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
            Log.d("emotion", "File saved");
        } catch (Exception ex) {

            Log.e("emotion", "Error writing to file: " + ex.toString());

        }

    }

    private void initSensor() {
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

    }

    private void startSensor() {

        runnableCode = new Runnable() {
            @Override
            public void run() {
                Log.d("Runnable","RUN");
                String fileName = getFilePath();
                saveFile = new File(fileName + ".json");
                if (isExternalStorageWritable()) {
                    writeFileToExternalStorage();
                }
//            filesavetimestamp = System.currentTimeMillis();
                handler.postDelayed(this, 3000);
            }
        };
        handler.post(runnableCode);



        if (System.currentTimeMillis() - filesavetimestamp > 3000) {  //save every 3 seconds
//            Log.d("startSensor", saveFile.getAbsolutePath());
            if (isExternalStorageWritable()) {
                writeFileToExternalStorage();
            }
            filesavetimestamp = System.currentTimeMillis();
        }
    }

    private void stopSensor() {
        sensorManager.unregisterListener(sensorEventListener);
    }
}
