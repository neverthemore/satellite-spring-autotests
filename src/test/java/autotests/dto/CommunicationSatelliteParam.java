package autotests.dto;

/**
 * Параметр создания спутника связи.
 * <p>
 * Повторяет JSON-контракт {@code seminars.factory.CommunicationSatelliteParam}
 * основного приложения. Поле {@code type} — дискриминатор полиморфной
 * десериализации Jackson ({@code @JsonTypeInfo}) на стороне сервера.
 * <p>
 * Тестовый проект независим от основного: классы не переиспользуются напрямую,
 * а только повторяют согласованную JSON-структуру.
 */
public class CommunicationSatelliteParam {

    private final String type = "COMMUNICATION";
    private String name;
    private double batteryLevel;
    private double bandwidth;

    public CommunicationSatelliteParam(String name, double batteryLevel, double bandwidth) {
        this.name = name;
        this.batteryLevel = batteryLevel;
        this.bandwidth = bandwidth;
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

    public double getBandwidth() {
        return bandwidth;
    }
}
