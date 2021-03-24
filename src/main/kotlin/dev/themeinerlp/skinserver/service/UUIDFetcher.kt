package dev.themeinerlp.skinserver.service

import com.fasterxml.jackson.databind.ObjectMapper
import dev.themeinerlp.skinserver.config.SkinServerConfig
import dev.themeinerlp.skinserver.model.PlayerSkin
import dev.themeinerlp.skinserver.model.SkinProfile
import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket4j
import io.github.bucket4j.Refill
import io.github.bucket4j.local.LocalBucket
import org.apache.http.client.ResponseHandler
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.config.RequestConfig.Builder
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.impl.client.HttpClients
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Configurable
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import java.lang.IllegalStateException
import java.net.*
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.nio.file.Files
import java.time.Duration
import java.util.*
import kotlin.collections.HashMap

@Service
class UUIDFetcher(
    @Qualifier("skinServerConfig")
    val config: SkinServerConfig,
    val mapper: ObjectMapper) {

    val httpClient: CloseableHttpClient = HttpClients.createDefault()
    val uuidRegex: Regex = Regex("([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]+)")
    val ipLeft: MutableMap<String, LocalBucket> = HashMap()
    val limit: Bandwidth
    init {
        val refill = Refill.intervally(600, Duration.ofMinutes(10))
        limit = Bandwidth.classic(600, refill)
        val bucket = Bucket4j.builder().addLimit(limit).build()
        this.config.connectionAddresses!!.forEach {
            this.ipLeft[it] = bucket
        }
    }



    fun findPlayer(username: String): SkinProfile {
        val getRequest = HttpGet("https://api.mojang.com/users/profiles/minecraft/${username}")
        getRequest.config = RequestConfig
            .custom()
            .setRedirectsEnabled(false)
            .setLocalAddress(InetAddress.getByName(getLocalAddress()))
            .setConnectTimeout(3000)
            .build()
        getRequest.setHeader("User-Agent","Minecraft-SkinServer")
        httpClient.execute(getRequest).use {
            if (it.statusLine.statusCode != 200) {
                throw IllegalStateException("Mojang has probably blocked you :(")
            }
            val node = mapper.readTree(it.entity.content)
            val profile = SkinProfile()
            val newUUID = node.get("id").asText().replaceFirst(uuidRegex, "$1-$2-$3-$4-$5" )
            val uuid = UUID.fromString(newUUID)
            profile.uuid = uuid.toString()
            profile.username = username
            profile.texture = getTexture(profile)
            return profile
        }
    }

    fun getUser(uuid: String): String? {
        val getRequest = HttpGet("https://sessionserver.mojang.com/session/minecraft/profile/${uuid}")
        getRequest.config = RequestConfig
            .custom()
            .setRedirectsEnabled(false)
            .setLocalAddress(InetAddress.getByName(getLocalAddress()))
            .setConnectTimeout(3000)
            .build()
        getRequest.setHeader("User-Agent","Minecraft-SkinServer")
        httpClient.execute(getRequest).use { it ->
            if (it.statusLine.statusCode != 200) {
                throw IllegalStateException("Mojang has probably blocked you :(")
            }
            it.entity.content.use { iss ->
                val content = iss.readAllBytes()
                return String(content)
            }
        }
    }

    private fun getTexture(profile: SkinProfile): String? {
        val text = getUser(profile.uuid!!)
        val node = mapper.readTree(text)
        if (node.has("properties")) {
            return node.get("properties").find {
                if (it.has("name") && it.get("name").asText().equals("textures", ignoreCase = true)) {
                    return it.get("value").asText()
                } else {
                    false
                }
            }!!.asText()
        } else {
            return null
        }
    }



    fun getLocalAddress(): String {
        val pair = this.ipLeft.toList().sortedBy { (_, value) -> value.availableTokens }.first()
        println("${pair.second.availableTokens} Tokens left for 10 Min for: ${pair.first}")
        return if (pair.second.tryConsume(1)) {
            pair.first
        } else {
            throw IllegalStateException("No Tokens left for IP ${pair.first}")
        }

    }

}