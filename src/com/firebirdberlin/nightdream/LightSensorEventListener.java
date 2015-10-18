package com.firebirdberlin.nightdream;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import de.greenrobot.event.EventBus;

public class LightSensorEventListener implements SensorEventListener {

    public int count = 0;
    private boolean pending = false;
    private float ambient_mean = 4.0f;
    private float last_value = 4.0f;
    private float last_mean_value = 4.0f;
    private SensorManager mSensorManager;
    private Sensor lightSensor = null;
    private Handler handler;
    private EventBus bus;


    public LightSensorEventListener(Context context){
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        lightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        handler = new Handler();
        bus = EventBus.getDefault();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if( event.sensor.getType() == Sensor.TYPE_LIGHT ){
            if (! pending)
                handler.postDelayed(calculateMeanValue, 10000);

            // triggers handler if no change occurs within the next 15s
            handler.removeCallbacks(sensorTimeout); // stop other instances
            handler.postDelayed(sensorTimeout, 15000);// start timer

            pending = true;
            last_value = event.values[0];
            ambient_mean += event.values[0];
            count += 1;
        }
    }

    public void register(){
        if (lightSensor == null) return;
        mSensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        handler.postDelayed(calculateMeanValue, 1000);
    }

    public void unregister(){
        handler.removeCallbacks(sensorTimeout); // stop other instances
        handler.removeCallbacks(calculateMeanValue);
        mSensorManager.unregisterListener(this);
    }

    private Runnable calculateMeanValue = new Runnable() {
        @Override
        public void run() {
            pending = false;
            if (count == 0) return; 
            ambient_mean /= (float) count;
            last_mean_value = ambient_mean;
            bus.post(new OnNewLightSensorValue(last_mean_value, count));
            count = 0;
            ambient_mean = 0.f;
        }
    };

    private Runnable sensorTimeout = new Runnable() {
        @Override
        public void run() {
            bus.post(new OnLightSensorValueTimeout(last_value));
        }
    };
}
