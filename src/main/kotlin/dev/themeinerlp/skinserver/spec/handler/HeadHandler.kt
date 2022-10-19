package dev.themeinerlp.skinserver.spec.handler

import dev.themeinerlp.skinserver.spec.database.HeadDatabaseHandler
import dev.themeinerlp.skinserver.utils.HeadView
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import java.util.UUID
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class HeadHandler {

    @Autowired
    lateinit var databaseHandler: HeadDatabaseHandler

    @Operation(
        summary = "Request a skin-head based on there username and size",
        description = "Render a head of a skin and prefetch inside of a database. Given size is scaling up or scaling down the head"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                description = "Successful requested head",
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
                description = "Username is empty",
                responseCode = "404",
                content = []
            ),
            ApiResponse(
                description = "view is empty",
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
        "head/username/{size:\\d+}/{username:[a-zA-Z0-9_]{0,16}}",
        "head/username/{size:\\d+}/{username:[a-zA-Z0-9_]{0,16}}/{rotation}",
        method = [RequestMethod.GET],
        produces = [MediaType.IMAGE_PNG_VALUE]
    )
    fun getHeadByUsername(
        @Parameter(
            description = "Head scale, default is 64",
            required = true,
            example = "64",
        )
        @PathVariable(required = true) size: Int? = 64,
        @Parameter(
            description = "Player name to display head",
            required = true,
            example = "Notch",
        )
        @PathVariable(required = true) username: String? = "Notch",
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
        return databaseHandler.getHeadByUsername(size, username, rotation, layer)
    }

    @Operation(
        summary = "Request a skin-head based on there uuid and size",
        description = "Render a head of a skin and prefetch inside of a database. Given size is scaling up or scaling down the head"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                description = "Successful requested head",
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
                description = "Uuid is empty",
                responseCode = "404",
                content = []
            ),
            ApiResponse(
                description = "view is empty",
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
        "head/uuid/{size:\\d+}/{uuid:[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}}",
        "head/uuid/{size:\\d+}/{uuid:[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}}/{rotation}",
        method = [RequestMethod.GET],
        produces = [MediaType.IMAGE_PNG_VALUE]
    )
    fun getHeadByUUID(
        @Parameter(
            description = "Head scale, default is 64",
            required = true,
            example = "64",
        )
        @PathVariable(required = true) size: Int? = 64,
        @Parameter(
            description = "Player uuid to display skin",
            required = true,
            example = "069a79f4-44e9-4726-a5be-fca90e38aaf5"
        )
        @PathVariable(required = true) uuid: UUID? = UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5"),
        @Parameter(
            description = "Site of the head",
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
        return databaseHandler.getHeadByUUID(size, uuid, rotation, layer)
    }

}