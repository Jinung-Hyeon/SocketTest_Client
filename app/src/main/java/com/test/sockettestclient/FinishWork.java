package com.test.sockettestclient;

import java.util.Calendar;

public class FinishWork {

    public long makeStartWorkTime(int startHour, int startMinute, int startSecond, int startMillisecond){
        Calendar finishWorkCalendar = Calendar.getInstance();

        finishWorkCalendar.set(Calendar.HOUR_OF_DAY, startHour);
        finishWorkCalendar.set(Calendar.MINUTE, startMinute);
        finishWorkCalendar.set(Calendar.SECOND, startSecond);
        finishWorkCalendar.set(Calendar.MILLISECOND, startMillisecond);


        // 일과 종료 시간.
        long finishTime = finishWorkCalendar.getTimeInMillis();

        return finishTime;
    }
}
