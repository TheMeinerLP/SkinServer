package dev.themeinerlp.skinserver.service

import com.fasterxml.jackson.databind.ObjectMapper
import dev.themeinerlp.skinserver.config.SkinServerConfig
import dev.themeinerlp.skinserver.model.PlayerSkin
import dev.themeinerlp.skinserver.model.SkinProfile
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
import java.util.*

@Service
class UUIDFetcher(
    @Qualifier("skinServerConfig")
    val config: SkinServerConfig,
    val mapper: ObjectMapper) {

    val httpClient: CloseableHttpClient = HttpClients.createDefault()
    val uuidRegex: Regex = Regex("([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]+)")

    fun findPlayer(username: String): SkinProfile {

        val getRequest = HttpGet("https://api.mojang.com/users/profiles/minecraft/${username}")
        getRequest.config = RequestConfig
            .custom()
            .setRedirectsEnabled(false)
            .setLocalAddress(InetAddress.getByName(getLocalAddress()))
            .setConnectTimeout(3000)
            .build()
        getRequest.setHeader("User-Agent","Minecraft-SkinServer")
        val execute = httpClient.execute(getRequest)
        if (execute.statusLine.statusCode != 200) {
            throw IllegalStateException("Mojang has probably blocked you :(")
        }
        val node = mapper.readTree(execute.entity.content)
        val profile = SkinProfile()
        val newUUID = node.get("id").asText().replaceFirst(uuidRegex, "$1-$2-$3-$4-$5" )
        val uuid = UUID.fromString(newUUID)
        profile.uuid = uuid.toString()
        profile.username = username
        profile.texture = getTexture(profile)
        execute.close()
        return profile
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
        val execute = httpClient.execute(getRequest)
        if (execute.statusLine.statusCode != 200) {
            throw IllegalStateException("Mojang has probably blocked you :(")
        }
        return mapper.readTree(execute.entity.content).asText()
    }

    private fun getTexture(profile: SkinProfile): String? {
        val node = mapper.readTree(getUser(profile.uuid!!))

        if (node.has("properties") && node.get("properties")[0]["name"].asText().equals("textures")) {
            return node.get("properties")[0]["value"].asText()
        }
        return null
    }



    fun getLocalAddress(): String {
        return this.config.connectionAddresses!!.random()
    }

}