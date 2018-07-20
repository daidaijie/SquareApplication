package com.jscheng.squareapplication;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class SquareRender implements GLSurfaceView.Renderer {
    private final static String TAG = SquareRender.class.getSimpleName();
    private int mVertexShader;
    private int mFragmentShader;
    private int mProgram;
    private int vPosition;
    private int vColor;
    private int vMatrix;
    private float[] mProjectMatrix;
    private float[] mViewMatrix;
    private float[] mResultMatrix;

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        int result[] = new int[1];
        mProjectMatrix = new float[16];
        mViewMatrix = new float[16];
        mResultMatrix = new float[16];

        mVertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(mVertexShader, vertexSource);
        GLES20.glCompileShader(mVertexShader);
        GLES20.glGetShaderiv(mVertexShader, GLES20.GL_COMPILE_STATUS, result, 0);
        if (result[0] == 0) {
            Log.e(TAG, "mVertexShader compiler error");
            Log.e(TAG, GLES20.glGetShaderInfoLog(mVertexShader));
            GLES20.glDeleteShader(mVertexShader);
            return;
        }

        mFragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(mFragmentShader, fragmentSource);
        GLES20.glCompileShader(mFragmentShader);
        GLES20.glGetShaderiv(mFragmentShader, GLES20.GL_COMPILE_STATUS, result, 0);
        if (result[0] == 0) {
            Log.e(TAG, "mFragmentShader compiler error");
            Log.e(TAG, GLES20.glGetShaderInfoLog(mFragmentShader));
            GLES20.glDeleteShader(mFragmentShader);
            return;
        }

        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, mVertexShader);
        GLES20.glAttachShader(mProgram, mFragmentShader);
        GLES20.glLinkProgram(mProgram);
        GLES20.glGetProgramiv(mProgram, GLES20.GL_LINK_STATUS, result, 0);
        if (result[0] != GLES20.GL_TRUE) {
            Log.e(TAG, "mProgram link error");
            Log.e(TAG, GLES20.glGetProgramInfoLog(mFragmentShader));
            GLES20.glDeleteProgram(mProgram);
        }
        vPosition = GLES20.glGetAttribLocation(mProgram, "vPosition");
        vColor = GLES20.glGetUniformLocation(mProgram, "vColor");
        vMatrix = GLES20.glGetUniformLocation(mProgram, "vMatrix");

        FloatBuffer vertexbuffer = getVertices();
        int[] vb = new int[1];
        GLES20.glGenBuffers(1, vb, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vb[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexbuffer.limit() * 4, vertexbuffer, GLES20.GL_STATIC_DRAW);
        GLES20.glVertexAttribPointer(vPosition, 3, GLES20.GL_FLOAT, false, 12, 0);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        // 宽高比
        float ratio = (float) width / height;

        // 透视投影
        Matrix.frustumM(mProjectMatrix, // 变换的矩阵
                0, // 矩阵起始位置
                -ratio, // 相对观察点近面的左边距
                ratio, // 相对观察点近面的右边距
                -1, // 相对观察点近面的下边距
                1, // 相对观察点近面的上边距
                3, // 相对观察点近面距离
                7// 相对观察远近面距离
        );
        // 相机位置
        Matrix.setLookAtM(mViewMatrix,
                0,
                0,
                0,
                7.0f,
                0f,
                0f,
                0f,
                0f,
                1.0f,
                0.0f);
        //计算变换举着
        Matrix.multiplyMM(mResultMatrix, 0, mProjectMatrix,0, mViewMatrix, 0);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        //FloatBuffer vertexbuffer = getVertices();
        GLES20.glUseProgram(mProgram);
        GLES20.glEnableVertexAttribArray(vPosition);
//        GLES20.glVertexAttribPointer(vPosition, 3, GLES20.GL_FLOAT, false, 12, vertexbuffer);
        GLES20.glEnableVertexAttribArray(vMatrix);
        GLES20.glUniformMatrix4fv(vMatrix,1,false, mResultMatrix,0);
        GLES20.glUniform4f(vColor, 0.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisableVertexAttribArray(vPosition);
    }

    private FloatBuffer getVertices() {
        float vertices[] = {
                0.5f,  0.5f, 0.0f,
                0.5f, -0.5f, 0.0f,
                -0.5f, 0.5f, 0.0f,
                -0.5f, -0.5f, 0.0f
        };
        ByteBuffer buffer = ByteBuffer.allocateDirect(vertices.length * 4);
        buffer.order(ByteOrder.nativeOrder());
        FloatBuffer vertextBuf = buffer.asFloatBuffer();
        vertextBuf.put(vertices);
        vertextBuf.position(0);
        return vertextBuf;
    }

    private final static String vertexSource =
            "attribute vec4 vPosition;\n" +
            "uniform mat4 vMatrix;" +
            " void main() {\n" +
            "     gl_Position = vMatrix * vPosition;\n" +
            " }";

    private final static String fragmentSource =
            "precision mediump float;\n" +
            " uniform vec4 vColor;\n" +
            " void main() {\n" +
            "     gl_FragColor = vColor;\n" +
            " }";
}
