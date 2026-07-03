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
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Тесты для {@code seminars.controller.ConstellationController}
 * (базовый путь {@code /api/constellations}) — прямой CRUD над группировками
 * через JPA-репозиторий, в обход фасада.
 */
@Epic("Satellite Management System API")
@Feature("Constellations CRUD")
class ConstellationApiTests extends BaseApiTest {

    // ------------------------------------------------------------------
    // GET /api/constellations
    // ------------------------------------------------------------------

    @Test
    @Severity(SeverityLevel.NORMAL)
    @Story("Получение всех группировок")
    @DisplayName("GET /api/constellations — возвращает список всех группировок")
    @Description("Позитивный сценарий: сначала создаём заведомо существующую группировку, " +
            "затем проверяем, что общий список группировок непустой и это массив.")
    void getAll_returnsListContainingCreatedConstellation() {
        String constellationName = TestDataGenerator.uniqueConstellationName("Orbit-List");
        createConstellation(constellationName);

        apiRequest()
        .when()
                .get("/api/constellations")
        .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(1))
                .body("constellationName", hasItem(constellationName));
    }

    // ------------------------------------------------------------------
    // GET /api/constellations/{name}
    // ------------------------------------------------------------------

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @Story("Получение группировки по имени")
    @DisplayName("GET /api/constellations/{name} — возвращает группировку вместе со спутниками")
    @Description("Позитивный сценарий: группировка с одним спутником связи. Ожидается 200 OK, " +
            "совпадение имени и один элемент в списке satellites.")
    void getByName_existingConstellation_returnsConstellationWithSatellites() {
        String constellationName = TestDataGenerator.uniqueConstellationName("Orbit-ByName");
        SatelliteSteps.createConstellationWithCommunicationSatellite(
                constellationName, TestDataGenerator.uniqueSatelliteName("Comm"), 0.8, 300.0);

        apiRequest()
        .when()
                .get("/api/constellations/{name}", constellationName)
        .then()
                .statusCode(200)
                .body("constellationName", equalTo(constellationName))
                .body("satellites", hasSize(1));
    }

    @Test
    @Severity(SeverityLevel.MINOR)
    @Story("Получение группировки по имени")
    @DisplayName("[Негативный] GET /api/constellations/{name} — 404 для несуществующей группировки")
    void getByName_unknownConstellation_returns404() {
        String unknownName = TestDataGenerator.uniqueConstellationName("Ghost-Orbit");

        apiRequest()
        .when()
                .get("/api/constellations/{name}", unknownName)
        .then()
                .statusCode(404);
    }

    // ------------------------------------------------------------------
    // POST /api/constellations?name=...
    // ------------------------------------------------------------------

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @Story("Создание группировки")
    @DisplayName("POST /api/constellations — создаёт новую пустую группировку")
    @Description("Позитивный сценарий: создание группировки с ранее не использовавшимся именем. " +
            "Ожидается 201 Created, присвоенный id и пустой список спутников.")
    void create_newConstellationName_returns201() {
        String constellationName = TestDataGenerator.uniqueConstellationName("Orbit-Create");

        apiRequest()
                .queryParam("name", constellationName)
        .when()
                .post("/api/constellations")
        .then()
                .statusCode(201)
                .body("constellationName", equalTo(constellationName))
                .body("id", notNullValue())
                .body("satellites", hasSize(0));
    }

    @Test
    @Severity(SeverityLevel.MINOR)
    @Story("Создание группировки")
    @DisplayName("[Негативный] POST /api/constellations — 422 при повторном создании с тем же именем")
    void create_duplicateConstellationName_returns422() {
        String constellationName = TestDataGenerator.uniqueConstellationName("Orbit-Dup");
        createConstellation(constellationName);

        apiRequest()
                .queryParam("name", constellationName)
        .when()
                .post("/api/constellations")
        .then()
                .statusCode(422);
    }

    // ------------------------------------------------------------------
    // DELETE /api/constellations/{name}
    // ------------------------------------------------------------------

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @Story("Удаление группировки")
    @DisplayName("DELETE /api/constellations/{name} — удаляет существующую группировку")
    @Description("Позитивный сценарий: группировка создаётся, затем удаляется. Ожидается 204 No Content.")
    void delete_existingConstellation_returns204() {
        String constellationName = TestDataGenerator.uniqueConstellationName("Orbit-Delete");
        createConstellation(constellationName);

        apiRequest()
        .when()
                .delete("/api/constellations/{name}", constellationName)
        .then()
                .statusCode(204);
    }

    @Test
    @Severity(SeverityLevel.MINOR)
    @Story("Удаление группировки")
    @DisplayName("[Негативный] DELETE /api/constellations/{name} — 404 для несуществующей группировки")
    void delete_unknownConstellation_returns404() {
        String unknownName = TestDataGenerator.uniqueConstellationName("Ghost-Orbit");

        apiRequest()
        .when()
                .delete("/api/constellations/{name}", unknownName)
        .then()
                .statusCode(404);
    }

    // ------------------------------------------------------------------

    private void createConstellation(String name) {
        apiRequest()
                .queryParam("name", name)
        .when()
                .post("/api/constellations")
        .then()
                .statusCode(201);
    }
}
