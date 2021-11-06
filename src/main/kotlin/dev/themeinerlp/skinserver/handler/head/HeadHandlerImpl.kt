package dev.themeinerlp.skinserver.handler.head

import dev.themeinerlp.skinserver.properties.SkinServerProperties
import dev.themeinerlp.skinserver.service.GameProfileService
import dev.themeinerlp.skinserver.service.RenderService
import dev.themeinerlp.skinserver.service.SkinService
import dev.themeinerlp.skinserver.spec.dao.skin.Skin
import dev.themeinerlp.skinserver.spec.handler.head.HeadDatabaseHandler
import dev.themeinerlp.skinserver.spec.repository.skin.SkinRepository
import dev.themeinerlp.skinserver.utils.HeadView
import java.util.Base64
import java.util.UUID
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException

@Component
class HeadHandlerImpl(
    val skinServerProperties: SkinServerProperties,
    val gameProfileService: GameProfileService,
    val skinService: SkinService,
    val renderService: RenderService,
    val skinRepository: SkinRepository
) : HeadDatabaseHandler {

    override fun getHeadByUsername(
        size: Int?,
        username: String?,
        rotation: HeadView?,
        layer: Boolean?
    ): ResponseEntity<Any> {
        size ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Size is empty")
        username ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Username is empty")
        rotation ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Rotation is empty")
        if (size < skinServerProperties.minSize || size > skinServerProperties.maxSize) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Size are to small or to big")
        }
        val skin = this.skinRepository.findByUsernameIgnoreCase(username) ?: getSkinBasedUsername(username)
        return renderHead(size, skin, rotation, layer ?: true)
    }

    override fun getHeadByUUID(size: Int?, uuid: UUID?, rotation: HeadView?, layer: Boolean?): ResponseEntity<Any> {
        size ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Size is empty")
        uuid ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "UUID is empty")
        rotation ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Rotation is empty")
        if (size < this.skinServerProperties.minSize || size > this.skinServerProperties.maxSize) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Size are to small or to big")
        }
        val skin = this.skinRepository.findByUuid(uuid) ?: getSkinBasedUUID(uuid)
        return renderHead(size, skin, rotation, layer ?: true)
    }


    private fun renderHead(size: Int, skin: Skin, rotation: HeadView, layer: Boolean): ResponseEntity<Any> {
        val value = Base64.getDecoder().decode(skin.texture)
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(
            InputStreamResource(
                this.renderService.renderHeadFromByteArray(size, rotation, value, layer).inputStream()
            )
        )
    }

    private fun getSkinBasedUUID(uuid: UUID): Skin {
        val user = this.gameProfileService.getGameProfile(uuid)
        val skinUrl = this.skinService.extractSkinUrl(this.gameProfileService.getTextureFromJson(user))
        val skin = Skin(
            username = this.gameProfileService.getNameFromJson(user),
            uuid = uuid,
            skinUrl = skinUrl,
            texture = String(Base64.getEncoder().encode(this.gameProfileService.downloadUrlToByteArray(skinUrl)))
        )
        return this.skinRepository.save(skin)
    }

    private fun getSkinBasedUsername(username: String): Skin {
        val player = this.gameProfileService.findGameProfile(username)
        val user = this.gameProfileService.getGameProfile(player.uuid)
        val skinUrl = this.skinService.extractSkinUrl(this.gameProfileService.getTextureFromJson(user))
        val skin = Skin(
            username = this.gameProfileService.getNameFromJson(user),
            uuid = player.uuid,
            skinUrl = skinUrl,
            texture = String(Base64.getEncoder().encode(this.gameProfileService.downloadUrlToByteArray(skinUrl)))
        )
        return this.skinRepository.save(skin)
    }
}