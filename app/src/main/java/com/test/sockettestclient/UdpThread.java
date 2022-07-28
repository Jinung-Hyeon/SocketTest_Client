package com.test.sockettestclient;

import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


// 서버에 UDP 통신을 하기위한 쓰레드
public class UdpThread extends Thread{
    private static final String TAG = "AlarmTest";
    public static String toServerSignal = "0";

    @Override
    public void run() {

        //String msg = String.valueOf(System.currentTimeMillis());
        Log.d(TAG, "서버로 보낸 메시지 : " + toServerSignal);

        try {
            byte[] buf = toServerSignal.getBytes();
            InetAddress address = InetAddress.getByName("127.0.0.1");

            DatagramSocket socket = new DatagramSocket();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 5001);
            socket.send(packet);
        } catch (Exception e) {
            Log.d(TAG, "e msg: " + e.toString());
            e.printStackTrace();
        }
    }
}
