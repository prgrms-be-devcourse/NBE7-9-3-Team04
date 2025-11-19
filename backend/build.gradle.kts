plugins {
    java
    jacoco
    id("org.springframework.boot") version "3.5.6"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.sonarqube") version "5.1.0.4882"
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    kotlin("plugin.jpa") version "1.9.25"
    kotlin("kapt") version "1.9.25"
}

group = "com"
version = "0.0.1-SNAPSHOT"
description = "backend"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    testImplementation("org.springframework.security:spring-security-test")
    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    testImplementation("com.h2database:h2")
    runtimeOnly("com.mysql:mysql-connector-j")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.13")
    implementation ("org.springframework.boot:spring-boot-starter-mail")
    implementation("com.github.napstr:logback-discord-appender:1.0.0")
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")
    implementation("org.springframework.ai:spring-ai-starter-model-openai")
    implementation("org.springframework.ai:spring-ai-starter-model-vertex-ai-gemini")
    implementation("com.mailgun:mailgun-java:1.0.1")

    implementation("org.springframework.boot:spring-boot-starter-webflux")

    implementation("io.github.resilience4j:resilience4j-spring-boot3:2.3.0")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    //querydsl
    kapt("io.github.openfeign.querydsl:querydsl-apt:7.1:jpa")
    implementation("io.github.openfeign.querydsl:querydsl-jpa:7.1")

    //Elasticsearch
    implementation("org.springframework.boot:spring-boot-starter-data-elasticsearch")

    //redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    testImplementation("com.github.codemonstur:embedded-redis:1.4.3")
    implementation("org.redisson:redisson-spring-boot-starter:3.52.0")

    //kotlin
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

}
extra["springAiVersion"] = "1.1.0-M1"

dependencyManagement {
    imports {
        mavenBom("org.springframework.ai:spring-ai-bom:${property("springAiVersion")}")
    }
}


kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

kapt {
    correctErrorTypes = true
    includeCompileClasspath = false
}

tasks.withType<Test> {
    useJUnitPlatform()
}

kapt {
    correctErrorTypes = true
    includeCompileClasspath = false
}

jacoco {
    toolVersion = "0.8.10"
}
tasks.jacocoTestReport {
    dependsOn(tasks.test)

    reports {
        xml.required.set(true)   // SonarQube가 필요로 함
        html.required.set(true)  // 사람이 보기 쉬운 리포트
    }
}

sonarqube {
    properties {
        property("sonar.projectKey", "backend_project")
        property("sonar.projectName", "Backend Kotlin Project")
        property("sonar.projectVersion", "1.0")

        property("sonar.host.url", "http://localhost:9000")
        property("sonar.token", System.getenv("SONAR_TOKEN") ?: "")

        property("sonar.sources", "src/main/java,src/main/resources")
        property("sonar.tests", "src/test/java,src/test/resources")

        property("sonar.java.coveragePlugin", "jacoco")
        property("sonar.coverage.jacoco.xmlReportPaths", "build/reports/jacoco/test/jacocoTestReport.xml")

        property("sonar.verbose", "true")
    }
}