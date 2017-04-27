package com.sscsps.keyloggerPlus;

import java.io.File;

/**
 * Created by sscsps on 24/04/2017.
 * A not so strong Crypto Algorithm to break
 */

class Crypto {

    //encryption is done line by line with this method.
    static String encryptL(String text, String key){
        //encryption Logic.
        String result = "";
        int dataLength = text.length();
        int tempcode;
        int keySum = 0;
        for(int i=0;i<16;i++){
            keySum += (i * Integer.parseInt(Character.toString(key.charAt(i))));
        }
        for(int i=0; i< text.length(); i++){
            tempcode = (int)text.charAt(i) * (int)key.charAt(i%16) * keySum * dataLength;
            result += tempcode + "-";
        }
        return 'x' + result;    //'x' represents that the log record is encrypted.
    }

    //decryption is done line by line with this method.
    static String decryptL(String text, String key){
        //decryption Logic

        //leave the 'x' from the log record
        text = text.substring(1, text.length());
        String result = "";
        int keyIndex = 0;
        String tempcode = "";
        int keySum = 0;
        for(int i=0;i<16;i++) {
            keySum += (i * Integer.parseInt(Character.toString(key.charAt(i))));
        }
        int dataLength = 0;
        for(int i=0; i<text.length();i++){
            if(text.charAt(i)=='-'){
                dataLength++;
            }
        }
        int letter;
        for(int i = 0; i<text.length(); i++){
            if(text.charAt(i)=='-'){
                letter = Integer.parseInt(tempcode);
                letter/=(int)key.charAt(keyIndex%16);
                letter/=keySum;
                letter/=dataLength;
                keyIndex++;
                tempcode="";
                result+=(char)letter;
                continue;
            }
            tempcode+=text.charAt(i);
        }
        return result;
    }
}
