package com.example.googlemeetscheduler;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
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
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Objects;

public class subActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    Button addSchedule;
    EditText editName, editTime, editCode;
    myDBHelper myHelper;
    SQLiteDatabase sqlDB;
    ActionBar.LayoutParams layoutparams;
    Typeface[] sCoreDreams = new Typeface[9];

    String[] days = {"월요일","화요일","수요일","목요일","금요일","토요일","일요일"};
    int selectedDay = 0;

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

        Spinner spinner = findViewById(R.id.daySpin);
        ArrayAdapter<String> adpter = new ArrayAdapter<>(getApplicationContext(), R.layout.textview, days);
        spinner.setAdapter(adpter);
        spinner.getBackground().setColorFilter(getResources().getColor(R.color.grayButNotGray), PorterDuff.Mode.SRC_ATOP);
        spinner.setOnItemSelectedListener(this);
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
                TimePickerDialog timePickerDialog = new TimePickerDialog(subActivity.this, android.R.style.Theme_Holo_Dialog_NoActionBar,
                        (view1, hourOfDay, minute1) -> {
                            String hourSet = String.valueOf(hourOfDay);
                            String minSet = String.valueOf(minute1);
                            if(hourSet.length() < 2) hourSet = "0" + hourSet;
                            if(minSet.length() < 2) minSet = "0" + minSet;
                            editTime.setText(hourSet + ":" + minSet);
                        }, hour, minute, true);
                timePickerDialog.show();

        });

        addSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!editName.getText().toString().equals("") && editName.getText().toString().length() > 0 && editTime.getText().toString().length() > 0){

                    sqlDB = myHelper.getWritableDatabase();

                    String[] hourMin = editTime.getText().toString().split(":");
                    if(hourMin[0].length() < 2) hourMin[0] = "0"+hourMin[0];
                    if(hourMin[1].length() < 2) hourMin[1] = "0"+hourMin[1];
                    String tmp = hourMin[0] + ":"+ hourMin[1];
                    sqlDB.execSQL("insert into scheduleTable(day, course, code, alarmTime, activation) values ('"+ selectedDay + "','"+ editName.getText().toString() + "','"+editCode.getText().toString()+"', '"+ tmp +"', '"+true+"');");
                    sqlDB.close();
                    openMain();

                }else if(editTime.getText().toString().equals("") && editTime.getText().toString().length() < 1){
                    sqlDB = myHelper.getWritableDatabase();
                    sqlDB.execSQL("insert into scheduleTable(day, course, code, alarmTime, activation) values ('"+ selectedDay + "','"+ editName.getText().toString() + "','"+editCode.getText().toString()+"', '"+ "00:00" +"', '"+true+"');");
                    sqlDB.close();
                    openMain();
                }else {
                    Toast.makeText(subActivity.this, "제목을 입력해주세요", Toast.LENGTH_SHORT).show();
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
        selectedDay = days[position].equals("월요일") ? 0 :
                        days[position].equals("화요일") ? 1 :
                        days[position].equals("수요일") ? 2 :
                        days[position].equals("목요일") ? 3 :
                        days[position].equals("금요일") ? 4 :
                        days[position].equals("토요일") ? 5 :
                        days[position].equals("일요일") ? 6 : 0;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public class myDBHelper extends SQLiteOpenHelper {
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
}
