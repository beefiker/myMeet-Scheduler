package com.example.googlemeetscheduler;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.util.Date;

import static java.time.LocalDateTime.now;

public class AlarmReceiver extends BroadcastReceiver {

    Context context;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {

        String state = intent.getExtras().getString("state");
        String scheduleName = intent.getExtras().getString("scheduleName");
        String scheduleCode = intent.getExtras().getString("scheduleCode");
        String scheduleTime = intent.getExtras().getString("scheduleTime");
        int scheduleId = intent.getExtras().getInt("scheduleId");
//        if(state.equals("on")){
            Toast.makeText(context,
                    "이름 : " + scheduleName + "\n"
                            + "코드 : " + scheduleCode + "\n"
                            + "시간 : " + scheduleTime + "\n"
                            + "아이디 : " + scheduleId + "\n",
                    Toast.LENGTH_LONG).show();
//        }
        System.out.println(now() + "scheduleId = " + scheduleId + "알람");
//        Toast.makeText(context, "앱인텐 : " + component1+ "\n아이디 : "+component2+"\n이름 : "+ scheduleName + "\n코드 : " + scheduleCode +"\n시간 : " + scheduleTime, Toast.LENGTH_LONG).show();
//        Toast.makeText(context, "컴포넌트1: " + component1 + "\n네임 : " + scheduleName +"\nIDM : " + component2,  Toast.LENGTH_LONG).show();


//        this.context = context;

//

//
//        Intent service_intent = new Intent(context, RingtonePlayingService.class);
//
//        service_intent.putExtra("state", get_your_string);
//
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
//            this.context.startForegroundService(service_intent);
//        }else{
//            this.context.startService(service_intent);
//        }
    }
}

