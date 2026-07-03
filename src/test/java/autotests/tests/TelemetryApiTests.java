package autotests.tests;

import autotests.base.BaseApiTest;
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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

/**
 * Тесты для {@code seminars.controller.TelemetryController}
 * (базовый путь {@code /api/telemetry}) — данные телеметрии, обновляемые
 * через gRPC-стрим от сервиса satellite-telemetry.
 * <p>
 * В тестовом окружении сервис satellite-telemetry обычно не запущен —
 * это НЕ мешает позитивным сценариям: контроллер отдаёт последние известные
 * значения из PostgreSQL (или {@code null} / "NO_DATA", если телеметрии ещё
 * не было), а не обращается к gRPC напрямую.
 */
@Epic("Satellite Management System API")
@Feature("Telemetry")
class TelemetryApiTests extends BaseApiTest {

    // ------------------------------------------------------------------
    // GET /api/telemetry
    // ------------------------------------------------------------------

    @Test
    @Severity(SeverityLevel.NORMAL)
    @Story("Телеметрия всех спутников")
    @DisplayName("GET /api/telemetry — возвращает телеметрию по всем спутникам")
    @Description("Позитивный сценарий: эндпоинт отвечает 200 OK и JSON-массивом независимо от того, " +
            "поступали ли уже данные телеметрии по gRPC.")
    void getAllTelemetry_returnsArray() {
        apiRequest()
        .when()
                .get("/api/telemetry")
        .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(0));
    }

    // ------------------------------------------------------------------
    // GET /api/telemetry/{satelliteId}
    // ------------------------------------------------------------------

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @Story("Телеметрия конкретного спутника")
    @DisplayName("GET /api/telemetry/{id} — возвращает телеметрию существующего спутника")
    @Description("Позитивный сценарий: создаём спутник, узнаём его ID через /api/satellites/by-name, " +
            "затем запрашиваем его телеметрию. Ожидается 200 OK, совпадение id/name и заполненное " +
            "поле temperatureStatus (NO_DATA, пока gRPC-стрим ничего не прислал).")
    void getTelemetry_existingSatellite_returnsTelemetryView() {
        String constellationName = TestDataGenerator.uniqueConstellationName("Orbit-Telemetry");
        String satelliteName = TestDataGenerator.uniqueSatelliteName("Comm");
        SatelliteSteps.createConstellationWithCommunicationSatellite(
                constellationName, satelliteName, 0.8, 500.0);

        long satelliteId = SatelliteSteps.getSatelliteIdByName(satelliteName);

        apiRequest()
        .when()
                .get("/api/telemetry/{id}", satelliteId)
        .then()
                .statusCode(200)
                .body("id", equalTo((int) satelliteId))
                .body("name", equalTo(satelliteName))
                .body("temperatureStatus", equalTo("NO_DATA"));
    }

    @Test
    @Severity(SeverityLevel.MINOR)
    @Story("Телеметрия конкретного спутника")
    @DisplayName("[Негативный] GET /api/telemetry/{id} — 404 для несуществующего ID")
    void getTelemetry_nonExistentId_returns404() {
        apiRequest()
        .when()
                .get("/api/telemetry/{id}", 999_999_999L)
        .then()
                .statusCode(404);
    }
}
