package com.apress.android.gamemenu;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

public class Game extends Activity {
	private GLSurfaceView _surfaceView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		_surfaceView = new GLSurfaceView(this);
		_surfaceView.setEGLContextClientVersion(2);
		_surfaceView.setRenderer(new GLES20Renderer());
		setContentView(_surfaceView);
	}

}
