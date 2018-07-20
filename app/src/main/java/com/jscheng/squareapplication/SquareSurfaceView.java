package com.jscheng.squareapplication;

import android.content.Context;
import android.opengl.GLSurfaceView;

public class SquareSurfaceView extends GLSurfaceView {
    private GLSurfaceView.Renderer mRender;
    public SquareSurfaceView(Context context) {
        super(context);
        setEGLContextClientVersion(2);
        mRender = new SquareRender();
        setRenderer(mRender);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }
}
