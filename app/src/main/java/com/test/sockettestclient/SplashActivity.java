package com.test.sockettestclient;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "AlarmTest";
    private Context mContext;

    String ip_text, port_text;
    IntentThread intentThread = new IntentThread();
    ImageView iv;

    // SharedPreference를 통해 등록할 서버 IP, PORT 번호 변수
    public static String IP = "";
    public static String PORT = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // 액션바 제거
        ActionBar ac = getSupportActionBar();
        ac.hide();
        Log.e(TAG, "여기는 splashActivity");
        mContext = this;
        ip_text = PreferenceManager.getIpString(mContext, "ip");    // ip라는 키값에 value를 가지고옴 (value는 프래그먼트에서 edittext안에 값을 저장)
        port_text = PreferenceManager.getPortString(mContext, "port"); // port라는 키값에 value를 가지고옴 (value는 프래그먼트에서 edittext안에 값을 저장)

        // ip와 port키들의 value 값이 없으면 -> 저장 정보가 없다고 판단. 정보입력 다이얼로그 생성. 아니면 저장 정보가있는걸로 판단. 다이얼로그띄우지 않음.
        if (ip_text.length() == 0 || port_text.length() == 0){
            Log.e("msg", "저장된 데이터가 없습니다.");
            CustomDialog dialog = new CustomDialog(this);
            dialog.show();

        } else {
            IP = ip_text;
            PORT = port_text;
            Log.e("msg", "저장된 정보 : ip = " + ip_text + " port = " + port_text);
            intentThread.start();
        }
    }

    class IntentThread extends Thread{
        @Override
        public void run() {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Intent i = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(i);
            finish();
        }
    }

}