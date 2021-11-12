package dev.themeinerlp.skinserver

import dev.themeinerlp.skinserver.service.testcontainer.TestContainer
import dev.themeinerlp.skinserver.utils.TestUtilsImplementation
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc


@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [SkinServerApplication::class])
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(initializers = [TestContainer::class])
abstract class TestBase {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var testUtilsImplementation: TestUtilsImplementation

}