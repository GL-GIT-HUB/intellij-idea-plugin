package com.example.xg.myapplication;

import com.example.xg.myapplication.com.Base;

/**
 * Created by XG on 2017/6/1.
 */
public class BaseBean extends Base {
    private String name;
    private static int age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

}
