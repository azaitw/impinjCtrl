package com.impinjCtrl;

import org.json.JSONArray;
import org.json.simple.JSONObject;
import lib.RequestManager;
import lib.Logging;

public class RfidEntryManager {
    private String eventId = "";
    private String raceId = "";
    private String mode = "";

    public static RfidEntryManager instance;
    public static RfidEntryManager getInstance() {
        if (instance == null) { instance = new RfidEntryManager(); }
        return instance;
    }
    public void setEventId (String eventId) { this.eventId = eventId; }
    public void setRaceId (String raceId) { this.raceId = raceId; }
    public void setMode (String mode) { this.mode = mode; }

    public void addEntry(String epc, Long timestamp, Short ant, Double signal) {
        RfidEntry record = new RfidEntry(epc, timestamp, ant, signal);
        JSONObject recordInJson;
        Logging logger = Logging.getInstance();
        if (mode == "race") {
            recordInJson = record.getRfidEntryForRace();
        } else {
            recordInJson = record.getRfidEntryForTest();
        }
        sendEntryToAPI(recordInJson);
        logger.addEntry(recordInJson.toJSONString());
    }
    private void sendEntryToAPI (JSONObject entry) {
        JSONObject result = new JSONObject();
        String output = "";
        JSONObject payload = new JSONObject();
        JSONArray records = new JSONArray();
        RequestManager requestManager = RequestManager.getInstance();
        try {
            records.put(entry);
            payload.put("mode", mode);
            payload.put("event", eventId);
            payload.put("race", raceId);
            payload.put("records", records);
            result.put("type", "rxdata");
            result.put("recordType", "partial");
            result.put("payload", payload);
            output = result.toJSONString();
        } catch (Exception e) {
            System.out.println("getEntryForSocketIO JSON error: " + e.getMessage());
        }
        requestManager.sendResult(output);
    }
}
