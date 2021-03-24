package dev.themeinerlp.skinserver.controller

import com.fasterxml.jackson.databind.ObjectMapper
import dev.themeinerlp.skinserver.config.SkinServerConfig
import dev.themeinerlp.skinserver.model.PlayerSkin
import dev.themeinerlp.skinserver.model.SkinProfile
import dev.themeinerlp.skinserver.repository.ProfileRepository
import dev.themeinerlp.skinserver.service.RenderService
import dev.themeinerlp.skinserver.service.SkinService
import dev.themeinerlp.skinserver.service.UUIDFetcher
import org.jetbrains.skija.*
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.io.InputStreamResource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.validation.constraints.NotBlank

@RestController
class SkinController(
    @Qualifier("skinServerConfig")
    val config: SkinServerConfig,
    val repository: ProfileRepository,
    val uuidFetcher: UUIDFetcher,
    val skinService: SkinService,
    val renderService: RenderService,
    val mapper: ObjectMapper
){

    @ResponseBody
    @RequestMapping(
        "skin/by/username/{size}/{username}",
        method = [RequestMethod.GET]
    )
    fun getByUsernameSkin(@NotBlank
                          @PathVariable(required = true) size: Int?,
                          @NotBlank
                          @PathVariable(required = true) username: String?): ResponseEntity<Any> {
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

        val url: String? = this.skinService.extractSkinUrl(skinProfile.texture)
        if (url == null) {
            this.repository.delete(skinProfile)
            return ResponseEntity.badRequest().body("URL is empty for database entry!")
        }
        if (!this.skinService.isCached(playerSkin, url)) {
            this.skinService.downloadSkin(url,playerSkin)
        }

        if (skinProfile.base64Texture == null) {
            this.skinService.saveTextureInDatabase(playerSkin, skinProfile)
        }
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(InputStreamResource(this.renderService.renderSkin(playerSkin).inputStream()))
    }

    @ResponseBody
    @RequestMapping(
        "skin/by/uuid/{size}/{uuid}",
        method = [RequestMethod.GET]
    )
    fun getByUUIDSkin(
        @NotBlank
        @PathVariable(required = true) size: Int?,
        @NotBlank
        @PathVariable(required = true) uuid: String?): ResponseEntity<Any> {
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
        val url: String? = this.skinService.extractSkinUrl(skinProfile.texture)
        if (url == null) {
            this.repository.delete(skinProfile)
            return ResponseEntity.badRequest().body("URL is empty for database entry!")
        }
        if (!this.skinService.isCached(playerSkin, url)) {
            this.skinService.downloadSkin(url,playerSkin)
        }

        if (skinProfile.base64Texture == null) {
            this.skinService.saveTextureInDatabase(playerSkin, skinProfile)
        }

        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(InputStreamResource(this.renderService.renderSkin(playerSkin).inputStream()))
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
}