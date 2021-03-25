package dev.themeinerlp.skinserver.controller

import dev.themeinerlp.skinserver.config.SkinServerConfig
import dev.themeinerlp.skinserver.model.Skin
import dev.themeinerlp.skinserver.repository.SkinRepository
import dev.themeinerlp.skinserver.service.RenderService
import dev.themeinerlp.skinserver.service.SkinService
import dev.themeinerlp.skinserver.service.GameProfileService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException
import java.util.*
import javax.validation.constraints.NotBlank

@RestController
class SkinController(
    @Qualifier("skinServerConfig")
    val config: SkinServerConfig,
    val gameProfileService: GameProfileService,
    val skinService: SkinService,
    val renderService: RenderService,
    val skinRepository: SkinRepository
) {

    @ResponseBody
    @RequestMapping(
        "skin/by/username/{size}/{username}",
        method = [RequestMethod.GET]
    )
    fun getByUsernameSkin(
        @NotBlank
        @PathVariable(required = true) size: Int,
        @NotBlank
        @PathVariable(required = true) username: String
    ): ResponseEntity<Any> {
        if (size < this.config.minSize!! || size > this.config.maxSize!!) {
            throw ResponseStatusException(
                HttpStatus.METHOD_NOT_ALLOWED,
                "\"${size}\" is no valide size! Use ${config.minSize} - ${config.maxSize}"
            )
        }
        var skin = this.skinRepository.findByUsername(username)
        if (skin == null) {
            skin = Skin()
            val player = this.gameProfileService.findGameProfile(username) ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Username cannot be found!"
            )
            val user = this.gameProfileService.getGameProfile(player.uuid!!) ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "User cannot be found!"
            )
            val skinUrl = this.skinService.extractSkinUrl(this.gameProfileService.getTextureFromJson(user) ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Skin URL are empty"
            ))?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Skin URL are empty"
            )
            skin.username = this.gameProfileService.getNameFromJson(user)
            skin.uuid = player.uuid!!
            skin.skinUrl = skinUrl
            skin.texture = String(Base64.getEncoder().encode(this.gameProfileService.downloadUrlToByteArray(skinUrl)))
            this.skinRepository.save(skin)
        }
        val value = Base64.getDecoder().decode(skin.texture)
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(
            InputStreamResource(
                this.renderService.renderSkinFromByteArray(size, value).inputStream()
            )
        )
    }

    @ResponseBody
    @RequestMapping(
        "skin/by/uuid/{size}/{uuid:[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}}",
        method = [RequestMethod.GET]
    )
    fun getByUUIDSkin(
        @NotBlank
        @PathVariable(required = true) size: Int,
        @NotBlank
        @PathVariable(required = true) uuid: UUID
    ): ResponseEntity<Any> {
        if (size < this.config.minSize!! || size > this.config.maxSize!!) {
            throw ResponseStatusException(
                HttpStatus.METHOD_NOT_ALLOWED,
                "\"${size}\" is no valide size! Use ${config.minSize} - ${config.maxSize}"
            )
        }
        var skin = this.skinRepository.findByUuid(uuid)
        if (skin == null) {
            skin = Skin()
            val user = this.gameProfileService.getGameProfile(uuid) ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "User cannot be found!"
            )
            val skinUrl = this.skinService.extractSkinUrl(this.gameProfileService.getTextureFromJson(user) ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Skin URL are empty"
            ))?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Skin URL are empty"
            )
            skin.username = this.gameProfileService.getNameFromJson(user)
            skin.uuid = uuid
            skin.skinUrl = skinUrl
            skin.texture = String(Base64.getEncoder().encode(this.gameProfileService.downloadUrlToByteArray(skinUrl)))
            this.skinRepository.save(skin)
        }
        val value = Base64.getDecoder().decode(skin.texture)
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(
            InputStreamResource(
                this.renderService.renderSkinFromByteArray(size, value).inputStream()
            )
        )
    }
}