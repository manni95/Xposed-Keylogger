package com.sscsps.keyloggerPlus;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatCallback;
import android.widget.TextView;

public class logViewerActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_viewer);
        String key="";
        Intent intent = getIntent();
        String isEncrypted = intent.getStringExtra(MainActivity.ENCRYPTED_MESSAGE);
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
//if(isEncrypted!=null)
//        if(isEncrypted.equalsIgnoreCase("true")){
//            key = message.substring(0, 16);
//
//        }
//        message = formatMessage(message);
        TextView textView = (TextView) findViewById(R.id.textView4);
        textView.setText(message);
    }

    private String formatMessage(String message) {
        String row = "";
        String returnMessage = "";
        for(long i=0;i<message.length(); i++){
            try{
                if(message.charAt((int)i+9)=='@'){
                    returnMessage += row + "\n";
                    row = "";
                }
            }catch (ArrayIndexOutOfBoundsException a){
                //ignore as this will be the last record and it will we copied later.
            }
            row += "" + message.charAt((int)i);
        }
        returnMessage += row + "\n";
        return returnMessage;
    }

    void decrypt(String fileData, String userKey){
        String row = "";
        String DecryptedData = "";
        for(long i=0;i<fileData.length(); i++){
            try{
                if(fileData.charAt((int)i)=='x'){
                    row = Crypto.decryptL(row, userKey);
                    DecryptedData += row + "\n";
                    row = "";
                }
            }catch (ArrayIndexOutOfBoundsException a){
                //ignore as this will be the last record and it will we copied later.
            }
            row += "" + fileData.charAt((int)i);
        }
        row = Crypto.decryptL(row, userKey);
        DecryptedData += row + "\n";
    }
}
