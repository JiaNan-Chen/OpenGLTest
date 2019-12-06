package com.example.ebo;

import android.content.Context;
import android.gesture.Gesture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.example.openglutil.OpenGLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyGLSurfaceView extends GLSurfaceView {
    private int mProgramObjectId;
    private int mVertexVBO, mVertexEBO;
    private FloatBuffer mVertexBuffer;
    private IntBuffer mElementBuffer;

    private float[] mVertex = {
            0f, 0.5f,
            -0.5f, -0.25f,
            0.5f, -0.25f,
            1, 1
    };

    private int[] mElement = {0, 1, 2, 0, 2, 3};

    public MyGLSurfaceView(Context context) {
        this(context, null);
    }

    public MyGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(2);
        mVertexBuffer = ByteBuffer.allocateDirect(4 * 2 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(mVertex);
        mVertexBuffer.position(0);
        mElementBuffer = ByteBuffer.allocateDirect(6 * 4).order(ByteOrder.nativeOrder()).asIntBuffer().put(mElement);
        mElementBuffer.position(0);
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

                int[] buffer = new int[2];
                GLES20.glGenBuffers(2, buffer, 0);
                mVertexVBO = buffer[0];
                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVertexVBO);
                GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, 4 * 2 * 4, mVertexBuffer, GLES20.GL_DYNAMIC_DRAW);
                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

                mVertexEBO = buffer[1];
                GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mVertexEBO);
                GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, 6 * 4, mElementBuffer, GLES20.GL_DYNAMIC_DRAW);
                GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {

            }

            @Override
            public void onDrawFrame(GL10 gl) {
                //0.先使用这个program?这一步应该可以放到onCreate中进行
                GLES20.glClearColor(1, 1, 1, 1);
                GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
                GLES20.glUseProgram(mProgramObjectId);
                //1.根据我们定义的取出定义的位置
                int aPosition = GLES20.glGetAttribLocation(mProgramObjectId, "aPosition");
                //2.开始启用我们的position
                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVertexVBO);
                GLES20.glEnableVertexAttribArray(aPosition);
                //3.将坐标数据放入
                //0f,     0.5f,
                //-0.5f,  -0.25f,
                //0.5f,   -0.25f
                GLES20.glVertexAttribPointer(
                        aPosition,      //上面得到的id
                        2,        //每个顶点的维度
                        GLES20.GL_FLOAT, false,
                        2 * 4,  //一个顶点需要多少个字节的偏移量
                        0);

                GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mVertexEBO);
                //绘制三角形.
                //draw arrays的几种方式 GL_TRIANGLES三角形 GL_TRIANGLE_STRIP三角形带的方式(开始的3个点描述一个三角形，后面每多一个点，多一个三角形) GL_TRIANGLE_FAN扇形(可以描述圆形)
//                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
                GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_INT, 0);
            }
        });
    }
}
