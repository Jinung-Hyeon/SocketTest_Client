package com.test.sockettestclient.constant;

import com.test.sockettestclient.MainActivity;
import com.test.sockettestclient.SplashActivity;

public class Constants {
    public static final String PASSWORD = "123456"; // 비밀번호
    public static final String SALT = "gCjhqPIi72A8nA=="; // SALT 문자열
    public static final String XOR_VARIABLE = "=TeoZk631kdj/vo="; //XOR 연산에 사용할 문자열
    public static final String REQUEST_URL = "http://" + SplashActivity.IP + ":" + SplashActivity.PORT + "/lifesupporter/api/did/broadcast?androidId=" + MainActivity.ANDROID_ID;
    public static final String BASE_URL = "http://" + SplashActivity.IP + ":" + SplashActivity.PORT + "/lifesupporter/";
    public static final String IMAGE_URL = BASE_URL + "resources/images/contents/";
    public static final String ADD_SALT_PASSWORD = PASSWORD + SALT;


}