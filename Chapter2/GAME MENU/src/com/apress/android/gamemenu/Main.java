package com.apress.android.gamemenu;

import android.app.Dialog;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class Main extends ListActivity implements OnItemClickListener {
	private ListView _listView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		setListAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, getResources()
						.getStringArray(R.array.options)));
		_listView = (ListView) findViewById(android.R.id.list);
		_listView.setOnItemClickListener(this);
	}

	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if (arg2 == 0) {
			startActivity(new Intent(Main.this, Game.class));
		}
		else if (arg2 == 1) {
			Dialog d = new Dialog(this);
			d.setContentView(R.layout.highscore);
			d.setTitle("High Score");
			d.show();
		}
		else if (arg2 == 2) {
			Dialog d = new Dialog(this);
			d.setContentView(R.layout.editplayer);
			d.setTitle("Edit Player");
			d.show();
		}
	}

}
