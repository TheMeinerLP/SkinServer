package dev.themeinerlp.skinserver.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "skinserver")
class SkinServerConfig {


    var maxSize: Int? = 512
    var minSize: Int? = 16
    var connectionAddresses: List<String>? = ArrayList()


}