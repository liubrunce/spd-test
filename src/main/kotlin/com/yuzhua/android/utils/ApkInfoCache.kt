package com.yuzhua.android.utils

import java.io.File

object ApkInfoCache {

    private val apkInfo:HashMap<String, ApkInfo> = hashMapOf()

    fun getApkInfo(packName:String,tag: ApkTag,dirPath:String):ApkInfo{
        val key = "${packName}_$tag"
        if(apkInfo.containsKey(key)){
            return apkInfo[key]!!
        }else{
            val f = File(dirPath+File.separator+packName+File.separator+"${tag}_apkInfo.json")
            if(!f.exists()){
                return ApkInfo()
            }

            val ai = fromJson<ApkInfo>(f.readText())
            apkInfo[key] = ai
            return ai
        }
    }

    fun setApkInfo(ai: ApkInfo){
        apkInfo["${ai.apkPackage}_${ai.apkTag.name}"] = ai
    }
}