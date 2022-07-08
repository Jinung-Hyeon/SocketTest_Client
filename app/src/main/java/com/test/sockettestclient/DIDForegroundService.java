package com.test.sockettestclient;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

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

    Handler handler;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler = new Handler();



        final String CHANNEL_ID = "DID Foreground Service ID";
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
                startForeground(1002, notification.build());
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

        if (ip.isEmpty() || port.isEmpty()) {
            Toast.makeText(this, "ip주소와 포트번호를 입력해주세요.", Toast.LENGTH_SHORT).show();

        } else {

            new Thread((Runnable) () -> {
                try {
                    //서버와 연결하는 소켓 생성
                    if(socket == null){
                        socket = new Socket(InetAddress.getByName(ip), Integer.parseInt(port));
                        is = new DataInputStream(socket.getInputStream());
                        os = new DataOutputStream(socket.getOutputStream());

                    }
                    //서버와 메세지를 주고받을 통로 구축

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


    private void _runOnUiThread(Runnable runnable){
        handler.post(runnable);
    }


}