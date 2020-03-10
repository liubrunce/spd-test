package com.yuzhua.android.utils

import java.util.ArrayList

open class People
open class World<T:People>
open class Student:People()
open class School< T:Student>:World<T>()

fun test(){
    val list = ArrayList<World<*>>()
    list.add(School<Student>())
}

