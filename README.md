# satellite-spring-autotests

Автоматизированные API-тесты для проекта [`satellite-spring`](../satellite-spring) —
учебного приложения по курсу системного дизайна (управление спутниковыми
группировками).

Проект **полностью независим** от основного приложения: он не подключает его
код как зависимость и взаимодействует с ним исключительно по HTTP/JSON,
как обычный клиент REST API.

## Стек

| Назначение              | Инструмент                                   |
|--------------------------|-----------------------------------------------|
| Сборщик                  | Gradle 8.12 (Kotlin DSL)                      |
| Тестовый фреймворк       | JUnit 5                                       |
| HTTP-клиент              | REST Assured 5.5.7                            |
| Отчётность               | Allure Report (плагин `io.qameta.allure`)     |
| Сериализация JSON        | Jackson Databind                              |
| Логирование              | SLF4J + Logback                               |
| Java                     | 21                                             |

## Покрытие эндпоинтов

Каждый из 17 REST-эндпоинтов основного приложения вызывается как минимум
в одном позитивном тесте. Дополнительно для части эндпоинтов добавлены
негативные сценарии (404 / 422) — сверх обязательного минимума.

| # | Метод и путь | Контроллер | Тестовый класс | Негативный сценарий |
|---|---|---|:--|:--:|
| 1 | `POST /api/add-satellites` | SpaceOperationController | `SpaceOperationApiTests` | — |
| 2 | `POST /api/missions` | SpaceOperationController | `SpaceOperationApiTests` | — |
| 3 | `POST /api/deploy` | SpaceOperationController | `SpaceOperationApiTests` | — |
| 4 | `GET /api/overview` | SpaceOperationController | `SpaceOperationApiTests` | — |
| 5 | `GET /api/constellations/{name}/report` | SpaceOperationController | `SpaceOperationApiTests` | ✅ 422 |
| 6 | `DELETE /api/constellations/{name}/satellites/{satName}` | SpaceOperationController | `SpaceOperationApiTests` | ✅ 404 |
| 7 | `GET /api/constellations` | ConstellationController | `ConstellationApiTests` | — |
| 8 | `GET /api/constellations/{name}` | ConstellationController | `ConstellationApiTests` | ✅ 404 |
| 9 | `POST /api/constellations` | ConstellationController | `ConstellationApiTests` | ✅ 422 |
| 10 | `DELETE /api/constellations/{name}` | ConstellationController | `ConstellationApiTests` | ✅ 404 |
| 11 | `GET /api/satellites` | SatelliteController | `SatelliteApiTests` | — |
| 12 | `GET /api/satellites/{id}` | SatelliteController | `SatelliteApiTests` | ✅ 404 |
| 13 | `GET /api/satellites/by-name` | SatelliteController | `SatelliteApiTests` | ✅ 404 |
| 14 | `GET /api/satellites/active` | SatelliteController | `SatelliteApiTests` | — |
| 15 | `GET /api/satellites/by-constellation` | SatelliteController | `SatelliteApiTests` | — |
| 16 | `GET /api/telemetry` | TelemetryController | `TelemetryApiTests` | — |
| 17 | `GET /api/telemetry/{id}` | TelemetryController | `TelemetryApiTests` | ✅ 404 |

Тесты создают собственные данные (группировки/спутники с уникальными
именами `<Префикс>-<uuid8>`) и не полагаются на предварительное состояние
базы — прогон повторяем и не конфликтует ни с демо-данными приложения,
ни с результатами предыдущих запусков.

## Предварительные требования

1. **JDK 21** (проверить: `java -version`).
2. Запущенный экземпляр основного приложения **satellite-spring** —
   см. `README.md` в его репозитории. Проще всего:
   ```bash
   cd satellite-spring
   docker compose up -d --build
   ```
   По умолчанию приложение слушает `http://localhost:8080`.

Интернет для загрузки зависимостей Gradle (Maven Central) нужен только
при самой первой сборке.

## Конфигурация адреса приложения

По умолчанию тесты обращаются к `http://localhost:8080`. Переопределить
можно тремя способами (приоритет сверху вниз), см. `autotests.config.TestConfig`:

```bash
# 1) флаг Gradle
./gradlew test -Dbase.url=http://localhost:9090

# 2) переменная окружения
BASE_URL=http://localhost:9090 ./gradlew test

# 3) правка src/test/resources/config.properties
```

## Запуск тестов

```bash
# Linux / macOS
./gradlew clean test

# Windows
gradlew.bat clean test
```

Результаты Allure по умолчанию складываются в `build/allure-results`
(директорию настраивает плагин `io.qameta.allure`, вручную создавать
не нужно). Консольный вывод показывает статус (`PASSED`/`FAILED`) по
каждому тесту; полные детали запроса/ответа печатаются только для
упавших проверок (см. `BaseApiTest`), чтобы не засорять лог.

## Генерация и просмотр Allure-отчёта

### Вариант A — через Gradle-плагин (рекомендуется, ничего дополнительно ставить не нужно)

```bash
# сгенерировать отчёт и сразу открыть его в браузере
./gradlew allureServe

# либо только сгенерировать статический HTML в build/allure-report
./gradlew allureReport
```

`allureServe` поднимает локальный веб-сервер и открывает отчёт в браузере
по умолчанию. `allureReport` кладёт статические файлы в
`build/reports/allure-report` — их можно открыть напрямую или заархивировать.

### Вариант B — через Allure CLI (если вариант A недоступен в вашей среде)

```bash
npm install -g allure
allure generate build/allure-results -o build/allure-report --clean
allure open build/allure-report
```

(`allure` также можно установить через Homebrew, Scoop или Windows-инсталлятор —
см. официальную документацию https://allurereport.org/docs/install/.)

### Что должно быть на главной странице отчёта

На стартовой странице Allure (виджет "Overview" / "Statistics") видно:
- общее количество тестов и разбивку по статусам (passed / failed / broken / skipped);
- при наличии падений — их количество и текст ошибки в разделе "Categories" / "Suites".

## Структура проекта

```
satellite-spring-autotests/
├── build.gradle.kts
├── settings.gradle.kts
├── src/test/java/autotests/
│   ├── base/BaseApiTest.java        — общая настройка RestAssured + Allure
│   ├── config/TestConfig.java       — определение base.url (см. выше)
│   ├── dto/                         — независимые DTO запросов (JSON-контракт)
│   ├── steps/SatelliteSteps.java    — переиспользуемые @Step-шаги (создание данных)
│   ├── utils/TestDataGenerator.java — генератор уникальных имён для изоляции тестов
│   └── tests/
│       ├── SpaceOperationApiTests.java   — /api/add-satellites, /missions, /deploy,
│       │                                    /overview, /constellations/{}/report,
│       │                                    DELETE /constellations/{}/satellites/{}
│       ├── ConstellationApiTests.java    — CRUD /api/constellations
│       ├── SatelliteApiTests.java        — /api/satellites/*
│       └── TelemetryApiTests.java        — /api/telemetry/*
└── src/test/resources/
    ├── config.properties            — base.url по умолчанию
    └── logback-test.xml             — конфигурация логирования
```

## Устранение неполадок

- **`Connection refused` / тесты падают массово** — приложение не запущено
  или слушает другой порт/хост. Проверьте `curl http://localhost:8080/api/overview`
  и при необходимости переопределите `base.url` (см. выше).
- **`allureServe`/`allureReport` не находит JUnit5-адаптер** — плагин
  `io.qameta.allure` определяет JUnit 5 по classpath автоматически; если по
  какой-то причине это не сработало в вашей версии Gradle/плагина, добавьте
  в `build.gradle.kts` явную зависимость
  `testImplementation("io.qameta.allure:allure-junit5:2.31.0")` и повторите сборку.
- **Тесты телеметрии** (`TelemetryApiTests`) ожидают `temperatureStatus = "NO_DATA"`,
  так как сервис `satellite-telemetry` (источник данных по gRPC) не входит
  в этот учебный проект и не поднимается вместе с `satellite-spring`. Это
  штатное поведение, а не ошибка окружения.
