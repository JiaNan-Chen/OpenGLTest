package com.example.cameratexture;

import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.os.Bundle;

import android.hardware.Camera;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import java.io.IOException;

import static com.example.cameratexture.CameraImpl.REQUEST_CAMERA_CODE;

public class MainActivity extends AppCompatActivity {
    private MyGLSurfaceView mGLSurfaceView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGLSurfaceView = findViewById(R.id.surface_view);
    }

    @Override
    protected void onResume() {
        super.onResume();
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                CameraImpl.getInstance().openCamera(MainActivity.this, mGLSurfaceView.getWidth(), mGLSurfaceView.getHeight());
                mGLSurfaceView.setTextureListener(new MyGLSurfaceView.TextureListener() {
                    @Override
                    public void textureCreateDown(SurfaceTexture surfaceTexture) {
                        Camera camera = CameraImpl.getInstance().getCamera();
                        if (camera != null) {
                            try {
                                camera.setPreviewTexture(surfaceTexture);
                                camera.startPreview();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        CameraImpl.getInstance().releaseCamera();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CAMERA_CODE && resultCode == RESULT_OK) {
            mGLSurfaceView.setTextureListener(new MyGLSurfaceView.TextureListener() {
                @Override
                public void textureCreateDown(SurfaceTexture surfaceTexture) {
                    Camera camera = CameraImpl.getInstance().getCamera();
                    if (camera != null) {
                        try {
                            camera.setPreviewTexture(surfaceTexture);
                            camera.startPreview();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } else {
            Toast.makeText(this, "需要开启相机权限！", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
