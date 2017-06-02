package com.example.xg.myapplication.com;

/**
 * Created by XG on 2017/6/2.
 */
public class Base {
    private  static final int countName = 0;
    private String strName;

    public static int getCountName() {
        return countName;
    }

    public String getStrName() {
        return strName;
    }

    public void setStrName(String strName) {
        this.strName = strName;
    }
}
