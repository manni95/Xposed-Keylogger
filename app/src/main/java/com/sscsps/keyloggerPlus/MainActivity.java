package com.sscsps.keyloggerPlus;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		final SharedPreferences sp = getSharedPreferences(Hook.mSharedPrefs,MODE_WORLD_READABLE);

		((TextView) findViewById(R.id.pathBox)).setText(sp.getString(Hook.mLogPath, Environment.getExternalStorageDirectory() + "/KeyLogs/logs.txt"));

		findViewById(R.id.saveButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sp.edit().putString(Hook.mLogPath, ((EditText) findViewById(R.id.pathBox)).getText().toString()).apply();
				preference_set();
			}
		});

		((CheckBox) findViewById(R.id.activeCheckBox)).setChecked(sp.getBoolean(Hook.mActive, false));

		((CheckBox) findViewById(R.id.activeCheckBox)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				sp.edit().putBoolean(Hook.mActive, isChecked).apply();
				preference_set();
			}
		});

	}
	private void preference_set(){
		Toast.makeText(MainActivity.this, "Settings saved. Reboot to use new preferences", Toast.LENGTH_SHORT).show();
	}
}
