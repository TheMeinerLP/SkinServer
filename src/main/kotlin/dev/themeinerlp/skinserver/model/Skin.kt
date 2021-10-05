package dev.themeinerlp.skinserver.model

import java.util.UUID
import org.springframework.data.annotation.Id

data class Skin(
    @Id
    var uuid: UUID?,
    var skinUrl: String?,
    var texture: String?,
    var username: String?
) {
    constructor() : this(null, null, null, null)
}