import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    java
    // Плагин Allure: подключает адаптер для JUnit5 автоматически (по classpath),
    // настраивает директорию build/allure-results и добавляет задачи
    // allureReport / allureServe для генерации и просмотра отчёта.
    id("io.qameta.allure") version "4.1.0"
}

group = "com.satellite.autotests"
version = "1.0.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

// Позволяет Allure подставлять реальные имена параметров (а не arg0, arg1...)
// в тексты шагов @Step, например {constellationName} в SatelliteSteps.
tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
}

repositories {
    mavenCentral()
}

val junitBomVersion = "5.11.4"
val restAssuredVersion = "5.5.7"
val allureRestAssuredVersion = "2.31.0"
val jacksonVersion = "2.18.2"
val logbackVersion = "1.5.37"
val slf4jVersion = "2.0.16"

dependencies {
    // ---- JUnit 5 ----
    testImplementation(platform("org.junit:junit-bom:$junitBomVersion"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    // Требуется Gradle для автоматической регистрации слушателей (в т.ч. Allure) через ServiceLoader
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // ---- REST Assured: HTTP-клиент для тестирования REST API ----
    testImplementation(platform("io.rest-assured:rest-assured-bom:$restAssuredVersion"))
    testImplementation("io.rest-assured:rest-assured")
    testImplementation("io.rest-assured:json-path")

    // ---- Allure: прикрепляет запрос/ответ каждого HTTP-вызова к отчёту.
    // Адаптер allure-jupiter (слушатель для JUnit5) плагин io.qameta.allure
    // добавляет самостоятельно, обнаружив junit-jupiter на classpath. ----
    testImplementation("io.qameta.allure:allure-rest-assured:$allureRestAssuredVersion")

    // ---- Jackson: сериализация DTO запросов в JSON ----
    testImplementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")

    // ---- Логирование (SLF4J + Logback) ----
    testImplementation("org.slf4j:slf4j-api:$slf4jVersion")
    testRuntimeOnly("ch.qos.logback:logback-classic:$logbackVersion")

    testImplementation("org.hamcrest:hamcrest:2.2")
}

tasks.test {
    useJUnitPlatform()

    // Базовый URL тестируемого приложения можно переопределить флагом Gradle:
    //   ./gradlew test -Dbase.url=http://localhost:9090
    // Приоритет разбирается в autotests.config.TestConfig:
    //   -Dbase.url  >  переменная окружения BASE_URL  >  config.properties  >  http://localhost:8080
    systemProperty("base.url", System.getProperty("base.url", ""))

    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = TestExceptionFormat.SHORT
        showStandardStreams = false
        showExceptions = true
        showCauses = true
    }

    // Тесты создают свои данные через API и не должны выполняться параллельно
    // друг с другом в рамках одного запуска — упрощает диагностику падений.
    maxParallelForks = 1
}
