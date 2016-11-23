package com.apress.android.addcontentview;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;

public class Main extends Activity {
	private GLSurfaceView _surfaceView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		_surfaceView = new GLSurfaceView(this);
		_surfaceView.setEGLContextClientVersion(2);
		_surfaceView.setRenderer(new GLES20Renderer());
		setContentView(_surfaceView);

		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setPadding(0, 200, 0, 0);

		Button buttonUp = new Button(this);
		buttonUp.setText("Up");
		buttonUp.setWidth(110);
		buttonUp.setHeight(85);
		LinearLayout.LayoutParams layoutParamsButtonUp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutParamsButtonUp.setMargins(0, 0, 0, 20);

		Button buttonDown = new Button(this);
		buttonDown.setText("Down");
		buttonDown.setWidth(110);
		buttonDown.setHeight(85);
		LinearLayout.LayoutParams layoutParamsButtonDown = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutParamsButtonDown.setMargins(0, 20, 0, 0);

		layout.addView(buttonUp, layoutParamsButtonUp);
		layout.addView(buttonDown, layoutParamsButtonDown);
		layout.setGravity(Gravity.CENTER | Gravity.RIGHT);

		addContentView(layout, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
	}

}
