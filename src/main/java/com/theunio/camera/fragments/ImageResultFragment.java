package com.theunio.camera.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.theunio.camera.ImageCropActivity;
import com.theunio.camera.R;
import com.theunio.camera.callbacks.CapturedMediaCallback;
import com.theunio.sharedresources.helper.DialogHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by goran on 17.12.2015.
 */
public class ImageResultFragment extends BaseResultFragment {

    private static final int REQUEST_IMAGE_CROP = 100;
    ImageView imgResult;
    View btnCrop;
    View btnRotate;

    DialogHelper dialogHelper;


    public static ImageResultFragment newInstance(File file, String name, CapturedMediaCallback callback) {
        ImageResultFragment resultFragment = new ImageResultFragment();
        resultFragment.setArguments(getBundle(file, TYPE_IMAGE, name));
        resultFragment.setCallback(callback);
        return resultFragment;
    }

    public ImageResultFragment() {

        setLayourResource(R.layout.picture_result);
    }

    @Override
    protected void setUiReferences(View view) {
        super.setUiReferences(view);
        imgResult = (ImageView) view.findViewById(R.id.imageResult);
        btnCrop = view.findViewById(R.id.btnCropImage);
        btnRotate = view.findViewById(R.id.btnRotateImage);
    }



    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        dialogHelper = new DialogHelper(getActivity());
        setImage(file);
    }

    private void setImage(File file) {
//        Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
//        if (bitmap != null) {
//            imgResult.setImageBitmap(bitmap);
//        }
        Picasso.with(getActivity()).load(file).fit().into(imgResult);

    }

    @Override
    protected void handleEvents(){
        super.handleEvents();
        btnCrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startImageCrop();
            }
        });

        btnRotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotateImage(file);
            }
        });
    }

    void rotateImage(final File file){
        dialogHelper.showProgress();
        new AsyncRotateImageFile(file).execute();
    }

    void startImageCrop(){
        Intent intent = new Intent(getActivity(), ImageCropActivity.class);
        intent.putExtra("file", file);
        startActivityForResult(intent, REQUEST_IMAGE_CROP);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == getActivity().RESULT_OK && requestCode == REQUEST_IMAGE_CROP){
            File file = (File) data.getSerializableExtra("file");
            setImage(file);
            this.file = file;
            Log.v("FILE", file.getAbsolutePath());
        }
    }

    private class AsyncRotateImageFile extends AsyncTask<String, Void, File> {

        File originalFile;
        Bitmap finalBitmap;
        public AsyncRotateImageFile(File originalFile) {

            this.originalFile = originalFile;

        }

        @Override
        protected File doInBackground(String... params) {

            Bitmap bitmap = BitmapFactory.decodeFile(originalFile.getAbsolutePath());

            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            finalBitmap = Bitmap.createBitmap(bitmap , 0, 0, bitmap .getWidth(), bitmap .getHeight(), matrix, true);
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(originalFile);
                finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);

                try {
                    fos.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return null;
            }

            return originalFile;

        }

        @Override
        protected void onPostExecute(File file) {

            if(file!=null){
                //Picasso.with(getActivity()).load(file).into(imgResult);
                //imgResult.setImageBitmap(finalBitmap);

                Picasso picasso = Picasso.with(getActivity());
                picasso.invalidate(file);
                picasso.load(file).into(imgResult);
            }

            dialogHelper.hideProgress();
        }


    }
}
