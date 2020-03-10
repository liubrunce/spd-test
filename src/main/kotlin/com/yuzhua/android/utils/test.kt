package com.yuzhua.android.utils

import java.io.File

fun main(args: Array<String>) {
    val l = 2883286170
    println(l shl 1)
}

/**
 * 主要是申请软著用的清除代码注释的空白行的方法
 */
fun clearDirDoc(){
    //需要清除的根目录
    val file = "F:\\Extends\\Android\\yidianwuyou\\app\\src\\main\\java"
    //保存的地址
    val saveFile = "F:\\yidianwuyou.txt"
    val sb = StringBuffer()
    foreachFile(file,sb)
    File(saveFile).writeText(sb.toString())
}

fun foreachFile(filePath:String,sb:StringBuffer){
    var file = File(filePath)
    file.listFiles().forEach {
        if(it.isDirectory){
            foreachFile(it.path,sb)
        }else if(it.name.endsWith(".kt") || it.name.endsWith(".java")){
            sb.appendln()
            sb.appendln(clearDoc(it.path))
            sb.appendln()
        }
    }
}


fun clearDoc(filePath:String):String{
    var file = File(filePath).readText()
    val patterns = hashMapOf(
            "([^:])\\/\\/.*".toRegex() to "$1",
            "\\s+\\/\\/.*".toRegex() to  "",
            "^\\/\\/.*".toRegex() to "",
            "^\\/\\*\\*.*\\*\\/$".toRegex() to "",
            "\\/\\*.*\\*\\/".toRegex() to "",
            "/\\*(\\s*\\*\\s*.*\\s*?)*\\*\\/".toRegex() to "",
            "\n\r+".toRegex() to ""
    )

    patterns.forEach { t, u -> file = file.replace(t,u) }
    return file
}

/**
 * 生成工号和密码
 */
fun creatJNmberAndPassword(){
    val jNumber = arrayListOf<String>("17815","17816","17905","17785")
    val sqls = StringBuilder()
    val ghs = StringBuilder()

    jNumber.forEach {
        val mima = getMima(6)
        sqls.appendln("update oa_staff set login_check_code = '$mima' where j_number = '$it';")
        ghs.appendln("工号：$it\t密码：$mima")
    }

    println(sqls.toString())
    println(ghs.toString())
}

/**
 * 生成密码
 */
fun getMima(size:Int):String{
    val strAll = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
    return buildString {
        (0 until size).forEach {
            val f = (Math.random()*62).toInt()
            append(strAll[f])
        }
    }
}

