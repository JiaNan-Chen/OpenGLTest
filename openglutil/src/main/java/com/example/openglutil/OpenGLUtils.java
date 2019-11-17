package com.example.openglutil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.microedition.khronos.opengles.GL10;

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
                sb.append(tempStr + "\n");
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

    public static int createTexture(Context context, int id) {

//        Bitmap mBitmap=Bitmap.createBitmap(30,30, Bitmap.Config.RGB_565);
//        Canvas canvas=new Canvas(mBitmap);
//
//        canvas.drawColor(0xff00ff00);
//        Paint paint=new Paint();
//        paint.setStrokeWidth(1);
//        paint.setColor(0xffff00ff);
//        canvas.drawPoint(15,15,paint);
//        //加载Bitmap
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        Bitmap mBitmap = BitmapFactory.decodeResource(context.getResources(), id,options);
        //保存到textureObjectId
        int[] textureObjectId = new int[1];
        if (mBitmap != null && !mBitmap.isRecycled()) {
            //生成一个纹理，保存到这个数组中
            GLES20.glGenTextures(1, textureObjectId, 0);
            //绑定GL_TEXTURE_2D
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureObjectId[0]);
            //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
            //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            //根据以上指定的参数，生成一个2D纹理
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap, 0);
            //回收释放
            mBitmap.recycle();
            //因为我们已经复制成功了。所以就进行解除绑定。防止修改
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
            return textureObjectId[0];
        }
        return 0;
    }

    public static int createExternalTexture() {
        int[] textureObjectIds = new int[1];
        //生成纹理iD
        GLES20.glGenTextures(1, textureObjectIds, 0);
        int textureObjectId = textureObjectIds[0];
        //将纹理ID绑定到GL_TEXTURE_EXTERNAL_OES
        //这里需要注意的是GL_TEXTURE_EXTERNAL_OES ，对应android 相机必须要的
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureObjectId);
        //设置放大缩小。设置边缘测量
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        return textureObjectId;
    }
}
