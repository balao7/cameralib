package com.theunio.camera.callbacks;

import java.io.File;

/**
 * Created by goran on 18.12.2015.
 */
public interface CapturedMediaCallback {

    public void cancel();
    public void save(File file, int type);
}
