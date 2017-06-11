package com.theunio.camera;

import android.app.Activity;
import android.hardware.Camera;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

/**
 * Created by goran on 18.12.2015.
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    public static final String TAG = "CameraPreview";
    private final List<Camera.Size> mSupportedPreviewSizes;

    private SurfaceHolder mHolder;
//    private Camera mCamera;

    int screenWidth;
    int screenHeight;

    Activity context;
    private Camera.Size mPreviewSize;


    int currentCameraId;

    boolean released;

    public CameraPreview(Activity context) {
        super(context);
        CameraSingleton.getInstance(context).setCameraPreview(this);
        this.context = context;


        DisplayMetrics dm = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(dm);
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;

        mSupportedPreviewSizes = CameraSingleton.getInstance().getCamera().getParameters().getSupportedPreviewSizes();

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            CameraSingleton.getInstance().getCamera().setPreviewDisplay(holder);
            CameraSingleton.getInstance().getCamera().startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {

//        mCamera.stopPreview();
//        mCamera.setPreviewCallback(null);
//        mCamera.release();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null){
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            CameraSingleton.getInstance().getCamera().stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        CameraSingleton.getInstance().startPreview();
    }

//    private void startPreview(){
//        // start preview with new settings
//        try {
//            CameraSingleton.getInstance().getCamera().setPreviewDisplay(mHolder);
//
//            Camera.Parameters parameters = mCamera.getParameters();
//            parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
//            mCamera.setParameters(parameters);
//
//
//            if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
//            {
//                mCamera.setDisplayOrientation(90);
//            }
//
//            mCamera.startPreview();
//            inPreview = true;
//
//
//            CameraSingleton.getInstance().setInPreview(true);
//        } catch (Exception e){
//            CameraSingleton.getInstance().setInPreview(false);
//            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
//        }
//    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio=(double)h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);

        if (mSupportedPreviewSizes != null) {
            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
            CameraSingleton.getInstance().setPreviewSize(mPreviewSize);
        }
    }

//    public void switchCamera(){
//        if ( CameraSingleton.getInstance().isInPreview()) {
//            mCamera.stopPreview();
//        }
//        //NB: if you don't release the current camera before switching, you app will crash
//
//        releaseCamera();
//
//        //swap the id of the camera to be used
//        if(currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK){
//            currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
//            callback.onCameraChanged(currentCameraId);
//        }
//        else {
//            currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
//            callback.onCameraChanged(currentCameraId);
//        }
//        mCamera = Camera.open(currentCameraId);
//        //Code snippet for this method from somewhere on android developers, i forget where
//       // setCameraDisplayOrientation(CameraActivity.this, currentCameraId, camera);
//        startPreview();
//
//    }

    // release Camera for other applications
//    public void releaseCamera() {
//        // check if Camera instance exists
//        if(isReleased()){
//            return;
//        }
//        if (mCamera != null) {
//            inPreview = false;
//            // first stop preview
//            mCamera.stopPreview();
//            // then cancel its preview callback
//
//
//
//            mCamera.setPreviewCallback(null);
//            getHolder().removeCallback(this);
//            // and finally release it
//            mCamera.release();
//            // sanitize you Camera object holder
//            mCamera = null;
//            released = true;
//        }
//    }

    public boolean isReleased() {
        return released;
    }

    public void setReleased(boolean released) {
        this.released = released;
    }

//    public Camera getCamera() {
//        return mCamera;
//    }
//
//    public void setCamera(Camera mCamera) {
//        this.mCamera = mCamera;
//    }
}