package com.yuzhua.android.utils;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ApkInfo{
    private int versionCode = -1;
    private String versionName = "-1";
    private String apkPackage = "null";
    private String minSdkVersion = "-1";
    private String targetSdkVersion = "-1";
    private String desc = "";
    private String downloadUrl = "";
    private int level = 0; //更新等级，0 表示普通更新，不强制；1 表示重要更新，强制更新。
    private ApkTag apkTag = ApkTag.RELEASE;
    private String md5 = "";

    public ApkTag getApkTag() {
        return apkTag;
    }

    public void setApkTag(ApkTag apkTag) {
        this.apkTag = apkTag;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    private long size = 0;

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    private List<String> usesPermission = new ArrayList<>();

    public String getTargetSdkVersion() {
        return targetSdkVersion;
    }

    public void setTargetSdkVersion(String targetSdkVersion) {
        this.targetSdkVersion = targetSdkVersion;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getApkPackage() {
        return apkPackage;
    }

    public void setApkPackage(String apkPackage) {
        this.apkPackage = apkPackage;
    }

    public String getMinSdkVersion() {
        return minSdkVersion;
    }

    public void setMinSdkVersion(String minSdkVersion) {
        this.minSdkVersion = minSdkVersion;
    }

    public List<String> getUsesPermission() {
        return usesPermission;
    }

    public void setUsesPermission(List<String> uses_permission) {
        this.usesPermission = uses_permission;
    }

    public void addUserPermission(String user_permission){
        this.usesPermission.add(user_permission);
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "";
        }
    }

    public boolean isEmpty(){
        return apkPackage.equals("null") && versionCode == -1 && versionName.equals("-1");
    }
}
