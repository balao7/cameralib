package com.theunio.camera;

import android.content.Context;
import android.content.pm.PackageManager;

/**
 * Created by goran on 02.12.2015.
 */
public class CameraHelper {

    /** Check if this device has a camera */
    public static boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

}
