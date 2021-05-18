package com.example.googlemeetscheduler;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Objects;

public class AddActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    Button addSchedule;
    EditText editName, editTime, editCode;
    myDBHelper myHelper;
    SQLiteDatabase sqlDB;
    ActionBar.LayoutParams layoutparams;
    Typeface[] sCoreDreams = new Typeface[9];

    String[] days = {"월요일","화요일","수요일","목요일","금요일","토요일","일요일"};
    int[] dayCounts = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    int selectedDay = 0;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint({"ResourceAsColor", "SetTextI18n"})
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subactivity_main);

        Spinner spinner = findViewById(R.id.daySpin);
        ArrayAdapter<String> adpter = new ArrayAdapter<>(getApplicationContext(), R.layout.textview, days);
        spinner.setAdapter(adpter);
        spinner.getBackground().setColorFilter(getResources().getColor(R.color.grayButNotGray), PorterDuff.Mode.SRC_ATOP);
        spinner.setOnItemSelectedListener(this);
        int nowDay = LocalDate.now().getDayOfWeek().getValue();
        switch (nowDay){
            case 1:
                nowDay = 0;
                break;
            case 2:
                nowDay = 1;
                break;
            case 3:
                nowDay = 2;
                break;
            case 4:
                nowDay = 3;
                break;
            case 5:
                nowDay = 4;
                break;
            case 6:
                nowDay = 5;
                break;
            case 7:
                nowDay = 6;
                break;
        }
        spinner.setSelection(nowDay);
        Drawable spinnerDrawable = spinner.getBackground().getConstantState().newDrawable();

        spinnerDrawable.setColorFilter(getResources().getColor(R.color.darkyButNotDark), PorterDuff.Mode.SRC_ATOP);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            spinner.setBackground(spinnerDrawable);
        }
        for (int i = 0; i < sCoreDreams.length-1; i++){
            sCoreDreams[i] =  Typeface.createFromAsset(getAssets(), "SCDream"+(i+1)+".otf");
        }

        Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable(0xfff8f9fa));
        ActionBar actionbar = getSupportActionBar();
        TextView textview = new TextView(getApplicationContext());

        layoutparams = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT);
        textview.setLayoutParams(layoutparams);
        textview.setText("Add Schedule");
        textview.setTextColor(R.color.darkyButNotDark);
        textview.setTypeface(sCoreDreams[4], Typeface.BOLD);
        textview.setTextSize(17);
        actionbar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionbar.setCustomView(textview);

        actionbar.setDisplayHomeAsUpEnabled(true);
        @SuppressLint("UseCompatLoadingForDrawables")
        Drawable backArrow = getDrawable(R.drawable.ic_baseline_arrow_back_24);
        backArrow.setColorFilter(getResources().getColor(R.color.blueblue), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(backArrow);

        addSchedule = findViewById(R.id.addSchedule);
        editName = findViewById(R.id.editName);
        editTime = findViewById(R.id.editTime);
        editCode = findViewById(R.id.editCode);

        myHelper = new myDBHelper(this);

        editTime.setOnClickListener(view -> {
                Calendar myCalendar = Calendar.getInstance();
                int hour = myCalendar.get(Calendar.HOUR_OF_DAY);
                int minute = myCalendar.get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog = new TimePickerDialog(AddActivity.this, android.R.style.Theme_Holo_Dialog_NoActionBar,
                        (view1, hourOfDay, minute1) -> {
                            String hourSet = String.valueOf(hourOfDay);
                            String minSet = String.valueOf(minute1);
                            if(hourSet.length() < 2) hourSet = "0" + hourSet;
                            if(minSet.length() < 2) minSet = "0" + minSet;
                            editTime.setText(hourSet + ":" + minSet);
                        }, hour, minute, true);
                timePickerDialog.show();

        });

        final int idm = (int) System.currentTimeMillis();

        addSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!editName.getText().toString().equals("") && editName.getText().toString().length() > 0 && editTime.getText().toString().length() > 1){

                    Calendar calendar = Calendar.getInstance();
                    int nowYear = calendar.get(Calendar.YEAR);
                    int nowMonth = calendar.get(Calendar.MONTH)+1;
                    int nowDayOfMonth = calendar.get(Calendar.DATE);
                    int nowDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                    int nowHour = calendar.get(Calendar.HOUR);
                    int nowMinute = calendar.get(Calendar.MINUTE);

                    boolean isLeap = (nowYear % 4 == 0 && nowYear % 100 != 0) || nowYear % 400 == 0;
                    // sync with myDB day  column format ( 0: mon, 1: tue, 2: wed, ... 6: sun);
                    if(isLeap){
                        dayCounts[1] += 1;
                    }

                    String[] hourMin = editTime.getText().toString().split(":");
                    if(hourMin[0].length() < 2) hourMin[0] = "0"+hourMin[0];
                    if(hourMin[1].length() < 2) hourMin[1] = "0"+hourMin[1];

                    int[] nowHourMin = {Integer.parseInt(hourMin[0]), Integer.parseInt(hourMin[1])};

                    nowDayOfMonth += Math.abs(nowDayOfWeek - selectedDay);

                    if(nowDayOfWeek == selectedDay){
                        if(nowHourMin[0] < nowHour){
                            if(nowHourMin[0] != 0){
                                nowDayOfMonth += 7;
                            }

                        }else if(nowHourMin[0] == nowHour){
                            if(nowHourMin[1] < nowMinute){
                                if(nowHourMin[1] != 0) {
                                    nowDayOfMonth += 7;
                                }
                            }
                        }
                    }

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
                    if(sMonth.length() < 2) sMonth = "0"+sMonth;
                    if(sDayOfMonth.length() < 2) sDayOfMonth = "0"+sDayOfMonth;
                    String newForm = nowYear +"-"+ sMonth +"-"+ sDayOfMonth +" ";
                    newForm += hourMin[0]+":"+hourMin[1]+":00";

                    sqlDB = myHelper.getWritableDatabase();
                    String tmp = hourMin[0] + ":"+ hourMin[1];
                    sqlDB.execSQL("insert into scheduleTable(id, day, course, code, alarmTime, activation) values ('"+ idm + "','"+ selectedDay + "','"+ editName.getText().toString() + "','"+editCode.getText().toString()+"', '"+ tmp +"', '"+true+"');");
                    sqlDB.execSQL("insert into alarmDetailTable(id, date) values ('"+ idm + "','"+ newForm + "');");
                    sqlDB.close();
                    openMain();

                }else if(editName.getText().toString().equals("") && editName.getText().toString().length() < 1){
                    Toast.makeText(AddActivity.this, "제목을 입력해주세요", Toast.LENGTH_SHORT).show();
                }else if(editTime.getText().toString().equals("") && editTime.getText().toString().length() < 1){
                    Toast.makeText(AddActivity.this, "시간을 설정해주세요", Toast.LENGTH_SHORT).show();
                }
            }

            private void openMain() {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectedDay = days[position].equals("월요일") ? 2 :
                        days[position].equals("화요일") ? 3 :
                        days[position].equals("수요일") ? 4 :
                        days[position].equals("목요일") ? 5 :
                        days[position].equals("금요일") ? 6 :
                        days[position].equals("토요일") ? 7 :
                        days[position].equals("일요일") ? 1 : 1;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public class myDBHelper extends SQLiteOpenHelper {
        public myDBHelper(@Nullable Context context) {
            super(context, "meetDB", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table scheduleTable (id INTEGER PRIMARY KEY AUTOINCREMENT, day INTEGER, course TEXT, code TEXT, alarmTime TEXT, activation TEXT)");
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
}
