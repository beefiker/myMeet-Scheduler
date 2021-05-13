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
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class detailActivity extends AppCompatActivity {
    EditText editName, editCode, editTime, editContent;
    Button btnUpdate, addReview;
    Switch editActivation;
    myDBHelper myHelper;
    SQLiteDatabase sqlDB;
    LinearLayout layoutContainer;
    ActionBar.LayoutParams layoutparams;
    Typeface[] sCoreDreams = new Typeface[9];

    static int STATIC_ID = 0;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        finish();
        return true;
    }

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail);

        layoutContainer = findViewById(R.id.layoutContainer);
        editContent = findViewById(R.id.editContent);
        addReview = findViewById(R.id.addReview);

        for (int i = 0; i < sCoreDreams.length-1; i++){
            sCoreDreams[i] =  Typeface.createFromAsset(getAssets(), "SCDream"+(i+1)+".otf");
        }

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xfff8f9fa));

        ActionBar actionbar = getSupportActionBar();
        TextView textview = new TextView(getApplicationContext());

        btnUpdate = findViewById(R.id.btnUpdate);

        myHelper = new myDBHelper(this);

        Intent intent = getIntent();
        int id = intent.getIntExtra("id", 0);
        String nid = "", name = "", code = "", time = "";
        String activation = "false";

        STATIC_ID = id;

        sqlDB = myHelper.getReadableDatabase();
        Cursor cursor = sqlDB.rawQuery("select * from scheduleTable where id = " + STATIC_ID, null);
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
            String updateName = editName.getText().toString();
            String updateCode = editCode.getText().toString();
            String updateTime = editTime.getText().toString();
            String updateAct = editActivation.getText().toString();
            sqlDB = myHelper.getWritableDatabase();
            sqlDB.execSQL("update scheduleTable set course = '"+updateName+"', code = '"+updateCode+"', alarmTime = '"+updateTime+"', activation = '"+updateAct+"'  where id = '"+ thisId +"' ");
            sqlDB.close();
            Intent intent1 = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent1);
            finish();
        });

        addReview.setOnClickListener(view -> {
            long now = System.currentTimeMillis();
            Date mDate = new Date(now);
            @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String getTime = simpleDateFormat.format(mDate);

            sqlDB = myHelper.getWritableDatabase();
            sqlDB.execSQL("insert into memoTable(num, content, regdate) values ('"+ STATIC_ID + "','"+editContent.getText().toString()+"', '"+ getTime +"');");
            sqlDB.close();
            showMemos();
        });

        cursor.close();
        sqlDB.close();

        showMemos();
    }

    @SuppressLint({"ResourceAsColor", "RtlHardcoded"})
    public void showMemos(){

        layoutContainer.removeAllViews();
        int memoId = 0;
        String memoContent = "";
        String memoRegdate = "";
        sqlDB = myHelper.getReadableDatabase();
        Cursor cursor1 = sqlDB.rawQuery("select * from memoTable where num = " + STATIC_ID, null);
        while(cursor1.moveToNext()){
            memoId = cursor1.getInt(1);
            memoContent = cursor1.getString(2);
            memoRegdate = cursor1.getString(3);


            LinearLayout hLayout = new LinearLayout(getApplicationContext());
            LinearLayout.LayoutParams hLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            hLayout.setLayoutParams(hLayoutParams);

            LinearLayout dividerLayout = new LinearLayout(getApplicationContext());
            LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
            dividerLayout.setBackgroundColor(R.color.grayButNotGray);
            dividerLayout.setLayoutParams(dividerParams);

            Button tvId = new Button(getApplicationContext());
            TextView tvContent = new TextView(getApplicationContext());
            TextView tvDate = new TextView(getApplicationContext());
            LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 4.0f);
            LinearLayout.LayoutParams dateParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 4.0f);
            dateParams.setMargins(0, 0, 0, 10);

            params.setMargins(50, 10, 0, 0);
            tvId.setLayoutParams(params);
            tvId.setTextSize(17);
            tvId.setTextColor(Color.rgb(255, 0, 0));
            tvId.setTypeface(sCoreDreams[7]);
            tvId.setText("X");

            tvContent.setLayoutParams(contentParams);
            tvContent.setTextSize(11);
            tvContent.setTextColor(Color.rgb(33,37,41));
            tvContent.setGravity(Gravity.CENTER_VERTICAL);
            tvContent.setTypeface(sCoreDreams[4]);

            tvContent.setText(memoContent);

            tvDate.setLayoutParams(dateParams);
            tvDate.setTextSize(9);
            tvDate.setTextColor(Color.rgb(33,37,41));
            tvDate.setGravity(Gravity.LEFT);
            tvDate.setTypeface(sCoreDreams[3]);
            tvDate.setText(memoRegdate);

            int finalMemoId = memoId;
            tvId.setOnClickListener(view -> {
                sqlDB = myHelper.getWritableDatabase();
                sqlDB.execSQL("delete from memoTable where id = '"+ finalMemoId +"'");
                sqlDB.close();

                Toast.makeText(this, "삭제된 메모 : " + finalMemoId, Toast.LENGTH_SHORT).show();
                showMemos();
            });

            hLayout.addView(tvContent);
            hLayout.addView(tvId);

            layoutContainer.addView(hLayout);
            layoutContainer.addView(tvDate);
            layoutContainer.addView(dividerLayout);

        }
        cursor1.close();
        sqlDB.close();
    }

    public static class myDBHelper extends SQLiteOpenHelper {
        public myDBHelper(@Nullable Context context) {
            super(context, "meetDBtest", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table memoTable (num INTEGER, id INTEGER PRIMARY KEY AUTOINCREMENT, content TEXT, regdate TEXT)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("drop table if exists scheduleTable");
            onCreate(db);
        }
    }
}
