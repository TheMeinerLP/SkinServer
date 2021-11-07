package dev.themeinerlp.skinserver

import dev.themeinerlp.skinserver.service.testcontainer.TestContainer
import dev.themeinerlp.skinserver.utils.TestUtilsImplementation
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner

@SpringBootTest(classes = [SkinServerApplication::class])
@AutoConfigureMockMvc
@ActiveProfiles("test")
@RunWith(SpringRunner::class)
@ContextConfiguration(initializers = [TestContainer::class])
abstract class TestBase {

    @Autowired
    lateinit var testUtilsImplementation: TestUtilsImplementation

}