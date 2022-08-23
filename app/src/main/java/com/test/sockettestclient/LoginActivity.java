package com.test.sockettestclient;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.test.sockettestclient.logininfo.CustomDialog;
import com.test.sockettestclient.logininfo.PreferenceManager;
import com.test.sockettestclient.worktime.RegistAlarm;
import com.test.sockettestclient.worktime.WorkTime;

import java.text.SimpleDateFormat;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "AlarmTest";
    private Context mContext;
    private WorkTime workTime;

    // 고유 식별자 아이디
    public static String ANDROID_ID = "";

    String ip_text;
    IntentThread intentThread = new IntentThread();

    // SharedPreference를 통해 등록할 서버 IP, PORT 번호 변수
    public static String IP = "";

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume (SplashActivity)");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        workTime = new WorkTime();


        ANDROID_ID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        //Log.e(TAG, "ANDROID_ID : " + ANDROID_ID + " (LoginActivity)");

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Log.e(TAG, "예약시간" + dateFormat.format(workTime.finishWorkTime().getTimeInMillis()) + " || 현재시간 : " + dateFormat.format(System.currentTimeMillis()) );


        // 액션바 제거
        ActionBar ac = getSupportActionBar();
        ac.hide();
        Log.e(TAG, "여기는 splashActivity");
        mContext = this;
        ip_text = PreferenceManager.getIpString(mContext, "ip");    // ip라는 키값에 value를 가지고옴 (value는 프래그먼트에서 edittext안에 값을 저장)

        // ip와 port키들의 value 값이 없으면 -> 저장 정보가 없다고 판단. 정보입력 다이얼로그 생성. 아니면 저장 정보가있는걸로 판단. 다이얼로그띄우지 않음.
        if (ip_text.length() == 0){
            Log.e("msg", "저장된 데이터가 없습니다.");
            CustomDialog dialog = new CustomDialog(this);
            dialog.show();

        } else {
            IP = ip_text;
            Log.e("msg", "저장된 정보 : ip = " + ip_text);
            if(System.currentTimeMillis() > workTime.finishWorkTime().getTimeInMillis()){ // 현재시간 > 일과종료시간 -> 일과종료 이후 조건문 : 화면 계속 켜기나 소켓통신을 하지않음
                Log.e(TAG, "업무시간이 아닙니다. (일과종료 이후 조건문)");
                finish();
            } else if (System.currentTimeMillis() < workTime.startWorkTime().getTimeInMillis()) { // 현재시간 < 일과시작시간 -> 일과 시작전 동작 조건문 : 화면 계속 켜기나 소켓통신을 하지않음
                Log.e(TAG, "업무시간이 아닙니다. (일과시작전 조건문)");
                finish();
            } else {
                intentThread.start();
            }
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
            // 알람 인텐트 등록
            RegistAlarm ra = new RegistAlarm(LoginActivity.this, LoginActivity.this);
            ra.RegistSleepAlarm();
            ra.RegistWakeUpAlarm();

            Intent i = new Intent(LoginActivity.this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(i);
            finish();
        }
    }

}