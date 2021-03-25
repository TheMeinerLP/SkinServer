package dev.themeinerlp.skinserver.repository

import dev.themeinerlp.skinserver.model.Skin
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.*

interface SkinRepository : MongoRepository<Skin, String> {

    fun findByUuid(uuid: UUID): Skin?
    fun findByUsername(username: String): Skin?

}