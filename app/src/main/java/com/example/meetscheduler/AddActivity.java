package com.example.meetscheduler;

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
import android.text.Editable;
import android.text.TextWatcher;
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

import com.bee.meetscheduler.R;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.Objects;
import java.util.regex.Pattern;

public class AddActivity extends AppCompatActivity {
    Button addSchedule;
    EditText editName, editTime, editCode;
    myDBHelper myHelper;
    SQLiteDatabase sqlDB;
    ActionBar.LayoutParams layoutParams;
    Typeface[] sCoreDreams = new Typeface[9];

    String[] days = {"MON","TUE","WED","THU","FRI","SAT","SUN"};
    String[] alarms = {"0m","5m","10m","15m","30m","1h"};
    int[] dayCounts = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    int selectedDay = 0;
    int selectedAlarmBefore = 0;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint({"ResourceAsColor", "SetTextI18n"})
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subactivity_main);


        Spinner daySpinner = findViewById(R.id.daySpin);
        daySpinner.setOnItemSelectedListener(new DaysSpinnerClass());

        Spinner alarmSpinner = findViewById(R.id.alarmSpin);
        alarmSpinner.setOnItemSelectedListener(new AlarmsSpinnerClass());

        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.textview, days);
        daySpinner.setAdapter(dayAdapter);
        ArrayAdapter<String> alarmAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.textview, alarms);
        alarmSpinner.setAdapter(alarmAdapter);
        daySpinner.getBackground().setColorFilter(getResources().getColor(R.color.grayButNotGray), PorterDuff.Mode.SRC_ATOP);
        alarmSpinner.getBackground().setColorFilter(getResources().getColor(R.color.grayButNotGray), PorterDuff.Mode.SRC_ATOP);
        Calendar calNow = Calendar.getInstance();
        int nowDay = calNow.get(Calendar.DAY_OF_WEEK);
        switch (nowDay){
            case 1:
                nowDay = 6;
                break;
            case 2:
                nowDay = 0;
                break;
            case 3:
                nowDay = 1;
                break;
            case 4:
                nowDay = 2;
                break;
            case 5:
                nowDay = 3;
                break;
            case 6:
                nowDay = 4;
                break;
            case 7:
                nowDay = 5;
                break;
        }
        daySpinner.setSelection(nowDay);
        Drawable daySpinnerDrawable = daySpinner.getBackground().getConstantState().newDrawable();
        Drawable alarmSpinnerDrawable = alarmSpinner.getBackground().getConstantState().newDrawable();

        daySpinnerDrawable.setColorFilter(getResources().getColor(R.color.darkyButNotDark), PorterDuff.Mode.SRC_ATOP);
        alarmSpinnerDrawable.setColorFilter(getResources().getColor(R.color.darkyButNotDark), PorterDuff.Mode.SRC_ATOP);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            daySpinner.setBackground(daySpinnerDrawable);
            alarmSpinner.setBackground(alarmSpinnerDrawable);
        }
        for (int i = 0; i < sCoreDreams.length-1; i++){
            sCoreDreams[i] =  Typeface.createFromAsset(getAssets(), "SCDream"+(i+1)+".otf");
        }

        Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable(0xfff8f9fa));
        ActionBar actionbar = getSupportActionBar();
        TextView textview = new TextView(getApplicationContext());

        layoutParams = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT);
        textview.setLayoutParams(layoutParams);
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

        TextInputLayout textInputLayout1 = findViewById(R.id.til1);
        TextInputLayout textInputLayout2 = findViewById(R.id.til2);
        TextInputLayout textInputLayout3 = findViewById(R.id.til3);

        editCode.addTextChangedListener(new TextWatcher() {
            int prevL = 0;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                prevL = editCode.getText().toString().length();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                boolean checkCharSeq = isEnglishOrNumber(charSequence.toString());
                if(!checkCharSeq){
                    editCode.setText(null);
                    Toast.makeText(AddActivity.this, "Incorrect Code Format", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                int length = editable.length();
                if ((prevL < length) && (length == 3 || length == 8)) {
                    editable.append("-");
                }
            }
        });

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

                    DaysInMonth daysInMonth = new DaysInMonth();
                    daysInMonth.checkLeap(nowYear);

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

                    if(nowDayOfMonth > daysInMonth.getDays(nowMonth-1)){
                       nowDayOfMonth = nowDayOfMonth - daysInMonth.getDays(nowMonth-1);
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

                    if(editCode.getText().toString().length() < 12){
                        editCode.setText(null);
                    }

                    String schName = editName.getText().toString();
                    schName = schName.replaceAll("'", "`");

                    sqlDB = myHelper.getWritableDatabase();
                    String tmp = hourMin[0] + ":"+ hourMin[1];
                    sqlDB.execSQL("insert into scheduleTable(id, day, course, code, alarmTime, activation) values ('"+ idm + "','"+ selectedDay + "','"+ schName + "','"+editCode.getText().toString()+"', '"+ tmp +"', '"+true+"');");
                    sqlDB.execSQL("insert into alarmDetailTable(id, date, alarmbefore) values ('"+ idm + "','"+ newForm + "','"+selectedAlarmBefore+"');");
                    sqlDB.close();
                    openMain();

                }else if(editName.getText().toString().equals("") && editTime.getText().toString().equals("")){
                    textInputLayout1.setError("Enter The Subject Name");
                    textInputLayout3.setError("Set Time for Alarm");
                }else if(editName.getText().toString().equals("") && editName.getText().toString().length() < 1){
                    textInputLayout1.setError("Enter The Subject Name");
                    Toast.makeText(AddActivity.this, "Please Set Name", Toast.LENGTH_SHORT).show();
                }else if(editTime.getText().toString().equals("") && editTime.getText().toString().length() < 1){
                    textInputLayout3.setError("Set Time for Alarm");
                    Toast.makeText(AddActivity.this, "Please Set Time", Toast.LENGTH_SHORT).show();
                }
            }

            private void openMain() {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });
    }

    public boolean isEnglishOrNumber(String s){
        return Pattern.matches("^[0-9a-zA-z-]*$", s);
    }

    public class myDBHelper extends SQLiteOpenHelper {
        public myDBHelper(@Nullable Context context) {
            super(context, "meetDB", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table scheduleTable (id INTEGER PRIMARY KEY AUTOINCREMENT, day INTEGER, course TEXT, code TEXT, alarmTime TEXT, activation TEXT)");
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

    class DaysSpinnerClass implements AdapterView.OnItemSelectedListener{

        @Override
        public void onItemSelected(AdapterView<?> parent, View v, int position, long id){
            selectedDay = days[position].equals("MON") ? 2 :
                            days[position].equals("TUE") ? 3 :
                            days[position].equals("WED") ? 4 :
                            days[position].equals("THU") ? 5 :
                            days[position].equals("FRI") ? 6 :
                            days[position].equals("SAT") ? 7 :
                            days[position].equals("SUN") ? 1 : 1;
            System.out.println("selectedDay = " + selectedDay);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    }

    class AlarmsSpinnerClass implements AdapterView.OnItemSelectedListener{

        @Override
        public void onItemSelected(AdapterView<?> parent, View v, int position, long id){
            selectedAlarmBefore = alarms[position].equals("5m") ? 5 :
                                    alarms[position].equals("10m") ? 10 :
                                    alarms[position].equals("15m") ? 15 :
                                    alarms[position].equals("30m") ? 30 :
                                    alarms[position].equals("1h") ? 60 : 0;
            System.out.println("selectedAlarmBefore = " + selectedAlarmBefore);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    }
}
