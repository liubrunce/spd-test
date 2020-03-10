package com.yuzhua.android.controller

import com.yuzhua.android.utils.*
import org.aspectj.util.FileUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.util.DigestUtils
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.FileInputStream
import java.net.InetAddress
import javax.servlet.MultipartConfigElement

@Controller
class ViewController {
    @Value("\${yz.filePath}")
    private var filePath: String? = null

    @Value("\${server.port}")
    private var port: String? = null

    @Autowired
    lateinit var mpe: MultipartConfigElement

    @RequestMapping("/ipaList")
    fun ipaList(model: Model):String {

        val file = File(mpe.location+"ipa"+File.separator)
        val map = mutableMapOf<String,List<IpaInfo>>()
        file.listFiles()
                .filter { it.isDirectory }
                .forEach {
                    val bundldId = it.name

                    val list = it.listFiles()
                            .filter { it.isDirectory }
                            .map {
                                val version = it.name
                                it.listFiles().filter{ it.name.endsWith(".plist") }
                                        .map {
                                            val ii = IpaInfo()
                                            ii.version = version
                                            ii.name = it.name.substring(0,it.name.lastIndexOf("."))+"_"+version
                                            ii.downUrl = "itms-services://?action=download-manifest&url=https://${InetAddress.getLocalHost().hostAddress}:$port/file/ipa/$bundldId/$version/${it.name}"
                                            ii
                                        }[0]
                            }
                    map[it.name] = list
                }
        model.addAttribute("ipas", map)
        return "/index"
    }
}