package dev.themeinerlp.skinserver.model

import org.springframework.data.annotation.Id
import java.util.*

class SkinProfile {
    @Id
    var id: String? = null
    var username: String? = null
    var uuid: UUID? = null
    var texture: String? = null
    var base64Texture: String? = null
}