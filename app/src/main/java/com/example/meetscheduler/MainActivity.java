package com.example.meetscheduler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
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
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.bee.meetscheduler.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Objects;
import java.util.regex.Pattern;

import lombok.Getter;
import lombok.Setter;

public class MainActivity extends AppCompatActivity {

    final static int SUNDAY = 1;
    final static int MONDAY = 2;
    final static int TUESDAY = 3;
    final static int WEDNESDAY = 4;
    final static int THURSDAY = 5;
    final static int FRIDAY = 6;
    final static int SATURDAY = 7;
    int count = 0;

    ArrayList<ImageButton> arrDeletes = new ArrayList<>();
    ArrayList<TextView> arrCodes = new ArrayList<>();
    LinearLayout layoutContainer;

    myDBHelper myHelper;
    SQLiteDatabase sqlDB;

    Typeface[] sCoreDreams = new Typeface[9];

    AlarmManager alarmManager;

    DayComponent cc = new DayComponent();

    private AdView mAdView;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint({"ResourceAsColor", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, initializationStatus -> {
        });

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable(0xfff8f9fa));
        ActionBar actionbar = getSupportActionBar();
        TextView textview = new TextView(getApplicationContext());
        ActionBar.LayoutParams actionLayoutParams = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT);
        textview.setLayoutParams(actionLayoutParams);
        textview.setText("Meet Scheduler");
        textview.setTextColor(R.color.darkyButNotDark);
        textview.setTypeface(sCoreDreams[2], Typeface.BOLD);
        textview.setTextSize(19);
        actionbar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionbar.setCustomView(textview);

        Toolbar actionBarToolbar = findViewById(R.id.action_bar);
        actionBarToolbar.setTitleTextColor(Color.rgb(33,33,33));

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
            db.execSQL("create table alarmDetailTable (id INTEGER PRIMARY KEY, date TEXT, alarmbefore INTEGER)");
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

    public boolean isNull(String checkItem){
        return checkItem.length() < 1;
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

            schName.setTextSize(17);
            schName.setTypeface(sCoreDreams[5]);

            schTime.setId(thisId+100000);
            schTime.setLayoutParams(paramsLEFT);

            RelativeLayout.LayoutParams paramsCENTER = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            paramsCENTER.setMargins(50, 10, 0, 0);

            if(isNull(scheduleCode)){
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
            arrCodes.get(count).setTextSize(11);
            arrCodes.get(count).setTextColor(Color.rgb(134, 142, 150));
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

            switch(day){
                case MONDAY:
                    cc.setTextColors(255,239, 71, 111);
                    cc.setBackgroundColors(255, 255, 214, 168);
                    cc.setDay("MON");
                    break;
                case TUESDAY:
                    cc.setTextColors(255,244, 162, 97);
                    cc.setBackgroundColors(255, 253, 255, 182);
                    cc.setDay("TUE");
                    break;
                case WEDNESDAY:
                    cc.setTextColors(255,6, 214, 160);
                    cc.setBackgroundColors(255, 202, 255, 191);
                    cc.setDay("WED");
                    break;
                case THURSDAY:
                    cc.setTextColors(255,17, 138, 178);
                    cc.setBackgroundColors(255, 155, 246, 255);
                    cc.setDay("THU");
                    break;
                case FRIDAY:
                    cc.setTextColors(255,7, 59, 78);
                    cc.setBackgroundColors(255, 160, 196, 255);
                    cc.setDay("FRI");
                    break;
                case SATURDAY:
                    cc.setTextColors(255,181, 23, 158);
                    cc.setBackgroundColors(255, 255, 198, 255);
                    cc.setDay("SAT");
                    break;
                case SUNDAY:
                default:
                    cc.setTextColors(255,231, 57, 70);
                    cc.setBackgroundColors(255, 255, 173, 173);
                    cc.setDay("SUN");
                    break;
            }
            schDay.setText(cc.getDay());
            schDay.setTextColor(Color.argb(cc.getAlpha(), cc.getTextR(), cc.getTextG(), cc.getTextB()));
            schName.setTextColor(Color.argb(cc.getAlpha(), cc.getTextR(), cc.getTextG(), cc.getTextB()));

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

            Cursor alarmCursor = sqlDB.rawQuery("select * from alarmDetailTable where id = "+ thisId, null);
            alarmCursor.moveToNext();
            final String alarmDate = alarmCursor.getString(1);
            final int alarmBefore = alarmCursor.getInt(2);
            alarmCursor.close();

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
            calendar.add(Calendar.MINUTE, -alarmBefore);

            if(scheduleActivation.equals("true")){
                myIntent.putExtra("state", "on");
            }else{
                myIntent.putExtra("state", "off");
            }

            myIntent.putExtra("scheduleId", thisId);
            myIntent.putExtra("scheduleName", scheduleName);
            myIntent.putExtra("scheduleCode", scheduleCode);
            myIntent.putExtra("alarmDate", alarmDate);
            PendingIntent appIntent = PendingIntent.getBroadcast(MainActivity.this, thisId, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            long calMillis = calendar.getTimeInMillis();
            long currMillis = System.currentTimeMillis();
            if(calMillis > currMillis){
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), appIntent);
            }else{
                myIntent.putExtra("state", "off");
                PendingIntent updateIntent = PendingIntent.getBroadcast(MainActivity.this, thisId, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), updateIntent);
            }

            aSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                sqlDB = myHelper.getWritableDatabase();
                    if(isChecked){
                        sqlDB.execSQL("update scheduleTable set activation = '"+true+"' where id = '"+ thisId +"'");
                    }else{
                        sqlDB.execSQL("update scheduleTable set activation = '"+false+"' where id = '"+ thisId +"'");
                    }
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), appIntent);
                sqlDB.close();
            });

            final String scCode = scheduleCode;
            final int[] rgbCodes = {cc.getTextR(), cc.getTextG(), cc.getBgB()};
            arrCodes.get(count).setOnClickListener(view -> {
                ViewDialog codePopup = new ViewDialog(thisId, scheduleName, false, rgbCodes[0], rgbCodes[1], rgbCodes[2]);
                codePopup.setCode(scCode);
                codePopup.showDialog(MainActivity.this);
            });
            arrDeletes.get(count).setOnClickListener(view -> {
                ViewDialog deletePopup = new ViewDialog(thisId, scheduleName, true, rgbCodes[0], rgbCodes[1], rgbCodes[2]);
                deletePopup.showDialog(MainActivity.this);
            });

            int finalColor_R = cc.getBgR();
            int finalColor_G = cc.getBgG();
            int finalColor_B = cc.getBgB();
            hLayout.setOnClickListener(view -> {
                Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
                intent.putExtra("color_Alpha", 110);
                intent.putExtra("color_R", finalColor_R);
                intent.putExtra("color_G", finalColor_G);
                intent.putExtra("color_B", finalColor_B);
                intent.putExtra("id", thisId);
                startActivity(intent);
            });
            shape.setColor(Color.argb(110, cc.getBgR(),  cc.getBgG(),  cc.getBgB()));
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

    @Getter
    @Setter
    public class ViewDialog {
        private int thisId, colorR, colorG, colorB;
        private String scheduleName;
        private boolean isDelete;
        private String schCode;
        public ViewDialog(int thisId, String scheduleName, boolean isDelete, int cR, int cG, int cB) {
            this.thisId = thisId;
            this.scheduleName = scheduleName;
            this.isDelete = isDelete;
            this.colorR = cR;
            this.colorG = cG;
            this.colorB = cB;
        }
        void setCode(String schCode){
            this.schCode = schCode;
        }

        @SuppressLint({"UseCompatLoadingForDrawables", "SetTextI18n"})
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        public void showDialog(Activity activity) {
            final Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            if (isDelete) {
                dialog.setContentView(R.layout.dialog);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                TextView itemName = dialog.findViewById(R.id.itemName);
                itemName.setText("\" " + scheduleName + " \"");
                itemName.setTextColor(Color.argb(255, colorR, colorG, colorB));
                CardView cv = dialog.findViewById(R.id.cardV);
                cv.setBackground(getDrawable(R.drawable.rounded_box));
                FrameLayout mDialogNo = dialog.findViewById(R.id.frmNo);
                mDialogNo.setOnClickListener(v -> dialog.dismiss());

                FrameLayout mDialogOk = dialog.findViewById(R.id.frmOk);
                mDialogOk.setOnClickListener(v -> {
                    sqlDB = myHelper.getWritableDatabase();
                    sqlDB.execSQL("delete from scheduleTable where id = '" + thisId + "'");
                    sqlDB.execSQL("delete from alarmDetailTable where id = '" + thisId + "'");
                    sqlDB.execSQL("delete from memoTable where num = '" + thisId + "'");
                    sqlDB.close();
                    cancelAlarm(thisId);
                    showLists();
                    dialog.cancel();
                });

            }else{
                dialog.setContentView(R.layout.meet_dialog);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                TextView itemName = dialog.findViewById(R.id.itemName);
                TextView itemLink = dialog.findViewById(R.id.itemLink);
                itemName.setText(scheduleName);
                itemLink.setText(schCode);

                itemName.setTextColor(Color.argb(255, colorR, colorG, colorB));
                CardView cv = dialog.findViewById(R.id.cardV);
                cv.setBackground(getDrawable(R.drawable.rounded_box));
                FrameLayout mDialogNo = dialog.findViewById(R.id.frmNo);
                mDialogNo.setOnClickListener(v -> dialog.dismiss());

                FrameLayout mDialogOk = dialog.findViewById(R.id.frmOk);
                mDialogOk.setOnClickListener(v -> {
                    Uri parse = Uri.parse("https://meet.google.com/"+schCode);
                    Intent intent = new Intent(Intent.ACTION_VIEW, parse);
                    startActivity(intent);
                    dialog.cancel();
                });

            }

            dialog.show();

        }
    }
}