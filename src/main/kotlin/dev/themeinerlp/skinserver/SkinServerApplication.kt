package dev.themeinerlp.skinserver

import dev.themeinerlp.skinserver.properties.SkinServerProperties
import dev.themeinerlp.skinserver.spec.SkinServerAPISpec
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.mongodb.MongoDatabaseFactory
import org.springframework.data.mongodb.MongoTransactionManager


@OpenAPIDefinition(
    info = Info(
        title = "Skin Server",
        description = "A simple server to pre-cache some minecraft skins and scale up and down or extract the head",
        version = "1.0.4"
    )
)

@SpringBootApplication(
    scanBasePackageClasses = [
        SkinServerApplication::class,
        SkinServerAPISpec::class
    ]
)
@ConfigurationPropertiesScan
@EnableConfigurationProperties(value = [SkinServerProperties::class])
class SkinServerApplication {

    @Bean
    fun transactionManager(dbFactory: MongoDatabaseFactory): MongoTransactionManager {
        return MongoTransactionManager(dbFactory)
    }
}

fun main(args: Array<String>) {
    runApplication<SkinServerApplication>(*args)
}
