package com.yuzhua.android.controller

import com.yuzhua.android.utils.ApkInfoCache
import com.yuzhua.android.utils.ApkTag
import com.yuzhua.android.utils.ApkUtil
import com.yuzhua.android.utils.IpaUtil
import org.aspectj.util.FileUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.ui.Model
import org.springframework.util.DigestUtils
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.FileInputStream
import java.net.InetAddress
import javax.servlet.MultipartConfigElement

@RestController
@RequestMapping("api/file")
class FileController {
    @Value("\${yz.filePath}")
    private var filePath: String? = null

    @Value("\${server.port}")
    private var port: String? = null

    @Autowired
    lateinit var mpe: MultipartConfigElement


    @PostMapping("/test1")
    fun testPush1(@RequestParam("fileName") file : String):Any{
//        println(file)
        println(file.toString())
        return file
    }

    @GetMapping("/test2")
    fun testPush2(@RequestParam("fileName") file : String):Any{
//        println(file)
        println(file.toString())
        return file
    }


    @PostMapping("/fileUpload")
    fun testPush(@RequestParam("fileName") file : MultipartFile):Any{
//        println(file)
        println(file.toString())
        val s = buildString {
            appendln(file.name)
            appendln(file.originalFilename)
            appendln(file.contentType)
            appendln(file.size)
        }
        return try {
            File(filePath).apply {
                if(!exists()){
                    if(!mkdirs()){
                        return "目录创建失败"
                    }
                }
            }
            file.transferTo(File(filePath+(file.originalFilename?:file.name)))
            "true\n$s"
        }catch (e:Exception){
            e.printStackTrace()
            "false\n$s"
        }
    }


    @PostMapping("/updateApk")
    fun updateApk(@RequestParam("file") file : MultipartFile,
                  @RequestParam("desc") desc:String,
                  @RequestParam("level",required = false,defaultValue = "0") level:Int,
                  @RequestParam("tag",required = false,defaultValue = "DEBUG") tag: ApkTag):Any{
        if(file.contentType != "application/vnd.android.package-archive"){
            return "Please upload the apk file"
        }

        val apkPath = "apk"+File.separator
        val apkDir = File(mpe.location+apkPath)
        if(!apkDir.exists()){
            apkDir.mkdirs()
        }
        val fileName = apkPath+"${System.currentTimeMillis()}_${tag.name}_apkfile.apk"
        return try {
            //保存文件
            file.transferTo(File(fileName))
//            DigestUtils.md5Hex(new FileInputStream(path));
            val md5 = DigestUtils.md5DigestAsHex(FileInputStream(mpe.location+fileName))
            val tempFile = File(mpe.location+fileName)
//            val md5 = MD5Encoder.encode(tempFile.readBytes())
            //读取apk基本信息
            val apkInfo = ApkUtil.readAPK(mpe.location + fileName)
            //获取缓存版本号
            val cacheApkInfo = ApkInfoCache.getApkInfo(apkInfo.apkPackage,tag,mpe.location+apkPath)
            //判断版本号
            if(apkInfo.versionCode <= cacheApkInfo.versionCode){
                return "The uploaded version of the application cannot be less than or equal to the historical version"
            }
            //移动临时版本
            val tempFile1 = File(mpe.location+apkPath+apkInfo.apkPackage+File.separator+apkInfo.apkPackage+"_"+tag.name+"_"+apkInfo.versionName+".apk")
            FileUtil.copyFile(tempFile,tempFile1)
            //删除上一个版本
            if(!cacheApkInfo.isEmpty){
                File(mpe.location+apkPath+cacheApkInfo.apkPackage+File.separator+cacheApkInfo.apkPackage+"_"+tag.name+"_"+cacheApkInfo.versionName+".apk").delete()
            }
            //保存apk信息
            apkInfo.desc = desc
            apkInfo.size = file.size
            apkInfo.level = level
            apkInfo.apkTag = tag
            apkInfo.md5 = md5
            File(mpe.location+apkPath+apkInfo.apkPackage+File.separator+"${tag}_apkInfo.json").writeText(apkInfo.toString())
            ApkInfoCache.setApkInfo(apkInfo)
            "Successfully uploaded application"
        }catch (e:Exception){
            e.printStackTrace()
            "Failed to upload application"
        }finally {
            val tempFile = File(mpe.location+fileName)
            if(tempFile.exists()){
                tempFile.delete()
            }
        }
    }

    @GetMapping("/getApk")
    fun getApk(@RequestParam("packName") packName : String,@RequestParam("tag",required = false,defaultValue = "RELEASE") tag:ApkTag):Any{
        return ApkInfoCache.getApkInfo(packName,tag,mpe.location+"apk"+File.separator).apply {
            downloadUrl = File.separator+"file"+File.separator+"apk"+File.separator+apkPackage+File.separator+apkPackage+"_"+tag.name+"_"+versionName+".apk"
        }
    }

    @PostMapping("/updateIpa")
    fun updateIpa(@RequestParam("file") file : MultipartFile):Any {
        val ipaPath = "ipa"+File.separator
        val ipaDir = File(ipaPath)
        if(!ipaDir.exists()){
            ipaDir.mkdirs()
        }

        val fileName = ipaPath+"${System.currentTimeMillis()}_ipafile.ipa"
        return try {
            //保存ipa文件
            file.transferTo(File(fileName))
            val tempFile = File(mpe.location+fileName)
            val map = IpaUtil.getVersionInfo(mpe.location+fileName)
            val filePath = mpe.location+ipaPath+map["cfBundleIdentifier"]+File.separator+map["cfBundleVersion"]+File.separator
            val ipaDir1 = File(filePath)
            if(!ipaDir1.exists()){
                ipaDir1.mkdirs()
            }
            FileUtil.copyFile(tempFile,File(filePath+map["cfBundleName"]+".ipa"))
            //保存plist文件
            val plist = File(filePath+map["cfBundleName"]+".plist")
            plist.writeText("""
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
	<key>items</key>
	<array>
		<dict>
			<key>assets</key>
			<array>
				<dict>
					<key>kind</key>
					<string>software-package</string>
					<key>url</key>
                        <string>https://${InetAddress.getLocalHost().hostAddress}:$port${"/file/ipa/"+map["cfBundleIdentifier"]+"/"+map["cfBundleVersion"]+"/"+map["cfBundleName"]+".ipa"}</string>
				</dict>
				<dict>
					<key>kind</key>
					<string>display-image</string>
					<key>url</key>
					<string>https://www.yidian51.com/images/common/logo_4.png</string>
				</dict>
				<dict>
					<key>kind</key>
					<string>full-size-image</string>
					<key>url</key>
					<string>https://www.yidian51.com/images/common/logo_4.png</string>
				</dict>
			</array>
			<key>metadata</key>
			<dict>
				<key>bundle-identifier</key>
				<string>${map["cfBundleIdentifier"]}</string>
				<key>bundle-version</key>
				<string>${map["cfBundleVersion"]}</string>
				<key>kind</key>
				<string>software</string>
				<key>title</key>
				<string>${map["cfBundleName"]}</string>
			</dict>
		</dict>
	</array>
</dict>
</plist>
            """.trimIndent())
            "Successfully uploaded application"
        }catch (e:Exception){
            e.printStackTrace()
            "Failed to upload application"
        }finally {
            val tempFile = File(mpe.location+fileName)
            if(tempFile.exists()){
                tempFile.delete()
            }
        }
    }
}