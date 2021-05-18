package com.example.googlemeetscheduler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
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
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Objects;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    int count = 0;

    ArrayList<ImageButton> arrDeletes = new ArrayList<>();
    ArrayList<TextView> arrCodes = new ArrayList<>();
    LinearLayout layoutContainer;

    myDBHelper myHelper;
    SQLiteDatabase sqlDB;

    Typeface[] sCoreDreams = new Typeface[9];

    AlarmManager alarmManager;
    ActionBar.LayoutParams actionLayoutParams;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint({"ResourceAsColor", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionbar = getSupportActionBar();
        TextView textview = new TextView(getApplicationContext());
        actionLayoutParams = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT);
        textview.setLayoutParams(actionLayoutParams);
        textview.setText("GoogleMeetScheduler");
        textview.setTextColor(R.color.darkyButNotDark);
        textview.setTypeface(sCoreDreams[2], Typeface.BOLD);
        textview.setTextSize(19);
        assert actionbar != null;
        actionbar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionbar.setCustomView(textview);

        Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable(0xfff8f9fa));

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
            super(context, "meetDB", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table scheduleTable (id INTEGER PRIMARY KEY, day INTEGER, course TEXT, code TEXT, alarmTime TEXT, activation TEXT)");
            db.execSQL("create table memoTable (num INTEGER, id INTEGER PRIMARY KEY AUTOINCREMENT, content TEXT, regdate TEXT)");
            db.execSQL("create table alarmDetailTable (id INTEGER PRIMARY KEY, date TEXT)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("drop table if exists scheduleTable");
            db.execSQL("drop table if exists memoTable");
            db.execSQL("drop table if exists alarmDetailTable");
            onCreate(db);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_option, menu);
        int positionOfMenuItem = 0;
        MenuItem item = menu.getItem(positionOfMenuItem);
        SpannableString s = new SpannableString("+");
        s.setSpan(new AbsoluteSizeSpan(90), 0, s.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        s.setSpan(new ForegroundColorSpan(Color.rgb(26,144,236)), 0, s.length(), 0);
        item.setTitle(s);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu1) {
            Intent intent = new Intent(getApplicationContext(), AddActivity.class);
            startActivity(intent);
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

    @SuppressLint({"RtlHardcoded", "SetTextI18n"})
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void showLists(){

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        layoutContainer.removeAllViews();
        sqlDB = myHelper.getReadableDatabase();
        Cursor cursor = sqlDB.rawQuery("select * from scheduleTable order by day, alarmTime", null);

        while(cursor.moveToNext()){

            final String scheduleName, scheduleCode, scheduleTime, scheduleActivation;
            final int thisId = cursor.getInt(0);
            final int day = cursor.getInt(1);
            scheduleName = cursor.getString(2);
            scheduleCode = cursor.getString(3);
            scheduleTime = cursor.getString(4);
            scheduleActivation = cursor.getString(5);

            RelativeLayout.LayoutParams hLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            hLayoutParams.setMargins(5, 10, 5, 30);
            RelativeLayout hLayout = new RelativeLayout(this);
            hLayout.setPadding(30, 20, 30, 20);

            hLayout.setLayoutParams(hLayoutParams);

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

            schName.setTextSize(16);
            schName.setTypeface(sCoreDreams[5]);

            schTime.setId(thisId+100000);
            schTime.setLayoutParams(paramsLEFT);

            RelativeLayout.LayoutParams paramsCENTER = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            paramsCENTER.setMargins(50, 10, 0, 0);

            if(scheduleCode.length() < 1){
                paramsCENTER.addRule(RelativeLayout.RIGHT_OF, schTime.getId());
                paramsCENTER.addRule(RelativeLayout.CENTER_VERTICAL, schTime.getId());
                schName.setTextSize(17);
            }else{
                paramsCENTER.addRule(RelativeLayout.RIGHT_OF, schTime.getId());
            }

            schName.setLayoutParams(paramsCENTER);

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.RIGHT_OF, schTime.getId());
            params.addRule(RelativeLayout.ALIGN_BOTTOM, schTime.getId());
            params.setMargins(50, 10, 0, 0);

            RelativeLayout.LayoutParams dayParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            dayParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            dayParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            schDay.setLayoutParams(dayParams);
            schDay.setTextSize(8);
            schDay.setTypeface(sCoreDreams[5]);

            arrCodes.get(count).setLayoutParams(params);
            arrCodes.get(count).setTextSize(12);
            arrCodes.get(count).setTextColor(Color.rgb(33,37,41));
            arrCodes.get(count).setTypeface(sCoreDreams[3]);

            schTime.setTextSize(28);
            schTime.setTypeface(sCoreDreams[4]);
            schTime.setTextColor(Color.rgb(112,112,121));

            arrDeletes.get(count).setImageResource(R.drawable.ic_baseline_delete_forever_24);
            arrDeletes.get(count).setBackgroundResource(R.color.trans);
            arrDeletes.get(count).setId(thisId);
            arrDeletes.get(count).setLayoutParams(paramsRIGHT);

            RelativeLayout.LayoutParams switchParam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            switchParam.setMargins(0, 0, 50, 0);
            switchParam.addRule(RelativeLayout.LEFT_OF, arrDeletes.get(count).getId());
            switchParam.addRule(RelativeLayout.CENTER_VERTICAL);
            aSwitch.setLayoutParams(switchParam);
            aSwitch.setChecked(scheduleActivation.equals("true"));
            aSwitch.setTrackResource(R.drawable.switch_track_selector);
            aSwitch.setThumbResource(R.drawable.switch_thumb);

            String newName = scheduleName.replaceAll(" ", "");
            if (isEnglishOrNumber(newName) && newName.length() > 16) {
                schName.setText(scheduleName.substring(0, 17) + "...");
            }else if(isEnglishOrNumber(newName) && newName.length() < 15){
                schName.setText(scheduleName);
            }else if(!isEnglishOrNumber(newName) && newName.length() > 11){
                schName.setText(scheduleName.substring(0, 11) + "...");
            }else{
                schName.setText(scheduleName);
            }
            arrCodes.get(count).setText(scheduleCode);

            String time1 = scheduleTime.substring(0,2);
            String time2 = scheduleTime.substring(2);
            schTime.setText(time1 + time2);

            GradientDrawable shape = new GradientDrawable();

            int color_Alpha = 110;
            int color_R;
            int color_G;
            int color_B;

            switch(day){
                case 2:
                    color_R = 255;
                    color_G = 214;
                    color_B = 165;
                    schDay.setText("MON");
                    schDay.setTextColor(Color.argb(255, 239, 71, 111));
                    schName.setTextColor(Color.argb(255, 239, 71, 111));
                    break;
                case 3:
                    color_R = 253;
                    color_G = 255;
                    color_B = 182;
                    schDay.setText("TUE");
                    schDay.setTextColor(Color.argb(255, 244, 162, 97));
                    schName.setTextColor(Color.argb(205, 244, 162, 97));
                    break;
                case 4:
                    color_R = 202;
                    color_G = 255;
                    color_B = 191;
                    schDay.setText("WED");
                    schDay.setTextColor(Color.argb(255, 6, 214, 160));
                    schName.setTextColor(Color.argb(255, 6, 214, 160));
                    break;
                case 5:
                    color_R = 155;
                    color_G = 246;
                    color_B = 255;
                    schDay.setText("THU");
                    schDay.setTextColor(Color.argb(255, 17,138,178));
                    schName.setTextColor(Color.argb(255, 17,138,178));
                    break;
                case 6:
                    color_R = 160;
                    color_G = 196;
                    color_B = 255;
                    schDay.setText("FRI");
                    schDay.setTextColor(Color.argb(255,  7,59,78));
                    schName.setTextColor(Color.argb(255,  7,59,78));
                    break;
                case 7:
                    color_R = 255;
                    color_G = 198;
                    color_B = 255;
                    schDay.setText("SAT");
                    schDay.setTextColor(Color.argb(255, 181, 23, 158));
                    schName.setTextColor(Color.argb(255, 181, 23, 158));
                    break;
                default:
                    color_R = 255;
                    color_G = 173;
                    color_B = 173;
                    schDay.setText("SUN");
                    schDay.setTextColor(Color.argb(255, 231,57,70));
                    schName.setTextColor(Color.argb(255, 231,57,70));
                    break;
            }

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

            @SuppressLint("Recycle")
            Cursor alarmCursor = sqlDB.rawQuery("select * from alarmDetailTable where id = "+ thisId, null);
            alarmCursor.moveToNext();
            final String alarmDate = alarmCursor.getString(1);

            final Calendar calendar = new GregorianCalendar();
            Intent myIntent = new Intent(MainActivity.this, AlarmReceiver.class);

            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date datetime = null;
            try{
                datetime = dateFormat.parse(alarmDate);
            }catch (ParseException e){
                e.printStackTrace();
            }

            assert datetime != null;
            calendar.setTime(datetime);

            if(scheduleActivation.equals("true")){
                myIntent.putExtra("state", "on");
                myIntent.putExtra("scheduleId", thisId);
                myIntent.putExtra("scheduleName", scheduleName);
                myIntent.putExtra("scheduleCode", scheduleCode);
                myIntent.putExtra("alarmDate", alarmDate);
            }else{
                myIntent.putExtra("state", "off");
                myIntent.putExtra("scheduleId", thisId);
                myIntent.putExtra("scheduleName", scheduleName);
                myIntent.putExtra("scheduleCode", scheduleCode);
                myIntent.putExtra("alarmDate", alarmDate);
            }

            PendingIntent appIntent = PendingIntent.getBroadcast(MainActivity.this, thisId, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            long calMillis = calendar.getTimeInMillis();
            long currMillis = System.currentTimeMillis();
            if(calMillis > currMillis){
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), appIntent);
            }else{

                myIntent.putExtra("state", "off");
                myIntent.putExtra("scheduleId", thisId);
                myIntent.putExtra("scheduleName", scheduleName);
                myIntent.putExtra("scheduleCode", scheduleCode);
                myIntent.putExtra("alarmDate", alarmDate);
                PendingIntent appIntent1 = PendingIntent.getBroadcast(MainActivity.this, thisId, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), appIntent1);
            }
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), appIntent);
            aSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                sqlDB = myHelper.getWritableDatabase();
                    if(isChecked){
                        sqlDB.execSQL("update scheduleTable set activation = '"+true+"' where id = '"+ thisId +"'");
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), appIntent);
                    }else{
//                        cancelAlarm(thisId);
                        sqlDB.execSQL("update scheduleTable set activation = '"+false+"' where id = '"+ thisId +"'");
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), appIntent);
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
                AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
                builder.setTitle("Deletes").setMessage("Are you sure ? \nDelete " + scheduleName)
                        .setPositiveButton("YES", (dialog, which) -> {
                            sqlDB = myHelper.getWritableDatabase();
                            sqlDB.execSQL("delete from scheduleTable where id = '"+ thisId +"'");
                            sqlDB.execSQL("delete from alarmDetailTable where id = '"+ thisId +"'");
                            sqlDB.execSQL("delete from memoTable where num = '"+ thisId +"'");
                            sqlDB.close();
                            cancelAlarm(thisId);
                            showLists();
                        }).setNegativeButton("NO", (dialog, which) -> {
                            }).show();
            });

            int finalColor_R = color_R;
            int finalColor_G = color_G;
            int finalColor_B = color_B;
            hLayout.setOnClickListener(view -> {
                Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
                intent.putExtra("color_Alpha", color_Alpha);
                intent.putExtra("color_R", finalColor_R);
                intent.putExtra("color_G", finalColor_G);
                intent.putExtra("color_B", finalColor_B);
                intent.putExtra("id", thisId);
                startActivity(intent);
            });

            shape.setColor(Color.argb(color_Alpha, color_R, color_G, color_B));
            shape.setCornerRadius(20);
            hLayout.setGravity(Gravity.LEFT);
            hLayout.setBackground(shape);
            hLayout.setElevation(11);

            hLayout.addView(schName);
            hLayout.addView(arrCodes.get(count));
            hLayout.addView(schTime);
            hLayout.addView(schDay);
            hLayout.addView(aSwitch);
            hLayout.addView(arrDeletes.get(count));

            layoutContainer.addView(hLayout);

            count++;
        }

        cursor.close();
        sqlDB.close();
    }

    public boolean isEnglishOrNumber(String s){
        return Pattern.matches("^[0-9a-zA-z]*$", s);
    }
}