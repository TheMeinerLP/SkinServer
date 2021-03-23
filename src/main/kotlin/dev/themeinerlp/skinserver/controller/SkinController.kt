package dev.themeinerlp.skinserver.controller

import dev.themeinerlp.skinserver.model.SkinProfile
import dev.themeinerlp.skinserver.repository.ProfileRepository
import org.springframework.web.bind.annotation.RestController

@RestController
class SkinController(repository: ProfileRepository) {

    private val repo: ProfileRepository = repository

    fun getAllSkins(): List<SkinProfile> {
        return this.repo.findAll()
    }

}