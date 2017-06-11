package com.theunio.camera;

import android.app.Activity;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;

/**
 * Created by goran on 13.01.2016.
 */
public class CameraSingleton
{
    private static final String TAG = "CameraSingleton";

    private static CameraSingleton _instance;
    Activity activity;
    private Camera camera;
    private int currentCameraId = -1;
    private CameraPreview cameraPreview;
    private boolean released = true;
    private boolean inPreview = false;

    private int screenOrientation;
    private Camera.Size mPreviewSize;

    public int getScreenOrientation() {
        return screenOrientation;
    }

    public void setScreenOrientation(int screenOrientation) {
        this.screenOrientation = screenOrientation;
    }

    private CameraSingleton(Activity activity)

    {
        this.activity = activity;
        loadDefaultCameraId();
    }


    private CameraSingleton()

    {
        loadDefaultCameraId();
    }

    public void loadDefaultCameraId(){
        if(isFrontCameraAvailable()){
            this.currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
        }
        else{
            this.currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        }
    }

    public synchronized static CameraSingleton getInstance()
    {
        if (_instance == null)
        {
            _instance = new CameraSingleton();
        }
        return _instance;
    }

    public synchronized static CameraSingleton getInstance(Activity activity)
    {
        if (_instance == null)
        {
            _instance = new CameraSingleton(activity);
        }
        return _instance;
    }

    public Camera getCamera(){

        if(camera!=null){
            return camera;
        }

        try {
            if(this.currentCameraId!=-1){
                camera = Camera.open(this.currentCameraId);
                if(activity!=null){

                    setCameraDisplayOrientation(currentCameraId);
                }
            }
            else {
                camera = Camera.open(); // attempt to get a Camera instance
                if(activity!=null){

                    setCameraDisplayOrientation(0);
                }
            }

            released = true;
        }
        catch (Exception e){
            released = false;
            Log.v("CAMERA_EXC", "CAMERA IS NOT AVAILABLE");
            // Camera is not available (in use or does not exist)
        }
        return camera; // returns null if camera is unavailable
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    public int getCurrentCameraId() {
        return currentCameraId;
    }

    public void setCurrentCameraId(int currentCameraId) {
        this.currentCameraId = currentCameraId;
    }

    public boolean hasCamera(){
        if(camera!=null){
            return true;
        }
        return false;
    }

    public CameraPreview getCameraPreview() {
        return cameraPreview;
    }

    public void setCameraPreview(CameraPreview cameraPreview) {
        this.cameraPreview = cameraPreview;
    }

    public void releaseCamera() {
        // check if Camera instance exists

        if (camera != null) {
           // inPreview = false;
            // first stop preview
            camera.stopPreview();
            // then cancel its preview callback



            camera.setPreviewCallback(null);
            if(getCameraPreview()!=null) {
                getCameraPreview().getHolder().removeCallback(getCameraPreview());
            }
            // and finally release it
            camera.release();
            // sanitize you Camera object holder
            camera = null;
            released = true;
        }
    }

    public boolean isInPreview() {
        return inPreview;
    }

    public void setInPreview(boolean inPreview) {
        this.inPreview = inPreview;
    }

    public void switchCamera(){
        if (CameraSingleton.getInstance().isInPreview()) {
            camera.stopPreview();
        }
        //NB: if you don't release the current camera before switching, you app will crash
        //mCamera.setPreviewCallback(null);
        //getHolder().removeCallback(this);
        //mCamera.setPreviewCallback(null);
        // mCamera.release();
        releaseCamera();

        //swap the id of the camera to be used
        if(currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK){
            currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;

        }
        else {
            currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;

        }
        camera = Camera.open(currentCameraId);
        if(activity!=null){
            setCameraDisplayOrientation(currentCameraId);
        }
        //Code snippet for this method from somewhere on android developers, i forget where
        // setCameraDisplayOrientation(CameraActivity.this, currentCameraId, camera);
        startPreview();

    }

    public  void setCameraDisplayOrientation(int cameraId)
    {

        //camera.stopPreview();
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }

        Log.v("DEGREES", String.valueOf(degrees));
        Log.v("DEGREES", "RESULT: " + String.valueOf(result));
        camera.setDisplayOrientation(result);


    }

    public void startPreview(){
        // start preview with new settings
        try {
            camera.setPreviewDisplay(getCameraPreview().getHolder());

            Camera.Parameters parameters = camera.getParameters();
            parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
            camera.setParameters(parameters);


//            if(getScreenOrientation() == Configuration.ORIENTATION_PORTRAIT)
//            {
//                camera.setDisplayOrientation(90);
//            }

            camera.startPreview();
            inPreview = true;


            CameraSingleton.getInstance().setInPreview(true);
        } catch (Exception e){
            CameraSingleton.getInstance().setInPreview(false);
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    public Camera.Size getPreviewSize() {
        return mPreviewSize;
    }

    public void setPreviewSize(Camera.Size mPreviewSize) {
        this.mPreviewSize = mPreviewSize;
    }

    private boolean isFrontCameraAvailable() {

        int cameraCount = 0;
        boolean isFrontCameraAvailable = false;
        cameraCount = Camera.getNumberOfCameras();

        while (cameraCount > 0) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            cameraCount--;
            Camera.getCameraInfo(cameraCount, cameraInfo);


            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                isFrontCameraAvailable = true;
                break;
            }

        }

        return isFrontCameraAvailable;
    }
}