package dev.themeinerlp.skinserver.repository

import dev.themeinerlp.skinserver.model.SkinProfile
import org.springframework.data.mongodb.repository.MongoRepository

interface ProfileRepository : MongoRepository<SkinProfile, String> {

    fun findProfileByUsername(username: String): SkinProfile?
    fun findProfileByUuid(uuid: String): SkinProfile?

}