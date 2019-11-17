package com.example.camerautil;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Camera;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class CameraImpl implements ICamera {
    public static final int REQUEST_CAMERA_CODE = 0;

    private static volatile CameraImpl instance;

    private CameraImpl() {
    }

    public static CameraImpl getInstance() {
        if (instance == null) {
            synchronized (CameraImpl.class) {
                if (instance == null) {
                    instance = new CameraImpl();
                }
            }
        }
        return instance;
    }

    private Camera mCamera;

    @Override
    public void openCamera(final Activity context, int sdtWidth, int dstHeight) {
        if (ContextCompat.checkSelfPermission(context,
                android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            //先判断有没有权限 ，没有就在这里进行权限的申请
            ActivityCompat.requestPermissions(context,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CAMERA_CODE);
            return;
        }
        releaseCamera();
        mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        parameters.setPreviewSize(parameters.getSupportedPreviewSizes().get(0).width, parameters.getSupportedPreviewSizes().get(0).height);
        mCamera.setParameters(parameters);
        mCamera.cancelAutoFocus();
    }

    @Override
    public Camera getCamera() {
        return mCamera;
    }

    @Override
    public void releaseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }
}
