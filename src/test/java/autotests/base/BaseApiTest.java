package autotests.base;

import autotests.config.TestConfig;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Базовый класс для всех API-тестов.
 * <p>
 * Настройка RestAssured выполняется в статическом блоке инициализации,
 * а не в JUnit-методе {@code @BeforeAll}: по правилам JVM статический блок
 * класса гарантированно выполняется РОВНО ОДИН РАЗ за всё время работы
 * процесса, при первой загрузке этого класса — какой бы из наследников
 * (SatelliteApiTests, ConstellationApiTests, ...) не был запущен первым.
 * <p>
 * Если бы настройка (в т.ч. {@code RestAssured.filters(new AllureRestAssured())})
 * находилась в {@code @BeforeAll}, JUnit5 вызывал бы её отдельно для каждого
 * тестового класса, и фильтр AllureRestAssured добавлялся бы в статический
 * список RestAssured многократно — на выходе каждый HTTP-вызов задваивался/
 * растраивался бы в отчёте Allure.
 */
public abstract class BaseApiTest {

    protected static final Logger log = LoggerFactory.getLogger(BaseApiTest.class);

    static {
        String baseUrl = TestConfig.getBaseUrl();
        RestAssured.baseURI = baseUrl;
        RestAssured.filters(new AllureRestAssured());
        // Полные детали запроса/ответа в консоли — только при упавшей проверке,
        // чтобы не засорять лог при успешном прогоне.
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        log.info("Автотесты API запускаются против: {}", baseUrl);
    }

    /**
     * Готовая к использованию спецификация запроса с базовыми заголовками.
     */
    protected RequestSpecification apiRequest() {
        return RestAssured.given()
                .contentType(ContentType.JSON);
    }
}
