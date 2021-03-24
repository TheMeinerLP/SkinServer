package dev.themeinerlp.skinserver.service

import com.fasterxml.jackson.databind.ObjectMapper
import dev.themeinerlp.skinserver.config.SkinServerConfig
import dev.themeinerlp.skinserver.model.PlayerSkin
import dev.themeinerlp.skinserver.model.SkinProfile
import dev.themeinerlp.skinserver.repository.ProfileRepository
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.util.*

@Service
class SkinService(
    @Qualifier("skinServerConfig")
    val config: SkinServerConfig,
    val repository: ProfileRepository,
    val uuidFetcher: UUIDFetcher,
    val mapper: ObjectMapper,
    val downloadService: DownloadService
) {
    /**
     * Put the downloaded skin into db
     */
    fun saveTextureInDatabase(playerSkin: PlayerSkin, skinProfile: SkinProfile) {
        skinProfile.base64Texture = String(Base64.getEncoder().encode(Files.readAllBytes(playerSkin.skinFile)))
        this.repository.save(skinProfile)
    }

    /**
     * Downloading the skin from url
     */
    fun downloadSkin(url: String, playerSkin: PlayerSkin) {
        if (!Files.exists(playerSkin.folderPath)) {
            Files.createDirectories(playerSkin.folderPath)
        }
        this.downloadService.downloadUrlToFile(url, playerSkin)
        Files.writeString(playerSkin.textureFile, url)
    }

    /**
     * Checks if a local file present before downloading again from backend
     */
    fun isCached(playerSkin: PlayerSkin, url: String): Boolean {
        return Files.exists(playerSkin.textureFile) && Files.readString(playerSkin.textureFile) === url
    }

    /**
     * Extract url from json string
     */
    fun extractSkinUrl(texture: String?): String? {
        if (texture == null) {
            return null
        }
        val jsonObject = String(Base64.getDecoder().decode(texture))
        val node = this.mapper.readTree(jsonObject)

        return if (node.has("textures")) {
            node.get("textures").get("SKIN").get("url").asText()
        } else {
            null
        }
    }
}