package com.test.sockettestclient;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "simpleTest";

    //화면 킬 시간 변수
    public static final int WAKEUP_HOUR = 8;
    public static final int WAKEUP_MINIUTE = 16;
    public static final int WAKEUP_SECOND = 0;
    public static final int WAKEUP_MILISECOND = 0;


    //화면 끌 시간 변수
    public static final int GOTOSLEEP_HOUR = 8;
    public static final int GOTOSLEEP_MINIUTE = 17;
    public static final int GOTOSLEEP_SECOND = 0;
    public static final int GOTOSLEEP_MILISECOND = 0;

    //로컬 IP사용시 127.0.0.1로 ip 변경하기
    //private final String ip = "127.0.0.1";
    private final String ip = "10.0.2.16";  // 에뮬레이터 ip
    private final String port = "5001";
    int endSignal = 0;

    Socket socket;     //클라이언트의 소켓
    Button btn_end;
    DataInputStream is;
    DataOutputStream os;


    @Override
    protected void onResume() {
        super.onResume();

        ClientSocketOpen(endSignal);
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

        //btn_end = findViewById(R.id.btn_end);
        //ClientSocketOpen(endSignal);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);



/*        btn_end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endSignal = 2;
                ClientSocketOpen(endSignal);
            }
        });*/


        Intent intent = getIntent();
        String wakeUpValue = intent.getStringExtra("KeepScreenOn");
        String goToSleepValue = intent.getStringExtra("ScreenOff");
        Log.e(TAG, "wakeUpValue : " + wakeUpValue + ", goToSleepValue : " + goToSleepValue);
        if (wakeUpValue != null){
            if (wakeUpValue.equals("KeepScreenOn")) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                Log.e(TAG, "화면 계속 켜기 ON!");
            }
        } else if (goToSleepValue != null){
            if (goToSleepValue.equals("ScreenOff")) {
                Toast.makeText(this, "잠시 후 화면이 종료됩니다.", Toast.LENGTH_SHORT).show();
            }
        }

        Alarm alarm = new Alarm();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(alarm, filter);

    }


    @Override
    protected void onStop() {
        super.onStop();
        /*
        try {
            socket.close(); //소켓을 닫는다.
            moveTaskToBack(true); // 태스크를 백그라운드로 이동
            finishAndRemoveTask(); // 액티비티 종료 + 태스크 리스트에서 지우기
            android.os.Process.killProcess(android.os.Process.myPid()); //앱 프로세스 종료
        } catch (IOException e) {
            e.printStackTrace();
        }
         */
    }

    public void ClientSocketOpen(int endSignal) {
        if (ip.isEmpty() || port.isEmpty()) {
            Toast.makeText(MainActivity.this, "ip주소와 포트번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
        } else {
            if(endSignal != 1) {
                //Toast.makeText(MainActivity.this, "서버 접속완료", Toast.LENGTH_SHORT).show();
            }
            new Thread((Runnable) () -> {
                try {
                    //서버와 연결하는 소켓 생성
                    if(endSignal == 0 && socket == null){
                        socket = new Socket(InetAddress.getByName(ip), Integer.parseInt(port));
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "\"스마트 AI 노인 돌봄시스템\"이 준비되었습니다.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    //서버와 메세지를 주고받을 통로 구축
                    is = new DataInputStream(socket.getInputStream());
                    os = new DataOutputStream(socket.getOutputStream());

                    if (endSignal == 2) {
                        os.write(endSignal);
                        os.flush();
                        //socket.close();
                        //moveTaskToBack(true); // 태스크를 백그라운드로 이동
                        //finishAndRemoveTask(); // 액티비티 종료 + 태스크 리스트에서 지우기
                        //android.os.Process.killProcess(android.os.Process.myPid()); //앱 프로세스 종료
                        Log.d(TAG, "signal: " + endSignal);
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
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
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        //getPackageList();
        ClientSocketOpen(2);
        Log.d(TAG, "homeKey: ");
    }

    //다른 앱을 실행시켜주는 메소드
    public void getPackageList() {
        if(endSignal == 0){
            //SDK30이상은 Manifest권한 추가가 필요 출처:https://inpro.tistory.com/214
            PackageManager pkgMgr = getPackageManager();
            List<ResolveInfo> mApps;
            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            mApps = pkgMgr.queryIntentActivities(mainIntent, 0);

            try {
                for (int i = 0; i < mApps.size(); i++) {
                    if(mApps.get(i).activityInfo.packageName.startsWith("com.test.sokettestserver")){
                        Log.d("start", "실행시킴");
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
    } //getPackageList()

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "onPause!!");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ClientSocketOpen(2);
    }
}