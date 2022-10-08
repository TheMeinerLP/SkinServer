import org.jetbrains.changelog.date

plugins {
    id("org.springframework.boot") version "2.7.4"
    id("io.spring.dependency-management") version "1.0.14.RELEASE"
    id("com.github.johnrengelman.processes") version "0.5.0"
    id("org.springdoc.openapi-gradle-plugin") version "1.4.0"
    id("org.openapi.generator") version "6.2.0"
    id("org.hidetake.swagger.generator") version "2.19.2"
    id("org.jetbrains.changelog") version "1.3.1"

    kotlin("jvm") version "1.7.20"
    kotlin("plugin.spring") version "1.7.20"
    kotlin("kapt") version "1.7.20"
}

repositories {
    mavenCentral()
}

group = "dev.themeinerlp"
version = "1.0.5-SNAPSHOT"

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}



dependencies {
    implementation(libs.bundles.springBoot)
    implementation(libs.bundles.springDocOpenApi)
    // Json Support
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.6.3-native-mt")
    // Http Client
    implementation("org.apache.httpcomponents:httpclient:4.5.13")
    // IP Bucket
    implementation("com.github.vladimir-bukhtoyarov:bucket4j-core:7.6.0")

    kapt("org.springframework.boot:spring-boot-configuration-processor:2.7.4")

    swaggerUI("org.webjars:swagger-ui:4.14.2")
    swaggerCodegen("io.swagger.codegen.v3:swagger-codegen-cli:3.0.35")
}



// Tasks
tasks {
    compileKotlin {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "17"
        }
    }

    test {
        useJUnitPlatform()
    }
    bootBuildImage {
        val repo = System.getenv("repository") ?: "skinserver"
        imageName = "ghcr.io/${repo.toLowerCase()}/${project.name.toLowerCase()}:${project.version}"
        docker {
            publishRegistry {
                username = System.getenv("username") ?: null
                password = System.getenv("password") ?: null
                url = "https://ghcr.io/v1/"
            }
        }
        isPublish = true
    }
}

changelog {
    header.set(provider { "[${version.get()}] - ${date()}" })
    keepUnreleasedSection.set(true)
}

swaggerSources {
    create("SkinServer") {
        setInputFile(File("$buildDir/openapi.json"))
    }
}
