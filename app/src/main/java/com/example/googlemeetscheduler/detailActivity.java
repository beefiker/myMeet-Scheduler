package com.example.googlemeetscheduler;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Calendar;

public class detailActivity extends AppCompatActivity {
    EditText editName, editCode, editTime;
    Button btnUpdate;
    Switch editActivation;
    myDBHelper myHelper;
    SQLiteDatabase sqlDB;
    ActionBar.LayoutParams layoutparams;
    Typeface[] sCoreDreams = new Typeface[9];

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        finish();
        return true;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail);

        for (int i = 0; i < sCoreDreams.length-1; i++){
            sCoreDreams[i] =  Typeface.createFromAsset(getAssets(), "SCDream"+(i+1)+".otf");
        }

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xfff8f9fa));

        ActionBar actionbar = getSupportActionBar();
        TextView textview = new TextView(getApplicationContext());

        btnUpdate = findViewById(R.id.btnUpdate);

//
//        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xfff8f9fa));
//
//        Toolbar actionBarToolbar = findViewById(R.id.action_bar);
//        if (actionBarToolbar != null) actionBarToolbar.setTitleTextColor(Color.rgb(33,33,33));

        myHelper = new myDBHelper(this);

        Intent intent = getIntent();
        int id = intent.getIntExtra("id", 0);
        String nid = "", name = "", code = "", time = "";
        String activation = "false";

        sqlDB = myHelper.getReadableDatabase();

        @SuppressLint("Recycle") Cursor cursor = sqlDB.rawQuery("select * from scheduleTable where id = " + id, null);
        cursor.moveToNext();
        final int thisId = cursor.getInt(0);
        nid = cursor.getString(0);
        name = cursor.getString(1);
        code = cursor.getString(2);
        time = cursor.getString(3);
        activation = cursor.getString(4);

        layoutparams = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT);
        textview.setLayoutParams(layoutparams);
        textview.setText(name);
        textview.setTextColor(Color.rgb(33,33,33));
        textview.setTypeface(sCoreDreams[4]);
        textview.setTextSize(20);
        actionbar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionbar.setCustomView(textview);

        actionbar.setDisplayHomeAsUpEnabled(true);
        Drawable backArrow = getResources().getDrawable(R.drawable.ic_baseline_arrow_back_24);
        backArrow.setColorFilter(getResources().getColor(R.color.blueblue), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(backArrow);

        editName = findViewById(R.id.editName);
        editCode = findViewById(R.id.editCode);
        editTime = findViewById(R.id.editTime);
        editActivation = findViewById(R.id.editActivation);

        editName.setText(name);
        editCode.setText(code);
        editTime.setText(time);
        if (activation.equals("true")) {
            editActivation.setChecked(true);
        } else {
            editActivation.setChecked(false);
        }

        editTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar myCalendar = Calendar.getInstance();
                int hour = myCalendar.get(Calendar.HOUR_OF_DAY);
                int minute = myCalendar.get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog = new TimePickerDialog(detailActivity.this, android.R.style.Theme_Holo_Dialog_NoActionBar,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                editTime.setText(hourOfDay + ":" + minute);
                            }
                        }, hour, minute, true);
                timePickerDialog.show();
            }
        });

        btnUpdate.setOnClickListener(view -> {
            String tempName = editName.getText().toString();
            String tempCode = editCode.getText().toString();
            String tempTime = editTime.getText().toString();
            Toast.makeText(this, "ID : " + id + "\nname : " + tempName + "\nCode : " + tempCode + "\ntime : " + tempTime +"\nActive : " + editActivation.isChecked() , Toast.LENGTH_SHORT).show();
        });


    }

    public static class myDBHelper extends SQLiteOpenHelper {
        public myDBHelper(@Nullable Context context) {
            super(context, "meetDBtest", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table scheduleTable (id INTEGER PRIMARY KEY AUTOINCREMENT, course TEXT, code TEXT, alarmTime TEXT, activation TEXT)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("drop table if exists scheduleTable");
            onCreate(db);
        }
    }
}
