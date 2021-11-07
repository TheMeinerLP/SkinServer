package dev.themeinerlp.skinserver.handler.skin.implementation

import dev.themeinerlp.skinserver.TestBase
import dev.themeinerlp.skinserver.spec.dao.skin.Skin
import dev.themeinerlp.skinserver.spec.repository.skin.SkinRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import java.util.*

class SkinDatabaseHandlerTest : TestBase() {

    @Autowired
    lateinit var skinRepository: SkinRepository

    @Test
    fun create() = runBlocking {
        val skin = skinRepository.save(
            Skin(
               texture = "fake",
               username = "FakeUsername",
               skinUrl = "https://fakeurl.org"
            )
        )

        val skinSkin = skinRepository.findByUuid(skin.uuid!!)
        Assert.assertEquals(skin, skinSkin)
    }

    @Test
    fun update() = runBlocking {
        val skins = testUtilsImplementation.createTestSkins()
        val toUpdateSkin = skins.random()
        val updatedSkin = skinRepository.save(
            toUpdateSkin.copy(
                texture = "RightValue",
                skinUrl = "RightUrl",
                username = "RightUsername"
            )
        )
        val databaseSkin = skinRepository.findByUuid(toUpdateSkin.uuid!!)!!

        Assert.assertEquals(updatedSkin, databaseSkin)
    }

    @Test
    fun `get By Ids`() = runBlocking {
        val skins = testUtilsImplementation.createTestSkins(3)
        val selectedSkin = listOf(skins[0], skins[2]).map { it.uuid!! }.toList()
        val databaseSkins = skinRepository.findAllById(selectedSkin).toList()
        Assert.assertEquals(2, databaseSkins.count())
        Assert.assertEquals(skins[0], databaseSkins[0])
        Assert.assertEquals(skins[2], databaseSkins[1])
    }

    @Test
    fun `get all`() = runBlocking {
        val count = 3
        val skins = testUtilsImplementation.createTestSkins(count)
        val databaseSkins = skinRepository.findAll().toList()
        Assert.assertEquals(9, databaseSkins.count())
        repeat(count) {
            Assert.assertEquals(databaseSkins[it], skins[it])
        }

    }

}