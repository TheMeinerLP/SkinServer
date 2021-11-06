package dev.themeinerlp.skinserver.spec.repository.skin

import dev.themeinerlp.skinserver.spec.dao.skin.Skin
import java.util.UUID
import org.springframework.data.mongodb.repository.MongoRepository

interface SkinRepository : MongoRepository<Skin, String> {

    fun findByUuid(uuid: UUID): Skin?
    fun findByUsernameIgnoreCase(username: String): Skin?
}