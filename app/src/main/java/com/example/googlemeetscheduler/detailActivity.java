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
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class detailActivity extends AppCompatActivity  implements AdapterView.OnItemSelectedListener{

    static int STATIC_ID = 0;

    EditText editName, editCode, editTime, editContent;
    Button btnUpdate, addReview;
    Switch editActivation;
    myDBHelper myHelper;
    SQLiteDatabase sqlDB;
    LinearLayout layoutContainer;
    ActionBar.LayoutParams layoutparams;
    Typeface[] sCoreDreams = new Typeface[9];

    String[] days = {"월요일","화요일","수요일","목요일","금요일","토요일","일요일"};
    int selectedDay = 0;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        finish();
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail);

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
        int day = 0;
        String nid = "", name = "", code = "", time = "";
        String activation = "false";

        STATIC_ID = id;

        sqlDB = myHelper.getReadableDatabase();
        Cursor cursor = sqlDB.rawQuery("select * from scheduleTable where id = " + STATIC_ID, null);
        cursor.moveToNext();
        final int thisId = cursor.getInt(0);
        nid = cursor.getString(0);
        day = cursor.getInt(1);
        name = cursor.getString(2);
        code = cursor.getString(3);
        time = cursor.getString(4);
        activation = cursor.getString(5);


        spinner.setSelection(day);

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

        String time1 = time.toString().substring(0,2);
        String time2 = time.toString().substring(2);
        editTime.setText(time1 + ":" + time2);

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
                        (view, hourOfDay, minute1) -> editTime.setText(hourOfDay + ":" + minute1), hour, minute, true);
                timePickerDialog.show();
            }
        });

        btnUpdate.setOnClickListener(view -> {
            String updateName = editName.getText().toString();
            String updateCode = editCode.getText().toString();
            String updateAct = editActivation.getText().toString();
            int updateDay = spinner.getSelectedItemPosition();
            String updateTime = editTime.getText().toString().replaceAll(":","");
            updateTime = updateTime.length() == 3 ? "0"+updateTime : updateTime;
            sqlDB = myHelper.getWritableDatabase();
            sqlDB.execSQL("update scheduleTable set day = '"+updateDay+"', course = '"+updateName+"', code = '"+updateCode+"', alarmTime = '"+updateTime+"', activation = '"+updateAct+"'  where id = '"+ thisId +"' ");
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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint({"ResourceAsColor", "RtlHardcoded"})
    public void showMemos(){

        layoutContainer.removeAllViews();
        int memoId = 0;
        String memoContent = "";
        String memoRegdate = "";
        sqlDB = myHelper.getReadableDatabase();
//        Cursor cursor1 = sqlDB.rawQuery("select * from memoTable where num = " + STATIC_ID, null);
        Cursor cursor1 = sqlDB.rawQuery("select * from memoTable where num = + '"+STATIC_ID+"'  order by regdate desc", null);
        while(cursor1.moveToNext()){
            memoId = cursor1.getInt(1);
            memoContent = cursor1.getString(2);
            memoRegdate = cursor1.getString(3);

            LinearLayout hLayout = new LinearLayout(getApplicationContext());
            LinearLayout.LayoutParams hLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            hLayoutParams.setMargins(0, 0, 0, 40);
            hLayout.setPadding(30, 0, 30, 0);
            hLayout.setLayoutParams(hLayoutParams);
            hLayout.setElevation(5);
            hLayout.setMinimumHeight(170);

            hLayout.setBackgroundResource(R.drawable.rounded_box);

            LinearLayout.LayoutParams LeftSideLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT,1);
            LinearLayout insidehLayoutLeftSide = new LinearLayout(getApplicationContext());
            insidehLayoutLeftSide.setLayoutParams(LeftSideLayoutParams);
            insidehLayoutLeftSide.setOrientation(LinearLayout.VERTICAL);
            insidehLayoutLeftSide.setPadding(30,30,30,30);

            Button btnRemoveReview = new Button(getApplicationContext());
            TextView tvContent = new TextView(getApplicationContext());
            TextView tvDate = new TextView(getApplicationContext());
            LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
            LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 4);
            LinearLayout.LayoutParams dateParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 4.0f);
            dateParams.setMargins(0, 0, 0, 10);
            btnParams.setMargins(30, 30, 30, 30);
            btnRemoveReview.setLayoutParams(btnParams);
            btnRemoveReview.setBackgroundResource(R.drawable.rounded_box_lightgray);
            btnRemoveReview.setTextSize(17);
            btnRemoveReview.setTextColor(Color.rgb(255, 0, 0));
            btnRemoveReview.setTypeface(sCoreDreams[7]);
            btnRemoveReview.setText("X");

            tvContent.setLayoutParams(contentParams);
            tvContent.setTextSize(11);
            tvContent.setTextColor(Color.rgb(33,37,41));
            tvContent.setGravity(Gravity.CENTER_VERTICAL);
            tvContent.setTypeface(sCoreDreams[4]);

            tvContent.setText(memoContent);

            tvDate.setLayoutParams(dateParams);
            tvDate.setTextSize(7);
            tvDate.setTextColor(Color.rgb(33,37,41));
            tvDate.setGravity(Gravity.LEFT);
            tvDate.setTypeface(sCoreDreams[2]);
            tvDate.setText(memoRegdate);

            int finalMemoId = memoId;
            btnRemoveReview.setOnClickListener(view -> {
                sqlDB = myHelper.getWritableDatabase();
                sqlDB.execSQL("delete from memoTable where id = '"+ finalMemoId +"'");
                sqlDB.close();

                showMemos();
            });

            insidehLayoutLeftSide.addView(tvDate);
            insidehLayoutLeftSide.addView(tvContent);

            hLayout.addView(insidehLayoutLeftSide);
            hLayout.addView(btnRemoveReview);

            layoutContainer.addView(hLayout);


        }
        cursor1.close();
        sqlDB.close();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

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
