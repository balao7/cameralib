package com.theunio.camera;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.theunio.camera.callbacks.CameraResultCallback;
import com.theunio.camera.callbacks.CapturedMediaCallback;
import com.theunio.camera.fragments.BaseResultFragment;
import com.theunio.camera.fragments.ImageResultFragment;
import com.theunio.camera.fragments.VideoResultFragment;

import java.io.File;

public class CameraActivity extends Activity implements CameraResultCallback, CameraConsts, CapturedMediaCallback {

    private static final String EXTRA_SCREEN = "screen";
    public static final String EXTRA_CAPTION = "caption";
    public static final String EXTRA_FILE = "file";
    public static final String EXTRA_TYPE = "type";
    public static final String EXTRA_NAME = "name";

    private static final int SCREEN_CAMERA = 0;
    private static final int SCREEN_IMAGE_RESULT = 1;
    private static final int SCREEN_VIDEO_RESULT = 2;

    private static final String TAG_CAMERA_FRAGMENT = "cameraFragment";
    private static final String TAG_RESULT_FRAGMENT = "resultFragment";

    // CameraView cameraView;
    FrameLayout container;

    int currentScreen = SCREEN_CAMERA;
    File captureFile;

    BaseResultFragment resultFragment;

    String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CameraSingleton.getInstance(this).loadDefaultCameraId();

        View decorView = getWindow().getDecorView();
// Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);


        setContentView(R.layout.activity_camera);

        if(getIntent().getExtras()!=null){
            name = getIntent().getExtras().getString(EXTRA_NAME, null);
        }

         extractBundle(savedInstanceState);

        container = (FrameLayout) findViewById(android.R.id.content);

        if (currentScreen == SCREEN_CAMERA) {
            showCameraFragment();
        } else if (currentScreen == SCREEN_IMAGE_RESULT) {
            onPictureResult(captureFile);
        } else if (currentScreen == SCREEN_VIDEO_RESULT) {
            onVideoResult(captureFile);
        }
    }

    private void extractBundle(Bundle bundle) {
        if (bundle != null) {
            if (bundle.containsKey(EXTRA_SCREEN)) {
                this.currentScreen = bundle.getInt(EXTRA_SCREEN, SCREEN_CAMERA);
            }

            if (bundle.containsKey(EXTRA_FILE)) {
                this.captureFile = (File) bundle.getSerializable(EXTRA_FILE);
            }

            if (bundle.containsKey(EXTRA_NAME)) {
                this.name = bundle.getString(EXTRA_NAME);
            }
        }
    }

    @Override
    public void onCameraCancel() {
        Log.v("Result", "OK");
    }

    @Override
    public void onPictureResult(File file) {
        Log.v("Result", "OK");
        captureFile = file;
        currentScreen = SCREEN_IMAGE_RESULT;
        showResultFragment(ImageResultFragment.newInstance(file, name, this));
    }

    @Override
    public void onVideoResult(File file) {
        Log.v("Result", "OK");
        captureFile = file;
        currentScreen = SCREEN_VIDEO_RESULT;
        showResultFragment(VideoResultFragment.newInstance(file, name, this));

    }

    private void showCameraFragment() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(android.R.id.content, CameraViewFragment.newInstance(name, this), TAG_CAMERA_FRAGMENT).commit();
    }

    private void showResultFragment(BaseResultFragment fragment) {
        this.resultFragment = fragment;
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(android.R.id.content, fragment, TAG_RESULT_FRAGMENT).commit();

    }

    public CameraResultCallback getCameraResultCallback() {
        return this;
    }

    private void restartActivity() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    @Override
    public void cancel() {
        currentScreen = SCREEN_CAMERA;
        showCameraFragment();
    }

    @Override
    public void save(File file, int type) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_FILE, captureFile);
        intent.putExtra(EXTRA_TYPE, type);
        if (resultFragment != null) {
            intent.putExtra(EXTRA_CAPTION, resultFragment.getCaption());
        }
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_SCREEN, currentScreen);
        outState.putString(EXTRA_NAME, name);
        if (captureFile != null) {
            outState.putSerializable(EXTRA_FILE, captureFile);
        }
    }


}
