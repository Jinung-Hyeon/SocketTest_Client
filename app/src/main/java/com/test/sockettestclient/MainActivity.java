package com.test.sockettestclient;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "AlarmTest";


    //화면 킬 시간 변수
    public static final int WAKEUP_HOUR = 9;
    public static final int WAKEUP_MINIUTE = 25;
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

    FinishWork fw = new FinishWork();

    public Socket socket;     //클라이언트의 소켓
    public DataInputStream is;
    public DataOutputStream os;
    public static int endSignal = 0;

    private long backKeyPressedTime = 0;
    private Toast toast;

    SocketThread socketthread;
    UdpThread udpThread;
    //TimerThread timerThread;

    public ImageView imageView;

    static int toggle = 0;


    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume!!" );



        if(System.currentTimeMillis() < fw.makeStartWorkTime(GOTOSLEEP_HOUR, GOTOSLEEP_MINIUTE, GOTOSLEEP_SECOND, GOTOSLEEP_MILISECOND)){
            // 화면 켜주는 기능
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

            // 화면 절전 모드 해제
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            Log.e(TAG, "화면 계속 켜기 ON!");

            Log.e(TAG, "일과시간입니다. 소켓을 실행합니다.");
            socketthread = new SocketThread();
            socketthread.start();
        }

        Alarm alarm = new Alarm();
        IntentFilter filter = new IntentFilter();
        if (toggle == 0){
            filter.addAction(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            toggle++;
        }
        registerReceiver(alarm, filter);

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

        imageView = findViewById(R.id.imageView);

        // 화면 절전 모드 해제
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        ActionBar ac = getSupportActionBar();
        ac.setTitle("2022년 지역 SW서비스사업화 사업 [지역현안해결형 SW개발]");


        // 알람 브로드캐스트 리시버에서 intent로 넘어오면 intent값을 받아서 처리리
        Intent intent = getIntent();
        String wakeUpValue = intent.getStringExtra("KeepScreenOn");
        String goToSleepValue = intent.getStringExtra("ScreenOff");
        Log.e(TAG, "wakeUpValue : " + wakeUpValue + ", goToSleepValue : " + goToSleepValue);



        if (goToSleepValue != null){ // 일과 시간 종료 알람 받고 넘오면 처리할 조건문. 화면 절전 모드 진입 시키고 5분뒤 TV꺼짐
            if (goToSleepValue.equals("ScreenOff")) {
                // 화면 절전 모드 진입
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                Toast.makeText(this, "잠시 후 화면이 종료됩니다.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "잠시 후 화면이 종료됩니다.");
            }
        } else if (wakeUpValue != null){
            if (wakeUpValue.equals("KeepScreenOn")) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                Log.e(TAG, "화면 계속 켜기 ON!");
            }
        } else if (wakeUpValue == null && goToSleepValue == null) { // 앱이 잘못 실행되어 시작, 종료 값이 없을때를 대비해 만든 조건문.
            Intent sendGoToSleepAlarmIntent = new Intent(this, GoToSleepAlarm.class);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, GOTOSLEEP_HOUR);
            c.set(Calendar.MINUTE, GOTOSLEEP_MINIUTE);
            c.set(Calendar.SECOND, GOTOSLEEP_SECOND);
            c.set(Calendar.MILLISECOND, GOTOSLEEP_MILISECOND);

            if(c.before(Calendar.getInstance())){
                c.add(Calendar.DATE, 1);
            }

            Log.e(TAG, "GoToSleep 알람 예약!!! 꺼짐 예약 시간 : " + dateFormat.format(c.getTimeInMillis()) + " || 현재시간 : " + dateFormat.format(System.currentTimeMillis()));
            PendingIntent goToSleepPendingIntent = PendingIntent.getBroadcast(this, 0, sendGoToSleepAlarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager goToSleepAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            AlarmManager.AlarmClockInfo goToSleepAc = new AlarmManager.AlarmClockInfo(c.getTimeInMillis(), goToSleepPendingIntent);
            goToSleepAlarmManager.setAlarmClock(goToSleepAc, goToSleepPendingIntent);

        }

    }

    class SocketThread extends Thread{
        @Override
        public void run() {
            if (System.currentTimeMillis() > fw.makeStartWorkTime(GOTOSLEEP_HOUR, GOTOSLEEP_MINIUTE, GOTOSLEEP_SECOND,GOTOSLEEP_MILISECOND)) {
                Log.e(TAG, "업무시간이 지나서 소켓을 실행하지 않습니다. ");
            } else {
                try {
                    //서버와 연결하는 소켓 생성
                    if (socket == null || socket.isClosed()){
                        socket = new Socket(InetAddress.getByName(ip), Integer.parseInt(port));
                        is = new DataInputStream(socket.getInputStream());
                        os = new DataOutputStream(socket.getOutputStream());
                    }
                    os.write(endSignal);
                    os.flush();
                    //is.close();
                    //socket.close();
                } catch (Exception e) {
                    Log.e(TAG, " 서버 소켓이 안열려 있는듯? WatchDog 죽은거아님? 다시실행 ㄱㄱ : " + e.toString());
                    // WatchDog앱이 죽어있을경우 앱을 다시 실행시켜줌.
                    getPackageList();
                    if(!socket.isClosed() || socket != null){
                        try {
                            is.close();
                            os.flush();
                            socket.close();
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    }
                }
            }
        }
    }


    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        // 홈 버튼 누르면
        Log.d(TAG, "homeKey: ");
        //getPackageList();

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "onPause!!");
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean moveTaskToBack(boolean nonRoot) {
        return super.moveTaskToBack(nonRoot);
    }

    @Override
    public void finishAndRemoveTask() {
        super.finishAndRemoveTask();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        backKeyPressed("뒤로가기 버튼을 한번 더 누르면 종료됩니다.", 5);
    }

    public void backKeyPressed(String msg, double time) {
        if (System.currentTimeMillis() > backKeyPressedTime + (time * 1000)) {
            try {
                UdpThread.toServerSignal = "1";
                udpThread = new UdpThread();
                udpThread.start();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            backKeyPressedTime = System.currentTimeMillis();
            showGuide(msg);
            return;
        }

        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            udpThread.interrupt();
            moveTaskToBack(true);
            finishAndRemoveTask();
            android.os.Process.killProcess(android.os.Process.myPid());
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
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy");
    }

    public void getPackageList() {
        //SDK30이상은 Manifest권한 추가가 필요 출처:https://inpro.tistory.com/214
        PackageManager pkgMgr = getPackageManager();
        List<ResolveInfo> mApps;
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mApps = pkgMgr.queryIntentActivities(mainIntent, 0);

        try {
            for (int i = 0; i < mApps.size(); i++) {
                if(mApps.get(i).activityInfo.packageName.startsWith("com.test.sokettestserver")){
                    Log.d(TAG, "실행시킴");
                    break;
                }
            }
            Intent intent = getPackageManager().getLaunchIntentForPackage("com.test.sokettestserver");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}