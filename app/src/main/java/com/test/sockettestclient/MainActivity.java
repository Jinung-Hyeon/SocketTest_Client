package com.test.sockettestclient;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "simpleTest";

    //화면 킬 시간 변수
    public static final int WAKEUP_HOUR = 8;
    public static final int WAKEUP_MINIUTE = 20
            ;
    public static final int WAKEUP_SECOND = 0;
    public static final int WAKEUP_MILISECOND = 0;

    //화면 끌 시간 변수
    public static final int GOTOSLEEP_HOUR = 17;
    public static final int GOTOSLEEP_MINIUTE = 24;
    public static final int GOTOSLEEP_SECOND = 0;
    public static final int GOTOSLEEP_MILISECOND = 0;

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
    protected void onResume() {
        super.onResume();


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

        //ClientSocketOpen(endSignal);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        Intent intent = getIntent();
        String wakeUpValue = intent.getStringExtra("KeepScreenOn");
        String goToSleepValue = intent.getStringExtra("ScreenOff");
        Log.e(TAG, "wakeUpValue : " + wakeUpValue + ", goToSleepValue : " + goToSleepValue);

        if (goToSleepValue != null){
            if (goToSleepValue.equals("ScreenOff")) {
                Toast.makeText(this, "잠시 후 화면이 종료됩니다.", Toast.LENGTH_SHORT).show();
            }
        } else if (wakeUpValue != null){
            if (wakeUpValue.equals("KeepScreenOn")) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                Log.e(TAG, "화면 계속 켜기 ON!");
            }
        } else if (wakeUpValue == null && goToSleepValue == null) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            Calendar c = Calendar.getInstance();
            Intent sendWakeUpAlarmIntent = new Intent(this, WakeUpAlarm.class);

            c.set(Calendar.HOUR_OF_DAY, WAKEUP_HOUR);
            c.set(Calendar.MINUTE, WAKEUP_MINIUTE);
            c.set(Calendar.SECOND, WAKEUP_SECOND);
            c.set(Calendar.MILLISECOND, WAKEUP_MILISECOND);
            Log.e(TAG, "WakeUp 알람 예약!!! time : " + c.getTime() + " getTimeInMillis : " + c.getTimeInMillis() + " currentTime : " + System.currentTimeMillis());

            if(c.before(Calendar.getInstance())){
                c.add(Calendar.DATE, 1);
            }

            PendingIntent wakeUpPendingIntent = PendingIntent.getBroadcast(this, 0, sendWakeUpAlarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager wakeUpAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            AlarmManager.AlarmClockInfo wakeUpAc = new AlarmManager.AlarmClockInfo(c.getTimeInMillis(), wakeUpPendingIntent);
            wakeUpAlarmManager.setAlarmClock(wakeUpAc, wakeUpPendingIntent);

            Log.e(TAG, "화면 계속 켜기 ON!");
        }

        startForeground();

        Alarm alarm = new Alarm();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(alarm, filter);

    }

//    public void ClientSocketOpen(int endSignal) {
//        if (ip.isEmpty() || port.isEmpty()) {
//            Toast.makeText(this, "ip주소와 포트번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
//        } else {
//            if(endSignal != 1) {
//                //Toast.makeText(MainActivity.this, "서버 접속완료", Toast.LENGTH_SHORT).show();
//            }
//            new Thread((Runnable) () -> {
//                try {
//                    //서버와 연결하는 소켓 생성
//                    if(endSignal == 0 && socket == null){
//                        socket = new Socket(InetAddress.getByName(ip), Integer.parseInt(port));
//                        //서버와 메세지를 주고받을 통로 구축
//                        is = new DataInputStream(socket.getInputStream());
//                        os = new DataOutputStream(socket.getOutputStream());
//                        MainActivity.this.runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                Toast.makeText(MainActivity.this, "\"스마트 AI 돌봄 시스템\"이 실행되었습니다.", Toast.LENGTH_SHORT).show();
//                            }
//                        });
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                try {
//
//                    if (endSignal == 2) {
//                        os.write(endSignal);
//                        os.flush();
//                        Log.e(TAG, "signal: " + endSignal);
//                    }
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }).start();
//        }
//    }


    @Override
    public boolean moveTaskToBack(boolean nonRoot) {
        return super.moveTaskToBack(nonRoot);
    }

    @Override
    public void finishAndRemoveTask() {
        super.finishAndRemoveTask();
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        DIDForegroundService didForegroundService = new DIDForegroundService();
        didForegroundService.ClientSocketOpen(2);
        Log.d(TAG, "homeKey: ");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "onPause!!");
        //onDestroy();
    }

    @Override
    public void onBackPressed() {
        backKeyPressed("뒤로가기 버튼을 한번 더 누르면 종료됩니다.", 5);
        Log.e(TAG, "소켓 연결 끊김? : " + socket.isConnected());

    }

    public void backKeyPressed(String msg, double time) {
        if (System.currentTimeMillis() > backKeyPressedTime + (time * 1000)) {
            backKeyPressedTime = System.currentTimeMillis();
            showGuide(msg);
            return;
        }

        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            onDestroy();
            toast.cancel();
        }
    }

    private void showGuide(String msg) {
        toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        try {
            socket.close();
            ActivityCompat.finishAffinity(this);
            System.exit(0);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void startForeground(){
        if(!foregroundServiceRunning()){
            Intent serviceIntent = new Intent(this, DIDForegroundService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                startForegroundService(serviceIntent);
            }
        }
    }

    //포그라운드 서비스가 실행중인지 확인하는 메소드
    public boolean foregroundServiceRunning() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service: activityManager.getRunningServices(Integer.MAX_VALUE)){
            if (DIDForegroundService.class.getName().equals(service.service.getClassName())){
                return true;
            }
        }
        return false;
    }

}