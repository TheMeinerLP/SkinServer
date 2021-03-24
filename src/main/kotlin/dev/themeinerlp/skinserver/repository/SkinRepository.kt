package dev.themeinerlp.skinserver.repository

import dev.themeinerlp.skinserver.model.Skin
import org.springframework.data.mongodb.repository.MongoRepository

interface SkinRepository : MongoRepository<Skin, String> {

    fun findByUuid(uuid: String): Skin
    fun findByTagsIsLike(tag: String): Skin

}