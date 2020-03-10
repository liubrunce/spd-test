package com.yuzhua.android.utils;

import java.util.ArrayList;
import java.util.List;

public class Test {
    public void test(){
        List<World<? extends People>> list = new ArrayList<>();
        list.add(new School<Student>());
    }
}
