package dev.themeinerlp.skinserver.controller

import com.fasterxml.jackson.databind.ObjectMapper
import dev.themeinerlp.skinserver.config.HeadView
import dev.themeinerlp.skinserver.config.SkinServerConfig
import dev.themeinerlp.skinserver.model.PlayerSkin
import dev.themeinerlp.skinserver.repository.ProfileRepository
import dev.themeinerlp.skinserver.service.RenderService
import dev.themeinerlp.skinserver.service.SkinService
import dev.themeinerlp.skinserver.service.UUIDFetcher
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.io.InputStreamResource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Optional;
import javax.servlet.http.Part
import javax.validation.constraints.NotBlank

@RequestMapping("rendern")
@RestController
class RenderController(
    @Qualifier("skinServerConfig")
    val config: SkinServerConfig,
    val renderService: RenderService,
) {

    @Operation(
        summary = "Render a head based on a file",
        description = "Render a head based on a file value of a png",
        responses = [
            ApiResponse(description = "User Head", content = [Content(mediaType = MediaType.IMAGE_PNG_VALUE)], responseCode = "200"),
            ApiResponse(description = "Something was wrong", content = [Content(mediaType = MediaType.TEXT_PLAIN_VALUE)], responseCode = "500")
        ]
    )
    @ResponseBody
    @RequestMapping(
        "{size:[0-9]+}",
        "{size:[0-9]+}/{rotation}",
        method = [RequestMethod.POST],
        produces = [MediaType.IMAGE_PNG_VALUE]
    )
    fun renderSkinHead(
        @RequestBody
        body: Part,
        @NotBlank
        @PathVariable(required = true) size: Int,
        @PathVariable(required = false) rotation: Optional<HeadView> = Optional.of(HeadView.Front)
    ): ResponseEntity<Any> {
        val rotationEnum = rotation.orElse(HeadView.Front)
        val content = body.inputStream.use {
            this.renderService.renderHeadFromByteArray(size,rotationEnum, it.readAllBytes())
        }
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(InputStreamResource(content.inputStream()))
    }

}