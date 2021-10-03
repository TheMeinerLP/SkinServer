package dev.themeinerlp.skinserver.properties

import dev.themeinerlp.skinserver.utils.Constants
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "skinserver")
@ConstructorBinding
data class SkinServerProperties(
    val maxSize: Int = 512,
    val minSize: Int = 16,
    val connectionAddresses: List<String> = Constants.DEFAULT_ADDRESSES
)
