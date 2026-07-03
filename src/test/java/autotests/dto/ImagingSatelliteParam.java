package autotests.dto;

/**
 * Параметр создания спутника дистанционного зондирования Земли (ДЗЗ).
 * <p>
 * Повторяет JSON-контракт {@code seminars.factory.ImagingSatelliteParam}
 * основного приложения. Поле {@code type} — дискриминатор полиморфной
 * десериализации Jackson ({@code @JsonTypeInfo}) на стороне сервера.
 */
public class ImagingSatelliteParam {

    private final String type = "IMAGE";
    private String name;
    private double batteryLevel;
    private double resolution;

    public ImagingSatelliteParam(String name, double batteryLevel, double resolution) {
        this.name = name;
        this.batteryLevel = batteryLevel;
        this.resolution = resolution;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public double getBatteryLevel() {
        return batteryLevel;
    }

    public double getResolution() {
        return resolution;
    }
}
