package autotests.tests;

import autotests.base.BaseApiTest;
import autotests.dto.AddSatelliteRequest;
import autotests.dto.CommunicationSatelliteParam;
import autotests.dto.ImagingSatelliteParam;
import autotests.dto.MissionRequest;
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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Тесты для {@code seminars.controller.SpaceOperationController}
 * (базовый путь {@code /api}) — фасадное API управления группировками:
 * добавление спутников, выполнение миссий, полное развёртывание,
 * сводки и вывод из эксплуатации.
 */
@Epic("Satellite Management System API")
@Feature("Space Operation Center")
class SpaceOperationApiTests extends BaseApiTest {

    // ------------------------------------------------------------------
    // POST /api/add-satellites
    // ------------------------------------------------------------------

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @Story("Добавление спутников")
    @DisplayName("POST /api/add-satellites — создаёт группировку и добавляет в неё спутники")
    @Description("Позитивный сценарий: новая группировка + спутник связи и спутник ДЗЗ одним запросом. " +
            "Ожидается 200 OK и массив с именами добавленных спутников.")
    void addSatellites_newConstellation_returnsAddedSatelliteNames() {
        String constellationName = TestDataGenerator.uniqueConstellationName("Orbit-Add");
        String commName = TestDataGenerator.uniqueSatelliteName("Comm");
        String imgName = TestDataGenerator.uniqueSatelliteName("Img");

        AddSatelliteRequest request = new AddSatelliteRequest(constellationName, List.of(
                new CommunicationSatelliteParam(commName, 0.85, 500.0),
                new ImagingSatelliteParam(imgName, 0.92, 2.5)
        ));

        apiRequest()
                .body(request)
        .when()
                .post("/api/add-satellites")
        .then()
                .statusCode(200)
                .body("size()", is(2))
                .body("$", hasItem(commName))
                .body("$", hasItem(imgName));
    }

    // ------------------------------------------------------------------
    // POST /api/missions
    // ------------------------------------------------------------------

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @Story("Выполнение миссий")
    @DisplayName("POST /api/missions — выполняет миссию для существующей группировки")
    @Description("Позитивный сценарий: группировка с одним спутником связи, миссия с " +
            "activateBeforeMission=true. Ожидается 200 OK.")
    void executeMission_existingConstellation_returnsOk() {
        String constellationName = TestDataGenerator.uniqueConstellationName("Orbit-Mission");
        SatelliteSteps.createConstellationWithCommunicationSatellite(
                constellationName, TestDataGenerator.uniqueSatelliteName("Comm"), 0.8, 500.0);

        MissionRequest missionRequest = new MissionRequest(constellationName, true);

        apiRequest()
                .body(missionRequest)
        .when()
                .post("/api/missions")
        .then()
                .statusCode(200);
    }

    // ------------------------------------------------------------------
    // POST /api/deploy
    // ------------------------------------------------------------------

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @Story("Развёртывание группировки")
    @DisplayName("POST /api/deploy — полный цикл: создание + активация + выполнение миссий")
    @Description("Позитивный сценарий: новая группировка со спутником ДЗЗ разворачивается одним вызовом. " +
            "Ожидается 200 OK и массив с именем развёрнутого спутника.")
    void deployConstellation_newConstellation_returnsDeployedSatelliteNames() {
        String constellationName = TestDataGenerator.uniqueConstellationName("Orbit-Deploy");
        String satelliteName = TestDataGenerator.uniqueSatelliteName("Img");

        AddSatelliteRequest request = new AddSatelliteRequest(constellationName,
                List.of(new ImagingSatelliteParam(satelliteName, 0.9, 1.5)));

        apiRequest()
                .body(request)
        .when()
                .post("/api/deploy")
        .then()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0]", equalTo(satelliteName));
    }

    // ------------------------------------------------------------------
    // GET /api/overview
    // ------------------------------------------------------------------

    @Test
    @Severity(SeverityLevel.NORMAL)
    @Story("Сводная информация")
    @DisplayName("GET /api/overview — возвращает сводку по всем группировкам")
    @Description("Позитивный сценарий: эндпоинт всегда отвечает 200 и содержит поля totalConstellations " +
            "и constellations, независимо от того, сколько группировок уже создано.")
    void getOverview_returnsSystemSummary() {
        apiRequest()
        .when()
                .get("/api/overview")
        .then()
                .statusCode(200)
                .body("totalConstellations", notNullValue())
                .body("constellations", notNullValue());
    }

    // ------------------------------------------------------------------
    // GET /api/constellations/{constellationName}/report
    // ------------------------------------------------------------------

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @Story("Отчёт по группировке")
    @DisplayName("GET /api/constellations/{name}/report — возвращает отчёт по существующей группировке")
    @Description("Позитивный сценарий: группировка с одним (ещё не активным) спутником связи. " +
            "Ожидается totalSatellites=1, activeSatellites=0.")
    void getConstellationReport_existingConstellation_returnsReport() {
        String constellationName = TestDataGenerator.uniqueConstellationName("Orbit-Report");
        SatelliteSteps.createConstellationWithCommunicationSatellite(
                constellationName, TestDataGenerator.uniqueSatelliteName("Comm"), 0.7, 400.0);

        apiRequest()
        .when()
                .get("/api/constellations/{name}/report", constellationName)
        .then()
                .statusCode(200)
                .body("constellationName", equalTo(constellationName))
                .body("totalSatellites", equalTo(1))
                .body("activeSatellites", equalTo(0));
    }

    @Test
    @Severity(SeverityLevel.MINOR)
    @Story("Отчёт по группировке")
    @DisplayName("[Негативный] GET /api/constellations/{name}/report — 422 для несуществующей группировки")
    void getConstellationReport_unknownConstellation_returns422() {
        String unknownName = TestDataGenerator.uniqueConstellationName("Ghost-Orbit");

        apiRequest()
        .when()
                .get("/api/constellations/{name}/report", unknownName)
        .then()
                .statusCode(422);
    }

    // ------------------------------------------------------------------
    // DELETE /api/constellations/{constellationName}/satellites/{satelliteName}
    // ------------------------------------------------------------------

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @Story("Вывод спутника из эксплуатации")
    @DisplayName("DELETE /api/constellations/{name}/satellites/{satName} — деактивирует существующий спутник")
    @Description("Позитивный сценарий: создаём спутник, затем деактивируем его через decommission-эндпоинт. " +
            "Ожидается 200 OK и текстовое подтверждение деактивации.")
    void decommissionSatellite_existingSatellite_deactivatesAndReturns200() {
        String constellationName = TestDataGenerator.uniqueConstellationName("Orbit-Decom");
        String satelliteName = TestDataGenerator.uniqueSatelliteName("Comm");
        SatelliteSteps.createConstellationWithCommunicationSatellite(
                constellationName, satelliteName, 0.8, 500.0);

        apiRequest()
        .when()
                .delete("/api/constellations/{con}/satellites/{sat}", constellationName, satelliteName)
        .then()
                .statusCode(200)
                .body(containsString("деактивирован"));
    }

    @Test
    @Severity(SeverityLevel.MINOR)
    @Story("Вывод спутника из эксплуатации")
    @DisplayName("[Негативный] DELETE /api/constellations/{name}/satellites/{satName} — 404 для несуществующего спутника")
    void decommissionSatellite_unknownSatellite_returns404() {
        String unknownConstellation = TestDataGenerator.uniqueConstellationName("Ghost-Orbit");

        apiRequest()
        .when()
                .delete("/api/constellations/{con}/satellites/{sat}", unknownConstellation, "НесуществующийСпутник")
        .then()
                .statusCode(404);
    }
}
