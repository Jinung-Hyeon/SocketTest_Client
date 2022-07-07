package com.test.sockettestclient;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Calendar;
import java.util.List;

public class DIDForegroundService extends Service {

    private static final String TAG = "ServerTest";

    //로컬 IP사용시 127.0.0.1로 ip 변경하기
    private final String ip = "127.0.0.1";
    //private final String ip = "10.0.2.16";  // 에뮬레이터 ip
    private final String port = "5001";

    public Socket socket;     //클라이언트의 소켓
    public DataInputStream is;
    public DataOutputStream os;
    int endSignal = 0;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        final String CHANNEL_ID = "Foreground Service ID";
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_ID,
                NotificationManager.IMPORTANCE_LOW
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getSystemService(NotificationManager.class).createNotificationChannel(channel);
                Notification.Builder notification = new Notification.Builder(this, CHANNEL_ID)
                        .setContentText("Service is running")
                        .setContentTitle("Service enabled")
                        .setSmallIcon(R.drawable.ic_launcher_background);
                startForeground(1001, notification.build());
            }
        }



        try {
            ClientSocketOpen(endSignal);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.START_STICKY;

    }


    public void ClientSocketOpen(int endSignal) {
        Toast.makeText(this, "\"스마트 AI 돌봄 시스템\"이 실행되었습니다.", Toast.LENGTH_SHORT).show();
        if (ip.isEmpty() || port.isEmpty()) {
            Toast.makeText(this, "ip주소와 포트번호를 입력해주세요.", Toast.LENGTH_SHORT).show();

        } else {
            if(endSignal != 1) {
                //Toast.makeText(MainActivity.this, "서버 접속완료", Toast.LENGTH_SHORT).show();
            }

            new Thread((Runnable) () -> {
                try {
                    //서버와 연결하는 소켓 생성
                    if(endSignal == 0 && socket == null){
                        socket = new Socket(InetAddress.getByName(ip), Integer.parseInt(port));
                        //서버와 메세지를 주고받을 통로 구축
                        is = new DataInputStream(socket.getInputStream());
                        os = new DataOutputStream(socket.getOutputStream());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {

                    if (endSignal == 2) {
                        os.write(endSignal);
                        os.flush();
                        Log.e(TAG, "signal: " + endSignal);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }


    //다른 앱을 실행시켜주는 메소드
    public void getPackageList() {
        //SDK30이상은 Manifest권한 추가가 필요 출처:https://inpro.tistory.com/214
        PackageManager pkgMgr = getPackageManager();
        List<ResolveInfo> mApps;
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mApps = pkgMgr.queryIntentActivities(mainIntent, 0);

        try {
            for (int i = 0; i < mApps.size(); i++) {
                if(mApps.get(i).activityInfo.packageName.startsWith("com.test.sockettestclient")){
                    Log.d(TAG, "실행시킴");
                    break;
                }
            }
            Intent intent = getPackageManager().getLaunchIntentForPackage("com.test.sockettestclient");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}