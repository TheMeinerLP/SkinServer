package dev.themeinerlp.skinserver.model

import org.springframework.data.annotation.Id

class Skin {

    @Id
    var uuid: String? = null
    var skinUrl: String? = null
    var tags: List<String>? = null

}