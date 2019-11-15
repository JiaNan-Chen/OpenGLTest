package com.example.openglutil;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class OpenGLUtils {
    public static final String TAG = OpenGLUtils.class.getSimpleName();

    /**
     * 对ShaderCode进行编译
     *
     * @param type       shader的type
     * @param shaderCode 进行编译的Shader代码
     * @return shaderObjectId
     */
    private static int compileShaderCode(int type, String shaderCode) {
        //得到一个着色器的ID。主要是对ID进行操作
        int shaderObjectId = GLES20.glCreateShader(type);
        //如果着色器的id不为0，则表示是可以用的
        if (shaderObjectId != 0) {
            //0.上传代码
            GLES20.glShaderSource(shaderObjectId, shaderCode);
            //1.编译代码.根据刚刚和代码绑定的ShaderObjectId进行编译
            GLES20.glCompileShader(shaderObjectId);

            //2.查询编译的状态
            int[] status = new int[1];
            //调用getShaderIv ，传入GL_COMPILE_STATUS进行查询
            GLES20.glGetShaderiv(shaderObjectId, GLES20.GL_COMPILE_STATUS, status, 0);

            if (status[0] == 0) { //等于0。则表示失败
                //失败的话，需要释放资源，就是删除这个引用
                GLES20.glDeleteShader(shaderObjectId);
                Log.e("OpenGL Utils", "compile failed!");
                return 0;
            }
        }
        //最后都会去返回这个shader的引用id
        return shaderObjectId;
    }

    private static String readAssetContent(Context context, String fileName) {
        InputStream open = null;
        BufferedReader reader = null;
        StringBuffer sb = new StringBuffer();
        try {
            open = context.getAssets().open(fileName);
            reader = new BufferedReader(new InputStreamReader(open));
            String tempStr;
            while ((tempStr = reader.readLine()) != null) {
                sb.append(tempStr);
            }
            reader.close();
            Log.i(TAG, sb.toString());
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (open != null) {
                try {
                    open.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

    public static int compileShaderFile(Context context, int type, String fileName) {
        if (fileName == null || fileName.length() <= 0) {
            return -1;
        }
        return compileShaderCode(type, readAssetContent(context, fileName));
    }
}
