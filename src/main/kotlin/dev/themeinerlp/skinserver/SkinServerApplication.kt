package dev.themeinerlp.skinserver

import dev.themeinerlp.skinserver.properties.SkinServerProperties
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.web.servlet.config.annotation.EnableWebMvc


@OpenAPIDefinition(
    info = Info(
        title = "Skin Server",
        description = "A simple server to pre-cache some minecraft skins and scale up and down or extract the head",
        version = "1.0.1-SNAPSHOT"
    )
)

@SpringBootApplication
@EnableConfigurationProperties(value = [SkinServerProperties::class])
@EnableWebMvc
class SkinServerApplication
fun main(args: Array<String>) {
    runApplication<SkinServerApplication>(*args)
}
