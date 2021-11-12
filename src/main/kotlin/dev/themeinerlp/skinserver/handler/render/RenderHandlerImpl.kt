package dev.themeinerlp.skinserver.handler.render

import dev.themeinerlp.skinserver.properties.SkinServerProperties
import dev.themeinerlp.skinserver.service.RenderService
import dev.themeinerlp.skinserver.spec.handler.render.RenderDatabaseHandler
import dev.themeinerlp.skinserver.utils.HeadView
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import javax.servlet.http.Part

@Component
class RenderHandlerImpl(
    val renderService: RenderService,
    val skinServerProperties: SkinServerProperties,
) : RenderDatabaseHandler {

    override fun renderHead(
        skin: Part?,
        size: Int?,
        rotation: HeadView?,
        layer: Boolean?
    ): ResponseEntity<Any> {
        size ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Size is empty")
        skin ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Skin is empty")
        if (size < skinServerProperties.minSize || size > skinServerProperties.maxSize) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Size are to small or to big")
        }
        val content = skin.inputStream.use {
            this.renderService.renderHeadFromByteArray(size, rotation ?: HeadView.Front, it.readBytes(), layer ?: true)
        }
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(InputStreamResource(content.inputStream()))
    }
}