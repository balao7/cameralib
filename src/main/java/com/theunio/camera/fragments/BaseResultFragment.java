package com.theunio.camera.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.theunio.camera.CameraActivity;
import com.theunio.camera.CameraConsts;
import com.theunio.camera.R;
import com.theunio.camera.callbacks.CapturedMediaCallback;

import java.io.File;

/**
 * Created by goran on 17.12.2015.
 */
public class BaseResultFragment extends Fragment implements CameraConsts {

    public static final String EXTRA_FILE = "file";
    public static final String EXTRA_FILE_TYPE = "fileType";

    CapturedMediaCallback callback;

    View btnCancel;
    View btnDone;
    EditText inputCaption;
    TextView txtSendTo;

    int layourResource = R.layout.picture_result;

    File file;
    int type = TYPE_IMAGE;
    String name;

    protected static Bundle getBundle(File file, int type, String name){
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_FILE, file);
        args.putInt(EXTRA_FILE_TYPE, type);
        args.putString(CameraActivity.EXTRA_NAME, name);
        return args;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        if(arguments != null)
        {
            handleArguments(arguments);
        }

    }

    private void handleArguments(Bundle arguments){
        this.file = (File) arguments.getSerializable(EXTRA_FILE);
        this.type = arguments.getInt(EXTRA_FILE_TYPE);
        this.name = arguments.getString(CameraActivity.EXTRA_NAME);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(layourResource, container, false);
        setUiReferences(rootView);
        handleEvents();

        if(name!=null){
            txtSendTo.setText("Send to \"" + name.trim() + "\"");
        }

        return rootView;
    }

    protected void setUiReferences(View view){
        btnCancel = view.findViewById(R.id.btnCancelCamera);
        btnDone = view.findViewById(R.id.btnDoneCamera);
        inputCaption = (EditText)view.findViewById(R.id.inputCaption);
        txtSendTo = (TextView)view.findViewById(R.id.txtSendTo);
    }

    protected void handleEvents(){
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getCallback()!=null){
                    getCallback().cancel();
                }
            }
        });

        btnDone.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                      if(getCallback()!=null){
                          getCallback().save(file, type);
                      }
                    }
                }
        );
    }


    public int getLayourResource() {
        return layourResource;
    }

    public void setLayourResource(int layourResource) {
        this.layourResource = layourResource;
    }

    public CapturedMediaCallback getCallback() {
        return callback;
    }

    public void setCallback(CapturedMediaCallback callback) {
        this.callback = callback;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getCaption(){
        return inputCaption.getText().toString();
    }
}
