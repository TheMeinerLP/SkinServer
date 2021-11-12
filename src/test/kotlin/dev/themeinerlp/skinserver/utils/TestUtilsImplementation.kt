package dev.themeinerlp.skinserver.utils

import dev.themeinerlp.skinserver.spec.dao.skin.Skin
import dev.themeinerlp.skinserver.spec.repository.skin.SkinRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class TestUtilsImplementation {

    @Autowired
    lateinit var skinRepository: SkinRepository

    fun createTestSkins(count: Int = 2): List<Skin> {
        return (1..count).map {
            skinRepository.save(
                Skin(
                    username = "fakeuser-$it",
                    skinUrl = "https://fakeurl-$it.org",
                    texture = "faketexture-$it"
                )
            )
        }
    }
}