package com.example.googlemeetscheduler;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

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

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subactivity_main);

        Spinner spinner = (Spinner) findViewById(R.id.daySpin);
        ArrayAdapter<String> adpter = new ArrayAdapter<String>(getApplicationContext(), R.layout.textview, days);
        spinner.setAdapter(adpter);
        spinner.getBackground().setColorFilter(getResources().getColor(R.color.grayButNotGray), PorterDuff.Mode.SRC_ATOP);
        spinner.setOnItemSelectedListener((AdapterView.OnItemSelectedListener) this);
        Drawable spinnerDrawable = spinner.getBackground().getConstantState().newDrawable();

        spinnerDrawable.setColorFilter(getResources().getColor(R.color.blueblue), PorterDuff.Mode.SRC_ATOP);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            spinner.setBackground(spinnerDrawable);
        }else{
            spinner.setBackgroundDrawable(spinnerDrawable);
        }
        for (int i = 0; i < sCoreDreams.length-1; i++){
            sCoreDreams[i] =  Typeface.createFromAsset(getAssets(), "SCDream"+(i+1)+".otf");
        }

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xfff8f9fa));
        ActionBar actionbar = getSupportActionBar();
        TextView textview = new TextView(getApplicationContext());

        layoutparams = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT);
        textview.setLayoutParams(layoutparams);
        textview.setText("Add Schedule");
        textview.setTextColor(Color.rgb(33,33,33));
        textview.setTypeface(sCoreDreams[4], Typeface.BOLD);
        textview.setTextSize(19);
        actionbar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionbar.setCustomView(textview);

        actionbar.setDisplayHomeAsUpEnabled(true);
        Drawable backArrow = getResources().getDrawable(R.drawable.ic_baseline_arrow_back_24);
        backArrow.setColorFilter(getResources().getColor(R.color.blueblue), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(backArrow);

        addSchedule = findViewById(R.id.addSchedule);
        editName = findViewById(R.id.editName);
        editTime = findViewById(R.id.editTime);
        editCode = findViewById(R.id.editCode);

        myHelper = new myDBHelper(this);

        editTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar myCalendar = Calendar.getInstance();
                int hour = myCalendar.get(Calendar.HOUR_OF_DAY);
                int minute = myCalendar.get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog = new TimePickerDialog(subActivity.this, android.R.style.Theme_Holo_Dialog_NoActionBar,
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                                editTime.setText(hourOfDay + ":" + minute);
                            }
                        }, hour, minute, true);
                timePickerDialog.show();
            }
        });

        addSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sqlDB = myHelper.getWritableDatabase();
                String tmp = editTime.getText().toString().replaceAll(":","");
                tmp = tmp.length() == 3 ? "0"+tmp : tmp;
                sqlDB.execSQL("insert into scheduleTable(day, course, code, alarmTime, activation) values ('"+ selectedDay + "','"+ editName.getText().toString() + "','"+editCode.getText().toString()+"', '"+ tmp +"', '"+false+"');");

                sqlDB.close();
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
        Toast.makeText(this, "selected " + selectedDay, Toast.LENGTH_SHORT).show();
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
