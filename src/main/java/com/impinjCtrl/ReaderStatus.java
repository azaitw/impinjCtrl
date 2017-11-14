package com.impinjCtrl;

import org.json.simple.JSONObject;

public class ReaderStatus {
    private Boolean isSingulating = false;
    private Boolean hasError = false;
    private String message = "";
    private String logFile = "";

    public static ReaderStatus instance;
    public static ReaderStatus getInstance() {
        if (instance == null) { instance = new ReaderStatus(); }
        return instance;
    }
    private Boolean getIsSingulating () { return isSingulating; }
    private Boolean getHasError () { return hasError; }
    private String getMessage () { return message; }
    private String getLogFile () { return logFile; }

    public void setIsSingulating (Boolean isSingulating) { this.isSingulating = isSingulating; }
    public void setHasError(Boolean hasError) { this.hasError = hasError; }
    public void setMessage(String message) { this.message = message; }
    public void setLogFile (String logFile) { this.logFile = logFile; }

    public String getReaderStatusInJSONString () {
        JSONObject result = new JSONObject();
        JSONObject payload = new JSONObject();
        payload.put("isSingulating", getIsSingulating());
        payload.put("logFile", getLogFile());
        result.put("type", "readerstatus");
        result.put("error", getHasError());
        result.put("message", getMessage());
        result.put("payload", payload);
        return result.toJSONString();
    }
}
