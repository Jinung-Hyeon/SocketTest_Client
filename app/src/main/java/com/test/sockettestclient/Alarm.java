package com.test.sockettestclient;

import static com.test.sockettestclient.MainActivity.GOTOSLEEP_HOUR;
import static com.test.sockettestclient.MainActivity.GOTOSLEEP_MILISECOND;
import static com.test.sockettestclient.MainActivity.GOTOSLEEP_MINIUTE;
import static com.test.sockettestclient.MainActivity.GOTOSLEEP_SECOND;
import static com.test.sockettestclient.MainActivity.WAKEUP_HOUR;
import static com.test.sockettestclient.MainActivity.WAKEUP_MILISECOND;
import static com.test.sockettestclient.MainActivity.WAKEUP_MINIUTE;
import static com.test.sockettestclient.MainActivity.WAKEUP_SECOND;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;

public class Alarm extends BroadcastReceiver {
    private static final String TAG = "AlarmTest";
    private Intent i, sendWakeUpAlarmIntent, sendGoToSleepAlarmIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        i = new Intent(context, MainActivity.class);
        sendWakeUpAlarmIntent = new Intent(context, WakeUpAlarm.class);
        sendGoToSleepAlarmIntent = new Intent(context, GoToSleepAlarm.class);

        Calendar c = Calendar.getInstance();

        if(action != null) {
            switch (action) {
                case Intent.ACTION_BOOT_COMPLETED:
                    Toast.makeText(context, "BOOT_COMPLETED", Toast.LENGTH_SHORT).show();
                    c.set(Calendar.HOUR_OF_DAY, GOTOSLEEP_HOUR);
                    c.set(Calendar.MINUTE, GOTOSLEEP_MINIUTE);
                    c.set(Calendar.SECOND, GOTOSLEEP_SECOND);
                    c.set(Calendar.MILLISECOND, GOTOSLEEP_MILISECOND);
                    Log.e(TAG, "GoToSleep 알람 예약!!! time : " + c.getTime() + " getTimeInMillis : " + c.getTimeInMillis() + " currentTime : " + System.currentTimeMillis());

                    if(c.before(Calendar.getInstance())){
                        c.add(Calendar.DATE, 1);
                    }

                    PendingIntent bootPendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, sendGoToSleepAlarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                    AlarmManager bootAlarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
                    AlarmManager.AlarmClockInfo bootAc = new AlarmManager.AlarmClockInfo(c.getTimeInMillis(), bootPendingIntent);
                    bootAlarmManager.setAlarmClock(bootAc, bootPendingIntent);
                    //context.startActivity(i);
                    break;
                case Intent.ACTION_SCREEN_ON:
                    Log.e(TAG, "SCREEN_ON");
                    c.set(Calendar.HOUR_OF_DAY, GOTOSLEEP_HOUR);
                    c.set(Calendar.MINUTE, GOTOSLEEP_MINIUTE);
                    c.set(Calendar.SECOND, GOTOSLEEP_SECOND);
                    c.set(Calendar.MILLISECOND, GOTOSLEEP_MILISECOND);
                    Log.e(TAG, "GoToSleep 알람 예약!!! time : " + c.getTime() + " getTimeInMillis : " + c.getTimeInMillis() + " currentTime : " + System.currentTimeMillis());

                    if(c.before(Calendar.getInstance())){
                        c.add(Calendar.DATE, 1);
                    }

                    PendingIntent goToSleepPendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, sendGoToSleepAlarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                    AlarmManager goToSleepAlarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
                    AlarmManager.AlarmClockInfo goToSleepAc = new AlarmManager.AlarmClockInfo(c.getTimeInMillis(), goToSleepPendingIntent);
                    goToSleepAlarmManager.setAlarmClock(goToSleepAc, goToSleepPendingIntent);
                    break;
                case Intent.ACTION_SCREEN_OFF:
                    Log.e(TAG, "SCREEN_OFF");
                    c.set(Calendar.HOUR_OF_DAY, WAKEUP_HOUR);
                    c.set(Calendar.MINUTE, WAKEUP_MINIUTE);
                    c.set(Calendar.SECOND, WAKEUP_SECOND);
                    c.set(Calendar.MILLISECOND, WAKEUP_MILISECOND);
                    Log.e(TAG, "WakeUp 알람 예약!!! time : " + c.getTime() + " getTimeInMillis : " + c.getTimeInMillis() + " currentTime : " + System.currentTimeMillis());

                    if(c.before(Calendar.getInstance())){
                        c.add(Calendar.DATE, 1);
                    }

                    PendingIntent wakeUpPendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, sendWakeUpAlarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                    AlarmManager wakeUpAlarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
                    AlarmManager.AlarmClockInfo wakeUpAc = new AlarmManager.AlarmClockInfo(c.getTimeInMillis(), wakeUpPendingIntent);
                    wakeUpAlarmManager.setAlarmClock(wakeUpAc, wakeUpPendingIntent);
                    //context.startActivity(i);
                    break;
            }
        }
    }
}
