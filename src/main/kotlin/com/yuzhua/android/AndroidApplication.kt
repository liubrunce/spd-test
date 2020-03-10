package com.yuzhua.android

import org.apache.catalina.connector.Connector
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory
import org.springframework.boot.web.servlet.server.ServletWebServerFactory
import org.springframework.context.annotation.Bean

@SpringBootApplication
class AndroidApplication{
    @Bean
    fun servletContainer(): ServletWebServerFactory {
        val tomcatServletWebServerFactory = TomcatServletWebServerFactory()
        val connector = Connector("org.apache.coyote.http11.Http11NioProtocol")
        connector.port = 9307
        tomcatServletWebServerFactory.addAdditionalTomcatConnectors(connector)
        return tomcatServletWebServerFactory
    }
}

fun main(args: Array<String>) {
    runApplication<AndroidApplication>(*args)
}
