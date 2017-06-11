package com.theunio.camera;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageCropActivity extends Activity {

    CropImageView cropImageView;
    View btnCancel;
    View btnOk;

    File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_crop);

        if(getIntent().getExtras()!=null){
            if(getIntent().getExtras().containsKey("file")){
                this.file = (File) getIntent().getExtras().getSerializable("file");
            }
        }

        if(file==null){
            Toast.makeText(getApplicationContext(), "File not found", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        cropImageView = (CropImageView) findViewById(R.id.cropImageView);
        btnCancel = findViewById(R.id.btnCancel);
        btnOk = findViewById(R.id.btnOk);

        cropImageView.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnResult();
            }
        });

    }

    private void returnResult(){
        try {
            File file = getFileFromBitmap(cropImageView.getCroppedImage());
            Intent intent = new Intent();
            intent.putExtra("file", file);
            setResult(RESULT_OK, intent);
            finish();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "There was an error", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private File getFileFromBitmap(Bitmap croppedBitmap) throws IOException {
        //create a file to write bitmap data
        String filename = String.valueOf(new java.util.Date().getTime()) + ".png";

        File f = new File(getExternalCacheDir(), filename);
        f.createNewFile();

        //Convert bitmap to byte array
        Bitmap bitmap = croppedBitmap;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
        byte[] bitmapdata = bos.toByteArray();

        //write the bytes in file
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(bitmapdata);
        fos.flush();
        fos.close();
        return f;
    }

}
