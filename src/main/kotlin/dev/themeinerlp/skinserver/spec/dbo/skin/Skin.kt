package dev.themeinerlp.skinserver.spec.dbo.skin

import java.util.UUID
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class Skin(
    @Id val uuid: UUID? = null,
    val skinUrl: String? = null,
    val texture: String? = null,
    val username: String? = null
)
