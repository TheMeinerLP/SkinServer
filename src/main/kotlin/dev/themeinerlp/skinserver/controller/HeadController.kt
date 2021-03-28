package dev.themeinerlp.skinserver.controller

import dev.themeinerlp.skinserver.config.HeadView
import dev.themeinerlp.skinserver.config.SkinServerConfig
import dev.themeinerlp.skinserver.model.Skin
import dev.themeinerlp.skinserver.repository.SkinRepository
import dev.themeinerlp.skinserver.service.GameProfileService
import dev.themeinerlp.skinserver.service.RenderService
import dev.themeinerlp.skinserver.service.SkinService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
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

@RequestMapping("head/by")
@RestController
class HeadController(
    @Qualifier("skinServerConfig")
    val config: SkinServerConfig,
    val gameProfileService: GameProfileService,
    val skinService: SkinService,
    val renderService: RenderService,
    val skinRepository: SkinRepository
) {

    @Operation(
        summary = "Get a player head based on a size, uuid and a rotation",
        description = "Renders a skin head based on there UUID, defined size a optional a rotation. Also with ?layer=false can be disable skin layer",
        responses = [
            ApiResponse(
                description = "User Head",
                content = [Content(mediaType = MediaType.IMAGE_PNG_VALUE)],
                responseCode = "200"
            ),
            ApiResponse(
                description = "User cannot be found!",
                content = [Content(mediaType = MediaType.TEXT_PLAIN_VALUE)],
                responseCode = "404"
            ),
            ApiResponse(
                description = "Skin URL are empty",
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
        "uuid/{size:[0-9]+}/{uuid:[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}}",
        "uuid/{size:[0-9]+}/{uuid:[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}}/{rotation}",
        method = [RequestMethod.GET],
        produces = [MediaType.IMAGE_PNG_VALUE]
    )
    fun getByUUIDHead(
        @Parameter(
            description = "A size for the head",
            required = true,
            example = "64",
            name = "size",
            `in` = ParameterIn.PATH
        )
        @PathVariable(required = true) size: Int,
        @Parameter(
            description = "The uuid for the head to be render",
            required = true,
            example = "05bf52c6-7bb0-4f13-8951-0e1fd803df35",
            name = "uuid",
            `in` = ParameterIn.PATH
        )
        @PathVariable(required = true) uuid: UUID,
        @Parameter(
            description = "Defines the render side of the head, front, right side etc.",
            required = false,
            example = "FRONT",
            name = "rotation",
            `in` = ParameterIn.PATH
        )
        @PathVariable(required = false, name = "rotation") rotation: Optional<HeadView> = Optional.of(HeadView.Front),
        @Parameter(
            description = "Allows to enable or disable skin layer",
            required = false,
            example = "true",
            name = "layer",
            `in` = ParameterIn.QUERY
        )
        @RequestParam(name = "layer", required = false) layer: Optional<Boolean> = Optional.of(true)
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
            val skinUrl = this.skinService.extractSkinUrl(
                this.gameProfileService.getTextureFromJson(user) ?: throw ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Skin URL are empty"
                )
            ) ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Skin URL are empty"
            )
            skin.username = this.gameProfileService.getNameFromJson(user)
            skin.uuid = uuid
            skin.skinUrl = skinUrl
            skin.texture = String(Base64.getEncoder().encode(this.gameProfileService.downloadUrlToByteArray(skinUrl)))
            this.skinRepository.save(skin)
        }
        val rotationEnum = rotation.orElse(HeadView.Front)
        val layerBoolean = layer.orElse(true)
        val value = Base64.getDecoder().decode(skin.texture)
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(
            InputStreamResource(
                this.renderService.renderHeadFromByteArray(size, rotationEnum, value, layerBoolean).inputStream()
            )
        )
    }

    @Operation(
        summary = "Get a player head based on a size, username from the database and a rotation",
        description = "Renders a skin head based on there username, defined size a optional a rotation. Also with ?layer=false can be disable skin layer",
        responses = [
            ApiResponse(
                description = "User Head",
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
        "username/{size:[0-9]{1,4}}/{username:[a-zA-Z0-9_]{0,16}}",
        "username/{size:[0-9]{1,4}}/{username:[a-zA-Z0-9_]{0,16}}/{rotation}",
        method = [RequestMethod.GET],
        produces = [MediaType.IMAGE_PNG_VALUE]
    )
    fun getByUsernameHead(
        @Parameter(
            description = "A size for the head",
            required = true,
            example = "64",
            name = "size",
            `in` = ParameterIn.PATH
        )
        @PathVariable(required = true, name = "size") size: Int,
        @Parameter(
            description = "The username for the head to be render",
            required = true,
            example = "Notch",
            name = "username",
            `in` = ParameterIn.PATH
        )
        @PathVariable(required = true, name = "username") username: String,
        @Parameter(
            description = "Defines the render side of the head, front, right side etc.",
            required = false,
            example = "FRONT",
            name = "rotation",
            `in` = ParameterIn.PATH
        )
        @PathVariable(required = false, name = "rotation") rotation: Optional<HeadView> = Optional.of(HeadView.Front),
        @Parameter(
            description = "Allows to enable or disable skin layer",
            required = false,
            example = "true",
            name = "layer",
            `in` = ParameterIn.QUERY
        )
        @RequestParam(name = "layer", required = false) layer: Optional<Boolean> = Optional.of(true)
    ): ResponseEntity<Any> {
        if (size < this.config.minSize!! || size > this.config.maxSize!!) {
            throw ResponseStatusException(
                HttpStatus.METHOD_NOT_ALLOWED,
                "\"${size}\" is no valide size! Use ${config.minSize} - ${config.maxSize}"
            )
        }
        var skin = this.skinRepository.findByUsernameIgnoreCase(username)
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
            val skinUrl = this.skinService.extractSkinUrl(
                this.gameProfileService.getTextureFromJson(user) ?: throw ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Skin URL are empty"
                )
            ) ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Skin URL are empty"
            )
            skin.username = this.gameProfileService.getNameFromJson(user)
            skin.uuid = player.uuid!!
            skin.skinUrl = skinUrl
            skin.texture = String(Base64.getEncoder().encode(this.gameProfileService.downloadUrlToByteArray(skinUrl)))
            this.skinRepository.save(skin)
        }
        val layerBoolean = layer.orElse(true)
        val rotationEnum = rotation.orElse(HeadView.Front)
        val value = Base64.getDecoder().decode(skin.texture)
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(
            InputStreamResource(
                this.renderService.renderHeadFromByteArray(size, rotationEnum, value, layerBoolean).inputStream()
            )
        )
    }
}