package dev.themeinerlp.skinserver.spec.handler.render

import dev.themeinerlp.skinserver.utils.HeadView
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import javax.servlet.http.Part
import org.springframework.web.bind.annotation.*

interface RenderDatabaseHandler {
    fun renderHead(skin: Part?, size: Int?, rotation: HeadView?, layer: Boolean?): ResponseEntity<Any>
}

@RestController
class RenderHandler {

    @Autowired
    lateinit var databaseHandler: RenderDatabaseHandler

    @Operation(
        summary = "Render a skin-head based on there username, skin file and size",
        description = "Render a head of the give file."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                description = "Successful render head",
                responseCode = "200",
                content = [
                    Content(
                        mediaType = MediaType.IMAGE_PNG_VALUE,
                    )
                ]
            ),
            ApiResponse(
                description = "Size is empty",
                responseCode = "404",
                content = []
            ),
            ApiResponse(
                description = "Skin is empty",
                responseCode = "404",
                content = []
            ),
            ApiResponse(
                description = "Size are to small or to big",
                responseCode = "500",
                content = []
            )
        ]
    )
    @RequestMapping(
        "render/{size:\\d+}/",
        "render/{size:\\d+}/{rotation}/",
        method = [RequestMethod.POST],
        produces = [MediaType.IMAGE_PNG_VALUE],
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    fun renderHead(
        @Parameter(
            description = "Skin file",
            required = true,
        )
        @RequestPart(required = true)
        skin: Part?,
        @Parameter(
            description = "Head scale, default is 64",
            required = true,
            example = "64",
        )
        @PathVariable(required = true)
        size: Int? = 64,
        @Parameter(
            description = "Side of the head",
            required = false,
            example = "Front",
        )
        @PathVariable(required = false) rotation: HeadView? = HeadView.Front,
        @Parameter(
            description = "Show layer of skin at the head, default true",
            required = false,
            example = "true",
        )
        @RequestParam(required = false) layer: Boolean? = true
    ): ResponseEntity<Any> {
        return databaseHandler.renderHead(skin, size, rotation, layer)
    }
}