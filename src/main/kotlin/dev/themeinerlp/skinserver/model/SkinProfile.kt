package dev.themeinerlp.skinserver.model

import org.springframework.data.annotation.Id

class SkinProfile {

    @Id
    var id: String? = null
    var username: String? = null
    var texture: String? = null
}