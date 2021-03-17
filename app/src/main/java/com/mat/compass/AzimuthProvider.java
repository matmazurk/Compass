package com.mat.compass;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.view.Surface;
import android.view.WindowManager;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class AzimuthProvider implements SensorEventListener {

    private static final int COMPASS_UPDATE_RATE_MS = 100;
    private long compassUpdateNextTimestamp;

    private WindowManager windowManager;
    private final float[] rotationMatrix = new float[9];
    private final float[] truncatedRotationVectorValue = new float[4];
    private final SensorManager sensorManager;
    private final Sensor sensor;
    private final MutableLiveData<Float> mAzimuth = new MutableLiveData<>(0f);
    private final MutableLiveData<Integer> mAccuracy = new MutableLiveData<>(0);

    AzimuthProvider(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
    }

    public LiveData<Float> getAzimuth() {
        return mAzimuth;
    }

    public LiveData<Integer> getAccuracy() {
        return mAccuracy;
    }

    public void startMeasuring(WindowManager windowManager) {
        this.windowManager = windowManager;
        sensorManager.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void stopMeasuring() {
        sensorManager.unregisterListener(this);
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    public void onSensorChanged(SensorEvent event) {
        long currentTime = SystemClock.elapsedRealtime();
        if (currentTime < compassUpdateNextTimestamp) {
            return;
        }
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            float[] rotationVectorValue = getRotationVectorFromSensorEvent(event);


            SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVectorValue);

            final int worldAxisForDeviceAxisX;
            final int worldAxisForDeviceAxisY;

            // Remap the axes as if the device screen was the instrument panel,
            // and adjust the rotation matrix for the device orientation.
            switch (windowManager.getDefaultDisplay().getRotation()) {
                case Surface.ROTATION_90:
                    worldAxisForDeviceAxisX = SensorManager.AXIS_Z;
                    worldAxisForDeviceAxisY = SensorManager.AXIS_MINUS_X;
                    break;
                case Surface.ROTATION_180:
                    worldAxisForDeviceAxisX = SensorManager.AXIS_MINUS_X;
                    worldAxisForDeviceAxisY = SensorManager.AXIS_MINUS_Z;
                    break;
                case Surface.ROTATION_270:
                    worldAxisForDeviceAxisX = SensorManager.AXIS_MINUS_Z;
                    worldAxisForDeviceAxisY = SensorManager.AXIS_X;
                    break;
                case Surface.ROTATION_0:
                default:
                    worldAxisForDeviceAxisX = SensorManager.AXIS_X;
                    worldAxisForDeviceAxisY = SensorManager.AXIS_Z;
                    break;
            }

            float[] adjustedRotationMatrix = new float[9];
            SensorManager.remapCoordinateSystem(rotationMatrix, worldAxisForDeviceAxisX,
                    worldAxisForDeviceAxisY, adjustedRotationMatrix);

            // Transform rotation matrix into azimuth/pitch/roll
            float[] orientation = new float[3];
            SensorManager.getOrientation(adjustedRotationMatrix, orientation);
//            SensorManager.getOrientation(rotationMatrix, orientation);

            // The x-axis is all we care about here.
            mAzimuth.postValue((float) Math.toDegrees(orientation[2]));
            compassUpdateNextTimestamp = currentTime + COMPASS_UPDATE_RATE_MS;

//        SensorManager.getRotationMatrixFromVector(rotationVector, rotationalReading);
//
//        SensorManager.getOrientation(rotationVector, orientationAngles);
//
//        mAzimuth.postValue(-1 * orientationAngles[0] * 180 / Math.PI);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            this.mAccuracy.postValue(accuracy);
        }
    }

    @NonNull
    private float[] getRotationVectorFromSensorEvent(@NonNull SensorEvent event) {
        if (event.values.length > 4) {
            // On some Samsung devices SensorManager.getRotationMatrixFromVector
            // appears to throw an exception if rotation vector has length > 4.
            // For the purposes of this class the first 4 values of the
            // rotation vector are sufficient (see crbug.com/335298 for details).
            // Only affects Android 4.3
            System.arraycopy(event.values, 0, truncatedRotationVectorValue, 0, 4);
            return truncatedRotationVectorValue;
        } else {
            return event.values;
        }
    }
}
