package com.theunio.camera.callbacks;

import java.io.File;

/**
 * Created by goran on 02.12.2015.
 */
public interface CameraResultCallback {
    void onCameraCancel();
    void onPictureResult(File file);
    void onVideoResult(File file);
}
