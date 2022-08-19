package com.test.sockettestclient.worktime;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.test.sockettestclient.MainActivity;
import com.test.sockettestclient.socket.UdpThread;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Alarm extends BroadcastReceiver {
    private static final String TAG = "AlarmTest";
    private Intent i, sendWakeUpAlarmIntent, sendGoToSleepAlarmIntent;
    private WorkTime workTime;



    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        i = new Intent(context, MainActivity.class);
        sendWakeUpAlarmIntent = new Intent(context, WakeUpAlarm.class);
        sendGoToSleepAlarmIntent = new Intent(context, GoToSleepAlarm.class);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        workTime = new WorkTime();
        Calendar wakeUp = workTime.startWorkTime();
        Calendar sleep = workTime.finishWorkTime();

        if(action != null) {
            switch (action) {
                case Intent.ACTION_SCREEN_ON:
                    Log.e(TAG, "SCREEN_ON");
                    Log.e(TAG, "GoToSleep 알람 예약!!! 꺼짐 예약 시간 : " + dateFormat.format(workTime.finishWorkTime().getTimeInMillis()) + " || 현재시간 : " + dateFormat.format(System.currentTimeMillis()));

                    if(workTime.finishWorkTime().before(Calendar.getInstance())){
                        sleep.add(Calendar.DATE, 1);
                    }

                    PendingIntent goToSleepPendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, sendGoToSleepAlarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                    AlarmManager goToSleepAlarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
                    AlarmManager.AlarmClockInfo goToSleepAc = new AlarmManager.AlarmClockInfo(sleep.getTimeInMillis(), goToSleepPendingIntent);
                    goToSleepAlarmManager.setAlarmClock(goToSleepAc, goToSleepPendingIntent);
                    break;
                case Intent.ACTION_SCREEN_OFF:
                    UdpThread.toServerSignal = "2";
                    UdpThread udpThread = new UdpThread();
                    udpThread.start();

                    Log.e(TAG, "SCREEN_OFF");

                    if (System.currentTimeMillis() >= workTime.finishWorkTime().getTimeInMillis()){  // 현재시간 > 일과종료시간 -> 일과종료후 화면이 꺼진것이므로 다음날 아침에 화면 깨울 시간 예약
                        if(workTime.startWorkTime().before(Calendar.getInstance())){
                            wakeUp.add(Calendar.DATE, 1);
                            //Log.e(TAG, dateFormat.format(c.getTimeInMillis()));
                        }

                        Log.e(TAG, "WakeUp 알람 예약!!! 켜짐 예약 시간 : " + dateFormat.format(wakeUp.getTimeInMillis()) + " || 현재시간 : " + dateFormat.format(System.currentTimeMillis()));
                        PendingIntent wakeUpPendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 1, sendWakeUpAlarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                        AlarmManager wakeUpAlarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
                        AlarmManager.AlarmClockInfo wakeUpAc = new AlarmManager.AlarmClockInfo(wakeUp.getTimeInMillis(), wakeUpPendingIntent);
                        wakeUpAlarmManager.setAlarmClock(wakeUpAc, wakeUpPendingIntent);
                        //context.startActivity(i);
                    } else if (System.currentTimeMillis() < workTime.finishWorkTime().getTimeInMillis()){
                        Log.e(TAG, "현재 시간 : " + dateFormat.format(System.currentTimeMillis()) + " 종료 예약 시간 : " + dateFormat.format(workTime.finishWorkTime().getTimeInMillis()));
                        Log.e(TAG, "아직 일과 종료시간 전입니다. 다시 화면을 실행합니다.");
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(i);
                    }
                    break;
            }
        }
    }


}
