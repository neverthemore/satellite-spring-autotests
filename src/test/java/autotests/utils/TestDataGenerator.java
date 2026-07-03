package autotests.utils;

import java.util.UUID;

/**
 * Генератор уникальных имён для группировок и спутников.
 * <p>
 * Зачем это нужно: тестируемое приложение хранит данные в PostgreSQL,
 * которая переживает перезапуски, а {@code Main.main()} досевает демо-данные
 * ("Орбита-1", "Орбита-2") при каждом старте. Имена группировок уникальны
 * в БД ({@code UNIQUE} по {@code constellation_name}), поэтому повторный
 * запуск автотестов с одними и теми же именами привёл бы к 422 там,
 * где ожидается 201/200. Суффикс UUID гарантирует, что каждый прогон
 * тестов создаёт свои собственные данные и не конфликтует ни с предыдущими
 * прогонами, ни с демо-данными приложения.
 */
public final class TestDataGenerator {

    private TestDataGenerator() {
    }

    public static String uniqueConstellationName(String prefix) {
        return prefix + "-" + shortId();
    }

    public static String uniqueSatelliteName(String prefix) {
        return prefix + "-" + shortId();
    }

    private static String shortId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
