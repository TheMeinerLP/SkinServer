import org.jetbrains.changelog.date

plugins {
    id("org.springframework.boot") version "2.5.5"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("com.github.johnrengelman.processes") version "0.5.0"
    id("org.springdoc.openapi-gradle-plugin") version "1.3.3"
    id("org.openapi.generator") version "5.3.0"
    id("org.hidetake.swagger.generator") version "2.18.2"
    id("org.jetbrains.changelog") version "1.3.1"


    kotlin("jvm") version "1.5.31"
    kotlin("plugin.spring") version "1.5.31"
    kotlin("kapt") version "1.4.32"

}

group = "dev.themeinerlp"
version = "1.0.3"
java.sourceCompatibility = JavaVersion.VERSION_1_8
java.targetCompatibility = JavaVersion.VERSION_1_8

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }

}

repositories {
    mavenCentral()
}

dependencies {
    // Boostrap
    implementation("org.springframework.cloud:spring-cloud-starter-bootstrap:3.0.4")
    // Database Support
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb:2.5.6")
    // Web Support
    implementation("org.springframework.boot:spring-boot-starter-web:2.5.6")
    implementation("org.springframework.boot:spring-boot-starter-actuator:2.5.6")
    // Kubernetes
    implementation("org.springframework.cloud:spring-cloud-starter-kubernetes-client-config:2.0.4")
    // Json Support
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.5.2-native-mt")
    // Session Support
    implementation("org.springframework.session:spring-session-core:2.5.3")
    // Http Client
    implementation("org.apache.httpcomponents:httpclient:4.5.13")
    // IP Bucket
    implementation("com.github.vladimir-bukhtoyarov:bucket4j-core:6.3.0")
    // Swagger Docs
    implementation("org.springdoc:springdoc-openapi-ui:1.5.12")
    implementation("org.springdoc:springdoc-openapi-kotlin:1.5.12")

    kapt("org.springframework.boot:spring-boot-configuration-processor:2.5.6")

    swaggerUI("org.webjars:swagger-ui:3.52.5")
    swaggerCodegen("io.swagger.codegen.v3:swagger-codegen-cli:3.0.28")

    // Unit Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    // Test Containers
    testImplementation("org.testcontainers:testcontainers-bom:1.16.2")
    testImplementation("org.testcontainers:junit-jupiter:1.16.2")
    testApi("org.testcontainers:mongodb:1.16.2")

}



// Tasks
tasks {
    compileKotlin {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "1.8"
        }
    }
    bootBuildImage {
        imageName = if (System.getenv("repository") != null) {
            "ghcr.io/${System.getenv("repository").toLowerCase()}/${project.name.toLowerCase()}:${project.version}"
        } else {
            "ghcr.io/skinserver/${project.name.toLowerCase()}:${project.version}"
        }

        docker {
            publishRegistry {
                username = System.getenv("username") ?: null
                password = System.getenv("password") ?: null
                url = "https://ghcr.io/v1/"
            }
        }
        isPublish = true
    }
    test {
        useJUnitPlatform()
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
