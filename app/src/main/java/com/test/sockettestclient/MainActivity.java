package com.test.sockettestclient;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Calendar;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "AlarmTest";


    //화면 킬 시간 변수
    public static final int WAKEUP_HOUR = 8;
    public static final int WAKEUP_MINIUTE = 30;
    public static final int WAKEUP_SECOND = 0;
    public static final int WAKEUP_MILISECOND = 0;

    //화면 끌 시간 변수
    public static final int GOTOSLEEP_HOUR = 17;
    public static final int GOTOSLEEP_MINIUTE = 24;
    public static final int GOTOSLEEP_SECOND = 0;
    public static final int GOTOSLEEP_MILISECOND = 0;
    private static final int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 1;

    //로컬 IP사용시 127.0.0.1로 ip 변경하기
    private final String ip = "127.0.0.1";
    //private final String ip = "10.0.2.16";  // 에뮬레이터 ip
    private final String port = "5001";



    public Socket socket;     //클라이언트의 소켓
    public DataInputStream is;
    public DataOutputStream os;
    int endSignal = 0;

    private long backKeyPressedTime = 0;
    private Toast toast;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.e(TAG, "onNewIntent!!");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume!!" );
        try {
            overridePendingTransition(0,0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ActionBar ac = getSupportActionBar();
        ac.setTitle("2022년 지역 SW서비스사업화 사업 [지역현안해결형 SW개발]");

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        Intent intent = getIntent();
        String wakeUpValue = intent.getStringExtra("KeepScreenOn");
        String goToSleepValue = intent.getStringExtra("ScreenOff");
        Log.e(TAG, "wakeUpValue : " + wakeUpValue + ", goToSleepValue : " + goToSleepValue);

        if (goToSleepValue != null){
            if (goToSleepValue.equals("ScreenOff")) {
                Toast.makeText(this, "잠시 후 화면이 종료됩니다.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "잠시 후 화면이 종료됩니다.");
            }
        } else if (wakeUpValue != null){
            if (wakeUpValue.equals("KeepScreenOn")) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                Log.e(TAG, "화면 계속 켜기 ON!");
            }
        } else if (wakeUpValue == null && goToSleepValue == null) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

//            Calendar c = Calendar.getInstance();
//            Intent sendGoToSleepAlarmIntent = new Intent(this, GoToSleepAlarm.class);
//
//            c.set(Calendar.HOUR_OF_DAY, GOTOSLEEP_HOUR);
//            c.set(Calendar.MINUTE, GOTOSLEEP_MINIUTE);
//            c.set(Calendar.SECOND, GOTOSLEEP_SECOND);
//            c.set(Calendar.MILLISECOND, GOTOSLEEP_MILISECOND);
//            Log.e(TAG, "GoToSleep 알람 예약!!! time : " + c.getTime() + " getTimeInMillis : " + c.getTimeInMillis() + " currentTime : " + System.currentTimeMillis());
//
//            if(c.before(Calendar.getInstance())){
//                c.add(Calendar.DATE, 1);
//            }
//
//            PendingIntent goToSleepPendingIntent = PendingIntent.getBroadcast(this, 0, sendGoToSleepAlarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
//            AlarmManager goToSleepAlarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
//            AlarmManager.AlarmClockInfo goToSleepAc = new AlarmManager.AlarmClockInfo(c.getTimeInMillis(), goToSleepPendingIntent);
//            goToSleepAlarmManager.setAlarmClock(goToSleepAc, goToSleepPendingIntent);

            Log.e(TAG, "화면 계속 켜기 ON!");
        }

        //브로드캐스트 리시버 사용시 액티비티 띄우지 못한 문제 Overlay View로 해결
        //다른 앱 위에 그리기 허용 체크 해야함.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {   // 마시멜로우 이상일 경우
            if (!Settings.canDrawOverlays(this)) {              // 다른앱 위에 그리기 체크
                Uri uri = Uri.fromParts("package" , getPackageName(), null);
                Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, uri);
                startActivityForResult(i, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
            } else {
                ClientSocketOpen(endSignal);
            }
        } else {
            ClientSocketOpen(endSignal);
        }

        Alarm alarm = new Alarm();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(alarm, filter);

    }

    public void ClientSocketOpen(int endSignal) {
        if (ip.isEmpty() || port.isEmpty()) {
            Toast.makeText(this, "ip주소와 포트번호를 입력해주세요.", Toast.LENGTH_SHORT).show();

        } else {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //서버와 연결하는 소켓 생성
                        if (socket == null){
                            socket = new Socket(InetAddress.getByName(ip), Integer.parseInt(port));
                            is = new DataInputStream(socket.getInputStream());
                            os = new DataOutputStream(socket.getOutputStream());
                        }

                        if (endSignal == 2) {
                            os.write(endSignal);
                            os.flush();
                            Log.e(TAG, "signal: " + endSignal);
                        }
                        socket.close();
                    } catch (Exception e) {
                        if(!socket.isClosed()){
                            try {


                                socket.close();
                            } catch (IOException ioException) {
                                ioException.printStackTrace();
                            }
                        }
                    }
                }
            }).start();
        }
    }


    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        ClientSocketOpen(2);
        Log.d(TAG, "homeKey: ");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "onPause!!");
    }

    @Override
    public void onBackPressed() {
        backKeyPressed("뒤로가기 버튼을 한번 더 누르면 종료됩니다.", 5);
    }

    public void backKeyPressed(String msg, double time) {
        if (System.currentTimeMillis() > backKeyPressedTime + (time * 1000)) {
            backKeyPressedTime = System.currentTimeMillis();
            showGuide(msg);
            return;
        }

        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            ActivityCompat.finishAffinity(this);
            System.exit(0);
            toast.cancel();
        }
    }

    private void showGuide(String msg) {
        toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(TAG, "onStart!! ");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e(TAG, "onRestart!!" );
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG, "onStop!!");
    }

    //    @Override
//    protected void onDestroy() {
//        Log.e(TAG, "onDestroy");
//        try {
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        super.onDestroy();
//
//    }

}