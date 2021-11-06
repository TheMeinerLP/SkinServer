package dev.themeinerlp.skinserver.handler.skin

import dev.themeinerlp.skinserver.properties.SkinServerProperties
import dev.themeinerlp.skinserver.service.GameProfileService
import dev.themeinerlp.skinserver.service.RenderService
import dev.themeinerlp.skinserver.service.SkinService
import dev.themeinerlp.skinserver.spec.dao.skin.Skin
import dev.themeinerlp.skinserver.spec.handler.skin.SkinDatabaseHandler
import dev.themeinerlp.skinserver.spec.repository.skin.SkinRepository
import java.util.Base64
import java.util.UUID
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException

@Component
class SkinHandlerImpl(
    val skinRepository: SkinRepository,
    val skinServerProperties: SkinServerProperties,
    val gameProfileService: GameProfileService,
    val skinService: SkinService,
    val renderService: RenderService,
) : SkinDatabaseHandler {

    override fun getSkinByUsername(size: Int?, username: String?): ResponseEntity<Any> {
        size ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Size is empty")
        username ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Username is empty")
        if (size < skinServerProperties.minSize || size > skinServerProperties.maxSize) {
           throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Size are to small or to big")
        }
        val skin = this.skinRepository.findByUsernameIgnoreCase(username) ?: getSkinBasedUsername(username)
        return renderSkin(size, skin)
    }

    override fun getSkinByUUID(size: Int?, uuid: UUID?): ResponseEntity<Any> {
        size ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Size is empty")
        uuid ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "UUID is empty")
        if (size < this.skinServerProperties.minSize || size > this.skinServerProperties.maxSize) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Size are to small or to big")
        }
        val skin = this.skinRepository.findByUuid(uuid) ?: getSkinBasedUUID(uuid)
        return renderSkin(size, skin)
    }

    private fun renderSkin(size: Int, skin: Skin): ResponseEntity<Any> {
        val value = Base64.getDecoder().decode(skin.texture)
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(
            InputStreamResource(
                this.renderService.renderSkinFromByteArray(size, value).inputStream()
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