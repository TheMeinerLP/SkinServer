package dev.themeinerlp.skinserver.controller

import dev.themeinerlp.skinserver.config.HeadView
import dev.themeinerlp.skinserver.service.RenderService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Encoding
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.core.io.InputStreamResource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.*
import javax.validation.constraints.NotBlank

@RequestMapping("rendern")
@RestController
class RenderController(
    val renderService: RenderService,
) {

    @Operation(
        summary = "Render a head based on a file",
        description = "Render a head based on a file value of a png",
        responses = [
            ApiResponse(
                description = "User Head",
                content = [Content(mediaType = MediaType.IMAGE_PNG_VALUE)],
                responseCode = "200"
            ),
            ApiResponse(
                description = "Something was wrong",
                content = [Content(mediaType = MediaType.TEXT_PLAIN_VALUE)],
                responseCode = "500"
            )
        ],
    )
    @ResponseBody
    @RequestMapping(
        "{size:[0-9]+}",
        "{size:[0-9]+}/{rotation}",
        method = [RequestMethod.POST],
        produces = [MediaType.IMAGE_PNG_VALUE],
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    fun renderSkinHead(
        @Parameter(
            description = "The skin file",
            required = true,
            name = "body"
        )
        @RequestPart("body", required = true)
        body: MultipartFile,
        @NotBlank
        @Parameter(
            description = "The size for the head to be render",
            required = true,
            example = "16",
            name = "size"
        )
        @PathVariable(required = true) size: Int,
        @Parameter(
            description = "Defines the render side of the head, front, right side etc.",
            required = true,
            example = "FRONT",
            name = "rotation"
        )
        @PathVariable(required = false) rotation: Optional<HeadView> = Optional.of(HeadView.Front),
        @Parameter(description = "Allows to enable or disable skin layer", required = false, example = "true")
        @RequestParam(name = "layer", required = false) layer: Optional<Boolean> = Optional.of(true)
    ): ResponseEntity<Any> {
        val rotationEnum = rotation.orElse(HeadView.Front)
        val layerBoolean = layer.orElse(true)
        val content = body.inputStream.use {
            this.renderService.renderHeadFromByteArray(size, rotationEnum, it.readAllBytes(), layerBoolean)
        }
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(InputStreamResource(content.inputStream()))
    }

}