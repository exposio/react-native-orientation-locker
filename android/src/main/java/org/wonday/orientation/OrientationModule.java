//
//  react-native-orientation-locker
//
//
//  Created by Wonday on 17/5/12.
//  Copyright (c) wonday.org All rights reserved.
//

package org.wonday.orientation;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.view.OrientationEventListener;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;
import android.util.DisplayMetrics;
import android.util.SparseIntArray;
import android.hardware.SensorManager;
import android.util.Log;

import com.facebook.common.logging.FLog;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.ReactConstants;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class OrientationModule extends ReactContextBaseJavaModule implements LifecycleEventListener{

    final BroadcastReceiver mReceiver;
    final OrientationEventListener mOrientationListener;
    final ReactApplicationContext ctx;
    private boolean isLocked = false;
    private String lastOrientationValue = "";
    private String lastDeviceOrientationValue = "";

 /**
     * Conversion from screen rotation to JPEG orientation.
     */
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }


    public OrientationModule(ReactApplicationContext reactContext) {
        super(reactContext);
        ctx = reactContext;

        mOrientationListener = new OrientationEventListener(reactContext, SensorManager.SENSOR_DELAY_UI) {

            @Override
            public void onOrientationChanged(int orientation) {
                FLog.d(ReactConstants.TAG,"DeviceOrientation changed to " + orientation);

                String deviceOrientationValue = getCurrentOrientation();

                if (!lastDeviceOrientationValue.equals(deviceOrientationValue)) {

                    lastDeviceOrientationValue = deviceOrientationValue;

                    WritableMap params = Arguments.createMap();
                    params.putString("deviceOrientation", deviceOrientationValue);
                    if (ctx.hasActiveCatalystInstance()) {
                        ctx
                        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                        .emit("deviceOrientationDidChange", params);
                    }
                }

                return;
            }
        };

        if (mOrientationListener.canDetectOrientation()) {
           FLog.d(ReactConstants.TAG, "orientation detect enabled.");
           mOrientationListener.enable();
        } else {
           FLog.d(ReactConstants.TAG, "orientation detect disabled.");
           mOrientationListener.disable();
        }

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String orientationValue = getCurrentOrientation();
                lastOrientationValue = orientationValue;

                FLog.d(ReactConstants.TAG,"Orientation changed to " + orientationValue);

                WritableMap params = Arguments.createMap();
                params.putString("orientation", orientationValue);
                if (ctx.hasActiveCatalystInstance()) {
                    ctx
                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit("orientationDidChange", params);
                }

            }
        };

        ctx.addLifecycleEventListener(this);
    }

    @Override
    public String getName() {
        return "Orientation";
    }

    private String getCurrentOrientation() {
        int deviceRotation = ((WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        int orientation = (360 - ORIENTATIONS.get(deviceRotation)) % 360;

        if (orientation == 90) {
            return "LANDSCAPE-LEFT";
        } else if (orientation == 270) {
            return "LANDSCAPE-RIGHT";
        }

        return "PORTRAIT";
    }

    @ReactMethod
    public void getOrientation(Callback callback) {
        String orientation = getCurrentOrientation();
        callback.invoke(orientation);
    }

    @ReactMethod
    public void getDeviceOrientation(Callback callback) {
        callback.invoke(lastDeviceOrientationValue);
    }

    @ReactMethod
    public void lockToPortrait() {
        final Activity activity = getCurrentActivity();
        if (activity == null) return;
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        isLocked = true;

        // force send an UI orientation event
        lastOrientationValue = "PORTRAIT";
        WritableMap params = Arguments.createMap();
        params.putString("orientation", lastOrientationValue);
        if (ctx.hasActiveCatalystInstance()) {
            ctx
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
            .emit("orientationDidChange", params);
        }

        // send a locked event
        WritableMap lockParams = Arguments.createMap();
        lockParams.putString("orientation", lastOrientationValue);
        if (ctx.hasActiveCatalystInstance()) {
            ctx
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
            .emit("lockDidChange", lockParams);
        }
    }

    @ReactMethod
    public void lockToPortraitUpsideDown() {
        final Activity activity = getCurrentActivity();
        if (activity == null) return;
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
        isLocked = true;

        // force send an UI orientation event
        lastOrientationValue = "PORTRAIT-UPSIDEDOWN";
        WritableMap params = Arguments.createMap();
        params.putString("orientation", lastOrientationValue);
        if (ctx.hasActiveCatalystInstance()) {
            ctx
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
            .emit("orientationDidChange", params);
        }

        // send a locked event
        WritableMap lockParams = Arguments.createMap();
        lockParams.putString("orientation", lastOrientationValue);
        if (ctx.hasActiveCatalystInstance()) {
            ctx
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
            .emit("lockDidChange", lockParams);
        }
    }

    @ReactMethod
    public void lockToLandscape() {
        final Activity activity = getCurrentActivity();
        if (activity == null) return;
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        isLocked = true;

        // force send an UI orientation event
        lastOrientationValue = "LANDSCAPE-LEFT";
        WritableMap params = Arguments.createMap();
        params.putString("orientation", lastOrientationValue);
        if (ctx.hasActiveCatalystInstance()) {
            ctx
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
            .emit("orientationDidChange", params);
        }

        // send a locked event
        WritableMap lockParams = Arguments.createMap();
        lockParams.putString("orientation", lastOrientationValue);
        if (ctx.hasActiveCatalystInstance()) {
            ctx
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
            .emit("lockDidChange", lockParams);
        }
    }

    @ReactMethod
    public void lockToLandscapeLeft() {
        final Activity activity = getCurrentActivity();
        if (activity == null) return;
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        isLocked = true;

        // force send an UI orientation event
        lastOrientationValue = "LANDSCAPE-LEFT";
        WritableMap params = Arguments.createMap();
        params.putString("orientation", lastOrientationValue);
        if (ctx.hasActiveCatalystInstance()) {
            ctx
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
            .emit("orientationDidChange", params);
        }

        // send a locked event
        WritableMap lockParams = Arguments.createMap();
        lockParams.putString("orientation", lastOrientationValue);
        if (ctx.hasActiveCatalystInstance()) {
            ctx
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
            .emit("lockDidChange", lockParams);
        }
    }

    @ReactMethod
    public void lockToLandscapeRight() {
        final Activity activity = getCurrentActivity();
        if (activity == null) return;
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
        isLocked = true;

        // force send an UI orientation event
        lastOrientationValue = "LANDSCAPE-RIGHT";
        WritableMap params = Arguments.createMap();
        params.putString("orientation", lastOrientationValue);
        if (ctx.hasActiveCatalystInstance()) {
            ctx
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
            .emit("orientationDidChange", params);
        }

        // send a locked event
        WritableMap lockParams = Arguments.createMap();
        lockParams.putString("orientation", lastOrientationValue);
        if (ctx.hasActiveCatalystInstance()) {
            ctx
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
            .emit("lockDidChange", lockParams);
        }
    }

    @ReactMethod
    public void unlockAllOrientations() {

        final Activity activity = getCurrentActivity();
        if (activity == null) return;
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        isLocked = false;

        //force send an UI orientation event when unlock
        lastOrientationValue = lastDeviceOrientationValue;
        WritableMap params = Arguments.createMap();
        params.putString("orientation", lastOrientationValue);
        if (ctx.hasActiveCatalystInstance()) {
            ctx
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
            .emit("orientationDidChange", params);
        }

        // send a unlocked event
        WritableMap lockParams = Arguments.createMap();
        lockParams.putString("orientation", "UNKNOWN");
        if (ctx.hasActiveCatalystInstance()) {
            ctx
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
            .emit("lockDidChange", lockParams);
        }
    }

    @ReactMethod
    public void getAutoRotateState(Callback callback) {
      final ContentResolver resolver = ctx.getContentResolver();
      boolean rotateLock = android.provider.Settings.System.getInt(
      resolver,
      android.provider.Settings.System.ACCELEROMETER_ROTATION, 0) == 1;
      callback.invoke(rotateLock);
    }

    @Override
    public @Nullable Map<String, Object> getConstants() {
        HashMap<String, Object> constants = new HashMap<String, Object>();

        String orientation = getCurrentOrientation();
        constants.put("initialOrientation", orientation);

        return constants;
    }

    @Override
    public void onHostResume() {
        FLog.i(ReactConstants.TAG, "orientation detect enabled.");
        mOrientationListener.enable();

        final Activity activity = getCurrentActivity();
        if (activity == null) return;
        activity.registerReceiver(mReceiver, new IntentFilter("onConfigurationChanged"));
    }
    @Override
    public void onHostPause() {
        FLog.d(ReactConstants.TAG, "orientation detect disabled.");
        mOrientationListener.disable();

        final Activity activity = getCurrentActivity();
        if (activity == null) return;
        try
        {
            activity.unregisterReceiver(mReceiver);
        }
        catch (java.lang.IllegalArgumentException e) {
            FLog.w(ReactConstants.TAG, "Receiver already unregistered", e);
        }
    }

    @Override
    public void onHostDestroy() {
        FLog.d(ReactConstants.TAG, "orientation detect disabled.");
        mOrientationListener.disable();

        final Activity activity = getCurrentActivity();
        if (activity == null) return;
        try
        {
            activity.unregisterReceiver(mReceiver);
        }
        catch (java.lang.IllegalArgumentException e) {
            FLog.w(ReactConstants.TAG, "Receiver already unregistered", e);
        }
    }
}
