package com.apress.android.setcontentview;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Button;

public class Main extends Activity {

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Button button = new Button(this);
		button.setText("SETCONTENTVIEW");
		setContentView(button);
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}
