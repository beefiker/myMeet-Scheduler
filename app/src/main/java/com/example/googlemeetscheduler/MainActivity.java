package com.example.googlemeetscheduler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class MainActivity extends AppCompatActivity {

    int count = 0;
    String scheduleId, scheduleName, scheduleCode, scheduleTime, scheduleActivation;

    ArrayList<ImageButton> arrDeletes = new ArrayList<>();
    ArrayList<TextView> arrCodes = new ArrayList<>();
    LinearLayout layoutContainer;

    myDBHelper myHelper;
    SQLiteDatabase sqlDB;

    Typeface[] sCoreDreams = new Typeface[9];

    AlarmManager alarmManager;
    ActionBar.LayoutParams layoutparams;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionbar = getSupportActionBar();
        TextView textview = new TextView(getApplicationContext());
        layoutparams = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT);
        textview.setLayoutParams(layoutparams);
        textview.setText("GoogleMeetScheduler");
        textview.setTextColor(Color.rgb(33,33,33));
        textview.setTypeface(sCoreDreams[2], Typeface.BOLD);
        textview.setTextSize(19);
        actionbar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionbar.setCustomView(textview);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xfff8f9fa));

        Toolbar actionBarToolbar = findViewById(R.id.action_bar);
        if (actionBarToolbar != null) actionBarToolbar.setTitleTextColor(Color.rgb(33,33,33));

        for (int i = 0; i < sCoreDreams.length-1; i++){
            sCoreDreams[i] =  Typeface.createFromAsset(getAssets(), "SCDream"+(i+1)+".otf");
        }

        layoutContainer = findViewById(R.id.layoutContainer);

        myHelper = new myDBHelper(this);

        showLists();

    }

    public static class myDBHelper extends SQLiteOpenHelper{
        public myDBHelper(@Nullable Context context) {
            super(context, "meetDBtest", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table scheduleTable (id INTEGER PRIMARY KEY AUTOINCREMENT, day INTEGER, course TEXT, code TEXT, alarmTime TEXT, activation TEXT)");
            db.execSQL("create table memoTable (num INTEGER, id INTEGER PRIMARY KEY AUTOINCREMENT, content TEXT, regdate TEXT)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("drop table if exists scheduleTable");
            db.execSQL("drop table if exists memoTable");
            onCreate(db);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_option, menu);
        int positionOfMenuItem = 0; // or whatever...
        MenuItem item = menu.getItem(positionOfMenuItem);
        SpannableString s = new SpannableString("+");
        s.setSpan(new AbsoluteSizeSpan(90), 0, s.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        s.setSpan(new ForegroundColorSpan(Color.rgb(26,144,236)), 0, s.length(), 0);
        item.setTitle(s);

        int positionOfMenuItem2 = 1; // or whatever...
        MenuItem item2 = menu.getItem(positionOfMenuItem2);
        SpannableString s2 = new SpannableString("Refresh");
        s2.setSpan(new ForegroundColorSpan(Color.rgb(26,144,236)), 0, s2.length(), 0);
        item2.setTitle(s2);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu1:
                Intent intent = new Intent(getApplicationContext(), subActivity.class);
                startActivity(intent);
                break;
            case R.id.menu2:
                showLists();
                break;
        }
        return super.onOptionsItemSelected(item);

    }

    private void cancelAlarm(int thisId) {

        Intent myIntent = new Intent(this, AlarmReceiver.class);
        PendingIntent appIntent = PendingIntent.getBroadcast(this, thisId, myIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        appIntent.cancel();
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(appIntent);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void showLists(){

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        final Intent myIntent = new Intent(this, AlarmReceiver.class);

        Calendar calendar = new GregorianCalendar();

        layoutContainer.removeAllViews();
        sqlDB = myHelper.getReadableDatabase();
        Cursor cursor = sqlDB.rawQuery("select * from scheduleTable order by day, alarmTime", null);

        while(cursor.moveToNext()){
            final int thisId = cursor.getInt(0);
            scheduleId = cursor.getString(0);
            int day = cursor.getInt(1);
            scheduleName = cursor.getString(2);
            scheduleCode = cursor.getString(3);
            scheduleTime = cursor.getString(4);
            scheduleActivation = cursor.getString(5);

            int schHour = 0;
            int schMin = 0;
            String[] schHourMin = scheduleTime.split(":");
            if(schHourMin.length > 1){
                schMin = Integer.parseInt(schHourMin[1]);
            }else if(schHourMin.length > 0){
                schHour = Integer.parseInt(schHourMin[0]);
                schMin = 0;
            }

            RelativeLayout.LayoutParams Lparams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            Lparams.setMargins(5, 10, 5, 30);
            RelativeLayout hLayout = new RelativeLayout(this);
            hLayout.setPadding(30, 20, 30, 20);

            GradientDrawable shape = new GradientDrawable();
            shape.setColor(Color.rgb(234, 236, 243));
            shape.setCornerRadius(20);
            hLayout.setGravity(Gravity.LEFT);
            hLayout.setBackground(shape);
            hLayout.setElevation(11);
            hLayout.setLayoutParams(Lparams);
            layoutContainer.addView(hLayout);

            TextView schName = new TextView(this);
            TextView schCode = new TextView(this);
            TextView schTime = new TextView(this);
            TextView schDay = new TextView(this);

            @SuppressLint("UseSwitchCompatOrMaterialCode")
            Switch aSwitch = new Switch(this);

            arrDeletes.add(new ImageButton(this));
            arrCodes.add(schCode);

            RelativeLayout.LayoutParams paramsLEFT = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            paramsLEFT.addRule(RelativeLayout.CENTER_VERTICAL);
            paramsLEFT.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

            RelativeLayout.LayoutParams paramsRIGHT = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            paramsRIGHT.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            paramsRIGHT.addRule(RelativeLayout.CENTER_VERTICAL);

            schName.setTextSize(18);
            schName.setTypeface(sCoreDreams[5]);

            schTime.setId(thisId+100000);
            schTime.setLayoutParams(paramsLEFT);

            RelativeLayout.LayoutParams paramsCENTER = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            paramsCENTER.setMargins(50, 10, 0, 0);
            paramsCENTER.addRule(RelativeLayout.RIGHT_OF, schTime.getId());
            schName.setLayoutParams(paramsCENTER);

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.RIGHT_OF, schTime.getId());
            params.addRule(RelativeLayout.ALIGN_BOTTOM, schTime.getId());
            params.setMargins(50, 10, 0, 0);

            RelativeLayout.LayoutParams dayParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            dayParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            dayParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            schDay.setLayoutParams(dayParams);
            schDay.setTextSize(9);
            schDay.setTypeface(sCoreDreams[6]);

            arrCodes.get(count).setLayoutParams(params);
            arrCodes.get(count).setTextSize(12);
            arrCodes.get(count).setTextColor(Color.rgb(33,37,41));
            arrCodes.get(count).setTypeface(sCoreDreams[3]);

            schTime.setTextSize(30);
            schTime.setTypeface(sCoreDreams[4]);
            schTime.setTextColor(Color.rgb(112,112,121));

            arrDeletes.get(count).setImageResource(R.drawable.ic_baseline_delete_24);
            arrDeletes.get(count).setBackgroundResource(R.color.trans);
            arrDeletes.get(count).setId(thisId);
            arrDeletes.get(count).setLayoutParams(paramsRIGHT);

            RelativeLayout.LayoutParams switchParam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            switchParam.setMargins(0, 0, 50, 0);
            switchParam.addRule(RelativeLayout.LEFT_OF, arrDeletes.get(count).getId());
            switchParam.addRule(RelativeLayout.CENTER_VERTICAL);
            aSwitch.setLayoutParams(switchParam);
            aSwitch.setHighlightColor(getResources().getColor(R.color.goldenYellow));
            aSwitch.setChecked(scheduleActivation.equals("true"));
            aSwitch.setTrackResource(R.drawable.switch_track_selector);
            aSwitch.setThumbResource(R.drawable.switch_thumb);

            schName.setText(scheduleName);
            arrCodes.get(count).setText(scheduleCode);

            String time1 = scheduleTime.substring(0,2);
            String time2 = scheduleTime.substring(2);
            schTime.setText(time1 + ":" + time2);

            switch(day){
                case 0:
                    schDay.setText("MON");
                    schDay.setTextColor(Color.argb(180, 239,71,111));
                    schName.setTextColor(Color.argb(180, 239,71,111));
                    break;
                case 1:
                    schDay.setText("TUE");
                    schDay.setTextColor(Color.argb(255, 255,209,102));
                    schName.setTextColor(Color.argb(255, 255,209,102));
                    break;
                case 2:
                    schDay.setText("WED");
                    schDay.setTextColor(Color.argb(180, 6,214,160));
                    schName.setTextColor(Color.argb(180, 6,214,160));
                    break;
                case 3:
                    schDay.setText("THU");
                    schDay.setTextColor(Color.argb(180, 17,138,178));
                    schName.setTextColor(Color.argb(180, 17,138,178));
                    break;
                case 4:
                    schDay.setText("FRI");
                    schDay.setTextColor(Color.argb(180,  7,59,78));
                    schName.setTextColor(Color.argb(180,  7,59,78));
                    break;
                case 5:
                    schDay.setText("SAT");
                    schDay.setTextColor(Color.argb(180, 244,162,97));
                    schName.setTextColor(Color.argb(180, 244,162,97));
                    break;
                default:
                    schDay.setText("SUN");
                    schDay.setTextColor(Color.argb(180, 231,111,81));
                    schName.setTextColor(Color.argb(180, 231,111,81));
                    break;

            }

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

            calendar.set(Calendar.HOUR_OF_DAY, schHour);
            calendar.set(Calendar.MINUTE, schMin);
            calendar.set(Calendar.SECOND, 0);

            if(scheduleActivation.equals("true")){
                myIntent.putExtra("state", "on");
                myIntent.putExtra("scheduleId", thisId);
                myIntent.putExtra("scheduleName", scheduleName);
                myIntent.putExtra("scheduleCode", scheduleCode);
                myIntent.putExtra("scheduleTime", scheduleTime);
                PendingIntent appIntent = PendingIntent.getBroadcast(this, thisId, myIntent, PendingIntent.FLAG_ONE_SHOT);
                alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY*7, appIntent);

            }else{
                myIntent.putExtra("state", "off");
                cancelAlarm(thisId);
            }

            // thisID 고유한 값으로 펜딩인텐트 생성
            PendingIntent appIntent = PendingIntent.getBroadcast(this, thisId, myIntent, PendingIntent.FLAG_ONE_SHOT);

            aSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                sqlDB = myHelper.getWritableDatabase();
                    if(isChecked){
                        sqlDB.execSQL("update scheduleTable set activation = '"+true+"' where id = '"+ thisId +"'");
                        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY*7, appIntent);
//                        Toast.makeText(this,  "앱인텐트 : " + appIntent + "\n활성화 id : " + thisId, Toast.LENGTH_SHORT).show();
                    }else{
                        // 펜딩인텐트 삭제
                        cancelAlarm(thisId);
//                        Toast.makeText(this, "앱인텐트 : " + appIntent  + "\n삭제할 id : " + thisId, Toast.LENGTH_SHORT).show();
                        sqlDB.execSQL("update scheduleTable set activation = '"+false+"' where id = '"+ thisId +"'");
                    }
                sqlDB.close();
            });

            final String scCode = scheduleCode;
            arrCodes.get(count).setOnClickListener(view -> {
                Uri parse = Uri.parse("https://meet.google.com/"+scCode);
                Intent intent = new Intent(Intent.ACTION_VIEW, parse);
                startActivity(intent);
            });
            arrDeletes.get(count).setOnClickListener(view -> {
                sqlDB = myHelper.getWritableDatabase();
                sqlDB.execSQL("delete from scheduleTable where id = '"+ thisId +"'");
                sqlDB.execSQL("delete from memoTable where num = '"+ thisId +"'");
                sqlDB.close();

                // 해당 펜딩 인덴트 삭제
                cancelAlarm(thisId);
                showLists();
            });

            hLayout.setOnClickListener(view -> {
                Intent intent = new Intent(getApplicationContext(), detailActivity.class);
                intent.putExtra("id", thisId);
                startActivity(intent);
            });

            hLayout.addView(schName);
            hLayout.addView(arrCodes.get(count));
            hLayout.addView(schTime);
            hLayout.addView(schDay);
            hLayout.addView(aSwitch);
            hLayout.addView(arrDeletes.get(count));

            count++;
        }

        cursor.close();
        sqlDB.close();
    }

}