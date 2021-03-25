package dev.themeinerlp.skinserver.service

import com.fasterxml.jackson.databind.ObjectMapper
import dev.themeinerlp.skinserver.config.SkinServerConfig
import dev.themeinerlp.skinserver.model.GameProfileHolder
import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket4j
import io.github.bucket4j.Refill
import io.github.bucket4j.local.LocalBucket
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.net.InetAddress
import java.time.Duration
import java.util.UUID;
import kotlin.collections.HashMap

@Service
class GameProfileService(
    @Qualifier("skinServerConfig")
    val config: SkinServerConfig,
    val mapper: ObjectMapper
) {

    val httpClient: CloseableHttpClient = HttpClients.createDefault()
    val uuidRegex: Regex = Regex("([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]+)")
    val ipLeft: MutableMap<String, LocalBucket> = HashMap()
    private final val limit: Bandwidth

    init {
        val refill = Refill.intervally(600, Duration.ofMinutes(10))
        limit = Bandwidth.classic(600, refill)
        val bucket = Bucket4j.builder().addLimit(limit).build()
        this.config.connectionAddresses!!.forEach {
            this.ipLeft[it] = bucket
        }
    }


    fun findGameProfile(username: String): GameProfileHolder? {
        val getRequest = HttpGet("https://api.mojang.com/users/profiles/minecraft/${username}")
        getRequest.config = RequestConfig
            .custom()
            .setRedirectsEnabled(false)
            .setLocalAddress(InetAddress.getByName(getLocalAddress()))
            .setConnectTimeout(3000)
            .build()
        getRequest.setHeader("User-Agent", "Minecraft-SkinServer")
        httpClient.execute(getRequest).use {
            if (it.statusLine.statusCode != 200) {
                throw ResponseStatusException(HttpStatus.BANDWIDTH_LIMIT_EXCEEDED, "Mojang has probably blocked you :(")
            }
            val node = mapper.readTree(it.entity.content)
            val profile = GameProfileHolder()
            val newUUID = node.get("id").asText().replaceFirst(uuidRegex, "$1-$2-$3-$4-$5")
            profile.uuid = UUID.fromString(newUUID)
            profile.name = node.get("name").asText()
            return profile
        }
    }

    fun getGameProfile(uuid: UUID): String? {
        val getRequest = HttpGet("https://sessionserver.mojang.com/session/minecraft/profile/$uuid")
        getRequest.config = RequestConfig
            .custom()
            .setRedirectsEnabled(false)
            .setLocalAddress(InetAddress.getByName(getLocalAddress()))
            .setConnectTimeout(3000)
            .build()
        getRequest.setHeader("User-Agent", "Minecraft-SkinServer")
        httpClient.execute(getRequest).use { it ->
            if (it.statusLine.statusCode != 200) {
                throw ResponseStatusException(HttpStatus.BANDWIDTH_LIMIT_EXCEEDED, "Mojang has probably blocked you :(")
            }
            it.entity.content.use { iss ->
                val content = iss.readAllBytes()
                return String(content)
            }
        }
    }

    fun getTextureFromJson(text: String): String? {
        val node = mapper.readTree(text)
        if (node.has("properties")) {
            return node.get("properties").find {
                if (it.has("name") && it.get("name").asText().equals("textures", ignoreCase = true)) {
                    return it.get("value").asText()
                } else {
                    throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Player have no skin value!!!")
                }
            }!!.asText()
        } else {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Player have no properties value!!!")
        }
    }
    fun getNameFromJson(text: String): String {
        val node = mapper.readTree(text)
        if (node.has("name")) {
            return node.get("name").asText()
        } else {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Player have no name value!!!")
        }
    }


    fun getLocalAddress(): String {
        val pair = this.ipLeft.toList().sortedBy { (_, value) -> value.availableTokens }.first()
        println("${pair.second.availableTokens} Tokens left for 10 Min for: ${pair.first}")
        return if (pair.second.tryConsume(1)) {
            pair.first
        } else {
            throw ResponseStatusException(HttpStatus.BANDWIDTH_LIMIT_EXCEEDED, "No Tokens left for IP ${pair.first}")
        }

    }

    fun downloadUrlToByteArray(url: String): ByteArray {
        val getRequest = HttpGet(url)
        getRequest.config = RequestConfig
            .custom()
            .setRedirectsEnabled(false)
            .setLocalAddress(InetAddress.getByName(getLocalAddress()))
            .setConnectTimeout(3000)
            .build()
        getRequest.setHeader("User-Agent", "Minecraft-SkinServer")
        httpClient.execute(getRequest).use {
            if (it.statusLine.statusCode != 200) {
                throw IllegalStateException("Mojang has probably blocked you :(")
            }
            return it.entity.content.readAllBytes()
        }
    }

}