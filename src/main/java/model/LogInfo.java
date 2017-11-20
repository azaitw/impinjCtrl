package model;

public class LogInfo {
    private String eventId;
    private String raceId;
    private Long startTime;
    private Long endTime;
    public LogInfo(String eventId, String raceId) {
        this.eventId = eventId;
        this.raceId = raceId;
    }
    public String getEventId() { return this.eventId; }
    public String getRaceId() { return this.raceId; }
    public Long getStartTime() { return this.startTime; }
    public Long getEndTime() { return this.endTime; }
    public void setStartTime(Long startTime) { this.startTime = startTime; }
    public void setEndTime(Long endTime) { this.endTime = endTime; }
}
