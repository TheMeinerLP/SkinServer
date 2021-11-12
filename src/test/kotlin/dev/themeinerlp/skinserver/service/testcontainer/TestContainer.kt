package dev.themeinerlp.skinserver.service.testcontainer

import java.lang.String.format
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.support.TestPropertySourceUtils
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.utility.DockerImageName

class TestContainer : ApplicationContextInitializer<ConfigurableApplicationContext> {

    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        val mongoDBContainer = MongoDBContainer(DockerImageName.parse("mongo:latest"))
        mongoDBContainer.start()
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
            applicationContext,
            format(
                "spring.data.mongodb.uri=mongodb://%s:%s",
                mongoDBContainer.containerIpAddress,
                mongoDBContainer.getMappedPort(27017)
            )
        )
    }
}