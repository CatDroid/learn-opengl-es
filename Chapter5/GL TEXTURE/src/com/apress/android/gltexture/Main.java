package com.apress.android.gltexture;

import android.app.Activity;
import android.graphics.YuvImage;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.RelativeLayout;

public class Main extends Activity implements OnTouchListener {
	private static final String TAG = "main";
	private GLSurfaceView _surfaceView;
	private final float _TOUCH_SENSITIVITY = 0.25f;
	private final float _ANGLE_SPAN = 90.0f;
	private float _dxFiltered = 0.0f;
	private float _zAngle = 0.0f;
	private float _filterSensitivity = 0.1f;
	private float _zAngleFiltered = 0.0f;
	private int _width;
	private float _touchedX;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		_surfaceView = new GLSurfaceView(this);
		_surfaceView.setEGLContextClientVersion(2);
		_surfaceView.setRenderer(new GLES20Renderer(this));
		setContentView(_surfaceView);
		RelativeLayout rl = new RelativeLayout(this);
		rl.setOnTouchListener(this);
		RelativeLayout.LayoutParams rllp = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.MATCH_PARENT);
		addContentView(rl, rllp);
		getDeviceWidth();
		
		//ldpi是120，mdpi是160，hdpi是240，xhdpi是320
		int dpi = this.getResources().getConfiguration().densityDpi;
		Log.d(TAG, "dpi " + dpi );
	}

	@Override
	protected void onPause() {
		_surfaceView.onPause();
		// Counter.reset();
		super.onPause();
		if (isFinishing()) {
			// save high scores etc
			GLES20Renderer.setZAngle(0);
			_dxFiltered = 0.0f;
			_zAngle = 0.0f;
			_zAngleFiltered = 0.0f;
			this.finish();
		}
	}

	public void getDeviceWidth() {
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels;
		int height = dm.heightPixels;
		if (width > height) {
			_width = width;
		} else {
			_width = height;
		}
	}

	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			_touchedX = event.getX();
		} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
			float touchedX = event.getX();
			float dx = Math.abs(_touchedX - touchedX);
			_dxFiltered = _dxFiltered * (1.0f - _filterSensitivity) + dx
					* _filterSensitivity;

			if (touchedX < _touchedX) {
				_zAngle = (2 * _dxFiltered / _width) * _TOUCH_SENSITIVITY
						* _ANGLE_SPAN;
				_zAngleFiltered = _zAngleFiltered * (1.0f - _filterSensitivity)
						+ _zAngle * _filterSensitivity;
				GLES20Renderer.setZAngle(GLES20Renderer.getZAngle()
						+ _zAngleFiltered);
			} else {
				_zAngle = (2 * _dxFiltered / _width) * _TOUCH_SENSITIVITY
						* _ANGLE_SPAN;
				_zAngleFiltered = _zAngleFiltered * (1.0f - _filterSensitivity)
						+ _zAngle * _filterSensitivity;
				GLES20Renderer.setZAngle(GLES20Renderer.getZAngle()
						- _zAngleFiltered);
			}
		}
		return true;
	}
}