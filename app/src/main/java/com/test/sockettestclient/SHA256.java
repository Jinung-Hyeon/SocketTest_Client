package com.test.sockettestclient;


import android.util.Log;

import com.test.sockettestclient.constant.Constants;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA256 {

    public String encrypt(String password) throws NoSuchAlgorithmException {
        StringBuilder xorCirculator = new StringBuilder();
        // PASSWORD + SALT 와 임의 문자열을 XOR 연산
        for (int i = 0; i < password.length() && i< Constants.XOR_VARIABLE.length(); i++){
            xorCirculator.append((char)(password.charAt(i) ^ Constants.XOR_VARIABLE.charAt(i)));
        }

        Log.e("msg", "password : " + password);
        Log.e("msg", "xor 연산 후 password : " + xorCirculator);

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        //MessageDigest md2 = MessageDigest.getInstance("SHA-256");
        md.update(xorCirculator.toString().getBytes());
        //md2.update(xorCirculator.toString().getBytes());


        //Log.e("msg", "xor 연산 후 암호화 값 : " + bytesToHex(md2.digest()));

        return bytesToHex(md.digest());
    }

    // 10진수 문자열로 변경
    private String bytesToHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }
}
