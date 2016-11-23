package com.apress.android.glesactivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

public class Main extends Activity {
	private GLSurfaceView _surfaceView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		_surfaceView = new GLSurfaceView(this);
		_surfaceView.setEGLContextClientVersion(1);
   		_surfaceView.setRenderer(new GLES10Renderer());
   		setContentView(_surfaceView);

		LinearLayout layout = new LinearLayout(this);

		LinearLayout.LayoutParams layoutParamsUpDown = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);
		layout.setGravity(Gravity.CENTER | Gravity.RIGHT);

		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View linearLayoutView = inflater
				.inflate(R.layout.main, layout, false);
		layout.addView(linearLayoutView);

		addContentView(layout, layoutParamsUpDown);

        Button b = (Button) findViewById(R.id.button1);
        b.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				startActivity(new Intent(Main.this, Second.class));
			}
		});
    }

}