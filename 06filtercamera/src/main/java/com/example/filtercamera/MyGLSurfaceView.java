package com.example.filtercamera;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.example.openglutil.OpenGLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyGLSurfaceView extends GLSurfaceView {
    public interface TextureListener {
        void textureCreateDown(SurfaceTexture surfaceTexture);
    }

    private int mProgramObjectId;
    private int mLogoObjectId;

    private int mTextureId;
    private int mLogoTextureId;

    private SurfaceTexture mSurfaceTexture;

    private FloatBuffer mVertexBuffer;

    private FloatBuffer mTextureBuffer;

    private FloatBuffer mLogoVertexBuffer;
    private FloatBuffer mLogoTextureBuffer;

    private TextureListener mTextureListener;

    private int mMode = 0;
    private int mAlpha = 0;

    public void setTextureListener(TextureListener listener) {
        if (mSurfaceTexture == null) {
            mTextureListener = listener;
            return;
        }
        listener.textureCreateDown(mSurfaceTexture);
    }

    private float[] mVertex = {
            -1f, 1f, 0,
            -1f, -1f, 0,
            1f, 1f, 0,
            1f, -1f, 0,
    };

    private float[] mTexturePosition = {
            0, 1,//0,1
            1, 1,//1,1
            0, 0,//0,0
            1, 0,//1,0
    };

    private float[] mLogoVertex = {
            -0.95f, 0.95f, 0,
            -0.95f, 0.85f, 0,
            -0.3f, 0.95f, 0,
            -0.3f, 0.85f, 0,
    };

    private float[] mLogoTexturePosition = {
            0, 0,
            0, 1,
            1, 0,
            1, 1,
    };

    public MyGLSurfaceView(Context context) {
        this(context, null);
    }

    public MyGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().setFormat(PixelFormat.RGBA_8888);
        setEGLContextClientVersion(2);
        mVertexBuffer = ByteBuffer.allocateDirect(4 * 3 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(mVertex);
        mVertexBuffer.position(0);
        mLogoVertexBuffer = ByteBuffer.allocateDirect(4 * 3 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(mLogoVertex);
        mLogoVertexBuffer.position(0);
        mTextureBuffer = ByteBuffer.allocateDirect(4 * 2 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(mTexturePosition);
        mTextureBuffer.position(0);
        mLogoTextureBuffer = ByteBuffer.allocateDirect(4 * 2 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(mLogoTexturePosition);
        mLogoTextureBuffer.position(0);
        setRenderer(new Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                GLES20.glEnable(GLES20.GL_BLEND); //打开混合功能
                GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA); //指定混合模式
                //在创建的时候，去创建这些着色器
                mProgramObjectId = GLES20.glCreateProgram();
                int vertexShaderObjectId = OpenGLUtils.compileShaderFile(getContext(), GLES20.GL_VERTEX_SHADER, "1/triangles_vertex.glsl");
                GLES20.glAttachShader(mProgramObjectId, vertexShaderObjectId);
                int fragmentShaderObjectId = OpenGLUtils.compileShaderFile(getContext(), GLES20.GL_FRAGMENT_SHADER, "1/triangles_color.glsl");
                GLES20.glAttachShader(mProgramObjectId, fragmentShaderObjectId);
                //4.最后，启动GL link program
                GLES20.glLinkProgram(mProgramObjectId);

                mTextureId = OpenGLUtils.createExternalTexture();
                mSurfaceTexture = new SurfaceTexture(mTextureId);
                mSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
                    @Override
                    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                        requestRender();
                    }
                });
                if (mTextureListener != null) {
                    mTextureListener.textureCreateDown(mSurfaceTexture);
                }

                //创建logo的programe和纹理
                mLogoObjectId = GLES20.glCreateProgram();
                int logoVertexShaderObjectId = OpenGLUtils.compileShaderFile(getContext(), GLES20.GL_VERTEX_SHADER, "1/logo_vertex.glsl");
                GLES20.glAttachShader(mLogoObjectId, logoVertexShaderObjectId);
                int logoFragmentShaderObjectId = OpenGLUtils.compileShaderFile(getContext(), GLES20.GL_FRAGMENT_SHADER, "1/logo_color.glsl");
                GLES20.glAttachShader(mLogoObjectId, logoFragmentShaderObjectId);
                //4.最后，启动GL link program
                GLES20.glLinkProgram(mLogoObjectId);
                mLogoTextureId = OpenGLUtils.createTexture(getContext(), R.drawable.logo);
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {

            }

            @Override
            public void onDrawFrame(GL10 gl) {
                GLES20.glClearColor(1, 0, 0, 1);
                GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

                //0.先使用这个program?这一步应该可以放到onCreate中进行
                GLES20.glUseProgram(mProgramObjectId);
                if (mSurfaceTexture != null) {
                    mSurfaceTexture.updateTexImage();
                }
//                Log.i("testdraw",Thread.currentThread().getName());

                //1.根据我们定义的取出定义的位置
                int aPosition = GLES20.glGetAttribLocation(mProgramObjectId, "aPosition");
                //2.开始启用我们的position
                GLES20.glEnableVertexAttribArray(aPosition);
                //3.将坐标数据放入
                GLES20.glVertexAttribPointer(
                        aPosition,  //上面得到的id
                        3, //告诉他用几个偏移量来描述一个顶点
                        GLES20.GL_FLOAT, false,
                        3 * 4, //一个顶点需要多少个字节的偏移量
                        mVertexBuffer);

                int aColor = GLES20.glGetAttribLocation(mProgramObjectId, "aTexturePosition");
                GLES20.glEnableVertexAttribArray(aColor);
                GLES20.glVertexAttribPointer(aColor, 2, GLES20.GL_FLOAT, false, 2 * 4, mTextureBuffer);

                GLES20.glUniform1i(GLES20.glGetUniformLocation(mProgramObjectId, "uMode"), mMode);

                // Set the active texture unit to texture unit 0.
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

                // Bind the texture to this unit.
                GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureId);

                // Tell the texture uniform sampler to use this texture in the shader by
                // telling it to read from texture unit 0.
                GLES20.glUniform1i(GLES20.glGetUniformLocation(mProgramObjectId, "uTextureUnit"), 0);
                //绘制三角形.
                //draw arrays的几种方式 GL_TRIANGLES三角形 GL_TRIANGLE_STRIP三角形带的方式(开始的3个点描述一个三角形，后面每多一个点，多一个三角形) GL_TRIANGLE_FAN扇形(可以描述圆形)
                GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

                //----------------------------------------------------------------

                GLES20.glUseProgram(mLogoObjectId);
                //1.根据我们定义的取出定义的位置
                int aTexturePosition = GLES20.glGetAttribLocation(mLogoObjectId, "aPosition");
                //2.开始启用我们的position
                GLES20.glEnableVertexAttribArray(aTexturePosition);
                //3.将坐标数据放入
                GLES20.glVertexAttribPointer(
                        aTexturePosition,  //上面得到的id
                        3, //告诉他用几个偏移量来描述一个顶点
                        GLES20.GL_FLOAT, false,
                        3 * 4, //一个顶点需要多少个字节的偏移量
                        mLogoVertexBuffer);

                int aTextureColor = GLES20.glGetAttribLocation(mLogoObjectId, "aTexturePosition");
                GLES20.glEnableVertexAttribArray(aTextureColor);
                GLES20.glVertexAttribPointer(aTextureColor, 2, GLES20.GL_FLOAT, false, 2 * 4, mLogoTextureBuffer);

                GLES20.glUniform1f(GLES20.glGetUniformLocation(mLogoObjectId, "uAlpha"), 1.0f *Math.abs( ((mAlpha % 200) - 100)) / 100);

                // Set the active texture unit to texture unit 0.
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

                // Bind the texture to this unit.
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mLogoTextureId);

                // Tell the texture uniform sampler to use this texture in the shader by
                // telling it to read from texture unit 0.
                GLES20.glUniform1i(GLES20.glGetUniformLocation(mLogoObjectId, "uTextureUnit"), 0);
                //绘制三角形.
                //draw arrays的几种方式 GL_TRIANGLES三角形 GL_TRIANGLE_STRIP三角形带的方式(开始的3个点描述一个三角形，后面每多一个点，多一个三角形) GL_TRIANGLE_FAN扇形(可以描述圆形)
                GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
                mAlpha ++;
                //禁止顶点数组的句柄
//                GLES20.glDisableVertexAttribArray(aPosition);
            }
        });
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    public void setMode(int mode) {
        mMode = mode;
    }
}
