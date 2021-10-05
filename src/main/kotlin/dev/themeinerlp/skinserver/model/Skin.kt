package dev.themeinerlp.skinserver.model

import org.springframework.data.annotation.Id
import java.util.*

data class Skin(
    @Id
    var uuid: UUID?,
    var skinUrl: String?,
    var texture: String?,
    var username: String?
) {
    constructor() : this(null, null, null, null)
}