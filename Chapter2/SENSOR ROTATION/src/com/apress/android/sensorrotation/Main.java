package com.apress.android.sensorrotation;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class Main extends Activity implements SensorEventListener {
	private GLSurfaceView _surfaceView;
	private float[] _inR = new float[16];
	private float[] _outR = new float[16];
	private float[] _values = new float[3];
	private float[] _I = new float[16];
	private float[] _accelVals = new float[3];
	private float[] _magVals = new float[3];
	private float[] _gravVals = new float[3];
	private static final float _SENSITIVITY = 0.5f;
	private TextView _textView;
	private float _a = 0.1f;
	private float _orientationFiltered = 0.0f;
	private float _gravityFiltered;
	private float[] _accelValsFiltered = new float[3];
	private float[] _magValsFiltered = new float[3];

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		_surfaceView = new GLSurfaceView(this);
		_surfaceView.setEGLContextClientVersion(2);
		_surfaceView.setRenderer(new GLES20Renderer());
		setContentView(_surfaceView);

		SensorManager sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

		sm.registerListener(this,
				sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_NORMAL);
		sm.registerListener(this,
				sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
				SensorManager.SENSOR_DELAY_NORMAL);
		sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_GRAVITY),
				SensorManager.SENSOR_DELAY_NORMAL);

		RelativeLayout layout = new RelativeLayout(this);
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.MATCH_PARENT);
		layout.setGravity(Gravity.TOP);
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		View textView = inflater.inflate(R.layout.main, layout, false);
		layout.addView(textView);
		addContentView(layout, layoutParams);
		_textView = (TextView) findViewById(R.id.textView1);
	}

	@Override
	protected void onPause() {
		_surfaceView.onPause();
		// Counter.reset();
		super.onPause();
		if (isFinishing()) {
			// save high scores etc
			GLES20Renderer.setZAngle(0);
			_orientationFiltered = 0.0f;
			_gravityFiltered = 0.0f;
			_accelValsFiltered[0] = 0.0f;
			_accelValsFiltered[1] = 0.0f;
			_accelValsFiltered[2] = 0.0f;
			_magValsFiltered[0] = 0.0f;
			_magValsFiltered[1] = 0.0f;
			_magValsFiltered[2] = 0.0f;
			this.finish();
		}
	}

	public void onSensorChanged(SensorEvent event) {
		if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
			return;
		}

		switch (event.sensor.getType()) {
		case Sensor.TYPE_ACCELEROMETER: {
			_accelVals = event.values.clone();
			_accelValsFiltered[0] = _accelValsFiltered[0] * (1.0f - _a)
					+ _accelVals[0] * _a;
			_accelValsFiltered[1] = _accelValsFiltered[1] * (1.0f - _a)
					+ _accelVals[1] * _a;
			_accelValsFiltered[2] = _accelValsFiltered[2] * (1.0f - _a)
					+ _accelVals[2] * _a;
			break;
		}
		case Sensor.TYPE_MAGNETIC_FIELD: {
			_magVals = event.values.clone();
			_magValsFiltered[0] = _magValsFiltered[0] * (1.0f - _a)
					+ _magVals[0] * _a;
			_magValsFiltered[1] = _magValsFiltered[1] * (1.0f - _a)
					+ _magVals[1] * _a;
			_magValsFiltered[2] = _magValsFiltered[2] * (1.0f - _a)
					+ _magVals[2] * _a;
			break;
		}
		case Sensor.TYPE_GRAVITY: {
			_gravVals = event.values.clone();
			break;
		}
		}
		if (_accelVals != null && _magVals != null && _gravVals != null) {
			boolean success = SensorManager.getRotationMatrix(_inR, _I,
					_accelValsFiltered, _magValsFiltered);
			_gravityFiltered = _gravityFiltered * (1.0f - _a)
					+ Math.abs(_gravVals[2]) * _a;
			float scaling = 0;
			SensorManager.remapCoordinateSystem(_inR, SensorManager.AXIS_Y,
					SensorManager.AXIS_X, _outR);
			if (success) {
				SensorManager.getOrientation(_outR, _values);
				if (_gravityFiltered >= SensorManager.GRAVITY_EARTH
						|| Math.abs(_values[1]) <= 0.2) {
					_gravityFiltered = SensorManager.GRAVITY_EARTH;
				}

				if (_gravityFiltered >= 6
						&& _gravityFiltered <= SensorManager.GRAVITY_EARTH * 1) {
					scaling = _SENSITIVITY
							+ (2 - (_gravityFiltered / SensorManager.GRAVITY_EARTH));
					_orientationFiltered = _orientationFiltered * (1.0f - _a)
							+ _outR[0] * _a;
					float zAngle = scaling * _orientationFiltered * 90;
					GLES20Renderer.setZAngle(zAngle);
					_textView.setText("Angle:         "
							+ Float.valueOf(zAngle).toString() + "\n");
					_textView.append("Fraction:      "
							+ Float.valueOf(_orientationFiltered).toString()
							+ "\n");
					_textView.append("Pitch:         "
							+ Float.valueOf(_values[1]).toString() + "\n");
					_textView
							.append("Gravity:       "
									+ Float.valueOf(_gravityFiltered)
											.toString() + "\n");
				} else {
					_textView
							.setText("Gravity:       "
									+ Float.valueOf(_gravityFiltered)
											.toString() + "\n");
				}
			}
		}
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}
}