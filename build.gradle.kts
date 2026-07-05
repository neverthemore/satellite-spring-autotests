import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    java
    id("io.qameta.allure") version "4.1.0"
}

group = "com.satellite.autotests"
version = "1.0.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}

repositories {
    mavenCentral()
}

val allureResultsDir = layout.buildDirectory.dir("allure-results")

allure {
    adapter {
        resultsDir.set(allureResultsDir)
    }
}

val junitBomVersion = "5.11.4"
val restAssuredVersion = "5.5.7"
val allureVersion = "2.31.0"
val jacksonVersion = "2.18.2"
val logbackVersion = "1.5.37"
val slf4jVersion = "2.0.16"

dependencies {
    testImplementation(platform("org.junit:junit-bom:$junitBomVersion"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    testImplementation(platform("io.rest-assured:rest-assured-bom:$restAssuredVersion"))
    testImplementation("io.rest-assured:rest-assured")
    testImplementation("io.rest-assured:json-path")

    testImplementation(platform("io.qameta.allure:allure-bom:$allureVersion"))
    testImplementation("io.qameta.allure:allure-jupiter")
    testImplementation("io.qameta.allure:allure-rest-assured")

    testImplementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")

    testImplementation("org.slf4j:slf4j-api:$slf4jVersion")
    testRuntimeOnly("ch.qos.logback:logback-classic:$logbackVersion")

    testImplementation("org.hamcrest:hamcrest:2.2")
}

tasks.test {
    useJUnitPlatform()
    systemProperty("base.url", System.getProperty("base.url", ""))
    systemProperty("allure.results.directory", allureResultsDir.get().asFile.absolutePath)

    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = TestExceptionFormat.SHORT
        showStandardStreams = false
        showExceptions = true
        showCauses = true
    }

    maxParallelForks = 1
}