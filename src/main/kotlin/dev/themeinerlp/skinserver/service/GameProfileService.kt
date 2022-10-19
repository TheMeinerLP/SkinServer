package dev.themeinerlp.skinserver.service

import com.fasterxml.jackson.databind.ObjectMapper
import dev.themeinerlp.skinserver.properties.SkinServerProperties
import dev.themeinerlp.skinserver.spec.dao.gameprofileholder.GameProfileHolder
import dev.themeinerlp.skinserver.utils.Constants
import dev.themeinerlp.skinserver.utils.USER_AGENT
import dev.themeinerlp.skinserver.utils.USER_AGENT_NAME
import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import io.github.bucket4j.Refill
import io.github.bucket4j.local.LocalBucket
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.net.InetAddress
import java.time.Duration
import java.util.UUID
import kotlin.collections.HashMap

@Service
class GameProfileService(
    skinServerProperties: SkinServerProperties,
    val mapper: ObjectMapper
) {

    val httpClient: CloseableHttpClient = HttpClients.createDefault()
    val ipLeft: MutableMap<String, LocalBucket> = HashMap()
    private val limit: Bandwidth

    init {
        val refill = Refill.intervally(600, Duration.ofMinutes(10))
        limit = Bandwidth.classic(600, refill)
        val bucket = Bucket.builder().addLimit(limit).build()
        skinServerProperties.connectionAddresses.forEach {
            ipLeft[it] = bucket
        }
    }


    fun findGameProfile(username: String): GameProfileHolder {
        val getRequest = HttpGet("${Constants.MOJANG_NAME_TO_UUID_URL}$username")
        getRequest.config = RequestConfig
            .custom()
            .setRedirectsEnabled(false)
            .setLocalAddress(InetAddress.getByName(getLocalAddress()))
            .setConnectTimeout(3000)
            .build()
        getRequest.setHeader(USER_AGENT_NAME, USER_AGENT)
        httpClient.execute(getRequest).use {
            if (it.statusLine.statusCode != 200) throw ResponseStatusException(
                HttpStatus.BANDWIDTH_LIMIT_EXCEEDED,
                "Mojang has probably blocked you :("
            )
            val node = mapper.readTree(it.entity.content)
            val newUUID = node.get("id").asText().replaceFirst(Constants.UUID_REGEX, "$1-$2-$3-$4-$5")
            return GameProfileHolder(UUID.fromString(newUUID), node.get("name").asText())
        }
    }

    fun getGameProfile(uuid: UUID): String {
        val getRequest = HttpGet("${Constants.MOJANG_PROFILE_URL}$uuid")
        getRequest.config = RequestConfig
            .custom()
            .setRedirectsEnabled(false)
            .setLocalAddress(InetAddress.getByName(getLocalAddress()))
            .setConnectTimeout(3000)
            .build()
        getRequest.setHeader(USER_AGENT_NAME, USER_AGENT)
        httpClient.execute(getRequest).use {
            if (it.statusLine.statusCode != 200) throw ResponseStatusException(HttpStatus.BANDWIDTH_LIMIT_EXCEEDED, "Mojang has probably blocked you :(")
            it.entity.content.use { iss ->
                return String(iss.readBytes())
            }
        }
    }

    fun getTextureFromJson(text: String): String {
        val node = mapper.readTree(text)
        if (node.has("properties")) {
            val properties = node.get("properties").map {
                if (it.has("name") && it.get("name").asText().equals("textures", ignoreCase = true)) {
                    return@map it.get("value").asText()
                } else throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Player have no name value!!!")
            }
            return properties.first() ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Player have no properties value!!!")
        } else throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Player have no properties value!!!")
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
        val pair = this.ipLeft.toList().minBy { (_, value) -> value.availableTokens }
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
        getRequest.setHeader(USER_AGENT_NAME, USER_AGENT)
        httpClient.execute(getRequest).use {
            if (it.statusLine.statusCode != 200) {
                throw IllegalStateException("Mojang has probably blocked you :(")
            }
            return it.entity.content.readBytes()
        }
    }

}