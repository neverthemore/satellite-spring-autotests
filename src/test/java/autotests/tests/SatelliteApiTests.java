package autotests.tests;

import autotests.base.BaseApiTest;
import autotests.dto.AddSatelliteRequest;
import autotests.dto.CommunicationSatelliteParam;
import autotests.dto.ImagingSatelliteParam;
import autotests.steps.SatelliteSteps;
import autotests.utils.TestDataGenerator;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;

/**
 * Тесты для {@code seminars.controller.SatelliteController}
 * (базовый путь {@code /api/satellites}) — точечный доступ к отдельным
 * спутникам без загрузки всей группировки.
 */
@Epic("Satellite Management System API")
@Feature("Satellites CRUD")
class SatelliteApiTests extends BaseApiTest {

    // ------------------------------------------------------------------
    // GET /api/satellites
    // ------------------------------------------------------------------

    @Test
    @Severity(SeverityLevel.NORMAL)
    @Story("Получение всех спутников")
    @DisplayName("GET /api/satellites — возвращает список всех спутников")
    @Description("Позитивный сценарий: создаём спутник, затем проверяем, что общий список " +
            "спутников непустой и содержит его имя.")
    void getAll_returnsListContainingCreatedSatellite() {
        String constellationName = TestDataGenerator.uniqueConstellationName("Orbit-AllSat");
        String satelliteName = TestDataGenerator.uniqueSatelliteName("Comm");
        SatelliteSteps.createConstellationWithCommunicationSatellite(
                constellationName, satelliteName, 0.8, 300.0);

        apiRequest()
        .when()
                .get("/api/satellites")
        .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(1))
                .body("name", hasItem(satelliteName));
    }

    // ------------------------------------------------------------------
    // GET /api/satellites/{id}
    // ------------------------------------------------------------------

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @Story("Получение спутника по ID")
    @DisplayName("GET /api/satellites/{id} — возвращает спутник по существующему ID")
    @Description("Позитивный сценарий: спутник создаётся, его ID сначала выясняется через " +
            "/api/satellites/by-name, затем спутник запрашивается по этому ID.")
    void getById_existingSatellite_returnsSatellite() {
        String constellationName = TestDataGenerator.uniqueConstellationName("Orbit-ById");
        String satelliteName = TestDataGenerator.uniqueSatelliteName("Img");
        SatelliteSteps.createConstellationWithImagingSatellite(constellationName, satelliteName, 0.9, 2.0);

        long satelliteId = SatelliteSteps.getSatelliteIdByName(satelliteName);

        apiRequest()
        .when()
                .get("/api/satellites/{id}", satelliteId)
        .then()
                .statusCode(200)
                .body("id", equalTo((int) satelliteId))
                .body("name", equalTo(satelliteName));
    }

    @Test
    @Severity(SeverityLevel.MINOR)
    @Story("Получение спутника по ID")
    @DisplayName("[Негативный] GET /api/satellites/{id} — 404 для несуществующего ID")
    void getById_nonExistentId_returns404() {
        apiRequest()
        .when()
                .get("/api/satellites/{id}", 999_999_999L)
        .then()
                .statusCode(404);
    }

    // ------------------------------------------------------------------
    // GET /api/satellites/by-name
    // ------------------------------------------------------------------

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @Story("Поиск спутника по имени")
    @DisplayName("GET /api/satellites/by-name — находит спутник по точному имени")
    @Description("Позитивный сценарий: спутник создаётся и находится по имени. Ожидается 200 OK " +
            "и совпадение всех полей.")
    void getByName_existingSatellite_returnsSatellite() {
        String constellationName = TestDataGenerator.uniqueConstellationName("Orbit-ByName");
        String satelliteName = TestDataGenerator.uniqueSatelliteName("Comm");
        SatelliteSteps.createConstellationWithCommunicationSatellite(
                constellationName, satelliteName, 0.6, 250.0);

        apiRequest()
                .queryParam("name", satelliteName)
        .when()
                .get("/api/satellites/by-name")
        .then()
                .statusCode(200)
                .body("name", equalTo(satelliteName));
    }

    @Test
    @Severity(SeverityLevel.MINOR)
    @Story("Поиск спутника по имени")
    @DisplayName("[Негативный] GET /api/satellites/by-name — 404 для несуществующего имени")
    void getByName_nonExistentName_returns404() {
        String unknownName = TestDataGenerator.uniqueSatelliteName("Ghost");

        apiRequest()
                .queryParam("name", unknownName)
        .when()
                .get("/api/satellites/by-name")
        .then()
                .statusCode(404);
    }

    // ------------------------------------------------------------------
    // GET /api/satellites/active
    // ------------------------------------------------------------------

    @Test
    @Severity(SeverityLevel.NORMAL)
    @Story("Активные спутники")
    @DisplayName("GET /api/satellites/active — возвращает только активные спутники")
    @Description("Позитивный сценарий: спутник разворачивается через /api/deploy (что активирует его " +
            "и выполняет миссию), после чего должен присутствовать в списке активных.")
    void getActive_afterDeploy_containsActivatedSatellite() {
        String constellationName = TestDataGenerator.uniqueConstellationName("Orbit-Active");
        String satelliteName = TestDataGenerator.uniqueSatelliteName("Comm");
        // Батарея заведомо выше порога (0.2) — активация гарантированно успешна
        SatelliteSteps.deployConstellationWithCommunicationSatellite(constellationName, satelliteName, 0.9, 500.0);

        apiRequest()
        .when()
                .get("/api/satellites/active")
        .then()
                .statusCode(200)
                .body("name", hasItem(satelliteName));
    }

    // ------------------------------------------------------------------
    // GET /api/satellites/by-constellation
    // ------------------------------------------------------------------

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @Story("Спутники конкретной группировки")
    @DisplayName("GET /api/satellites/by-constellation — возвращает только спутники указанной группировки")
    @Description("Позитивный сценарий: группировка с двумя спутниками (связь + ДЗЗ). Ожидается " +
            "ровно 2 элемента с соответствующими именами.")
    void getByConstellation_existingConstellation_returnsItsSatellites() {
        String constellationName = TestDataGenerator.uniqueConstellationName("Orbit-ByConst");
        String commName = TestDataGenerator.uniqueSatelliteName("Comm");
        String imgName = TestDataGenerator.uniqueSatelliteName("Img");

        AddSatelliteRequest request = new AddSatelliteRequest(constellationName, List.of(
                new CommunicationSatelliteParam(commName, 0.7, 350.0),
                new ImagingSatelliteParam(imgName, 0.75, 1.8)
        ));
        apiRequest().body(request)
                .when().post("/api/add-satellites")
                .then().statusCode(200);

        apiRequest()
                .queryParam("constellationName", constellationName)
        .when()
                .get("/api/satellites/by-constellation")
        .then()
                .statusCode(200)
                .body("size()", equalTo(2))
                .body("name", hasItem(commName))
                .body("name", hasItem(imgName));
    }
}
