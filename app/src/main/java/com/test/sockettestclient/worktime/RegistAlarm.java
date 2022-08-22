package com.test.sockettestclient.worktime;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class RegistAlarm {


    private final String TAG = "AlarmTest";

    private WorkTime workTime = new WorkTime();

    private Context mContext;
    private Activity mActivity;
    private Calendar sleep, wake;

    public RegistAlarm (Context context, Activity activity) {
        this.mContext = context;
        this.mActivity = activity;
    }

    public void RegistSleepAlarm(){
        sleep = workTime.finishWorkTime();
        Intent sendGoToSleepAlarmIntent = new Intent(mActivity, GoToSleepAlarm.class);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        if(workTime.finishWorkTime().before(Calendar.getInstance())){
            sleep.add(Calendar.DATE, 1);
        }

        Log.e(TAG, "GoToSleep 알람 예약!!! 꺼짐 예약 시간 : " + dateFormat.format(sleep.getTimeInMillis()) + " || 현재시간 : " + dateFormat.format(System.currentTimeMillis()));
        PendingIntent goToSleepPendingIntent = PendingIntent.getBroadcast(mContext.getApplicationContext(), 0, sendGoToSleepAlarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager goToSleepAlarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        AlarmManager.AlarmClockInfo goToSleepAc = new AlarmManager.AlarmClockInfo(sleep.getTimeInMillis(), goToSleepPendingIntent);
        goToSleepAlarmManager.setAlarmClock(goToSleepAc, goToSleepPendingIntent);
    }

    public void RegistWakeUpAlarm(){
        wake = workTime.startWorkTime();
        Intent sendWakeUpAlarmIntent = new Intent(mActivity, WakeUpAlarm.class);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        if(workTime.startWorkTime().before(Calendar.getInstance())){
            wake.add(Calendar.DATE, 1);
        }

        Log.e(TAG, "WakeUp 알람 예약!!! 켜짐 예약 시간 : " + dateFormat.format(wake.getTimeInMillis()) + " || 현재시간 : " + dateFormat.format(System.currentTimeMillis()));
        PendingIntent wakeUpPendingIntent = PendingIntent.getBroadcast(mContext.getApplicationContext(), 1, sendWakeUpAlarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager wakeUpAlarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        AlarmManager.AlarmClockInfo wakeUpAc = new AlarmManager.AlarmClockInfo(wake.getTimeInMillis(), wakeUpPendingIntent);
        wakeUpAlarmManager.setAlarmClock(wakeUpAc, wakeUpPendingIntent);

    }
}
