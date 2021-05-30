package com.example.meetscheduler;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.bee.meetscheduler.R;

public class RingtonePlayingService extends Service {

    MediaPlayer mediaPlayer;
    int startId;
    boolean isRunning;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String getState = intent.getExtras().getString("state");
        String getName = intent.getExtras().getString("name");
        String getCode = intent.getExtras().getString("code");
        int getId = intent.getExtras().getInt("id");

        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "default";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Notification Channel",
                    NotificationManager.IMPORTANCE_HIGH);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Uri parse = Uri.parse("https://meet.google.com/"+getCode);
            Intent mIntent = new Intent(Intent.ACTION_VIEW, parse);
            PendingIntent mPendingIntent = PendingIntent.getActivity(this,0, mIntent,0);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("GoogleMeetScheduler")
                    .setContentText(getName+" starts on "+getCode)
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText("["+getName+"] join with meet link: https://meet.google.com/"+getCode))
                    .setSmallIcon(R.drawable.ic_baseline_notifications_24)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(mPendingIntent)
                    .setAutoCancel(true)
                    .build();

            notification.flags |= NotificationCompat.FLAG_AUTO_CANCEL;
            startForeground(getId, notification);
            stopForeground(false);
            Toast.makeText(this, "Subject : "+getName+"\nCode : "+getCode, Toast.LENGTH_LONG).show();
        }

        assert getState != null;
        switch (getState) {
            case "alarm on":
                startId = 1;
                break;
            case "alarm off":
                startId = 0;
                break;
            default:
                startId = 0;
                break;
        }
        // 알람음 재생 X , 알람음 시작 클릭
        if(!this.isRunning && startId == 1) {

            mediaPlayer = MediaPlayer.create(this,R.raw.love_alarm_notification);
            mediaPlayer.start();

            this.isRunning = true;
            this.startId = 0;
        }
        // 알람음 재생 O , 알람음 종료 버튼 클릭
        else if(this.isRunning && startId == 0) {

            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();

            this.isRunning = false;
            this.startId = 0;
        }
        // 알람음 재생 X , 알람음 종료 버튼 클릭
        else if(!this.isRunning && startId == 0) {

            this.isRunning = false;
            this.startId = 0;

        }
        // 알람음 재생 O, 알람음 시작 버튼 클릭
        else if(this.isRunning && startId == 1){

            this.isRunning = true;
            this.startId = 1;
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d("onDestory() 실행", "서비스 파괴");

    }
}
