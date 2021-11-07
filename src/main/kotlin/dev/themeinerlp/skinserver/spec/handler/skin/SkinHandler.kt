package dev.themeinerlp.skinserver.spec.handler.skin

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
import org.springframework.web.bind.annotation.RestController

interface SkinDatabaseHandler {

    fun getSkinByUsername(size: Int?, username: String?): ResponseEntity<Any>
    fun getSkinByUUID(size: Int?, uuid: UUID?): ResponseEntity<Any>
}

@RestController
class SkinHandler {

    @Autowired
    lateinit var databaseHandler: SkinDatabaseHandler

    @Operation(
        summary = "Request a skin based on there username and size",
        description = "Render a skin and prefetch inside of a database. Given size is scaling up or scaling down the skin"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                description = "Successful requested skin",
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
                description = "Size are to small or to big",
                responseCode = "500",
                content = []
            )
        ]
    )
    @RequestMapping("skin/username/{size:\\d+}/{username:[a-zA-Z0-9_]{0,16}}/",
        method = [RequestMethod.GET],
        produces = [MediaType.IMAGE_PNG_VALUE]
    )
    fun getSkinByUsername(
        @Parameter(
            description = "Skin scale size, default is 64",
            required = true,
            example = "64",
        )
        @PathVariable(required = true) size: Int? = 64,
        @Parameter(
            description = "Player name to display skin",
            required = true,
            example = "Notch",
        )
        @PathVariable(required = true) username: String?
    ): ResponseEntity<Any> {
        return databaseHandler.getSkinByUsername(size, username)
    }

    @Operation(
        summary = "Request a skin based on there uuid and size",
        description = "Render a skin and prefetch inside of a database. Given size is scaling up or scaling down the skin"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                description = "Successful requested skin",
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
                description = "Size are to small or to big",
                responseCode = "500",
                content = []
            )
        ]
    )
    @RequestMapping(
        "skin/uuid/{size:\\d+}/{uuid:[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}}/",
        method = [RequestMethod.GET],
        produces = [MediaType.IMAGE_PNG_VALUE]
    )
    fun gerSkinByUUID(
        @Parameter(
            description = "Skin scale site, default 64",
            required = true,
            example = "64"
        )
        @PathVariable(required = true) size: Int? = 64,
        @Parameter(
            description = "Player uuid to display skin",
            required = true,
            example = "069a79f4-44e9-4726-a5be-fca90e38aaf5"
        )
        @PathVariable(required = true) uuid: UUID? = UUID.fromString("069a79f4-44e9-4726-a5be-fca90e38aaf5")
    ): ResponseEntity<Any> {
        return databaseHandler.getSkinByUUID(size, uuid)
    }
}