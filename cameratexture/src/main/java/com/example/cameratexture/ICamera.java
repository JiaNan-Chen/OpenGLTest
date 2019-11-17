package com.example.cameratexture;

import android.app.Activity;
import android.hardware.Camera;

public interface ICamera {
    void openCamera(Activity context,int sdtWidth,int dstHeight);

    Camera getCamera();

    void releaseCamera();
}
