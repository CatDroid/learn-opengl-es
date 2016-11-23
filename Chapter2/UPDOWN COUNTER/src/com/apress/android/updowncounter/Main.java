package com.apress.android.updowncounter;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Main extends Activity {
	private GLSurfaceView _surfaceView;
	private TextView _textView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		_surfaceView = new GLSurfaceView(this);
		_surfaceView.setEGLContextClientVersion(2);
		_surfaceView.setRenderer(new GLES20Renderer());
		setContentView(_surfaceView);

		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.HORIZONTAL);
		layout.setGravity(Gravity.BOTTOM | Gravity.LEFT);

		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);

		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View linearLayoutView = inflater
				.inflate(R.layout.updown, layout, false);
		View textView = inflater.inflate(R.layout.counter, layout, false);
		layout.addView(linearLayoutView);
		layout.addView(textView);

		addContentView(layout, layoutParams);

		_textView = (TextView) findViewById(R.id.counter);
		setUpDownClickListeners();
	}

	public void setUpDownClickListeners() {
		Button buttonUp, buttonDown;

		buttonUp = (Button) findViewById(R.id.up);
		buttonDown = (Button) findViewById(R.id.down);

		buttonUp.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Counter.getUpDownNextValue();
				_textView.setText(String.valueOf(Counter.getUpDownValue()));
			}
		});
		buttonDown.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Counter.getUpDownPreviousValue();
				_textView.setText(String.valueOf(Counter.getUpDownValue()));
			}
		});
	}

}
