package autotests.steps;

import autotests.dto.AddSatelliteRequest;
import autotests.dto.CommunicationSatelliteParam;
import autotests.dto.ImagingSatelliteParam;
import io.qameta.allure.Step;
import io.restassured.http.ContentType;

import java.util.List;

import static io.restassured.RestAssured.given;

/**
 * Переиспользуемые составные шаги над API, общие для нескольких тестовых
 * классов (аналог Screenplay/Steps-паттерна). Каждый шаг размечен
 * {@code @Step}, поэтому в отчёте Allure виден не только сырой HTTP-вызов
 * (это добавляет {@code AllureRestAssured}), но и его бизнес-смысл.
 * <p>
 * Методы используют {@code RestAssured.given()} напрямую и полагаются на то,
 * что {@code RestAssured.baseURI} и фильтр Allure уже сконфигурированы
 * статическим блоком {@link autotests.base.BaseApiTest}.
 */
public final class SatelliteSteps {

    private SatelliteSteps() {
    }

    @Step("Создать группировку '{constellationName}' со спутником связи '{satelliteName}'")
    public static void createConstellationWithCommunicationSatellite(
            String constellationName, String satelliteName, double batteryLevel, double bandwidth) {
        AddSatelliteRequest request = new AddSatelliteRequest(constellationName,
                List.of(new CommunicationSatelliteParam(satelliteName, batteryLevel, bandwidth)));

        given()
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .post("/api/add-satellites")
        .then()
                .statusCode(200);
    }

    @Step("Создать группировку '{constellationName}' со спутником ДЗЗ '{satelliteName}'")
    public static void createConstellationWithImagingSatellite(
            String constellationName, String satelliteName, double batteryLevel, double resolution) {
        AddSatelliteRequest request = new AddSatelliteRequest(constellationName,
                List.of(new ImagingSatelliteParam(satelliteName, batteryLevel, resolution)));

        given()
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .post("/api/add-satellites")
        .then()
                .statusCode(200);
    }

    @Step("Развернуть группировку '{constellationName}' со спутником связи '{satelliteName}' (создание + активация + миссия)")
    public static void deployConstellationWithCommunicationSatellite(
            String constellationName, String satelliteName, double batteryLevel, double bandwidth) {
        AddSatelliteRequest request = new AddSatelliteRequest(constellationName,
                List.of(new CommunicationSatelliteParam(satelliteName, batteryLevel, bandwidth)));

        given()
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .post("/api/deploy")
        .then()
                .statusCode(200);
    }

    @Step("Получить ID спутника по имени '{satelliteName}'")
    public static long getSatelliteIdByName(String satelliteName) {
        return given()
                .queryParam("name", satelliteName)
        .when()
                .get("/api/satellites/by-name")
        .then()
                .statusCode(200)
                .extract().jsonPath().getLong("id");
    }
}
