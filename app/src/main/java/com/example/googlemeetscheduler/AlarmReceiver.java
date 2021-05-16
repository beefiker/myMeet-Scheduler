package com.example.googlemeetscheduler;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.SystemClock;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;

import static java.time.LocalDateTime.now;

public class AlarmReceiver extends BroadcastReceiver {

    MainActivity.myDBHelper myHelper;
    SQLiteDatabase sqlDB;

    int[] dayCounts = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {

        String state = intent.getExtras().getString("state");
        String scheduleName = intent.getExtras().getString("scheduleName");
        String scheduleCode = intent.getExtras().getString("scheduleCode");
        String scheduleTime = intent.getExtras().getString("scheduleTime");
        int scheduleId = intent.getExtras().getInt("scheduleId");

        myHelper = new MainActivity.myDBHelper(context);

        sqlDB = myHelper.getWritableDatabase();
        @SuppressLint("Recycle")
        Cursor alarmCursor1 = sqlDB.rawQuery("select * from alarmDetailTable where id = "+ scheduleId, null);
        alarmCursor1.moveToFirst();
        if(alarmCursor1.getCount() > 0) {
            alarmCursor1.moveToFirst();
            final String alarmDate = alarmCursor1.getString(1);

            String splitDates[] = alarmDate.split(" ");
            String dates[] = splitDates[0].split("-");
            String times[] = splitDates[1].split(":");

            int nowYear = Integer.parseInt(dates[0]);
            int nowMonth = Integer.parseInt(dates[1]);
            int nowDayOfMonth = Integer.parseInt(dates[2]);
            int nowHour =  Integer.parseInt(times[0]);
            int nowMinute =  Integer.parseInt(times[1]);

            boolean isLeap = (nowYear % 4 == 0 && nowYear % 100 != 0) || nowYear % 400 == 0;
            // sync with myDB day  column format ( 0: mon, 1: tue, 2: wed, ... 6: sun);
            if(isLeap){
                dayCounts[1] += 1;
            }

            nowDayOfMonth += 7;

            if(nowDayOfMonth > dayCounts[nowMonth-1]){
                nowDayOfMonth = nowDayOfMonth - dayCounts[nowMonth-1];
                if(nowMonth>12){
                    nowYear++;
                    nowMonth -= 12;
                }
                nowMonth++;
            }

            String sMonth = String.valueOf(nowMonth);
            String sDayOfMonth = String.valueOf(nowDayOfMonth);
            String sHour = String.valueOf(nowHour);
            String sMinute = String.valueOf(nowMinute);
            if(sMonth.length() < 2) sMonth = "0"+sMonth;
            if(sDayOfMonth.length() < 2) sDayOfMonth = "0"+sDayOfMonth;
            if(sHour.length() < 2) sHour = "0"+sHour;
            if(sMinute.length() < 2) sMinute = "0"+sMinute;

            String newForm = nowYear +"-"+ sMonth +"-"+ sDayOfMonth +" ";
            newForm += sHour+":"+sMinute+":00";

            System.out.println("update to : " + newForm);
            if(state.equals("on")){

                sqlDB = myHelper.getWritableDatabase();
                sqlDB.execSQL("update alarmDetailTable set date = '"+newForm+"' where id = '"+scheduleId+"' ");
                sqlDB.close();
                Toast.makeText(context,
                        "이름 : " + scheduleName + "\n"
                                + "코드 : " + scheduleCode + "\n"
                                + "시간 : " + scheduleTime + "\n"
                                + "아이디 : " + scheduleId + "\n"
                                + alarmDate,
                        Toast.LENGTH_LONG).show();
                System.out.println(LocalDate.now() + " : " + LocalTime.now() + "알람 : "+ scheduleName );
            }
        }
        sqlDB.close();

    }
}

