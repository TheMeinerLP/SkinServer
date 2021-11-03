import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.5.5"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("com.github.johnrengelman.processes") version "0.5.0"
    id("org.springdoc.openapi-gradle-plugin") version "1.3.3"
    id("org.openapi.generator") version "5.3.0"

    kotlin("jvm") version "1.5.31"
    kotlin("plugin.spring") version "1.5.31"
    kotlin("kapt") version "1.4.32"

}

group = "dev.themeinerlp"
version = "1.0.0-SNAPSHOT"
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
    // Database Support
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb:2.5.5")
    // Web Support
    implementation("org.springframework.boot:spring-boot-starter-web:2.5.5")
    // Json Support
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.5.2-native-mt")
    // Session Support
    implementation("org.springframework.session:spring-session-core:2.5.2")
    // Http Client
    implementation("org.apache.httpcomponents:httpclient:4.5.13")
    // IP Bucket
    implementation("com.github.vladimir-bukhtoyarov:bucket4j-core:6.3.0")
    // Swagger Docs
    implementation("org.springdoc:springdoc-openapi-webmvc-core:1.5.11")
    implementation("org.springdoc:springdoc-openapi-ui:1.5.11")
    implementation("org.springdoc:springdoc-openapi-kotlin:1.5.11")
    implementation("org.springdoc:springdoc-openapi-javadoc:1.5.11")
    implementation("org.springdoc:springdoc-openapi-data-rest:1.5.11")

    kapt("org.springframework.boot:spring-boot-configuration-processor:2.5.5")

    //testImplementation("org.springframework.boot:spring-boot-starter-test")
    //testImplementation("io.projectreactor:reactor-test")
}
tasks.withType<KotlinCompile> {
    dependsOn(tasks.withType(ProcessResources::class.java))
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}

tasks {
    openApiGenerate {
        generatorName.set("dynamic-html")
        inputSpec.set("$buildDir/openapi.json")
        outputDir.set("$buildDir/docs")
        apiPackage.set("org.openapi.example.api")
        invokerPackage.set("org.openapi.example.invoker")
        modelPackage.set("org.openapi.example.model")
        configOptions.put("dateLibrary", "java8")
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