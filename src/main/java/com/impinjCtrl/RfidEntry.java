package com.impinjCtrl;

import org.json.simple.JSONObject;

public class RfidEntry {
    private String epc = "";
    private Long timestamp;
    private Short ant;
    private Double signal;

    RfidEntry(String epc, Long timestamp, Short ant, Double signal){
        this.epc = epc;
        this.timestamp = timestamp;
        this.ant = ant;
        this.signal = signal;
    }
    private String getEpc() { return epc; }
    private Long getTimestamp() { return timestamp; }
    private Short getAnt() { return ant; }
    private Double getSignal() { return signal; }

    public JSONObject getRfidEntryForRace() {
        JSONObject result = new JSONObject();
        result.put("epc", getEpc());
        result.put("timestamp", getTimestamp());
        return result;
    }
    public JSONObject getRfidEntryForTest() {
        JSONObject result = new JSONObject();
        result.put("epc", getEpc());
        result.put("timestamp", getTimestamp());
        result.put("ant", getAnt());
        result.put("signal", getSignal());
        return result;
    }
}
