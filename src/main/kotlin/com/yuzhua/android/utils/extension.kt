package com.yuzhua.android.utils

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.ValueNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.math.BigDecimal
import java.math.BigInteger
import java.net.InetAddress
import java.util.stream.Collectors
import javax.servlet.http.HttpServletRequest


/**
 * 获取客户端地址
 */
fun HttpServletRequest.getIpAddr(): String {
    var ip = getHeader("x-forwarded-for")
    if (ip.isNullOrBlank() || "unknown".equals(ip, ignoreCase = true)) {
        ip = getHeader("Proxy-Client-IP")
    }
    if (ip.isNullOrBlank() || "unknown".equals(ip!!, ignoreCase = true)) {
        ip = getHeader("WL-Proxy-Client-IP")
    }
    if (ip.isNullOrBlank() || "unknown".equals(ip!!, ignoreCase = true)) {
        ip = remoteAddr
        if (ip == "127.0.0.1") {
            //根据网卡取本机配置的IP
            var inet: InetAddress? = null
            try {
                inet = InetAddress.getLocalHost()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            ip = inet!!.hostAddress
        }
    }
    // 多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
    if (ip != null && ip.length > 15) {
        if (ip.indexOf(",") > 0) {
            ip = ip.substring(0, ip.indexOf(","))
        }
    }
    return ip
}

/**
 * 快速生成json对象
 */
fun json(vararg pair: Pair<String, Any?>): ObjectNode {
    val on = ObjectMapper().createObjectNode()
    pair.forEach {
        val temp = it.second
        when (temp) {
            null -> on.putNull(it.first)
            is Short -> on.put(it.first, temp)
            is Int -> on.put(it.first, temp)
            is Long -> on.put(it.first, temp)
            is Float -> on.put(it.first, temp)
            is Double -> on.put(it.first, temp)
            is BigDecimal -> on.put(it.first, temp)
            is BigInteger -> on.put(it.first, temp)
            is String -> on.put(it.first, temp)
            is Boolean -> on.put(it.first, temp)
            is JsonNode -> on.set(it.first, temp)
            is Array<*> -> {
                val array = on.putArray(it.first)
                array.addArray(temp)
            }
            is Collection<*> -> {
                val array = on.putArray(it.first)
                array.addArray(temp)
            }
            is Map<*, *> -> {
                on.set(it.first, json(temp))
            }
            is Any -> on.putPOJO(it.first, temp)
        }
    }
    return on
}

/**
 * 快速生成json对象
 */
fun json(m: Map<*, *>): ObjectNode = json(*m.map { Pair(it.key.toString(), it.value) }.toTypedArray())

fun toJson(any:Any) = ObjectMapper().writeValueAsString(any)


/**
 * 快速生成json对象
 */
fun json(vararg any: Any?): ArrayNode {
//        println(any.joinToString { it.toString() })
    val an = ObjectMapper().createArrayNode()
    an.addArray(any)
    return an
}

/**
 * 快速生成json对象
 * 生成的json对象的内部元素都是平级的
 */
fun jsonPeers(vararg any: Any?): ArrayNode {
    val an = ObjectMapper().createArrayNode()
    an.addArray(any, true)
    return an
}

fun ArrayNode.addArray(any: Array<*>, isPeers: Boolean = false) {
    any.forEach { add(it, isPeers) }
}

fun ArrayNode.addArray(any: Iterable<*>, isPeers: Boolean = false) {
    any.forEach { add(it, isPeers) }
}

fun ArrayNode.add(it: Any?, isPeers: Boolean = false) {
//        println(it!!::class.simpleName)
//        TextNode
    when (it) {
        it == null -> addNull()
        is Int -> add(it)
        is Long -> add(it)
        is Float -> add(it)
        is Double -> add(it)
        is BigDecimal -> add(it)
        is BigInteger -> add(it)
        is String -> add(it)
        is Boolean -> add(it)
        is ValueNode -> add(it)
        is JsonNode -> if (isPeers) addArray(it, true) else add(it)
        is Array<*> -> if (isPeers) addArray(it, true) else {
            add(json(*it))
        }
        is Collection<*> -> if (isPeers) addArray(it, true) else {
            add(json(*(it.toTypedArray())))
        }
        is Any -> addPOJO(it)
    }
}

/**
 * 快速实现系列化及反序列化
 */
inline fun <reified T> fromJson(any: Any): T {
    return try {
        val om = ObjectMapper()
        val s = any as? String ?: om.writeValueAsString(any)
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false)
        om.readValue<T>(s, object : TypeReference<T>() {})
    }catch (e: JsonProcessingException){
        val om = jacksonObjectMapper()
        val s = any as? String ?: om.writeValueAsString(any)
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false)
        om.readValue<T>(s, object : TypeReference<T>() {})
    }


}


/**
 * 把data class 的类转成MarkDown的表格形式
 */
fun generateMarkDown(s:String){
    println("""
|参数|类型|默认值|描述|
|:-------|:-------|:-------|:-------|""")
    s.split("\n").map { it.trim() }.forEach {
        println(it.replaceFirst("val ", "| ")
                .replaceFirst(":", " | ")
                .replaceFirst("=", " | ")
                .replaceFirst("//", " | ")
                .replaceFirst(",", "") + " |")
    }
}

/**
 * 给img地址添加前缀
 */
fun String?.toImgUrl(imgPre:String):String?{
    return when {
        this.isNullOrBlank() -> null
        this!!.startsWith("http") -> this
        !imgPre.endsWith("/") && !this.startsWith("/") -> imgPre+"/"+this
        else -> imgPre+this
    }
}


