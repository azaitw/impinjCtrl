package model;

import java.util.ArrayList;

public class TxData {
    private String type;
    private TxDataPayload payload;

    public TxData(String eventId, String raceId) {
        this.payload = new TxDataPayload();
        if (raceId == "") {
            this.type = "txdata";
            this.payload.setRaceId(raceId);
        } else {
            this.type = "txdata_test";
            this.payload.setEventId(eventId);
        }
    }
    public String getType() { return this.type; }
    public TxDataPayload getPayload() { return this.payload; }

    public String getEventId() { return this.payload.eventId; }
    public String getRaceId() { return this.payload.raceId; }
    public void addRecord (Record record) { this.payload.addRecord(record); }
    public void setRecords(ArrayList<Record> records) { this.payload.setRecords(records); }

    private static class TxDataPayload {
        private String eventId;
        private String raceId;
        private ArrayList<Record> records = new ArrayList();
        public void setRecords(ArrayList<Record> records) {
            this.records = records;
        }
        public void addRecord (Record record) { this.records.add(record); }

        public void setEventId (String eventId) { this.eventId = eventId; }
        public void setRaceId (String raceId) { this.raceId = raceId; }
    }
}
