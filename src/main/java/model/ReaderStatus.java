package model;

public class ReaderStatus {
    private ReaderStatusPayload payload;
    private String type;

    public ReaderStatus() {
        this.type = "readerstatus";
        this.payload = new ReaderStatusPayload();
    }
    public String getType() { return this.type; }
    public ReaderStatusPayload getPayload() { return this.payload; }

    public String getMessage() { return this.payload.message; }
    public Boolean getError() { return this.payload.error; }
    public Boolean getIsSingulating() { return this.payload.isSingulating; }
    public String getLogFile() { return this.payload.logFile; }

    public void setMessage(String message) { this.payload.setMessage(message); }
    public void setError(Boolean error) { this.payload.setError(error); }
    public void setIsSingulating(Boolean isSingulating) { this.payload.setIsSingulating(isSingulating); }
    public void setLogFile(String logFile) { this.payload.setLogFile(logFile); }

    private static class ReaderStatusPayload {
        private String message;
        private Boolean error;
        private Boolean isSingulating;
        private String logFile;

        public void setMessage (String message) { this.message = message; }
        public void setError(Boolean error) { this.error = error; }
        public void setIsSingulating(Boolean isSingulating) { this.isSingulating = isSingulating; }
        public void setLogFile(String logFile) {
            this.logFile = logFile;
        }
    }
}
