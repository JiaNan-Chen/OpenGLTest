package com.example.a13imagecamaretrack;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.example.openglutil.OpenGLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyGLSurfaceView extends GLSurfaceView {
    private static final String TAG = MyGLSurfaceView.class.getSimpleName();
    private int mProgramObjectId;

    private int mTextureId;

    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTextureBuffer;
    private FloatBuffer mModelMatrixBuffer;
    private FloatBuffer mViewMatrixBuffer;
    private FloatBuffer mPerspectiveBuffer;

    private float[] mVertex = {
            -0.5f, -0.5f, 0,
            -0.5f, 0.5f, 0,
            0.5f, -0.5f, 0,
            0.5f, 0.5f, 0,
    };

    private float[] mTexturePosition = {
            0, 1,
            0, 0,
            1, 1,
            1, 0,
    };

    private float[] mModelMatrix = {
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1,
    };

    private float[] mViewMatrix = new float[16];
    private float[] mPerspectiveMatrix = new float[16];

    private static final float DISTANCE_XZ = 2;

    private double mDegreeX = 0, mTempDegreeX = 0;
    private double mDegreeY = Math.PI / 2, mTempDegreeY = Math.PI / 2;
    private float mDownX, mDownY;

    public MyGLSurfaceView(Context context) {
        this(context, null);
    }

    public MyGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(2);

        mVertexBuffer = ByteBuffer.allocateDirect(4 * 3 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(mVertex);
        mVertexBuffer.position(0);
        mTextureBuffer = ByteBuffer.allocateDirect(4 * 2 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(mTexturePosition);
        mTextureBuffer.position(0);
        setRenderer(new Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                //在创建的时候，去创建这些着色器
                mProgramObjectId = GLES20.glCreateProgram();
                int vertexShaderObjectId = OpenGLUtils.compileShaderFile(getContext(), GLES20.GL_VERTEX_SHADER, "1/triangles_vertex.glsl");
                GLES20.glAttachShader(mProgramObjectId, vertexShaderObjectId);
                int fragmentShaderObjectId = OpenGLUtils.compileShaderFile(getContext(), GLES20.GL_FRAGMENT_SHADER, "1/triangles_color.glsl");
                GLES20.glAttachShader(mProgramObjectId, fragmentShaderObjectId);
                //4.最后，启动GL link program
                GLES20.glLinkProgram(mProgramObjectId);

                mTextureId = OpenGLUtils.createTexture(getContext(), R.drawable.pic);
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
                if (width < height) {
                    mModelMatrix[5] = 1.0f * width / height;
                } else {
                    mModelMatrix[0] = 1.0f * height / width;
                }
                mModelMatrixBuffer = ByteBuffer.allocateDirect(4 * 4 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(mModelMatrix);
                mModelMatrixBuffer.position(0);

                Matrix.perspectiveM(mPerspectiveMatrix, 0, 45, 1.0f, 0.5f, 3f);
                mPerspectiveBuffer = ByteBuffer.allocateDirect(4 * 4 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(mPerspectiveMatrix);
                mPerspectiveBuffer.position(0);
            }

            @Override

            public void onDrawFrame(GL10 gl) {
//                Log.i(TAG,"开始刷新");
                Matrix.setLookAtM(mViewMatrix, 0, (float) (Math.sin(mDegreeY) * Math.sin(mDegreeX) * DISTANCE_XZ), (float) (Math.cos(mDegreeY) * DISTANCE_XZ), (float) (Math.sin(mDegreeY) * Math.cos(mDegreeX) * DISTANCE_XZ), 0, 0, 0, 0, Math.sin(mDegreeY) > 0 ? 1 : -1, 0);
                mViewMatrixBuffer = ByteBuffer.allocateDirect(4 * 4 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(mViewMatrix);
                mViewMatrixBuffer.position(0);

                //0.先使用这个program?这一步应该可以放到onCreate中进行
                GLES20.glClearColor(1, 1, 1, 1);
                GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
                GLES20.glUseProgram(mProgramObjectId);
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

                int aModelMatrix = GLES20.glGetUniformLocation(mProgramObjectId, "aModelMatrix");
                GLES20.glEnableVertexAttribArray(aModelMatrix);
                GLES20.glUniformMatrix4fv(aModelMatrix, 1, false, mModelMatrixBuffer);

                int aViewMatrix = GLES20.glGetUniformLocation(mProgramObjectId, "aViewMatrix");
                GLES20.glEnableVertexAttribArray(aViewMatrix);
                GLES20.glUniformMatrix4fv(aViewMatrix, 1, false, mViewMatrixBuffer);

                int aPerspectiveMatrix = GLES20.glGetUniformLocation(mProgramObjectId, "aPerspectiveMatrix");
                GLES20.glEnableVertexAttribArray(aPerspectiveMatrix);
                GLES20.glUniformMatrix4fv(aPerspectiveMatrix, 1, false, mPerspectiveBuffer);

                int aColor = GLES20.glGetAttribLocation(mProgramObjectId, "aTexturePosition");
                GLES20.glEnableVertexAttribArray(aColor);
                GLES20.glVertexAttribPointer(aColor, 2, GLES20.GL_FLOAT, false, 2 * 4, mTextureBuffer);

                // Set the active texture unit to texture unit 0.
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

                // Bind the texture to this unit.
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);

                // Tell the texture uniform sampler to use this texture in the shader by
                // telling it to read from texture unit 0.
                GLES20.glUniform1i(GLES20.glGetUniformLocation(mProgramObjectId, "uTextureUnit"), 0);
                //绘制三角形.
                //draw arrays的几种方式 GL_TRIANGLES三角形 GL_TRIANGLE_STRIP三角形带的方式(开始的3个点描述一个三角形，后面每多一个点，多一个三角形) GL_TRIANGLE_FAN扇形(可以描述圆形)
                GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
            }
        });
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mDownX = event.getX();
            mDownY = event.getY();
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            mDegreeX = mTempDegreeX - ((event.getX() - mDownX) / getWidth()) * Math.PI / 2;
            mDegreeY = mTempDegreeY - ((event.getY() - mDownY) / getHeight()) * Math.PI / 2;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            mTempDegreeX -= ((event.getX() - mDownX) / getWidth()) * Math.PI / 2;
            mTempDegreeY -= ((event.getY() - mDownY) / getHeight()) * Math.PI / 2;
        }
        return true;
    }
}
