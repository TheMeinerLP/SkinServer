package dev.themeinerlp.skinserver.handler.skin.integration

import dev.themeinerlp.skinserver.TestBase
import dev.themeinerlp.skinserver.spec.dao.skin.Skin
import dev.themeinerlp.skinserver.spec.repository.skin.SkinRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ResourceLoader
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.get
import org.springframework.transaction.annotation.Transactional
import java.nio.file.Files
import java.util.Base64

@Transactional
class SkinHandlerTest : TestBase() {

    @Autowired
    lateinit var skinRepository: SkinRepository

    @Autowired
    lateinit var resourceLoader: ResourceLoader

    fun createTestData(): Skin {
        return skinRepository.save(
            Skin(
                username = "fakeuser",
                skinUrl = "fakeurl",
                texture = Base64.getEncoder()
                    .encodeToString(Files.readAllBytes(resourceLoader.getResource("classpath:fakeuser.png").file.toPath()))
            )
        )
    }

    @Test
    fun `get Skin by Username`() {
        createTestData()
        mockMvc.get("/skin/username/64/fakeuser/") {
            accept(MediaType.IMAGE_PNG)
            contentType = MediaType.IMAGE_PNG
        }.andExpect {
            status {
                isOk()
            }
        }
    }

    @Test
    fun `get Skin by UUID`() {
        val uuid = createTestData().uuid
        mockMvc.get("/skin/uuid/64/$uuid/") {
            accept(MediaType.IMAGE_PNG)
            contentType = MediaType.IMAGE_PNG
        }.andExpect {
            status {
                isOk()
            }
        }
    }
}