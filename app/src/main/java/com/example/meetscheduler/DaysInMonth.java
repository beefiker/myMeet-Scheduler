package com.example.meetscheduler;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DaysInMonth {
    private int[] dayCounts = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

    public void checkLeap(int nowYear){
        boolean isLeap = (nowYear % 4 == 0 && nowYear % 100 != 0) || nowYear % 400 == 0;
        // sync with myDB day  column format ( 0: mon, 1: tue, 2: wed, ... 6: sun);
        if(isLeap){
            dayCounts[1] += 1;
        }
    }

    public int getDays(int nowMonth){
        return dayCounts[nowMonth];
    }

}
