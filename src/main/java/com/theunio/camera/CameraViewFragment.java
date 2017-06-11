package com.theunio.camera;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.theunio.camera.callbacks.CameraPreviewCallback;
import com.theunio.camera.callbacks.CameraResultCallback;
import com.theunio.camera.callbacks.CameraUICallback;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


/**
 * Created by goran on 02.12.2015.
 */
public class CameraViewFragment extends Fragment implements CameraUICallback, CameraPreviewCallback {

    public static final String TAG = "CameraView";

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    private static final String EXTRA_CURRENT_CAMERA_ID = "cameraId";

    FrameLayout previewContainer;

    View btnSwitch;
    View btnFlash;
    View btnCapture;

    View layoutTimer;
    TextView txtTimer;
    TextView txtSendTo;

    private CameraPreview mPreview;
    private MediaRecorder mMediaRecorder;

    CameraResultCallback cameraCallback;

    File videoCaptureFile = null;

    boolean isRecording = false;

    TaskScheduler timer;
    int mSecondsElapsed = 0;
    private static int currentCameraId = -1;

    String name;
    //----------------------------------------------------------------------------------------------
    //  CAMERA API v1 VARIABLES & CALLBACKS
    //----------------------------------------------------------------------------------------------


    public static CameraViewFragment newInstance(String name, CameraResultCallback callback) {
        CameraViewFragment cameraFragment = new CameraViewFragment();
        Bundle args = new Bundle();
        args.putString(CameraActivity.EXTRA_NAME, name);
        cameraFragment.setArguments(args);
        cameraFragment.setCameraCallback(callback);
        return cameraFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.camera_layout_with_container, container, false);
        setUIReferences(rootView);

        return rootView;
    }

    private void setUiProperties(){
        hideTimer();


        if(Camera.getNumberOfCameras() <= 1){
            btnSwitch.setVisibility(View.INVISIBLE);
        }
        else{
            btnSwitch.setVisibility(View.VISIBLE);
        }

        if(name!=null){
            txtSendTo.setText("Send to \"" + name.trim() + "\"");
        }

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(getArguments()!=null){
            if(getArguments().containsKey(CameraActivity.EXTRA_NAME)){
                this.name = getArguments().getString(CameraActivity.EXTRA_NAME);
            }
        }

        if (savedInstanceState != null) {
          if(savedInstanceState.containsKey(EXTRA_CURRENT_CAMERA_ID)){
              this.currentCameraId = savedInstanceState.getInt(EXTRA_CURRENT_CAMERA_ID);
          }

            if(savedInstanceState.containsKey(CameraActivity.EXTRA_NAME)){
                this.name = savedInstanceState.getString(CameraActivity.EXTRA_NAME);
            }
        }

        setUiProperties();
        handleEvents();

    }

    private void setUIReferences(View view) {
        previewContainer = (FrameLayout) view.findViewById(R.id.camera_preview);
        btnSwitch = view.findViewById(R.id.btnCameraSwitch);
        btnFlash = view.findViewById(R.id.btnCameraFlashlight);
        btnCapture = view.findViewById(R.id.btnCapture);

        layoutTimer  = view.findViewById(R.id.layoutTimer);
        txtTimer  = (TextView) view.findViewById(R.id.txtTimer);
        txtSendTo = (TextView) view.findViewById(R.id.txtSendTo);
    }

    private void handleEvents(){
        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCaptureClicked();
            }
        });

        btnCapture.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onCaptureLongClicked();
                return false;
            }
        });

        btnCapture.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        break;
                    case MotionEvent.ACTION_UP:

                        btnCapture.setSelected(false);
                        stopVideoRecording();

                        break;
                }
                return false;
            }

        });

        btnFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFlashClicked();
            }
        });

        btnSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSwitchClicked();
            }
        });
    }

    //----------------------------------------------------------------------------------------------
    //  CAMERA EVENT HANDLERS
    //----------------------------------------------------------------------------------------------

    /** A safe way to get an instance of the Camera object. */
//    public  Camera getCameraInstance(){
//        Camera c = null;
//        try {
//            if(this.currentCameraId!=-1){
//                c = Camera.open(this.currentCameraId);
//            }
//            else {
//                c = Camera.open(); // attempt to get a Camera instance
//            }
//
//            mPreview.setReleased(false);
//        }
//        catch (Exception e){
//        Log.v("CAMERA_EXC", "CAMERA IS NOT AVAILABLE");
//            // Camera is not available (in use or does not exist)
//        }
//        return c; // returns null if camera is unavailable
//    }

    //----------------------------------------------------------------------------------------------
    // PICTURE CALLBACK
    //----------------------------------------------------------------------------------------------

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {


            int angleToRotate = getRoatationAngle(getActivity(), CameraSingleton.getInstance().getCurrentCameraId());

            if(CameraSingleton.getInstance().getCurrentCameraId()== Camera.CameraInfo.CAMERA_FACING_FRONT) {
                // Solve image inverting problem
                Log.v("INVERTING_LOGIC", String.valueOf(angleToRotate));
                if(angleToRotate==90) {
                    angleToRotate = angleToRotate + 180;
                }
            }

            Bitmap orignalImage = BitmapFactory.decodeByteArray(data, 0, data.length);
            Bitmap rotatedBitmapImage = rotate(orignalImage, angleToRotate);

            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null){
                Toast.makeText(getActivity(), "File error while taking image", Toast.LENGTH_LONG).show();
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);

                rotatedBitmapImage.compress(Bitmap.CompressFormat.PNG, 85, fos);

                fos.flush();
                fos.close();

//                fos.write(data);
//                fos.close();

                releaseMediaRecorder();       // if you are using MediaRecorder, release it first
                //releaseCamera();              // release the camera immediately on pause event
                CameraSingleton.getInstance(getActivity()).releaseCamera();
                cameraCallback.onPictureResult(pictureFile);
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
        }
    };

    public static int getRoatationAngle(Activity mContext, int cameraId) {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = mContext.getWindowManager().getDefaultDisplay().getRotation();
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
        return result;
    }

    //----------------------------------------------------------------------------------------------
    // FRAGMENT LIFECYCLE METHODS
    //----------------------------------------------------------------------------------------------


    public static Bitmap rotate(Bitmap bitmap, int degree) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Matrix mtx = new Matrix();
        mtx.postRotate(degree);

        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }

    private void releaseMediaRecorder(){
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            CameraSingleton.getInstance(getActivity()).getCamera().lock();           // lock camera for later use
        }
    }

    //----------------------------------------------------------------------------------------------
    //  CAMERA OUTPUT FILES
    //----------------------------------------------------------------------------------------------


    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d(TAG, "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    public CameraResultCallback getCameraCallback() {
        return cameraCallback;
    }

    public void setCameraCallback(CameraResultCallback cameraCallback) {
        this.cameraCallback = cameraCallback;
    }

    public void onPause() {
        super.onPause();

//        if (mCamera != null) {
//
//            mCamera.setPreviewCallback(null);
//            mPreview.getHolder().removeCallback(mPreview);
//            mCamera.release();
//        }

       // if(mPreview!=null && mPreview.isReleased()==false){
            CameraSingleton.getInstance(getActivity()).releaseCamera();
        //}
    }


    @Override
    public void onResume()
    {
        super.onResume();
        CameraSingleton.getInstance(getActivity()).setScreenOrientation(getActivity().getResources().getConfiguration().orientation);
        try
        {
            CameraSingleton.getInstance(getActivity()).getCamera();
            addPreview();
        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    private void addPreview(){
        mPreview = new CameraPreview(getActivity());
        previewContainer.addView(mPreview);
    }

    @Override
    public void onCaptureClicked() {
        CameraSingleton.getInstance(getActivity()).getCamera().takePicture(null, null, mPicture);
    }

    @Override
    public void onCaptureLongClicked() {

            if(btnCapture.isSelected()==false) {
                btnCapture.setSelected(true);
                captureVideo();
            }
            else{
                Toast.makeText(getActivity(), "Recording in process", Toast.LENGTH_LONG).show();
            }

    }

    @Override
    public void onFlashClicked() {

    }

    @Override
    public void onSwitchClicked() {
        CameraSingleton.getInstance(getActivity()).switchCamera();
    }

    public void captureVideo(){
        if (isRecording) {
            stopVideoRecording();
        } else {
            // initialize video camera
            if (prepareVideoRecorder()) {
                // Camera is available and unlocked, MediaRecorder is prepared,
                // now you can start recording
                mMediaRecorder.start();

                // inform the user that recording has started
                //setCaptureButtonText("Stop");
                startTimer();
                isRecording = true;
            } else {
                // prepare didn't work, release the camera
                releaseMediaRecorder();
                // inform user
            }
        }
    }

    private void stopVideoRecording(){
        stopTimer();

        if(mMediaRecorder==null){
            return;
        }
        // stop recording and release camera
        mMediaRecorder.stop();  // stop the recording
        releaseMediaRecorder(); // release the MediaRecorder object
        CameraSingleton.getInstance(getActivity()).getCamera().lock();         // take camera access back from MediaRecorder

        // inform the user that recording has stopped
        //setCaptureButtonText("Capture");

        isRecording = false;

        if(cameraCallback!=null && videoCaptureFile!=null) {
            cameraCallback.onVideoResult(videoCaptureFile);
        }

    }

    private void stopRecording() {
        if (isRecording) {
            // stop recording and release camera
            mMediaRecorder.stop();  // stop the recording
            releaseMediaRecorder();
            isRecording = false;
        }
    }

    private boolean prepareVideoRecorder(){

        if(!CameraSingleton.getInstance(getActivity()).hasCamera()){
            Toast.makeText(getActivity(), "Camera not prepared", Toast.LENGTH_LONG).show();
            return false;
        }

        //camera = getCameraInstance();
        mMediaRecorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        CameraSingleton.getInstance(getActivity()).getCamera().unlock();
        mMediaRecorder.setCamera(CameraSingleton.getInstance(getActivity()).getCamera());

        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        // Step 4: Set output file
        this.videoCaptureFile = getOutputMediaFile(MEDIA_TYPE_VIDEO);
        mMediaRecorder.setOutputFile(videoCaptureFile.toString());

        // Step 5: Set the preview output
        int angleToRotate = getRoatationAngle(getActivity(), CameraSingleton.getInstance(getActivity()).getCurrentCameraId());

        if(CameraSingleton.getInstance().getCurrentCameraId()== Camera.CameraInfo.CAMERA_FACING_FRONT) {
            // Solve image inverting problem
            Log.v("INVERTING_LOGIC", String.valueOf(angleToRotate));
            if(angleToRotate==90) {
                angleToRotate = angleToRotate + 180;
            }
        }


        mMediaRecorder.setOrientationHint(angleToRotate);
        mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());

        // Step 6: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    private void resetTimer(){
        mSecondsElapsed = 0;
        txtTimer.setText("00:00");
    }

    private void showTimer(){

        layoutTimer.setVisibility(View.VISIBLE);
    }

    private void hideTimer(){
        layoutTimer.setVisibility(View.GONE);
    }

    private void startTimer(){
        resetTimer();
        showTimer();
        if(timer==null) {
            timer = new TaskScheduler();
        }
        if(videoTimerRunnable==null) {
            videoTimerRunnable = new VideoTimerRunnable();
        }
        timer.scheduleAtFixedRate(videoTimerRunnable,1000);

    }

    private VideoTimerRunnable videoTimerRunnable;

    @Override
    public void onCameraChanged(int cameraId) {
        this.currentCameraId = cameraId;
    }

    private class VideoTimerRunnable implements Runnable{

        @Override
        public void run() {
            TimeZone tz = TimeZone.getTimeZone("UTC");
            SimpleDateFormat df = new SimpleDateFormat("mm:ss");
            df.setTimeZone(tz);
            String time = df.format(new Date(mSecondsElapsed*1000));
            txtTimer.setText(time);

            mSecondsElapsed += 1;
        }
    }

    private void stopTimer(){
        if(timer!=null && videoTimerRunnable!=null){
            timer.stop(videoTimerRunnable);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //Save the fragment's state here
        if(this.currentCameraId!=-1) {
            outState.putInt(EXTRA_CURRENT_CAMERA_ID, this.currentCameraId);
        }

    }

}
