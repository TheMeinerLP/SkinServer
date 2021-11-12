package dev.themeinerlp.skinserver.handler.render.integration

import com.fasterxml.jackson.databind.ObjectMapper
import dev.themeinerlp.skinserver.TestBase
import dev.themeinerlp.skinserver.spec.dao.skin.Skin
import dev.themeinerlp.skinserver.spec.repository.skin.SkinRepository
import dev.themeinerlp.skinserver.utils.HeadView
import java.nio.file.Files
import java.util.Base64
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ResourceLoader
import org.springframework.http.MediaType
import org.springframework.mock.web.MockPart
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@Transactional
class RenderHandlerTest : TestBase() {

    @Autowired
    lateinit var skinRepository: SkinRepository

    @Autowired
    lateinit var resourceLoader: ResourceLoader

    @Autowired
    private lateinit var objectMapper: ObjectMapper

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
    fun `render Head`() {
        val head = MockPart(
            "skin",
            Files.readAllBytes(resourceLoader.getResource("classpath:fakeuser.png").file.toPath())
        ).apply {
            headers.contentType = MediaType.IMAGE_JPEG
        }
        mockMvc.perform(multipart("/render/64/").part(head)).andExpect(status().`is`(200))
    }

    @ParameterizedTest
    @ValueSource(ints = [16, 32, 64, 128, 256, 512])
    fun `render Head different sizes`(size: Int) {
        val head = MockPart(
            "skin",
            Files.readAllBytes(resourceLoader.getResource("classpath:fakeuser.png").file.toPath())
        ).apply {
            headers.contentType = MediaType.IMAGE_JPEG
        }
        mockMvc.perform(multipart("/render/$size/").part(head)).andExpect(status().`is`(200))
    }

    @ParameterizedTest
    @EnumSource(HeadView::class)
    fun `render Head different rotations`(rotation: HeadView) {
        val head = MockPart(
            "skin",
            Files.readAllBytes(resourceLoader.getResource("classpath:fakeuser.png").file.toPath())
        ).apply {
            headers.contentType = MediaType.IMAGE_JPEG
        }
        mockMvc.perform(multipart("/render/64/$rotation/").part(head)).andExpect(status().`is`(200))
    }

}