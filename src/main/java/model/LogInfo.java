package model;

public class LogInfo {
    private String eventId;
    private String raceId;
    public LogInfo(String eventId, String raceId) {
        this.eventId = eventId;
        this.raceId = raceId;
    }
    public String getEventId() { return this.eventId; }
    public String getRaceId() { return this.raceId; }
}
