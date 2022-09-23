package com.test.sockettestclient.worktime;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.test.sockettestclient.MainActivity;
import com.test.sockettestclient.socket.UdpThread;

import java.text.SimpleDateFormat;

public class Alarm extends BroadcastReceiver {
    private static final String TAG = "AlarmTest";
    private Intent i;
    private com.test.sockettestclient.worktime.WorkTime workTime;



    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        i = new Intent(context, MainActivity.class);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        workTime = new com.test.sockettestclient.worktime.WorkTime();

        if(action != null) {
            switch (action) {
                case Intent.ACTION_SCREEN_OFF:
                    UdpThread.toServerSignal = "2";
                    UdpThread udpThread = new UdpThread();
                    udpThread.start();

                    Log.e(TAG, "SCREEN_OFF");
                    if (System.currentTimeMillis() < workTime.finishWorkTime().getTimeInMillis()){
                        Log.e(TAG, "현재 시간 : " + dateFormat.format(System.currentTimeMillis()) + " 종료 예약 시간 : " + dateFormat.format(workTime.finishWorkTime().getTimeInMillis()));
                        Log.e(TAG, "아직 일과 종료시간 전입니다. 다시 화면을 실행합니다.");
                        i.putExtra("turnOn","turnOn");
                        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        context.startActivity(i);
                    }
                    break;
            }
        }
    }


}
