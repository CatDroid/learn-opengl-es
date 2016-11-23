package com.apress.android.gldepthtest;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

public class Main extends Activity {
	private GLSurfaceView _surfaceView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		_surfaceView = new GLSurfaceView(this);
		_surfaceView.setEGLContextClientVersion(2);
		_surfaceView.setRenderer(new GLES20Renderer());
		setContentView(_surfaceView);
	}

}
