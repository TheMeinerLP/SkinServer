package dev.themeinerlp.skinserver

import dev.themeinerlp.skinserver.config.SkinServerConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.web.servlet.config.annotation.EnableWebMvc

@SpringBootApplication
@EnableConfigurationProperties(value = [SkinServerConfig::class])
@EnableWebMvc
class SkinServerApplication

fun main(args: Array<String>) {
    runApplication<SkinServerApplication>(*args)
}
