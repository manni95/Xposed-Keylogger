package com.sscsps.keyloggerPlus;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		final String defaultToast = "Settings saved. Reboot to use new preferences";
		final SharedPreferences sp = getSharedPreferences(Hook.mSharedPrefs,MODE_WORLD_READABLE);


		if(!sp.getBoolean(Hook.mUseDate, false))
			((TextView) findViewById(R.id.pathBox)).setText(sp.getString(Hook.mLogPath, "KeyLogs/logs.txt"));
		else
			((TextView) findViewById(R.id.pathBox)).setText(sp.getString(Hook.mLogPath, "KeyLogs"));


		findViewById(R.id.saveButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sp.edit().putString(Hook.mLogPath, ((EditText) findViewById(R.id.pathBox)).getText().toString()).apply();
				showToast(defaultToast);
			}
		});

		((CheckBox) findViewById(R.id.activeCheckBox)).setChecked(sp.getBoolean(Hook.mActive, false));

		((CheckBox) findViewById(R.id.activeCheckBox)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				sp.edit().putBoolean(Hook.mActive, isChecked).apply();
				showToast(defaultToast);
			}
		});

		((CheckBox) findViewById(R.id.dateCheckBox)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				sp.edit().putBoolean(Hook.mUseDate, isChecked).apply();
				if(isChecked)
					showToast("Put a folder's path in the \"TextBox\" above.");
				else
					showToast("Put a file's path in the \"TextBox\" above.");
				showToast(defaultToast);
			}
		});

		((CheckBox) findViewById(R.id.encryption)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(((EditText) findViewById(R.id.EncryptionKey)).getText().toString().length()!=16){
					Toast.makeText(MainActivity.this, "Enter a 16 Digit Key, the key you entered is not 16 digit.",Toast.LENGTH_SHORT).show();
					return;
				}
			}
		});

		findViewById(R.id.keySaveButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(((EditText) findViewById(R.id.EncryptionKey)).getText().toString().length()!=16){
					showToast("Enter a 16 Digit Key, the key you entered is not 16 digit.");
					return;
				}
				if(!((CheckBox)findViewById(R.id.encryption)).isChecked()){
                    showToast("Enable the encryption checkbox first and then Save.");
                    return;
                }
				sp.edit().putString(Hook.mEncryptKey, ((EditText) findViewById(R.id.EncryptionKey)).getText().toString()).apply();
				sp.edit().putBoolean(Hook.mEncrypt, ((CheckBox) findViewById(R.id.encryption)).isChecked()).apply();
				showToast("Key saved and encryption is now turned ON");
				showToast(defaultToast);
			}
		});



	}
	private void showToast(String text){
		Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
	}
}
