import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.5.5"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("com.github.johnrengelman.processes") version "0.5.0"
    id("org.springdoc.openapi-gradle-plugin") version "1.3.1"

    kotlin("jvm") version "1.4.31"
    kotlin("plugin.spring") version "1.4.31"
}

group = "dev.themeinerlp"
version = "1.0.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }

}

repositories {
    mavenCentral()
}

dependencies {
    // Database Support
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb:2.5.5")
    // Web Support
    implementation("org.springframework.boot:spring-boot-starter-web:2.5.5")
    // Json Support
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.5")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.5.2-native-mt")
    // Session Support
    implementation("org.springframework.session:spring-session-core:2.5.2")
    // Http Client
    implementation("org.apache.httpcomponents:httpclient:4.5.13")
    // IP Bucket
    implementation("com.github.vladimir-bukhtoyarov:bucket4j-core:6.2.0")
    // Swagger Docs
    implementation("org.springdoc:springdoc-openapi-webmvc-core:1.5.10")
    implementation("org.springdoc:springdoc-openapi-ui:1.5.10")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor:2.5.5")

    //testImplementation("org.springframework.boot:spring-boot-starter-test")
    //testImplementation("io.projectreactor:reactor-test")
}
tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootBuildImage> {
    imageName = "ghcr.io/${System.getenv("repository") ?: "skinserver".toLowerCase()}/${project.name.toLowerCase()}:${project.version}"
    docker {
        publishRegistry {
            username = System.getenv("username") ?: null
            password = System.getenv("password") ?: null
            url = "https://ghcr.io/v1/"
        }
    }
    isPublish = true
}