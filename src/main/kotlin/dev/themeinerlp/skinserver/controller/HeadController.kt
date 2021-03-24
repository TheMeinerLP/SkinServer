package dev.themeinerlp.skinserver.controller

import com.fasterxml.jackson.databind.ObjectMapper
import dev.themeinerlp.skinserver.config.HeadView
import dev.themeinerlp.skinserver.config.SkinServerConfig
import dev.themeinerlp.skinserver.model.PlayerSkin
import dev.themeinerlp.skinserver.model.SkinProfile
import dev.themeinerlp.skinserver.repository.ProfileRepository
import dev.themeinerlp.skinserver.service.RenderService
import dev.themeinerlp.skinserver.service.SkinService
import dev.themeinerlp.skinserver.service.UUIDFetcher
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@RequestMapping("head/by")
@RestController
class HeadController(
    @Qualifier("skinServerConfig")
    val config: SkinServerConfig,
    val repository: ProfileRepository,
    val uuidFetcher: UUIDFetcher,
    val skinService: SkinService,
    val renderService: RenderService,
    val mapper: ObjectMapper
) {

    @Operation(
        summary = "Get a user head of a specified size",
        description = "Get a user head based on there UUID and Size and optional on the Rotation",
        responses = [
            ApiResponse(description = "User Head", content = [Content(mediaType = MediaType.IMAGE_PNG_VALUE)], responseCode = "200"),
            ApiResponse(description = "URL is empty for database entry!", content = [Content(mediaType = MediaType.TEXT_PLAIN_VALUE)], responseCode = "404"),
            ApiResponse(description = "Size are to big or to small", content = [Content(mediaType = MediaType.TEXT_PLAIN_VALUE)], responseCode = "405"),
            ApiResponse(description = "Something was wrong", content = [Content(mediaType = MediaType.TEXT_PLAIN_VALUE)], responseCode = "500")
        ]
    )
    @ResponseBody
    @RequestMapping(
        path = ["uuid/{size:[0-9]+}/{uuid:[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}}",
                "uuid/{size:[0-9]+}/{uuid:[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}}/{rotation}"],
        method = [RequestMethod.GET],
        produces = [MediaType.IMAGE_PNG_VALUE]
    )
    fun getByUUIDHead(
        @NotBlank
        @PathVariable(required = true) size: Int,
        @NotBlank
        @PathVariable(required = true) uuid: UUID,
        @PathVariable(name = "rotation", required = false) rotation: Optional<HeadView> = Optional.of(HeadView.Front)
    ): ResponseEntity<Any> {
        if (size < this.config.minSize!! || size > this.config.maxSize!!) {
            throw ResponseStatusException(
                HttpStatus.METHOD_NOT_ALLOWED,
                "\"${size}\" is no valide size! Use ${config.minSize} - ${config.maxSize}"
            )
        }
        var skinProfile: SkinProfile? = this.repository.findProfileByUuid(uuid)
        val username = if (skinProfile?.username != null) {
            skinProfile.username
        } else {
            this.mapper.readTree(this.uuidFetcher.getUser(uuid))["name"].asText()
        }
        val rotationEnum = rotation.orElse(HeadView.Front)
        val playerSkin = PlayerSkin(username!!, size, rotationEnum)
        if (skinProfile == null) {
            skinProfile = uuidFetcher.findPlayer(username)
            this.repository.insert(skinProfile)
        }
        val url: String? = this.skinService.extractSkinUrl(skinProfile.texture)
        if (url == null) {
            this.repository.delete(skinProfile)
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "URL is empty for database entry!")
        }
        if (!this.skinService.isCached(playerSkin, url)) {
            this.skinService.downloadSkin(url, playerSkin)
        }

        if (skinProfile.base64Texture == null) {
            this.skinService.saveTextureInDatabase(playerSkin, skinProfile)
        }

        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG)
            .body(InputStreamResource(this.renderService.renderHead(playerSkin).inputStream()))
    }

    @Operation(
        summary = "Get a user head of a specified size",
        description = "Get a user head based on there username and Size and optional on the Rotation",
        responses = [
            ApiResponse(description = "User Head", content = [Content(mediaType = MediaType.IMAGE_PNG_VALUE)], responseCode = "200"),
            ApiResponse(description = "URL is empty for database entry!", content = [Content(mediaType = MediaType.TEXT_PLAIN_VALUE)], responseCode = "404"),
            ApiResponse(description = "Size are to big or to small", content = [Content(mediaType = MediaType.TEXT_PLAIN_VALUE)], responseCode = "405"),
            ApiResponse(description = "Something was wrong", content = [Content(mediaType = MediaType.TEXT_PLAIN_VALUE)], responseCode = "500")
        ]
    )
    @ResponseBody
    @RequestMapping(
        "username/{size:[0-9]{1,4}}/{username:[a-zA-Z0-9_]{0,16}}",
        "username/{size:[0-9]{1,4}}/{username:[a-zA-Z0-9_]{0,16}}/{rotation}",
        method = [RequestMethod.GET],
        produces = [MediaType.IMAGE_PNG_VALUE]
    )
    fun getByUsernameHead(
        @NotBlank
        @PathVariable(required = true) size: Int,
        @NotBlank
        @Size(max = 16)
        @PathVariable(required = true) username: String,
        @PathVariable(required = false) rotation: Optional<HeadView> = Optional.of(HeadView.Front)
    ): ResponseEntity<Any> {
        if (size < this.config.minSize!! || size > this.config.maxSize!!) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,

                "\"${size}\" is no valide size! Use ${config.minSize} - ${config.maxSize}"
            )
        }
        val rotationEnum = rotation.orElse(HeadView.Front)
        val playerSkin = PlayerSkin(username, size, rotationEnum)
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
            this.skinService.downloadSkin(url, playerSkin)
        }
        if (skinProfile.base64Texture == null) {
            this.skinService.saveTextureInDatabase(playerSkin, skinProfile)
        }
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG)
            .body(InputStreamResource(this.renderService.renderHead(playerSkin).inputStream()))
    }
}