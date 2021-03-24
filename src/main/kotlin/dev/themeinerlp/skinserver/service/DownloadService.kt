package dev.themeinerlp.skinserver.service

import com.fasterxml.jackson.databind.ObjectMapper
import dev.themeinerlp.skinserver.config.SkinServerConfig
import dev.themeinerlp.skinserver.model.PlayerSkin
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import java.lang.IllegalStateException
import java.net.InetAddress
import java.nio.file.Files

@Service
class DownloadService(
    @Qualifier("skinServerConfig")
    val config: SkinServerConfig,
    val mapper: ObjectMapper
) {

    val httpClient: CloseableHttpClient = HttpClients.createDefault()


    fun downloadUrlToFile(url: String, playerSkin: PlayerSkin) {
        val getRequest = HttpGet(url)
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
        val newOutputStream = Files.newOutputStream(playerSkin.skinFile)
        newOutputStream.write(execute.entity.content.readAllBytes())
        newOutputStream.close()
        execute.close()
    }

    fun getLocalAddress(): String {
        return this.config.connectionAddresses!!.random()
    }


}