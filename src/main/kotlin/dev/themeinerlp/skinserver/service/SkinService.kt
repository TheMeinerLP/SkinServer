package dev.themeinerlp.skinserver.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import java.util.Base64;
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

@Service
class SkinService(
    val mapper: ObjectMapper
) {

    /**
     * Extract url from json string
     */
    fun extractSkinUrl(texture: String?): String {
        if (texture == null) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Texture are empty")
        val jsonObject = String(Base64.getDecoder().decode(texture))
        val node = this.mapper.readTree(jsonObject)

        return if (node.has("textures")) {
            node.get("textures").get("SKIN").get("url").asText()
        } else throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot extract skin texture")
    }
}