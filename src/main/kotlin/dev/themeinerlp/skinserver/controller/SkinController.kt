package dev.themeinerlp.skinserver.controller

import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import dev.themeinerlp.skinserver.config.SkinServerConfig
import dev.themeinerlp.skinserver.model.PlayerSkin
import dev.themeinerlp.skinserver.model.SkinProfile
import dev.themeinerlp.skinserver.repository.ProfileRepository
import dev.themeinerlp.skinserver.service.UUIDFetcher
import org.jetbrains.skija.*
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
        "skin/by/username/{size}/{username}",
        method = [RequestMethod.GET]
    )
    fun getByUsernameSkin(@PathVariable size: Int?, @PathVariable username: String?): ResponseEntity<Any> {
        val response = checkDefaultParameters(size,username)
        if (response != null) {
            return response
        }
        val playerSkin = PlayerSkin(username!!, size!!)
        var skinProfile: SkinProfile? = this.repository.findProfileByUsername(username)
        if (skinProfile == null) {
            skinProfile = uuidFetcher.findPlayer(username)
            this.repository.insert(skinProfile)
        }
        val url: String = getSkinUrl(skinProfile.texture) ?: return ResponseEntity.badRequest().body("URL is empty for database entry!")
        if (!isCached(playerSkin, url)) {
            downloadSkin(url,playerSkin)
        }

        if (skinProfile.base64Texture == null) {
            saveTextureInDatabase(playerSkin, skinProfile)
        }
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(InputStreamResource(renderSkin(playerSkin).inputStream()))
    }

    @ResponseBody
    @RequestMapping(
        "skin/by/uuid/{size}/{uuid}",
        method = [RequestMethod.GET]
    )
    fun getByUUIDSkin(@PathVariable size: Int?, @PathVariable uuid: String?): ResponseEntity<Any> {
        if (uuid == null) {
            return ResponseEntity.badRequest().body("\"${uuid}\" is required!")
        }
        var skinProfile: SkinProfile? = this.repository.findProfileByUuid(uuid)
        val username = if (skinProfile?.username != null) {
            skinProfile.username
        } else {
            this.mapper.readTree(this.uuidFetcher.getUser(uuid))["name"].asText()
        }
        val response = checkDefaultParameters(size,username)
        if (response != null) {
            return response
        }
        val playerSkin = PlayerSkin(username!!, size!!)
        if (skinProfile == null) {
            skinProfile = uuidFetcher.findPlayer(username)
            this.repository.insert(skinProfile)
        }
        val url: String = getSkinUrl(skinProfile.texture) ?: return ResponseEntity.badRequest().body("URL is empty for database entry!")
        if (!isCached(playerSkin, url)) {
            downloadSkin(url,playerSkin)
        }

        if (skinProfile.base64Texture == null) {
            saveTextureInDatabase(playerSkin, skinProfile)
        }

        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(InputStreamResource(renderSkin(playerSkin).inputStream()))
    }


    @ResponseBody
    @RequestMapping(
        "head/by/uuid/{size}/{uuid}",
        method = [RequestMethod.GET]
    )
    fun getByUUIDHead(@PathVariable size: Int?, @PathVariable uuid: String?): ResponseEntity<Any> {
        if (uuid == null) {
            return ResponseEntity.badRequest().body("\"${uuid}\" is required!")
        }
        var skinProfile: SkinProfile? = this.repository.findProfileByUuid(uuid)
        val username = if (skinProfile?.username != null) {
            skinProfile.username
        } else {
            this.mapper.readTree(this.uuidFetcher.getUser(uuid))["name"].asText()
        }
        val response = checkDefaultParameters(size,username)
        if (response != null) {
            return response
        }
        val playerSkin = PlayerSkin(username!!, size!!)
        if (skinProfile == null) {
            skinProfile = uuidFetcher.findPlayer(username)
            this.repository.insert(skinProfile)
        }
        val url: String = getSkinUrl(skinProfile.texture) ?: return ResponseEntity.badRequest().body("URL is empty for database entry!")
        if (!isCached(playerSkin, url)) {
            downloadSkin(url,playerSkin)
        }

        if (skinProfile.base64Texture == null) {
            saveTextureInDatabase(playerSkin, skinProfile)
        }

        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(InputStreamResource(renderHead(playerSkin).inputStream()))
    }


    @ResponseBody
    @RequestMapping(
        "head/by/username/{size}/{username}",
        method = [RequestMethod.GET]
    )
    fun getByUsernameHead(@PathVariable size: Int?, @PathVariable username: String?): ResponseEntity<Any> {
        val response = checkDefaultParameters(size,username)
        if (response != null) {
            return response
        }
        val playerSkin = PlayerSkin(username!!, size!!)
        var skinProfile: SkinProfile? = this.repository.findProfileByUsername(username)
        if (skinProfile == null) {
            skinProfile = uuidFetcher.findPlayer(username)
            this.repository.insert(skinProfile)
        }
        val url: String = getSkinUrl(skinProfile.texture) ?: return ResponseEntity.badRequest().body("URL is empty for database entry!")
        if (!isCached(playerSkin, url)) {
            downloadSkin(url,playerSkin)
        }
        if (skinProfile.base64Texture == null) {
            saveTextureInDatabase(playerSkin, skinProfile)
        }
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(InputStreamResource(renderHead(playerSkin).inputStream()))
    }

    fun checkDefaultParameters(size: Int?, value: String?): ResponseEntity<Any>? {
        if (size == null) {
            val node = this.mapper.createObjectNode()
            node.put("error", "\"${size}\" is no valide size! Use ${config.minSize} - ${config.maxSize}")
            return ResponseEntity.badRequest().body(node.asText())
        }
        if (size < this.config.minSize!! || size > this.config.maxSize!!) {
            val node = this.mapper.createObjectNode()
            node.put("error","\"${size}\" is no valide size! Use ${config.minSize} - ${config.maxSize}")
            return ResponseEntity.badRequest().body(node.asText())
        }
        if (value == null) {
            val node = this.mapper.createObjectNode()
            node.put("error", "\"${value}\" is required!")
            return ResponseEntity.badRequest().body(node.asText())
        }
        return null
    }

    fun saveTextureInDatabase(playerSkin: PlayerSkin, skinProfile: SkinProfile) {
        skinProfile.base64Texture = String(Base64.getEncoder().encode(Files.readAllBytes(playerSkin.skinFile)))
        this.repository.save(skinProfile)
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
        val surface = Surface.makeRasterN32Premul(playerSkin.size, playerSkin.size)
        Data.makeFromFileName(playerSkin.skinFile.toString()).use {
            val skinImage = Image.makeFromEncoded(it.bytes)

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
        }
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