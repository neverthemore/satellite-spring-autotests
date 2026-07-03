package autotests.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Конфигурация автотестов: определяет адрес тестируемого приложения (satellite-spring).
 * <p>
 * Порядок приоритета (первое найденное значение побеждает):
 * <ol>
 *     <li>System property {@code -Dbase.url=...}
 *         (например: {@code ./gradlew test -Dbase.url=http://localhost:9090})</li>
 *     <li>Переменная окружения {@code BASE_URL}</li>
 *     <li>Файл {@code src/test/resources/config.properties}, ключ {@code base.url}</li>
 *     <li>Значение по умолчанию — {@code http://localhost:8080}
 *         (стандартный порт satellite-spring из application.yaml)</li>
 * </ol>
 */
public final class TestConfig {

    private static final String CONFIG_FILE = "config.properties";
    private static final String DEFAULT_BASE_URL = "http://localhost:8080";

    private static final Properties PROPERTIES = loadProperties();

    private TestConfig() {
    }

    public static String getBaseUrl() {
        String systemProperty = System.getProperty("base.url");
        if (isNotBlank(systemProperty)) {
            return trimTrailingSlash(systemProperty);
        }

        String envVar = System.getenv("BASE_URL");
        if (isNotBlank(envVar)) {
            return trimTrailingSlash(envVar);
        }

        String fromFile = PROPERTIES.getProperty("base.url");
        if (isNotBlank(fromFile)) {
            return trimTrailingSlash(fromFile);
        }

        return DEFAULT_BASE_URL;
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = TestConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось загрузить " + CONFIG_FILE, e);
        }
        return properties;
    }

    private static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    private static String trimTrailingSlash(String value) {
        String trimmed = value.trim();
        return trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
    }
}
