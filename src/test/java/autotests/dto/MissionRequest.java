package autotests.dto;

/**
 * Тело запроса {@code POST /api/missions}.
 * Повторяет JSON-контракт {@code seminars.facade.MissionRequest}.
 */
public class MissionRequest {

    private String constellationName;
    private boolean activateBeforeMission;

    public MissionRequest(String constellationName, boolean activateBeforeMission) {
        this.constellationName = constellationName;
        this.activateBeforeMission = activateBeforeMission;
    }

    public String getConstellationName() {
        return constellationName;
    }

    public boolean isActivateBeforeMission() {
        return activateBeforeMission;
    }
}
