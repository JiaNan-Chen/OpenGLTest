package com.example.opengltest.view;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.example.opengltest.util.OpenGLUtils;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyGLSurfaceView extends GLSurfaceView {
    private int mProgramObjectId;

    public MyGLSurfaceView(Context context) {
        this(context, null);
    }

    public MyGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(2);
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
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {

            }

            @Override
            public void onDrawFrame(GL10 gl) {
                //0.先使用这个program?这一步应该可以放到onCreate中进行
                GLES20.glUseProgram(mProgramObjectId);

                //1.根据我们定义的取出定义的位置
                int vPosition = GLES20.glGetAttribLocation(mProgramObjectId, "aPosition");
                //2.开始启用我们的position
                GLES20.glEnableVertexAttribArray(vPosition);
                //3.将坐标数据放入
                GLES20.glVertexAttribPointer(
                        vPosition,  //上面得到的id
                        COORDS_PER_VERTEX, //告诉他用几个偏移量来描述一个顶点
                        GLES20.GL_FLOAT, false,
                        STRIDE, //一个顶点需要多少个字节的偏移量
                        mVertexFloatBuffer);

                //取出颜色
                int uColor = GLES20.glGetUniformLocation(mProgramObjectId, "uColor");

                //开始绘制
                //设置绘制三角形的颜色
                GLES20.glUniform4fv(
                        uColor,
                        1,
                        TRIANGLE_COLOR,
                        0
                );

                //绘制三角形.
                //draw arrays的几种方式 GL_TRIANGLES三角形 GL_TRIANGLE_STRIP三角形带的方式(开始的3个点描述一个三角形，后面每多一个点，多一个三角形) GL_TRIANGLE_FAN扇形(可以描述圆形)
                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, VERTEX_COUNT);
                //禁止顶点数组的句柄
                GLES20.glDisableVertexAttribArray(vPosition);
            }
        });
    }
}
