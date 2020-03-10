package com.yuzhua.android.utils

import com.dd.plist.NSDictionary
import com.dd.plist.NSString
import com.dd.plist.PropertyListParser

import java.io.*
import java.util.Enumeration
import java.util.HashMap
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

object IpaUtil {

    @Throws(Exception::class)
    fun getZipInfo(filePath: String): ByteArray? {
        var datas: ByteArray? = null
        // 定义输入输出流对象
        var zipFile: ZipFile? = null
        try {
            // 创建zip文件对象
            zipFile = ZipFile(filePath)
            // 得到zip文件条目枚举对象
            val zipEnum = zipFile.entries()
            // 定义对象
            var entry: ZipEntry? = null
            var entryName: String? = null
            val names: Array<String>? = null
            val length: Int
            // 循环读取条目
            while (zipEnum.hasMoreElements()) {
                // 得到当前条目
                entry = zipEnum.nextElement()
                entryName = entry!!.name
                if (entryName!!.endsWith(".app/Info.plist")) {
                    // 为Info.plist文件,则输出到文件
                    val input = zipFile.getInputStream(entry)
                    datas = input.readBytes()
                    input.close()
                    break
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        } finally {
            // 必须关流，否则文件无法删除
            if (zipFile != null) {
                zipFile.close()
            }
        }
        return datas
    }

    /**
     * 通过IPA文件获取Info信息
     */
    @Throws(Exception::class)
    fun getVersionInfo(ipa: String): Map<String, String> {

        val bytes = getZipInfo(ipa)
//        println(String(bytes!!))
        val map = HashMap<String, String>()
        // 需要第三方jar包dd-plist
        val rootDict = PropertyListParser.parse(bytes!!) as NSDictionary
        // 应用包名
        map["cfBundleIdentifier"] = (rootDict.objectForKey("CFBundleIdentifier") as NSString?).toString()
        // 应用名称
        map["cfBundleName"] = (rootDict.objectForKey("CFBundleName") as NSString?).toString()
        // 应用版本
        map["cfBundleVersion"] = (rootDict.objectForKey("CFBundleShortVersionString") as NSString?).toString()+"."+(rootDict.objectForKey("CFBundleVersion") as NSString?).toString()
        // 应用展示的名称
        map["cfBundleDisplayName"] = (rootDict.objectForKey("CFBundleDisplayName") as NSString?).toString()
        // 应用所需IOS最低版本
        map["minimumOSVersion"] = (rootDict.objectForKey("MinimumOSVersion") as NSString?).toString()
        map["cfBundleShortVersionString"] = (rootDict.objectForKey("CFBundleShortVersionString") as NSString?).toString()

        return map
    }


}
