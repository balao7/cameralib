package com.theunio.camera.fragments;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.theunio.camera.R;
import com.theunio.camera.callbacks.CapturedMediaCallback;

import java.io.File;

/**
 * Created by goran on 17.12.2015.
 */
public class VideoResultFragment extends BaseResultFragment implements SeekBar.OnSeekBarChangeListener {

    VideoView videoView;

    TextView txtElapsed;
    TextView txtTotal;
    SeekBar seekBar;

    View btnPlay;

    Handler mHandler;

    public static VideoResultFragment newInstance(File file, String name, CapturedMediaCallback callback) {
        VideoResultFragment resultFragment = new VideoResultFragment();
        resultFragment.setArguments(getBundle(file, TYPE_VIDEO, name));
        resultFragment.setCallback(callback);
        resultFragment.setType(TYPE_VIDEO);
        return resultFragment;
    }

    public VideoResultFragment() {
        setLayourResource(R.layout.video_result);
    }

    @Override
    protected void setUiReferences(View view) {
        super.setUiReferences(view);
        videoView = (VideoView) view.findViewById(R.id.videoView);
        txtElapsed = (TextView) view.findViewById(R.id.txtElapsed);
        txtTotal = (TextView) view.findViewById(R.id.txtTotal);
        seekBar = (SeekBar) view.findViewById(R.id.seekBar);
        btnPlay = view.findViewById(R.id.btnPlay);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mHandler = new Handler();
        videoView.setVideoPath(file.getAbsolutePath());
        videoView.seekTo(100);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                txtTotal.setText(getFormattedTime(videoView.getDuration()));
            }
        });

        updateProgressBar();

    }


    @Override
     protected void handleEvents(){
        super.handleEvents();

        videoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                onVideoTouch();
                return false;
            }
        });

        seekBar.setOnSeekBarChangeListener(this);


        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPlayClick();
            }
        });

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                videoView.seekTo(100);
                btnPlay.setVisibility(View.VISIBLE);
            }
        });

    }

    private void onPlayClick(){

        if(videoView.isPlaying()==false) {
            videoView.start();
            btnPlay.setVisibility(View.GONE);
        }

    }

    private void onVideoTouch(){
        if(videoView.isPlaying()){
            videoView.pause();
            btnPlay.setVisibility(View.VISIBLE);
        }
    }

    private Runnable updateTimeTask = new Runnable() {
        public void run() {
            seekBar.setProgress(videoView.getCurrentPosition());
            seekBar.setMax(videoView.getDuration());
            seekBar.postDelayed(this, 100);
        }
    };

    private void updateProgressBar() {

        mHandler.postDelayed(updateTimeTask, 100);
    }

    @Override
    public void onProgressChanged(SeekBar seekbar, int progress,boolean fromTouch) {
        txtElapsed.setText(getFormattedTime(seekbar.getProgress()));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekbar) {
        mHandler.removeCallbacks(updateTimeTask);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekbar) {
        mHandler.removeCallbacks(updateTimeTask);
        videoView.seekTo(seekbar.getProgress());
        updateProgressBar();
    }

    private String getFormattedTime(int milisecond){
        int minutes = (int)(milisecond % (1000*60*60)) / (1000*60);
        int seconds = (int) ((milisecond % (1000*60*60)) % (1000*60) / 1000);

        String minutesString = String.valueOf(minutes);
        if(minutesString.length()==1){
            minutesString = "0" + minutesString;
        }

        String secondsString = String.valueOf(seconds);
        if(secondsString.length()==1){
            secondsString = "0" + secondsString;
        }

        return minutesString + ":" + secondsString;
    }


}
