package com.example.meetscheduler;

public class DayComponent {
    private int alpha, textR, textG, textB, bgR, bgG, bgB;
    private String day;
    public void setDay(String day){
        this.day = day;
    }
    public void setTextColors(int alpha, int r, int g, int b){
        this.alpha = alpha;
        this.textR = r;
        this.textG = g;
        this.textB = b;
    }
    public void setBackgroundColors(int alpha, int r, int g, int b){
        this.alpha = alpha;
        this.bgR = r;
        this.bgG = g;
        this.bgB = b;
    }

    public int getAlpha() {
        return alpha;
    }

    public String getDay() {
        return day;
    }

    public int getTextR() {
        return textR;
    }

    public int getTextG() {
        return textG;
    }

    public int getTextB() {
        return textB;
    }

    public int getBgR() {
        return bgR;
    }

    public int getBgG() {
        return bgG;
    }

    public int getBgB() {
        return bgB;
    }
}
