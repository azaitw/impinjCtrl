package model;

public class ReaderStatus {
    private ReaderStatusPayload payload;
    private String type = "readerstatus";

    public ReaderStatus() {
        this.type = "readerstatus";
        this.payload = new ReaderStatusPayload();
    }
    public String getType() { return this.type; }
    public ReaderStatusPayload getPayload() { return this.payload; }

    public String getMessage() { return this.payload.message; }
    public Boolean getError() { return this.payload.error; }
    public Boolean getIsSingulating() { return this.payload.isSingulating; }
    public String getRaceId() { return this.payload.raceId; }
    public Long getStartTime() { return this.payload.startTime; }
    public Long getEndTime() { return this.payload.endTime; }
    public Boolean getIsDebugMode() { return this.payload.debugMode; }
    public String getLogFile() { return this.payload.logFile; }

    public void setMessage(String message) { this.payload.setMessage(message); }
    public void setError(Boolean error) { this.payload.setError(error); }
    public void setIsSingulating(Boolean isSingulating) { this.payload.setIsSingulating(isSingulating); }
    public void setRaceId(String raceId) { this.payload.raceId = raceId; }
    public void setStartTime(Long startTime) { this.payload.setStartTime(startTime); }
    public void setEndTime(Long endTime) { this.payload.setEndTime(endTime); }
    public void setDebugMode(Boolean debugMode) { this.payload.setDebugMode(debugMode); }
    public void setLogFile(String logFile) { this.payload.setLogFile(logFile); }

    private static class ReaderStatusPayload {
        private String message;
        private Boolean error;
        private Boolean isSingulating;
        private String raceId;
        private Long startTime;
        private Long endTime;
        private Boolean debugMode;
        private String logFile;

        public void setMessage (String message) { this.message = message; }
        public void setError(Boolean error) { this.error = error; }
        public void setIsSingulating(Boolean isSingulating) { this.isSingulating = isSingulating; }
        public void setRaceId(String raceId) { this.raceId = raceId; }
        public void setStartTime(Long startTime) { this.startTime = startTime; }
        public void setEndTime(Long endTime) { this.endTime = endTime; }
        public void setDebugMode(Boolean debugMode) { this.debugMode = debugMode; }
        public void setLogFile(String logFile) {
            this.logFile = logFile;
        }
    }
}
