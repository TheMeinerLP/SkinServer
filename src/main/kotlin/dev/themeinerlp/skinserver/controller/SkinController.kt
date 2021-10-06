package dev.themeinerlp.skinserver.controller

import dev.themeinerlp.skinserver.model.Skin
import dev.themeinerlp.skinserver.properties.SkinServerProperties
import dev.themeinerlp.skinserver.repository.SkinRepository
import dev.themeinerlp.skinserver.service.RenderService
import dev.themeinerlp.skinserver.service.SkinService
import dev.themeinerlp.skinserver.service.GameProfileService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import java.util.Base64
import java.util.UUID
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

@RestController
class SkinController(
    val skinServerProperties: SkinServerProperties,
    val gameProfileService: GameProfileService,
    val skinService: SkinService,
    val renderService: RenderService,
    val skinRepository: SkinRepository
) {

    @Operation(
        summary = "Get a player based on a size, username from the database",
        description = "Renders a skin based on there username, defined size",
        responses = [
            ApiResponse(
                description = "User Skin",
                content = [Content(mediaType = MediaType.IMAGE_PNG_VALUE)],
                responseCode = "200"
            ),
            ApiResponse(
                description = "URL is empty for database entry!",
                content = [Content(mediaType = MediaType.TEXT_PLAIN_VALUE)],
                responseCode = "404"
            ),
            ApiResponse(
                description = "Size are to big or to small",
                content = [Content(mediaType = MediaType.TEXT_PLAIN_VALUE)],
                responseCode = "405"
            ),
            ApiResponse(
                description = "Something was wrong",
                content = [Content(mediaType = MediaType.TEXT_PLAIN_VALUE)],
                responseCode = "500"
            )
        ]
    )
    @ResponseBody
    @RequestMapping(
        "skin/username/{size}/{username}",
        method = [RequestMethod.GET]
    )
    fun getByUsernameSkin(
        @Parameter(
            description = "A size for the skin",
            required = true,
            example = "64",
            name = "size",
            `in` = ParameterIn.PATH
        )
        @PathVariable(required = true) size: Int,
        @Parameter(
            description = "The username for the skin to be render",
            required = true,
            example = "Notch",
            name = "username",
            `in` = ParameterIn.PATH
        )
        @PathVariable(required = true) username: String
    ): ResponseEntity<Any> {
        if (size < this.skinServerProperties.minSize || size > this.skinServerProperties.maxSize)
            throw ResponseStatusException(
                HttpStatus.METHOD_NOT_ALLOWED,
                "\"${size}\" is no valide size! Use ${skinServerProperties.minSize} - ${skinServerProperties.maxSize}"
            )

        var skin = this.skinRepository.findByUsernameIgnoreCase(username)
        if (skin == null) {
            skin = Skin()
            val player = this.gameProfileService.findGameProfile(username)
            val user = this.gameProfileService.getGameProfile(player.uuid)
            val skinUrl = this.skinService.extractSkinUrl(this.gameProfileService.getTextureFromJson(user))
            skin.username = this.gameProfileService.getNameFromJson(user)
            skin.uuid = player.uuid
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

    @Operation(
        summary = "Get a player based on a size, uuid from the database",
        description = "Renders a skin based on there uuid, defined size",
        responses = [
            ApiResponse(
                description = "User Skin",
                content = [Content(mediaType = MediaType.IMAGE_PNG_VALUE)],
                responseCode = "200"
            ),
            ApiResponse(
                description = "URL is empty for database entry!",
                content = [Content(mediaType = MediaType.TEXT_PLAIN_VALUE)],
                responseCode = "404"
            ),
            ApiResponse(
                description = "Size are to big or to small",
                content = [Content(mediaType = MediaType.TEXT_PLAIN_VALUE)],
                responseCode = "405"
            ),
            ApiResponse(
                description = "Something was wrong",
                content = [Content(mediaType = MediaType.TEXT_PLAIN_VALUE)],
                responseCode = "500"
            )
        ]
    )
    @ResponseBody
    @RequestMapping(
        "skin/uuid/{size}/{uuid:[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}}",
        method = [RequestMethod.GET]
    )
    fun getByUUIDSkin(
        @Parameter(
            description = "A size for the skin",
            required = true,
            example = "64",
            name = "size",
            `in` = ParameterIn.PATH
        )
        @PathVariable(required = true) size: Int,
        @Parameter(
            description = "The uuid for the skin to be render",
            required = true,
            example = "05bf52c6-7bb0-4f13-8951-0e1fd803df35",
            name = "uuid",
            `in` = ParameterIn.PATH
        )
        @PathVariable(required = true) uuid: UUID
    ): ResponseEntity<Any> {
        if (size < this.skinServerProperties.minSize || size > this.skinServerProperties.maxSize)
            throw ResponseStatusException(
                HttpStatus.METHOD_NOT_ALLOWED,
                "\"${size}\" is no valide size! Use ${skinServerProperties.minSize} - ${skinServerProperties.maxSize}"
            )
        var skin = this.skinRepository.findByUuid(uuid)
        if (skin == null) {
            skin = Skin()
            val user = this.gameProfileService.getGameProfile(uuid)
            val skinUrl = this.skinService.extractSkinUrl(this.gameProfileService.getTextureFromJson(user))
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