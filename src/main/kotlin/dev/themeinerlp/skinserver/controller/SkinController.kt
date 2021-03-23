package dev.themeinerlp.skinserver.controller

import com.fasterxml.jackson.databind.ObjectMapper
import dev.themeinerlp.skinserver.config.SkinServerConfig
import dev.themeinerlp.skinserver.model.PlayerSkin
import dev.themeinerlp.skinserver.model.SkinProfile
import dev.themeinerlp.skinserver.repository.ProfileRepository
import dev.themeinerlp.skinserver.service.UUIDFetcher
import org.jetbrains.skija.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.io.InputStreamResource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.nio.file.Files
import java.util.*

@RestController
class SkinController(
    @Qualifier("skinServerConfig")
    val config: SkinServerConfig,
    val repository: ProfileRepository,
    val uuidFetcher: UUIDFetcher,
    val mapper: ObjectMapper
){

    @ResponseBody
    @RequestMapping(
        "skin/{size}/{username}",
        method = [RequestMethod.GET]
    )
    fun getSkin(@PathVariable size: Int?, @PathVariable username: String?): ResponseEntity<Any> {
        if (size == null) {
            return ResponseEntity.badRequest().body("\"${size}\" is no valide size! Use ${config.minSize} - ${config.maxSize}")
        }
        if (size < this.config.minSize!! || size > this.config.maxSize!!) {
            return ResponseEntity.badRequest().body("\"${size}\" is no valide size! Use ${config.minSize} - ${config.maxSize}")
        }
        if (username == null) {
            return ResponseEntity.badRequest().body("\"${username}\" is required!")
        }
        val playerSkin = PlayerSkin(username, size)
        var skinProfile: SkinProfile? = this.repository.findProfileByUsername(username)
        if (skinProfile == null) {
            skinProfile = uuidFetcher.findPlayer(username)
            this.repository.insert(skinProfile)
        }
        val url: String = getSkinUrl(skinProfile.texture) ?: return ResponseEntity.badRequest().body("URL is empty for database entry!")
        if (!isCached(playerSkin, url)) {
            downloadSkin(url,playerSkin)
        }

        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(InputStreamResource(renderSkin(playerSkin).inputStream()))
    }


    @ResponseBody
    @RequestMapping(
        "head/{size}/{username}",
        method = [RequestMethod.GET]
    )
    fun getHead(@PathVariable size: Int?, @PathVariable username: String?): ResponseEntity<Any> {
        if (size == null) {
            return ResponseEntity.badRequest().body("\"${size}\" is no valide size! Use ${config.minSize} - ${config.maxSize}")
        }
        if (size < this.config.minSize!! || size > this.config.maxSize!!) {
            return ResponseEntity.badRequest().body("\"${size}\" is no valide size! Use ${config.minSize} - ${config.maxSize}")
        }
        if (username == null) {
            return ResponseEntity.badRequest().body("\"${username}\" is required!")
        }
        val playerSkin = PlayerSkin(username, size)
        var skinProfile: SkinProfile? = this.repository.findProfileByUsername(username)
        if (skinProfile == null) {
            skinProfile = uuidFetcher.findPlayer(username)
            this.repository.insert(skinProfile)
        }
        val url: String = getSkinUrl(skinProfile.texture) ?: return ResponseEntity.badRequest().body("URL is empty for database entry!")
        if (!isCached(playerSkin, url)) {
            downloadSkin(url,playerSkin)
        }

        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(InputStreamResource(renderHead(playerSkin).inputStream()))
    }

    fun getSkinUrl(texture: String?): String? {
        if (texture == null) {
            return null
        }
        val jsonObject = String(Base64.getDecoder().decode(texture))
        val node = this.mapper.readTree(jsonObject)
        return node.get("textures").get("SKIN").get("url").asText()
    }

    fun isCached(playerSkin: PlayerSkin, url: String): Boolean {
        return Files.exists(playerSkin.textureFile) && Files.readString(playerSkin.textureFile) === url
    }

    fun renderHead(playerSkin: PlayerSkin): ByteArray {
        val skinData = Data.makeFromFileName(playerSkin.skinFile.toString())
        val skinImage = Image.makeFromEncoded(skinData.bytes)
        val surface = Surface.makeRasterN32Premul(playerSkin.size, playerSkin.size)
        val canvas = surface.canvas
        val small = skinImage.height == 32
        if (small) {
            canvas.drawImageRect(
                skinImage, Rect.makeWH(skinImage.width.toFloat(), skinImage.height.toFloat())
                    .offset(8F,8F),
                Rect.makeWH(playerSkin.size.toFloat(), playerSkin.size.toFloat() / 2).scale(8F, 8F )
            )
            canvas.drawImageRect(
                skinImage, Rect.makeWH(skinImage.width.toFloat(), skinImage.height.toFloat())
                    .offset(40F,8F),
                Rect.makeWH(playerSkin.size.toFloat(), playerSkin.size.toFloat() / 2 ).scale(8F, 8F )
            )
        } else {
            canvas.drawImageRect(
                skinImage, Rect.makeWH(skinImage.width.toFloat(), skinImage.height.toFloat())
                    .offset(8F,8F),
                Rect.makeWH(playerSkin.size.toFloat(), playerSkin.size.toFloat()).scale(8F, 8F )
            )
            canvas.drawImageRect(
                skinImage, Rect.makeWH(skinImage.width.toFloat(), skinImage.height.toFloat())
                    .offset(40F,8F),
                Rect.makeWH(playerSkin.size.toFloat(), playerSkin.size.toFloat()).scale(8F, 8F )
            )
        }

        skinData.close()
        return surface.makeImageSnapshot().encodeToData()!!.bytes
    }

    fun renderSkin(playerSkin: PlayerSkin): ByteArray {
        val skinData = Data.makeFromFileName(playerSkin.skinFile.toString())
        val skinImage = Image.makeFromEncoded(skinData.bytes)
        val surface = Surface.makeRasterN32Premul(playerSkin.size, playerSkin.size)
        val canvas = surface.canvas
        val small = skinImage.height == 32
        if (small) {
            canvas.drawImageRect(
                skinImage, Rect.makeWH(skinImage.width.toFloat(), skinImage.height.toFloat()),
                Rect.makeWH(playerSkin.size.toFloat(), playerSkin.size.toFloat() / 2)
            )
        } else {
            canvas.drawImageRect(
                skinImage, Rect.makeWH(skinImage.width.toFloat(), skinImage.height.toFloat()),
                Rect.makeWH(playerSkin.size.toFloat(), playerSkin.size.toFloat())
            )
        }

        skinData.close()
        return surface.makeImageSnapshot().encodeToData()!!.bytes
    }

    fun downloadSkin(url: String, playerSkin: PlayerSkin) {
        if (!Files.exists(playerSkin.folderPath)) {
            Files.createDirectories(playerSkin.folderPath)
        }
        this.uuidFetcher.downloadUrlToFile(url, playerSkin)
        Files.writeString(playerSkin.textureFile, url)
    }


}