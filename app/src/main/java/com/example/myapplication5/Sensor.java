package com.example.myapplication5;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class Sensor extends Service {
    SensorManager sensorManager;
    @Override
    public void onCreate(){
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy(){

    }
}
