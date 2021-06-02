package com.example.meetscheduler;

public class DaysInMonth {
    private int[] dayCounts = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

    public void checkLeap(int nowYear){
        boolean isLeap = (nowYear % 4 == 0 && nowYear % 100 != 0) || nowYear % 400 == 0;
        if(isLeap){
            dayCounts[1] += 1;
        }
    }

    public int getDays(int nowMonth){
        return dayCounts[nowMonth];
    }

}
