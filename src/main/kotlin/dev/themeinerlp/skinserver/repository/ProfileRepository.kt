package dev.themeinerlp.skinserver.repository

import dev.themeinerlp.skinserver.model.SkinProfile
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.*

interface ProfileRepository : MongoRepository<SkinProfile, String> {

    fun findProfileByUsername(username: String): SkinProfile?
    fun findProfileByUuid(uuid: UUID): SkinProfile?

}