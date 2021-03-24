package dev.themeinerlp.skinserver.controller

import com.fasterxml.jackson.databind.ObjectMapper
import dev.themeinerlp.skinserver.config.HeadView
import dev.themeinerlp.skinserver.config.SkinServerConfig
import dev.themeinerlp.skinserver.model.PlayerSkin
import dev.themeinerlp.skinserver.repository.ProfileRepository
import dev.themeinerlp.skinserver.service.RenderService
import dev.themeinerlp.skinserver.service.SkinService
import dev.themeinerlp.skinserver.service.UUIDFetcher
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
    val repository: ProfileRepository,
    val uuidFetcher: UUIDFetcher,
    val skinService: SkinService,
    val renderService: RenderService,
    val mapper: ObjectMapper
) {

    @ResponseBody
    @RequestMapping(
        "{size}",
        "{size}/{rotation}",
        method = [RequestMethod.POST]
    )
    fun renderSkinHead(
        @RequestBody
        body: Part,
        @NotBlank
        @PathVariable(required = true) size: Int?,
        @PathVariable(required = false) rotation: Optional<String>
    ): ResponseEntity<Any> {
        val rotationEnum = if (rotation.isPresent) {
            val firstOrNull = HeadView.values().firstOrNull { it.name.equals(rotation.get(), ignoreCase = true) }
            firstOrNull ?: HeadView.Front
        } else {
            HeadView.Front
        }
        val playerSkin = PlayerSkin("example", size!!, rotationEnum)
        val content = body.inputStream.use {
            this.renderService.renderHeadFromByteArray(playerSkin, it.readAllBytes())
        }
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(InputStreamResource(content.inputStream()))
    }

}