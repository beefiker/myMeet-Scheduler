package com.example.meetscheduler;

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
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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

import com.bee.meetscheduler.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.regex.Pattern;

public class DetailActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener
{
    final static int SUNDAY = 1;
    final static int MONDAY = 2;
    final static int TUESDAY = 3;
    final static int WEDNESDAY = 4;
    final static int THURSDAY = 5;
    final static int FRIDAY = 6;
    final static int SATURDAY = 7;
    final static int Before0 = 0;
    final static int Before5 = 5;
    final static int Before10 = 10;
    final static int Before15 = 15;
    final static int Before30 = 30;
    final static int Before60 = 60;

    static int STATIC_ID = 0;
    EditText editName, editCode, editTime, editContent;
    Button btnUpdate;
    ImageButton addReview;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch editActivation;
    myDBHelper myHelper;
    SQLiteDatabase sqlDB;
    LinearLayout detailContainer, layoutContainer;
    ActionBar.LayoutParams layoutParams;
    Typeface[] sCoreDreams = new Typeface[9];

    String[] days = {"MON","TUE","WED","THU","FRI","SAT","SUN"};
    String[] alarms = {"0m","5m","10m","15m","30m","1h"};
    int selectedDay = 0;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        finish();
        return true;
    }
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectedDay = position;
        System.out.println(position + " selected");
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
     class DaysSpinnerClass implements AdapterView.OnItemSelectedListener{
        @Override
        public void onItemSelected(AdapterView<?> parent, View v, int position, long id){
            selectedDay = position;
            System.out.println("selectedDay = " + selectedDay);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint({"ResourceAsColor", "SetTextI18n"})
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail);

        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        Spinner daySpinner = findViewById(R.id.daySpin);
        Spinner alarmSpinner = findViewById(R.id.alarmSpin);
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.textview, days);
        ArrayAdapter<String> alarmAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.textview, alarms);
        daySpinner.setAdapter(dayAdapter);
        alarmSpinner.setAdapter(alarmAdapter);
        daySpinner.getBackground().setColorFilter(getResources().getColor(R.color.grayButNotGray), PorterDuff.Mode.SRC_ATOP);
        daySpinner.setOnItemSelectedListener(new DaysSpinnerClass());
        alarmSpinner.setOnItemSelectedListener(null);
        Drawable daySpinnerDrawable = daySpinner.getBackground().getConstantState().newDrawable();
        daySpinnerDrawable.setColorFilter(getResources().getColor(R.color.darkyButNotDark), PorterDuff.Mode.SRC_ATOP);
        Drawable alarmSpinnerDrawable = daySpinner.getBackground().getConstantState().newDrawable();
        alarmSpinnerDrawable.setColorFilter(getResources().getColor(R.color.darkyButNotDark), PorterDuff.Mode.SRC_ATOP);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            daySpinner.setBackground(daySpinnerDrawable);
            alarmSpinner.setBackground(alarmSpinnerDrawable);
        }

        layoutContainer = findViewById(R.id.layoutContainer);
        editContent = findViewById(R.id.editContent);
        addReview = findViewById(R.id.addReview);

        for (int i = 0; i < sCoreDreams.length-1; i++){
            sCoreDreams[i] =  Typeface.createFromAsset(getAssets(), "SCDream"+(i+1)+".otf");
        }

        Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable(0xfff8f9fa));

        ActionBar actionbar = getSupportActionBar();
        TextView textview = new TextView(getApplicationContext());

        btnUpdate = findViewById(R.id.btnUpdate);

        myHelper = new myDBHelper(this);

        Intent intent = getIntent();
        int color_Alpha = intent.getIntExtra("color_Alpha", 0);
        int color_R = intent.getIntExtra("color_R", 0);
        int color_G = intent.getIntExtra("color_G", 0);
        int color_B = intent.getIntExtra("color_B", 0);
        int id = intent.getIntExtra("id", 0);
        int day; // day of week
        String name, code, time;
        String activation;

        STATIC_ID = id;

        detailContainer = findViewById(R.id.detailContainer);
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(Color.argb(color_Alpha, color_R, color_G, color_B));
        shape.setCornerRadius(20);
        detailContainer.setBackground(shape);

        sqlDB = myHelper.getReadableDatabase();
        Cursor cursor = sqlDB.rawQuery("select * from scheduleTable where id = " + STATIC_ID, null);
        cursor.moveToNext();
        final int thisId = cursor.getInt(0);
        day = cursor.getInt(1);
        int nDay = cursor.getInt(1);
        name = cursor.getString(2);
        code = cursor.getString(3);
        time = cursor.getString(4);
        activation = cursor.getString(5);

        Cursor alarmCursor = sqlDB.rawQuery("select * from alarmDetailTable where id = "+ STATIC_ID, null);
        alarmCursor.moveToNext();
        String alarmDate = alarmCursor.getString(1);
        int alarmBefore = alarmCursor.getInt(2);
        alarmCursor.close();
        switch (alarmBefore){
            case 0:
                alarmBefore = 0;
                break;
            case 5:
                alarmBefore = 1;
                break;
            case 10:
                alarmBefore = 2;
                break;
            case 15:
                alarmBefore = 3;
                break;
            case 30:
                alarmBefore = 4;
                break;
            default:
                alarmBefore = 5;
                break;
        }
        alarmSpinner.setSelection(alarmBefore);

        alarmDate = alarmDate.replaceAll("[^0-9]","");
        System.out.println("alarm : " + alarmDate);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, Integer.parseInt(alarmDate.substring(0,4)));
        cal.set(Calendar.MONTH, Integer.parseInt(alarmDate.substring(4,6))-1);
        cal.set(Calendar.DATE, Integer.parseInt(alarmDate.substring(6,8)));

        cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(alarmDate.substring(8,10)));
        cal.set(Calendar.MINUTE, Integer.parseInt(alarmDate.substring(10,12)));
        cal.set(Calendar.SECOND, Integer.parseInt(alarmDate.substring(12,14)));

        switch (day){
            case 1:
                day = 6;
                break;
            case 2:
                day = 0;
                break;
            case 3:
                day = 1;
                break;
            case 4:
                day = 2;
                break;
            case 5:
                day = 3;
                break;
            case 6:
                day = 4;
                break;
            default:
                day = 5;
                break;
        }
        daySpinner.setSelection(day);

        layoutParams = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT);
        textview.setLayoutParams(layoutParams);
        textview.setText(name);
        textview.setTextColor(Color.rgb(33,33,33));
        textview.setTypeface(sCoreDreams[4]);
        textview.setTextSize(20);
        actionbar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionbar.setCustomView(textview);

        actionbar.setDisplayHomeAsUpEnabled(true);
        @SuppressLint("UseCompatLoadingForDrawables")
        Drawable backArrow = getDrawable(R.drawable.ic_baseline_arrow_back_24);
        backArrow.setColorFilter(getResources().getColor(R.color.blueblue), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(backArrow);

        editName = findViewById(R.id.editName);
        editCode = findViewById(R.id.editCode);
        editTime = findViewById(R.id.editTime);
        editActivation = findViewById(R.id.editActivation);

        editName.setText(name);
        editCode.setText(code);
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
                    Toast.makeText(DetailActivity.this, "Incorrect Code Format", Toast.LENGTH_SHORT).show();
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
        String time1 = time.substring(0,2);
        String time2 = time.substring(2);
        editTime.setText(time1 + time2);

        editActivation.setChecked(activation.equals("true"));

        editTime.setOnClickListener(v -> {
            Calendar myCalendar = Calendar.getInstance();
            int hour = myCalendar.get(Calendar.HOUR_OF_DAY);
            int minute = myCalendar.get(Calendar.MINUTE);
            TimePickerDialog timePickerDialog = new TimePickerDialog(DetailActivity.this, android.R.style.Theme_Holo_Dialog_NoActionBar,
                    (view, hourOfDay, minute1) -> {
                        String hourSet = String.valueOf(hourOfDay);
                        String minSet = String.valueOf(minute1);
                        if(hourSet.length() < 2) hourSet = "0" + hourSet;
                        if(minSet.length() < 2) minSet = "0" + minSet;
                        editTime.setText(hourSet + ":" + minSet);
                    }, hour, minute, true);
            timePickerDialog.show();
        });

        switch (nDay){
            case 1:
                nDay = 6;
                break;
            case 2:
                nDay = 0;
                break;
            case 3:
                nDay = 1;
                break;
            case 4:
                nDay = 2;
                break;
            case 5:
                nDay = 3;
                break;
            case 6:
                nDay = 4;
                break;
            case 7:
                nDay = 5;
                break;
        }

        final int dbDay = nDay;
        btnUpdate.setOnClickListener(view -> {
            String updateName = editName.getText().toString();
            String updateCode = editCode.getText().toString();
            boolean updateAct = !editActivation.getText().toString().equals("false");
            int updateDay = daySpinner.getSelectedItemPosition();
            switch(updateDay){
                case 6:
                    updateDay = SUNDAY;
                    break;
                case 0:
                    updateDay = MONDAY;
                    break;
                case 1:
                    updateDay = TUESDAY;
                    break;
                case 2:
                    updateDay = WEDNESDAY;
                    break;
                case 3:
                    updateDay = THURSDAY;
                    break;
                case 4:
                    updateDay = FRIDAY;
                    break;
                default:
                    updateDay = SATURDAY;
                    break;
            }
            String[] hourMin = editTime.getText().toString().split(":");
            if(hourMin[0].length() < 2) hourMin[0] = "0"+hourMin[0];
            if(hourMin[1].length() < 2) hourMin[1] = "0"+hourMin[1];
            String tmp = hourMin[0] + ":"+ hourMin[1];

            if(updateCode.length() < 12){
                editCode.setText(null);
                updateCode = "";
            }

            sqlDB = myHelper.getWritableDatabase();
            sqlDB.execSQL("update scheduleTable set day = '"+updateDay+"', course = '"+updateName+"', code = '"+updateCode+"', alarmTime = '"+tmp+"', activation = '"+updateAct+"'  where id = '"+ thisId +"' ");

            Intent intent1 = new Intent(getApplicationContext(), MainActivity.class);

            Calendar systemCal = Calendar.getInstance();

            int dayGap = dbDay - selectedDay;
            if(dayGap < 0){
                cal.add(Calendar.DATE, Math.abs(dayGap));
            }else if(dayGap > 0){
                cal.add(Calendar.DATE, (7-dayGap));
            }
            int checkTimeGap = (int) (cal.getTimeInMillis() - systemCal.getTimeInMillis());
            if(checkTimeGap > 604800){
                cal.add(Calendar.DATE, -7);
            }
            int year = cal.get(Calendar.YEAR);
            int mon = cal.get(Calendar.MONTH) + 1;
            String newMon = String.valueOf(mon);
            if(newMon.length() < 2) newMon = "0"+newMon;
            int date = cal.get(Calendar.DATE);
            String newDate = String.valueOf(date);
            if(newDate.length() < 2) newDate = "0"+newDate;
            String updateDate = year +"-"+newMon+"-"+newDate+" "+tmp+":00";
            int updateBefore = alarmSpinner.getSelectedItemPosition();
            switch(updateBefore){
                case 0:
                    updateBefore = Before0;
                    break;
                case 1:
                    updateBefore = Before5;
                    break;
                case 2:
                    updateBefore = Before10;
                    break;
                case 3:
                    updateBefore = Before15;
                    break;
                case 4:
                    updateBefore = Before30;
                    break;
                default:
                    updateBefore = Before60;
                    break;
            }
            sqlDB.execSQL("update alarmDetailTable set date = '"+updateDate+"', alarmbefore = '"+updateBefore+"'  where id = '"+ STATIC_ID +"' ");
            sqlDB.close();
            startActivity(intent1);
            finish();
        });

        addReview.setOnClickListener(view -> {
            long now = System.currentTimeMillis();
            Date mDate = new Date(now);
            @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String getTime = simpleDateFormat.format(mDate);
            String content = editContent.getText().toString();
            content = content.replaceAll("'", "`");
            if(editContent.getText().toString().length() > 0) {
                sqlDB = myHelper.getWritableDatabase();
                sqlDB.execSQL("insert into memoTable(num, content, regdate) values ('" + STATIC_ID + "','" + content + "', '" + getTime + "');");
                sqlDB.close();
                showMemos();
                editContent.setText(null);
                inputMethodManager.hideSoftInputFromWindow(editContent.getWindowToken(), 0);
            }else{
                Toast.makeText(this, "메모를 입력해주세요", Toast.LENGTH_SHORT).show();
            }
        });

        cursor.close();
        sqlDB.close();

        showMemos();
    }


    public boolean isEnglishOrNumber(String s){
        return Pattern.matches("^[0-9a-zA-z-]*$", s);
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint({"ResourceAsColor", "RtlHardcoded"})
    public void showMemos(){

        layoutContainer.removeAllViews();
        int memoId;
        String memoContent;
        String memoRegdate;
        sqlDB = myHelper.getReadableDatabase();
        Cursor cursor1 = sqlDB.rawQuery("select * from memoTable where num = + '"+STATIC_ID+"'  order by regdate desc", null);
        while(cursor1.moveToNext()){
            memoId = cursor1.getInt(1);
            memoContent = cursor1.getString(2);
            memoRegdate = cursor1.getString(3);

            LinearLayout hLayout = new LinearLayout(getApplicationContext());
            LinearLayout.LayoutParams hLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            hLayoutParams.setMargins(0, 0, 0, 30);
            hLayout.setPadding(30, 0, 30, 0);
            hLayout.setLayoutParams(hLayoutParams);
            hLayout.setElevation(3);
            hLayout.setMinimumHeight(150);

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
            LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 6f);
            LinearLayout.LayoutParams dateParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 4.0f);

            btnRemoveReview.setLayoutParams(btnParams);
            btnRemoveReview.setBackgroundResource(R.drawable.rounded_box_red);
            btnRemoveReview.setTextSize(17);
            btnRemoveReview.setTextColor(Color.rgb(255, 255, 255));
            btnRemoveReview.setTypeface(sCoreDreams[7]);
            btnRemoveReview.setText(R.string.x);

            tvContent.setLayoutParams(contentParams);
            tvContent.setTextSize(12);
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

    public static class myDBHelper extends SQLiteOpenHelper {
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


}
