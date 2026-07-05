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
| Отчётность               | Allure Report (плагин `io.qameta.allure` + адаптеры `allure-jupiter`/`allure-rest-assured`) |
| Сериализация JSON        | Jackson Databind                              |
| Логирование              | SLF4J + Logback                               |
| Java                     | 21 (см. про автозагрузку JDK ниже — руками ставить необязательно) |

Node.js/npm для запуска тестов **не требуется**. Он нужен только для одного
из вариантов генерации Allure-отчёта (см. ниже) — и то опционально, есть
путь совсем без него.

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

1. **JDK 21.** Если локально не найдётся подходящей версии — Gradle скачает
   её сам (см. `org.gradle.toolchains.foojay-resolver-convention` в
   `settings.gradle.kts`); для этого нужен обычный доступ в интернет при
   первом запуске. Ставить JDK вручную не обязательно.
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

Результаты Allure складываются в `build/allure-results` — путь зафиксирован
явно в `build.gradle.kts` (и через конфигурацию плагина, и системным
свойством `allure.results.directory`), создавать директорию вручную не нужно.
Консольный вывод показывает статус (`PASSED`/`FAILED`) по каждому тесту;
полные детали запроса/ответа печатаются только для упавших проверок
(см. `BaseApiTest`), чтобы не засорять лог.

> **Windows: кириллица в консоли выглядит как «крякозябры»** (`Ч тючтЁр∙рхЄ...`)?
> Это только отображение в терминале, не в файлах и не в самом Allure-отчёте
> (у него свой UTF-8). Лечится командой `chcp 65001` перед запуском Gradle,
> но на результаты тестов это никак не влияет — можно игнорировать.

## Генерация и просмотр Allure-отчёта

Адаптеры (`allure-jupiter`, `allure-rest-assured`) пишут результаты в
`build/allure-results` независимо от того, чем вы будете строить сам отчёт —
ниже три равноценных варианта, различается только шаг «результаты → HTML».

### Вариант A — автономный Allure CLI, без Node.js (самый надёжный)

Не требует ничего, кроме уже установленной Java:

1. Скачайте `allure-*.zip` со страницы
   [github.com/allure-framework/allure2/releases/latest](https://github.com/allure-framework/allure2/releases/latest)
   (раздел **Assets**) и распакуйте куда угодно, например в `C:\allure-2.44.0`.
2. Постройте и откройте отчёт (поправьте путь на свою папку распаковки):
   ```powershell
   C:\allure-2.44.0\bin\allure.bat generate build\allure-results --clean -o build\allure-report
   C:\allure-2.44.0\bin\allure.bat open build\allure-report
   ```
   Linux/macOS: тот же `bin/allure` без `.bat`.

### Вариант B — через npm (если Node.js уже установлен)

```bash
npm install allure-commandline --no-save   # разово; --no-save — чтобы не плодить package.json
npx allure generate build/allure-results --clean -o build/allure-report
npx allure open build/allure-report
```

### Вариант C — через Gradle-плагин (`allureServe` / `allureReport`)

```bash
./gradlew allureServe
```

Плагин `io.qameta.allure` по умолчанию тянет Allure 3 (npm-пакет `allure`,
качается автоматически при первом запуске). Учтите: Allure 3 спроектирован
под сценарий «прогнать тесты и построить отчёт одной командой» и реагирует
только на данные, появляющиеся **по ходу его собственной работы** — если
тесты уже были запущены отдельно (`./gradlew test`), `allureServe` может
зависнуть или показать пустой отчёт, не подхватив уже готовые результаты.
В этом случае используйте вариант A или B.

### Что должно быть на главной странице отчёта

На стартовой странице Allure (виджет "Overview" / "Statistics") видно:
- общее количество тестов и разбивку по статусам (passed / failed / broken / skipped);
- при наличии падений — их количество и текст ошибки в разделе "Categories" / "Suites".

## Структура проекта

```
satellite-spring-autotests/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
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
- **`error: unmappable character ... for encoding windows-1251`** при
  компиляции — активная кодовая страница Windows не UTF-8, а в исходниках
  есть кириллица. Уже исправлено явным `options.encoding = "UTF-8"` в
  `build.gradle.kts`; если всё равно возникает — добавьте `chcp 65001` перед
  запуском или `-Dfile.encoding=UTF-8` к вызову Gradle.
- **Allure-отчёт пустой, хотя тесты отработали** — проверьте, что результаты
  вообще записались: `Get-ChildItem -Recurse -Filter *-result.json` из корня
  проекта должен найти по одному файлу на тест. Если файлы есть, а отчёт
  пустой — вероятно, вы используете `allureServe`/Allure 3 отдельно от
  прогона тестов (см. вариант C выше); переключитесь на вариант A или B.
- **`allureServe` падает с `Cannot find a Java installation ... toolchain
  download repositories have not been configured`** — на машине нет
  локальной JDK 21, а автозагрузка не подключена. Уже исправлено плагином
  `org.gradle.toolchains.foojay-resolver-convention` в `settings.gradle.kts`
  (Gradle скачает JDK сам при следующем запуске, нужен интернет).
- **`npm`/`npx` не распознаются как команда** — Node.js не установлен.
  Используйте вариант A (автономный Allure CLI, без Node) или поставьте
  Node.js с [nodejs.org](https://nodejs.org) для варианта B.
- **Тесты телеметрии** (`TelemetryApiTests`) ожидают `temperatureStatus = "NO_DATA"`,
  так как сервис `satellite-telemetry` (источник данных по gRPC) не входит
  в этот учебный проект и не поднимается вместе с `satellite-spring`. Это
  штатное поведение, а не ошибка окружения.

### Известная открытая проблема

При прогоне против инстанса `satellite-spring`, который уже успел накопить
много данных (частые перезапуски демо-сида + повторные прогоны тестов),
три теста стабильно падают по коду ответа, а не по содержимому:
`ConstellationApiTests.getAll_returnsListContainingCreatedConstellation`,
`ConstellationApiTests.delete_existingConstellation_returns204`,
`SpaceOperationApiTests.getOverview_returnsSystemSummary`. Все три работают
со всей таблицей группировок целиком, а не по одному имени — рабочая
гипотеза в том, что дело в объёме/состоянии накопленных данных, но это не
подтверждено логами приложения. Точная причина ещё не установлена;
`docker compose down -v` (чистая база) — быстрый способ проверить гипотезу
перед сдачей задания.
