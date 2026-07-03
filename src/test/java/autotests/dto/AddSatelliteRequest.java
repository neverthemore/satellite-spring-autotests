package autotests.dto;

import java.util.List;

/**
 * Тело запроса {@code POST /api/add-satellites} и {@code POST /api/deploy}.
 * Повторяет JSON-контракт {@code seminars.facade.AddSatelliteRequest}.
 * <p>
 * {@code satelliteParams} намеренно типизирован как {@code List<Object>},
 * так как список может содержать смесь {@link CommunicationSatelliteParam}
 * и {@link ImagingSatelliteParam} — Jackson сериализует каждый элемент
 * согласно его собственному классу.
 */
public class AddSatelliteRequest {

    private String constellationName;
    private List<Object> satelliteParams;

    public AddSatelliteRequest(String constellationName, List<Object> satelliteParams) {
        this.constellationName = constellationName;
        this.satelliteParams = satelliteParams;
    }

    public String getConstellationName() {
        return constellationName;
    }

    public List<Object> getSatelliteParams() {
        return satelliteParams;
    }
}
