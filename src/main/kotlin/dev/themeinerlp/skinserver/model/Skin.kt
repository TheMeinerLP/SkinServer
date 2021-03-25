package dev.themeinerlp.skinserver.model

import org.springframework.data.annotation.Id
import java.util.*

class Skin {

    @Id
    var uuid: UUID? = null
    var skinUrl: String? = null
    var texture: String? = null
    var username: String? = null

}